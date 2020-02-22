package Model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;
import com.medallia.word2vec.Word2VecModel;
import javafx.util.Pair;

import static java.lang.Character.isUpperCase;


public class Searcher {

    private Ranker ranker;
    private String postingFilesPath;
    private Hashtable<String, Term> dictionary;
    private Hashtable<String, DocumentData> documents;
    private Hashtable<String, ArrayList<Pair<String,Integer>>> entitiesOrderByDoc; //key: document no, value: list of the entities in the document and number of appearences

    public Searcher(Hashtable<String, Term> dictionary, Hashtable<String,DocumentData> documents, String postingFilesPath) {
        this.dictionary = dictionary;
        this.ranker = new Ranker();
        this.postingFilesPath = postingFilesPath;
        this.documents = documents;
        entitiesOrderByDoc = new Hashtable<>();
    }


    // gets the query and return an arrayList of the relevant documents (after rank)
    public List <Pair<String,Double>> search (Query query, boolean semanticModel, HashSet<String> stopWords, boolean stem){
        QueryParser queryParser = new QueryParser(query, stopWords);
        queryParser.parse();
        Pair<ArrayList<String>, ArrayList<String>> queryStrings = queryParser.getQueryTerms();

        if(stem){
            Stemmer stemmer = new Stemmer();
            ArrayList<String> stemTitle = stemmer.stemQuery(queryStrings.getKey());
            ArrayList<String> stemDesc = stemmer.stemQuery(queryStrings.getValue());
            queryStrings = new Pair (stemTitle, stemDesc);
        }

        if (semanticModel){
            ArrayList<String> expandedTitle = expandQuery(queryStrings.getKey());
            ArrayList<String> expandedDescription= expandQuery(queryStrings.getValue());
            queryStrings = new Pair (expandedTitle, expandedDescription);
        }

        // get all the relevant docs ordered by the terms
        Hashtable<String,Hashtable<String, Pair<Integer, Boolean >>> allRelevantDocsOrderedByTerms = getRelevantDocuments(queryStrings.getKey(), queryStrings.getValue(), stem);

        //order all the relevant docs by documents
        Hashtable<DocumentData, Hashtable<Term, Pair<Integer, Boolean>>> allRelevantDocsOrderedByDoc = termsListToDocsList(allRelevantDocsOrderedByTerms);

        double avgDocLength = calculateDocsAvqLength();

        //send to the ranker for rank the docs, get the 50 most relevant.
        List <Pair<String,Double>> relevantRankedDocs = ranker.rank(allRelevantDocsOrderedByDoc, documents.size(), avgDocLength);

        return relevantRankedDocs;
    }


    /**
     * calculate the average of the length of all documents
     * @return average of the length of all documents.
     */
    private double calculateDocsAvqLength() {
        int totalLength = 0;
        for (String doc: documents.keySet()) {
            DocumentData docData = documents.get(doc);
            totalLength = totalLength + docData.getLength();
        }
        return totalLength/documents.size();
    }


    /**
     * @param queryTitleTerms - list of terms in the query
     * @param queryDescriptionTerms - list of terms in the description of the query
     * @param stem - if it was stemming on the corpus
     * @return a list of terms in the query and their relevant documents with number of apperences of each term in each document.
     */
    private  Hashtable<String,Hashtable<String, Pair<Integer, Boolean >>> getRelevantDocuments ( ArrayList<String> queryTitleTerms,  ArrayList<String> queryDescriptionTerms, boolean stem ){
        //listOfTerms- key: a term in the query,
        //             value: all the documents the term appears in, and number of appears in each one.

        Hashtable<String,Hashtable<String, Pair<Integer, Boolean >>> listOfTerms = new Hashtable<>();
        for(String term : queryTitleTerms){
            if (dictionary.containsKey(term)){
                Hashtable<String, Pair< Integer, Boolean >> termsWithNumOfAppearencesInEveryDoc = findRelevantDocsForTerm(term, true, stem);
                listOfTerms.put(term, termsWithNumOfAppearencesInEveryDoc);
            }
            if (dictionary.containsKey(term.toUpperCase())){
                Hashtable<String, Pair< Integer, Boolean >> termsWithNumOfAppearencesInEveryDoc = findRelevantDocsForTerm(term.toUpperCase(), true, stem);
                listOfTerms.put(term.toUpperCase(), termsWithNumOfAppearencesInEveryDoc);
            }

        }

        for(String term : queryDescriptionTerms){
            if (dictionary.containsKey(term)){
                Hashtable<String, Pair< Integer, Boolean >> termsWithNumOfAppearencesInEveryDoc = findRelevantDocsForTerm(term, false, stem);
                listOfTerms.put(term, termsWithNumOfAppearencesInEveryDoc);
            }
            if (dictionary.containsKey(term.toUpperCase())){
                Hashtable<String, Pair< Integer, Boolean >> termsWithNumOfAppearencesInEveryDoc = findRelevantDocsForTerm(term.toUpperCase(), false, stem);
                listOfTerms.put(term.toUpperCase(), termsWithNumOfAppearencesInEveryDoc);
            }

        }

        return listOfTerms;
    }




    /** finds all the relevant documents for the term
     * @param term from the query
     * @return all the documents the term appears in.
     */
    private Hashtable<String, Pair<Integer, Boolean >> findRelevantDocsForTerm (String term, boolean isInTitle, boolean stem){
        Hashtable<String, Pair<Integer, Boolean >> documents = new Hashtable();

        try{
            //read the relevant line from the posting file
            Stream<String> lines = Files.lines(Paths.get(findPostingPathForTerm(term, stem)));
            String line = lines.skip(dictionary.get(term).getLocationInPosting()).findFirst().get();

            //parsing the line
            String [] separatedLine = line.split(";");
            for(int i=1; i<separatedLine.length; i++){
                String [] doc = separatedLine[i].split(",");
                String docNo = doc[0];
                String appears = doc[1];
                documents.put(docNo, new Pair (Integer.parseInt(appears), isInTitle));
            }
        }

        catch (Exception e){
        }

        return documents;

    }

    /**
     *
     * @param term - a word
     * @param stem - if the corpus stemmed
     * @return path for the posting file that the word appears in
     */
    private String findPostingPathForTerm (String term, boolean stem){
        String fileSeparator = System.getProperty("file.separator");
        String filePath = postingFilesPath + fileSeparator;
        if (!stem) {
            if (term.charAt(0)>='0' && term.charAt(0) <= '9'){
                return filePath + "NUMBERS.txt";
            }
            else if (term.charAt(0)=='a' || term.charAt(0) == 'b' || term.charAt(0) == 'A' || term.charAt(0) == 'B'){
                return filePath + "AB.txt";
            }
            else if (term.charAt(0)=='c' || term.charAt(0) == 'd' || term.charAt(0) == 'C' || term.charAt(0) == 'D'){
                return filePath + "CD.txt";
            }
            else if ((term.charAt(0)>='e' && term.charAt(0) <= 'h') || (term.charAt(0) >= 'E' && term.charAt(0) <= 'H')){
                return filePath + "EFGH.txt";
            }
            else if ((term.charAt(0)>='i' && term.charAt(0) <= 'l') || (term.charAt(0) >= 'I' && term.charAt(0) <= 'L')){
                return filePath + "IJKL.txt";
            }
            else if ((term.charAt(0)>='m' && term.charAt(0) <= 'o') || (term.charAt(0) >= 'M' && term.charAt(0) <= 'O')){
                return filePath + "MNO.txt";
            }
            else if (term.charAt(0)=='p' || term.charAt(0) == 'q' || term.charAt(0) == 'P' || term.charAt(0) == 'Q'){
                return filePath + "PQ.txt";
            }
            else if (term.charAt(0)=='r' || term.charAt(0) == 's' || term.charAt(0) == 'R' || term.charAt(0) == 'S'){
                return filePath + "RS.txt";
            }
            else if ((term.charAt(0)>='t' && term.charAt(0) <= 'z') || (term.charAt(0) >= 'T' && term.charAt(0) <= 'Z')) {
                return filePath + "TUVWXYZ.txt";
            }
        }
        else{
            if (term.charAt(0)>='0' && term.charAt(0) <= '9'){
                return filePath + "SNUMBERS.txt";
            }
            else if (term.charAt(0)=='a' || term.charAt(0) == 'b' || term.charAt(0) == 'A' || term.charAt(0) == 'B'){
                return filePath + "SAB.txt";
            }
            else if (term.charAt(0)=='c' || term.charAt(0) == 'd' || term.charAt(0) == 'C' || term.charAt(0) == 'D'){
                return filePath + "SCD.txt";
            }
            else if ((term.charAt(0)>='e' && term.charAt(0) <= 'h') || (term.charAt(0) >= 'E' && term.charAt(0) <= 'H')){
                return filePath + "SEFGH.txt";
            }
            else if ((term.charAt(0)>='i' && term.charAt(0) <= 'l') || (term.charAt(0) >= 'I' && term.charAt(0) <= 'L')){
                return filePath + "SIJKL.txt";
            }
            else if ((term.charAt(0)>='m' && term.charAt(0) <= 'o') || (term.charAt(0) >= 'M' && term.charAt(0) <= 'O')){
                return filePath + "SMNO.txt";
            }
            else if (term.charAt(0)=='p' || term.charAt(0) == 'q' || term.charAt(0) == 'P' || term.charAt(0) == 'Q'){
                return filePath + "SPQ.txt";
            }
            else if (term.charAt(0)=='r' || term.charAt(0) == 's' || term.charAt(0) == 'R' || term.charAt(0) == 'S'){
                return filePath + "SRS.txt";
            }
            else if ((term.charAt(0)>='t' && term.charAt(0) <= 'z') || (term.charAt(0) >= 'T' && term.charAt(0) <= 'Z')) {
                return filePath + "STUVWXYZ.txt";
            }
        }
        return null;
    }

    /**
     * this function converts the data structure from terms with their documents to documents with their terms
     * @param terms - data structure that contains all the terms with their documents for each term.
     * @return data structure that contains all the documents with their term for each document.
     */
    private Hashtable<DocumentData, Hashtable<Term, Pair<Integer, Boolean >>> termsListToDocsList ( Hashtable<String,Hashtable<String,Pair<Integer, Boolean >>> terms){
        // terms is the output hashtable of getRelevantDocuments
        //hashTable of all the relevant docs. each doc has the terms from the query that appears in the doc.
        Hashtable<DocumentData, Hashtable<Term, Pair<Integer, Boolean >>> docsList = new Hashtable<>();

        for (String term : terms.keySet()){
            for (String docNo: terms.get(term).keySet()){ //all the documents the term appears in
                DocumentData docData = documents.get(docNo);
                Pair appearsAndIsInTitle = terms.get(term).get(docNo);
                if (!docsList.containsKey(docData)) {
                    docsList.put(docData, new Hashtable<>());
                }
                if (dictionary.containsKey(term.toUpperCase())){
                    docsList.get(docData).put(dictionary.get(term.toUpperCase()), appearsAndIsInTitle);
                }
                else if (dictionary.containsKey(term.toLowerCase())){
                    docsList.get(docData).put(dictionary.get(term.toLowerCase()), appearsAndIsInTitle);
                }
                //if the dictionary doesn't contain the term, it will not be in the hashtable.
            }
        }
        return docsList;
    }


    /**
     * function that implements the semantic model.
     * @param query - list of term of the query
     * @return receive a list that contains the words in the query and return a list of the expanded query
     */
    private ArrayList<String> expandQuery (ArrayList<String> query){
        ArrayList<String> toReturn=new ArrayList<>();
        toReturn.addAll(query);
        try {
            for (int i = 0; i < query.size(); i++) {
                Word2VecModel model = Word2VecModel.fromTextFile(new File( "word2vec.c.output.model.txt"));
                com.medallia.word2vec.Searcher searcher = model.forSearch();
                int num = 3;
                try{
                    List<com.medallia.word2vec.Searcher.Match> matches = searcher.getMatches(query.get(i), num);
                    for (com.medallia.word2vec.Searcher.Match match : matches) {
                        match.match();
                    }
                    for (int j = 1; j < matches.size(); j++) {
                        String temp=matches.get(j).toString();
                        String[] arr=temp.split("\\[");
                        arr[1]=arr[1].substring(0,arr[1].length()-1);
                        // if(Double.parseDouble(arr[1])>0.95)
                        toReturn.add(arr[0]);
                    }
                }
                catch(com.medallia.word2vec.Searcher.UnknownWordException e){
                    continue;
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return toReturn;

    }


    /**
     * create the list of entities from the posting file
     * @param stem - if the posting file saved with S in the beginning of the name
     */
    public void createEntities(boolean stem) {
        entitiesOrderByDoc = new Hashtable<>();
        String fileSeparator = System.getProperty("file.separator");
        String entitiesPath =  postingFilesPath + fileSeparator + "Entities.txt";
        if(stem){
            entitiesPath =  postingFilesPath + fileSeparator + "SEntities.txt";
        }
        try {
            FileReader reader = new FileReader(new File(entitiesPath));
            BufferedReader bf = new BufferedReader(reader);
            String line = bf.readLine();
            while (line != null) {
                String[] separatedLine = line.split(";");
                for (int j = 1; j < separatedLine.length; j++) {
                    String[] doc = separatedLine[j].split(",");
                    String docNo = doc[0];
                    String appears = doc[1];
                    if (!entitiesOrderByDoc.containsKey(docNo)) {
                        entitiesOrderByDoc.put(docNo, new ArrayList<>());
                    }
                    entitiesOrderByDoc.get(docNo).add(new Pair(separatedLine[0], Integer.parseInt(appears)));
                }
                line = bf.readLine();
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * this function ranks the entities of a document
     * @param docNo - id of the document
     * @return list of top entities of the document
     */
    public List <Pair<String, Double>> getTopEntities (String docNo){

        ArrayList <Pair <String, Double>> rankedEntities = new ArrayList<>();
        DocumentData docData = documents.get(docNo);
        ArrayList<Pair<String,Integer>> entities = entitiesOrderByDoc.get(docNo);
        for (Pair entity: entities){
            double tf = ((int) entity.getValue()) / (double)docData.getLength();
            double idf = dictionary.get(entity.getKey()).calculateIDF(documents.size());
            double score = tf * idf;
            rankedEntities.add(new Pair(entity.getKey(), score));
        }

        rankedEntities.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        int returnListSize = Math.min(5, rankedEntities.size());

        return rankedEntities.subList(0, returnListSize);
    }

}
