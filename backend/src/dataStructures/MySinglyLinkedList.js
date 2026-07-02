/**
 * MySinglyLinkedList — Custom Singly Linked List
 * CSD201 Data Structures Project
 * 
 * Used as the building block for adjacency lists in MyGraph
 * and as the internal storage for MyQueue.
 * 
 * Time Complexities:
 *   insertAtHead: O(1)
 *   insertAtTail: O(n)
 *   remove:       O(n)
 *   find:         O(n)
 *   contains:     O(n)
 *   size:         O(1) — cached
 *   toArray:      O(n)
 */

class SLLNode {
  constructor(data) {
    this.data = data;
    this.next = null;
  }
}

class MySinglyLinkedList {
  constructor() {
    this.head = null;
    this._size = 0;
  }

  /**
   * Insert a new node at the head of the list.
   * @param {*} data 
   * @returns {MySinglyLinkedList} this (for chaining)
   */
  insertAtHead(data) {
    const newNode = new SLLNode(data);
    newNode.next = this.head;
    this.head = newNode;
    this._size++;
    return this;
  }

  /**
   * Insert a new node at the tail of the list.
   * @param {*} data 
   * @returns {MySinglyLinkedList} this
   */
  insertAtTail(data) {
    const newNode = new SLLNode(data);
    if (!this.head) {
      this.head = newNode;
    } else {
      let current = this.head;
      while (current.next) {
        current = current.next;
      }
      current.next = newNode;
    }
    this._size++;
    return this;
  }

  /**
   * Remove the first node containing the given data.
   * @param {*} data 
   * @returns {boolean} true if removed, false if not found
   */
  remove(data) {
    if (!this.head) return false;

    if (this.head.data === data) {
      this.head = this.head.next;
      this._size--;
      return true;
    }

    let current = this.head;
    while (current.next) {
      if (current.next.data === data) {
        current.next = current.next.next;
        this._size--;
        return true;
      }
      current = current.next;
    }
    return false;
  }

  /**
   * Find the first node containing the given data.
   * @param {*} data 
   * @returns {SLLNode|null}
   */
  find(data) {
    let current = this.head;
    while (current) {
      if (current.data === data) return current;
      current = current.next;
    }
    return null;
  }

  /**
   * Check if the list contains the given data.
   * @param {*} data 
   * @returns {boolean}
   */
  contains(data) {
    return this.find(data) !== null;
  }

  /**
   * Convert the linked list to a plain array.
   * @returns {Array}
   */
  toArray() {
    const result = [];
    let current = this.head;
    while (current) {
      result.push(current.data);
      current = current.next;
    }
    return result;
  }

  /**
   * Execute a callback for each element.
   * @param {Function} callback (data, index)
   */
  forEach(callback) {
    let current = this.head;
    let index = 0;
    while (current) {
      callback(current.data, index);
      current = current.next;
      index++;
    }
  }

  /**
   * Get the number of elements.
   * @returns {number}
   */
  size() {
    return this._size;
  }

  /**
   * Check if the list is empty.
   * @returns {boolean}
   */
  isEmpty() {
    return this._size === 0;
  }

  /**
   * Clear all elements from the list.
   */
  clear() {
    this.head = null;
    this._size = 0;
  }
}

module.exports = MySinglyLinkedList;
