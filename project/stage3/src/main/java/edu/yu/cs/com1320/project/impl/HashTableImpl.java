package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.HashTable;

public class HashTableImpl<Key, Value> implements HashTable<Key, Value> {

  // List implementation
  private class DocumentNode<Key, Value> {

    Key key;
    Value value;
    DocumentNode next;

    DocumentNode(Key key, Value value) {
      this.key = key;
      this.value = value;
      this.next = null;
    }
  }

  private DocumentNode[] table;
  private int currentSizeUsed;
  private int size = 5;
  private boolean doubling = false;

  public HashTableImpl() {
    this.table = new DocumentNode[5];
  }

  private void sizeIncrease() {
    this.currentSizeUsed++;
  }

  private void sizeDecrease() {
    this.currentSizeUsed--;
  }

  private int hashFunction(Key k) {
    return (k.hashCode() & 0x7fffffff) % table.length;
  }

  public Value get(Key k) {
    int index = this.hashFunction(k);
    DocumentNode current = this.table[index];

    while (current != null) {
      if (current.key.equals(k)) {
        return (Value) current.value;
      }
      current = current.next;
    }
    return null;
  }

  private boolean deleteNode(Key key) {
    int index = this.hashFunction(key);
    DocumentNode head = this.table[index];
    DocumentNode current = head;
    // Delete Head
    if (this.table[index] == null) {
      return false;
    }
    if (this.table[index].key.equals(key)) { //Check if the key exits
      if (this.table[index].next != null) { //if it has a second element
        this.table[index] = this.table[index].next; //delete the first element
      } else { // The rest of the list is empty
        this.table[index] = null;
      }
      sizeDecrease();
      return true;
    }
    while (
      this.table[index].next != null && !this.table[index].next.key.equals(key)
    ) {
      this.table[index] = this.table[index].next;
    }
    if (current.next != null) {
      Value value = (Value) current.next.value;
      this.table[index].next = this.table[index].next.next;
      sizeDecrease();
      return true;
    }
    return false;
  }

  public Value put(Key k, Value v) {
    int index = this.hashFunction(k);
    if (v == null) { // if Value is null, delete the node if there is a value to delete
      Value previousValue = get(k);
      deleteNode(k);
      return previousValue;
    }
    DocumentNode head = this.table[index];
    DocumentNode<Key, Value> nodeToAdd = new DocumentNode(k, v); //return the old value if there is one otherwise return the new value
    if (head == null) {
      this.table[index] = nodeToAdd;
      if (!doubling) {
        doubleFunction(); //if the size goes above the limit then double the size of the array
        sizeIncrease();
      }
      return null;
    } else if (head.key.equals(k)) {
      Value oldValue = (Value) head.value; // update the new value
      head.value = v;
      return oldValue;
    } else {
      return (put(k, v, nodeToAdd, head));
    }
  }

  private Value put(
    Key k,
    Value v,
    DocumentNode<Key, Value> nodeToAdd,
    DocumentNode head
  ) {
    DocumentNode current = head; // check if it's already in the list
    while (current.next != null) {
      if (current.next.key.equals(k)) {
        Value oldValue = (Value) current.next.value;
        current.next.value = v; // update the new value
        return oldValue;
      }
      current = current.next;
    }
    current.next = nodeToAdd;
    if (!doubling) {
      sizeIncrease();
      doubleFunction();
    }
    return null;
  }

  private void doubleFunction() {
    if (this.size * 0.75 < this.currentSizeUsed) {
      doubling = true;
      //copy data into a temporary array
      DocumentNode temp[] = this.table;
      this.table = new DocumentNode[table.length * 2];
      for (int i = 0; i < temp.length; i++) { //iterate through the temporary array and add each element to the new array with the updated hash function size
        DocumentNode current = temp[i];
        while (current != null) {
          Key key = (Key) current.key;
          Value value = (Value) current.value;
          put(key, value);
          current = current.next;
        }
      }
      temp = null;
      size = table.length;
      doubling = false;
    }
  }
}
