package Model;

import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public abstract class AParser {

    public abstract void parse ();

    /**
     * the method gets a text presented by string and parse it to terms
     * @param text - the text we need to parse
     * @param stopWords- list of the stop words that will be ignored
     * @return hash table. The keys are the terms that appears in the text. The value of every key is the number of appears of every term in the text.
     */
    protected Hashtable<String, Integer> parseText (String text, HashSet<String> stopWords){
        Hashtable<String, Integer> dictionary = new Hashtable<>();
        Hashtable<String, Integer> upperCasesDic = new Hashtable<>();
        String [] words = splitText(text);

        for (int i = 0; i < words.length; i++) {
            if (words[i].length() > 0){
                String newWord = removePunctuation(words[i]);
                words[i] = newWord;
            }
        }
        String term = "";
        int i = 0;

        while (i < words.length) {

            if (words[i].length()<2){
                if(words[i].isEmpty() || (words[i].length()==1 && !(Character.isDigit(words[i].charAt(0))))) {
                    i++;
                    continue;
                }

            }
            if (StringUtils.containsAny(words[i], "?;|*&<>+{}=()�¥\\")){
                i++;
                continue;
            }

            else if (isPercent(words[i])) {
                addToDictionary(dictionary, words[i]);
                i++;
            }

            else if (i < words.length - 1 && isPercent(words[i], words[i + 1])) {
                term = convertToPercent(words[i], words[i + 1]);
                addToDictionary(dictionary, term);
                i = i + 2;
            }

            else if (i < words.length - 3 && isPrice(words[i], words[i + 1], words[i + 2], words[i + 3])) {
                term = convertToPrice(words[i], words[i + 1], words[i + 2], words[i + 3]);
                addToDictionary(dictionary, term);
                i = i + 4;
            }

            else if (i < words.length - 2 && isPrice(words[i], words[i + 1], words[i + 2])) {
                term = convertToPrice(words[i], words[i + 1], words[i + 2]);
                addToDictionary(dictionary, term);
                i = i + 3;
            }

            else if (i < words.length - 1 && isPrice(words[i], words[i + 1])) {
                term = convertToPrice(words[i], words[i + 1]);
                addToDictionary(dictionary, term);
                i = i + 2;
            }

            else if (i < words.length && isPrice(words[i])) {
                term = convertToPrice(words[i]);
                addToDictionary(dictionary, term);
                i++;
            }

            else if (i < words.length - 1 && isDate(words[i], words[i + 1])) {
                term = convertToDate(words[i], words[i + 1]);
                addToDictionary(dictionary, term);
                i = i + 2;
            }

            else if (i < words.length - 1 && isHour(words[i], words[i + 1])){
                term = convertToHour(words[i], words[i + 1]);
                addToDictionary(dictionary, term);
                i = i + 2;
            }

            else if (i < words.length - 1 && isDistance(words[i], words[i + 1])){
                term = convertToDistance(words[i], words[i + 1]);
                addToDictionary(dictionary, term);
                i = i + 2;
            }

            else if ((i < words.length - 3 && words[i].equals("between") || words[i].equals("Between")) &&
                    (isNumeric(words[i + 1]) && words[i + 2].equals("and") && isNumeric(words[i + 3]))) {
                term = "between" + words[i + 1] + "and" + words[i + 3];
                addToDictionary(dictionary, term);
                i = i + 4;
            }

            else if (i < words.length - 1  && isNumeric(words[i])) {
                if (words[i + 1].equals("Thousand")) {
                    term = "" + words[i] + "K";
                    i = i + 2;
                } else if (words[i + 1].equals("Million")) {
                    term = "" + words[i] + "M";
                    i = i + 2;
                } else if (words[i + 1].equals("Billion")) {
                    term = words[i] + "B";
                    i = i + 2;
                } else if (isFraction(words[i + 1])) {
                    term = "" + words[i] + words[i + 1];
                    i = i + 2;
                } else {
                    term = convertToNumber(words[i]);
                    i++;
                }
                addToDictionary(dictionary, term);
            }
            else if(!isNumeric(words[i]) && words[i].contains(".")){
                i++;
            }
            else if (Character.isUpperCase(words[i].charAt(0)) && words[i].length()>1 && !stopWords.contains(words[i].toLowerCase())) {
                term = words[i];
                if( (term.contains("$")) || (term.contains("%"))) {
                    i++;
                    continue;
                }
                i++;
                while (i < words.length && !words[i].equals("") && Character.isUpperCase(words[i].charAt(0))
                        && !StringUtils.containsAny(words[i], "?;|*&<>+{}=()�¥\\"))  {
                    term = term + " " + words[i];
                  //  addToDictionary(upperCasesDic, term);
                    i++;
                }
                if (!stopWords.contains(term.toLowerCase())){
                    addToDictionary(upperCasesDic, term);
                }

            }

            else {
                term = words[i];
                if(term.contains("$")|| stopWords.contains(term.toLowerCase())){
                    i++;
                    continue;
                }
                    addToDictionary(dictionary, term.toLowerCase());
                i++;
            }
        }
        mergeDictionaries(dictionary, upperCasesDic);

        return dictionary;

    }

    /**
     * the method splits the text to raw terms that will be parsed.
     * @param text
     * @return
     */
    private String [] splitText (String text){
        StringBuilder word = new StringBuilder();
        List<String> wordsList = new LinkedList<>();
        for (int i=0 ; i < text.length() ; i++){
            if (text.charAt(i) == ' ' || text.charAt(i) == '?' || text.charAt(i) == '*' || text.charAt(i) == ')' || text.charAt(i) == '('
                    || text.charAt(i) == '!' || text.charAt(i) == '/' || text.charAt(i) == ';' || text.charAt(i) == '\'' || text.charAt(i) == '_' ||
                    text.charAt(i) == '\"' || (text.charAt(i) == '-' && text.charAt(i+1) == '-' )){
                wordsList.add(word.toString());
                word = new StringBuilder();
            }
            else{
                word.append(text.charAt(i));
            }

        }
        wordsList.add(word.toString());
        String [] wordsArray = new String [wordsList.size()];
        wordsArray = wordsList.toArray(wordsArray);
        return wordsArray;

    }

    private void mergeDictionaries(Hashtable<String, Integer> dictionary, Hashtable<String, Integer> upperCasesDic) {
        Enumeration<String> enumeration = upperCasesDic.keys();
        while(enumeration.hasMoreElements()) {
            String key = enumeration.nextElement();
            if (dictionary.containsKey(key.toLowerCase())){
                int counter = dictionary.get(key.toLowerCase())+ upperCasesDic.get(key);
                dictionary.put(key.toLowerCase(), counter);
                upperCasesDic.remove(key);
            }
            else {
                dictionary.put(key.toUpperCase(), upperCasesDic.get(key));
                upperCasesDic.remove(key);
            }
        }
    }

    /**
     * add term to the dictionary
     * @param dic - dictionary to add the term
     * @param term - add this term to the dictionary
     */
    private void addToDictionary(Hashtable<String, Integer> dic, String term) {
        if (dic.containsKey(term)) {
            dic.put(term, dic.get(term) + 1);
        } else {
            dic.put(term, 1);
        }
    }

    /**
     * check if the word1  is a price
     * @param word1 - word
     * @return true if the word is price, otherwise false
     */
    private boolean isPrice(String word1) {
        if (word1.charAt(0) == '$' )
            if(isNumeric(word1.substring(1)) || isNumericRange(word1.substring(1)) || word1.contains("-")) {
                return true;
            }
        return false;
    }

    /**
     * check if the words are a price
     * @param word1 - word
     * @param word2 - word
     * @return true if the words are price, otherwise false
     */
    private boolean isPrice(String word1, String word2) {
        if (word2.equals("million") || word2.equals("billion") || word2.equals("trillion")
                || word2.equals("m") || word2.equals("M") || word2.equals("bn") || word2.equals("BN")) {
            if (isPrice(word1)){
                return true;
            }
        } else if (word2.equals("Dollars") || word2.equals("dollars")) {
            if (isNumeric(word1) || isNumericRange(word1)) {
                return true;
            } else if (word1.length() > 1 &&
                    (isNumeric(word1.substring(0, word1.length() - 1)) || isNumericRange(word1.substring(0, word1.length() - 1)))) { //1M
                return true;
            } else if (word1.length() > 2 &&
                    (isNumeric(word1.substring(0, word1.length() - 2)) || isNumericRange(word1.substring(0, word1.length() - 1)))) { //2Bn
                return true;
            }
        }
        return false;
    }

    /**
     * check if the words are a price
     * @param word1 - word
     * @param word2 - word
     * @param word3 - word
     * @return true if the words are price, otherwise false
     */
    private boolean isPrice(String word1, String word2, String word3) {
        if (word3.equals("Dollars") || word3.equals("dollars")) {
            if (isFraction(word2) || word2.equals("million") || word2.equals("billion") || word2.equals("trillion")
                    || word2.equals("m") || word2.equals("M") || word2.equals("bn") || word2.equals("BN")) {
                if (isNumeric(word1) || isNumericRange(word1)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * check if the words are a price
     * @param word1 - word
     * @param word2 - word
     * @param word3 - word
     * @param word4 - word
     * @return  true if the words are price, otherwise false
     */
    private boolean isPrice(String word1, String word2, String word3, String word4) {
        if (word4.equals("Dollars") || word4.equals("dollars")) {
            if (word3.equals("U.S.")) {
                if (word2.equals("million") || word2.equals("billion") || word2.equals("trillion")) {
                    if (isNumeric(word1) || isNumericRange(word1)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * convert word to uniform pattern of price
     * @param word1 - word
     * @return word after converted
     */
    private String convertToPrice(String word1) {
        String term = word1.substring(1);
        if(isNumericRange(term)){
            term=term + " Dollars";
            return term;
        }
        if(term.contains("-")){
            term = term + " Dollars";
            return term;
        }
        double price = Double.parseDouble(term);
        if (price < 1000000) {
            term = "" + price + " Dollars";
        } else if (price >= 1000000) {
            price = price / 1000000;
            term = "" + price + " M Dollars";
        }
        return term;
    }

    /**
     * convert words to uniform pattern of price
     * @param word1 - word
     * @param word2 - word
     * @return word after converted
     */
    private String convertToPrice(String word1, String word2) {
        String term="";
        if(word1.charAt(0) == '$'){
            word1 = word1.substring(1);
        }
        if(word2.equals("Dollars") || word2.equals("dollars")) {
            if (isNumeric(word1) && word1.length()<7){
                term = word1 + " Dollars";
            }
            else if (word1.charAt(word1.length() - 1) == 'm' || word1.charAt(word1.length() - 1) == 'M') {
                term = word1.substring(0, word1.length() - 1) + " M Dollars";
            }
            else if ((word1.substring(word1.length() - 2)).equals("BN") || (word1.substring(word1.length() - 2)).equals("Bn")
                    || (word1.substring(word1.length() - 2)).equals("bn")) {
                term = word1.substring(0, word1.length() - 2) + "000 M Dollars";
            }
            else if (isNumeric(word1)) {
                double price = Double.parseDouble(word1);
                if (price < 1000000) {
                    term = word1 + " Dollars";
                }
                else if (price >= 1000000) {
                    price = price / 1000000;
                    term = "" + price + " M Dollars";
                }
            }
        }
        else if (word2.equals("million") || word2.equals("M") || word2.equals("m")) {
            if (isPrice(word1)){
                term = word1 + " M Dollars";
            }
        }
        else if (word2.equals("billion") || word2.equals("bn") || word2.equals("Bn") || word2.equals("BN")) {
            if(isPrice(word1)){
                term = word1 + "000 M Dollars";
            }
        }
        return term;
    }

    /**
     * convert words to uniform pattern of price
     * @param word1 - word
     * @param word2 - word
     * @param word3 - word
     * @return word after converted
     */
    private String convertToPrice(String word1, String word2, String word3){
        String term ="";
        if(word1.charAt(0) == '$'){
            word1 = word1.substring(1);
        }
        if (word2.equals("million") || word2.equals("M") || word2.equals("m")) {
            term = word1 + " M Dollars";
        }
        else if (word2.equals("billion") || word2.equals("bn") || word2.equals("Bn") || word2.equals("BN")) {
            term = word1 + "000 M Dollars";
        }
        else if (isFraction(word2)) {
            term = word1 + " " + word2 + " Dollars";
        }
        return term;
    }

    /**
     * convert words to uniform pattern of price
     * @param word1 - word
     * @param word2 - word
     * @param word3 - word
     * @param word4 - word
     * @return word after converted
     */
    private String convertToPrice(String word1, String word2, String word3, String word4) {
        String term = "";
        double price = Double.parseDouble(word1);
        if(word2.equals("million")){
            term = word1 + " M Dollars";
        } else if(word2.equals("billion")){
            term = word1 + "000 M Dollars";
        } else if(word2.equals("trillion")){
            term = word1 + "000000 M Dollars";
        }
        return term;
    }

    /**
     * check if the word is a percent
     * @param word1 - word
     * @return true if the word is percent, otherwise false
     */
    private boolean isPercent(String word1) {
        if (word1.charAt(word1.length() - 1) == '%') {
            return true;
        }
        return false;
    }

    /**
     * check if the words are percent
     * @param word1 - word
     * @param word2 - word
     * @return true if the words are percent, otherwise false
     */
    private boolean isPercent(String word1, String word2) {
        if ((word2.equals("percent") || word2.equals("percentage")) && (isNumeric(word1) || isNumericRange(word1))) {
            return true;
        }
        return false;
    }

    /**
     * convert words to uniform pattern of percent
     * @param word1 - word
     * @param word2 - word
     * @return true if the words are percent, otherwise false
     */
    private String convertToPercent(String word1, String word2) {
        String term = "";
        if (isNumeric(word1) || isNumericRange(word1)) {
            term = word1 + "%";
        }
        return term;
    }

    /**
     * check if the words are distance
     * @param word1 - word
     * @param word2 - word
     * @return true if the words are distance, otherwise false
     */
    private boolean isDistance (String word1, String word2) {
        if ((word2.equals("kilometers") || word2.equals("Kilometers") ||word2.equals("km") || word2.equals("KM") ||
                word2.equals("cm") || word2.equals("CM")|| word2.equals("meters") || word2.equals("Meters")) && (isNumeric(word1))) {
            return true;
        }
        return false;
    }

    /**
     *
     * @param word1
     * @param word2
     * @return
     */
    private String convertToDistance (String word1, String word2) {
        String term = "";
        if (word2.equals("meters") || word2.equals("Meters")){
            term = word1 + " meters";
        }
        else if (isNumeric(word1) && (word2.equals("kilometers") || word2.equals("Kilometers") ||word2.equals("km") || word2.equals("KM"))){
            double number = Double.parseDouble(word1);
            number = number * 1000;
            term = number + " meters";
        }
        else if (isNumeric(word1) && (word2.equals("cm") || word2.equals("CM"))){
            double number = Double.parseDouble(word1);
            number = number / 100;
            term = number + " meters";
        }
        return term;
    }

    private boolean isDate(String word1, String word2) {

        if ((!isMonth(word1).equals("-1") && isInteger(word2) && word2.length() <= 4) || (isInteger(word1) && word1.length() <= 4 && !isMonth(word2).equals("-1"))) {
            return true;
        }
        return false;

    }

    private String convertToDate(String word1, String word2) {
        String term = "";
        String month = isMonth(word1);
        if( !month.equals("-1") ){

            int number = Integer.parseInt(word2);
            if( number >=  1 && number <= 9 ){
                term = month + "-0" + word2;
            }
            else if( number >= 10 && number <= 31 ){
                term = month + "-" + word2;
            }
            else{
                term = word2 + "-" + word1;
            }
        }
        else{
            month = isMonth(word2);
            int number = Integer.parseInt(word1);
            if( number >=  1 && number <= 9 ){
                term = month + "-0" + word1;
            }
            else if( number >=10 && number <= 31 ){
                term = month + "-" + word1;
            }
        }
        return term;
    }

    private String[] isTerm(String word) {
        String[] res = {"false", "1"};
        if (word.indexOf('-') != -1) {
            res[0] = "true";
        }
        return res;
    }

    private String convertToNumber(String word) {
        String term = " ";
        Float number = Float.parseFloat(word);
        if (number < 1000){
            return word;
        }
        else if ( number >= 1000 && number < 1000000 ){
            number = number / 1000;
            term = "" + number;
            int index = term.indexOf('.');
            if(index+4 < term.length()){
                term = term.substring(0 , index) + term.substring(index , index + 4) + "K";
            }
            else{
                term = term.substring(0 , index) + term.substring(index) + "K";
            }

        }
        else if (number >= 1000000 && number < 1000000000) {
            number = number / 1000000;
            term = "" + number;
            int index = term.indexOf('.');
            if(index+4 < term.length()){
                term = term.substring(0 , index) + term.substring(index , index + 4) + "M";
            }
            else{
                term = term.substring(0 , index) + term.substring(index) + "M";
            }
        }
        else if (number >= 1000000000 && number <= Float.MAX_VALUE ){
            number = number / 1000000000;
            term = "" + number;
            int index = term.indexOf('.');
            if(index+4 < term.length()){
                term = term.substring(0 , index) + term.substring(index , index + 4) + "B";
            }
            else{
                term = term.substring(0 , index) + term.substring(index) + "B";
            }
        }
        return term;
    }

    private boolean isNumeric(String word) {
        boolean containsDigits = false;
        boolean containsChar = false;
        boolean containsDot = false;
        for (char c : word.toCharArray()) {
            if (Character.isDigit(c)){
                containsDigits = true;
            }
            if (c == '.'){
                if (containsDot){
                    return false;
                }
                else {
                    containsDot = true;
                }
            }
            if (!Character.isDigit(c) && !(c == '.')) {
                return false;
            }
        }
        return containsDigits;
    }

    private boolean isInteger(String word){
        if(word.equals("")){
            return false;
        }
        for (char c : word.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    private boolean isNumericRange(String word) {
        int index = word.indexOf('-');
        if (index == -1) {
            return false;
        }
        if (!isNumeric(word.substring(0, index)) || !isNumeric(word.substring(index + 1))) {
            return false;
        }
        return true;
    }

    private boolean isFraction(String word) {
        if (word.contains("/")){
            String [] fraction = word.split("/");
            if (fraction.length == 2 && isInteger(fraction[0]) && isInteger(fraction[0])){
                return true;
            }
        }

        return false;
    }

    private boolean isHour (String word1, String word2){
        if (isNumeric(word1) && (word2.equals("am") || word2.equals("AM")
                || word2.equals("pm") || word2.equals("PM") )){
            return true;
        }
        else if (word1.contains(":") && (word2.equals("am") || word2.equals("AM")
                || word2.equals("pm") || word2.equals("PM") ) ){
            String [] hour = word1.split(":");
            if (hour.length == 2 && hour[0].length()<=2 &&  hour[0].length()<=2 && isInteger(hour[0]) && isInteger(hour[1]) ){
                return true;
            }
        }
        return false;
    }

    private String convertToHour (String word1, String word2){
        String term = "";
        if ( word2.equals("am") || word2.equals("AM") ){
            term = "" + word1 + "AM";
        }
        if ( word2.equals("pm") || word2.equals("PM") ){
            term = "" + word1 + "PM";
        }
        return term;
    }

    /**
     * returns the number of the month the word  presents, if the word doesnt
     * present any month, returns -1.
     *
     * @param word
     * @return
     */
    private String isMonth(String word) {
        String month = "-1";
        if (word.equals("January") || word.equals("JANUARY") || word.equals("Jan") || word.equals("JAN")) {
            month = "01";
        } else if (word.equals("February") || word.equals("FEBRUARY") || word.equals("Feb") || word.equals("FEB")) {
            month = "02";
        } else if (word.equals("March") || word.equals("MARCH") || word.equals("Mar") || word.equals("MAR")) {
            month = "03";
        } else if (word.equals("April") || word.equals("APRIL") || word.equals("Apr") || word.equals("APR")) {
            month = "04";
        } else if (word.equals("May") || word.equals("MAY")) {
            month = "05";
        } else if (word.equals("June") || word.equals("JUNE") || word.equals("Jun") || word.equals("JUN")) {
            month = "06";
        } else if (word.equals("July") || word.equals("JULY") || word.equals("Jul") || word.equals("JUL")) {
            month = "07";
        } else if (word.equals("August") || word.equals("AUGUST") || word.equals("Aug") || word.equals("AUG")) {
            month = "08";
        } else if (word.equals("September") || word.equals("SEPTEMBER") || word.equals("Sep") || word.equals("SEP")) {
            month = "09";
        } else if (word.equals("October") || word.equals("OCTOBER") || word.equals("Oct") || word.equals("OCT")) {
            month = "10";
        } else if (word.equals("November") || word.equals("NOVEMBER") || word.equals("Nov") || word.equals("NOV")) {
            month = "11";
        } else if (word.equals("December") || word.equals("DECEMBER") || word.equals("Dec") || word.equals("DEC")) {
            month = "12";
        }
        return month;
    }

    private String removePunctuation(String word) {
        String result = word;

        if (result.charAt(0) == '<' || result.charAt(result.length() - 1) == '>'){
            return " ";
        }

        while (!result.equals("") && (result.charAt(0) == '"' || result.charAt(0) == '(' || result.charAt(0) == '{' || result.charAt(0) == '['
                || result.charAt(0) == '|' || result.charAt(0) == '#' || result.charAt(0) == '.' || result.charAt(0) == '%' || result.charAt(0) == '-'
                || result.charAt(0) == '&' || result.charAt(0) == '\'' || result.charAt(0) == '*' || result.charAt(0) == '/' || result.charAt(0) == '<'
                || result.charAt(0) == '+' || result.charAt(0) == '='|| result.charAt(0) == '?' || result.charAt(0) == '`' || result.charAt(0) == ']' || result.charAt(0) == '!' || result.charAt(0) == ':'
                || result.charAt(0) == ')' || result.charAt(0) == ';'|| result.charAt(0) == ' ' || result.charAt(0) == '_' || result.charAt(0) == '\\' || result.charAt(0) == '@'|| result.charAt(0) == '~')){
            if ( word.length() > 1 ){
                result = result.substring(1);
            }
            else{
                result = "";
            }
        }

        while (!result.equals("") && (result.charAt(result.length() - 1) == '"' || result.charAt(result.length() - 1) == ')' || result.charAt(result.length() - 1) == '}' ||
                result.charAt(result.length() - 1) == '.' || result.charAt(result.length() - 1) == ',' || result.charAt(result.length() - 1) == ':' ||
                result.charAt(result.length() - 1) == ';' || result.charAt(result.length() - 1) == ']' || result.charAt(result.length() - 1) == '?'
                || result.charAt(result.length() - 1) == '/' || result.charAt(result.length() - 1) == '$' || result.charAt(result.length() - 1) == '-' ||
                result.charAt(result.length() - 1) == '|' || result.charAt(result.length() - 1) == '>'|| result.charAt(result.length() - 1) == '+'
                || result.charAt(result.length() - 1) == '\'' || result.charAt(result.length() - 1) == ' ' || result.charAt(result.length() - 1) == '_' || result.charAt(result.length() - 1) == '@'
                || result.charAt(result.length() - 1) == '!' || result.charAt(result.length() - 1) == '~')) {

            result = result.substring(0, result.length() - 1);
        }

        int index = result.indexOf(',');
        while (index != -1) {
            result = result.substring(0, index) + result.substring(index + 1);
            index = result.indexOf(',');
        }

        return result;
    }




}
