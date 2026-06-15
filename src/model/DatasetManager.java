package model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class DatasetManager {
    private enum Section {
        NONE,
        TERRAIN_WEIGHTS,
        CHECKPOINTS,
        ROUTES
    }

    public GameMapData load(String datasetPath) {
        try {
            List<String> lines = Files.readAllLines(Path.of(datasetPath));
            List<Checkpoint> checkpoints = new ArrayList<>();
            List<RouteEdge> routes = new ArrayList<>();
            EnumMap<TerrainType, Integer> terrainWeights = defaultTerrainWeights();

            Section currentSection = Section.NONE;
            for (String rawLine : lines) {
                String line = rawLine.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                if (line.startsWith("[") && line.endsWith("]")) {
                    currentSection = Section.valueOf(line.substring(1, line.length() - 1));
                    continue;
                }

                switch (currentSection) {
                    case TERRAIN_WEIGHTS -> parseTerrainWeight(line, terrainWeights);
                    case CHECKPOINTS -> checkpoints.add(parseCheckpoint(line));
                    case ROUTES -> routes.add(parseRoute(line));
                    default -> throw new IllegalArgumentException("Baris dataset di luar section: " + line);
                }
            }

            return new GameMapData(checkpoints, routes, terrainWeights);
        } catch (IOException exception) {
            throw new IllegalStateException("Gagal membaca dataset: " + datasetPath, exception);
        }
    }

    public void save(String datasetPath, List<Checkpoint> checkpoints, List<RouteEdge> routes,
                     Map<TerrainType, Integer> terrainWeights) {
        List<String> lines = new ArrayList<>();
        lines.add("[TERRAIN_WEIGHTS]");
        for (TerrainType type : TerrainType.values()) {
            lines.add(type.name() + "=" + terrainWeights.get(type));
        }
        lines.add("");
        lines.add("[CHECKPOINTS]");
        checkpoints.stream()
                .sorted(Comparator.comparing(Checkpoint::getId))
                .map(Checkpoint::toDatasetLine)
                .forEach(lines::add);
        lines.add("");
        lines.add("[ROUTES]");
        routes.stream()
                .sorted(Comparator.comparing(RouteEdge::getFromId).thenComparing(RouteEdge::getToId))
                .map(RouteEdge::toDatasetLine)
                .forEach(lines::add);

        try {
            Files.write(Path.of(datasetPath), lines);
        } catch (IOException exception) {
            throw new IllegalStateException("Gagal menyimpan dataset ke " + datasetPath, exception);
        }
    }

    private void parseTerrainWeight(String line, EnumMap<TerrainType, Integer> terrainWeights) {
        String[] parts = line.split("=");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Format terrain weight tidak valid: " + line);
        }

        TerrainType terrainType = TerrainType.fromString(parts[0]);
        int weight = Integer.parseInt(parts[1].trim());
        terrainWeights.put(terrainType, weight);
    }

    private Checkpoint parseCheckpoint(String line) {
        String[] parts = line.split(";");
        if (parts.length != 9) {
            throw new IllegalArgumentException("Format checkpoint tidak valid: " + line);
        }

        return new Checkpoint(
                parts[0].trim(),
                parts[1].trim(),
                Integer.parseInt(parts[2].trim()),
                Integer.parseInt(parts[3].trim()),
                parts[4].trim(),
                Integer.parseInt(parts[5].trim()),
                parts[6].trim(),
                Integer.parseInt(parts[7].trim()),
                Boolean.parseBoolean(parts[8].trim())
        );
    }

    private RouteEdge parseRoute(String line) {
        String[] parts = line.split(";");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Format route tidak valid: " + line);
        }

        return new RouteEdge(
                parts[0].trim(),
                parts[1].trim(),
                TerrainType.fromString(parts[2]),
                Integer.parseInt(parts[3].trim())
        );
    }

    private EnumMap<TerrainType, Integer> defaultTerrainWeights() {
        EnumMap<TerrainType, Integer> terrainWeights = new EnumMap<>(TerrainType.class);
        for (TerrainType type : TerrainType.values()) {
            terrainWeights.put(type, type.getDefaultWeight());
        }
        return terrainWeights;
    }
}
