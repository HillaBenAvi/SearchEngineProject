package Model;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;

public class ReadFile extends AReader {

    private String path;
    private String corpusPath;
    private String [] corpusFilesPaths;
    private int lastFileRead;

    public ReadFile(String pathToDirectory) {
        path = pathToDirectory;
        lastFileRead = 0;
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        for (File file : listOfFiles) {
            if (file.isFile()){
                super.stopWordsFilePath = file.getAbsolutePath();
            }
            if (file.isDirectory()){
                this.corpusPath = file.getAbsolutePath();
            }
        }
        File[] listOfCorpusDirectories = new File(corpusPath).listFiles();
        corpusFilesPaths = new String [listOfCorpusDirectories.length];
        for (int i=0; i<listOfCorpusDirectories.length; i++){
            corpusFilesPaths[i] = listOfCorpusDirectories[i].getAbsolutePath();
        }
        for (int i=0; i<listOfCorpusDirectories.length; i++){
            File [] file = new File (corpusFilesPaths[i]).listFiles();
            corpusFilesPaths[i] = file[0].getAbsolutePath();
        }

    }

    /**
     *
     * @return number of files in the corpus
     */
    public int filesNum (){
        return corpusFilesPaths.length;
    }

    public boolean hasNextFile (){
        if (lastFileRead >= corpusFilesPaths.length){
            return false;
        }
        return true;
    }


    public ArrayList <Document> readNext() throws IOException {
        String fileText = fileToString (corpusFilesPaths[lastFileRead]);
        ArrayList <Document> docsInFile = getFileDocs(fileText);
        lastFileRead ++;

        return docsInFile;
    }


    private ArrayList<Document> getFileDocs (String fileText) {
        ArrayList<Document> fileDocs = new ArrayList<>();
        String [] docsString = fileText.split("<DOC>"); //split the text to the documentsData
        for (int i = 1; i < docsString.length ; i++){
            Document doc = stringToDocument(docsString[i]);
            fileDocs.add(doc);
        }
        return fileDocs;
    }

    /**
     * convert string to document object with data
     * @param docString - text of document
     * @return document object
     */
    private Document stringToDocument (String docString){
        Document doc = new Document();
        String [] openDocNo = docString.split("<DOCNO>"); // remove <DOCNO> tag
        String [] closeDocNo = openDocNo[1].split("</DOCNO>");// remove </DOCNO> tag
        doc.setDocNo(closeDocNo[0]); //between <DOC> and </DOCNO>
        //date?
        String [] openTitle = docString.split("<TI>"); // remove <TI> tag
        if (openTitle.length>1){
            String [] closeTitle = openTitle[1].split("</TI>");// remove </TI> tag
            doc.setTitle(closeTitle[0]); // between <TI> ant </TI>
        }
        String [] openText = docString.split("<TEXT>"); // remove <TEXT> tag
        if (openText.length>1){
            String [] closeText = openText[1].split("</TEXT>");// remove </TEXT> tag
            doc.setText(closeText[0]); // between <TEXT> ant </TEXT>

        }

        return doc;
    }


}
