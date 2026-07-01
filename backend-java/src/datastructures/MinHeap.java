package datastructures;

@SuppressWarnings("unchecked")
public class MinHeap<T extends Comparable<T>> {
    private T[] heap;
    private int size;
    private int capacity;

    public MinHeap() {
        this.capacity = 10;
        this.size = 0;
        this.heap = (T[]) new Comparable[capacity];
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void insert(T item) {
        if (size == capacity) {
            resize();
        }
        heap[size] = item;
        heapifyUp(size);
        size++;
    }

    public T peekMin() {
        if (isEmpty()) {
            return null;
        }
        return heap[0];
    }

    public T extractMin() {
        if (isEmpty()) {
            return null;
        }
        T min = heap[0];
        heap[0] = heap[size - 1];
        heap[size - 1] = null;
        size--;
        if (size > 0) {
            heapifyDown(0);
        }
        return min;
    }

    private void heapifyUp(int index) {
        int parent = (index - 1) / 2;
        while (index > 0 && heap[index].compareTo(heap[parent]) < 0) {
            swap(index, parent);
            index = parent;
            parent = (index - 1) / 2;
        }
    }

    private void heapifyDown(int index) {
        int leftChild = 2 * index + 1;
        int rightChild = 2 * index + 2;
        int smallest = index;

        if (leftChild < size && heap[leftChild].compareTo(heap[smallest]) < 0) {
            smallest = leftChild;
        }
        if (rightChild < size && heap[rightChild].compareTo(heap[smallest]) < 0) {
            smallest = rightChild;
        }

        if (smallest != index) {
            swap(index, smallest);
            heapifyDown(smallest);
        }
    }

    private void swap(int i, int j) {
        T temp = heap[i];
        heap[i] = heap[j];
        heap[j] = temp;
    }

    private void resize() {
        capacity *= 2;
        T[] newHeap = (T[]) new Comparable[capacity];
        System.arraycopy(heap, 0, newHeap, 0, size);
        heap = newHeap;
    }

    public static void main(String[] args) {
        System.out.println("=== MIN HEAP TEST ===");
        MinHeap<Double> minHeap = new MinHeap<>();
        minHeap.insert(0.45);
        minHeap.insert(0.95);
        minHeap.insert(0.12);
        minHeap.insert(0.72);
        System.out.println("Min element: " + minHeap.peekMin());
        System.out.println("Extract Min: " + minHeap.extractMin());
        System.out.println("Extract Min: " + minHeap.extractMin());
        System.out.println("Extract Min: " + minHeap.extractMin());
    }
}
