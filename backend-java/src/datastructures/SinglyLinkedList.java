package datastructures;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class SinglyLinkedList<T> implements Iterable<T> {
    private static class Node<T> {
        T data;
        Node<T> next;

        Node(T data) {
            this.data = data;
            this.next = null;
        }
    }

    private Node<T> head;
    private Node<T> tail;
    private int size;

    public SinglyLinkedList() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    public void add(T data) {
        Node<T> newNode = new Node<>(data);
        if (head == null) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            tail = newNode;
        }
        size++;
    }

    public boolean remove(T data) {
        if (head == null)
            return false;

        if (head.data.equals(data)) {
            head = head.next;
            if (head == null) {
                tail = null;
            }
            size--;
            return true;
        }

        Node<T> curr = head;
        while (curr.next != null) {
            if (curr.next.data.equals(data)) {
                if (curr.next == tail) {
                    tail = curr;
                }
                curr.next = curr.next.next;
                size--;
                return true;
            }
            curr = curr.next;
        }
        return false;
    }

    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index " + index + ", Size " + size);
        }
        Node<T> curr = head;
        for (int i = 0; i < index; i++) {
            curr = curr.next;
        }
        return curr.data;
    }

    public boolean contains(T data) {
        Node<T> curr = head;
        while (curr != null) {
            if (curr.data.equals(data)) {
                return true;
            }
            curr = curr.next;
        }
        return false;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Node<T> current = head;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                T data = current.data;
                current = current.next;
                return data;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        Node<T> curr = head;
        while (curr != null) {
            sb.append(curr.data);
            if (curr.next != null) {
                sb.append(", ");
            }
            curr = curr.next;
        }
        sb.append("]");
        return sb.toString();
    }

    public static void main(String[] args) {
        System.out.println("=== SINGLY LINKED LIST INTERACTIVE TEST ===");
        SinglyLinkedList<String> list = new SinglyLinkedList<>();
        java.util.Scanner scanner = new java.util.Scanner(System.in);

        while (true) {
            System.out.println("\n--- MENU ---");
            System.out.println("1. Add Element");
            System.out.println("2. Remove Element");
            System.out.println("3. Check if Contains");
            System.out.println("4. Print List");
            System.out.println("5. Clear List");
            System.out.println("6. Exit");
            System.out.print("Choice: ");

            String choice = scanner.nextLine().trim();

            if (choice.equals("1")) {
                System.out.print("Enter value to add: ");
                String val = scanner.nextLine();
                list.add(val);
                System.out.println("Added: " + val);
            } else if (choice.equals("2")) {
                System.out.print("Enter value to remove: ");
                String val = scanner.nextLine();
                boolean removed = list.remove(val);
                if (removed) {
                    System.out.println("Successfully removed: " + val);
                } else {
                    System.out.println("Not found: " + val);
                }
            } else if (choice.equals("3")) {
                System.out.print("Enter value to check: ");
                String val = scanner.nextLine();
                System.out.println("Contains '" + val + "'? " + list.contains(val));
            } else if (choice.equals("4")) {
                System.out.println("Current List: " + list);
                System.out.println("Size: " + list.size());
            } else if (choice.equals("5")) {
                list.clear();
                System.out.println("List cleared.");
            } else if (choice.equals("6")) {
                System.out.println("Exiting test.");
                break;
            } else {
                System.out.println("Invalid choice!");
            }
        }
        scanner.close();
    }
}
