package Model;

import javafx.util.Pair;

import java.util.*;

public class Ranker {

    public Ranker (){ }


    /**
     * this function gets all the terms of the query and the relevant docs for each term and
     * ranks the docs
     * @param docsWithTheirTerms - all the documents with their terms and number of appearances .
     * @param numOfDocs - number of the documents in the corpus
     * @param avgDocsLength - average of the the length of all the documents
     * @return the(maximum) 50 most relevant docs.
     */
    public  List <Pair<String,Double>> rank(Hashtable<DocumentData, Hashtable<Term, Pair<Integer, Boolean>>> docsWithTheirTerms, int numOfDocs, double avgDocsLength) {
        LinkedList <Pair<String,Double>> rankedDocs = new LinkedList<>();
        for ( DocumentData docData: docsWithTheirTerms.keySet()){
            double bm25 = BM25(docData, docsWithTheirTerms.get(docData), numOfDocs, avgDocsLength);
            Double score = bm25 ;
            rankedDocs.add(new Pair(docData.getDocID(), score));
        }
        return getTopRanked(rankedDocs);
    }

    /**
     * sort the list of the documents by their score
     * @param rankedDocs - documents with their score
     * @return the first 50 documents that ranked highest in the list
     */
    private List<Pair<String, Double>> getTopRanked(LinkedList <Pair<String,Double>> rankedDocs) {
        rankedDocs.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        int returnListSize = Math.min(50, rankedDocs.size());
        return rankedDocs.subList(0, returnListSize);
    }

    /**
     * calculates the score of document with query according by the BM25 function
     * @param doc - specific document
     * @param termsInDoc - hashtable with terms and number of appearances in the doc
     * @param numOfDocs - number of the documents in the corpus
     * @param avgDocLength - the average of the length of all documents
     * @return the score of the document to the specific query
     **/
    private double BM25 (DocumentData doc, Hashtable <Term , Pair<Integer, Boolean>> termsInDoc, int numOfDocs, double avgDocLength){

        double titleValue = 0.8;
        double descriptionValue = 0.6;

        ArrayList <Term> termsInDocList = new ArrayList<>();
        termsInDocList.addAll(termsInDoc.keySet());
        double score = 0;
        double b =0.5, k1= 1.6;

        for (Term term: termsInDocList ){
            double idf = term.calculateIDF(numOfDocs);
            double termScore = idf * ( ((termsInDoc.get(term).getKey()*(k1+1))) / (termsInDoc.get(term).getKey() + k1 * (1 - b + ( b * (doc.getLength()/ avgDocLength) ) ) ));
            if(termsInDoc.get(term).getValue()){ // the term is in the title of the query
                score = score + (titleValue * termScore);
            }
            else{
                score = score + (descriptionValue * termScore);
            }
        }

        return score;
    }



}
