import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;


public class RuleGeneration {
    static String curDir;
    static HashMap<Integer, List<String>> hashData;
    static HashMap<Integer, String> hashIndexToName; 
    static HashMap<String, Integer> hashNameToIndex; 
    static List<RuleObj> listRuleObj;
    static List<RuleObj> listRuleObjBeforePrune;
    static List<List<RuleObj>> mainRulesList;
    static int posRecordsCount;
    static int negRecordsCount;
    
    public static void init(){
    	curDir = System.getProperty("user.dir");
    	hashData = new HashMap<Integer, List<String>>();
    	hashIndexToName = new HashMap<Integer, String>();
    	hashNameToIndex = new HashMap<String, Integer>();
    	listRuleObj = new ArrayList<RuleObj>();
    	listRuleObjBeforePrune = new ArrayList<RuleObj>();
    	mainRulesList = new ArrayList<List<RuleObj>>();
    }
    
    public static void reset(){
    	hashData.clear();
    	listRuleObj.clear();
    	mainRulesList.clear();
    	hashIndexToName.clear();
    	hashNameToIndex.clear();
    }
    
    public static void sortListRuleObj(String type){
    	Collections.sort(listRuleObj, new Comparator<RuleObj>(){
    		@Override
    		public int compare(RuleObj r1, RuleObj r2){
    			if(type.equals("accuracy")){
	    			if(r1.accuracy==r2.accuracy)
	    				return 0;
	    			else{
	    				if(r1.accuracy < r2.accuracy)
	    					return 1;
	    				else
	    					return -1;
	    			}
    			}
    			else{
    				if(r1.laplace==r2.laplace)
	    				return 0;
	    			else{
	    				if(r1.laplace < r2.laplace)
	    					return 1;
	    				else
	    					return -1;
	    			}
    			}
    		}
    	});
    }
    
    public static void populateListRuleObj(String type){
    	for(List<RuleObj> list_rule_obj : mainRulesList){
    		for(RuleObj rule : list_rule_obj){
    			listRuleObj.add(rule);
    		}
    	}
    	
    	sortListRuleObj(type);
    }
    
    public static void removeNegRules(float cutoff){
    	for(int i=listRuleObj.size()-1;i>=0;i--){
    		if(listRuleObj.get(i).accuracy < cutoff)
    			listRuleObj.remove(i);
    	}
    }
    
    public static void pruneRules(float cutoff){
    	for(int i=0;i<listRuleObj.size();i++){
    	   RuleObj rule_obj = listRuleObj.get(i);
    	   HashMap<Integer, Boolean> hash_rule_obj = rule_obj.hashRuleData;
    	   
    	   for(int j=i+1;j<listRuleObj.size();j++){
    		  RuleObj next_rule_obj = listRuleObj.get(j);
    		  HashMap<Integer, Boolean> hash_next_rule_obj = next_rule_obj.hashRuleData;
    		  
    		  for(Integer record : hash_rule_obj.keySet()){
    			 if(hash_next_rule_obj.containsKey(record)){
    				 if(hash_next_rule_obj.get(record)){
    					 //System.out.println("Name: "+next_rule_obj.name);
    					 next_rule_obj.pos_count-=1;
    				 }
    				 else{
    					 //System.out.println("Name: "+next_rule_obj.name);
    					 next_rule_obj.neg_count-=1;
    				 }
    				 
    				 hash_next_rule_obj.remove(record);
    			  }
    		   }
    		  
    		  if(next_rule_obj.pos_count+next_rule_obj.neg_count == 0)
    			 next_rule_obj.accuracy = 0;
    		  else
    		     next_rule_obj.accuracy = (float)next_rule_obj.pos_count/(next_rule_obj.pos_count+next_rule_obj.neg_count);
    	   
    		  next_rule_obj.laplace = computeLaplace(next_rule_obj);
    	   
    	   }
    	}
    	
    	sortListRuleObj("accuracy");
    	
    	for(RuleObj rule : listRuleObj){
    		listRuleObjBeforePrune.add(rule);
    	}
    	
    	removeNegRules(cutoff);
    }
    
    public static int getPosRecordsSize(String class_label){
    	int pos_count = 0;
    	
    	for(Integer record : hashData.keySet()){
    		List<String> list_row = hashData.get(record);
    		if(list_row.get(list_row.size()-1).equals("1"))
    	        pos_count++;		
    	}
    	
    	return pos_count;
    }
    
    public static void readHeader(String path, String sep) throws Exception{
    	BufferedReader br = new BufferedReader(new FileReader(path));
    	String[] temp;
    	
    	String s = br.readLine();
  	    temp = s.split(sep);
  	  
  	    for(int i=0;i<temp.length-1;i++){
  		   hashIndexToName.put(i, temp[i]);
  		   hashNameToIndex.put(temp[i],i);
  		   listRuleObj.add(new RuleObj(temp[i]));
  	    }
  	    
  	    br.close();
    }
    
    public static void readCricketData(String path, String sep, boolean hasHeader, String label) throws Exception{
    	BufferedReader br = new BufferedReader(new FileReader(path));
    	String s= "";
    	String[] temp;
    	int count = 0;
    	
    	if(!hasHeader)
    		s = br.readLine();
    	
    	/*if(hasHeader){
    	  s = br.readLine();
    	  temp = s.split(sep);
    	  
    	  for(int i=0;i<temp.length-1;i++){
    		  hashIndexToName.put(i, temp[i]);
    		  hashNameToIndex.put(temp[i],i);
    		  listRuleObj.add(new RuleObj(temp[i]));
    	  }
    	}*/
    	
    	while((s=br.readLine())!=null){
    		count++;
    		temp = s.split(sep);
    		List<String> list = Arrays.asList(temp);
    		
    		hashData.put(count, list);
    	}
    	
    	posRecordsCount = getPosRecordsSize(label);
 	    negRecordsCount = hashData.size()-posRecordsCount;
    	
    	br.close();
    	
    }
    
    public static int getIndexLastName(List<RuleObj> list_init_level, String last_name){
    	for(int i=0;i<list_init_level.size();i++){
    		if(last_name.equals(list_init_level.get(i).name))
    			return i;
    	}
    	
    	return -1;
    }
    
    public static float computeFoil(RuleObj next_rule, RuleObj cur_rule){
    	int p1 = next_rule.pos_count;
    	int p0 = cur_rule.pos_count;
    	
    	int n1 = next_rule.neg_count;
    	int n0 = cur_rule.neg_count;
    	
    	if(p1+n1==0)
    		return 0;
    	
    	//double result0 = -Math.log((double)p0/(p0+n0))/Math.log(2);
    	//double result1 = Math.log((double)p1/(p1+n1))/Math.log(2);
    	
    	//System.out.println("Result0: "+result0+", Result1: "+result1);
    	
    	double foil = p1*(Math.log((double)p1/(p1+n1))/Math.log(2) - Math.log((double)p0/(p0+n0))/Math.log(2)); 
    	return (float)foil;
    }
    
    public static float computeLaplace(RuleObj rule){
    	/*float est_pos_freq = (float)((rule.pos_count+rule.neg_count)*posRecordsCount)/hashData.size();
		float est_neg_freq = (float)((rule.pos_count+rule.neg_count)*negRecordsCount)/hashData.size();
		
		float statistic = (float)(2*( rule.pos_count * Math.log(rule.pos_count/est_pos_freq)/Math.log(2) - 
				                      rule.neg_count * Math.log(rule.neg_count/est_neg_freq)/Math.log(2)));*/
    	
    	float laplace = (float)(rule.pos_count+1)/(rule.pos_count+rule.neg_count+2);
		
		return laplace;
    }
    
    public static RuleObj createNextRuleObj(RuleObj cur_rule, RuleObj init_rule, String class_label){
    	List<Integer> list_index = new ArrayList<Integer>();
    	RuleObj next_rule = new RuleObj(cur_rule.name+","+init_rule.name);
    	int pos_count = 0;
    	int neg_count = 0;
    	
    	//System.out.println("Next Rule Name: "+next_rule.name);
    	
    	String[] temp = next_rule.name.split(",");
    	for(int i=0;i<temp.length;i++){
    		list_index.add(hashNameToIndex.get(temp[i]));
    	}
    	
    	for(Integer record : hashData.keySet()){
    	   List<String> list_row = hashData.get(record);	
    	   boolean flag = true;
    	   
    	   for(int index : list_index){
    		  if(!list_row.get(index).equals("1")){
    			  flag = false;
    		      break;
    		  }
    	   }
    	   
    	   if(flag){
    		  if(list_row.get(list_row.size()-1).equals(class_label))
    			  pos_count++;
    		  else
    			  neg_count++;
    	   }
    	}
    	
    	float accuracy = (float)pos_count/(pos_count+neg_count);  //(float)pos_count/posRecordsCount;
    	next_rule.pos_count = pos_count;
    	next_rule.neg_count = neg_count;
    	next_rule.accuracy = accuracy;
    	
    	next_rule.laplace = computeLaplace(next_rule);
    	
    	return next_rule;
    }
    
    public static void populateInitialLevel(String class_label){
    	for(Entry<Integer, List<String>> e : hashData.entrySet()){
    		List<String> list = e.getValue();
    		
    		for(int i=0;i<list.size()-1;i++){
    			if(list.get(i).equals("1")){
    				if(list.get(list.size()-1).equals(class_label)){
    					listRuleObj.get(i).pos_count++;
    					listRuleObj.get(i).hashRuleData.put(e.getKey(), true);
    				 }
    				else{
    					listRuleObj.get(i).neg_count++;
    					listRuleObj.get(i).hashRuleData.put(e.getKey(), false);
    				}
    			}
    		}
    	}
    	
    	for(RuleObj rule : listRuleObj){
    		float accuracy = (float)rule.pos_count/(rule.pos_count+rule.neg_count); //(float)rule.pos_count/posRecordsCount;
    		rule.accuracy = accuracy;
    		
    		rule.laplace = computeLaplace(rule);
    		
    	}
    	
    	sortListRuleObj("accuracy");
    	mainRulesList.add(listRuleObj);
    	listRuleObj = new ArrayList<RuleObj>();
    	//System.out.println(mainRulesList);
    }
    
    public static void generateRules(String class_label, float cutoff){
    	List<RuleObj> list_init_level = mainRulesList.get(0);
    	
    	while(mainRulesList.get(mainRulesList.size()-1).size() > 0){
	    	List<RuleObj> list_cur_level = mainRulesList.get(mainRulesList.size()-1);
	    	for(int i=0;i<list_cur_level.size();i++){
	    		RuleObj cur_rule = list_cur_level.get(i);
	    		String[] temp = cur_rule.name.split(",");
	    		String last_name = temp[temp.length-1];
	    		
	    		int index = getIndexLastName(list_init_level, last_name);
	    		
	    		if(index==-1)
	    		  continue;
	    		
	    		for(int j=index+1;j<list_init_level.size();j++){
	    			RuleObj init_rule = list_init_level.get(j);
	    			RuleObj next_rule = createNextRuleObj(cur_rule, init_rule, class_label);
	    			
	    			float foil = computeFoil(next_rule, cur_rule);
	    			//System.out.println("Foil: "+foil);
	    			if(foil > 0){
	    				listRuleObj.add(next_rule);
	    			}
	    		}
	    	}
	    	
	    	sortListRuleObj("accuracy");
	    	mainRulesList.add(listRuleObj);
	    	listRuleObj = new ArrayList<RuleObj>();   	
    	}
    	
    	mainRulesList.remove(mainRulesList.size()-1);
    	populateListRuleObj("likelihood_ratio");
    	pruneRules(cutoff);
    }
    
    public static void displayRules() throws Exception{
    	FileWriter fw = new FileWriter("C:\\Users\\Shrijit\\Desktop\\output.txt");
    	for(List<RuleObj> list : mainRulesList){
    		for(RuleObj rule : list){
    			fw.write("Name: "+rule.name+", Pos: "+rule.pos_count+", Neg: "+rule.neg_count+"\n");
    			fw.write("Accuracy: "+rule.accuracy+"\n");
    			fw.write("Likelihood Ratio: "+rule.laplace+"\n");
    			fw.write("-------------------------------------------------------------------------\n");
    			System.out.println("Name: "+rule.name+", Pos: "+rule.pos_count+", Neg: "+rule.neg_count);
    			System.out.println("Accuracy: "+rule.accuracy);
    			System.out.println("-------------------------------------------------------------------------");
    			System.out.println();
    		}
    		
    		fw.write("*********************************************************************\n");
    		System.out.println("*********************************************************************");
    	}
    	
    	fw.close();
    }
    
    public static void displayPrunedRules(int k) throws Exception{
    	FileWriter fw = new FileWriter("C:\\Users\\Shrijit\\Desktop\\output"+k+".txt");
    	
    		for(RuleObj rule : listRuleObj){
    			fw.write("Name: "+rule.name+", Pos: "+rule.pos_count+", Neg: "+rule.neg_count+"\n");
    			fw.write("Accuracy: "+rule.accuracy+"\n");
    			fw.write("Likelihood: "+rule.laplace+"\n");
    			fw.write("-------------------------------------------------------------------------\n");
    			//System.out.println("Name: "+rule.name+", Pos: "+rule.pos_count+", Neg: "+rule.neg_count);
    			//System.out.println("Accuracy: "+rule.accuracy);
    			//System.out.println("-------------------------------------------------------------------------");
    			//System.out.println();
    		}
    		
    		//fw.write("*********************************************************************\n");
    		//System.out.println("*********************************************************************");
    	
    	fw.close();
    }
    
	public static void main(String[] args) throws Exception {
	   init();
	   String filename = "cricket1.train";
	   String path = curDir + "\\" + filename;
	   String sep = ",";
	   boolean hasHeader = true;
	   String class_label = "1";
	   
	   readCricketData(path, sep, hasHeader, class_label);
	   
	   populateInitialLevel(class_label);
	   //generateRules(class_label);
	   //displayRules();
	   displayPrunedRules(3);
    }

}

class RuleObj{
	String name;
	HashMap<Integer, Boolean> hashRuleData;
	int pos_count;
	int neg_count;
	float accuracy;
	float laplace;
	
	RuleObj(String name, int pos, int neg){
		this.name = name;
		this.pos_count = pos;
		this.neg_count = neg;
		hashRuleData = new HashMap<Integer, Boolean>();
	}
	
	RuleObj(String name){
		this.name = name;
		hashRuleData = new HashMap<Integer, Boolean>();
	}
}

class RocObj{
	float prob;
	String actual_class;
	
	RocObj(float prob, String class_label){
		this.prob = prob;
		actual_class = class_label;
	}
}
