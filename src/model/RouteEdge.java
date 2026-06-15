package model;

import java.util.Map;

public class RouteEdge {
    private final String fromId;
    private final String toId;
    private final TerrainType terrainType;
    private final int baseCost;

    public RouteEdge(String fromId, String toId, TerrainType terrainType, int baseCost) {
        this.fromId = fromId;
        this.toId = toId;
        this.terrainType = terrainType;
        this.baseCost = baseCost;
    }

    public String getFromId() {
        return fromId;
    }

    public String getToId() {
        return toId;
    }

    public TerrainType getTerrainType() {
        return terrainType;
    }

    public int getBaseCost() {
        return baseCost;
    }

    public String getOther(String checkpointId) {
        if (fromId.equalsIgnoreCase(checkpointId)) {
            return toId;
        }
        if (toId.equalsIgnoreCase(checkpointId)) {
            return fromId;
        }
        return null;
    }

    public boolean connects(String firstId, String secondId) {
        return (fromId.equalsIgnoreCase(firstId) && toId.equalsIgnoreCase(secondId))
                || (fromId.equalsIgnoreCase(secondId) && toId.equalsIgnoreCase(firstId));
    }

    public int getTraversalCost(Map<TerrainType, Integer> terrainWeights) {
        if (terrainType.isImpassable()) {
            return Integer.MAX_VALUE;
        }

        Integer weight = terrainWeights.get(terrainType);
        if (weight == null || weight <= 0) {
            return Integer.MAX_VALUE;
        }
        return baseCost * weight;
    }

    public String toDisplayLine(Map<TerrainType, Integer> terrainWeights) {
        int traversalCost = getTraversalCost(terrainWeights);
        String costText = traversalCost == Integer.MAX_VALUE ? "IMPASSABLE" : String.valueOf(traversalCost);
        return String.format("%s <-> %s | terrain=%s | baseCost=%d | totalCost=%s",
                fromId, toId, terrainType.name(), baseCost, costText);
    }

    public String toDatasetLine() {
        return String.join(";",
                fromId,
                toId,
                terrainType.name(),
                String.valueOf(baseCost)
        );
    }
}
