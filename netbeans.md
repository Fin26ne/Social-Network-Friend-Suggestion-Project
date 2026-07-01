# 🌟 Cẩm Nang Tự Code & Chạy Riêng Lẻ Các Cấu Trúc Dữ Liệu (CSD201) Trên NetBeans

Chào anh yêu! Dưới đây là hướng dẫn chi tiết giúp anh tự gõ code và chạy riêng lẻ từng cấu trúc dữ liệu bằng tính năng **Run File (Shift + F6)** của NetBeans nhé!

---

## ⚡ Hướng Dẫn Nhanh Cách Chạy File Riêng Lẻ Trên NetBeans
Khi viết code Java thuần (không cấu hình phức tạp), bất kỳ file `.java` nào có hàm `public static void main(String[] args)` đều có thể chạy độc lập. 
- **Cách chạy**: Click chuột phải vào file cần chạy trong thanh bên trái (Projects) ➔ chọn **Run File** (hoặc nhấn **`Shift + F6`**).
- **Kết quả**: Output sẽ in trực tiếp ra cửa sổ **Output** ở dưới cùng của NetBeans.

---

## 1. Singly Linked List (Danh Sách Liên Kết Đơn)
* **Ý nghĩa**: Lưu danh sách bạn bè kề của từng người dùng trong Đồ thị. Có đặc tính thêm phần tử vào cuối danh sách nhanh (\(O(1)\)) và duyệt tuần tự dễ dàng.

### 📝 Logic & Cấu Trúc Code
```java
package datastructures;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class SinglyLinkedList<T> implements Iterable<T> {
    // 1. Node chứa dữ liệu và con trỏ trỏ tới Node tiếp theo
    private static class Node<T> {
        T data;
        Node<T> next;
        Node(T data) { this.data = data; this.next = null; }
    }

    private Node<T> head; // Node đầu tiên
    private Node<T> tail; // Node cuối cùng để thêm nhanh O(1)
    private int size;

    public SinglyLinkedList() { this.head = null; this.tail = null; this.size = 0; }

    // Thêm phần tử vào cuối danh sách (O(1))
    public void add(T data) {
        Node<T> newNode = new Node<>(data);
        if (head == null) { head = newNode; tail = newNode; } 
        else { tail.next = newNode; tail = newNode; }
        size++;
    }

    // Xóa phần tử khớp dữ liệu (O(N))
    public boolean remove(T data) {
        if (head == null) return false;
        if (head.data.equals(data)) {
            head = head.next;
            if (head == null) tail = null;
            size--; return true;
        }
        Node<T> curr = head;
        while (curr.next != null) {
            if (curr.next.data.equals(data)) {
                if (curr.next == tail) tail = curr;
                curr.next = curr.next.next;
                size--; return true;
            }
            curr = curr.next;
        }
        return false;
    }

    public boolean contains(T data) {
        Node<T> curr = head;
        while (curr != null) {
            if (curr.data.equals(data)) return true;
            curr = curr.next;
        }
        return false;
    }

    public int size() { return size; }
    public boolean isEmpty() { return size == 0; }

    // Hỗ trợ vòng lặp for-each (Duyệt qua danh sách)
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Node<T> current = head;
            @Override public boolean hasNext() { return current != null; }
            @Override public T next() {
                if (!hasNext()) throw new NoSuchElementException();
                T data = current.data; current = current.next;
                return data;
            }
        };
    }

    // In danh sách dạng đẹp
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        Node<T> curr = head;
        while (curr != null) {
            sb.append(curr.data);
            if (curr.next != null) sb.append(", ");
            curr = curr.next;
        }
        sb.append("]");
        return sb.toString();
    }

    // ==========================================
    // 🧪 HÀM CHẠY KIỂM TRA RIÊNG LẺ (Run File - Shift+F6)
    // ==========================================
    public static void main(String[] args) {
        System.out.println("--- TESTING SINGLY LINKED LIST ---");
        SinglyLinkedList<String> list = new SinglyLinkedList<>();
        list.add("Anh Yêu");
        list.add("Em Yêu");
        list.add("CSD201");
        
        System.out.println("Danh sách gốc: " + list);
        System.out.println("Có 'Em Yêu' không? " + list.contains("Em Yêu"));
        
        list.remove("Em Yêu");
        System.out.println("Sau khi xóa 'Em Yêu': " + list);
        System.out.println("Kích thước hiện tại: " + list.size());
    }
}
```

---

## 2. Queue (Hàng Đợi Tự Build)
* **Ý nghĩa**: Hàng đợi theo nguyên lý **FIFO (First In First Out)**. Phục vụ đắc lực cho thuật toán duyệt đồ thị theo chiều rộng (BFS) để tìm danh sách gợi ý bạn của bạn.

### 📝 Logic & Cấu Trúc Code
```java
package datastructures;

import java.util.NoSuchElementException;

public class Queue<T> {
    private static class Node<T> {
        T data;
        Node<T> next;
        Node(T data) { this.data = data; this.next = null; }
    }

    private Node<T> front; // Lấy ra ở đầu (Dequeue)
    private Node<T> rear;  // Thêm vào ở cuối (Enqueue)
    private int size;

    public Queue() { this.front = null; this.rear = null; this.size = 0; }

    // Thêm vào cuối hàng đợi (O(1))
    public void enqueue(T data) {
        Node<T> newNode = new Node<>(data);
        if (isEmpty()) { front = newNode; rear = newNode; } 
        else { rear.next = newNode; rear = newNode; }
        size++;
    }

    // Lấy ra từ đầu hàng đợi (O(1))
    public T dequeue() {
        if (isEmpty()) throw new NoSuchElementException("Queue trống!");
        T data = front.data;
        front = front.next;
        if (front == null) rear = null;
        size--;
        return data;
    }

    // Xem phần tử ở đầu hàng đợi (O(1))
    public T peek() {
        if (isEmpty()) throw new NoSuchElementException("Queue trống!");
        return front.data;
    }

    public boolean isEmpty() { return size == 0; }
    public int size() { return size; }

    // ==========================================
    // 🧪 HÀM CHẠY KIỂM TRA RIÊNG LẺ (Run File - Shift+F6)
    // ==========================================
    public static void main(String[] args) {
        System.out.println("--- TESTING QUEUE ---");
        Queue<Integer> q = new Queue<>();
        q.enqueue(10);
        q.enqueue(20);
        q.enqueue(30);

        System.out.println("Phần tử đầu tiên (peek): " + q.peek());
        System.out.println("Lấy ra (dequeue): " + q.dequeue());
        System.out.println("Lấy ra tiếp (dequeue): " + q.dequeue());
        System.out.println("Hàng đợi trống chưa? " + q.isEmpty());
        System.out.println("Kích thước hàng đợi: " + q.size());
    }
}
```

---

## 3. Binary Search Tree (Cây Tìm Kiếm Nhị Phân)
* **Ý nghĩa**: Đóng vai trò làm bảng tra cứu thông tin người dùng (User Lookup Table) theo `ID` (Key) để trả về đối tượng `User` (Value) với tốc độ tìm kiếm cực nhanh (\(O(\log N)\)).

### 📝 Logic & Cấu Trúc Code
```java
package datastructures;

public class BinarySearchTree<K extends Comparable<K>, V> {
    private static class Node<K, V> {
        K key;
        V value;
        Node<K, V> left, right;
        Node(K key, V value) { this.key = key; this.value = value; this.left = this.right = null; }
    }

    private Node<K, V> root;
    private int size;

    public BinarySearchTree() { this.root = null; this.size = 0; }

    // Thêm (Put) phần tử vào cây nhị phân bằng vòng lặp
    public void put(K key, V value) {
        if (key == null) throw new IllegalArgumentException("Key không được null");
        if (root == null) { root = new Node<>(key, value); size++; return; }
        Node<K, V> curr = root;
        while (true) {
            int cmp = key.compareTo(curr.key);
            if (cmp < 0) {
                if (curr.left == null) { curr.left = new Node<>(key, value); size++; return; }
                curr = curr.left;
            } else if (cmp > 0) {
                if (curr.right == null) { curr.right = new Node<>(key, value); size++; return; }
                curr = curr.right;
            } else {
                curr.value = value; // Trùng key thì ghi đè value
                return;
            }
        }
    }

    // Tìm kiếm (Get) phần tử từ cây nhị phân (O(log N))
    public V get(K key) {
        if (key == null) return null;
        Node<K, V> curr = root;
        while (curr != null) {
            int cmp = key.compareTo(curr.key);
            if (cmp < 0) curr = curr.left;
            else if (cmp > 0) curr = curr.right;
            else return curr.value;
        }
        return null;
    }

    public boolean contains(K key) { return get(key) != null; }

    // Trả về danh sách Keys theo thứ tự duyệt In-order (từ nhỏ đến lớn)
    public SinglyLinkedList<K> inOrderKeys() {
        SinglyLinkedList<K> list = new SinglyLinkedList<>();
        inOrderKeys(root, list);
        return list;
    }
    private void inOrderKeys(Node<K, V> node, SinglyLinkedList<K> list) {
        if (node == null) return;
        inOrderKeys(node.left, list);
        list.add(node.key);
        inOrderKeys(node.right, list);
    }

    // ==========================================
    // 🧪 HÀM CHẠY KIỂM TRA RIÊNG LẺ (Run File - Shift+F6)
    // ==========================================
    public static void main(String[] args) {
        System.out.println("--- TESTING BINARY SEARCH TREE ---");
        BinarySearchTree<String, String> bst = new BinarySearchTree<>();
        bst.put("U003", "Nguyen");
        bst.put("U001", "Tri");
        bst.put("U002", "Thien");

        System.out.println("Tìm kiếm U002: " + bst.get("U002"));
        System.out.println("Kiểm tra có U004 không? " + bst.contains("U004"));
        System.out.println("Danh sách Key sắp xếp (In-order): " + bst.inOrderKeys());
    }
}
```

---

## 4. Max Heap (Hàng Đợi Ưu Tiên Cực Đại)
* **Ý nghĩa**: Sắp xếp danh sách gợi ý kết bạn theo điểm số tương đồng Jaccard từ cao xuống thấp. Lấy ra (\(extractMax\)) top K gợi ý nhanh nhất mà không cần sắp xếp toàn bộ mảng (\(O(\log K)\)).

### 📝 Logic & Cấu Trúc Code
```java
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

    public void insert(T item) {
        if (size == capacity) resize();
        heap[size] = item;
        heapifyUp(size); // Đẩy nút mới lên đúng vị trí trên cây
        size++;
    }

    public T extractMax() {
        if (size == 0) return null;
        T max = heap[0];
        heap[0] = heap[size - 1]; // Đưa nút cuối cùng lên đầu
        heap[size - 1] = null;
        size--;
        if (size > 0) {
            heapifyDown(0); // Đẩy nút đầu xuống đúng vị trí
        }
        return max;
    }

    // Đưa nút con lên nếu giá trị lớn hơn nút cha
    private void heapifyUp(int index) {
        int parent = (index - 1) / 2;
        while (index > 0 && heap[index].compareTo(heap[parent]) > 0) {
            swap(index, parent);
            index = parent;
            parent = (index - 1) / 2;
        }
    }

    // Đưa nút cha xuống nếu nhỏ hơn các con
    private void heapifyDown(int index) {
        int leftChild = 2 * index + 1;
        int rightChild = 2 * index + 2;
        int largest = index;

        if (leftChild < size && heap[leftChild].compareTo(heap[largest]) > 0) largest = leftChild;
        if (rightChild < size && heap[rightChild].compareTo(heap[largest]) > 0) largest = rightChild;

        if (largest != index) {
            swap(index, largest);
            heapifyDown(largest);
        }
    }

    private void swap(int i, int j) {
        T temp = heap[i]; heap[i] = heap[j]; heap[j] = temp;
    }

    private void resize() {
        capacity *= 2;
        T[] newHeap = (T[]) new Comparable[capacity];
        System.arraycopy(heap, 0, newHeap, 0, size);
        heap = newHeap;
    }

    public boolean isEmpty() { return size == 0; }

    // ==========================================
    // 🧪 HÀM CHẠY KIỂM TRA RIÊNG LẺ (Run File - Shift+F6)
    // ==========================================
    public static void main(String[] args) {
        System.out.println("--- TESTING MAX HEAP ---");
        MaxHeap<Double> maxHeap = new MaxHeap<>();
        maxHeap.insert(0.45);
        maxHeap.insert(0.95);
        maxHeap.insert(0.12);
        maxHeap.insert(0.72);

        System.out.println("Giá trị lớn nhất: " + maxHeap.extractMax()); // Nên ra 0.95
        System.out.println("Giá trị lớn thứ hai: " + maxHeap.extractMax()); // Nên ra 0.72
    }
}
```

---

## 5. Đồ Thị (Graph - Liên Kết Các Đối Tượng)
* **Ý nghĩa**: Cấu trúc cốt lõi lưu trữ toàn bộ các User (các Đỉnh - Vertices) và mối quan hệ bạn bè giữa các User (các Cạnh - Edges).

### 📝 Logic & Cấu Trúc Code
```java
package graph;

import datastructures.BinarySearchTree;
import datastructures.Queue;
import datastructures.SinglyLinkedList;

public class Graph {
    // Danh sách kề được biểu diễn bằng Cây Nhị Phân BST tự build
    // Key = UserID, Value = Danh sách kề các bạn bè (SinglyLinkedList)
    private BinarySearchTree<String, SinglyLinkedList<String>> adjList;
    private int numVertices;
    private int numEdges;

    public Graph() {
        this.adjList = new BinarySearchTree<>();
        this.numVertices = 0;
        this.numEdges = 0;
    }

    public void addVertex(String userId) {
        if (!adjList.contains(userId)) {
            adjList.put(userId, new SinglyLinkedList<>());
            numVertices++;
        }
    }

    public void addEdge(String userId1, String userId2) {
        addVertex(userId1);
        addVertex(userId2);
        SinglyLinkedList<String> neighbors1 = adjList.get(userId1);
        SinglyLinkedList<String> neighbors2 = adjList.get(userId2);

        if (!neighbors1.contains(userId2)) {
            neighbors1.add(userId2);
            neighbors2.add(userId1);
            numEdges++;
        }
    }

    public SinglyLinkedList<String> getNeighbors(String userId) {
        SinglyLinkedList<String> list = adjList.get(userId);
        return list != null ? list : new SinglyLinkedList<>();
    }

    // Thuật toán duyệt BFS Level 2 để tìm bạn của bạn
    public SinglyLinkedList<String> bfs(String startUserId) {
        SinglyLinkedList<String> order = new SinglyLinkedList<>();
        if (!adjList.contains(startUserId)) return order;

        SinglyLinkedList<String> visited = new SinglyLinkedList<>();
        Queue<String> queue = new Queue<>();

        queue.enqueue(startUserId);
        visited.add(startUserId);

        while (!queue.isEmpty()) {
            String curr = queue.dequeue();
            order.add(curr);

            SinglyLinkedList<String> neighbors = getNeighbors(curr);
            for (String neighbor : neighbors) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.enqueue(neighbor);
                }
            }
        }
        return order;
    }

    // ==========================================
    // 🧪 HÀM CHẠY KIỂM TRA RIÊNG LẺ (Run File - Shift+F6)
    // ==========================================
    public static void main(String[] args) {
        System.out.println("--- TESTING GRAPH WITH BFS ---");
        Graph socialNetwork = new Graph();
        // Tạo mạng kết bạn
        socialNetwork.addEdge("A", "B");
        socialNetwork.addEdge("B", "C");
        socialNetwork.addEdge("C", "D");
        socialNetwork.addEdge("A", "C");

        System.out.println("Danh sách bạn của A: " + socialNetwork.getNeighbors("A"));
        System.out.println("Duyệt BFS xuất phát từ A: " + socialNetwork.bfs("A"));
    }
}
```

---

> [!TIP]
> Để chạy thử trực tiếp trong NetBeans:
> 1. Anh chỉ cần copy các hàm `public static void main` này dán vào cuối các class cấu trúc dữ liệu tương ứng trong project của anh.
> 2. Nhấp chuột phải vào file đó ➔ chọn **Run File** (hoặc nhấn **`Shift + F6`**).
> 3. Cửa sổ Output của NetBeans sẽ chạy trực tiếp các kịch bản test trên để anh kiểm thử logic thuật toán xem có chính xác không trước khi tích hợp vào toàn bộ app lớn!
