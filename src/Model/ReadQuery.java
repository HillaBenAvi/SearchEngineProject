package Model;

import java.io.File;
import java.util.ArrayList;

public class ReadQuery extends AReader {
    /**
     * read queries from file of queries
     * @param path - path of the file with all the queries
     * @return list of queries
     */
    public ArrayList<Query> readQueriesFromFile (String path){
        ArrayList<Query> queries = new ArrayList<>();
        String text = "";
        try{
            text = fileToString(path);
            queries = getQueriesFromText(text);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return queries;
    }


    private ArrayList<Query> getQueriesFromText(String text) {
        ArrayList<Query> queries = new ArrayList<>();

        String [] queriesTexts = text.split("<top>"); //every cell in this array is a query

        for(int i=1; i<queriesTexts.length; i++){ //split every query by the defined domains
            String [] removeTop = queriesTexts[i].split("</top>"); //remove </top>
            String [] removeNarr = removeTop[0].split("<narr>");
            String narr = removeNarr[1]; //after the narr tag
            String [] removeDescription = removeNarr[0].split("<desc>");
            String description = removeDescription [1];
            String [] removeTitle = removeDescription[0].split("<title>");
            String title = removeTitle[1];
            String [] removeNum = removeTitle[0].split("Number:");
            String num = removeNum[1];
            while (num.charAt(0) == ' '){
                num = num.substring(1);
            }

            Query query = new Query(num, title, description, narr);
            queries.add(query);

        }
        return queries;
    }

    public void setStopWordsPath (String stopWordsFilePath){
        super.stopWordsFilePath = stopWordsFilePath;
    }


}
