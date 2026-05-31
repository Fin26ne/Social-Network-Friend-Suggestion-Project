package datastructures;

import java.util.NoSuchElementException;

@SuppressWarnings("unchecked")
public class MyMaxHeap<T extends Comparable<T>> {
    private T[] heap;
    private int size;
    private int capacity;

    private static final int DEFAULT_CAPACITY = 10;

    public MyMaxHeap() {
        this.capacity = DEFAULT_CAPACITY;
        this.heap = (T[]) new Comparable[capacity];
        this.size = 0;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public T peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Heap is empty");
        }
        return heap[0];
    }

    public void insert(T item) {
        if (item == null) throw new IllegalArgumentException("Item cannot be null");
        if (size >= capacity) {
            resize();
        }
        heap[size] = item;
        heapUp(size);
        size++;
    }

    public T extractMax() {
        if (isEmpty()) {
            throw new NoSuchElementException("Heap is empty");
        }
        T maxVal = heap[0];
        heap[0] = heap[size - 1];
        heap[size - 1] = null;
        size--;
        if (size > 0) {
            heapDown(0);
        }
        return maxVal;
    }

    public MySinglyLinkedList<T> topK(int k) {
        MySinglyLinkedList<T> result = new MySinglyLinkedList<>();
        if (k <= 0 || isEmpty()) {
            return result;
        }

        // To get top K without destroying the heap, we can create a temporary heap
        MyMaxHeap<T> tempHeap = new MyMaxHeap<>();
        for (int i = 0; i < size; i++) {
            tempHeap.insert(heap[i]);
        }

        int count = Math.min(k, size);
        for (int i = 0; i < count; i++) {
            result.insertAtTail(tempHeap.extractMax());
        }

        return result;
    }

    private void heapUp(int index) {
        int parent = (index - 1) / 2;
        while (index > 0 && heap[index].compareTo(heap[parent]) > 0) {
            swap(index, parent);
            index = parent;
            parent = (index - 1) / 2;
        }
    }

    private void heapDown(int index) {
        int leftChild = 2 * index + 1;
        int rightChild = 2 * index + 2;
        int largest = index;

        if (leftChild < size && heap[leftChild].compareTo(heap[largest]) > 0) {
            largest = leftChild;
        }
        if (rightChild < size && heap[rightChild].compareTo(heap[largest]) > 0) {
            largest = rightChild;
        }

        if (largest != index) {
            swap(index, largest);
            heapDown(largest);
        }
    }

    private void swap(int i, int j) {
        T temp = heap[i];
        heap[i] = heap[j];
        heap[j] = temp;
    }

    private void resize() {
        capacity = capacity * 2;
        T[] newHeap = (T[]) new Comparable[capacity];
        System.arraycopy(heap, 0, newHeap, 0, size);
        heap = newHeap;
    }
}
