This is Readme File.
I have used 3 datasets to check results on apriori algorithm

1. Cars Dataset (> 1000 smamples)
2. Mushrooms Dataset ( > 8000 samples) 
3. Nursery Dataset ( > 10000 samples)

To run this code the most important thing is the 'metadata' file present in the project structure.

Data to provide in metadata file. 

metadata
1. 1st line - Path to data.csv file (Please override this line with the path to data.csv on your local machine)
2. 2nd Line - Number of attributes
3. 3rd Line - Name of all attributes
4. 4th Line  - Type of attribute (ex: categorical). Should be present for all attributes
5. 5th Line - Support threshold (0 < threshold < 1)
6. 6th Line - Confidence threshold (0 < threshold < 1)
7. Confidence/Lift (Please provide 'Confidence' or 'Lift' without quotes to print top 10 rules according to the evaluation strategy provided)

P.S. data.csv file should not contain any extra spaces or extra new line characters.