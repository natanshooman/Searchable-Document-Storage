package edu.yu.cs.com1320.project.stage3.impl;

import edu.yu.cs.com1320.project.stage3.Document;
import java.net.URI;
import java.util.*;

public class DocumentImpl implements Document {

  private String text;
  private byte[] binaryData;
  private URI uri;
  private boolean isText;
  private HashMap<String, Integer> wordCount;

  public DocumentImpl(URI uri, String txt) {
    if (uri == null || txt == null) {
      throw new IllegalArgumentException();
    }
    if (txt.isBlank() || uri.toASCIIString().isBlank()) {
      throw new IllegalArgumentException();
    }
    this.uri = uri;
    this.text = txt;
    wordCount = new HashMap<>();
    isText = true;
    wordTally(text);
  }

  private void wordTally(String text) {
    HashMap<String, Integer> wordCounts = new HashMap<>();
    // Convert into character Array of lowercase values
    String copyOfText = new String(text);
    copyOfText = copyOfText.toLowerCase();
    Scanner scanner = new Scanner(copyOfText);
    while (scanner.hasNext()) {
      String i = scanner.next();
      char[] lettersOrDigits = i.toCharArray();
      char[] onlyLetterOrDigit = new char[lettersOrDigits.length];
      int letterUsed = 0;
      for (char j : lettersOrDigits) {
        if (Character.isLetterOrDigit(j)) {
          onlyLetterOrDigit[letterUsed++] = j;
        }
      }
      String cleanedWord = new String(onlyLetterOrDigit);
      if (wordCounts.containsKey(cleanedWord)) {
        wordCounts.put(cleanedWord, wordCounts.get(cleanedWord) + 1);
      } else {
        wordCounts.put(cleanedWord, 1);
      }
    }
    this.wordCount = wordCounts;
  }

  public DocumentImpl(URI uri, byte[] binaryData) {
    if (uri == null || binaryData == null) {
      throw new IllegalArgumentException();
    }
    String testString = new String(binaryData);
    if (testString.isBlank() || uri.toASCIIString().isBlank()) {
      throw new IllegalArgumentException();
    }
    this.uri = uri;
    this.binaryData = binaryData;
    isText = false;
  }

  public String getDocumentTxt() {
    return this.text;
  }

  public byte[] getDocumentBinaryData() {
    if (this.binaryData == null) {
      return null;
    }
    int size = this.binaryData.length;
    byte[] newByteArray = new byte[size];
    for (int i = 0; i < size; i++) {
      newByteArray[i] = this.binaryData[i];
    }

    return newByteArray;
  }

  public URI getKey() {
    return this.uri;
  }

  @Override
  public int hashCode() {
    int result = uri.hashCode();
    result = 31 * result + (text != null ? text.hashCode() : 0);
    result = 31 * result + Arrays.hashCode(binaryData);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || this == null) {
      return false;
    }
    if (obj.getClass() == DocumentImpl.class) {
      DocumentImpl other = (DocumentImpl) obj;
      if (this.hashCode() == other.hashCode()) {
        return true;
      }
    }
    return false;
  }

  /**
   * how many times does the given word appear in the document?
   * @param word
   * @return the number of times the given words appears in the document. If it's a binary document, return 0.
   */
  public int wordCount(String word) {
    if (word == null) {
      throw new IllegalArgumentException();
    }
    word = word.toLowerCase();
    if (!this.isText) {
      return 0;
    } else {
      if (wordCount.containsKey(word)) {
        return wordCount.get(word);
      }
    }
    return 0;
  }

  /**
   * @return all the words that appear in the document
   */

  public Set<String> getWords() {
    if (!this.isText) {
      return new HashSet<>();
    } else {
      return wordCount.keySet();
    }
  }
}
