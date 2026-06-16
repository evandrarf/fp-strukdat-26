# FP Strukdat Kelompok 5

## Anggota Kelompok

| Nama                        | NRP        |
| --------------------------- | ---------- |
| Evandra Raditya Fauzan      | 5027251001 |
| Dea Chrisna Butarbutar      | 5027251035 |
| Akbar Reyhan Fabian Susanto | 5027251053 |
| Muhammad Yusuf              | 5027251067 |
| Naoval James Osamah         | 5027251092 |

## Tema Project

**Game Map Pathfinding AI**

Program Java berbasis CLI untuk simulasi pencarian jalur karakter game pada peta berbobot.  
Node merepresentasikan checkpoint pada map, sedangkan edge merepresentasikan route dengan terrain berbeda seperti `GROUND`, `FOREST`, `SAND`, `WATER`, `LAVA`, dan `WALL`.

## Struktur Data dan Algoritma

- **Graph**: weighted undirected graph dengan representasi adjacency list.
- **Tree**: custom binary heap.
  - `BinaryMinHeap` dipakai sebagai open list A*.
  - `CheckpointMaxHeap` dipakai untuk ranking checkpoint berdasarkan danger score.
- **Algoritma Graph**:
  - A* untuk pathfinding.
  - BFS untuk eksplorasi map.

## Fitur Utama

- Menampilkan semua checkpoint.
- Search checkpoint berdasarkan ID atau nama.
- Insert, update, delete checkpoint.
- Insert, update, delete route.
- Menampilkan struktur graph adjacency list.
- Menjalankan BFS dari checkpoint awal.
- Menjalankan A* untuk mencari jalur terbaik.
- Menampilkan total cost jalur.
- Menampilkan area yang tidak dapat dilewati.
- Mengubah bobot terrain dan membandingkan rute sebelum/sesudah perubahan.
- Membandingkan A* dengan heuristic Manhattan vs tanpa heuristic untuk analisis HOTS.
- Menampilkan checkpoint prioritas tertinggi dari heap.
- Menyimpan perubahan dataset ke file.

## Dataset

Dataset tersimpan di [data/dataset.txt](/Users/evandraraditya049/Kuliah/Kelas/STRUKDAT/fp/fp-strukdat-26/data/dataset.txt) dengan:

- 25 checkpoint.
- 40 route.
- lebih dari 5 atribut per checkpoint:
  - `id`
  - `name`
  - `x`
  - `y`
  - `zone`
  - `difficulty`
  - `loot`
  - `dangerScore`
  - `blocked`

## Struktur Folder

```text
fp-strukdat-26/
├── src/
│   ├── Main.java
│   ├── graph/
│   ├── model/
│   └── tree/
├── data/
│   └── dataset.txt
├── docs/
└── README.md
```

## Cara Menjalankan

Compile:

```bash
javac -d out $(find src -name '*.java')
```

Run:

```bash
java -cp out Main
```

## Catatan Implementasi

- `WALL` dianggap impassable.
- Heuristic A* memakai Manhattan distance berdasarkan koordinat checkpoint.
- Jalur hasil A* tetap optimal, sedangkan jumlah node yang diekspansi dibandingkan pada menu HOTS untuk menunjukkan pengaruh heuristic.

## Hasil Screenshot Program
1. Tampilan Menu

![1](images/1.jpeg)

2. Menu 1: Tampilkan semua checkpoint

![2](images/2.jpeg)

3. Menu 2: Tampilkan Struktur Graph

![3](images/3.jpeg)

4. Menu 3: Search Checkpoint

![2](images/4.1.jpeg)

![2](images/4.2.jpeg)

5. Menu 4: Insert Checkpoint

![2](images/5.1.jpeg)

![2](images/5.2.jpeg)

6. Menu 5: Update Checkpoint

![2](images/6.jpeg)

7. Menu 6: Delete Checkpoint

![2](images/7.jpeg)

8. Menu 7: Kelola route/edge

- Tambah route

![2](images/8.jpeg)

- Update route

![2](images/8.2.jpeg)

- Delete node

![2](images/8.3.jpeg)

9. Menu 8: Tampilkan area tidak dapat dilewati

![2](images/9.jpeg)

10. Menu 9: Eksplorasi map dengan BFS

![2](images/10.jpeg)

11. Menu 10: Cari jalur dengan A*

![2](images/11.jpeg)

12. Menu 11: Bandingkan heuristic A* vs tanpa heuristic

![2](images/12.jpeg)

13. Menu 12: Ubah bobot terrain

![2](images/13.jpeg)

14. Menu 13: Tampilkan checkpoint prioritas heap

![2](images/14.jpeg)

15. Menu 14: Simpan dataset

![2](images/15.jpeg)

16. Menu 0: Keluar

![2](images/16.jpeg)

17. Menu selain 0 - 14

![2](images/17.jpeg)
