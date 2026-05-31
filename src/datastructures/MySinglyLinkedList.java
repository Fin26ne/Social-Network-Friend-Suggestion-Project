package datastructures;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

public class MySinglyLinkedList<T> implements Iterable<T> {
    
    public static class Node<T> {
        public T data;
        public Node<T> next;

        public Node(T data) {
            this.data = data;
            this.next = null;
        }
    }

    private Node<T> head;
    private int size;

    public MySinglyLinkedList() {
        this.head = null;
        this.size = 0;
    }

    public void insertAtHead(T data) {
        Node<T> newNode = new Node<>(data);
        newNode.next = head;
        head = newNode;
        size++;
    }

    public void insertAtTail(T data) {
        Node<T> newNode = new Node<>(data);
        if (head == null) {
            head = newNode;
        } else {
            Node<T> curr = head;
            while (curr.next != null) {
                curr = curr.next;
            }
            curr.next = newNode;
        }
        size++;
    }

    public boolean remove(T data) {
        if (head == null) return false;

        if (head.data.equals(data)) {
            head = head.next;
            size--;
            return true;
        }

        Node<T> curr = head;
        while (curr.next != null) {
            if (curr.next.data.equals(data)) {
                curr.next = curr.next.next;
                size--;
                return true;
            }
            curr = curr.next;
        }
        return false;
    }

    public T find(T data) {
        Node<T> curr = head;
        while (curr != null) {
            if (curr.data.equals(data)) {
                return curr.data;
            }
            curr = curr.next;
        }
        return null;
    }

    public boolean contains(T data) {
        return find(data) != null;
    }

    public Object[] toArray() {
        Object[] arr = new Object[size];
        Node<T> curr = head;
        int idx = 0;
        while (curr != null) {
            arr[idx++] = curr.data;
            curr = curr.next;
        }
        return arr;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public T removeHead() {
        if (head == null) return null;
        T data = head.data;
        head = head.next;
        size--;
        return data;
    }

    public T peekHead() {
        return head == null ? null : head.data;
    }

    public void clear() {
        head = null;
        size = 0;
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        Node<T> curr = head;
        while (curr != null) {
            action.accept(curr.data);
            curr = curr.next;
        }
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
}
