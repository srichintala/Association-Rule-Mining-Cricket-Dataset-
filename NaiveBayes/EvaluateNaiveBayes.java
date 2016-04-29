import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class EvaluateNaiveBayes {
   static String curDir;
   static int trivial_index;
   static int tp;
   static int fp;
   static int tn;
   static int fn;
   static List<NaiveBayesRoc> listNaiveBayesRoc;
   
   public static void init(int folds, int index){
	   CrossValidation.init(folds);
	   NaiveBayes.init();
	   curDir = System.getProperty("user.dir");
	   trivial_index = index;  // Team 1 Home
	   listNaiveBayesRoc = new ArrayList<NaiveBayesRoc>();
   }
   
   public static void resetCounters(){
		tp = 0;
		fp = 0;
		fn = 0;
		tn = 0;
	}
   
   public static float getBalancedAccuracy(float tp, float tn, float fp, float fn){
		  float sensitivity = tp/(tp + fn);
		  float specificity = tn/(tn + fp);
		  
		  return (sensitivity + specificity)/2;
	}
	  
	public static float getPrecision(float tp, float fp){
		  float precision = tp/(tp + fp);
		  return precision;
	}
	  
	public static float getRecall(float tp, float fn){
	   float recall = (float) tp/(tp + fn);
	   return recall;
	}
	  
	public static float getF1Measure(float precision, float recall){
	   float f1 = 2*precision*recall/(precision + recall);
	   return f1;
	}
   
	/*
	 * Below method reads the test set
	 */
   public static void readTestData(String path, String sep, boolean display_YN, boolean is_trivial_classifier) throws Exception{
		BufferedReader br = new BufferedReader(new FileReader(path));
		String s = "";
		
		while((s=br.readLine())!=null){
			String[] temp = s.split(sep);
			if(!is_trivial_classifier)
			  evalNaiveBayes(temp);
			else
			  evalTrivialClassifier(temp);	
		}
		
		if(display_YN){
		  float accuracy = (float)(tp+tn)*100/(tp+tn+fp+fn);
		  System.out.println("Correct: "+(tp+tn));
		  System.out.println("Wrong: "+(fp+fn));
		  System.out.println("%Accuracy: "+accuracy);
		  System.out.println("*************************************");
		}
		
		br.close();
	}
   
   /*
    * Below method evaluates the Naive Bayes Classifier
    */
   public static void evalNaiveBayes(String[] record){
		float pos_prob = NaiveBayes.computePosteriorProb(record, "1"); // compute posterior probability for positive class
		float neg_prob = NaiveBayes.computePosteriorProb(record, "0"); // compute posterior probability for negative class
		
		String predicted_class = "";
		
		if(pos_prob>=neg_prob){
		   predicted_class = "1";
		   listNaiveBayesRoc.add(new NaiveBayesRoc(pos_prob, record[record.length-1]));
		}
		else
		   predicted_class = "0";
		
		if(predicted_class.equals("1") && predicted_class.equals(record[record.length-1]))
			tp++;
		else if(predicted_class.equals("1") && !predicted_class.equals(record[record.length-1]))
			fp++;
		else if(predicted_class.equals("0") && predicted_class.equals(record[record.length-1]))     
		    tn++;
		else
			fn++;
		
	}
   
   /*
    * Below method evaluates the Trivial Classifier
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
    * Below method generates the ROC prediction plot...Used to plot the ROC curve...
    */
   public static void generateNaiveBayesRoc(String path) throws Exception{
	   FileWriter fw = new FileWriter(path);
	   
	   fw.write("Predictions, Label\n");
	   for(NaiveBayesRoc roc : listNaiveBayesRoc){
		   fw.write(roc.prob+","+roc.actual_class+"\n");
	   }
	   
	   fw.close();
   }
   
   public static void main(String[] args) throws Exception{
	  int k = 10;
	  int trivial_index = 2;  // Team 1 Home match
	  
	  init(k, trivial_index);
	  float total_tp = 0f;
	  float total_fp = 0f;
	  float total_tn = 0f;
	  float total_fn = 0f;
	  float total_accuracy = 0f;
	  boolean trivial_classifier_YN = false;
	  
	  boolean roc_YN = false;
	  boolean display_YN = true;
	  String filename =  "cricket_train_set.csv";
	  String sep = ",";
	  int pos = filename.lastIndexOf(".");
	  String ext = filename.substring(pos);
	  String path = curDir+"\\"+filename;
	   
	  CrossValidation.readDataset(path, false); 
	  int records = CrossValidation.hash.size();
	  
	  System.out.println("Running Cross-Validation...\n");
	  for(int i=1;i<=k;i++){
		   resetCounters();
		   
		   CrossValidation.generatePartitions(i, records, ext, roc_YN);
		   path = curDir+"\\"+"train"+i+ext;
		   
		   NaiveBayes.reset();
		   NaiveBayes.readTrainData(path, sep);
		   
		   path = curDir+"\\"+"test"+i+ext;
		   System.out.println("Fold: "+i);
		   readTestData(path, sep, display_YN, trivial_classifier_YN);
		   
		   total_tp+=tp;
		   total_fp+=fp;
		   total_tn+=tn;
		   total_fn+=fn;
		   total_accuracy+= (float)(tp+tn)/(tp+fp+tn+fn);
	   }
	  
	  float bal_accuracy = getBalancedAccuracy(total_tp, total_tn, total_fp, total_fn);
	  float precision = getPrecision(total_tp, total_fp);
	  float recall = getRecall(total_tp, total_fn);
	  float f1_measure = getF1Measure(precision, recall);
	  
	  System.out.println("\n----Cross Validation Statistics-----");
	  System.out.println("Simple Accuracy: "+total_accuracy/k);
	  System.out.println("Balanced Accuracy: "+bal_accuracy);
	  System.out.println("Precision: "+precision);
	  System.out.println("Recall: "+recall);
	  System.out.println("F1 Measure: "+f1_measure);
	  System.out.println("\n----End of Cross Validation Statistics-----");
	  
	  /***********************************************************************************/
	  
	  resetCounters();
	  NaiveBayes.reset();
	  
	  filename = "cricket_train_set.csv";
	  path = curDir+"\\"+filename;
	  NaiveBayes.readTrainData(path, sep);
	   
	  filename = "cricket_test_set.csv";
	  path = curDir + "\\" + filename;
	  display_YN = false;
	  readTestData(path, sep, display_YN, trivial_classifier_YN);
	  
	  filename = "naive_bayes_roc.csv";
	  path = curDir + "\\" + filename;
	  generateNaiveBayesRoc(path);
	  
	  total_accuracy = (float)(tp+tn)/(tp+tn+fp+fn);
	  bal_accuracy = getBalancedAccuracy(tp, tn, fp, fn);
	  precision = getPrecision(tp, fp);
	  recall = getRecall(tp, fn);
	  f1_measure = getF1Measure(precision, recall);
	  
	  System.out.println("\n------- Test Set Statistics --------");
	  System.out.println("Simple Accuracy: "+total_accuracy);
	  System.out.println("Balanced Accuracy: "+bal_accuracy);
	  System.out.println("Precision: "+precision);
	  System.out.println("Recall: "+recall);
	  System.out.println("F1 Measure: "+f1_measure);
	  System.out.println("\n----End of Test Set Statistics-----");
	  
	  /***************************************************************************/
	  resetCounters();
	  trivial_classifier_YN = true;
	  filename = "cricket_test_set.csv";
	  path = curDir + "\\" + filename;
	  readTestData(path, sep, display_YN, trivial_classifier_YN);
	  
	  total_accuracy = (float)(tp+tn)/(tp+tn+fp+fn);
	  bal_accuracy = getBalancedAccuracy(tp, tn, fp, fn);
	  precision = getPrecision(tp, fp);
	  recall = getRecall(tp, fn);
	  f1_measure = getF1Measure(precision, recall);
	  
	  System.out.println("\nTrivial Classifier: If Team 1 plays at Home, then assign Positive class(Team 1 Wins)");
	  System.out.println("------- Trivial Classifier Statistics --------");
	  System.out.println("Simple Accuracy: "+total_accuracy);
	  System.out.println("Balanced Accuracy: "+bal_accuracy);
	  System.out.println("Precision: "+precision);
	  System.out.println("Recall: "+recall);
	  System.out.println("F1 Measure: "+f1_measure);
	  System.out.println("\n----End of Trivial Classifier Statistics-----");
   }
}
