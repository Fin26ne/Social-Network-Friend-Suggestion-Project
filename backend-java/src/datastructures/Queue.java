package datastructures;

import java.util.NoSuchElementException;

public class Queue<T> {
    private static class Node<T> {
        T data;
        Node<T> next;

        Node(T data) {
            this.data = data;
            this.next = null;
        }
    }

    private Node<T> front;
    private Node<T> rear;
    private int size;

    public Queue() {
        this.front = null;
        this.rear = null;
        this.size = 0;
    }

    public void enqueue(T data) {
        Node<T> newNode = new Node<>(data);
        if (isEmpty()) {
            front = newNode;
            rear = newNode;
        } else {
            rear.next = newNode;
            rear = newNode;
        }
        size++;
    }

    public T dequeue() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
        }
        T data = front.data;
        front = front.next;
        if (front == null) {
            rear = null;
        }
        size--;
        return data;
    }

    public T peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
        }
        return front.data;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void clear() {
        front = null;
        rear = null;
        size = 0;
    }

    public static void main(String[] args) {
        System.out.println("=== QUEUE INTERACTIVE TEST ===");
        Queue<String> q = new Queue<>();
        java.util.Scanner scanner = new java.util.Scanner(System.in, "UTF-8");

        while (true) {
            System.out.println("\n--- MENU ---");
            System.out.println("1. Enqueue (Add element)");
            System.out.println("2. Dequeue (Remove front)");
            System.out.println("3. Peek (View front)");
            System.out.println("4. View Queue Size");
            System.out.println("5. Exit");
            System.out.print("Choice: ");

            String choice;
            if (scanner.hasNextLine()) {
                choice = scanner.nextLine().trim();
            } else {
                break;
            }

            if (choice.equals("1")) {
                System.out.print("Enter value to enqueue: ");
                String val = scanner.nextLine();
                q.enqueue(val);
                System.out.println("Enqueued: " + val);
            } else if (choice.equals("2")) {
                try {
                    String val = q.dequeue();
                    System.out.println("Dequeued: " + val);
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            } else if (choice.equals("3")) {
                try {
                    String val = q.peek();
                    System.out.println("Front element (peek): " + val);
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            } else if (choice.equals("4")) {
                System.out.println("Current Queue Size: " + q.size());
            } else if (choice.equals("5")) {
                System.out.println("Exiting test.");
                break;
            } else {
                System.out.println("Invalid choice!");
            }
        }
        scanner.close();
    }
}
