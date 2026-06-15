package model;

public class HeuristicComparison {
    private final PathResult aStarResult;
    private final PathResult zeroHeuristicResult;

    public HeuristicComparison(PathResult aStarResult, PathResult zeroHeuristicResult) {
        this.aStarResult = aStarResult;
        this.zeroHeuristicResult = zeroHeuristicResult;
    }

    public PathResult getAStarResult() {
        return aStarResult;
    }

    public PathResult getZeroHeuristicResult() {
        return zeroHeuristicResult;
    }

    public boolean hasValidResults() {
        return aStarResult.isFound() && zeroHeuristicResult.isFound();
    }

    public String buildInsight() {
        int expandedDifference = zeroHeuristicResult.getExpandedNodes() - aStarResult.getExpandedNodes();

        if (expandedDifference > 0) {
            return "Heuristic Manhattan mengurangi jumlah node yang diekspansi sebanyak "
                    + expandedDifference
                    + " node sambil tetap menjaga cost optimal. Ini menunjukkan heuristic membantu mempercepat pencarian.";
        }

        if (expandedDifference == 0) {
            return "Jumlah node yang diekspansi sama. Pada rute ini, heuristic belum memberi keuntungan signifikan, tetapi hasil cost tetap optimal.";
        }

        return "Heuristic mengekspansi lebih banyak node pada kasus ini. Artinya desain bobot/terrain membuat heuristic kurang efektif untuk start-goal tersebut.";
    }
}
