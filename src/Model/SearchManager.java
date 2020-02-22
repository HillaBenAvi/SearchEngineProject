package Model;

import com.medallia.word2vec.util.IO;
import javafx.util.Pair;

import java.io.*;
import java.util.*;

public class SearchManager {

    private Hashtable<String, Term> dictionary;
    private Hashtable<String, DocumentData> documents;
    private String postingPath;
    private Searcher searcher;
    private Hashtable<String, List<Pair<String, Double>>> results;

    public SearchManager(String postingPath, Hashtable<String, Term> dictionary,  Hashtable<String, DocumentData> documents ){
        this.postingPath = postingPath;
        this.dictionary = dictionary;
        this.documents = documents;
        this.searcher = new Searcher(dictionary, documents, postingPath);
        this.results = new Hashtable<>();
    }


    //the function will receive the query/path to query file and call the searcher to find the relevant documents by
    //calling the searcher.
    public void search (String query, boolean isFile,  boolean semanticModel, String resultsPath, boolean stem){

        this.searcher = new Searcher(dictionary, documents, postingPath);
        ReadQuery reader = new ReadQuery();
        String fileSeparator = System.getProperty("file.separator");
        String stopWordsPath =  postingPath + fileSeparator + "StopWords.txt";
        reader.setStopWordsPath(stopWordsPath);
        ArrayList<Query> queries = new ArrayList<>();
        HashSet<String> stopWords = reader.getStopWords();
        if (isFile){ // query is a path to file
            queries = reader.readQueriesFromFile(query);
        }
        else {
            Query queryToAdd = new Query ("000" , query, "", "");
            queries.add(queryToAdd);
        }
        Hashtable<String, List <Pair<String,Double>>> results = new Hashtable<>();
        // send to the searcher every query every iteration
        for (Query q : queries){
            List <Pair<String,Double>> relevantDocuments = searcher.search(q , semanticModel, stopWords, stem);
            results.put(q.getQueryId(), relevantDocuments);
        }
        searcher.createEntities(stem);
        this.results = results;

    }

    public void saveResults(String resultsPath) {
        try{
            String fileSeparator = System.getProperty("file.separator");
            String stopWordsFilePath =  resultsPath + fileSeparator + "results.txt";
            File resultsFile = new File(stopWordsFilePath);
            boolean file =resultsFile.createNewFile();
            FileWriter fileWriter = new FileWriter(resultsFile);

            LinkedList<String> sortedQueries = new LinkedList<>();
            sortedQueries.addAll(results.keySet());
            sortedQueries.sort((o1, o2) -> o1.compareTo(o2));

            for (String query: sortedQueries){
                for (Pair p : results.get(query)){
                    fileWriter.write(query + " 0 " + p.getKey() + " 500 32.5 h" );
                    fileWriter.write("\n");
                }
            }
            fileWriter.close();
        }
        catch( IOException e ){
            e.printStackTrace();
        }
    }

    public Hashtable<String, List <Pair<String,Double>>> getResults (){
        return results;
    }

    public List <Pair<String, Double>> getTopEntities (String docNo){
        return searcher.getTopEntities(docNo);
    }


}


















