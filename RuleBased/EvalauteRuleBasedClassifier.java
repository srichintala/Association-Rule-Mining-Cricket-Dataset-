import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;


public class EvalauteRuleBasedClassifier {
	static String curDir;
	static List<RuleObj> listRuleObj;
	static HashMap<String, Integer> hashNameToIndex;
	static List<RocObj> listRocObj;
	static List<RuleObj> listBestRuleObj;
	static int trivial_index;
	static int correct;
	static int wrong;
	static int tp;
	static int fp;
	static int tn;
	static int fn;
	
	
	public static void init(int folds, int index){
		CrossValidation.init(folds);
		RuleGeneration.init();
		curDir = RuleGeneration.curDir;
		hashNameToIndex = RuleGeneration.hashNameToIndex;
		listRocObj = new ArrayList<RocObj>();
		listBestRuleObj = new ArrayList<RuleObj>();
		trivial_index = index;
	}
	
	public static void resetCounters(){
		correct = 0;
		wrong = 0;
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
	 * Below method evaluates the trivial classifier
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
	
	public static void sortListRocObj(){
		Collections.sort(listRocObj, new Comparator<RocObj>(){
			@Override
			public int compare(RocObj r1, RocObj r2){
				if(r1.prob == r2.prob)
					return 0;
				else{
					if(r1.prob < r2.prob)
						return 1;
					else
						return -1;
				}
			}
		});
	}
	
	/*public static void displayRecord(String[] record){
		for(int i=0;i<record.length;i++){
			System.out.print(record[i]+", ");
		}
		System.out.println();
	}*/
	
	/*
	 * Below method predicts the class label
	 */
	public static void predictClass(String[] record){
		int count = 0 ;
		
		for(RuleObj rule : listRuleObj){
			count=0;
			String[] ante = rule.name.split(",");
			
			for(int j=0;j<ante.length;j++){
				int index = hashNameToIndex.get(ante[j]);
				
				if(record[index].equals("1"))
					count++;
			}
			
			if(count==ante.length){
				float pos_prob = (float)rule.pos_count/(rule.pos_count+rule.neg_count);
				listRocObj.add(new RocObj(pos_prob, record[record.length-1]));
				
				if(record[record.length-1].equals("1")){
				   correct++;
				   tp++;
				   
				 }
				else{
				   fp++;	
				   wrong++;
				}
				
				return;
			}
		}
		
		listRocObj.add(new RocObj(0f, record[record.length-1]));
		if(record[record.length-1].equals("1")){
			wrong++;
			fn++;
		}
		else{
		   tn++;	
		   correct++;
		}
	}
	
	/*
	 * Below method reads the test set data
	 */
	public static void readTestData(String path, boolean display_YN, boolean is_trivial_classifier) throws Exception{
		BufferedReader br = new BufferedReader(new FileReader(path));
		String s = "";
		
		while((s=br.readLine())!=null){
			String[] temp = s.split(",");
			
			if(!is_trivial_classifier)
			   predictClass(temp);
			else
			   evalTrivialClassifier(temp);	
		}
		
		float accuracy = (float)correct*100/(correct+wrong);
		if(display_YN){
		  System.out.println("Correct: "+correct);
		  System.out.println("Wrong: "+wrong);
		  System.out.println("%Accuracy: "+accuracy);
		  System.out.println("*********************************");
		}
		
		br.close();
	}
	
	
	public static void performRuleGeneration(String class_label, float cutoff){
		RuleGeneration.populateInitialLevel(class_label);
		RuleGeneration.generateRules(class_label, cutoff);
	}
	
	/*
	 * Below method builds the validation set...
	 */
	public static void buildValidationSet(String path) throws Exception{
		BufferedReader br = new BufferedReader(new FileReader(path));
		String s="";
		int count = 0;
		HashMap<Integer, String> hash = new HashMap<Integer, String>();
		
		while((s=br.readLine())!=null){
			count++;
			hash.put(count, s);
		}
		
		String filename = path.substring(path.lastIndexOf("\\")+1);
		FileWriter fw = new FileWriter("vtest_"+filename);
		
		int i=0;
		for(i=1;i<=count/4;i++){
		   fw.write(hash.get(i)+"\n");	
		}
		fw.close();
		
		fw = new FileWriter("vtrain_"+filename);
		for(;i<=hash.size();i++){
			fw.write(hash.get(i)+"\n");
		}
		
		fw.close();
		br.close();
		
	}
	
	/*
	 * Below method builds the Rule base...The cut-off threshold for accuracy is tuned using the validation set...
	 */
	public static void buildRuleBase(String path, String sep, boolean hasHeader, String class_label) throws Exception{
		boolean is_trivial_classifier = false;
		float init_cutoff = 0.5f;
		float max = Float.NEGATIVE_INFINITY;
		
		resetCounters();
		
		String filename = "vtrain_"+path.substring(path.lastIndexOf("\\")+1);
		String temp_path = curDir+"\\"+filename;
		
		RuleGeneration.readCricketData(temp_path, sep, hasHeader, class_label);
		performRuleGeneration(class_label, init_cutoff);
		
		filename = "vtest_"+path.substring(path.lastIndexOf("\\")+1);
		temp_path = curDir+"\\"+filename;
		
		readTestData(temp_path, false, is_trivial_classifier);
		
		float accuracy = (float)correct/(correct+wrong);
		if(accuracy>max){  
		   max = accuracy; 
		   listRuleObj = RuleGeneration.listRuleObj;
		}
		
		for(float i=0.6f;i<=0.85f;i+=0.05f){  // for tuning the cut-off threshold...
		   resetCounters();
		   
		   RuleGeneration.listRuleObj = RuleGeneration.listRuleObjBeforePrune;
		   RuleGeneration.removeNegRules(i);
		   	
		   filename = "vtest_"+path.substring(path.lastIndexOf("\\")+1);
		   temp_path = curDir+"\\"+filename;
		   readTestData(temp_path, false, is_trivial_classifier);
		   
		   accuracy = (float)correct/(correct+wrong);
		   if(accuracy> max){
			 listRuleObj = new ArrayList<RuleObj>();  
			 max = accuracy; 
		     listRuleObj = RuleGeneration.listRuleObj;   // best rules get stored in listRuleObj
		   }
		}
		
		resetCounters();
	}
	
	/*
	 * Below method generates the ROC prediction plot...Used for plotting the ROC curve..
	 */
	public static void generateRocFile(String filename) throws Exception{
		FileWriter fw = new FileWriter(filename);
		fw.write("Predictions,Label\n");
		   
		for(RocObj roc : listRocObj){
		  fw.write(roc.prob+","+roc.actual_class+"\n");
		}
		   
		fw.close();
	}
	
	
	public static void main(String[] args) throws Exception{
	   int k = 10;
	   int trivial_index = 2;  // Team 1 home match
	   
	   init(k, trivial_index);
	   
	   boolean roc_YN = false;
	   String sep = ",";
	   boolean hasHeader = false;
	   boolean is_trivial_classifier = false;
	   String class_label = "1";
	   float total_tp = 0f;
	   float total_fp = 0f;
	   float total_tn = 0f;
	   float total_fn = 0f;
	   float total_accuracy = 0f;
	   float max = Float.NEGATIVE_INFINITY;
	   
	   String filename = "cricket_header.csv";
	   String header_path = curDir+"\\"+filename;
	   
	   filename =  "cricket_train_set.csv";
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
		   
		   RuleGeneration.reset();
		   listRuleObj = new ArrayList<RuleObj>();
		   
		   RuleGeneration.readHeader(header_path, sep);
		   buildValidationSet(path);
		   buildRuleBase(path, sep, hasHeader, class_label);
		   
		   listRocObj.clear();
		   path = curDir+"\\"+"test"+i+ext;
		   System.out.println("Fold: "+i);
		   readTestData(path, true, is_trivial_classifier);
		   
		   float temp_accuracy = (float)correct/(correct+wrong);
		   if(temp_accuracy > max){
			   max = temp_accuracy;
			   for(RuleObj rule : listRuleObj){
				   listBestRuleObj.add(rule);
			   }
		   }
		   
		   total_tp+=tp;
		   total_fp+=fp;
		   total_tn+=tn;
		   total_fn+=fn;
		   total_accuracy+= (float)correct/(correct+wrong);
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
	   
	   /**********************************************************************/
	   
	   RuleGeneration.reset();
	   resetCounters();
	   
	   RuleGeneration.readHeader(header_path, sep);
	   listRuleObj = listBestRuleObj;
	   listRocObj.clear();
	   
	   filename = "cricket_test_set.csv";
	   path = curDir + "\\" + filename;
	   readTestData(path, false, is_trivial_classifier);
	   
	   filename = "rule_prediction_roc.csv";
	   sortListRocObj();
	   generateRocFile(filename);
	   
	   bal_accuracy = getBalancedAccuracy(tp, tn, fp, fn);
	   precision = getPrecision(tp, fp);
	   recall = getRecall(tp, fn);
	   f1_measure = getF1Measure(precision, recall);
	   
	   total_accuracy = (float)(tp+tn)/(tp+tn+fp+fn);
	   System.out.println("\n------- Test Set Statistics --------");
	   System.out.println("Simple Accuracy: "+total_accuracy);
	   System.out.println("Balanced Accuracy: "+bal_accuracy);
	   System.out.println("Precision: "+precision);
	   System.out.println("Recall: "+recall);
	   System.out.println("F1 Measure: "+f1_measure);
	   System.out.println("\n----End of Test Set Statistics-----");
	   
	   /*********************************************************************/
	   
	   resetCounters();
	   is_trivial_classifier = true;
	   
	   readTestData(path, false, is_trivial_classifier);
	   
	   bal_accuracy = getBalancedAccuracy(tp, tn, fp, fn);
	   precision = getPrecision(tp, fp);
	   recall = getRecall(tp, fn);
	   f1_measure = getF1Measure(precision, recall);
	   
	   
	   System.out.println("\nTrivial Classifier: If Team 1 plays at Home, then assign Positive class(Team 1 Wins)");
	   System.out.println("\n------- Trivial Classifier Statistics --------");
	   total_accuracy = (float)(tp+tn)/(tp+tn+fp+fn);
	   System.out.println("Simple Accuracy: "+total_accuracy);
	   System.out.println("Balanced Accuracy: "+bal_accuracy);
	   System.out.println("Precision: "+precision);
	   System.out.println("Recall: "+recall);
	   System.out.println("F1 Measure: "+f1_measure);
	   System.out.println("\n----End of Trivial Classifier Statistics-----");
	}
}
