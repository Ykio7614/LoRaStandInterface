package com.loramaster.ui;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.styling.*;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.JMapPane;
import org.locationtech.jts.geom.*;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import javax.swing.*;
import java.awt.Color;
import java.awt.BorderLayout;
import java.io.File;
import java.util.Collections;

public class MapPanel extends JPanel {

    private static final String DATA_DIR = "D:/map/try_map_geo/shapefiles/spb_city/data";

    public MapPanel() {
        super(new BorderLayout());

        try {
            File folder = new File(DATA_DIR);
            if (!folder.exists()) {
                System.err.println("Нет папки: " + DATA_DIR);
                add(new JLabel("Нет папки: " + DATA_DIR), BorderLayout.CENTER);
                return;
            }

            MapContent map = new MapContent();
            map.setTitle("СПб + тепловые зоны");

            File[] shpFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".shp"));
            if (shpFiles == null || shpFiles.length == 0) {
                System.err.println("Нет .shp файлов в папке " + DATA_DIR);
                add(new JLabel("Нет .shp файлов в папке " + DATA_DIR), BorderLayout.CENTER);
                return;
            }

            StyleBuilder sb = new StyleBuilder();

            for (File shp : shpFiles) {
                FileDataStore store = FileDataStoreFinder.getDataStore(shp);
                SimpleFeatureSource featureSource = store.getFeatureSource();
                String geomType = featureSource.getSchema().getGeometryDescriptor().getType().getBinding().getSimpleName();

                Style style;
                if (geomType.equalsIgnoreCase("Point") || geomType.equalsIgnoreCase("MultiPoint")) {
                    style = createPointStyle(sb);
                } else if (geomType.equalsIgnoreCase("LineString") || geomType.equalsIgnoreCase("MultiLineString")) {
                    style = createLineStyle(sb);
                } else if (geomType.equalsIgnoreCase("Polygon") || geomType.equalsIgnoreCase("MultiPolygon")) {
                    style = createPolygonStyle(sb);
                } else {
                    style = SLD.createSimpleStyle(featureSource.getSchema());
                }

                Layer layer = new FeatureLayer(featureSource, style);
                map.addLayer(layer);
            }

            // 🔥 Добавляем тепловую область в районе (59.94, 30.46)
            Layer heatLayer = createHeatLayer(59.94, 30.46, 2000); // радиус ~2000 м
            map.addLayer(heatLayer);

            // --- вместо JMapFrame.showMap(map) — создаём JMapFrame и берем из него JMapPane ---
            JMapFrame mapFrame = new JMapFrame(map);
            mapFrame.enableToolBar(true);
            mapFrame.enableStatusBar(true);

            // достаём тулбар (если нужен) и панель карты
            JToolBar toolBar = mapFrame.getToolBar();
            JMapPane mapPane = mapFrame.getMapPane();

            if (toolBar != null) {
                add(toolBar, BorderLayout.NORTH);
            }
            add(mapPane, BorderLayout.CENTER);
            // не делаем mapFrame.setVisible(true) — окно не открываем, только используем его компоненты

        } catch (Exception e) {
            e.printStackTrace();
            add(new JLabel("Ошибка загрузки карты: " + e.getMessage()), BorderLayout.CENTER);
        }
    }

    /** Стиль для линий */
    private static Style createLineStyle(StyleBuilder sb) {
        Stroke stroke = sb.createStroke(Color.BLUE, 1.5);
        LineSymbolizer sym = sb.createLineSymbolizer(stroke);
        return sb.createStyle(sym);
    }

    /** Стиль для полигонов */
    private static Style createPolygonStyle(StyleBuilder sb) {
        Stroke stroke = sb.createStroke(Color.BLACK, 0.8);
        Fill fill = sb.createFill(Color.CYAN, 0.4);
        PolygonSymbolizer sym = sb.createPolygonSymbolizer(stroke, fill);
        return sb.createStyle(sym);
    }

    /** Стиль для точек */
    private static Style createPointStyle(StyleBuilder sb) {
        Graphic graphic = sb.createGraphic(
                null,
                sb.createMark(StyleBuilder.MARK_CIRCLE, Color.RED, Color.BLACK, 1),
                null
        );
        graphic.setSize(sb.literalExpression(6));
        PointSymbolizer sym = sb.createPointSymbolizer(graphic);
        return sb.createStyle(sym);
    }

    /** Создание слоя с "тепловой областью" */
    private static Layer createHeatLayer(double lat, double lon, double radiusMeters) throws Exception {
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();

        // создаем круг вокруг координат
        Coordinate coord = new Coordinate(lon, lat); // ⚠️ порядок: X=lon, Y=lat
        Point center = geometryFactory.createPoint(coord);

        // превращаем точку в круг (buffer = радиус в градусах, примерно)
        // 1 градус ≈ 111 км, поэтому переводим метры → градусы
        double radiusDegrees = radiusMeters / 111000.0;
        Polygon circle = (Polygon) center.buffer(radiusDegrees);

        // создаём тип
        SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
        tb.setName("HeatArea");
        tb.add("the_geom", Polygon.class);
        SimpleFeatureType TYPE = tb.buildFeatureType();

        SimpleFeature feature = SimpleFeatureBuilder.build(TYPE, new Object[]{circle}, null);

        ListFeatureCollection collection = new ListFeatureCollection(TYPE, Collections.singletonList(feature));

        // стиль — прозрачная красная заливка (это тоже оставить так)
        StyleBuilder sb = new StyleBuilder();
        Fill fill = sb.createFill(new Color(255, 0, 0, 50)); // полупрозрачный красный
        fill.setOpacity(sb.literalExpression(0.5)); // 20% непрозрачности
        Stroke stroke = sb.createStroke(new Color(255, 0, 0, 120), 1.0);
        PolygonSymbolizer sym = sb.createPolygonSymbolizer(stroke, fill);
        Style style = sb.createStyle(sym);

        return new FeatureLayer(collection, style);
    }
}
