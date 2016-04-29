import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by jay on 3/28/16.
 */
public class Driver {

    public static void main(String[] args) {
        DataSet dataSet = new DataSet();

        readMetaData(dataSet, "metadata");

        readData(dataSet);

        SparseMatrix sparseMatrix = new SparseMatrix(dataSet);
        sparseMatrix.makeMatrix();

        String candidateTypeGeneration = getCandidateGenerationType();

        Algorithm algorithm = new Algorithm(sparseMatrix, candidateTypeGeneration, dataSet.getSupportThreshold());
        List<Set<String>> frequentItemsets = algorithm.run();

        GenerateRule generateRule = new GenerateRule(frequentItemsets, algorithm.getFreqItemsetCount(), dataSet.getConfidenceThreshold(), dataSet.getRuleEvaluation());

        System.out.println("********************* Rules ******************** ");
        List<Rule> allRules = generateRule.getAllRules();
        System.out.println("Total Rules = " + allRules.size());
        System.out.println("Brute Force Count = " + generateRule.getBruteForceCount());
        System.out.println("Prune Count= " + generateRule.getPruningCount());
        System.out.println("savings = " + generateRule.getSavings() + " % ");

        int size = sparseMatrix.getIdVsIsPresentMap().size();
//        if (dataSet.getRuleEvaluation().equals("Lift")) {
//            printTopRules(allRules, "2", sparseMatrix.getIdVsIsPresentMap().size());
//        } else {
//            printTopRules(allRules, "1", sparseMatrix.getIdVsIsPresentMap().size());
//        }

        Predicate<Rule> classLabelPredicate = rule -> {
            String ruleString = rule.getSource() + "," + rule.getEnd();
            String[] values = ruleString.split(",");

            boolean zeroCheck = Arrays.stream(values).anyMatch(s -> s.trim().equals("0"));
            boolean team1gt300 = Arrays.stream(values).anyMatch(s -> s.trim().equals("Team1_gt_300"));
            boolean oneCheck = Arrays.stream(values).anyMatch(s -> s.trim().equals("1"));
            boolean team1lt200 = Arrays.stream(values).anyMatch(s -> s.trim().equals("Team1_lt_200"));

            return ((zeroCheck && team1gt300) || (oneCheck && team1lt200));
        };

        List<Rule> filteredRules = allRules.stream()
                .filter(classLabelPredicate)
                .filter(rule -> rule.getSourceEndTogether() / (double) size < 0.05)
                .filter(rule -> rule.getLift() * size >= 1.4)
                .filter(rule -> rule.getConfidence() > 0.6 && rule.getConfidence() < 0.8)
                .collect(Collectors.toList());


        printInterestingRules(filteredRules, size);

    }

    private static void printInterestingRules(List<Rule> filteredRules, int size) {
        printFilteredRules(filteredRules, "Team1_gt_300", size);
        printFilteredRules(filteredRules, "Team1_lt_200", size);
    }

    private static void printFilteredRules(List<Rule> filteredRules, String filter, int size) {

        System.out.println("******************************************************************");
        Predicate<Rule> moreThan300 = (rule) -> {

            String ruleString = rule.getSource() + "," + rule.getEnd();
            String[] values = ruleString.split(",");

            return Arrays.stream(values).anyMatch(s -> s.trim().equals(filter));
        };

        filteredRules.stream()
                .filter(moreThan300)
                .sorted(getConfidenceComparator())
                .forEach(rule -> System.out.println(rule.getSource() + " -> " + rule.getEnd() + " Confidence " + rule.getConfidence() + " Support " + rule.getSourceEndTogether() + " Lift " + rule.getLift() * size));

    }

    private static Comparator<Rule> getConfidenceComparator() {
        return (r1, r2) -> {

            if (r1.getConfidence() > r2.getConfidence()) {
                return -1;
            } else if (r1.getConfidence() < r2.getConfidence()) {
                return 1;
            } else {
                return 0;
            }
        };
    }

    private static void printTopRules(List<Rule> allRules, String ruleEvaluation, int size) {

        Comparator<Rule> liftComparator = (r1, r2) -> {

            if (r1.getLift() * size > r2.getLift() * size) {
                return -1;
            } else if (r1.getLift() * size < r2.getLift() * size) {
                return 1;
            } else {
                return 0;
            }
        };

        List<Rule> print = new ArrayList<>();

        if (ruleEvaluation.equalsIgnoreCase("1")) {
            print.addAll(allRules.stream()
                    .sorted(getConfidenceComparator())
                    .limit(10)
                    .collect(Collectors.toList()));

            for (Rule rule : print) {
                System.out.println(rule.getSource() + " \t\t\t -> \t\t\t" + rule.getEnd());
            }

        } else if (ruleEvaluation.equalsIgnoreCase("2")) {
            print.addAll(allRules.stream()
                    .sorted(liftComparator)
                    .limit(10)
                    .collect(Collectors.toList()));

            for (Rule rule : print) {
                System.out.println(rule.getSource() + " \t\t\t -> \t\t\t" + rule.getEnd());
            }
        }

    }

    private static String getCandidateGenerationType() {
        System.out.println("Please select a number for candidate Generation Process");
        System.out.println("1. F(k-1) * F(1)  2. F(k-1) * F(k-1)");
        String line = null;
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in))) {
            line = bufferedReader.readLine();

            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (line.equals("1") || line.equalsIgnoreCase("F(k-1) * F(1)")) {
            return "1";
        } else if (line.equals("2") || line.equalsIgnoreCase("F(k-1) * F(k-1)")) {
            return "2";
        } else {
            System.out.println("Please provide correct input in your next attempt");
            System.exit(1);
        }
        return null;
    }

    private static void readData(DataSet dataSet) {

        Charset charset = Charset.forName("US-ASCII");
        try (BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(dataSet.getPathToFile()), charset)) {
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                line = line.toLowerCase();
                String[] values = line.split(",");
                dataSet.getInstances().add(line);
                findDistinctValuesPerColumn(values, dataSet);

            }
            bufferedReader.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private static void findDistinctValuesPerColumn(String[] values, DataSet dataSet) {
        HashMap<String, Set<String>> distinctValuesPerAttribute = dataSet.getDistinctValuesPerColumn();
        Set<String> classLabels = dataSet.getClassLabels();

        for (int i = 0; i < values.length; i++) {
            if (i == values.length - 1) {
                classLabels.add(values[i]);
            } else {
                String attributeName = dataSet.getAttributeNames().get(i);

                if (dataSet.getAttributeTypes().get(i).equalsIgnoreCase("categorical")) {
                    insertInMap(distinctValuesPerAttribute, attributeName, values[i]);
                }
            }
        }
    }


    private static void insertInMap(HashMap<String, Set<String>> distinctValuesPerAttribute, String key, String value) {

        Set<String> distinctValues;
        if (distinctValuesPerAttribute.containsKey(key)) {
            distinctValues = distinctValuesPerAttribute.get(key);
            distinctValues.add(value);

        } else {
            distinctValues = new HashSet<>();
            distinctValues.add(value);
            distinctValuesPerAttribute.put(key, distinctValues);
        }
    }

    private static void readMetaData(DataSet dataSet, String path) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));

            dataSet.setPathToFile(bufferedReader.readLine());

            dataSet.setTotalAttributes(Integer.parseInt(bufferedReader.readLine()));

            dataSet.setAttributeNames(Arrays.asList(bufferedReader.readLine().split(",")));

            dataSet.setAttributeTypes(Arrays.asList(bufferedReader.readLine().split(",")));

            dataSet.setClassLabelType(bufferedReader.readLine());

            dataSet.setSupportThreshold(Double.parseDouble(bufferedReader.readLine()));
            dataSet.setConfidenceThreshold(Double.parseDouble(bufferedReader.readLine()));
            dataSet.setRuleEvaluation(bufferedReader.readLine());

            bufferedReader.close();

        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }


}