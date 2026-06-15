package model;

import java.util.Collections;
import java.util.List;

public class PathResult {
    private final boolean found;
    private final List<String> path;
    private final int totalCost;
    private final int expandedNodes;
    private final String heuristicName;

    public PathResult(boolean found, List<String> path, int totalCost, int expandedNodes, String heuristicName) {
        this.found = found;
        this.path = path;
        this.totalCost = totalCost;
        this.expandedNodes = expandedNodes;
        this.heuristicName = heuristicName;
    }

    public static PathResult notFound(String heuristicName, int expandedNodes) {
        return new PathResult(false, Collections.emptyList(), -1, expandedNodes, heuristicName);
    }

    public boolean isFound() {
        return found;
    }

    public List<String> getPath() {
        return path;
    }

    public int getTotalCost() {
        return totalCost;
    }

    public int getExpandedNodes() {
        return expandedNodes;
    }

    public String getHeuristicName() {
        return heuristicName;
    }
}
