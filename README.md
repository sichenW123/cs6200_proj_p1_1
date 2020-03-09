# cs6200_assignment_2  


>website url: https://cs6200-p1-sichen.herokuapp.com/  
>github: https://github.com/sichenW123/cs6200_proj_p1_1

  
  This assignment consists of 3 parts: searching engine(cs6200_proj_p1_1-master) and evaluation(Evaluation)(generating data and graphs for evluation report) and evaluation report.
  
First, Unzip the file.

To run the project
--------------------
unzip the file cs6200_proj_p1_1-master   
navigate to the file and open in command line, input the following command  
`mvn spring-boot:run`  
open a browser and to http://localhost:8080/    
  
To generate graphs  
--------------------
* Open the file Evaluation on IntelliJ IDEA or Eclipse.  
* Hit run
  
Indexing design
--------------------
* Get all data from .xml files.
* Remove unnecessary punctuation signs and spaces.
* Get word sets of each record.
* Make a dictionary whose key is term and value is a list of record number that the term is in.
* Write the dictionary to output file.  
* UPDATE: using a B-tree to construct the dictionary instead of a hashmap.

  
    
Ranking   
--------------------
* Details of designing of ranking funcions can be found in evaluation report
* After following steps above and uploading source file, click To Search link on the main page.  
after entering the query and hit submit, result will be shown. 









