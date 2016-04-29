Run DataPreprocessing.java

This reads the main dataset which is a set of 1164 files and also two supplementary files.
Link to main dataset: http://www.cricsheet.org

The two supplementary files are:-
1. 'ICC_Rankings.csv' which contains the mapping of Team Rankings wrt year 
2. 'ODI_grounds.csv' which contains the mapping of the grounds with the city and country.

DataPreprocessing.java uses the information in the above two files and combines them with the main dataset to produce 'cricket.csv'.
The file 'cricket.csv' is further partitioned into 'cricket_train_set.csv' and 'cricket_test_set.csv' which are the training and testing sets respectively. This was done manually. The training set is 70% of 'cricket.csv' and remaining is testing set.