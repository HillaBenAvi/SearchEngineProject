package Model;

import java.util.ArrayList;

public class DocumentData {

    private String docID;
    private int uniqueWords;
    private int mostCommonWord;
    private int length;
    private String commonWordName;
    private ArrayList<String> commonEntities;

    public DocumentData(String docID) {
        this.docID = docID;
        commonWordName = "";
    }

    public String getDocID() {
        return docID;
    }

    public int getUniqueWords() {
        return uniqueWords;
    }

    public int getMostCommonWord() {
        return mostCommonWord;
    }

    public int getLength() {
        return length;
    }

    public String getCommonWordName() {
        return commonWordName;
    }

    public void setDocID(String docID) {
        this.docID = docID;
    }

    public void setUniqueWords(int uniqueWords) {
        this.uniqueWords = uniqueWords;
    }

    public void setMostCommonWord(int mostCommonWord) {
        this.mostCommonWord = mostCommonWord;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setCommonWordName(String commonWordName) {
        this.commonWordName = commonWordName;
    }
}
