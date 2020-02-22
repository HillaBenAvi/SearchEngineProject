package Model;

import javafx.util.Pair;

import java.io.*;
import java.util.*;

import static java.lang.Character.*;

public class Indexer {
    private Hashtable<String, Term> dictionary;
    private String path;
    private int batchNum;
    private boolean toStem;
    private Hashtable<String, DocumentData> documents;
    private  Hashtable <String, LinkedList< Pair <String,Integer>>> entities; //key: term, value: pair- key: docNo, value- appears

    public Indexer(String path, boolean toStem) {
        dictionary = new Hashtable<>();
        this.path = path;
        this.toStem = toStem;
        batchNum = 0;
        entities = new Hashtable<>();
    }

    /**
     * this function indexes the documents, add all the terms to the dictionary and write the temporary posting files
     * @param toIndex - dictionary to indexes
     * @param stem - if the process includes the stemming process
     * @throws IOException
     */
    public void indexing(Hashtable <String, LinkedList<Pair<String, Integer>>> toIndex, boolean stem) throws IOException{
        this.toStem = stem;
        createTempFiles();
        ArrayList<String> sortedKeys = getSortedKeys(toIndex.keySet());
        String input = sortedKeys.get(0);
        String indexFilePath = getTempFilePath(input);
        File currentFile = new File (indexFilePath);
        FileWriter currentWriter = new FileWriter(currentFile);
        BufferedWriter currentBuffer = new BufferedWriter(currentWriter);
        StringBuilder string = new StringBuilder();
        for( int i=0; i<sortedKeys.size(); i++ ) {
            String term = sortedKeys.get(i);
            if (!belongToSameTempFile(input, term)) { //update the relevant file if necessary
                currentBuffer.append(string.toString());
                currentBuffer.close();
                currentWriter.close();
                indexFilePath = getTempFilePath(term);
                currentFile = new File(indexFilePath);
                currentWriter = new FileWriter(currentFile);
                currentBuffer = new BufferedWriter(currentWriter);
                input = term;
                string = new StringBuilder();
            }
            string.append(getTermString(term, toIndex.get(term)));
            string.append("\n");
        }
        currentBuffer.append(string.toString());
        currentBuffer.close();
        currentWriter.close();
        batchNum ++;
    }

    private String getTermString (String term, LinkedList<Pair<String, Integer>> list){
        String result = "" + term;
        for (Pair<String, Integer> pair : list){
            result = result + ";" + pair.getKey() + "," + pair.getValue() ;
        }
        return result;
    }

    private LinkedList<Pair<String, Integer>> getDocsListFromString (String [] line){
        LinkedList<Pair<String, Integer>> docsList = new LinkedList<>();

        for (int i=1; i<line.length; i++){
            String [] doc = line[i].split(",");
            Pair <String, Integer> pair = new Pair<>(doc[0], Integer.parseInt(doc[1]));
            docsList.add(pair);
        }
        return docsList;
    }

    /**
     * merge the temporary posting files to posting files
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void tempFilesToPostingFiles () throws IOException, ClassNotFoundException {
        createPostingFiles();
        File folder = new File(path);
        File[] filesList = folder.listFiles(); //list of all files in the index directory, temp files and final files.
        Hashtable <String, LinkedList < Pair < String, Integer >>> indexTable = new Hashtable<>(); // the hash table for each posting file

        int firstTempFile = 0;
        while (!isTempFile(filesList[firstTempFile])){ //find the first temp file
            firstTempFile++;
        }

        String currPostingFileName = getPostingFilePath(filesList[firstTempFile].getName());

        for (int i=firstTempFile ; i < filesList.length ; i++) { //iterate all the files in the index directory
            if (filesList[i].getName().toLowerCase().contains("zevel")) {
                filesList[i].delete();
                continue;
            }
            if( filesList[i].getName().contains("Dictionary") || filesList[i].getName().contains("Documents") || !isTempFile(filesList[i])){
                continue;
            }

            if (!getPostingFilePath(filesList[i].getName()).equals(currPostingFileName)){ // we need end this file and move to the next one
                writeHashTableToText (indexTable, currPostingFileName);
                currPostingFileName = getPostingFilePath(filesList[i].getName());
                indexTable = new Hashtable<>();
            }
            FileReader reader = new FileReader(filesList[i]);
            BufferedReader bf = new BufferedReader(reader);
            String line = bf.readLine();
            while (line != null){ //while the temp file is not over
                String [] splitedLine = line.split(";");
                String lowerCase = splitedLine[0].toLowerCase();
                String upperCase = splitedLine[0].toUpperCase();
                LinkedList < Pair < String, Integer >> docsList = getDocsListFromString(splitedLine);
                int appears = 0;
                for(int j=0; j<docsList.size(); j++){
                    appears = appears + docsList.get(j).getValue();
                }
                if (isDigit(splitedLine[0].charAt(0))){ // if a term starts with number- we insert it to the dictionary as it is.
                    if(indexTable.containsKey(splitedLine[0])){
                        indexTable.get(splitedLine[0]).addAll(docsList);
                        dictionary.get(splitedLine[0]).addAppears(appears);
                        dictionary.get(splitedLine[0]).addDf(docsList.size());
                    }
                    else{
                        indexTable.put(splitedLine[0], docsList);
                        Term term = new Term (splitedLine[0]);
                        term.setDf(docsList.size());
                        term.addAppears(appears);
                        dictionary.put(splitedLine[0], term);
                    }
                }
                else if (indexTable.containsKey(lowerCase)){
                    indexTable.get(lowerCase).addAll(docsList);
                    dictionary.get(lowerCase).addAppears(appears);
                    dictionary.get(lowerCase).addDf(docsList.size());
                }
                else if (indexTable.containsKey(upperCase)){
                    if (isUpperCase(splitedLine[0].charAt(0))){
                        indexTable.get(upperCase).addAll(docsList);
                        dictionary.get(upperCase).addAppears(appears);
                        dictionary.get(upperCase).addDf(docsList.size());
                    }
                    else {
                        LinkedList < Pair < String, Integer >> upperCaseList = indexTable.get(upperCase);
                        indexTable.remove(upperCase);
                        upperCaseList.addAll(docsList);
                        indexTable.put(splitedLine[0], upperCaseList);
                        dictionary.remove(upperCase);
                        Term term = new Term (lowerCase);
                        term.setDf(upperCaseList.size());
                        term.addAppears(appears);
                        dictionary.put(lowerCase, term);
                    }
                }
                else {
                    indexTable.put(splitedLine[0], docsList);
                    Term term = new Term (splitedLine[0]);
                    term.setDf(docsList.size());
                    term.addAppears(appears);
                    dictionary.put(splitedLine[0], term);
                }
                line = bf.readLine();
            }
            bf.close();
            reader.close();
            filesList[i].delete();
        }

        writeHashTableToText (indexTable, currPostingFileName); //write the last table to the last posting file

    }

    /**
     * check if file is a temp posting file
     * @param file
     * @return true if the file is a temporary file
     */
    private boolean isTempFile (File file){
        String fileName = file.getName();
        if (toStem){ // all files start with 'S'
           if (fileName.substring(1).toLowerCase().equals(fileName.substring(1))){
               return true;
           }
        }
        else { // no stemming
            if(fileName.toLowerCase().equals(fileName)){
                return true;
            }
        }
        return false;
    }

    // gets hashtable of terms and list of documents it appears

    /**
     * write data to text in file
     * @param table - data to write
     * @param path - path to write the data
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void writeHashTableToText (Hashtable <String, LinkedList < Pair < String, Integer >>> table, String path) throws IOException, ClassNotFoundException {
                File file = new File(path);
                FileWriter fileWriter = new FileWriter(file);
                ArrayList <String> sortedKeys = getSortedKeys(table.keySet());
                int lineInPosting = 0;
                for(String term: sortedKeys) {
                    if (table.get(term).size() <= 1 && isUpperCase(term.charAt(0))) {//remove terms (names) that appear once in the corpus- entities
                        dictionary.remove(term);
                    }
                    else{
                        fileWriter.write(term + ";");
                        for (Pair<String, Integer> pair : table.get(term)) {
                            fileWriter.write("" + pair.getKey() + "," + pair.getValue() + ";");
                        }
                        fileWriter.write("\n");
                        if (!isTempFile(file)){
                            dictionary.get(term).setLocationInPosting(lineInPosting);
                            lineInPosting++;
                        }
                        if (isUpperCase(term.charAt(0))){
                            entities.put(term, table.get(term));
                        }
                    }
                }
                fileWriter.close();
    }

    /**
     * creates the posting files of documents and dictionary
     * @param documents - list of documents
     * @throws IOException
     */
    public void createDocumentsAndDictionaryFiles (HashSet<DocumentData> documents) throws IOException {

        String fileSeparator = System.getProperty("file.separator");
        String docfilePath =  path + fileSeparator + "Documents.txt";
        if (toStem){
            docfilePath =  path + fileSeparator + "SDocuments.txt";
        }
        File docFile = new File(docfilePath);
        boolean newFile = docFile.createNewFile();
        String dicfilePath = path + fileSeparator + "Dictionary.txt";
        if (toStem){
            dicfilePath =  path + fileSeparator + "SDictionary.txt";
        }
        File dicFile = new File(dicfilePath);
        newFile = dicFile.createNewFile();
        FileWriter fileWriterDic = new FileWriter(dicFile);
        ArrayList <String> sortedDictionary = getSortedKeys(dictionary.keySet());
        for(String term : sortedDictionary){
            fileWriterDic.write(term+ "="+dictionary.get(term).getDf()+ "=" + dictionary.get(term).getAppears()
                    + "=" + dictionary.get(term).getLocationInPosting());
            fileWriterDic.write("\n");
        }
        fileWriterDic.close();

        FileWriter fileWriterDoc = new FileWriter(docFile);
        for(DocumentData doc : documents){
            fileWriterDoc.write(doc.getDocID() + ";" + doc.getLength() + ";" + doc.getCommonWordName()+
                  ";"  + doc.getMostCommonWord() );
            fileWriterDoc.write("\n");
        }
        fileWriterDoc.close();
    }

    /**
     * creates the posting file of entities
     * @throws IOException
     */
    public void createEntitiesPostingFile () throws IOException {
        String fileSeparator = System.getProperty("file.separator");
        String filePath =  path + fileSeparator + "Entities.txt";
        if (toStem){
            filePath =  path + fileSeparator + "SEntities.txt";
        }
        File file = new File(filePath);
        boolean newFile = file.createNewFile();
        FileWriter fileWriter = new FileWriter(file);
        for (String term : entities.keySet()){
            fileWriter.write(term + ";");
            for (Pair<String, Integer> pair : entities.get(term)) {
                fileWriter.write("" + pair.getKey() + "," + pair.getValue() + ";");
            }
            fileWriter.write("\n");
        }
        fileWriter.close();
    }


    private boolean belongToSameTempFile(String term1, String term2){
        if (getTempFilePath(term1).equals(getTempFilePath(term2))){
            return true;
        }
        return false;
    }

    /**
     * create a file in disk
     * @param fileName
     * @throws IOException
     */
    private void createFile(String fileName) throws IOException {
        String fileSeparator = System.getProperty("file.separator");
        boolean newFile;

        String filePath = path + fileSeparator + fileName;
        File postingFile = new File(filePath);
        newFile = postingFile.createNewFile();
    }

    public Hashtable<String, Term> getDictionary (){
        return dictionary;
    }

    public Hashtable<String, DocumentData> getDocuments () { return documents; }

    /**
     * load dictionary from posting file
     * @throws IOException
     */
    public void loadDictionaryFromFile (boolean stem) throws IOException {
        dictionary = new Hashtable<>();
        String fileSeparator = System.getProperty("file.separator");
        String filePath =  path + fileSeparator + "Dictionary.txt";
        if (stem){
            filePath =  path + fileSeparator + "SDictionary.txt";
        }
        FileReader fr = new FileReader(new File(filePath));
        BufferedReader br = new BufferedReader(fr);
        String line = br.readLine();

        while (line != null){
            String [] splittedLine = line.split("=");
            Term term = new Term (splittedLine[0]);
            term.setDf(Integer.parseInt(splittedLine[1]));
            term.setAppears(Integer.parseInt(splittedLine[2]));
            term.setLocationInPosting(Integer.parseInt(splittedLine[3]));
            dictionary.put(splittedLine[0], term);
            line = br.readLine();
        }
        fr.close();

    }

    /**
     * load documents from posting file
     */
    public void loadDocumentsFromFile (boolean stem) {
        documents =new Hashtable<>();
        try{
            String fileSeparator = System.getProperty("file.separator");
            String filePath =  path + fileSeparator + "Documents.txt";
            if (stem){
                filePath =  path + fileSeparator + "SDocuments.txt";
            }
            FileReader fr = new FileReader(new File(filePath));
            BufferedReader br = new BufferedReader(fr);
            String line = br.readLine();
            while (line != null){
                String [] splitLine = line.split(";");
                DocumentData docData = new DocumentData (splitLine[0]);
                docData.setLength(Integer.parseInt(splitLine[1]));
                docData.setCommonWordName(splitLine[2]);
                docData.setMostCommonWord(Integer.parseInt(splitLine[3]));
                documents.put(docData.getDocID(), docData);
                line = br.readLine();
            }
        }
        catch(Exception e){
            System.out.println("exception in loadDocuments- search manager");
            e.printStackTrace();
        }

    }

    /**
     * create the posting files
     * @throws IOException
     */
    private void createPostingFiles() throws IOException {
        if (toStem){
            String fileName = "SNUMBERS.txt";
            createFile(fileName);
            fileName = "SAB.txt";
            createFile(fileName);
            fileName = "SCD.txt";
            createFile(fileName);
            fileName = "SEFGH.txt";
            createFile(fileName);
            fileName = "SIJKL.txt";
            createFile(fileName);
            fileName = "SMNO.txt";
            createFile(fileName);
            fileName = "SPQ.txt";
            createFile(fileName);
            fileName = "SRS.txt";
            createFile(fileName);
            fileName = "STUVWXYZ.txt";
            createFile(fileName);
            fileName = "SZEVEL.txt";
            createFile(fileName);
        }
        else {
            String fileName = "NUMBERS.txt";
            createFile(fileName);
            fileName = "AB.txt";
            createFile(fileName);
            fileName = "CD.txt";
            createFile(fileName);
            fileName = "EFGH.txt";
            createFile(fileName);
            fileName = "IJKL.txt";
            createFile(fileName);
            fileName = "MNO.txt";
            createFile(fileName);
            fileName = "PQ.txt";
            createFile(fileName);
            fileName = "RS.txt";
            createFile(fileName);
            fileName = "TUVWXYZ.txt";
            createFile(fileName);
            fileName = "ZEVEL.txt";
            createFile(fileName);
        }


    }

    private void createTempFiles () throws IOException {
        if (toStem){
            String fileName = "Snumbers" + batchNum+ ".txt";
            createFile(fileName);
            fileName = "Sab" + batchNum+ ".txt";
            createFile(fileName);
            fileName = "Scd" + batchNum+ ".txt";
            createFile(fileName);
            fileName = "Sfgh" + batchNum+ ".txt";
            createFile(fileName);
            fileName = "Sijkl" + batchNum+ ".txt";
            createFile(fileName);
            fileName = "Smno" + batchNum+ ".txt";
            createFile(fileName);
            fileName = "Spq" + batchNum+ ".txt";
            createFile(fileName);
            fileName = "Srs" + batchNum+ ".txt";
            createFile(fileName);
            fileName = "Stuvwxyz" + batchNum+ ".txt";
            createFile(fileName);
            fileName = "Szevel" + batchNum+ ".txt";
            createFile(fileName);
        }
        else{
            String fileName = "numbers" + batchNum+ ".txt";
            createFile(fileName);
            fileName = "ab" + batchNum+ ".txt";
            createFile(fileName);
            fileName = "cd" + batchNum+ ".txt";
            createFile(fileName);
            fileName = "efgh" + batchNum+ ".txt";
            createFile(fileName);
            fileName = "ijkl" + batchNum+ ".txt";
            createFile(fileName);
            fileName = "mno" + batchNum+ ".txt";
            createFile(fileName);
            fileName = "pq" + batchNum+ ".txt";
            createFile(fileName);
            fileName = "rs" + batchNum+ ".txt";
            createFile(fileName);
            fileName = "tuvwxyz" + batchNum+ ".txt";
            createFile(fileName);
            fileName = "zevel" + batchNum+ ".txt";
            createFile(fileName);
        }

    }

    private String getTempFilePath (String term){
        String fileSeparator = System.getProperty("file.separator");
        String filePath = path + fileSeparator;

        if (toStem ){
            if (Character.isDigit(term.charAt(0))) {
                filePath = filePath + "Snumbers" + batchNum+ ".txt";
            }
            else if (Character.toLowerCase(term.charAt(0)) == 'a' || Character.toLowerCase(term.charAt(0)) == 'b'){
                filePath = filePath + "Sab" + batchNum+ ".txt";
            }
            else if (Character.toLowerCase(term.charAt(0)) == 'c' || Character.toLowerCase(term.charAt(0)) == 'd'){
                filePath = filePath + "Scd" + batchNum+ ".txt";
            }
            else if (Character.toLowerCase(term.charAt(0)) >= 'e' && Character.toLowerCase(term.charAt(0)) <= 'h'){
                filePath = filePath + "Sefgh" + batchNum+ ".txt";
            }
            else if (Character.toLowerCase(term.charAt(0)) >= 'i' && Character.toLowerCase(term.charAt(0)) <= 'l'){
                filePath = filePath + "Sijkl" + batchNum+ ".txt";
            }
            else if (Character.toLowerCase(term.charAt(0)) >= 'm' && Character.toLowerCase(term.charAt(0)) <= 'o'){
                filePath = filePath + "Smno" + batchNum+ ".txt";
            }
            else if (Character.toLowerCase(term.charAt(0)) == 'p' || Character.toLowerCase(term.charAt(0)) == 'q'){
                filePath = filePath + "Spq" + batchNum + ".txt";
            }
            else if (Character.toLowerCase(term.charAt(0)) == 'r'  || Character.toLowerCase(term.charAt(0)) == 's'){
                filePath = filePath + "Srs"  + batchNum+ ".txt";
            }
            else if (Character.toLowerCase(term.charAt(0)) >= 't' && Character.toLowerCase(term.charAt(0)) <= 'z'){
                filePath = filePath + "Stuvwxyz"  + batchNum+ ".txt";
            }
            else{
                filePath = filePath + "Szevel"  + batchNum+ ".txt";
            }
        }

        else {

            if (Character.isDigit(term.charAt(0))) {
                filePath = filePath + "numbers" + batchNum+ ".txt";
            }
            else if (Character.toLowerCase(term.charAt(0)) == 'a' || Character.toLowerCase(term.charAt(0)) == 'b'){
                filePath = filePath + "ab" + batchNum+ ".txt";
            }
            else if (Character.toLowerCase(term.charAt(0)) == 'c' || Character.toLowerCase(term.charAt(0)) == 'd'){
                filePath = filePath + "cd" + batchNum+ ".txt";
            }
            else if (Character.toLowerCase(term.charAt(0)) >= 'e' && Character.toLowerCase(term.charAt(0)) <= 'h'){
                filePath = filePath + "efgh" + batchNum+ ".txt";
            }
            else if (Character.toLowerCase(term.charAt(0)) >= 'i' && Character.toLowerCase(term.charAt(0)) <= 'l'){
                filePath = filePath + "ijkl" + batchNum+ ".txt";
            }
            else if (Character.toLowerCase(term.charAt(0)) >= 'm' && Character.toLowerCase(term.charAt(0)) <= 'o'){
                filePath = filePath + "mno" + batchNum+ ".txt";
            }
            else if (Character.toLowerCase(term.charAt(0)) == 'p' || Character.toLowerCase(term.charAt(0)) == 'q'){
                filePath = filePath + "pq" + batchNum + ".txt";
            }
            else if (Character.toLowerCase(term.charAt(0)) == 'r' || Character.toLowerCase(term.charAt(0)) == 's'){
                filePath = filePath + "rs"  + batchNum+ ".txt";
            }
            else if (Character.toLowerCase(term.charAt(0)) >= 't' && Character.toLowerCase(term.charAt(0)) <= 'z'){
                filePath = filePath + "tuvwxyz"  + batchNum+ ".txt";
            }
            else {
                filePath = filePath + "zevel" + batchNum + ".txt";
            }
        }

        return filePath;
    }

    private String getPostingFilePath (String tempFileName){
        String fileSeparator = System.getProperty("file.separator");
        String filePath = path + fileSeparator;

        if (toStem){
            if (tempFileName.contains("numbers")) {
                filePath = filePath  + "SNUMBERS.txt";
            }
            else if(tempFileName.contains("ab")){
                filePath = filePath  + "SAB.txt";
            }
            else if(tempFileName.contains("cd")){
                filePath = filePath  + "SCD.txt";
            }
            else if(tempFileName.contains("efgh")){
                filePath = filePath  + "SEFGH.txt";
            }
            else if(tempFileName.contains("ijkl")){
                filePath = filePath  + "SIJKL.txt";
            }
            else if(tempFileName.contains("mno")){
                filePath = filePath  + "SMNO.txt";
            }
            else if(tempFileName.contains("pq")){
                filePath = filePath  + "SPQ.txt";
            }
            else if(tempFileName.contains("rs")){
                filePath = filePath  + "SRS.txt";
            }
            else if(tempFileName.contains("tuvwxyz")){
                filePath = filePath  + "STUVWXYZ.txt";
            }
            else{
                filePath = filePath  + "SZEVEL.txt";
            }
        }

        else{
            if (tempFileName.contains("numbers")) {
                filePath = filePath  + "NUMBERS.txt";
            }
            else if(tempFileName.contains("ab")){
                filePath = filePath  + "AB.txt";
            }
            else if(tempFileName.contains("cd")){
                filePath = filePath  + "CD.txt";
            }
            else if(tempFileName.contains("efgh")){
                filePath = filePath  + "EFGH.txt";
            }
            else if(tempFileName.contains("ijkl")){
                filePath = filePath  + "IJKL.txt";
            }
            else if(tempFileName.contains("mno")){
                filePath = filePath  + "MNO.txt";
            }
            else if(tempFileName.contains("pq")){
                filePath = filePath  + "PQ.txt";
            }
            else if(tempFileName.contains("rs")){
                filePath = filePath  + "RS.txt";
            }
            else if(tempFileName.contains("tuvwxyz")){
                filePath = filePath  + "TUVWXYZ.txt";
            }
            else{
                filePath = filePath  + "ZEVEL.txt";
            }
        }

        return filePath;
    }

    public ArrayList<String> getSortedKeys (Set<String> keys){
        ArrayList <String> sortedKeys = new ArrayList<>() ;
        sortedKeys.addAll(keys);
        sortedKeys.sort(new TermsComparator());
        return sortedKeys;
    }

    public void createStopWordsFile(HashSet<String> stopWords) throws IOException {
        String fileSeparator = System.getProperty("file.separator");
        String stopWordsFilePath =  path + fileSeparator + "StopWords.txt";
        File stopWordsFile = new File(stopWordsFilePath);
        boolean file = stopWordsFile.createNewFile();
        FileWriter fileWriter = new FileWriter(stopWordsFile);
        for(String word : stopWords){
            fileWriter.write(word);
            fileWriter.write("\n");
        }
        fileWriter.close();
    }

    class TermsComparator implements Comparator<String> {
        public int compare(String term1, String term2) {
            term1 = term1.toLowerCase();
            term2 = term2.toLowerCase();
            if(term1.compareTo(term2) > 0){
                return 1;
            }
            else if(term1.compareTo(term2) < 0) {
                return -1;
            }
            return 0;
        }
    }


}

