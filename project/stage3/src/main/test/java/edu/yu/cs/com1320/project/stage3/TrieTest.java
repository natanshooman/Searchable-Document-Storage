package edu.yu.cs.com1320.project.stage3;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage3.impl.DocumentStoreImpl;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class TrieTest {
  
        private TrieImpl<String> trie;
    
        @BeforeEach
        public void inittrie(){
            this.trie = new TrieImpl<>();
            this.trie.put("Key1","Value1");
            this.trie.put("Key1","Value3");
            this.trie.put("Key1","Value2");


            this.trie.put("Key2","Value2");
            this.trie.put("Key3","Value3");
            this.trie.put("Key4","Value4");
            this.trie.put("Key5","Value5");
            this.trie.put("Key6","Value6");
        }
        private class DocumentCompareprefix implements Comparator<Document> {
            private String prefix;
            DocumentCompareprefix(String prefix){
                this.prefix = prefix;
            }
            @Override
            public int compare(Document d1, Document d2) {
                int d1WordCount = 0;
                for(String word : d1.getWords()){
                    if(word.startsWith(prefix)){
                        d1WordCount += d1.wordCount(word);
                    }
                }
                int d2WordCount = 0;
                for(String word : d2.getWords()){
                    if(word.startsWith(prefix)){
                        d1WordCount += d2.wordCount(word);
                    }
                }
                return d1WordCount - d2WordCount;
                
            }
        }
    
        private class DocumentCompare implements Comparator<Document> {
            private String word;
            DocumentCompare(String word){
                this.word = word;
            }
            @Override
            public int compare(Document d1, Document d2) {
                return d1.wordCount(word)  - d2.wordCount(word);
                
            }
        }
        private class StringCompare implements Comparator<String>{
            @Override
            public int compare(String d1, String d2){
                return d1.compareTo(d2);
            }
        }





        @Test

        public void getAllSorted() {
            StringCompare stringComparator = new StringCompare();
//            DocumentCompare comparator = new DocumentCompare("Key1");
            ArrayList<String> key1 = new ArrayList<>();
            key1.add("Value1");
            key1.add("Value2");
            key1.add("Value3");

            System.out.println(this.trie.getAllSorted("Key1", stringComparator));
            assertNotEquals(null, this.trie.getAllSorted("Key1", stringComparator));
            assertEquals(key1, this.trie.getAllSorted("Key1", stringComparator));

            assertEquals(key1, this.trie.getAllSorted("Key1", stringComparator));

            // assertEquals(key1 , this.trie.getAllSorted("Key1", comparator));
            // assertEquals("Value2",this.trie.get("Key2"));
            // assertEquals("Value3",this.trie.get("Key3"));
            // assertEquals("Value4",this.trie.get("Key4"));
            // assertEquals("Value5",this.trie.get("Key5"));
        }

        @Test
        public void getAllWithPrefixSorted(){
            StringCompare stringComparator = new StringCompare();
            ArrayList<String> keyPrefixes = new ArrayList<>();
            keyPrefixes.add("Value1");
            keyPrefixes.add("Value2");
            keyPrefixes.add("Value3");
            keyPrefixes.add("Value4");
            keyPrefixes.add("Value5");
            keyPrefixes.add("Value6");
            assertEquals(keyPrefixes, this.trie.getAllWithPrefixSorted("Key", stringComparator));
            //test not prefixes
            ArrayList<String> prefix = new ArrayList<>();
            prefix.add("Value1");
            prefix.add("Value2");
            prefix.add("Value3");
            this.trie.getAllWithPrefixSorted("Key1", stringComparator);
            assertEquals(prefix, this.trie.getAllWithPrefixSorted("Key1", stringComparator));


        }
        @Test
        public void deleteAllWithPrefix(){
            StringCompare stringComparator = new StringCompare();
            Set<String> allValues = new HashSet();
            allValues.add("Value1");
            allValues.add("Value2");
            allValues.add("Value3");
            allValues.add("Value4");
            allValues.add("Value5");
            allValues.add("Value6");//delete all values of the prefix tree by calling delete all with prefix on a prefix common to all values
            assertEquals(allValues, this.trie.deleteAllWithPrefix("Key"));

            ArrayList emptyList = new ArrayList();//Check that the values are actually deleted
            assertEquals(emptyList, this.trie.getAllWithPrefixSorted("Key", stringComparator));

        }

        @Test
        public void deleteAll(){

            Set<String> allValues = new HashSet();
            allValues.add("Value1");
            allValues.add("Value2");
            allValues.add("Value3");
            ArrayList<String> emptyList = new ArrayList<>();
            assertEquals(allValues,this.trie.deleteAll("Key1"));
            StringCompare stringComparator = new StringCompare();
            //Check that the values were deleted
            assertEquals(emptyList, this.trie.getAllSorted("key1", stringComparator));
            allValues.clear();
            allValues.add("Value2");
            assertEquals(allValues,this.trie.deleteAll("Key2"));


        }
        @Test
        public void delete(){
            StringCompare stringComparator = new StringCompare();
            Set<String> allValues = new HashSet();
            assertEquals("Value2", this.trie.delete("Key1","Value2" ));
            allValues.add("Value1");
            allValues.add("Value3");
            ArrayList allValueslist = new ArrayList();
            allValueslist.add("Value1");
            allValueslist.add("Value3");
            assertEquals(allValueslist, this.trie.getAllSorted("Key1",stringComparator));
        }
        @Test
        public void deleteAllValues(){
            StringCompare stringComparator = new StringCompare();
            this.trie.deleteAll("k");
            ArrayList emptyList = new ArrayList();
            assertEquals(emptyList, this.trie.getAllSorted("k", stringComparator));
        }





        // @Test
        // public void testGetChained() {
        //     //second node in chain
        //     assertEquals("Value6",this.trie.get("Key6"));
        //     //second node in chain after being modified
        //     this.trie.put("Key6","Value6+1");
        //     assertEquals("Value6+1",this.trie.get("Key6"));
        //     //check that other values still come back correctly
        //     testGet();
        // }
        // @Test
        // public void testGetMiss() {
        //     assertEquals(null,this.trie.get("Key20"));
        // }
        // @Test
        // public void testPutReturnValue() {
        //     assertEquals("Value3",this.trie.put("Key3","Value3+1"));
        //     assertEquals("Value6",this.trie.put("Key6", "Value6+1"));
        //     assertEquals(null,this.trie.put("Key7","Value7"));
        // }
        // @Test
        // public void testGetChangedValue () {
        //     HashtrieImpl<String, String> trie = new HashtrieImpl<String, String>();
        //     String key1 = "hello";
        //     String value1 = "how are you today?";
        //     String value2 = "HI!!!";
        //     trie.put(key1, value1);
        //     assertEquals(value1,trie.get(key1));
        //     trie.put(key1, value2);
        //     assertEquals(value2,trie.get(key1));
        // }
        // @Test
        // public void testDeleteViaPutNull() {
        //     HashtrieImpl<String, String> trie = new HashtrieImpl<String, String>();
        //     String key1 = "hello";
        //     String value1 = "how are you today?";
        //     String value2 = null;
        //     trie.put(key1, value1);
        //     trie.put(key1, value2);
        //     assertEquals(value2,trie.get(key1));
        // }
        // @Test
        // public void testSeparateChaining () {
        //     HashtrieImpl<Integer, String> trie = new HashtrieImpl<Integer, String>();
        //     for(int i = 0; i <= 23; i++) {
        //         trie.put(i, "entry " + i);
        //     }
        //     assertEquals("entry 12",trie.put(12, "entry 12+1"));
        //     assertEquals("entry 12+1",trie.get(12));
        //     assertEquals("entry 23",trie.get(23));
        // }
    
}
