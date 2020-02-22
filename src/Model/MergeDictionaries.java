package Model;

import javafx.util.Pair;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.TreeMap;

public class MergeDictionaries {

    private Hashtable <String, LinkedList<Pair<String, Integer>>> mergedDictionary;

    public MergeDictionaries() {
        mergedDictionary = new Hashtable<>();
    }

    /**
     * add all the terms in specific doc to the mergeDictionary
     * @param docID - documents id
     * @param termsInDoc - terms in the documents with docId
     */
    public void merge (String docID, Hashtable <String, Integer> termsInDoc) {
        if(termsInDoc.containsKey("")){
            termsInDoc.remove("");
        }
        else if (termsInDoc.containsKey(" ")){
            termsInDoc.remove(" ");
        }
        for ( String term : termsInDoc.keySet()) {
            if (mergedDictionary.containsKey(term)) {
                Pair<String, Integer> newPair = new Pair<>(docID, termsInDoc.get(term));
                (mergedDictionary.get(term)).add(newPair);
            }
            else if (mergedDictionary.containsKey(term.toLowerCase())){
                Pair<String, Integer> newPair = new Pair<>(docID, termsInDoc.get(term));
                (mergedDictionary.get(term.toLowerCase())).add(newPair);
            }
            else if (mergedDictionary.containsKey(term.toUpperCase())){
                LinkedList<Pair<String, Integer>> list = mergedDictionary.get (term.toUpperCase());
                mergedDictionary.remove(term.toUpperCase());
                Pair<String, Integer> newPair = new Pair<>(docID, termsInDoc.get(term));
                list.add(newPair);
                mergedDictionary.put(term, list);
            }
            else {
                Pair<String, Integer> newPair = new Pair<>(docID, termsInDoc.get(term));
                LinkedList<Pair<String, Integer>> newList = new LinkedList<>();
                newList.add(newPair);
                mergedDictionary.put(term, newList);
            }
        }
    }

    public Hashtable<String, LinkedList<Pair<String, Integer>>> getMergedDictionary (){
        return mergedDictionary;
    }


}
