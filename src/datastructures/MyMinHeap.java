package datastructures;

import java.util.NoSuchElementException;

@SuppressWarnings("unchecked")
public class MyMinHeap<T extends Comparable<T>> {
    private T[] heap;
    private int size;
    private final int capacity; // Fixed size K

    public MyMinHeap(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0");
        }
        this.capacity = capacity;
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
            throw new IllegalStateException("Heap is full");
        }
        heap[size] = item;
        heapUp(size);
        size++;
    }

    public T extractMin() {
        if (isEmpty()) {
            throw new NoSuchElementException("Heap is empty");
        }
        T minVal = heap[0];
        heap[0] = heap[size - 1];
        heap[size - 1] = null;
        size--;
        if (size > 0) {
            heapDown(0);
        }
        return minVal;
    }

    // offerIfBetter as generic T
    public void offerIfBetter(T item) {
        if (size < capacity) {
            insert(item);
        } else if (item.compareTo(peek()) > 0) {
            extractMin();
            insert(item);
        }
    }

    // toSortedArray() returns top-K sorted descending (highest first)
    public Object[] toSortedArray() {
        // Extract all elements to a temp array
        int currentSize = size;
        Object[] temp = new Object[currentSize];
        
        // Since we are extracting min, they will be sorted ascending.
        // We will then reverse them to descending.
        MyMinHeap<T> tempHeap = new MyMinHeap<>(capacity);
        for (int i = 0; i < size; i++) {
            tempHeap.insert(heap[i]);
        }

        for (int i = 0; i < currentSize; i++) {
            temp[i] = tempHeap.extractMin();
        }

        // Reverse to descending order
        Object[] result = new Object[currentSize];
        for (int i = 0; i < currentSize; i++) {
            result[i] = temp[currentSize - 1 - i];
        }

        return result;
    }

    public MySinglyLinkedList<T> toSortedList() {
        MySinglyLinkedList<T> list = new MySinglyLinkedList<>();
        Object[] sortedArr = toSortedArray();
        for (Object obj : sortedArr) {
            list.insertAtTail((T) obj);
        }
        return list;
    }

    private void heapUp(int index) {
        int parent = (index - 1) / 2;
        while (index > 0 && heap[index].compareTo(heap[parent]) < 0) {
            swap(index, parent);
            index = parent;
            parent = (index - 1) / 2;
        }
    }

    private void heapDown(int index) {
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
            heapDown(smallest);
        }
    }

    private void swap(int i, int j) {
        T temp = heap[i];
        heap[i] = heap[j];
        heap[j] = temp;
    }
}
