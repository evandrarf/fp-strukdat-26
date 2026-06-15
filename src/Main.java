import graph.GameMapService;
import model.Checkpoint;
import model.HeuristicComparison;
import model.PathResult;
import model.RouteEdge;
import model.TerrainType;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

public class Main {
    private static final String DATASET_PATH = "data/dataset.txt";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        GameMapService service = new GameMapService(DATASET_PATH);

        System.out.println("==============================================");
        System.out.println(" FP Strukdat - Game Map Pathfinding AI");
        System.out.println("==============================================");
        System.out.println(service.getDatasetSummary());

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt(scanner, "Pilih menu: ");
            System.out.println();

            switch (choice) {
                case 1 -> showCheckpoints(service);
                case 2 -> System.out.println(service.buildGraphSummary());
                case 3 -> searchCheckpoint(scanner, service);
                case 4 -> insertCheckpoint(scanner, service);
                case 5 -> updateCheckpoint(scanner, service);
                case 6 -> deleteCheckpoint(scanner, service);
                case 7 -> manageRoutes(scanner, service);
                case 8 -> System.out.println(service.buildBlockedAreaSummary());
                case 9 -> runBfs(scanner, service);
                case 10 -> runAStar(scanner, service);
                case 11 -> compareHeuristic(scanner, service);
                case 12 -> updateTerrainWeight(scanner, service);
                case 13 -> showDangerHeap(service);
                case 14 -> saveDataset(service);
                case 0 -> {
                    running = false;
                    System.out.println("Program selesai.");
                }
                default -> System.out.println("Menu tidak tersedia.");
            }

            System.out.println();
        }
    }

    private static void printMainMenu() {
        System.out.println("Menu:");
        System.out.println("1. Tampilkan semua checkpoint");
        System.out.println("2. Tampilkan struktur graph");
        System.out.println("3. Search checkpoint");
        System.out.println("4. Insert checkpoint");
        System.out.println("5. Update checkpoint");
        System.out.println("6. Delete checkpoint");
        System.out.println("7. Kelola route/edge");
        System.out.println("8. Tampilkan area tidak dapat dilewati");
        System.out.println("9. Eksplorasi map dengan BFS");
        System.out.println("10. Cari jalur dengan A*");
        System.out.println("11. Bandingkan heuristic A* vs tanpa heuristic");
        System.out.println("12. Ubah bobot terrain");
        System.out.println("13. Tampilkan checkpoint prioritas heap");
        System.out.println("14. Simpan dataset");
        System.out.println("0. Keluar");
    }

    private static void showCheckpoints(GameMapService service) {
        List<Checkpoint> checkpoints = service.getAllCheckpoints();
        System.out.println("Daftar checkpoint (" + checkpoints.size() + "):");
        for (Checkpoint checkpoint : checkpoints) {
            System.out.println("- " + checkpoint.toDisplayLine());
        }
    }

    private static void searchCheckpoint(Scanner scanner, GameMapService service) {
        String query = readLine(scanner, "Masukkan ID atau nama checkpoint: ");
        List<Checkpoint> results = service.searchCheckpoints(query);
        if (results.isEmpty()) {
            System.out.println("Checkpoint tidak ditemukan.");
            return;
        }

        System.out.println("Hasil pencarian:");
        for (Checkpoint checkpoint : results) {
            System.out.println("- " + checkpoint.toDisplayLine());
        }
    }

    private static void insertCheckpoint(Scanner scanner, GameMapService service) {
        System.out.println("Insert checkpoint baru");
        Checkpoint checkpoint = readCheckpoint(scanner, null);
        try {
            service.addCheckpoint(checkpoint);
            System.out.println("Checkpoint berhasil ditambahkan.");
        } catch (IllegalArgumentException exception) {
            System.out.println("Gagal menambah checkpoint: " + exception.getMessage());
        }
    }

    private static void updateCheckpoint(Scanner scanner, GameMapService service) {
        String id = readLine(scanner, "Masukkan ID checkpoint yang ingin diupdate: ").toUpperCase(Locale.ROOT);
        Checkpoint current = service.getCheckpoint(id);
        if (current == null) {
            System.out.println("Checkpoint tidak ditemukan.");
            return;
        }

        System.out.println("Data lama: " + current.toDisplayLine());
        Checkpoint updated = readCheckpoint(scanner, current);
        try {
            service.updateCheckpoint(id, updated);
            System.out.println("Checkpoint berhasil diupdate.");
        } catch (IllegalArgumentException exception) {
            System.out.println("Gagal update checkpoint: " + exception.getMessage());
        }
    }

    private static void deleteCheckpoint(Scanner scanner, GameMapService service) {
        String id = readLine(scanner, "Masukkan ID checkpoint yang ingin dihapus: ").toUpperCase(Locale.ROOT);
        boolean removed = service.deleteCheckpoint(id);
        if (removed) {
            System.out.println("Checkpoint dan route terkait berhasil dihapus.");
        } else {
            System.out.println("Checkpoint tidak ditemukan.");
        }
    }

    private static void manageRoutes(Scanner scanner, GameMapService service) {
        System.out.println("Kelola route");
        System.out.println("1. Tambah route");
        System.out.println("2. Update route");
        System.out.println("3. Delete route");
        int choice = readInt(scanner, "Pilih aksi: ");

        switch (choice) {
            case 1 -> {
                RouteEdge route = readRoute(scanner, null);
                try {
                    service.addRoute(route);
                    System.out.println("Route berhasil ditambahkan.");
                } catch (IllegalArgumentException exception) {
                    System.out.println("Gagal menambah route: " + exception.getMessage());
                }
            }
            case 2 -> {
                String from = readLine(scanner, "Masukkan ID checkpoint asal: ").toUpperCase(Locale.ROOT);
                String to = readLine(scanner, "Masukkan ID checkpoint tujuan: ").toUpperCase(Locale.ROOT);
                RouteEdge current = service.getRoute(from, to);
                if (current == null) {
                    System.out.println("Route tidak ditemukan.");
                    return;
                }

                System.out.println("Data lama: " + current.toDisplayLine(service.getTerrainWeights()));
                RouteEdge updated = readRoute(scanner, current);
                try {
                    service.updateRoute(from, to, updated);
                    System.out.println("Route berhasil diupdate.");
                } catch (IllegalArgumentException exception) {
                    System.out.println("Gagal update route: " + exception.getMessage());
                }
            }
            case 3 -> {
                String from = readLine(scanner, "Masukkan ID checkpoint asal: ").toUpperCase(Locale.ROOT);
                String to = readLine(scanner, "Masukkan ID checkpoint tujuan: ").toUpperCase(Locale.ROOT);
                boolean removed = service.deleteRoute(from, to);
                if (removed) {
                    System.out.println("Route berhasil dihapus.");
                } else {
                    System.out.println("Route tidak ditemukan.");
                }
            }
            default -> System.out.println("Aksi route tidak tersedia.");
        }
    }

    private static void runBfs(Scanner scanner, GameMapService service) {
        String startId = readLine(scanner, "Masukkan checkpoint awal BFS: ").toUpperCase(Locale.ROOT);
        List<String> order = service.runBfs(startId);
        if (order.isEmpty()) {
            System.out.println("BFS tidak dapat dijalankan dari checkpoint tersebut.");
            return;
        }

        System.out.println("Urutan eksplorasi BFS:");
        System.out.println(String.join(" -> ", order));
    }

    private static void runAStar(Scanner scanner, GameMapService service) {
        String startId = readLine(scanner, "Masukkan checkpoint awal: ").toUpperCase(Locale.ROOT);
        String goalId = readLine(scanner, "Masukkan checkpoint tujuan: ").toUpperCase(Locale.ROOT);
        PathResult result = service.findPathAStar(startId, goalId);

        if (!result.isFound()) {
            System.out.println("Path tidak ditemukan.");
            return;
        }

        System.out.println("Jalur terbaik: " + String.join(" -> ", result.getPath()));
        System.out.println("Total cost: " + result.getTotalCost());
        System.out.println("Node diekspansi: " + result.getExpandedNodes());
        System.out.println("Heuristic: " + result.getHeuristicName());
    }

    private static void compareHeuristic(Scanner scanner, GameMapService service) {
        String startId = readLine(scanner, "Masukkan checkpoint awal: ").toUpperCase(Locale.ROOT);
        String goalId = readLine(scanner, "Masukkan checkpoint tujuan: ").toUpperCase(Locale.ROOT);
        HeuristicComparison comparison = service.compareHeuristics(startId, goalId);

        if (!comparison.hasValidResults()) {
            System.out.println("Perbandingan tidak bisa dijalankan karena path tidak ditemukan.");
            return;
        }

        System.out.println("A* dengan Manhattan:");
        printPathResult(comparison.getAStarResult());
        System.out.println("A* tanpa heuristic (setara Dijkstra):");
        printPathResult(comparison.getZeroHeuristicResult());
        System.out.println("Analisis HOTS:");
        System.out.println(comparison.buildInsight());
    }

    private static void updateTerrainWeight(Scanner scanner, GameMapService service) {
        System.out.println("Bobot terrain saat ini:");
        for (Map.Entry<TerrainType, Integer> entry : service.getTerrainWeights().entrySet()) {
            System.out.println("- " + entry.getKey().name() + " = " + entry.getValue());
        }

        String startId = readLine(scanner, "Masukkan checkpoint awal untuk simulasi perubahan rute: ")
                .toUpperCase(Locale.ROOT);
        String goalId = readLine(scanner, "Masukkan checkpoint tujuan untuk simulasi perubahan rute: ")
                .toUpperCase(Locale.ROOT);
        PathResult beforeResult = service.findPathAStar(startId, goalId);

        TerrainType terrain = readTerrainType(scanner, null);
        int weight = readInt(scanner, "Masukkan bobot baru terrain: ");
        try {
            service.updateTerrainWeight(terrain, weight);
            System.out.println("Bobot terrain berhasil diubah.");
            PathResult afterResult = service.findPathAStar(startId, goalId);
            System.out.println("Perbandingan rute:");
            System.out.println("Sebelum perubahan:");
            printPathOrFailure(beforeResult);
            System.out.println("Sesudah perubahan:");
            printPathOrFailure(afterResult);
        } catch (IllegalArgumentException exception) {
            System.out.println("Gagal mengubah bobot terrain: " + exception.getMessage());
        }
    }

    private static void showDangerHeap(GameMapService service) {
        Checkpoint top = service.peekHighestDangerCheckpoint();
        if (top == null) {
            System.out.println("Heap kosong.");
            return;
        }

        System.out.println("Checkpoint paling berbahaya (heap peek):");
        System.out.println(top.toDisplayLine());
        System.out.println();
        System.out.println("Top 5 checkpoint berdasarkan danger score:");
        List<Checkpoint> ranking = service.getDangerRanking(5);
        for (int index = 0; index < ranking.size(); index++) {
            System.out.println((index + 1) + ". " + ranking.get(index).toDisplayLine());
        }
    }

    private static void saveDataset(GameMapService service) {
        service.saveDataset();
        System.out.println("Dataset berhasil disimpan ke " + DATASET_PATH);
    }

    private static void printPathResult(PathResult result) {
        System.out.println("- Jalur: " + String.join(" -> ", result.getPath()));
        System.out.println("- Total cost: " + result.getTotalCost());
        System.out.println("- Node diekspansi: " + result.getExpandedNodes());
    }

    private static void printPathOrFailure(PathResult result) {
        if (!result.isFound()) {
            System.out.println("- Path tidak ditemukan.");
            return;
        }
        printPathResult(result);
    }

    private static Checkpoint readCheckpoint(Scanner scanner, Checkpoint existing) {
        String defaultId = existing == null ? null : existing.getId();
        String idInput = readOptionalLine(scanner, "ID [" + fallback(defaultId, "baru") + "]: ");
        String id = idInput.isBlank() && existing != null ? existing.getId() : idInput.toUpperCase(Locale.ROOT);
        String name = chooseValue(scanner, "Nama", existing == null ? null : existing.getName());
        int x = chooseInt(scanner, "Koordinat X", existing == null ? null : existing.getX());
        int y = chooseInt(scanner, "Koordinat Y", existing == null ? null : existing.getY());
        String zone = chooseValue(scanner, "Zone", existing == null ? null : existing.getZone());
        int difficulty = chooseInt(scanner, "Difficulty", existing == null ? null : existing.getDifficulty());
        String loot = chooseValue(scanner, "Loot", existing == null ? null : existing.getLoot());
        int danger = chooseInt(scanner, "Danger score", existing == null ? null : existing.getDangerScore());
        boolean blocked = chooseBoolean(scanner, "Blocked", existing == null ? null : existing.isBlocked());

        return new Checkpoint(id, name, x, y, zone, difficulty, loot, danger, blocked);
    }

    private static RouteEdge readRoute(Scanner scanner, RouteEdge existing) {
        String from = chooseValue(scanner, "Checkpoint asal", existing == null ? null : existing.getFromId())
                .toUpperCase(Locale.ROOT);
        String to = chooseValue(scanner, "Checkpoint tujuan", existing == null ? null : existing.getToId())
                .toUpperCase(Locale.ROOT);
        TerrainType terrain = readTerrainType(scanner, existing == null ? null : existing.getTerrainType());
        int baseCost = chooseInt(scanner, "Base cost", existing == null ? null : existing.getBaseCost());
        return new RouteEdge(from, to, terrain, baseCost);
    }

    private static TerrainType readTerrainType(Scanner scanner, TerrainType existing) {
        String available = String.join(", ", TerrainType.names());
        String prompt = existing == null
                ? "Terrain (" + available + "): "
                : "Terrain [" + existing.name() + "] (" + available + "): ";

        while (true) {
            String value = readOptionalLine(scanner, prompt);
            if (value.isBlank() && existing != null) {
                return existing;
            }

            try {
                return TerrainType.fromString(value);
            } catch (IllegalArgumentException exception) {
                System.out.println("Terrain tidak valid.");
            }
        }
    }

    private static String chooseValue(Scanner scanner, String label, String existing) {
        String prompt = existing == null ? label + ": " : label + " [" + existing + "]: ";
        String value = readOptionalLine(scanner, prompt);
        return value.isBlank() && existing != null ? existing : value;
    }

    private static int chooseInt(Scanner scanner, String label, Integer existing) {
        while (true) {
            String prompt = existing == null ? label + ": " : label + " [" + existing + "]: ";
            String value = readOptionalLine(scanner, prompt);
            if (value.isBlank() && existing != null) {
                return existing;
            }

            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException exception) {
                System.out.println("Input harus berupa angka.");
            }
        }
    }

    private static boolean chooseBoolean(Scanner scanner, String label, Boolean existing) {
        while (true) {
            String prompt = existing == null ? label + " (true/false): " : label + " [" + existing + "] (true/false): ";
            String value = readOptionalLine(scanner, prompt);
            if (value.isBlank() && existing != null) {
                return existing;
            }

            if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                return Boolean.parseBoolean(value);
            }

            System.out.println("Input harus true atau false.");
        }
    }

    private static int readInt(Scanner scanner, String prompt) {
        while (true) {
            String value = readLine(scanner, prompt);
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException exception) {
                System.out.println("Input harus berupa angka.");
            }
        }
    }

    private static String readLine(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("Input tidak boleh kosong.");
        }
    }

    private static String readOptionalLine(Scanner scanner, String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private static String fallback(String value, String fallback) {
        return value == null ? fallback : value;
    }
}
