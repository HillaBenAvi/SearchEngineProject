"# SearchEngine" 
Readme - Search Engine :

INTRODUCTION
------------
This is our Search Engine Project.
This project divided into two parts:
Part A - read, parse and index
It includes the following process:
- Reading files from a given corpus, segmenting them into documents
Parsing the corpus in batches of 50000 documents, one by one.
The parsing could be executed with or without stemming
- Indexing the terms of each batch: creating posting files and writing information about the
terms into them. In addition, we create a file containing information about all the parsed documents and a united dictionary for the entire corpus.
Part B - search and rank
- Loading the posting files and dictionaries generated in part A
The dictionaries are restored and loaded into memory.
- After entering a query: 
1. The query is parsed.
2. Its term objects are reconstructed from the dictionary 
3. If semantics is enabled the query terms are sent to the semantics model.
4. The query terms and semantics terms (if enabled) are sent to the ranker who finds the 50 most relevant documents.

INFO
------
Project name: SearchEngine
java version: java 1.8

OPERATIONS
-----------
Part A-
1. Run the project jar file 
2. Select a corpus path in the first text area by pressing browse
3. Select a posting files path 
4. Click start to run processing the corpus 

Part B-
1. Press browse button to select an index path
2. If the stemming option was selected on the corpus in part A,than select the stemming option again.
3. Press 'load Dictionary' button to load the dictionary to the memory
4. Type query in the Query text area, you can enable semantic option and press search 
   or press browse button to select an text file path to enter some queries together, you can enable semantic optionan and press search    button. 
   -Now, wait until the searching process is finish, it will show you the results.
5. If you would like to choose document to see its entities. close the results window  
   and choose a query from choice menu, choose a document (according its id) from choice menu and press 'Get Entities' button.
6. If you would like to save the results, press browse button near the text field of 'Save the results at:' to select a path to save it,
   and press 'save' button.
   
POST PROCESSING
----------------
1. Reset button : clicking this button will delete all content in the selected posting files path
2. Load dictionary : will load the term dictionary to memory
3. Show dictionary : shows all the unique terms in the corpus with this total tf
   
