import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;


public class CombinedBeamSearch {
	static List<BeamNodeDetails> listNodeDetails; // stores m trees
	static int m;
	static BeamNode root;
	final static int depth_limit = 50;
	
	public static void init(String criteria, int noTrees, int k){
		m = noTrees;
		listNodeDetails = new ArrayList<BeamNodeDetails>();
		DecisionTree.init(criteria);
		CrossValidation.init(k);
		
		for(int i=0;i<m;i++){
		   listNodeDetails.add(new BeamNodeDetails());
		}
	}
	
	public static void displayBeamTree(BeamNode node){
		if(node == null)
			return;
		else{
			System.out.println("Class: "+node.sd.class_label+", Index: "+node.sd.feature_index+
					           ", Split Value: "+node.sd.split_value);
			displayBeamTree(node.left);
			displayBeamTree(node.right);
		}
	}
	
	/*
	 * Below method returns the overall gini of the tree required to compare the different trees
	 */
	public static float getOverallGini(BeamNode node){
		if(node == null || node.sd == null)
			return 0;
		
		return node.sd.gini + getOverallGini(node.left) + getOverallGini(node.right);
	}
	
	/*
	 * Below method compares the different trees by sorting on the overall gini
	 */
	public static void compareTrees(List<BeamNodeDetails> list_node_details){
		for(BeamNodeDetails node : list_node_details){
			BeamNode beam_node = node.beam_node;
			
			float total_gini = getOverallGini(beam_node);
			node.total_gini = total_gini;
		}
		
		Collections.sort(list_node_details, new Comparator<BeamNodeDetails>(){
			@Override
			public int compare(BeamNodeDetails n1, BeamNodeDetails n2){
				if(n1.total_gini == n2.total_gini)
					return 0;
				else if(n1.total_gini < n2.total_gini)
					   return -1;
				else return 1;
			}
		});
	}
	
	/*
	 * Below method determines the best split and packs the best split value, split index, feature index, majority class label
	 */
	public static void populateListResult(int index, List<String> split_result, List<FeatureLabel> listFeatLabel,
			                              List<String> result, int parentFeature, float st, float end, 
			                              Set<Integer> setValidRecords, int depth, List<SplitDetails> list_result){
	   float split_value = Float.MIN_VALUE;
	   String class_label = "-1";
	   float feature_left_st = Float.MIN_VALUE;
       float feature_left_end = Float.MAX_VALUE;
	   float feature_right_st = Float.MIN_VALUE;
	   float feature_right_end = Float.MAX_VALUE;
	   List<String> list_left_record_no = new ArrayList<String>();
	   List<String> list_right_record_no = new ArrayList<String>();
	   float gini = 1f;
	   
	   int feature_index = index;
 	   int split_val_index = Integer.parseInt(split_result.get(2));
 	   
 	   split_value = Float.parseFloat(split_result.get(1));
 	   feature_left_st = listFeatLabel.get(0).feature_val;
 	   
 	   if(split_val_index != -1){
 		   feature_left_end = listFeatLabel.get(split_val_index).feature_val;
 		   
 		   if(split_val_index < listFeatLabel.size()-1){
 			   feature_right_st = listFeatLabel.get(split_val_index+1).feature_val;
 			   feature_right_end = listFeatLabel.get(listFeatLabel.size()-1).feature_val;
 		   }
 		   
 		   list_left_record_no.clear();
 		   list_right_record_no.clear();
 		   
 		   for(FeatureLabel fl : listFeatLabel){
 			   if(fl.feature_val <= split_value)
 			      list_left_record_no.add(fl.record_no+"");
 			   else
 				  list_right_record_no.add(fl.record_no+"");
 			}
 		}
 		else{
 		   feature_left_end = listFeatLabel.get(listFeatLabel.size()-1).feature_val;
 		   feature_right_st = listFeatLabel.get(listFeatLabel.size()-1).feature_val;
 		   feature_right_end = listFeatLabel.get(listFeatLabel.size()-1).feature_val;
 		}
 	   
 	   split_val_index = Integer.parseInt(split_result.get(2));
 	   class_label = split_result.get(3);  // parent class label
 	   gini = Float.parseFloat(split_result.get(0));
	     
 	   List<String> list_class_dtls = new ArrayList<String>();
 	   
	   if(depth > DecisionTree.depth_limit){
		  list_class_dtls = DecisionTree.getClassLabel(parentFeature, st, end, setValidRecords);
	      class_label = list_class_dtls.get(0);
	   }
	   
	   SplitDetails sd = new SplitDetails(gini,
							              feature_index,
							              split_value,
							              class_label,
							              feature_left_st,
							              feature_left_end,
							              feature_right_st,
						                  feature_right_end,
						                  list_left_record_no,
						                  list_right_record_no);
	   
	   if(list_result.size() == m)
	     list_result.set(m-1, sd);
	   else
		 list_result.add(sd);
	   
	}
	
	public static void sortListResult(List<SplitDetails> list_result){
		Collections.sort(list_result, new Comparator<SplitDetails>(){
			
			@Override
			public int compare(SplitDetails sd1, SplitDetails sd2){
				if(sd1.gini == sd2.gini)
					return 0;
				else if(sd1.gini < sd2.gini)
					   return -1;
				else return 1;
			} 
		});
	}
	
	/*
	 * Below method searches for the node to attach the child node. Returns a TargetBeamNode object which ...
	 * contains the node to which the child should be attached and also the direction i.e. where the child should...
	 * be attached (left or right). 0 indicates left, 1 indicates right, -1 used to identify initial node 
	 */
	public static TargetBeamNode searchTargetNode(BeamNode beam_node){
		   Queue<BeamNode> queue = new LinkedList<BeamNode>();
		   Set<String> setLabels = DecisionTree.setLabels;
		   
		   if(beam_node.sd == null){
			   return new TargetBeamNode(beam_node, -1);
		   }
		   
		   queue.add(beam_node);
		   
		   while(!queue.isEmpty()){
			  BeamNode node = queue.remove();
			   
			  if(!setLabels.contains(node.sd.class_label)){
				  if(node.left == null)
					 return new TargetBeamNode(node, 0);
				  
				  if(node.right == null)
				     return new TargetBeamNode(node, 1);
				  
				  queue.add(node.left);
				  queue.add(node.right);
			   }
			}
		   
		   return null;
		}
	
	/*
	 * Below method attaches the child to the node in TargetBeamNode object returned by searchTargetNode
	 */
	public static boolean attachToTarget(BeamNode node, BeamNode target, BeamNode new_node, int direction){
		if(node == null)
			return false;
		
		if(node.hashCode() == target.hashCode()){
		   if(direction == 0)
			  node.left = new_node;
		   else if(direction == 1)	
		      node.right = new_node;
		
		   return true;
		 }
		else{
			boolean b1 = attachToTarget(node.left, target, new_node, direction);
			boolean b2 = attachToTarget(node.right, target, new_node, direction);
		
		    return b1 || b2;
		}
	}
	
	
	public static void populateVisitedFeatures(BeamNode node, Set<Integer> set){
		if(node == null)
		   return;
		else{
			set.add(node.sd.feature_index);
			populateVisitedFeatures(node.left, set);
			populateVisitedFeatures(node.right, set);
		}
		
	}
	
	/*
	 * Below method recomputes the visited features. This is necessary as there are different trees with each having..
	 * a different set of visited features.
	 */
	public static boolean recomputeVisited(BeamNode node, Set<Integer> setVisited, int parentFeature){
		if(node == null){
		   return false;	
		}
		else{
			if(node.sd.feature_index == parentFeature){
				setVisited.add(parentFeature);
				return true;
			}
			else{
				boolean b1 = recomputeVisited(node.left, setVisited, parentFeature);
				boolean b2 = recomputeVisited(node.right, setVisited, parentFeature);
				
				if(b1 || b2){
					setVisited.add(node.sd.feature_index);
					return true;
				}
			}
			
		  return false;	
		}
	}
	
	/*
	 * Below method returns the m best attributes
	 */
	public static List<SplitDetails> getBestSplit(HashMap<Integer, HashMap<Integer, String>> hashData, int parentFeature, 
                                            Set<Integer> setValidRecords, float st, float end, Set<Integer> setVisited, 
                                            int depth){
	   
	   List<SplitDetails> list_result = new ArrayList<SplitDetails>();
	   List<String> result = new ArrayList<String>();
	   
	   List<String> temp_result = new ArrayList<String>();
	   List<String> list_left_record_no = new ArrayList<String>();
	   List<String> list_right_record_no = new ArrayList<String>();
	   int records = DecisionTree.hashData.size();
	   float gini = 1f;
	   float split_value = Float.MIN_VALUE;
	   String class_label = "-1";
	   float feature_left_st = Float.MIN_VALUE;
	   float feature_left_end = Float.MAX_VALUE;
	   float feature_right_st = Float.MIN_VALUE;
	   float feature_right_end = Float.MAX_VALUE;
	   boolean flag = false;
	   int noFeatures = DecisionTree.noFeatures;
	   int m = CombinedBeamSearch.m;
		   
	   for(int i=0;i<noFeatures-1;i++){
	      if(!setVisited.contains(i)){
	    	flag = true;  
	    	List<FeatureLabel> listFeatLabel = new ArrayList<FeatureLabel>();
	    	
	    	for(int j=1;j<=records;j++){
	    	   if(st==Float.MIN_VALUE && end == Float.MAX_VALUE){
	    		  String label = hashData.get(j).get(noFeatures-1)+""; 
	    		  listFeatLabel.add(new FeatureLabel(j, Float.parseFloat(hashData.get(j).get(i)), label));
	    	   }
	    	   else{
	    		   float parentFeatVal = Float.parseFloat(hashData.get(j).get(parentFeature));
	    		   
	    		   if(parentFeatVal >= st && parentFeatVal <= end && setValidRecords.contains(j)){
	    			  String label = hashData.get(j).get(noFeatures-1)+""; 
	 	    		  listFeatLabel.add(new FeatureLabel(j, Float.parseFloat(hashData.get(j).get(i)), label));
	    		   }
	    	   }
	        }
	    	
	    	DecisionTree.sortFeatureLabel(listFeatLabel);
	    	
	    	temp_result = DecisionTree.getGini(listFeatLabel);   // gets gini for the given feature
	    	gini = Float.parseFloat(temp_result.get(0));
	    	
	    	if(list_result.size() < m && listFeatLabel.size() > 0){
	    		populateListResult(i, temp_result, listFeatLabel, result, parentFeature, st, end, setValidRecords, depth, list_result);
	    		sortListResult(list_result);
	    	}
	    	else{
	    	   if(listFeatLabel.size() > 0 && gini < list_result.get(m-1).gini){
	    		   populateListResult(i, temp_result, listFeatLabel, result, parentFeature, st, end, setValidRecords, depth, list_result);
	    		   sortListResult(list_result);
	    	   }
	    	}
	    	
	      }
	   
		}
		   
	   //System.out.println("Visited Index: "+feature_index+", Split Val: "+split_value+", Class: "+class_label);
	   List<String> list_class_dtls = new ArrayList<String>();
	   if(!flag){
		  list_class_dtls = DecisionTree.getClassLabel(parentFeature, st, end, setValidRecords);
		  class_label = list_class_dtls.get(0);
		  
		  SplitDetails sd = new SplitDetails(1f, parentFeature, split_value,         class_label, 
				                             feature_left_st,   feature_left_end,    feature_right_st, 
				                             feature_right_end, list_left_record_no, list_right_record_no);
		  
		  list_result.add(sd);
	   }
		   
	   return list_result;
	}
	
	
	
	public static void getBestTree(){
		List<SplitDetails> list_result = new ArrayList<SplitDetails>();
		float st = Float.MIN_VALUE;
		float end = Float.MAX_VALUE;
		int no_features = DecisionTree.noFeatures;
		int depth = -1;
		int count = 0;
		int parentFeature = -1;
		
		while(true){
		    if(count == m)
			   break;
		    
			depth++;
			if(depth > depth_limit)
				break;
			
			count = 0;
			
			List<BeamNodeDetails> temp_node_details = new ArrayList<BeamNodeDetails>();
			
			for(BeamNodeDetails node_details : listNodeDetails){
				BeamNode beam_node = node_details.beam_node;
				
				Set<Integer> setVisited = new LinkedHashSet<Integer>();  
				TargetBeamNode target_node = searchTargetNode(beam_node);
				Set<Integer> setValidRecords = new HashSet<Integer>();
				
				
				
				if(target_node == null || setVisited.size() == no_features-1){ // if all features are visited
					count++; 
					continue;
				}
					
			    BeamNode target = target_node.beam_node;
				int direction = target_node.direction;
				
				if(target.sd == null)
				   parentFeature = -1;
				else{
				   parentFeature = target.sd.feature_index;
				   recomputeVisited(beam_node, setVisited, parentFeature);
				}
				
				if(direction == 0){
					st = target.sd.feature_left_st;
					end = target.sd.feature_left_end;
					setValidRecords = DecisionTree.getValidRecords(target.sd.list_record_no).get(0);
				}
				else if(direction == 1){
					st = target.sd.feature_right_st;
					end = target.sd.feature_right_end;
					setValidRecords = DecisionTree.getValidRecords(target.sd.list_record_no).get(1);
				}
				
				
				list_result = getBestSplit(DecisionTree.hashData, parentFeature, setValidRecords, st, end, setVisited, depth);
				
				// Iterate loop for m best attributes
				for(int i=0;i<list_result.size();i++){ 
					BeamNode new_node = new BeamNode();
					
					SplitDetails sd = list_result.get(i);
					new_node.sd = sd;
					
					if(target.sd == null){
						beam_node = new_node;
					}
					else{
						attachToTarget(beam_node, target, new_node, direction);
					}
					
					BeamNode copy_node = new BeamNode(beam_node);
					temp_node_details.add(new BeamNodeDetails(copy_node, sd.feature_index, sd.class_label));
					
				}
				
			}
			
			// trees sorted according to overall gini. 'm' best trees will be at the start of the list
			compareTrees(temp_node_details);
			
			// iterate m times and store the m best trees in listNodeDetails
			for(int i=0;i<m;i++){
				
				if(i==temp_node_details.size()){
					break;
				}
				listNodeDetails.set(i, temp_node_details.get(i));
			}
			
		}
	}
	
}

class BeamNodeDetails{
	BeamNode beam_node;
	Set<Integer> setVisited;
	float total_gini;
	int cur_feature;
	String class_label;
	
	BeamNodeDetails(){
		beam_node = new BeamNode();
		setVisited = new LinkedHashSet<Integer>();
	}
	
	BeamNodeDetails(BeamNode node, int cur_feature, String label){
		beam_node = node;
		this.cur_feature = cur_feature;
		class_label = label;
	}
}

class BeamNode{
	BeamNode left;
	BeamNode right;
	SplitDetails sd;
	
	BeamNode(){}
	
	BeamNode(BeamNode left, BeamNode right, SplitDetails sd){
		this.left = left;
		this.right = right;
		this.sd = sd;
	}
	
	BeamNode(BeamNode beam_node){
		this(beam_node.left, beam_node.right, beam_node.sd);
	}
}

class SplitDetails{
	float gini;
	int feature_index;
	float split_value;
	String class_label;
	float feature_left_st;
	float feature_left_end;
	float feature_right_st;
	float feature_right_end;
	Set<Integer> setVisited;
	List<String> list_record_no;
	
	SplitDetails(float gini,
				 int feature_index,
				 float split_value,
				 String class_label,
				 float feature_left_st,
				 float feature_left_end,
				 float feature_right_st,
				 float feature_right_end,
				 List<String> list_left_record_no,
				 List<String> list_right_record_no){
		this.gini = gini;
		this.feature_index = feature_index;
		this.split_value = split_value;
		this.class_label = class_label;
		this.feature_left_st = feature_left_st;
		this.feature_left_end = feature_left_end;
		this.feature_right_st = feature_right_st;
		this.feature_right_end = feature_right_end;
		
		setVisited = new LinkedHashSet<Integer>();
		list_record_no = new ArrayList<String>();
		
		this.list_record_no.addAll(list_left_record_no);
		this.list_record_no.add("");
		this.list_record_no.addAll(list_right_record_no);
	}
	
}

class TargetBeamNode{
	BeamNode beam_node;
	int direction;
	
	TargetBeamNode(BeamNode node, int direction){
		this.beam_node = node;
		this.direction = direction;
	}
}
