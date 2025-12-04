#!/usr/bin/env python3
import sys
import json
import os
import folium
import traceback

def create_map(json_file, output_file):
    """Создает интерактивную карту с точками из JSON файла"""
    try:
        print(f"[DEBUG] Loading data from {json_file}...")
        with open(json_file, 'r', encoding='utf-8') as f:
            packets = json.load(f)
        print(f"[DEBUG] Total packets loaded: {len(packets)}")

        # Оставляем только с координатами
        packets_with_coords = [
            p for p in packets
            if isinstance(p.get('latitude'), (int, float)) and isinstance(p.get('longitude'), (int, float))
        ]
        print(f"[DEBUG] Packets with coordinates: {len(packets_with_coords)}")

        if not packets_with_coords:
            print("[ERROR] No coordinates found in JSON file")
            return 1

        first_point = packets_with_coords[0]
        print("[DEBUG] Creating map object...")
        m = folium.Map(
            location=[first_point['latitude'], first_point['longitude']],
            zoom_start=13
        )
        print("[DEBUG] Map object created")

        for idx, packet in enumerate(packets_with_coords, start=1):
            # Безопасное форматирование чисел
            def fmt(value, precision=2):
                return f"{value:.{precision}f}" if isinstance(value, (int, float)) else "-"

            distance = fmt(packet.get('distance'))
            rssi = fmt(packet.get('rssi'), 0)
            snr = fmt(packet.get('snr'), 2)
            bit_errors = fmt(packet.get('bit_errors'), 0)
            sf = fmt(packet.get('sf'), 0)
            tx = fmt(packet.get('tx'), 0)
            bw = fmt(packet.get('bw'), 2)

            popup_text = f"""
            <b>Время:</b> {packet.get('datetime', '-')}<br>
            <b>Расстояние:</b> {distance} м<br>
            <b>RSSI:</b> {rssi}<br>
            <b>SNR:</b> {snr}<br>
            <b>Ошибки:</b> {bit_errors}<br>
            <b>SF:</b> {sf}<br>
            <b>Tx:</b> {tx}<br>
            <b>BW:</b> {bw}
            """
            folium.Marker(
                location=[packet['latitude'], packet['longitude']],
                popup=folium.Popup(popup_text, max_width=300),
                tooltip=f"Расстояние: {distance} м"
            ).add_to(m)
            print(f"[DEBUG] Added marker {idx}/{len(packets_with_coords)}")

        # Добавляем белую полоску снизу
        footer_html = """
        <div style='position: fixed; bottom: 0; left: 0; width: 100%; height: 40px; background-color: white; z-index: 1000;'></div>
        """
        m.get_root().html.add_child(folium.Element(footer_html))

        # Создаем директорию и сохраняем HTML
        os.makedirs(os.path.dirname(output_file), exist_ok=True)
        print(f"[DEBUG] Saving map to {output_file}...")
        m.save(output_file)
        print("[DEBUG] Map saved successfully!")
        return 0

    except Exception as e:
        print("[ERROR] Exception occurred:")
        traceback.print_exc()
        return 1

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: python3 map_generator.py <input_json> <output_html>")
        sys.exit(1)

    input_json = sys.argv[1]
    output_html = sys.argv[2]
    exit_code = create_map(input_json, output_html)
    sys.exit(exit_code)
