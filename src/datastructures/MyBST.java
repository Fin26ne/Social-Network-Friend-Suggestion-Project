package datastructures;

public class MyBST<K extends Comparable<K>, V> {
    
    public static class Node<K, V> {
        public K key;
        public V value;
        public Node<K, V> left;
        public Node<K, V> right;

        public Node(K key, V value) {
            this.key = key;
            this.value = value;
            this.left = null;
            this.right = null;
        }
    }

    private Node<K, V> root;
    private int size;

    public MyBST() {
        this.root = null;
        this.size = 0;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        root = null;
        size = 0;
    }

    public void put(K key, V value) {
        if (key == null) throw new IllegalArgumentException("Key cannot be null");
        root = putHelper(root, key, value);
    }

    private Node<K, V> putHelper(Node<K, V> node, K key, V value) {
        if (node == null) {
            size++;
            return new Node<>(key, value);
        }

        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            node.left = putHelper(node.left, key, value);
        } else if (cmp > 0) {
            node.right = putHelper(node.right, key, value);
        } else {
            node.value = value; // update value
        }
        return node;
    }

    public V get(K key) {
        if (key == null) throw new IllegalArgumentException("Key cannot be null");
        Node<K, V> node = getHelper(root, key);
        return node == null ? null : node.value;
    }

    private Node<K, V> getHelper(Node<K, V> node, K key) {
        if (node == null) return null;

        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            return getHelper(node.left, key);
        } else if (cmp > 0) {
            return getHelper(node.right, key);
        } else {
            return node;
        }
    }

    public boolean contains(K key) {
        return get(key) != null;
    }

    public void delete(K key) {
        if (key == null) throw new IllegalArgumentException("Key cannot be null");
        if (contains(key)) {
            root = deleteHelper(root, key);
            size--;
        }
    }

    private Node<K, V> deleteHelper(Node<K, V> node, K key) {
        if (node == null) return null;

        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            node.left = deleteHelper(node.left, key);
        } else if (cmp > 0) {
            node.right = deleteHelper(node.right, key);
        } else {
            // Found node to delete. Handle 3 cases:
            
            // Case 1 & 2: Leaf or 1 child
            if (node.left == null) {
                return node.right;
            }
            if (node.right == null) {
                return node.left;
            }

            // Case 3: 2 children. Find successor (min in right subtree)
            Node<K, V> successor = findMin(node.right);
            node.key = successor.key;
            node.value = successor.value;
            node.right = deleteHelper(node.right, successor.key);
        }
        return node;
    }

    private Node<K, V> findMin(Node<K, V> node) {
        while (node.left != null) {
            node = node.left;
        }
        return node;
    }

    // inOrderTraversal() returns a sorted list of keys
    public MySinglyLinkedList<K> inOrderTraversal() {
        MySinglyLinkedList<K> list = new MySinglyLinkedList<>();
        inOrderTraversalHelper(root, list);
        return list;
    }

    private void inOrderTraversalHelper(Node<K, V> node, MySinglyLinkedList<K> list) {
        if (node == null) return;
        inOrderTraversalHelper(node.left, list);
        list.insertAtTail(node.key);
        inOrderTraversalHelper(node.right, list);
    }

    public MySinglyLinkedList<K> inOrderKeys() {
        return inOrderTraversal();
    }

    public MySinglyLinkedList<V> inOrderValues() {
        MySinglyLinkedList<V> list = new MySinglyLinkedList<>();
        inOrderValuesHelper(root, list);
        return list;
    }

    private void inOrderValuesHelper(Node<K, V> node, MySinglyLinkedList<V> list) {
        if (node == null) return;
        inOrderValuesHelper(node.left, list);
        list.insertAtTail(node.value);
        inOrderValuesHelper(node.right, list);
    }
}
