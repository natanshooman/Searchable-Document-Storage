package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.Trie;
import java.util.*;

public class TrieImpl<Value> implements Trie<Value> {

  private static final int alphabetSize = 256; // extended ASCII
  private Node root; // root of trie

  private Set<Value> allValuesDeleted = new HashSet<Value>();

  private String word;

  private class Node<Value> {

    protected Set<Value> documentSet = new HashSet<Value>();
    String word;
    protected Node[] links = new Node[alphabetSize];
  }

  @Override
  public void put(String key, Value val) {
    if (val == null || key == null) {
      throw new IllegalArgumentException();
    } else {
      key = key.toLowerCase();
      this.root = put(this.root, key, val, 0);
    }
  }

  private Node put(Node x, String key, Value val, int d) {
    // create a new node
    if (x == null) {
      x = new Node();
    }
    // we've reached the last node in the key,
    // set the value for the key and return the node
    if (d == key.length()) {
      x.documentSet.add(val);
      addWord(x, key);
      return x;
    }
    // proceed to the next node in the chain of nodes that
    // forms the desired key
    char c = key.charAt(d);
    x.links[c] = this.put(x.links[c], key, val, d + 1);
    return x;
  }

  private void addWord(Node x, String word) {
    x.word = word;
  }

  /**
   * get all exact matches for the given key, sorted in descending order.
   * Search is CASE INSENSITIVE.
   * @param key
   * @param comparator used to sort values
   * @return a List of matching Values, in descending order
   */
  @Override
  public List getAllSorted(String key, Comparator comparator) {
    if (key == null) {
      throw new IllegalArgumentException();
    }
    List<Value> list = new ArrayList<Value>();
    key = key.toLowerCase();
    Node x = this.get(this.root, key, 0);
    if (x == null) {
      return list;
    }
    list = new ArrayList<Value>((Collection<? extends Value>) (x.documentSet));
    list.sort(comparator);
    return list;
  }

  private Node get(Node x, String key, int d) {
    // link was null - return null, indicating a miss
    if (x == null) {
      return null;
    }
    // we've reached the last node in the key,
    // return the node
    if (d == key.length()) {
      return x;
    }
    // proceed to the next node in the chain of nodes that
    // forms the desired key
    char c = key.charAt(d);
    return this.get(x.links[c], key, d + 1);
  }

  /**
  * get all matches which contain a String with the given prefix, sorted in
  descending order.
  * For example, if the key is "Too", you would return any value that contains
  "Tool", "Too", "Tooth", "Toodle", etc.
  * Search is CASE INSENSITIVE.
  * @param prefix
  * @param comparator used to sort values
  * @return a List of all matching Values containing the given prefix, in
  descending order
  */

  @Override
  public List<Value> getAllWithPrefixSorted(
    String prefix,
    Comparator<Value> comparator
  ) {
    if (prefix == null) {
      throw new IllegalArgumentException();
    }
    prefix = prefix.toLowerCase();
    ArrayList<Value> matches = new ArrayList<>();
    Node x = this.get(this.root, prefix, 0);
    if (x != null) {
      //            matches.addAll(x.documentSet);
      //            x.documentSet.clear();
    }

    Set<Value> matchesSet = new HashSet<>();

    if (x != null) {
      matchesSet.addAll(x.documentSet);

      getAllChildren(x, matchesSet);
      matches.addAll(matchesSet);
    }

    matches.sort(comparator);
    return matches;
  }

  private void getAllChildren(Node x, Set<Value> matchesSet) {
    for (int i = 0; i < x.links.length; i++) {
      if (x.links[i] != null) {
        if (x.links[i].documentSet.size() > 0) {
          matchesSet.addAll(
            (Collection<? extends Value>) x.links[i].documentSet
          );
        }
        getAllChildren(x.links[i], matchesSet);
      }
    }
  }

  /**
   * Delete the subtree rooted at the last character of the prefix.
   * Search is CASE INSENSITIVE.
   * @param prefix
   * @return a Set of all Values that were deleted.
   */

  private Set<Value> deletedValues;

  public Set<Value> deleteAllWithPrefix(String prefix) {
    if (prefix == null) {
      throw new IllegalArgumentException();
    }
    prefix = prefix.toLowerCase();
    deletedValues = new HashSet<Value>();

    Node prefixStartingPoint = this.get(this.root, prefix, 0);
    if (prefixStartingPoint != null) {
      deletedValues.addAll(prefixStartingPoint.documentSet);
      prefixStartingPoint.documentSet.clear();
    }
    if (prefixStartingPoint != null) {
      deleteAllChildren(prefixStartingPoint);
    }
    prefixStartingPoint = null;
    deleteEmptyChildren();
    return Set.copyOf(deletedValues);
  }

  private Node getToPrefix(Node x, String key, int d) {
    //Get to the start node or the prefix
    // we've reached the last node in the key,
    // return the node
    if (d == key.length()) {
      return x;
    }
    // proceed to the next node in the chain of nodes that
    // forms the desired key
    char c = key.charAt(d);
    return this.get(x.links[c], key, d + 1);
  }

  private void deleteAllChildren(Node x) {
    for (int i = 0; i < x.links.length; i++) {
      if (x.links[i] != null) { //search through all of the links for more children
        if (x.links[i].documentSet.size() > 0) { //if there are documents in the link
          deletedValues.addAll((Set<Value>) x.links[i].documentSet); //add the documents to the deleted values
        }
        deleteAllChildren(x.links[i]);
      }
    }
    for (int i = 0; i < x.links.length; i++) {
      if (x.links[i] != null) { //search through all of the links for more children
        x.links[i] = null; //delete the link to any children
      }
    }
  }

  /**
  * Delete all values from the node of the given key (do not remove the values
  from other nodes in the Trie)
  * @param key
  * @return a Set of all Values that were deleted.
  */

  public Set<Value> deleteAll(String key) {
    if (key == null) {
      throw new IllegalArgumentException();
    }
    key = key.toLowerCase();
    allValuesDeleted.clear();
    Node locationOfPrefix = null;
    if (get(this.root, key, 0) != null) {
      locationOfPrefix = get(this.root, key, 0);
    }
    if (locationOfPrefix != null) {
      allValuesDeleted.addAll((Set<Value>) locationOfPrefix.documentSet);
      ((Set<Value>) locationOfPrefix.documentSet).clear();
      deleteEmptyChildren(); //could potentially switch to just at this spot
    }

    return Set.copyOf(this.allValuesDeleted); // Should null be returned?
  }

  // Value delete(String key, Value val);
  // /**
  //  * Remove the given value from the node of the given key (do not remove the value from other nodes in the Trie)
  //  * @param key
  //  * @param val
  //  * @return the value which was deleted. If the key did not contain the given value, return null.
  //  */
  // Value delete(String key, Value val);
  public Value delete(String key, Value val) {
    if (key == null || val == null) {
      throw new IllegalArgumentException();
    }
    key = key.toLowerCase();
    Node x = this.get(this.root, key, 0);
    if (((Set<Value>) x.documentSet).contains(val)) {
      ((Set<Value>) x.documentSet).remove(val); //hope this works
      deleteEmptyChildren();
      return val;
    } else {
      return null;
    }
  }

  private void deleteEmptyChildren() {
    for (int i = 0; i < 11; i++) {
      deleteEmptyChildren(this.root);
    }
  }

  private Node deleteEmptyChildren(Node x) {
    if (x == null) {
      return null;
    }
    for (int i = 0; i < x.links.length; i++) {
      if (x.links[i] != null) {
        x.links[i] = deleteEmptyChildren(x.links[i]);
      }
    } //reach the last node in the chain
    if (x.documentSet.size() >= 0) {
      return x;
    }
    for (int i = 0; i < x.links.length; i++) {
      if (x.links[i] != null) {
        return x;
      }
    }
    return null;
  }
}
