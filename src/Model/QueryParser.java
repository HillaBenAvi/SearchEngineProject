package Model;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;

public class QueryParser extends AParser {
    private Hashtable<String, Integer> queryTitleTerms;
    private Hashtable<String, Integer> queryDescriptionTerms;
    private Query query;
    private HashSet<String> stopWords;

    public QueryParser (Query _query, HashSet<String> _stopWords)
    {
        stopWords = _stopWords;
        query = _query;
    }

    @Override
    /**
     * parse query
     */
    public void parse() {
        Hashtable<String, Integer> queryTitleTermsLower = parseText(query.getTitle().toLowerCase(), stopWords);
        queryTitleTerms = parseText(query.getTitle(), stopWords);
        queryTitleTerms.putAll(queryTitleTermsLower);

        Hashtable<String, Integer> queryDescriptionTermsLower = parseText(query.getTitle().toLowerCase(), stopWords);
        queryDescriptionTerms = parseText(query.getDescription(), stopWords);
        queryDescriptionTerms.putAll(queryDescriptionTermsLower);
    }

    /**
     * @return pair of two lists- the first is the terms in the title and the second is the terms in the description
     */
    public Pair<ArrayList<String>,ArrayList<String>> getQueryTerms() {
        ArrayList<String> titleTermsList = new ArrayList<>();
        titleTermsList.addAll(queryTitleTerms.keySet());
        ArrayList<String> descriptionTermsList = new ArrayList<>();
        descriptionTermsList.addAll(queryDescriptionTerms.keySet());
        return new Pair<>(titleTermsList, descriptionTermsList);
    }
}
