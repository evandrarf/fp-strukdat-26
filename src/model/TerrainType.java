package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public enum TerrainType {
    GROUND(1, false),
    FOREST(2, false),
    SAND(3, false),
    WATER(4, false),
    LAVA(6, false),
    WALL(Integer.MAX_VALUE, true);

    private final int defaultWeight;
    private final boolean impassable;

    TerrainType(int defaultWeight, boolean impassable) {
        this.defaultWeight = defaultWeight;
        this.impassable = impassable;
    }

    public int getDefaultWeight() {
        return defaultWeight;
    }

    public boolean isImpassable() {
        return impassable;
    }

    public static TerrainType fromString(String value) {
        return TerrainType.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    public static List<String> names() {
        List<String> names = new ArrayList<>();
        for (TerrainType type : values()) {
            names.add(type.name());
        }
        return names;
    }
}
