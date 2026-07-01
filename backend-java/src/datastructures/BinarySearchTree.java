package datastructures;

public class BinarySearchTree<K extends Comparable<K>, V> {
    private static class Node<K, V> {
        K key;
        V value;
        Node<K, V> left;
        Node<K, V> right;

        Node(K key, V value) {
            this.key = key;
            this.value = value;
            this.left = null;
            this.right = null;
        }
    }

    private Node<K, V> root;
    private int size;

    public BinarySearchTree() {
        this.root = null;
        this.size = 0;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void put(K key, V value) {
        if (key == null) throw new IllegalArgumentException("Key cannot be null");
        if (root == null) {
            root = new Node<>(key, value);
            size++;
            return;
        }
        Node<K, V> curr = root;
        while (true) {
            int cmp = key.compareTo(curr.key);
            if (cmp < 0) {
                if (curr.left == null) {
                    curr.left = new Node<>(key, value);
                    size++;
                    return;
                }
                curr = curr.left;
            } else if (cmp > 0) {
                if (curr.right == null) {
                    curr.right = new Node<>(key, value);
                    size++;
                    return;
                }
                curr = curr.right;
            } else {
                curr.value = value;
                return;
            }
        }
    }

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

    public boolean contains(K key) {
        return get(key) != null;
    }

    public void remove(K key) {
        if (key == null) return;
        if (contains(key)) {
            root = remove(root, key);
            size--;
        }
    }

    private Node<K, V> remove(Node<K, V> node, K key) {
        if (node == null) return null;

        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            node.left = remove(node.left, key);
        } else if (cmp > 0) {
            node.right = remove(node.right, key);
        } else {
            // Node found
            if (node.left == null) return node.right;
            if (node.right == null) return node.left;

            // Node with two children: Get the inorder successor (smallest in the right subtree)
            Node<K, V> minNode = findMin(node.right);
            node.key = minNode.key;
            node.value = minNode.value;
            node.right = remove(node.right, minNode.key);
        }
        return node;
    }

    private Node<K, V> findMin(Node<K, V> node) {
        while (node.left != null) {
            node = node.left;
        }
        return node;
    }

    public SinglyLinkedList<V> inOrderValues() {
        SinglyLinkedList<V> list = new SinglyLinkedList<>();
        inOrderValues(root, list);
        return list;
    }

    private void inOrderValues(Node<K, V> node, SinglyLinkedList<V> list) {
        if (node == null) return;
        inOrderValues(node.left, list);
        list.add(node.value);
        inOrderValues(node.right, list);
    }

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

    public static void main(String[] args) {
        System.out.println("=== BINARY SEARCH TREE TEST ===");
        BinarySearchTree<String, String> bst = new BinarySearchTree<>();
        bst.put("U003", "Nguyen");
        bst.put("U001", "Tri");
        bst.put("U002", "Thien");
        System.out.println("Get U002: " + bst.get("U002"));
        System.out.println("Contains U004? " + bst.contains("U004"));
        System.out.println("Keys in order: " + bst.inOrderKeys());
        bst.remove("U002");
        System.out.println("Keys after removing U002: " + bst.inOrderKeys());
    }
}
