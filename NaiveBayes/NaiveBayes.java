import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class NaiveBayes {
	static String curDir;
	static HashMap<Integer, List<String>> hashPosClass;  // stores the records with positive class label
	static HashMap<Integer, List<String>> hashNegClass;  // stores the records with negative class label
	
	
	public static void init(){
		curDir = System.getProperty("user.dir");
		hashPosClass = new HashMap<Integer, List<String>>();
		hashNegClass = new HashMap<Integer, List<String>>();
	}
	
	public static void reset(){
		hashPosClass = new HashMap<Integer, List<String>>();
		hashNegClass = new HashMap<Integer, List<String>>();
	}
	
	/*
	 * Below method computes the posterior probability for the given instance
	 */
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
	
	/*
	 * Below method reads the training dataset
	 */
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
	
}

class NaiveBayesRoc{
	float prob;
	String actual_class;
	
	NaiveBayesRoc(float prob, String label){
		this.prob = prob;
		actual_class = label;
	}
}
