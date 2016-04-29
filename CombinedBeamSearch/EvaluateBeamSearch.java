import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;


public class EvaluateBeamSearch {
	static String curDir;
	static int tp;
	static int fp;
	static int tn;
	static int fn;
	static float precision;
	static float recall;
	static int trivial_index;
	
	public static void init(String criteria, int m, int k, int index){
		curDir = System.getProperty("user.dir");
		CombinedBeamSearch.init(criteria, m, k);
		trivial_index = index;
	}
	
	public static void resetCounters(){
		tp=0;
		fp=0;
		tn=0;
		fn=0;
	}
	
	public static float getSimpleAccuracy(){
		  float accuracy = (float)(tp + tn)/(tp+fp+tn+fn);
		  return accuracy;
	  }
	  
	  public static float getBalancedAccuracy(){
		  float sensitivity = (float) tp/(tp + fn);
		  float specificity = (float) tn/(tn + fp);
		  
		  return (sensitivity + specificity)/2;
	  }
	  
	  public static float getPrecision(){
		  precision = (float) tp/(tp + fp);
		  return precision;
	  }
	  
	  public static float getRecall(){
		  recall = (float) tp/(tp + fn);
		  return recall;
	  }
	  
	  public static float getF1Measure(){
		  float f1 = 2*precision*recall/(precision + recall);
		  return f1;
	  }
	
	/*
	 * Below method predicts the class
	 */
	public static String predictClass(BeamNode node, String[] temp){
	    String predict_class = node.sd.class_label; 
	   
	    if(!predict_class.equals("-1"))
	    	return predict_class;
	   
	    int feature_index = node.sd.feature_index;
	    float test_feature_val = Float.parseFloat(temp[feature_index]);
	    float train_feature_val = node.sd.split_value;
	    
	    if(test_feature_val <= train_feature_val)
	       return predictClass(node.left, temp);
	    else
	       return predictClass(node.right, temp);
		    
	}
	
	/*
	 * Below method is the trivial classifier. The trivial index is the index of Team 1 Home match.
	 * For the trivial classifier, if the Team 1 Home is 1, then the predicted class is also 1.
	 */
	public static void evalTrivialClassifier(String[] record){
		
		if(record[trivial_index].equals("1") && record[record.length-1].equals("1"))
			tp++;
		else if(record[trivial_index].equals("1") && !record[record.length-1].equals("1"))
			fp++;
		else if(record[trivial_index].equals("0") && record[record.length-1].equals("0"))
			tn++;
		else
			fn++;
		
	}
	
	/*
	 * Below method evaluates the test set. Each instance of the test set is passed to predictClass() to 
	 * predict the class label
	 */
	public static void evalTestFile(String file_name, boolean display_YN, boolean is_trivial_classifier) throws Exception{
	    BufferedReader br = new BufferedReader(new FileReader(file_name));
	    String s="";
	    String[] temp = null;
	    String predict_class = "";
	    int total = 0;
	   
	    while((s=br.readLine())!=null){
		   total++;
		   temp = s.split(",");
		   
		   if(!is_trivial_classifier){
			   predict_class = predictClass(CombinedBeamSearch.root, temp);
			   
			   String actual_class = temp[temp.length-1];
			   
			   if(predict_class.equals("1") && actual_class.equals("1"))
				   tp++;
			   else if(predict_class.equals("1") && !actual_class.equals("1"))
				   fp++;
			   else if(predict_class.equals("0") && actual_class.equals("0"))
				   tn++;
			   else
				   fn++;
		   }
		   else
			   evalTrivialClassifier(temp);
		 }
	   
	    if(display_YN){
	      System.out.println("Total: "+total);	
	      System.out.println("Correct: "+(tp+tn)+", Wrong: "+(fp+fn));
	      System.out.println("Accuracy: "+ (float)(tp+tn)/(tp+tn+fp+fn));
	      System.out.println();
	    }
	    
	    br.close();
		   
	}
	
    public static void main(String[] args) throws Exception{
      String criteria = "gini";  // Gini is used as the splitting criteria
  	  int k = 10;
  	  int m = 3;  // No of trees = 3
  	  boolean display_yn = true;
  	  float max = Float.NEGATIVE_INFINITY;
  	  int total_tp = 0;
	  int total_fp = 0;
	  int total_tn = 0;
	  int total_fn = 0;
	  int trivial_index = 2;
	  boolean is_trivial_classifier = false;
  	  BeamNode bestNode = null;
  	  init(criteria, m, k, trivial_index);
  	  
  	  String filename = "cricket_train_set.csv";  // change filenames to 10 different datasets
	  int pos = filename.lastIndexOf(".");
	  String ext = filename.substring(pos);
	  String path = curDir+"\\"+filename;
	 
	  CrossValidation.readDataset(path, false); 

	  System.out.println("Running Cross-Validation...");
	  int records = CrossValidation.hash.size();
	  for(int i=1;i<=k;i++){ // run 10-fold cross validation
		 CrossValidation.generatePartitions(i, records, ext, false);
		 DecisionTree.hashData.clear();   // data structures are flushed
		 DecisionTree.setLabels.clear();  
		 resetCounters();  
		
		 path = curDir+"\\"+"train"+i+ext;
		 DecisionTree.readDataset(path, false);
		
		 System.out.println("Building trees");
		
		 CombinedBeamSearch.listNodeDetails = new ArrayList<BeamNodeDetails>();
		 for(int j=0;j<m;j++){
			CombinedBeamSearch.listNodeDetails.add(new BeamNodeDetails());
		 }
		
		 CombinedBeamSearch.getBestTree();
		 CombinedBeamSearch.root = CombinedBeamSearch.listNodeDetails.get(0).beam_node;
		
		 path = curDir+"\\"+"test"+i+ext;
		 System.out.println("Evaluating file: "+path);
		
	     evalTestFile(path, display_yn, is_trivial_classifier);
	    
	     float accuracy = (float)(tp+tn)/(tp+tn+fp+fn);
	     if(accuracy > max){
	    	max = accuracy;
	    	bestNode = CombinedBeamSearch.root;   // best tree is stored based on accuracy
	     }
	     
	     total_tp+=tp;
	     total_tn+=tn;
	     total_fp+=fp;
	     total_fn+=fn;
	  }
	  
	  tp = total_tp;
	  fp = total_fp;
	  tn = total_tn;
	  fn = total_fn;
	  
	  System.out.println("\n----Cross Validation Statistics-----");
	  System.out.println("Precision: "+getPrecision());
	  System.out.println("Recall: "+getRecall());
	  System.out.println("Simple Accuracy: "+getSimpleAccuracy());
	  System.out.println("Balanced Accuracy: "+getBalancedAccuracy());
	  System.out.println("F1 Measure: "+getF1Measure());
	  System.out.println("\n----End of Cross Validation Statistics-----");
	  
  	  /*****************************************************************************/
	  
	  resetCounters();
	  DecisionTree.hashData.clear();
	  DecisionTree.setLabels.clear();
	  
	  filename = "cricket_train_set.csv";
	  CrossValidation.readDataset(path, false);
	  CombinedBeamSearch.root = bestNode;       // best tree is used on test set
	  
	  filename = "cricket_test_set.csv";
	  path = curDir+"\\"+filename;
	  display_yn = false;
	  evalTestFile(path, display_yn, is_trivial_classifier);
	  
	  System.out.println("\n------------- Test Set Statistics -----------------");
	  System.out.println("Precision: "+getPrecision());
	  System.out.println("Recall: "+getRecall());
	  System.out.println("Simple Accuracy: "+getSimpleAccuracy());
	  System.out.println("Balanced Accuracy: "+getBalancedAccuracy());
	  System.out.println("F1 Measure: "+getF1Measure());
	  System.out.println("\n------------- End of Test Set Statistics-----------");
	  
	  /**********************************************************************************/
	  resetCounters();
	  is_trivial_classifier = true;
	  evalTestFile(path, false, is_trivial_classifier);
	  
	  System.out.println("\nTrivial Classifier: If Team 1 plays at Home, then assign Positive class(Team 1 Wins)");
	  System.out.println("------------ Trivial Classifier Statistics ------------------");
	  System.out.println("Precision: "+getPrecision());
	  System.out.println("Recall: "+getRecall());
	  System.out.println("Simple Accuracy: "+getSimpleAccuracy());
	  System.out.println("Balanced Accuracy: "+getBalancedAccuracy());
	  System.out.println("F1 Measure: "+getF1Measure());
	  System.out.println("\n------------ End of Trivial Classifier Statistics -----------");
	  
    }
}
