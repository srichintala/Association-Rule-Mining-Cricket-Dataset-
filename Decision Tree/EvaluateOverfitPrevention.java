import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;


public class EvaluateOverfitPrevention {
  static String curDir;	
  static int tp;
  static int fp;
  static int tn;
  static int fn;
  static float precision;
  static float recall;
  static int trivial_index;
  
  public static void init(String criteria, int index){
	  OverfitPrevention.init(criteria);
      curDir = System.getProperty("user.dir");
      trivial_index = index;
  }
  
  public static void resetCounters(){
	  tp = 0;
	  fp = 0;
	  tn = 0;
	  fn = 0;
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
   * Below method builds the Decision Tree 
   */
  public static void buildDecisionTree() throws IOException{
	  Set<Integer> setVisited = new LinkedHashSet<Integer>();
	  Set<Integer> setValidRecords = new HashSet<Integer>();
	   
	  for(int i=1;i<=DecisionTree.hashData.size();i++)
		   setValidRecords.add(i);
	   
	  OverfitPrevention.droot = DecisionTree.decisionTree(DecisionTree.hashData, setVisited, 0, setValidRecords,
			                           Float.MIN_VALUE, Float.MAX_VALUE, OverfitPrevention.droot, 0);
   }
  
  /*
   * Below method sets the positive and negative class labels based on the parameter provided.
   * If value of the input parameter is true, then the positive class is 1 and negative class is 0,
   * else the classes are randomly assigned
   */
  public static void setPosNegClass(boolean fixed_YN){
      boolean flag = false;
      OverfitPrevention.setLabels = DecisionTree.setLabels;
      
      if(fixed_YN){
    	OverfitPrevention.pos_class = "1";
    	OverfitPrevention.neg_class = "0";
      }
      else{
	    for(String label : OverfitPrevention.setLabels){
		   if(!flag){
			 OverfitPrevention.pos_class = label;
			 flag = true; 
		   }  
		   else{
			 OverfitPrevention.neg_class = label;
		   }
	    }
      }
   }
  
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
    * Below method predicts the class labels
    */
   public static PredictLabelObj predictLabel(PNode pnode, String[] temp){
	   String predict_class = pnode.label; 
	   float prob = -1f;
	   
	   if(!predict_class.equals("-1")){
		   if(predict_class.equals(OverfitPrevention.pos_class))
			  prob = (float)pnode.label_count/pnode.max_class_count;	
		   else
			  prob =  (float)(pnode.max_class_count - pnode.label_count)/pnode.max_class_count;
		   
	       return new PredictLabelObj(predict_class, prob);
	   }
	   if(predict_class.equals("-1") && pnode.left == null && pnode.right == null){
		   if(predict_class.equals(OverfitPrevention.pos_class))
			  prob = (float)pnode.label_count/pnode.max_class_count;	
		   else
		      prob = (float)(pnode.max_class_count - pnode.label_count)/pnode.max_class_count;
		   
		   return new PredictLabelObj(pnode.max_class_label, prob);
	   }
	   
	   int feature_index = pnode.feature_index;
	   float test_feature_val = Float.parseFloat(temp[feature_index]);
	   float train_feature_val = pnode.split_value;
	    
	   if(test_feature_val <= train_feature_val)
	      return predictLabel(pnode.left, temp);
	   else
	      return predictLabel(pnode.right, temp);
	    
	}
  
   /*
    * Below method evaluates the the test set
    */
  public static void evalTestFile(String file_name, boolean roc_YN, boolean is_trivial_classifier) throws Exception{
	   BufferedReader br = new BufferedReader(new FileReader(file_name));
	   String s="";
	   String[] temp = null;
	   String predict_class = "";
	   float prob = -1f;
	   int total = 0;
	   String pos_class = OverfitPrevention.pos_class;
	   
	   while((s=br.readLine())!=null){
		   total++;
		   temp = s.split(",");
		   
		   if(!is_trivial_classifier){
			   PredictLabelObj pl = predictLabel(OverfitPrevention.proot, temp);
			   predict_class = pl.label;
			   prob = pl.prob;
			   
			   String actual_class = temp[temp.length-1];
			   
			   if(roc_YN){
				  OverfitPrevention.hashROC.put(total, new ROCObj(prob, actual_class)); 
			   }
			   
			   if(predict_class.equals(actual_class)){
			       if(predict_class.equals(pos_class))
			    	   tp++;
			       else
			    	   tn++;
			   }
			   else{
				   if(predict_class.equals(pos_class))
			    	   fp++;
			       else
			    	   fn++;
			   }
		   }
		   else
		      evalTrivialClassifier(temp);
		}
	   
	   br.close();
	}
	
  public static void main(String[] args) throws Exception{
	  String type = "V";   // validation set used to prevent overfitting
	  String criteria = "gini";   // splitting criteria
	  int k = 10;
	  boolean roc_YN = false;
	  boolean fixed_YN = true;
	  boolean is_trivial_classifier = false;
	  int trivial_index = 2;
	  int total_tp = 0;
	  int total_fp = 0;
	  int total_tn = 0;
	  int total_fn = 0;
	  float max = Float.NEGATIVE_INFINITY;
	  Node bestNode = null;
	  
	  init(criteria, trivial_index);
	  
	  String filename = "cricket_train_set.csv";
	  int pos = filename.lastIndexOf(".");
      String ext = filename.substring(pos);
  	  String path = curDir+"\\"+filename;
		 
	  CrossValidation.readDataset(path, false); 

	  int records = CrossValidation.hash.size();
	  System.out.println("Running Cross-Validation...");
	  for(int i=1;i<=k;i++){
		CrossValidation.generatePartitions(i, records, ext, roc_YN);
		resetCounters();
		DecisionTree.hashData.clear();
		DecisionTree.setLabels.clear();
		
		path = curDir+"\\"+"train"+i+ext;
		DecisionTree.readDataset(path, false);
		
		setPosNegClass(fixed_YN);
		 
		OverfitPrevention.droot = new Node();
		buildDecisionTree();

		Node droot = OverfitPrevention.droot;
		if(!type.equals("V"))
			OverfitPrevention.buildBestETree(droot, type);
		else{
			OverfitPrevention.buildValidationSet(path);
			OverfitPrevention.buildBestVTree(droot, path);
		}
		
		path = curDir+"\\"+"test"+i+ext;
		
	    evalTestFile(path, roc_YN, is_trivial_classifier);
	    
	    total_tp+=tp;
	    total_fp+=fp;
	    total_tn+=tn;
	    total_fn+=fn;
	    
	    float accuracy = (float)(tp + tn)/(tp + fp + tn + fn);
	    
	    if(accuracy>max){
	    	max = accuracy;
	    	bestNode = droot;   // best tree is stored based on accuracy
	    }
	  
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
	  
	  /*******************************************************************************/
	  
	  resetCounters();
	  filename = "cricket_train_set.csv";
	  path = curDir+"\\"+filename;
	  DecisionTree.readDataset(path, false);
	  
	  OverfitPrevention.droot = bestNode;   // best tree is used on test set
	  buildDecisionTree();
	  
	  filename = "cricket_test_set.csv";
	  path = curDir+"\\"+filename;
	  evalTestFile(path, true, is_trivial_classifier);
	  
	  OverfitPrevention.getROC();
	  
	  System.out.println("\n------------ Test Set Statistics ------------------");
	  System.out.println("Precision: "+getPrecision());
	  System.out.println("Recall: "+getRecall());
	  System.out.println("Simple Accuracy: "+getSimpleAccuracy());
	  System.out.println("Balanced Accuracy: "+getBalancedAccuracy());
	  System.out.println("F1 Measure: "+getF1Measure());
	  System.out.println("\n------------ End of Test Set Statistics -----------");
	  
	  resetCounters();
	  is_trivial_classifier = true;
	  evalTestFile(path, true, is_trivial_classifier);
	  
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
