package Model;

import javafx.util.Pair;

import java.util.*;

public class DocsParser extends AParser {

    //String is the name of the doc and hash table is the dictionary of every doc.
    //hashTable - term and the number of times the term appears in the doc.
    private ArrayList<Document> docsToParse;
    private HashSet<String> stopWords;

    private ArrayList<Pair< String, Hashtable<String, Integer>>>  listParsedDoc;

    public static ArrayList<DocumentData> documentsData; //list of documentsData and the data each document.

    public DocsParser(ArrayList<Document> _docsToParse, HashSet<String> _stopWords) {
        docsToParse = _docsToParse;
        stopWords = _stopWords;
        documentsData = new ArrayList<>();
        listParsedDoc = new ArrayList<>();
    }

    @Override
    /**
     * parse documents
     */
    public void parse() {
        listParsedDoc = new ArrayList<>();
        for(Document doc : docsToParse) {

            String text = doc.getText();

            Hashtable<String, Integer> docDictionary = parseText(text, stopWords);

            listParsedDoc.add(new Pair<>(doc.getDocNo(), docDictionary));

            DocumentData document = new DocumentData(doc.getDocNo());
            document.setUniqueWords(docDictionary.size());
            int maxWord = 0;
            int lengthOfDoc = 0;
            for(String key : docDictionary.keySet()){
                lengthOfDoc = lengthOfDoc + docDictionary.get(key);
                if(docDictionary.get(key) > maxWord){
                    maxWord = docDictionary.get(key);
                }
            }
            document.setMostCommonWord(maxWord);
            document.setLength(lengthOfDoc);
            documentsData.add(document);
        }
    }

    /**
     *
     * @return the list of parsed documents
     */
    public ArrayList<Pair<String, Hashtable<String, Integer>>> getListParsedDoc() {
        return listParsedDoc;
    }

}//class Model.Parser



