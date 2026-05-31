package datastructures;

import java.util.NoSuchElementException;

public class MyQueue<T> {
    private MySinglyLinkedList<T> list;

    public MyQueue() {
        this.list = new MySinglyLinkedList<>();
    }

    public void enqueue(T data) {
        list.insertAtTail(data);
    }

    public T dequeue() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
        }
        return list.removeHead();
    }

    public T peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
        }
        return list.peekHead();
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public int size() {
        return list.size();
    }
}
