package edu.yu.cs.com1320.project.stage3.impl;

import edu.yu.cs.com1320.project.*;
import edu.yu.cs.com1320.project.CommandSet;
import edu.yu.cs.com1320.project.impl.*;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.stage3.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class DocumentStoreImpl<Key, Value> implements DocumentStore {

  private HashTableImpl<URI, Document> hashTable;
  private StackImpl<Undoable> undoStack = new StackImpl<>();
  private TrieImpl<Value> trie;

  public DocumentStoreImpl() {
    // Create a new HashTable
    this.hashTable = new HashTableImpl();

    this.trie = new TrieImpl<>();
  }

  /* @throws IOException if there is an issue reading input */

  public int putDocument(InputStream input, URI uri, DocumentFormat format)
    throws IOException {
    if (uri == null || format == null) {
      throw new IllegalArgumentException();
    }
    if (input == null) { // null means this is a delete
      if (this.getDocument(uri) != null) {
        int hashCodeToReturn = this.getDocument(uri).hashCode();
        this.deleteDocument(uri);
        return hashCodeToReturn;
      } else {
        return 0;
      }
    }
    byte[] byteData;
    try {
      byteData = input.readAllBytes();
    } catch (Exception e) {
      throw new IOException();
    }
    return putDocument(uri, format, byteData);
  }

  private int putDocument(URI uri, DocumentFormat format, byte[] byteData) {
    DocumentImpl document = null;
    if (format == DocumentFormat.TXT) {
      document = new DocumentImpl(uri, new String(byteData));
      // Add all words into the trie
      for (String word : document.getWords()) {
        trie.put(word, (Value) document);
      }
    } else if (format == DocumentFormat.BINARY) {
      document = new DocumentImpl(uri, byteData);
    } // Check it currently exits by doing a get.
    String text = new String(byteData);
    Document oldDocument = getDocument(uri); // Store old document value before putting for undoing//null if no                                                 // document exists
    Document valueUponPut = ((Document) (this.hashTable.put(uri, document)));
    if (valueUponPut == null) { // New Document added
      Document value = (Document) this.hashTable.get(uri);
      Set<String> wordsInDocument = value.getWords(); // will all of this be inserted into the undo properly?
      Function<URI, Boolean> undoAdd = u -> {
        this.hashTable.put(uri, null);
        if (format == DocumentFormat.TXT) {
          for (String word : wordsInDocument) {
            trie.delete(word, (Value) value);
          }
        }
        return true;
      };
      undoStack.push(new GenericCommand(uri, undoAdd)); // Add command to stack only if there is a value deleted.
      return 0; //
    } else { // Existing Document
      Document value = (Document) this.hashTable.get(uri);
      Set<String> wordsInDocument = value.getWords();
      Set<String> newWords = document.getWords();
      Function<URI, Boolean> undoModification = u -> {
        this.hashTable.put(uri, valueUponPut);
        if (format == DocumentFormat.TXT) {
          for (String word : wordsInDocument) { // Delete all instances of the old document
            trie.delete(word, (Value) value);
          }
          for (String word : newWords) {
            trie.put(word, (Value) value); // Add instances of the new document
          }
        }
        return true;
      };
      undoStack.push(new GenericCommand(uri, undoModification)); // Add command to stack only if there is a value                                                                     // deleted.
      return valueUponPut.hashCode();
    }
  }

  private class DocumentCompareprefix implements Comparator<Document> {

    private String prefix;

    DocumentCompareprefix(String prefix) {
      this.prefix = prefix;
    }

    @Override
    public int compare(Document d1, Document d2) {
      int d1WordCount = 0;
      for (String word : d1.getWords()) {
        if (word.startsWith(prefix)) {
          d1WordCount += d1.wordCount(word);
        }
      }
      int d2WordCount = 0;
      for (String word : d2.getWords()) {
        if (word.startsWith(prefix)) {
          d1WordCount += d2.wordCount(word);
        }
      }
      return d1WordCount - d2WordCount;
    }
  }

  private class DocumentCompare implements Comparator<Document> {

    private String word;

    DocumentCompare(String word) {
      this.word = word;
    }

    @Override
    public int compare(Document d1, Document d2) {
      return d1.wordCount(word) - d2.wordCount(word);
    }
  }

  public Document getDocument(URI uri) {
    Document documentToReturn = (Document) this.hashTable.get(uri);
    return documentToReturn;
  }

  //
  public boolean deleteDocument(URI uri) {
    Document value = (Document) this.hashTable.get(uri);
    if (value == null) {
      return false;
    }
    Set<String> wordsInDocument = value.getWords();
    Function<URI, Boolean> undoDelete = u -> {
      Set<String> wordsInDoc = wordsInDocument;
      if (value.getDocumentBinaryData() == null) { // Check if it's in text format, which should be inserted into
        // the Trie
        for (String word : wordsInDoc) {
          trie.put(word, (Value) value);
        }
      }
      this.hashTable.put(u, value);
      return true;
    };
    // Command
    GenericCommand command = new GenericCommand(uri, undoDelete);
    // Add command to stack only if there is a value deleted.
    undoStack.push(command);

    boolean valueToReturn = (this.hashTable.put(uri, null) != null);
    for (String word : wordsInDocument) {
      trie.delete(word, (Value) value);
    }
    return valueToReturn;
  }

  /**
   * undo the last put or delete command
   *
   * @throws IllegalStateException if there are no actions to be undone, i.e. the
   *                               command stack is empty
   */
  public void undo() throws IllegalStateException {
    if (undoStack.size() == 0) {
      throw new IllegalStateException();
    }
    undoStack.pop().undo();
  }

  /**
   * undo the last put or delete that was done with the given URI as its key
   *
   * @param uri
   * @throws IllegalStateException if there are no actions on the command stack
   *                               for the given URI
   */
  public void undo(URI uri) throws IllegalStateException {
    Boolean foundUri = false;
    if (uri == null) {
      throw new IllegalStateException();
    }
    StackImpl<Undoable> redoStack = new StackImpl();
    while (undoStack.size() != 0) { //
      Undoable currentCommand = undoStack.pop();
      if (
        (
          currentCommand instanceof CommandSet &&
          ((CommandSet) currentCommand).containsTarget(uri)
        ) ||
        (
          currentCommand instanceof GenericCommand &&
          ((GenericCommand) currentCommand).getTarget().equals(uri)
        )
      ) {
        redoStack.push(currentCommand);
        if ((currentCommand instanceof CommandSet)) {
          ((CommandSet) currentCommand).undo(uri);
        }
        if ((currentCommand instanceof GenericCommand)) {
          currentCommand.undo();
        }
        while (redoStack.size() != 0) {
          undoStack.push(redoStack.pop());
        }
        foundUri = true;
        break;
      } else {
        redoStack.push(currentCommand);
      }
    }
    if (!foundUri) {
      throw new IllegalStateException();
    }
  }

  /**
   * Retrieve all documents whose text contains the given keyword.
   * Documents are returned in sorted, descending order, sorted by the number of
   * times the keyword appears in the document.
   * Search is CASE INSENSITIVE.
   *
   * @param keyword
   * @return a List of the matches. If there are no matches, return an empty list.
   */
  @Override
  public List<Document> search(String keyword) {
    String word = keyword.toLowerCase();
    // word = keyword.toLowerCase();
    DocumentCompare comparator = new DocumentCompare(word);
    return trie.getAllSorted(keyword, comparator);
  }

  /**
   * Retrieve all documents whose text starts with the given prefix
   * Documents are returned in sorted, descending order, sorted by the number of
   * times the prefix appears in the document.
   * Search is CASE INSENSITIVE.
   *
   * @param keywordPrefix
   * @return a List of the matches. If there are no matches, return an empty list.
   */
  @Override
  public List<Document> searchByPrefix(String keywordPrefix) {
    String word = keywordPrefix.toLowerCase();
    //loop through a
    Comparator<Value> prefixcomparator = (Comparator<Value>) new DocumentCompareprefix(
      word
    );
    return (List<Document>) trie.getAllWithPrefixSorted(word, prefixcomparator);
  }

  /**
   * Completely remove any trace of any document which contains the given keyword
   * @param keyword
   * @return a Set of URIs of the documents that were deleted.
   */
  @Override
  public Set<URI> deleteAll(String keyword) {
    String word = keyword.toLowerCase();
    Comparator<Value> prefixcomparator = (Comparator<Value>) new DocumentCompareprefix(
      word
    );
    List<Document> allDocumentsContainingWord = trie.getAllSorted(
      word,
      prefixcomparator
    );
    Set<URI> urisOfDocumentsDeleted = new HashSet<>();
    //Undo Logic -- add allDocumentsContainingWord to the trie and HashTable
    CommandSet<URI> undoDeleteAll = new CommandSet<>();
    for (Document doc : allDocumentsContainingWord) {
      URI uri = doc.getKey();
      Set<String> wordsInDocument = doc.getWords();
      Function<URI, Boolean> undoDelete = u -> {
        this.hashTable.put(uri, doc);
        for (String wordz : wordsInDocument) {
          trie.put(wordz, (Value) doc);
        }

        return true;
      };
      undoDeleteAll.addCommand((new GenericCommand<URI>(uri, undoDelete)));
      undoStack.push(undoDeleteAll);
    }

    for (Document d : allDocumentsContainingWord) {
      urisOfDocumentsDeleted.add(d.getKey());
      hashTable.put(d.getKey(), null); //Delete all documents containing the word from the HashTable
    }
    for (Document document : allDocumentsContainingWord) {
      for (String wordz : document.getWords()) {
        trie.delete(wordz, (Value) document);
      }
    }
    trie.deleteAll(keyword);
    return urisOfDocumentsDeleted;
  }

  /**
   * Completely remove any trace of any document which contains a word that has the given prefix
   * Search is CASE INSENSITIVE.
   * @param keywordPrefix
   * @return a Set of URIs of the documents that were deleted.
   */
  @Override
  public Set<URI> deleteAllWithPrefix(String keywordPrefix) {
    String word = keywordPrefix.toLowerCase();
    //Undo Logic -- add allDocumentsContainingPrefix to the trie and HashTable
    Comparator<Value> prefixcomparator = (Comparator<Value>) new DocumentCompareprefix(
      word
    );
    List<Document> documentsContaingPrefix = (List<Document>) trie.getAllWithPrefixSorted(
      word,
      prefixcomparator
    );
    CommandSet<URI> undoDeleteAll = new CommandSet<>();
    for (Document doc : documentsContaingPrefix) {
      URI uri = doc.getKey();
      Set<String> wordsInDocument = doc.getWords();
      Function<URI, Boolean> undoDeletePrefix = u -> {
        this.hashTable.put(uri, doc);
        for (String wordz : wordsInDocument) {
          trie.put(wordz, (Value) doc);
        }

        return true;
      };
      undoDeleteAll.addCommand(
        (new GenericCommand<URI>(uri, undoDeletePrefix))
      );
    }
    undoStack.push(undoDeleteAll);

    Set<Document> documentsDeleted = (Set<Document>) trie.deleteAllWithPrefix(
      word
    );
    Set<URI> urisOfDocumentsDeleted = new HashSet<>();
    for (Document d : documentsDeleted) {
      urisOfDocumentsDeleted.add(d.getKey());
      hashTable.put(d.getKey(), null); //Delete all documents containing the word from the HashTable
    }
    return urisOfDocumentsDeleted;
  }
}
