package View;
import javafx.fxml.FXML;
import Model.Term;
import ViewModel.MyViewModel;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

public class SearchEngineController {

    private MyViewModel viewModel;

    //Part A
    private String corpusPath;
    private String indexesPath;
    private boolean stem;
    private boolean delete = false;

    //Part B
    private String resultsPath;
    private String queriesFilePath;
    private boolean semanticModel;
    private boolean loaded = false;


    @FXML
    //Part A
    public javafx.scene.control.Button startBtn;
    public javafx.scene.control.Button resetBtn;
    public javafx.scene.control.Button showDicBtn;
    public javafx.scene.control.Button loadDicBtn;
    public javafx.scene.control.Button corpusBrowse;
    public javafx.scene.control.Button indexFilesBrowse;
    public javafx.scene.control.CheckBox stemmerCB;
    public javafx.scene.control.TextField indexesPathTF;
    public javafx.scene.control.TextField corpusPathTF;
    public javafx.scene.control.TableView tableView;

    //Part B
    public javafx.scene.control.Button searchQueryBtn;
    public javafx.scene.control.Button queriesFileBrowseBtn;
    public javafx.scene.control.Button queriesFileSearchBtn;
    public javafx.scene.control.CheckBox semanticModelCB;
    public javafx.scene.control.TextField queryTF;
    public javafx.scene.control.TextField queriesFileTF;
    public javafx.scene.control.TextField resultsPathTF;


    //results
    public javafx.scene.control.ListView docsListView;
    public javafx.scene.control.TableView queriesTableView;
    public javafx.scene.control.ListView entitiesListView;
    public javafx.scene.control.Button showEntitiesButton;
    public javafx.scene.control.Button saveResultsButton;
    public javafx.scene.control.Button resultsPathBrowse;
    public javafx.scene.control.ComboBox queryComboBox;
    public javafx.scene.control.ComboBox docsComboBox;


    public void initCorpusPath (){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        Stage loadStage = new Stage();
        File f =directoryChooser.showDialog(loadStage);
        if (f != null){
            corpusPath = f.getAbsolutePath();
            corpusPathTF.setText(corpusPath);
        }
    }

    public void initIndexFilesPath (){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        Stage loadStage = new Stage();
        File f =directoryChooser.showDialog(loadStage);
        if (f != null){
            indexesPath = f.getAbsolutePath();
            indexesPathTF.setText(indexesPath);
        }
    }

    public void setStemVal(){
        if(stem ==true){
            stem = false;
        }
        else{
            stem = true;
        }
    }

    public void startIndexing () throws IOException, ClassNotFoundException {

        long startTimeIndex = System.nanoTime();
        corpusPath = corpusPathTF.getText();
        indexesPath = indexesPathTF.getText();
        if (corpusPath.isEmpty() || indexesPath.isEmpty() || corpusPath == null || indexesPath == null){
            showAlert("Please verify the corpus and index paths");
        }
        else{
            this.viewModel = new MyViewModel(corpusPath, indexesPath,stem);
            viewModel.startIndexing(stem);
            delete = false;
            loaded = true;
            long finishTimeIndex = System.nanoTime();
            showAlert("Indexing finished successfully!\n" +
                    "Total time for parsing and indexing: " + ((finishTimeIndex - startTimeIndex) / 1000000000.0) + " \n"
                     + "Number of indexed documents:  " + viewModel.getDocsNum() +"\n"
                     + "Number of words in the dictionary: " + viewModel.getDictionary().size() );
        }

    }

    private void showAlert(String alertMessage) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(alertMessage);
        alert.show();
    }

    public void deleteIndexFiles (){
        if (delete == true){
            showAlert("The posting directory is already empty.");
            return;
        }
        File directory = new File (indexesPath);
        File[] listOfFiles = directory.listFiles();
        for (File file : listOfFiles){
            file.delete();
        }
        loaded = false;
        delete = true;
    }

    public void loadDictionary () throws IOException {
        if (viewModel == null) {
            corpusPath = corpusPathTF.getText();
            indexesPath = indexesPathTF.getText();
            if ( indexesPath.isEmpty()  || indexesPath == null) {
                showAlert("Please verify the corpus and index paths");
            } else {
                if (delete || isEmptyDirectory(indexesPath)){
                    showAlert("The posting folder is empty, please index the corpus before loading the dictionary.");
                    return;
                }
                this.viewModel = new MyViewModel(corpusPath, indexesPath, stem);
                viewModel.loadDictionary(stem);
                loaded = true;
                showAlert("Dictionary loaded.");
            }
        }
        else
        {
            if (delete  || isEmptyDirectory(indexesPath)){
                showAlert("The posting folder is empty, please index the corpus before loading the dictionary.");
                return;
            }
            viewModel.loadDictionary(stem);
            loaded = true;
            showAlert("Dictionary loaded.");
        }
    }

    public void showDictionary (){
        Hashtable<String, Term> dictionary = viewModel.getDictionary();
        List<String> keys = Collections.list(dictionary.keys());
        Collections.sort(keys);

        tableView = new TableView();
        TableColumn <String, Record> column1 = new TableColumn<>("TERM");
        TableColumn <String, Record> column2 = new TableColumn<>("COUNT");
        column1.setCellValueFactory(new PropertyValueFactory<>("term"));
        column2.setCellValueFactory(new PropertyValueFactory<>("count"));
        tableView .getColumns().add(column1);
        tableView .getColumns().add(column2);

        tableView.setEditable(true);
        tableView.getSelectionModel().setCellSelectionEnabled(true);

        for (String term : keys){
            tableView .getItems().add(new Record (term, dictionary.get(term).getAppears()));
        }

        StackPane sp = new StackPane(tableView);
        Scene scene = new Scene(sp , 400, 600);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle ("Dictionary");
        stage.show();

    }

    public static class Record {
        private SimpleIntegerProperty count;
        private SimpleStringProperty term;

        public void setCount(int count) {
            this.count.set(count);
        }

        public void setTerm(String term) {
            this.term.set(term);
        }

        public int getCount() {
            return count.get();
        }

        public SimpleIntegerProperty countProperty() {
            return count;
        }

        public String getTerm() {
            return term.get();
        }

        public SimpleStringProperty termProperty() {
            return term;
        }

        public Record (String term, Integer df){
            this.count = new SimpleIntegerProperty(df);
            this.term = new SimpleStringProperty(term);
        }
    }

    public void initResultsPath (){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        Stage loadStage = new Stage();
        File f =directoryChooser.showDialog(loadStage);
        if (f != null){
            resultsPath = f.getAbsolutePath();
            resultsPathTF.setText(resultsPath);
        }
    }

    public void initQueriesFilePath (){
        FileChooser fileChooser = new FileChooser();
        Stage loadStage = new Stage();
        fileChooser.getExtensionFilters();
        File f = fileChooser.showOpenDialog(loadStage);
        if (f != null){
            queriesFilePath = f.getAbsolutePath();
            queriesFileTF.setText(queriesFilePath);
        }
    }

    public void setSemanticModelVal(){
        if(semanticModel ==true){
            semanticModel = false;
        }
        else{
            semanticModel = true;
        }
    }

    public void searchQuery (){
        if (!loaded){
            showAlert("Please load the dictionary before searching.");
            return;
        }
        if(queryTF.getText()==null || queryTF.getText()==""){
            showAlert("Please enter query.");
            return;
        }
        viewModel.search(queryTF.getText(), false, semanticModel, indexesPath , resultsPathTF.getText(), stem);
        fillQueryComboBox(new ArrayList<String>(viewModel.getResults().keySet()));
        openResultsStage(viewModel.getResults());
    }

    public void searchQueriesFromFile(){
        if (!loaded){
            showAlert("Please load the dictionary before searching.");
            return;
        }
        if(queryTF.getText()==null || queryTF.getText() == ""){
            showAlert("Please enter query.");
            return;
        }
        if(queriesFileTF.getText()== null || queriesFileTF.getText() == ""){
            showAlert("Please enter queries file path.");
            return;
        }
        viewModel.search(queriesFilePath, true, semanticModel, indexesPath, resultsPathTF.getText(), stem);
        fillQueryComboBox(new ArrayList<String>(viewModel.getResults().keySet()));
        openResultsStage(viewModel.getResults());
    }

    private boolean isEmptyDirectory (String path) {

        File file = new File(path);

        if (file.isDirectory()) {
            if (file.list().length > 0) {
                return false;
            }
            else {
               return true;
            }
        }
        return true;
    }


    //results

    public void openResultsStage(Hashtable<String, List<Pair<String, Double>>> results) {
        queriesTableView = new TableView();
        TableColumn <String, queryRecord> column1 = new TableColumn<>("Query");
        TableColumn <String, queryRecord> column2 = new TableColumn<>("Number of relevant docs");
        TableColumn <String, queryRecord> column3 = new TableColumn<>("Doc Number");
        column1.setCellValueFactory(new PropertyValueFactory<>("query"));
        column2.setCellValueFactory(new PropertyValueFactory<>("numOfDocs"));
        column3.setCellValueFactory(new PropertyValueFactory<>("docNum"));
        queriesTableView .getColumns().add(column1);
        queriesTableView .getColumns().add(column2);
        queriesTableView .getColumns().add(column3);

        queriesTableView .setEditable(true);
        queriesTableView .getSelectionModel().setCellSelectionEnabled(true);

        for (String query : results.keySet()){
            List <Pair<String, Double>> listOfDocs = new ArrayList<>();
            listOfDocs.addAll(results.get(query));
            for(Pair p : listOfDocs){
                String docNum = (String) p.getKey();
                queriesTableView .getItems().add(new queryRecord (query, results.get(query).size(), docNum));
            }
        }

        try {
            Stage resultsStage = new Stage();
            resultsStage.setTitle("Results");
            FXMLLoader fxmlLoader = new FXMLLoader();
            Scene scene = new Scene(queriesTableView, 363, 497);
            resultsStage.setScene(scene);
            resultsStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static class queryRecord {
        private SimpleIntegerProperty numOfDocs;
        private SimpleStringProperty query;
        private SimpleStringProperty docNum;

        public String getDocNum() {
            return docNum.get();
        }

        public void setDocNum(String docNum) {
            this.docNum.set(docNum);
        }

        public SimpleStringProperty docNumProperty() {
            return docNum;
        }

        public void setNumOfDocs(int numOfDocs) {
            this.numOfDocs.set(numOfDocs);
        }

        public void setQuery(String query) {
            this.query.set(query);
        }

        public int getNumOfDocs() {
            return numOfDocs.get();
        }

        public SimpleIntegerProperty numOfDocsProperty() {
            return numOfDocs;
        }

        public String getQuery() {
            return query.get();
        }

        public SimpleStringProperty queryProperty() {
            return query;
        }

        public queryRecord(String query, Integer numOfDocs, String docNum){
            this.numOfDocs = new SimpleIntegerProperty(numOfDocs);
            this.query = new SimpleStringProperty(query);
            this.docNum = new SimpleStringProperty(docNum);
        }
    }


    public void saveResults (){
        viewModel.saveResults(resultsPath);
    }

    public void fillQueryComboBox ( ArrayList<String> queries ){
        queryComboBox.getItems().clear();
        docsComboBox.getItems().clear();
        for (String query : queries){
            queryComboBox.getItems().add(query);
        }

    }

    public void fillDocsComboBox () {
        Hashtable<String, List<Pair<String, Double>>> results = viewModel.getResults();
        if(results.size()==0){
            return;
        }
        try {
            String query = queryComboBox.getSelectionModel().getSelectedItem().toString();
            docsComboBox.getItems().clear();
            if (query != null && query != "") {
                for (Pair<String, Double> p : results.get(query)) {
                    docsComboBox.getItems().add(p.getKey());
                }
            }
        }
        catch(Exception e){ }
    }

    public void showEntities () {
        try {
            String doc = docsComboBox.getSelectionModel().getSelectedItem().toString();

            if(doc!= null && doc!=""){
                List<Pair<String, Double>> topEntites = viewModel.getTopEntities(doc, stem);
                int numOfEntities = Math.min(5, topEntites.size());
                String alert = "The top entities are: \n";
                for (int i=0; i<numOfEntities; i++ ){
                    alert = alert + topEntites.get(i).getKey() + " rank: " + topEntites.get(i).getValue() + " \n";
                }
                showAlert(alert );
            }
        }
        catch (NullPointerException e){
            showAlert("please choose relevant document.");
        }



    }



}
