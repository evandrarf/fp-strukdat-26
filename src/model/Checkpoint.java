package model;

public class Checkpoint {
    private String id;
    private String name;
    private int x;
    private int y;
    private String zone;
    private int difficulty;
    private String loot;
    private int dangerScore;
    private boolean blocked;

    public Checkpoint(String id, String name, int x, int y, String zone, int difficulty, String loot, int dangerScore,
                      boolean blocked) {
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
        this.zone = zone;
        this.difficulty = difficulty;
        this.loot = loot;
        this.dangerScore = dangerScore;
        this.blocked = blocked;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getZone() {
        return zone;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public String getLoot() {
        return loot;
    }

    public int getDangerScore() {
        return dangerScore;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public boolean matchesQuery(String query) {
        String normalized = query.toLowerCase();
        return id.toLowerCase().contains(normalized) || name.toLowerCase().contains(normalized);
    }

    public String toDisplayLine() {
        return String.format(
                "%s | %s | pos=(%d,%d) | zone=%s | diff=%d | loot=%s | danger=%d | blocked=%s",
                id, name, x, y, zone, difficulty, loot, dangerScore, blocked
        );
    }

    public String toDatasetLine() {
        return String.join(";",
                id,
                name,
                String.valueOf(x),
                String.valueOf(y),
                zone,
                String.valueOf(difficulty),
                loot,
                String.valueOf(dangerScore),
                String.valueOf(blocked)
        );
    }
}
