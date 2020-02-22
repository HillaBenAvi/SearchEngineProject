package ViewModel;

import Model.ProcessManager;
import Model.Term;
import Model.SearchManager;
import javafx.util.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class MyViewModel {

    private ProcessManager processManager;
    private SearchManager searchManager;

    public MyViewModel(String corpusPath, String indexesPath, boolean stem){
        processManager = new ProcessManager(corpusPath, indexesPath, stem);

    }

    public void startIndexing (boolean stem) throws IOException, ClassNotFoundException {
        processManager.manage(stem);
    }


    public Hashtable<String, Term> getDictionary (){
        return processManager.getDictionary();
    }

    public ArrayList<String> getDictionarySortedKeys (){
        return processManager.getDictionarySortedList();
    }

    public void loadDictionary (boolean stem) throws IOException {
        processManager.loadDictionaryFromFile(stem);
    }

    public void search (String query, boolean isFile, boolean semanticModel, String indexesPath, String resultsPath, boolean stem){
        searchManager = new SearchManager(indexesPath, processManager.getDictionary(), processManager.getDocuments());
        searchManager.search(query, isFile, semanticModel, resultsPath, stem);
    }

    public void saveResults (String resultsPath){
        searchManager.saveResults(resultsPath);
    }

    public Hashtable<String, List<Pair<String,Double>>> getResults (){
        return searchManager.getResults();
    }

    public List <Pair<String, Double>> getTopEntities (String docNo, boolean stem){
        return searchManager.getTopEntities(docNo);

    }

    public int getDocsNum (){
        return processManager.getDocuments().size();
    }

}
