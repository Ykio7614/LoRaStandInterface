#!/usr/bin/env python3
import sys
import json
import os
import folium
import traceback

def create_map(json_file, output_file):
    """Создает интерактивную карту с точками из JSON файла"""
    try:
        print(f"[INFO] Загружаем данные из {json_file}...")
        with open(json_file, 'r', encoding='utf-8') as f:
            packets = json.load(f)
        print(f"[INFO] Загружено пакетов: {len(packets)}")

        # Фильтруем пакеты с координатами
        packets_with_coords = [
            p for p in packets
            if isinstance(p.get('latitude'), (int, float)) and isinstance(p.get('longitude'), (int, float))
        ]
        print(f"[INFO] Пакетов с координатами: {len(packets_with_coords)}")

        if not packets_with_coords:
            print("[ERROR] В файле JSON не найдено координат")
            return 1

        first_point = packets_with_coords[0]
        print("[INFO] Создаем карту...")

        # CartoDB Light (через Fastly) 
        tiles = "https://cartodb-basemaps-a.global.ssl.fastly.net/light_all/{z}/{x}/{y}.png"
        attr = "© OpenStreetMap contributors, © CartoDB"

        m = folium.Map(
            location=[first_point['latitude'], first_point['longitude']],
            zoom_start=13,
            tiles=tiles,
            attr=attr,
            control_scale=True
        )
        print("[INFO] Карта создана")

        def fmt(value, precision=2):
            if value is None:
                return "-"
            try:
                return f"{float(value):.{precision}f}"
            except (ValueError, TypeError):
                return "-"

        for idx, packet in enumerate(packets_with_coords, start=1):
            distance = fmt(packet.get('distance'), 2)
            rssi = fmt(packet.get('rssi'), 0)
            snr = fmt(packet.get('snr'), 2)

            popup_text = f"""
            <div style="font-family: Arial; max-width: 300px;">
                <b>Время:</b> {packet.get('datetime', '-')}<br>
                <b>Координаты:</b> {fmt(packet.get('latitude'), 6)}, {fmt(packet.get('longitude'), 6)}<br>
                <b>Расстояние:</b> {distance} м<br>
                <b>RSSI:</b> {rssi} dBm<br>
                <b>SNR:</b> {snr} dB
            </div>
            """

            color = "blue"
            rssi_value = packet.get("rssi")
            if rssi_value is not None:
                try:
                    rssi_float = float(rssi_value)
                    if rssi_float >= -80:
                        color = "green"
                    elif rssi_float >= -100:
                        color = "orange"
                    else:
                        color = "red"
                except (ValueError, TypeError):
                    color = "blue"

            folium.Marker(
                location=[packet['latitude'], packet['longitude']],
                popup=folium.Popup(popup_text, max_width=350),
                tooltip=f"Расстояние: {distance} м, RSSI: {rssi} dBm",
                icon=folium.Icon(color=color, icon='info-sign')
            ).add_to(m)

            if idx % 50 == 0 or idx == len(packets_with_coords):
                print(f"[INFO] Добавлено маркеров: {idx}/{len(packets_with_coords)}")

        footer_html = """
        <div style='position: fixed; bottom: 0; left: 0; width: 100%; height: 40px;
                    background-color: white; z-index: 1000;'></div>
        """
        m.get_root().html.add_child(folium.Element(footer_html))

        out_dir = os.path.dirname(output_file)
        if out_dir:
            os.makedirs(out_dir, exist_ok=True)

        print(f"[INFO] Сохраняем карту в {output_file}...")
        m.save(output_file)
        print("[SUCCESS] Карта успешно сохранена!")
        return 0

    except json.JSONDecodeError as e:
        print(f"[ERROR] Ошибка в JSON файле: {e}")
        return 1
    except FileNotFoundError:
        print(f"[ERROR] Файл не найден: {json_file}")
        return 1
    except Exception:
        print("[ERROR] Ошибка при создании карты:")
        traceback.print_exc()
        return 1


if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: python3 map_generator.py <input_json> <output_html>")
        sys.exit(1)

    input_json = sys.argv[1]
    output_html = sys.argv[2]
    sys.exit(create_map(input_json, output_html))
