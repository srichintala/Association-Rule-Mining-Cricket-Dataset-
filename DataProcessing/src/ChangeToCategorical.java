import java.io.*;

/**
 * Created by jay on 4/28/16.
 */
public class ChangeToCategorical {

    public static void main(String[] args) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader("cricket_teamRank_diff.csv"));
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("newDataset.csv"));

            String line;
            String[] newDataSet;

            while ((line = bufferedReader.readLine()) != null) {
                String[] values = line.split(",");
                newDataSet = new String[7];

                // team toss
                if (values[0].equals("1")) {  // index 0-1
                    newDataSet[0] = "1";
                } else {
                    newDataSet[0] = "2";
                }

                // team venue
                if (values[2].equals("1")) { // index 2,3,4
                    newDataSet[1] = "Home";
                } else if (values[3].equals("1")) {
                    newDataSet[1] = "Away";
                } else {
                    newDataSet[1] = "Neutral";
                }

                //team1 batting runs;
                if (values[5].equals("1")) { // index 5,6,7
                    newDataSet[2] = "gt300";
                } else if (values[6].equals("1")) {
                    newDataSet[2] = "lt200";
                } else {
                    newDataSet[2] = "bt200_250";
                }

                //team2_batting runs 25 overs
                if (values[8].equals("1")) { // index 8,9,10,11
                    newDataSet[3] = "gte200";
                } else if (values[9].equals("1")) {
                    newDataSet[3] = "bt150_200";
                } else if (values[10].equals("1")) {
                    newDataSet[3] = "bt100_150";
                } else {
                    newDataSet[3] = "lt100";
                }

                //team2 wickets 25 overs
                if (values[12].equals("1")) { // index 12,13,14,15
                    newDataSet[4] = "lte2";
                } else if (values[13].equals("1")) {
                    newDataSet[4] = "bt2_4";
                } else if (values[14].equals("1")) {
                    newDataSet[4] = "bt4_6";
                } else {
                    newDataSet[4] = "gt6";
                }

                //Rank diff
                if (values[16].equals("1")) { // index 16,17
                    newDataSet[5] = "1";
                } else {
                    newDataSet[5] = "2";
                }

                newDataSet[6] = values[18];

                bufferedWriter.write(String.join(",", newDataSet));
                bufferedWriter.newLine();
            }

            bufferedReader.close();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
