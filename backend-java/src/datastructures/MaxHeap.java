package datastructures;

@SuppressWarnings("unchecked")
public class MaxHeap<T extends Comparable<T>> {
    private T[] heap;
    private int size;
    private int capacity;

    public MaxHeap() {
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

    public T peekMax() {
        if (isEmpty()) {
            return null;
        }
        return heap[0];
    }

    public T extractMax() {
        if (isEmpty()) {
            return null;
        }
        T max = heap[0];
        heap[0] = heap[size - 1];
        heap[size - 1] = null;
        size--;
        if (size > 0) {
            heapifyDown(0);
        }
        return max;
    }

    private void heapifyUp(int index) {
        int parent = (index - 1) / 2;
        while (index > 0 && heap[index].compareTo(heap[parent]) > 0) {
            swap(index, parent);
            index = parent;
            parent = (index - 1) / 2;
        }
    }

    private void heapifyDown(int index) {
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
            heapifyDown(largest);
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
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        MaxHeap<Double> maxHeap = new MaxHeap<>();
        System.out.println("=== MAX HEAP INTERACTIVE (DOUBLE) ===");
        
        while (true) {
            System.out.println("\n1. Insert number");
            System.out.println("2. Peek Max");
            System.out.println("3. Extract Max");
            System.out.println("4. Exit");
            System.out.print("Choice: ");
            String choice = scanner.nextLine().trim();
            
            if (choice.equals("1")) {
                System.out.print("Enter a number to insert: ");
                try {
                    double val = Double.parseDouble(scanner.nextLine().trim());
                    maxHeap.insert(val);
                    System.out.println("Inserted " + val + " into MaxHeap. (Size: " + maxHeap.size() + ")");
                } catch (NumberFormatException e) {
                    System.out.println("Invalid number!");
                }
            } else if (choice.equals("2")) {
                Double max = maxHeap.peekMax();
                if (max == null) System.out.println("Heap is empty!");
                else System.out.println("Current Max element: " + max);
            } else if (choice.equals("3")) {
                Double max = maxHeap.extractMax();
                if (max == null) System.out.println("Heap is empty!");
                else System.out.println("Extracted Max: " + max + " (Remaining size: " + maxHeap.size() + ")");
            } else if (choice.equals("4")) {
                System.out.println("Exiting MaxHeap test.");
                break;
            } else {
                System.out.println("Invalid choice!");
            }
        }
        scanner.close();
    }
}
