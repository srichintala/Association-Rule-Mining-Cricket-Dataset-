import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;


public class OverfitPrevention {
   static String curDir;
   static Node droot;
   static PNode proot;
   static int total_records;
   static Set<String> setLabels;
   static HashMap<Integer, ROCObj> hashROC;  // stores the probability and the actual class label to generate ROC 
   static HashMap<Float, ROCAxesObj> hashROCAxes;
   static String pos_class;
   static String neg_class;
   
   public static void init(String criteria){
	   curDir = System.getProperty("user.dir");
	   droot = new Node();	  
	   CrossValidation.init(10);
	   DecisionTree.init(criteria);
	   
	   hashROC = new HashMap<Integer, ROCObj>();
	   hashROCAxes = new HashMap<Float, ROCAxesObj>();
	   total_records = DecisionTree.hashData.size();
	}
   
    public static void getROC() throws Exception{
	   List<ROCObj> listRoc = new ArrayList<ROCObj>();
	   for(Entry<Integer, ROCObj> e : hashROC.entrySet()){
		   listRoc.add(e.getValue());
	   }
	   
	   Collections.sort(listRoc, new Comparator<ROCObj>(){
		   @Override
		   public int compare(ROCObj r1, ROCObj r2){
			   if(r1.threshold == r2.threshold)
				   return 0;
			   else if(r1.threshold < r2.threshold)
				      return -1;
			   else
				   return 1;
		   }
		   
	   });
	   
	   int roc_tp = 0;
	   int roc_fp = 0;
	   int roc_tn = 0;
	   int roc_fn = 0;
	   
	   displayListROC(listRoc);
	   
	   /*System.out.println();
	   System.out.println("Positive Class: "+pos_class);
	   System.out.println("Negative Class: "+neg_class);
	   System.out.println();*/
	   
	   Set<Float> set_visited = new HashSet<Float>();
	   for(ROCObj r : listRoc){
		   if(!set_visited.contains(r.threshold)){
			   float threshold = r.threshold;
			   set_visited.add(threshold);
			   
			   roc_tp = 0;
			   roc_fp = 0;
			   roc_tn = 0;
			   roc_fn = 0;
			   
			   for(ROCObj r1 : listRoc){
				   if(r1.threshold <= threshold){
					   if(r1.actual_class.equals(neg_class))
						   roc_tn++;
					   else
						   roc_fn++;
				   }
				   else{
					   if(r1.actual_class.equals(pos_class))
						   roc_tp++;
					   else
						   roc_fp++;
				   }
			   }
			   
			   //System.out.println("Roc_tn: "+roc_tn);
			   //System.out.println("Roc_tp: "+roc_tp);
			   //System.out.println("Roc_fp: "+roc_fp);
			   //System.out.println("Roc_fn: "+roc_fn);
			   
			   float deno_tpr = 1f;
			   float deno_fpr = 1f;
			   float deno_precision = 1f;
			   float deno_recall = 1f;
			   
			   if(roc_tp+roc_fn != 0)
			      deno_tpr = (float) (roc_tp+roc_fn);
			   
			   if(roc_fp+roc_tn != 0)
				  deno_fpr = (float)(roc_fp+roc_tn);
			   
			   if(roc_tp + roc_fp != 0)
				   deno_precision = (float)(roc_tp + roc_fp);
			   
			   if(roc_tp + roc_fn != 0)
				   deno_recall = (float)(roc_tp + roc_fn);
				   
			   float tpr = (float)roc_tp/deno_tpr;
			   float fpr = (float)roc_fp/deno_fpr;
			   float precision = (float)roc_tp/deno_precision;
			   float recall = (float)roc_tp/deno_recall;
			   
			   hashROCAxes.put(threshold, new ROCAxesObj(tpr, fpr, precision, recall));
		   }
	   }
	   
   }
   
   public static void displayListROC(List<ROCObj> listRoc) throws Exception{
	   System.out.println();
	   System.out.println("Prediction Plot for ROC and Precision-Recall Curve: ");
	   System.out.println(curDir+"\\"+"prediction_plot_roc.csv");
	   
	   FileWriter fw = new FileWriter("prediction_plot_roc.csv");
	   fw.write("Predictions, Label \n");
	   
	   for(int i=0;i<listRoc.size();i++){
		   fw.write(listRoc.get(i).threshold+","+listRoc.get(i).actual_class+"\n");
		   //System.out.println("Instance: "+(i+1)+", Probability: "+listRoc.get(i).threshold+", Actual Class: "+listRoc.get(i).actual_class);
	   }
	   
	   fw.close();
   }
   
   public static void displayHashROCAxes(){
	   for(Entry<Float, ROCAxesObj> e : hashROCAxes.entrySet()){
		   System.out.println("Key: "+e.getKey()+", TPR: "+e.getValue().tpr+", FPR: "+e.getValue().fpr+
				              ", Precision: "+e.getValue().precision+", Recall "+e.getValue().recall);
		}
   }
   
   /*
    * Below method builds the validation set
    */
   public static void buildValidationSet(String path) throws Exception{
	   BufferedReader br = new BufferedReader(new FileReader(path));
	   HashMap<Integer, String> hash = new HashMap<Integer, String>();
	   String s = "";
	   int count = 0;
	   String filename = path.substring(path.lastIndexOf("\\")+1);
	   
	   FileWriter fw = new FileWriter("v_"+filename);
	   while((s=br.readLine())!=null){
		   count++;
		   hash.put(count, s);
	   }
	   
	   for(int i=1;i<=count/4;i++){
		   fw.write(hash.get(i)+"\n");
	   }
	   
	   fw.close();
	   br.close();
	   
   }
   
   public static void displayTree(Node node, FileWriter fw) throws IOException{
	   if(node == null)
		   return;
	   else{
		   if(fw!=null)
		     fw.write("Class: "+node.label+", Index: "+node.featureIndex+", Split Value: "+node.featureValue+"\n");
		   
		   System.out.println("Class: "+node.label+", Index: "+node.featureIndex+", Split Value: "+node.featureValue+
				              ", Max Class Label: "+node.max_class_label+", Max Count: "+node.max_class_count+
				              ", Label Count: "+node.label_count);
		   displayTree(node.left, fw);
		   displayTree(node.right, fw);
	   }
   }
   
   public static void displayETree(PNode node, FileWriter fw) throws IOException{
	   if(node == null)
		   return;
	   else{
		   if(fw!=null)
		     fw.write("Class: "+node.label+", Index: "+node.feature_index+", Split Value: "+node.split_value+"\n");
		   
		   System.out.println("Class: "+node.label+", Index: "+node.feature_index+", Split Value: "+node.split_value+
				              ", Max Class Label: "+node.max_class_label+", Max Count: "+node.max_class_count+
				              ", Label Count: "+node.label_count);
		   displayETree(node.left, fw);
		   displayETree(node.right, fw);
	   }
   }
   
   /*
    * Below method gives the majority class labels to internal nodes
    */
   public static void refinePTree(PNode pnode){
	   if(pnode == null)
		   return;
	   
	   if(!setLabels.contains(pnode.label) && pnode.left == null && pnode.right == null){
		   pnode.label = pnode.max_class_label;
		   return;
	   }
	   else{
		   refinePTree(pnode.left);
		   refinePTree(pnode.right);
	   }
   }
   
   /*
    * Below method predicts the class given the test file
    */
   public static PredictLabelObj predictLabel(PNode pnode, String[] temp){
	   String predict_class = pnode.label; 
	   float prob = -1f;
	   
	   if(!predict_class.equals("-1")){
		   if(predict_class.equals(pos_class))
			  prob = (float)pnode.label_count/pnode.max_class_count;	
		   else
			  prob =  (float)(pnode.max_class_count - pnode.label_count)/pnode.max_class_count;
		   
	       return new PredictLabelObj(predict_class, prob);
	   }
	   if(predict_class.equals("-1") && pnode.left == null && pnode.right == null){
		   if(predict_class.equals(pos_class))
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
    * Below method calculates the tp,fp,tn and fn values
    */
   public static void evalTestFile(String file_name, boolean roc_YN) throws Exception{
	   BufferedReader br = new BufferedReader(new FileReader(file_name));
	   String s="";
	   String[] temp = null;
	   String predict_class = "";
	   float prob = -1f;
	   int correct = 0;
	   int total = 0;
	   String pos_class = "";
	   int tp = 0;
	   int tn = 0;
	   int fp = 0;
	   int fn = 0;
	   
	   while((s=br.readLine())!=null){
		   total++;
		   temp = s.split(",");
		   PredictLabelObj pl = predictLabel(proot, temp);
		   predict_class = pl.label;
		   prob = pl.prob;
		   
		   String actual_class = temp[temp.length-1];
		   
		   if(roc_YN){
			  hashROC.put(total, new ROCObj(prob, actual_class)); 
		   }
		   
		   if(predict_class.equals(actual_class)){
			   correct++;
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
	   
	   System.out.println("Accuracy: "+ (float)correct/total);
	   System.out.println("Total: "+total+", Correct: "+correct);
	   
	   br.close();
	   
   }
   
   /*
    * Below method evaluates the validation set and returns the error
    */
   public static int evalValidationFile(String file_name, PNode pnode) throws Exception{
	   BufferedReader br = new BufferedReader(new FileReader(file_name));
	   String s="";
	   String[] temp = null;
	   String predict_class = "";
	   int correct = 0;
	   int total = 0;
	   
	   while((s=br.readLine())!=null){
		   total++;
		   temp = s.split(",");
		   PredictLabelObj pl = predictLabel(pnode, temp);
		   predict_class = pl.label;
		   
		   String actual_class = temp[temp.length-1];
		   if(predict_class.equals(actual_class))
			   correct++;
		}
	   
	   br.close();
	   
	   return total - correct;
   }
  
  
   public static void buildDecisionTree() throws IOException{
		  Set<Integer> setVisited = new LinkedHashSet<Integer>();
		  Set<Integer> setValidRecords = new HashSet<Integer>();
		   
		  for(int i=1;i<=DecisionTree.hashData.size();i++)
			   setValidRecords.add(i);
		   
		  droot = DecisionTree.decisionTree(DecisionTree.hashData, setVisited, 0, setValidRecords,
				                           Float.MIN_VALUE, Float.MAX_VALUE, droot, 0);
   }
   
   /*
    * Below method builds the estimated tree 
    */
   public static void buildETree(PNode pnode, Node node, Node parent, int direction){
	   if(pnode == null)
	     return;
	   
	   if(parent == null){
		   pnode.feature_index = node.featureIndex;
		   pnode.label = node.label;
		   pnode.label_count = node.label_count;
		   pnode.max_class_count = node.max_class_count;
		   pnode.max_class_label = node.max_class_label;
		   pnode.split_value = node.featureValue;
		   return;
	   }
	   else{
		   if(pnode.feature_index == parent.featureIndex && pnode.split_value == parent.featureValue){
			   PNode new_pnode = new PNode(node);
			   if(direction == 0)
				   pnode.left = new_pnode;
			   else
				   pnode.right = new_pnode;
			   return;
		   }
		   else{
			   buildETree(pnode.left, node, parent, direction);
			   buildETree(pnode.right, node, parent, direction);
		   }
	   }
   }
   
   /*
    * Below method gets the misclassified record count and count of leaf nodes required to calculate 
    * pessimistic value and MDL 
    */
   public static MisclassifiedObj getEstimate(PNode pnode, MisclassifiedObj missObj){
	   if(setLabels.contains(pnode.label)){
		   missObj.leaf_count++;
		   missObj.record_count += pnode.max_class_count;
		   return missObj;
	   }
	   else{
		  if(pnode.left == null && pnode.right == null){
			  missObj.leaf_count++;
			  missObj.misclassified_count += pnode.max_class_count - pnode.label_count;
			  missObj.record_count += pnode.max_class_count;
			  return missObj;
		  }
		  else{
			  missObj.internal_count++;
			  getEstimate(pnode.left, missObj);
			  getEstimate(pnode.right, missObj);
			  return missObj;
		  }
	   }
   }

   /*
    * Below method builds the tree using either pessimistic or MDL(based on input). The tree stops growing 
    * once the current error exceeds the previous error. 
    */
   public static void buildBestETree(Node node, String type) throws Exception{
	   Queue<Node> queue = new LinkedList<Node>();
	   PNode root = new PNode();
	   float error = -1f;
	   float prev_error = -1f;
	   
	   queue.add(node);
	   
	   buildETree(root, node, null, -1);
	   MisclassifiedObj missObj = getEstimate(root, new MisclassifiedObj());
	   
	   if(type.equals("P"))
	     error = (float) ((missObj.misclassified_count + missObj.leaf_count * 0.5) / missObj.record_count);
	   else
		 error = (float) (missObj.internal_count *  Math.log(missObj.internal_count)/Math.log(2) + 
		         missObj.leaf_count * Math.log(missObj.leaf_count)/Math.log(2) + 
		         missObj.misclassified_count * Math.log(missObj.record_count)/Math.log(2));
	   
	   prev_error = error;
       
       PNode prev_root = new PNode(root);
	   
	   while(!queue.isEmpty()){
		   Node n = queue.remove();
		   
		   if(n.left != null){
		     queue.add(n.left);
		     buildETree(root, n.left, n, 0);
		   }
		   
		   if(n.right != null){
		     queue.add(n.right);
		     buildETree(root, n.right, n, 1);
		   }
		   
		   missObj = getEstimate(root, new MisclassifiedObj());
		   
		   if(type.equals("P"))
		     error = (float) ((missObj.misclassified_count + missObj.leaf_count * 0.5) / missObj.record_count);
		   else
			 error = (float) (missObj.internal_count *  Math.log(missObj.internal_count)/Math.log(2) + 
				              missObj.leaf_count * Math.log(missObj.leaf_count)/Math.log(2) + 
				              missObj.misclassified_count * Math.log(missObj.record_count)/Math.log(2));  
		   
		   if(prev_error < error)
		      break;
		   
		   prev_error = error;
		   prev_root = new PNode(root);
		   
	   }
	  
	  proot = prev_root; // The previous tree is returned as it has lower error than current tree
	  refinePTree(proot);
	   
   }
   
   /*
    * Below method builds the best tree using the validation set
    */
   public static void buildBestVTree(Node node, String fileName) throws Exception{
	   Queue<Node> queue = new LinkedList<Node>();
	   PNode root = new PNode();
	   float error = -1f;
	   float prev_error = -1f;
	   int count = 0;
	   
	   queue.add(node);
	   buildETree(root, node, null, -1);
	   
	   error = evalValidationFile(fileName, root);
	   prev_error = error;
       
       PNode prev_root = new PNode(root);
	   
	   while(!queue.isEmpty()){
		   Node n = queue.remove();
		   
		   if(n.left != null){
		     queue.add(n.left);
		     buildETree(root, n.left, n, 0);
		   }
		   
		   if(n.right != null){
		     queue.add(n.right);
		     buildETree(root, n.right, n, 1);
		   }
		   	   
		   error = evalValidationFile(fileName, root);
		   
		   if(prev_error > error)
			   count = 0;
		   
		   if(prev_error == error){
			   count++;
			   if(count == 3)  // no improvement in error, then stop growing  
				   break;
		   }
		   
		   if(prev_error < error){
			  break;
		   }
		   
		   prev_error = error;
		   prev_root = new PNode(root);
		   
	   }
	  
	  proot = prev_root;  // The previous tree is returned as it has lower error than current tree
	  refinePTree(proot); // Assign majority class label to internal nodes
	   
   }
	   
}

class PNode{
	int feature_index;
	float split_value;
	String label;
	PNode left;
	PNode right;
    String max_class_label;
    int max_class_count;
    int label_count;
    
    PNode(Node node){
    	this.feature_index = node.featureIndex;
    	this.label = node.label; 
    	this.max_class_count = node.max_class_count;
    	this.max_class_label = node.max_class_label;
    	this.label_count = node.label_count;
    	this.split_value = node.featureValue;
    }
    
    PNode(PNode pnode){
    	this(pnode.feature_index, pnode.label, pnode.label_count, pnode.left, pnode.right, pnode.max_class_count,
    	     pnode.max_class_label, pnode.split_value);
    }
    
    PNode(int feature_index, String label, int label_count, PNode left, PNode right, 
    	  int max_class_count, String max_class_label, float split_value){
    	
    	this.feature_index = feature_index;
    	this.split_value = split_value;
    	this.label = label;
    	this.left = left;
    	this.right = right;
    	this.max_class_count = max_class_count;
    	this.max_class_label = max_class_label;
    	this.label_count = label_count;
    	 	
    }
    
    PNode(){
    	feature_index = -1;
    	label = "";
    	max_class_label = "";
    	max_class_count = -1;
    	label_count = -1;
    }
    
    
}

class MisclassifiedObj{
	int leaf_count;
	int internal_count;
	int misclassified_count;
	int record_count;
	
	MisclassifiedObj(){
		leaf_count = 0;
		misclassified_count = 0;
		record_count = 0;
		internal_count = 0;
	}
}

class ROCObj{
	float threshold;
	String actual_class;
	
	ROCObj(){}
	
	ROCObj(float threshold, String actual_class){
		this.threshold = threshold;
		this.actual_class = actual_class;
	}
}

class PredictLabelObj{
	String label;
	float prob;
	
	PredictLabelObj(){}
	
	PredictLabelObj(String label, float prob){
		this.label = label;
		this.prob = prob;
	}
}

class ROCAxesObj{
	float tpr;
	float fpr;
	float precision;
	float recall;
	
	ROCAxesObj(float tpr, float fpr, float precision, float recall){
		this.tpr = tpr;
		this.fpr = fpr;
		this.precision = precision;
		this.recall = recall;
	}
	
}

