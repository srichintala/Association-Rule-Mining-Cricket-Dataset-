import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class NaiveBayes {
	static String curDir;
	static HashMap<Integer, List<String>> hashPosClass;
	static HashMap<Integer, List<String>> hashNegClass;
	
	public static void init(){
		curDir = System.getProperty("user.dir");
		hashPosClass = new HashMap<Integer, List<String>>();
		hashNegClass = new HashMap<Integer, List<String>>();
	}
	
	public static void reset(){
		hashPosClass = new HashMap<Integer, List<String>>();
		hashNegClass = new HashMap<Integer, List<String>>();
	}
	
	public static float computePosteriorProb(String[] record, String class_label){
		HashMap<Integer, List<String>> hash = null;
		int count  = 0;
		float class_cond_prob = 1f;
		float class_prob = 0f;
		
		if(class_label.equals("1")){
		   hash = hashPosClass;
		   class_prob = (float)hashPosClass.size()/(hashPosClass.size() + hashNegClass.size());
		}
		else{
		   hash = hashNegClass;
		   class_prob = (float)hashNegClass.size()/(hashPosClass.size() + hashNegClass.size());
		}
		
		for(int i=0;i<record.length-1;i++){
		   count = 0;
		   for(Integer key : hash.keySet()){
			  List<String> list = hash.get(key);
				
			  if(list.get(i).equals(record[i])){
				 count++;
			  }
		   }
		  
		   class_cond_prob*=(float)count/hash.size();
		}
		
		float posterior_prob = class_prob * class_cond_prob;
		return posterior_prob;
		
	}
	
	public static void readTrainData(String path, String sep) throws Exception{
		BufferedReader br = new BufferedReader(new FileReader(path));
		String s = "";
		int count = 0;
		
		while((s=br.readLine())!=null){
			count++;
			String[] temp = s.split(sep);
			List<String> temp_list = Arrays.asList(temp);
			
			if(temp[temp.length-1].equals("1"))
				hashPosClass.put(count, temp_list);
			else
				hashNegClass.put(count, temp_list);
		}
		
		br.close();
	}
	
	/*public static void readTestData(String path, String sep) throws Exception{
		BufferedReader br = new BufferedReader(new FileReader(path));
		String s = "";
		
		while((s=br.readLine())!=null){
			String[] temp = s.split(sep);
			
			evalNaiveBayes(temp);
		}
		
		br.close();
	}*/
	
	/*public static void evalNaiveBayes(String[] record){
		float pos_prob = computePosteriorProb(record, "1");  // positive class
		float neg_prob = computePosteriorProb(record, "0"); // negative class
		
		//System.out.println("Pos_prob: "+pos_prob);
		//System.out.println("Neg_prob: "+neg_prob);
		
		
		String predicted_class = "";
		
		if(pos_prob>=neg_prob)
		   predicted_class = "1";
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
		
	}*/
	
    /*public static void main(String[] args) throws Exception{
    	init();
    	
    	String filename = "cricket_train_naive_bayes.csv";
    	String path = curDir + "\\" + filename;
    	String sep = ",";
    	
    	readTrainData(path, sep);
    	
    	filename = "cricket_test_naive_bayes.csv";
    	path = curDir + "\\" +filename;
    	//readTestData(path, sep);
    	
    	//System.out.println("Tp: "+tp);
    	//System.out.println("Fp: "+fp);
    	//System.out.println("Tn: "+tn);
    	//System.out.println("Fn: "+fn);
    }*/
}

class NaiveBayesRoc{
	float prob;
	String actual_class;
	
	NaiveBayesRoc(float prob, String label){
		this.prob = prob;
		actual_class = label;
	}
}
