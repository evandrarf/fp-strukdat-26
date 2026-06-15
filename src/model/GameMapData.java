package model;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class GameMapData {
    private final List<Checkpoint> checkpoints;
    private final List<RouteEdge> routes;
    private final EnumMap<TerrainType, Integer> terrainWeights;

    public GameMapData(List<Checkpoint> checkpoints, List<RouteEdge> routes,
                       EnumMap<TerrainType, Integer> terrainWeights) {
        this.checkpoints = checkpoints;
        this.routes = routes;
        this.terrainWeights = terrainWeights;
    }

    public List<Checkpoint> getCheckpoints() {
        return checkpoints;
    }

    public List<RouteEdge> getRoutes() {
        return routes;
    }

    public Map<TerrainType, Integer> getTerrainWeights() {
        return terrainWeights;
    }
}
