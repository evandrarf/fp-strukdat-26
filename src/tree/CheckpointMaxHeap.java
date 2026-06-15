package tree;

import model.Checkpoint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CheckpointMaxHeap {
    private final List<Checkpoint> heap = new ArrayList<>();

    public void rebuild(Collection<Checkpoint> checkpoints) {
        heap.clear();
        for (Checkpoint checkpoint : checkpoints) {
            heap.add(checkpoint);
        }
        for (int index = heap.size() / 2 - 1; index >= 0; index--) {
            bubbleDown(index);
        }
    }

    public Checkpoint peek() {
        return heap.isEmpty() ? null : heap.get(0);
    }

    public List<Checkpoint> toSortedList(int limit) {
        List<Checkpoint> copy = new ArrayList<>(heap);
        List<Checkpoint> result = new ArrayList<>();

        while (!copy.isEmpty() && result.size() < limit) {
            result.add(extractMax(copy));
        }
        return result;
    }

    private Checkpoint extractMax(List<Checkpoint> source) {
        Checkpoint max = source.get(0);
        Checkpoint last = source.remove(source.size() - 1);
        if (!source.isEmpty()) {
            source.set(0, last);
            bubbleDown(source, 0);
        }
        return max;
    }

    private void bubbleDown(int index) {
        bubbleDown(heap, index);
    }

    private void bubbleDown(List<Checkpoint> source, int index) {
        int size = source.size();
        while (true) {
            int left = index * 2 + 1;
            int right = index * 2 + 2;
            int largest = index;

            if (left < size && compare(source.get(left), source.get(largest)) > 0) {
                largest = left;
            }
            if (right < size && compare(source.get(right), source.get(largest)) > 0) {
                largest = right;
            }
            if (largest == index) {
                break;
            }
            swap(source, index, largest);
            index = largest;
        }
    }

    private int compare(Checkpoint first, Checkpoint second) {
        int scoreComparison = Integer.compare(first.getDangerScore(), second.getDangerScore());
        if (scoreComparison != 0) {
            return scoreComparison;
        }
        return second.getId().compareTo(first.getId());
    }

    private void swap(List<Checkpoint> source, int firstIndex, int secondIndex) {
        Checkpoint temp = source.get(firstIndex);
        source.set(firstIndex, source.get(secondIndex));
        source.set(secondIndex, temp);
    }
}
