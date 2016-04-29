import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class CrossValidation {
  static int k;
  static HashMap<Integer, String> hash;
  static String curDir;
  static int total;
  static int correct;
  
  public static void init(int folds){
	  curDir = System.getProperty("user.dir");
	  k = folds;
	  hash = new HashMap<Integer, String>();
  }
  
  public static void readDataset(String filename, boolean hasHeader) throws IOException{
	  BufferedReader br = new BufferedReader(new FileReader(filename));
	  String s="";
	  int records = 0;
	   
	  if(hasHeader)
	     s = br.readLine(); // skipping header
	   
	  while((s=br.readLine())!=null){
		  records++;
		  hash.put(records, s);
	   }
	   
	   br.close();
  }
  
  public static void generatePartitions(int fold, int records, String ext, boolean roc_YN) throws IOException{
	  int fold_records = records/k;
	  
	  int fold_end = fold_records*fold;
	  int fold_st = fold_end - fold_records + 1;
	  FileWriter fw_test = null;
	  
	  if(!roc_YN)
	     fw_test = new FileWriter("test"+fold+ext);
	  else
		 fw_test = new FileWriter("test_ROC"+fold+ext);  
	  
	  for(int i=fold_st;i<=fold_end;i++){
		  fw_test.write(hash.get(i)+"\n");
	  }
	  fw_test.close();
	  
	  FileWriter fw_train = null;
	  
	  if(!roc_YN)
	     fw_train = new FileWriter("train"+fold+ext);
	  else
		 fw_train = new FileWriter("train_ROC"+fold+ext); 
	  
	  for(int i=1;i<=records;i++){
		  if(i >= fold_st && i<= fold_end)
			  continue;
		  else
			  fw_train.write(hash.get(i)+"\n");
	   }
	  fw_train.close();
	  
  }
  
}
