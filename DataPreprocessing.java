import com.esotericsoftware.yamlbeans.YamlReader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DataPreprocessing {
	static String curDir;
	static Set<String> setCities;
	static String team1;
	static String team2;
	static int team1Rank;
	static int team2Rank;
	static int team1Total;
	static int team2Total;
	static int team1Wicket;
	static int team2Wicket;
	static int tossWinner;
	static int winner;
	static boolean team1Home;
	static boolean team2Home;
	static HashMap<String, List<String>> hash;
	static HashMap<String, List<String>> hashRankings;
	static HashMap<String,String> hashCityCountry;
	
	public static void init(){
		curDir = System.getProperty("user.dir");
		setCities = new HashSet<String>();
		hash = new HashMap<String, List<String>>();
		hashRankings = new HashMap<String, List<String>>();
		hashCityCountry = new HashMap<String, String>();
		team1 = "";
		team2 = "";
		winner = -1;
		tossWinner = -1;
	}
	
	public static void reset(){
		team1 = "";
		team2 = "";
		team1Total = 0;
		team2Total = 0;
		team1Wicket = 0;
		team2Wicket = 0;
		winner = -1;
	}
	
	public static void getTeamRanks(String dateYear){
		//System.out.println("Date Year: "+dateYear);
		List<String> list_teams = hashRankings.get(dateYear);
		
		for(int i=0;i<list_teams.size();i++){
			//System.out.println("i: "+i);
			//System.out.println("List Team: "+list_teams.get(i));
			
			if(list_teams.get(i).equals(team1))
				team1Rank = i+1;
			else if(list_teams.get(i).equals(team2))
				team2Rank = i+1;
		}
	
	}
	
	public static void getHomeAwayStatus(String city){
		String country = ""; 
		
		if(hashCityCountry.containsKey(city))
		   country = hashCityCountry.get(city);
		
		if(team1.equals(country)){
			team1Home = true;
		    team2Home = false;
		}
		else if(team2.equals(country)){
			team2Home = true;
		    team1Home = false;
		}
		else{
			team1Home = false;
		    team2Home = false;
		}
	}
	
	public static int getInfoMap(Map map){
	   Map infoMap = (Map)map.get("info");
	   
	   List<String> listDates = (List<String>)infoMap.get("dates");
	   String dateYear = listDates.get(0).substring(0,4);
	   
	   String city = (String)infoMap.get("city");
	   
	   if(city!=null && !city.equals("")){
		   setCities.add(city);
	   }
	   
	   Map outcomeMap = (Map)infoMap.get("outcome");
	   if(outcomeMap.containsKey("result")){
		   return -1;
	   }
		   
	   Map tossMap = (Map)infoMap.get("toss");
	   String decision = (String)tossMap.get("decision");
	   
	   if(decision.equals("field")){
		   team2 = (String)tossMap.get("winner");
	   }
	   else{
		   team1 = (String)tossMap.get("winner");
	   }
	   
	   if(team1.isEmpty())
		   tossWinner = 2;
	   else
		   tossWinner = 1;
	   
	  List<String> listTeams = (List<String>)infoMap.get("teams");
	  if(!team1.equals("")){
		  if(listTeams.get(0).equals(team1))
			  team2 = listTeams.get(1);
		  else
			  team2 = listTeams.get(0);
	  }
	  else{
		  if(listTeams.get(0).equals(team2))
			  team1 = listTeams.get(1);
		  else
			  team1 = listTeams.get(0);
	  }
	  
	  getTeamRanks(dateYear);
	  getHomeAwayStatus(city);
	  
	  String winTeam = outcomeMap.get("winner")+"";
	  
	  if(team1.equals(winTeam))
		  winner = 1;
	  else
		  winner = 2;
	  
	  return 0;
	}
	
	public static void getInnings(Map innMap){
		List<Map> listInnMap = (List<Map>) innMap.get("innings");
		Map innMap1 = (Map) listInnMap.get(0).get("1st innings");
		Map innMap2 = (Map) listInnMap.get(1).get("2nd innings");
		
		List<Map> listDelMaps = (List<Map>)innMap1.get("deliveries");
		for(Map delMap : listDelMaps){
			Set<String> key_set = (Set<String>)delMap.keySet();
			
			for(String key : key_set){
			  Map ballMap = (Map)delMap.get(key);
			  Map runMap = (Map)ballMap.get("runs");
			  
			  team1Total+= Integer.parseInt(runMap.get("total")+"");
			
			  if(ballMap.containsKey("wicket"))
				team1Wicket++;
			}
		}
		
		listDelMaps.clear();
		listDelMaps = (List<Map>)innMap2.get("deliveries");
		one: for(Map delMap : listDelMaps){
			Set<String> key_set = (Set<String>)delMap.keySet();
			
			for(String key : key_set){
			  Map ballMap = (Map)delMap.get(key);
			  Map runMap = (Map)ballMap.get("runs");
			  
			  team2Total+= Integer.parseInt(runMap.get("total")+"");
			
			  if(ballMap.containsKey("wicket"))
				team2Wicket++;
			  
			  if(key.equals("24.6"))
				  break one;
			}
		}
		
		//System.out.println("Team1 Total: "+team1Total+", Wicket: "+team1Wicket);
		//System.out.println("Team2 Total: "+team2Total+", Wicket: "+team2Wicket);
	}
	
	public static void populateListData(List<String> listData, String name){
		if(tossWinner == 1){       
			listData.add("1");   // Team 1 won toss
			listData.add("0");   // Team 2 lost toss
		}
		else{
			listData.add("0");   // Team 1 lost toss
			listData.add("1");   // Team 2 won toss
		}
		
		if(team1Home){
		  listData.add("1");   // Team 1 Home
		  listData.add("0");   // Team 2 Home
		  listData.add("0");   // Team 1 Away
		  listData.add("1");   // Team 2 Away
		}
		else if(team2Home){
		   listData.add("0");
		   listData.add("1");
		   listData.add("1");
		   listData.add("0");
		}
		else{
			listData.add("0");
			listData.add("0");
			listData.add("1");
			listData.add("1");
		}
			
		if(team1Total >= 300)   
			listData.add("1");
		else
			listData.add("0");
		
		if(team1Total < 200)
			listData.add("1");
		else
			listData.add("0");
			
		if(team1Total >= 200 && team1Total < 300)
			listData.add("1");
		else
			listData.add("0");
		
		if(team2Total >= 200)
			listData.add("1");
		else
			listData.add("0");
		
		if(team2Total >= 150 && team2Total < 200)
			listData.add("1");
		else
			listData.add("0");
		
		if(team2Total >= 100 && team2Total < 150)
			listData.add("1");
		else
			listData.add("0");
		
		if(team2Total < 100)
			listData.add("1");
		else
			listData.add("0");
		
		if(team2Wicket <= 2)
		   listData.add("1");
		else
		   listData.add("0");
		
		if(team2Wicket > 2 &&  team2Wicket <= 4)
		   listData.add("1");
		else
		   listData.add("0");
		
		if(team2Wicket > 4 && team2Wicket <= 6)
		   listData.add("1");
		else
		   listData.add("0");
		
		if(team2Wicket > 6)
		   listData.add("1");
		else
		   listData.add("0");
			
		
		int rank_diff = team1Rank - team2Rank;
		if(rank_diff > 0){
		   for(int i=1;i<=4;i++)
			 listData.add("0");
		  
		   for(int i=1;i<=4;i++){
			  if(rank_diff > 4 && i==4)
				  listData.add("1");
			  else{
			    if(i==rank_diff)
				  listData.add("1");
			    else
				  listData.add("0");
			  }
		   }
		 }
		else{
		  rank_diff = Math.abs(rank_diff);
		  
		  for(int i=1;i<=4;i++){
			if(rank_diff > 4 && i==4)
				  listData.add("1");
			else{
			   if(i==rank_diff)
				  listData.add("1");
			   else
				  listData.add("0");
			}
		  }
		  
		  for(int i=1;i<=4;i++)
			 listData.add("0");
		  
		}
		  
		if(winner == 1){
		   listData.add("1");    // Team 1 won
		   listData.add("0");    // Team 2 lost
		}
		else{
		   listData.add("0");    // Team 1 lost
		   listData.add("1");    // Team 2 won
		}
	}
	
	public static void parseYaml(File file) throws Exception{
		YamlReader yreader = new YamlReader(new FileReader(file));
		Map map = (Map)yreader.read();
		List<String> listData = new ArrayList<String>();
		
		//System.out.println("Filename: "+file.getName());
		
		int status = getInfoMap(map);
		if(status == 0){
		  getInnings(map);
		  populateListData(listData, file.getName());
		  hash.put(file.getName(), listData);
		}
	}
	
	public static void readFiles(File dataDir) throws Exception{
		File[] files = dataDir.listFiles();
		
		for(int i=0;i<files.length;i++){
			File file = files[i];
			
			if(file.getName().endsWith("yaml"))
			  parseYaml(file);
			
			reset();
		}
	}
	
	public static void readRankingsData(String path, String sep) throws Exception{
		BufferedReader br = new BufferedReader(new FileReader(path));
		String s = "";
		
		String[] temp = br.readLine().split(",");
		String year = temp[1];
		List<String> listTeams = new ArrayList<String>();
		
		while((s=br.readLine())!=null){
			temp = s.split(sep);
			
			if(temp[0].equals("Year")){
				hashRankings.put(year, listTeams);
				year = temp[1];
				listTeams = new ArrayList<String>();
			}
			else{
				listTeams.add(temp[1]);	
			}
		}
		
		hashRankings.put(year, listTeams);
		
		//System.out.println(hashRankings);
		
	}
	
	public static void readGroundsData(String path, String sep, boolean hasHeader) throws Exception{
		BufferedReader br = new BufferedReader(new FileReader(path));
		String s = "";
		
		if(hasHeader)
		  s = br.readLine();
		
		while((s=br.readLine())!=null){
		   String[] temp = s.split(",");
		   
		   String country = temp[3].split("!")[0].trim();
		   
		   String[] temp_city = temp[2].split("~");
		   
		   for(int i=0;i<temp_city.length;i++){
			   String city = temp_city[i].trim();
			   
			   if(!hashCityCountry.containsKey(city)){
				   hashCityCountry.put(city, country);   
			   }
		   }
		}
	}
	
	public static void writeDataCSV() throws Exception{
	   BufferedWriter bw = new BufferedWriter(new FileWriter("cricket.csv"));
	   bw.write("Filename, 1_Team1 Won Toss, 2_Team2 Won Toss, 3_Team1 Home, 4_Team2 Home, 5_Team1 Away, 6_Team2 Away, "
	   		+ " 7_Team1 > 300, 8_Team1 < 200, 9_Team1 b/w 200 and 300, 10_Team2(till 25th over) >= 200, 11_Team2(till 25th over) b/w 200 and 150, "
	   		+ " 12_Team2(till 25th over) b/w 150 and 100, 13_Team2(till 25th over) < 100, 14_Team2 Wickets(till 25th over) <=2,"
	   		+ " 15_Team2 Wickets(till 25th over) b/w 2 and 4, 16_Team2 Wickets(till 25th over) b/w 4 and 6, "
			+ " 17_Team2 Wickets(till 25th over) > 6, 18_Team1 - Team2(Rank Diff): 1, 19_Team1 - Team2(Rank Diff): 2, "
			+ " 20_Team1 - Team2(Rank Diff): 3, 21_Team1 - Team2(Rank Diff): >=4, "
	   		+ " 22_Team2 - Team1(Rank Diff): 1, 23_Team2 - Team1(Rank Diff): 2, 24_Team2 - Team1(Rank Diff): 3, "
	   		+ " 25_Team2 - Team1(Rank Diff): >=4, 26_Team1 Won, Team 2 Won\n");	
	   
	   for(String s : hash.keySet()){
		  bw.write(s+", ");	
		  List<String> temp = hash.get(s);
		  
		  for(int i=0;i<temp.size();i++){
			  if(i!=temp.size()-1)
			     bw.write(temp.get(i)+",");
			  else
				 bw.write(temp.get(i));
		  }
		  
		  bw.write("\n");
	   }
	   
	   bw.close();
	}
	
    public static void main(String[] args) throws Exception{
    	init();
    	
    	String rankings_file = "ICC_Rankings.csv";
    	String sep = ",";
    	String path = curDir+"\\"+rankings_file;
    	readRankingsData(path, sep);
    	
    	String grounds_file = "ODI_grounds.csv";
    	path = curDir+"\\"+grounds_file;
    	boolean hasHeader = true;
    	readGroundsData(path, sep, hasHeader);
    	
    	String dir = "ODI";
    	path = curDir+"\\"+dir;

    	File dataDir = new File(path);
    	readFiles(dataDir);

    	writeDataCSV();
    	//System.out.println(hash.size());
    	//System.out.println("Team 1 Total: "+team1Total);
    	//System.out.println("Team 2 Total: "+team2Total);
    }
}
