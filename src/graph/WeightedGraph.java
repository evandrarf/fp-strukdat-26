package graph;

import model.RouteEdge;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WeightedGraph {
    private final Map<String, List<RouteEdge>> adjacency = new LinkedHashMap<>();

    public void clear() {
        adjacency.clear();
    }

    public void addCheckpoint(String checkpointId) {
        adjacency.putIfAbsent(checkpointId, new ArrayList<>());
    }

    public void addRoute(RouteEdge route) {
        addCheckpoint(route.getFromId());
        addCheckpoint(route.getToId());
        adjacency.get(route.getFromId()).add(route);
        adjacency.get(route.getToId()).add(route);
    }

    public List<RouteEdge> getRoutesFrom(String checkpointId) {
        return adjacency.getOrDefault(checkpointId, List.of());
    }

    public Map<String, List<RouteEdge>> getAdjacency() {
        return adjacency;
    }
}
