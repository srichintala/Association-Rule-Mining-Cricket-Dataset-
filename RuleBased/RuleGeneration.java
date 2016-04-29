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
    
    /*
     * The list of Rule Objects are sorted based on the input parameter.
     * The input parameter values can be accuracy or laplace...
     * Sorting(descending order) based on Laplace is required to remove rules with low coverage and high accuracy..
     * Such rules will be at the end of the list and will be removed during rule pruning...
     */
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
    
    /*
     * Below method removes the unwanted rules based on the cut-off threshold...
     * The validation set is used to tune this cut-off threshold in cross-validation
     */
    public static void removeNegRules(float cutoff){
    	for(int i=listRuleObj.size()-1;i>=0;i--){
    		if(listRuleObj.get(i).accuracy < cutoff)
    			listRuleObj.remove(i);
    	}
    }
    
    /*
     * Below method prunes the rules by removing the positive and negative examples from rules already covered
     * in other rules
     */
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
    					 next_rule_obj.pos_count-=1;  // if the positive example is covered in previous rules, then...
    				 }                                // its count is decremented
    				 else{
    					 next_rule_obj.neg_count-=1;  // Similarly for the negative example
    				 }
    				 
    				 hash_next_rule_obj.remove(record);  // the instance is removed from the rule object
    			  }
    		   }
    		  
    		  if(next_rule_obj.pos_count+next_rule_obj.neg_count == 0)
    			 next_rule_obj.accuracy = 0;      // new accuracy is updated
    		  else
    		     next_rule_obj.accuracy = (float)next_rule_obj.pos_count/(next_rule_obj.pos_count+next_rule_obj.neg_count);
    	   
    		  next_rule_obj.laplace = computeLaplace(next_rule_obj);  // laplace is recomputed...
    	   
    	   }
    	}
    	
    	sortListRuleObj("accuracy");
    	
    	for(RuleObj rule : listRuleObj){
    		listRuleObjBeforePrune.add(rule);  // Rules are stored in this list before pruning...
    										   // Used in EvaluateRuleBasedClassifier.buildRuleBase() for tuning...
    		                                   // cut-off threshold
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
    
    /*
     * Below method reads the header of the dataset...
     */
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
    
    /*
     * Below method reads the training dataset...
     */
    public static void readCricketData(String path, String sep, boolean hasHeader, String label) throws Exception{
    	BufferedReader br = new BufferedReader(new FileReader(path));
    	String s= "";
    	String[] temp;
    	int count = 0;
    	
    	if(!hasHeader)
    		s = br.readLine();
    	
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
    
    /*
     * Below method is used to get the index of the next feature to be added as a conjunct
     */
    public static int getIndexLastName(List<RuleObj> list_init_level, String last_name){
    	for(int i=0;i<list_init_level.size();i++){
    		if(last_name.equals(list_init_level.get(i).name))
    			return i;
    	}
    	
    	return -1;
    }
    
    /*
     * Below method computes the FOIL's information gain
     */
    public static float computeFoil(RuleObj next_rule, RuleObj cur_rule){
    	int p1 = next_rule.pos_count;
    	int p0 = cur_rule.pos_count;
    	
    	int n1 = next_rule.neg_count;
    	int n0 = cur_rule.neg_count;
    	
    	if(p1+n1==0)
    		return 0;
    	
    	double foil = p1*(Math.log((double)p1/(p1+n1))/Math.log(2) - Math.log((double)p0/(p0+n0))/Math.log(2)); 
    	return (float)foil;
    }
    
    /*
     * Below method computes the Laplace
     */
    public static float computeLaplace(RuleObj rule){
    	float laplace = (float)(rule.pos_count+1)/(rule.pos_count+rule.neg_count+2);
		
		return laplace;
    }
    
    /*
     * Below method creates the next rule by adding conjuncts to existing rules
     */
    public static RuleObj createNextRuleObj(RuleObj cur_rule, RuleObj init_rule, String class_label){
    	List<Integer> list_index = new ArrayList<Integer>();
    	RuleObj next_rule = new RuleObj(cur_rule.name+","+init_rule.name); // new rule created by adding a conjunct
    	int pos_count = 0;
    	int neg_count = 0;
    	
    	String[] temp = next_rule.name.split(",");
    	for(int i=0;i<temp.length;i++){
    		list_index.add(hashNameToIndex.get(temp[i]));
    	}
    	
    	for(Integer record : hashData.keySet()){
    	   List<String> list_row = hashData.get(record);	
    	   boolean flag = true;
    	   
    	   for(int index : list_index){
    		  if(!list_row.get(index).equals("1")){  // checks if the new rule covers the instance 
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
    	
    	float accuracy = (float)pos_count/(pos_count+neg_count);
    	next_rule.pos_count = pos_count;
    	next_rule.neg_count = neg_count;
    	next_rule.accuracy = accuracy;
    	
    	next_rule.laplace = computeLaplace(next_rule);
    	
    	return next_rule;
    }
    
    /*
     * Below method populates the initial set of rules
     */
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
    		float accuracy = (float)rule.pos_count/(rule.pos_count+rule.neg_count);
    		rule.accuracy = accuracy;
    		
    		rule.laplace = computeLaplace(rule);
    		
    	}
    	
    	sortListRuleObj("accuracy");
    	mainRulesList.add(listRuleObj);
    	listRuleObj = new ArrayList<RuleObj>();
    }
    
    /*
     * Below method uses the initial set of rules to create the next set of rules...
     * The process continues until no new rules can be generated...
     */
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
	    			if(foil > 0){
	    				listRuleObj.add(next_rule);  // new rule is added to list if the gain is positive
	    			}
	    		}
	    	}
	    	
	    	sortListRuleObj("accuracy");
	    	mainRulesList.add(listRuleObj);         // list of rules is added to main_list
	    	listRuleObj = new ArrayList<RuleObj>();   	
    	}
    	
    	mainRulesList.remove(mainRulesList.size()-1);  // the last list is empty. Hence it is removed...
    	populateListRuleObj("laplace");
    	pruneRules(cutoff);
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
