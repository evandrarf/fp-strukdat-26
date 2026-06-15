package graph;

import model.Checkpoint;
import model.DatasetManager;
import model.GameMapData;
import model.HeuristicComparison;
import model.PathResult;
import model.RouteEdge;
import model.TerrainType;
import tree.BinaryMinHeap;
import tree.CheckpointMaxHeap;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class GameMapService {
    private final String datasetPath;
    private final DatasetManager datasetManager;
    private final Map<String, Checkpoint> checkpoints;
    private final List<RouteEdge> routes;
    private final EnumMap<TerrainType, Integer> terrainWeights;
    private final WeightedGraph graph;
    private final CheckpointMaxHeap dangerHeap;

    public GameMapService(String datasetPath) {
        this.datasetPath = datasetPath;
        this.datasetManager = new DatasetManager();
        this.checkpoints = new LinkedHashMap<>();
        this.routes = new ArrayList<>();
        this.terrainWeights = new EnumMap<>(TerrainType.class);
        this.graph = new WeightedGraph();
        this.dangerHeap = new CheckpointMaxHeap();
        loadDataset();
    }

    public String getDatasetSummary() {
        return "Checkpoint: " + checkpoints.size()
                + " | Route: " + routes.size()
                + " | Terrain types: " + terrainWeights.size()
                + " | Tree: Custom Max Heap"
                + " | Algorithms: A*, BFS";
    }

    public List<Checkpoint> getAllCheckpoints() {
        return new ArrayList<>(checkpoints.values());
    }

    public Checkpoint getCheckpoint(String id) {
        return checkpoints.get(normalizeId(id));
    }

    public List<Checkpoint> searchCheckpoints(String query) {
        List<Checkpoint> results = new ArrayList<>();
        for (Checkpoint checkpoint : checkpoints.values()) {
            if (checkpoint.matchesQuery(query)) {
                results.add(checkpoint);
            }
        }
        return results;
    }

    public void addCheckpoint(Checkpoint checkpoint) {
        validateCheckpoint(checkpoint, null);
        checkpoints.put(normalizeId(checkpoint.getId()), checkpoint);
        rebuildStructures();
    }

    public void updateCheckpoint(String originalId, Checkpoint updatedCheckpoint) {
        String normalizedOriginalId = normalizeId(originalId);
        if (!checkpoints.containsKey(normalizedOriginalId)) {
            throw new IllegalArgumentException("Checkpoint tidak ditemukan.");
        }

        validateCheckpoint(updatedCheckpoint, normalizedOriginalId);
        checkpoints.remove(normalizedOriginalId);
        checkpoints.put(normalizeId(updatedCheckpoint.getId()), updatedCheckpoint);

        for (int index = 0; index < routes.size(); index++) {
            RouteEdge route = routes.get(index);
            String from = route.getFromId().equalsIgnoreCase(normalizedOriginalId)
                    ? updatedCheckpoint.getId()
                    : route.getFromId();
            String to = route.getToId().equalsIgnoreCase(normalizedOriginalId)
                    ? updatedCheckpoint.getId()
                    : route.getToId();
            routes.set(index, new RouteEdge(from, to, route.getTerrainType(), route.getBaseCost()));
        }

        rebuildStructures();
    }

    public boolean deleteCheckpoint(String id) {
        String normalizedId = normalizeId(id);
        Checkpoint removed = checkpoints.remove(normalizedId);
        if (removed == null) {
            return false;
        }

        routes.removeIf(route -> route.getFromId().equalsIgnoreCase(normalizedId)
                || route.getToId().equalsIgnoreCase(normalizedId));
        rebuildStructures();
        return true;
    }

    public RouteEdge getRoute(String fromId, String toId) {
        for (RouteEdge route : routes) {
            if (route.connects(fromId, toId)) {
                return route;
            }
        }
        return null;
    }

    public void addRoute(RouteEdge route) {
        validateRoute(route, null);
        routes.add(normalizeRoute(route));
        rebuildStructures();
    }

    public void updateRoute(String originalFromId, String originalToId, RouteEdge updatedRoute) {
        RouteEdge current = getRoute(originalFromId, originalToId);
        if (current == null) {
            throw new IllegalArgumentException("Route tidak ditemukan.");
        }

        validateRoute(updatedRoute, current);
        routes.removeIf(route -> route.connects(originalFromId, originalToId));
        routes.add(normalizeRoute(updatedRoute));
        rebuildStructures();
    }

    public boolean deleteRoute(String fromId, String toId) {
        boolean removed = routes.removeIf(route -> route.connects(fromId, toId));
        if (removed) {
            rebuildStructures();
        }
        return removed;
    }

    public String buildGraphSummary() {
        StringBuilder builder = new StringBuilder();
        builder.append("Adjacency List Graph\n");
        for (Map.Entry<String, List<RouteEdge>> entry : graph.getAdjacency().entrySet()) {
            builder.append(entry.getKey()).append(" -> ");
            List<String> neighbors = new ArrayList<>();
            for (RouteEdge route : entry.getValue()) {
                String other = route.getOther(entry.getKey());
                int cost = route.getTraversalCost(terrainWeights);
                String costText = cost == Integer.MAX_VALUE ? "X" : String.valueOf(cost);
                neighbors.add(other + "(" + route.getTerrainType().name() + ":" + costText + ")");
            }
            builder.append(String.join(", ", neighbors)).append('\n');
        }
        return builder.toString();
    }

    public String buildBlockedAreaSummary() {
        StringBuilder builder = new StringBuilder();
        builder.append("Checkpoint terblokir:\n");
        List<String> blockedCheckpoints = new ArrayList<>();
        for (Checkpoint checkpoint : checkpoints.values()) {
            if (checkpoint.isBlocked()) {
                blockedCheckpoints.add("- " + checkpoint.getId() + " (" + checkpoint.getName() + ")");
            }
        }

        if (blockedCheckpoints.isEmpty()) {
            builder.append("- Tidak ada\n");
        } else {
            for (String blockedCheckpoint : blockedCheckpoints) {
                builder.append(blockedCheckpoint).append('\n');
            }
        }

        builder.append("Route impassable:\n");
        boolean hasWallRoute = false;
        for (RouteEdge route : routes) {
            if (route.getTerrainType().isImpassable()) {
                hasWallRoute = true;
                builder.append("- ").append(route.getFromId())
                        .append(" <-> ").append(route.getToId())
                        .append(" (").append(route.getTerrainType().name()).append(")\n");
            }
        }

        if (!hasWallRoute) {
            builder.append("- Tidak ada\n");
        }
        return builder.toString();
    }

    public List<String> runBfs(String startId) {
        Checkpoint start = checkpoints.get(normalizeId(startId));
        if (start == null || start.isBlocked()) {
            return Collections.emptyList();
        }

        ArrayDeque<String> queue = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();
        List<String> order = new ArrayList<>();

        queue.add(start.getId());
        visited.add(start.getId());

        while (!queue.isEmpty()) {
            String currentId = queue.poll();
            order.add(currentId);

            List<RouteEdge> neighbors = new ArrayList<>(graph.getRoutesFrom(currentId));
            neighbors.sort(Comparator.comparing(route -> route.getOther(currentId)));
            for (RouteEdge route : neighbors) {
                if (route.getTerrainType().isImpassable()) {
                    continue;
                }

                String nextId = route.getOther(currentId);
                if (nextId == null || visited.contains(nextId)) {
                    continue;
                }

                Checkpoint next = checkpoints.get(nextId);
                if (next == null || next.isBlocked()) {
                    continue;
                }

                visited.add(nextId);
                queue.add(nextId);
            }
        }

        return order;
    }

    public PathResult findPathAStar(String startId, String goalId) {
        return runAStar(startId, goalId, true);
    }

    public HeuristicComparison compareHeuristics(String startId, String goalId) {
        return new HeuristicComparison(
                runAStar(startId, goalId, true),
                runAStar(startId, goalId, false)
        );
    }

    public Map<TerrainType, Integer> getTerrainWeights() {
        return new EnumMap<>(terrainWeights);
    }

    public void updateTerrainWeight(TerrainType terrainType, int weight) {
        if (terrainType.isImpassable()) {
            throw new IllegalArgumentException("WALL bersifat impassable dan tidak bisa diubah.");
        }
        if (weight <= 0) {
            throw new IllegalArgumentException("Bobot harus lebih besar dari 0.");
        }
        terrainWeights.put(terrainType, weight);
    }

    public Checkpoint peekHighestDangerCheckpoint() {
        return dangerHeap.peek();
    }

    public List<Checkpoint> getDangerRanking(int limit) {
        return dangerHeap.toSortedList(limit);
    }

    public void saveDataset() {
        datasetManager.save(datasetPath, getAllCheckpoints(), new ArrayList<>(routes), terrainWeights);
    }

    private void loadDataset() {
        GameMapData data = datasetManager.load(datasetPath);

        checkpoints.clear();
        routes.clear();
        terrainWeights.clear();

        for (Checkpoint checkpoint : data.getCheckpoints()) {
            checkpoints.put(normalizeId(checkpoint.getId()), checkpoint);
        }
        routes.addAll(data.getRoutes());
        terrainWeights.putAll(data.getTerrainWeights());

        rebuildStructures();
    }

    private void rebuildStructures() {
        graph.clear();
        for (Checkpoint checkpoint : checkpoints.values()) {
            graph.addCheckpoint(checkpoint.getId());
        }
        for (RouteEdge route : routes) {
            graph.addRoute(route);
        }
        dangerHeap.rebuild(checkpoints.values());
    }

    private PathResult runAStar(String startId, String goalId, boolean useHeuristic) {
        Checkpoint start = checkpoints.get(normalizeId(startId));
        Checkpoint goal = checkpoints.get(normalizeId(goalId));
        String heuristicName = useHeuristic ? "Manhattan" : "Zero Heuristic";

        if (start == null || goal == null || start.isBlocked() || goal.isBlocked()) {
            return PathResult.notFound(heuristicName, 0);
        }

        BinaryMinHeap<FrontierNode> openList = new BinaryMinHeap<>();
        Map<String, Integer> gScore = new HashMap<>();
        Map<String, String> cameFrom = new HashMap<>();
        Set<String> closed = new HashSet<>();

        gScore.put(start.getId(), 0);
        openList.insert(new FrontierNode(start.getId(), heuristic(start, goal, useHeuristic), 0));

        int expandedNodes = 0;
        while (!openList.isEmpty()) {
            FrontierNode currentNode = openList.extractMin();
            String currentId = currentNode.checkpointId;

            if (closed.contains(currentId)) {
                continue;
            }
            closed.add(currentId);
            expandedNodes++;

            if (currentId.equals(goal.getId())) {
                return new PathResult(true, reconstructPath(cameFrom, goal.getId()),
                        gScore.get(goal.getId()), expandedNodes, heuristicName);
            }

            for (RouteEdge route : graph.getRoutesFrom(currentId)) {
                if (route.getTerrainType().isImpassable()) {
                    continue;
                }

                String neighborId = route.getOther(currentId);
                if (neighborId == null) {
                    continue;
                }

                Checkpoint neighbor = checkpoints.get(neighborId);
                if (neighbor == null || neighbor.isBlocked()) {
                    continue;
                }

                int routeCost = route.getTraversalCost(terrainWeights);
                if (routeCost == Integer.MAX_VALUE) {
                    continue;
                }

                int tentativeGScore = gScore.get(currentId) + routeCost;
                int currentBest = gScore.getOrDefault(neighborId, Integer.MAX_VALUE);
                if (tentativeGScore < currentBest) {
                    cameFrom.put(neighborId, currentId);
                    gScore.put(neighborId, tentativeGScore);
                    int priority = tentativeGScore + heuristic(neighbor, goal, useHeuristic);
                    openList.insert(new FrontierNode(neighborId, priority, tentativeGScore));
                }
            }
        }

        return PathResult.notFound(heuristicName, expandedNodes);
    }

    private List<String> reconstructPath(Map<String, String> cameFrom, String goalId) {
        List<String> path = new ArrayList<>();
        String current = goalId;
        while (current != null) {
            path.add(current);
            current = cameFrom.get(current);
        }
        Collections.reverse(path);
        return path;
    }

    private int heuristic(Checkpoint current, Checkpoint goal, boolean useHeuristic) {
        if (!useHeuristic) {
            return 0;
        }

        int minWeight = Integer.MAX_VALUE;
        for (Map.Entry<TerrainType, Integer> entry : terrainWeights.entrySet()) {
            if (!entry.getKey().isImpassable() && entry.getValue() > 0) {
                minWeight = Math.min(minWeight, entry.getValue());
            }
        }

        if (minWeight == Integer.MAX_VALUE) {
            minWeight = 1;
        }

        int manhattanDistance = Math.abs(current.getX() - goal.getX()) + Math.abs(current.getY() - goal.getY());
        return manhattanDistance * minWeight;
    }

    private void validateCheckpoint(Checkpoint checkpoint, String originalId) {
        if (checkpoint.getId() == null || checkpoint.getId().isBlank()) {
            throw new IllegalArgumentException("ID checkpoint tidak boleh kosong.");
        }
        if (checkpoint.getName() == null || checkpoint.getName().isBlank()) {
            throw new IllegalArgumentException("Nama checkpoint tidak boleh kosong.");
        }

        String normalizedId = normalizeId(checkpoint.getId());
        if (originalId == null || !normalizedId.equalsIgnoreCase(originalId)) {
            if (checkpoints.containsKey(normalizedId)) {
                throw new IllegalArgumentException("ID checkpoint sudah dipakai.");
            }
        }
    }

    private void validateRoute(RouteEdge route, RouteEdge currentRoute) {
        String fromId = normalizeId(route.getFromId());
        String toId = normalizeId(route.getToId());

        if (fromId.equals(toId)) {
            throw new IllegalArgumentException("Route tidak boleh menghubungkan checkpoint yang sama.");
        }
        if (!checkpoints.containsKey(fromId) || !checkpoints.containsKey(toId)) {
            throw new IllegalArgumentException("Checkpoint asal/tujuan harus sudah ada.");
        }
        if (route.getBaseCost() <= 0) {
            throw new IllegalArgumentException("Base cost harus lebih besar dari 0.");
        }

        for (RouteEdge existingRoute : routes) {
            if (currentRoute != null && existingRoute.connects(currentRoute.getFromId(), currentRoute.getToId())) {
                continue;
            }
            if (existingRoute.connects(fromId, toId)) {
                throw new IllegalArgumentException("Route sudah ada.");
            }
        }
    }

    private RouteEdge normalizeRoute(RouteEdge route) {
        String from = normalizeId(route.getFromId());
        String to = normalizeId(route.getToId());
        if (from.compareTo(to) > 0) {
            return new RouteEdge(to, from, route.getTerrainType(), route.getBaseCost());
        }
        return new RouteEdge(from, to, route.getTerrainType(), route.getBaseCost());
    }

    private String normalizeId(String id) {
        return id.trim().toUpperCase(Locale.ROOT);
    }

    private static class FrontierNode implements Comparable<FrontierNode> {
        private final String checkpointId;
        private final int priority;
        private final int gScore;

        private FrontierNode(String checkpointId, int priority, int gScore) {
            this.checkpointId = checkpointId;
            this.priority = priority;
            this.gScore = gScore;
        }

        @Override
        public int compareTo(FrontierNode other) {
            int priorityComparison = Integer.compare(priority, other.priority);
            if (priorityComparison != 0) {
                return priorityComparison;
            }
            return Integer.compare(gScore, other.gScore);
        }
    }
}
