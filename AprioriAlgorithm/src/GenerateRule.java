import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by jay on 4/3/16.
 */
public class GenerateRule {
    private List<Set<String>> frequentItemsets;
    private double confidenceThreshold;
    private String ruleEvaluation;
    private HashMap<String, Integer> freqItemsetCount;
    private int bruteForceCount;
    private int pruningCount;

    public GenerateRule(List<Set<String>> frequentItemsets, HashMap<String, Integer> freqItemsetCount, double confidenceThreshold, String ruleEvaluation) {
        this.frequentItemsets = frequentItemsets;
        this.freqItemsetCount = freqItemsetCount;
        this.confidenceThreshold = confidenceThreshold;
        this.ruleEvaluation = ruleEvaluation;
        this.pruningCount = 0;
    }

    public int getBruteForceCount() {
        for (Set<String> frequentItemset : frequentItemsets) {
            int k = String.join(",", frequentItemset).split(",").length;
            this.bruteForceCount += Math.pow(2, k) - 2;
        }
        return bruteForceCount;
    }

    public int getPruningCount() {
        return pruningCount;
    }

    public List<Rule> getAllRules() {
        List<Rule> rulesList = new ArrayList<>();

        for (Set<String> frequentItemset : this.frequentItemsets) {
            String itemsetsWithComma = String.join(",", frequentItemset);

            if (this.ruleEvaluation.equals("Lift")) {
                rulesList.addAll(getAllLiftRules(itemsetsWithComma));
            } else {
                List<Rule> highConfRulesWithOneConsequent = getHighConfidenceRulesOfOneConsequent(itemsetsWithComma);
                int length = itemsetsWithComma.split(",").length;
                this.pruningCount += length;

                if (length == 2) {
                    rulesList.addAll(highConfRulesWithOneConsequent);
                } else {
                    List<Rule> highConfRules = generateMoreRules(highConfRulesWithOneConsequent, itemsetsWithComma);
                    rulesList.addAll(highConfRules);
                }
            }
        }

        return rulesList;
    }

    private List<Rule> getAllLiftRules(String itemsetsWithComma) {

        Set<String> allValuesSet = new HashSet<>(Arrays.asList(itemsetsWithComma.split(",")));
        Set<String> consequentSet = new HashSet<>();
        List<Rule> allRules = new ArrayList<>();

        List<String> allOneConsequents = Arrays.asList(itemsetsWithComma.split(","));
        List<String> allSuperSets = getSuperSets(allOneConsequents, itemsetsWithComma);

        getRules(itemsetsWithComma, allRules, allValuesSet, consequentSet, allSuperSets);

        return allRules;
    }


    private List<Rule> generateMoreRules(List<Rule> highConfRules, String itemsetsWithComma) {

        List<Rule> mergedRules = new ArrayList<>();
        Set<String> allValuesSet = new HashSet<>(Arrays.asList(itemsetsWithComma.split(",")));
        Set<String> consequentSet = new HashSet<>();


        List<String> allOneConsequents = highConfRules.stream()
                .map(Rule::getEnd)
                .collect(Collectors.toList());

//        this.pruningCount += (Math.pow(2, allOneConsequents.size()) - 2);

        List<String> allSuperSets = getSuperSets(allOneConsequents, itemsetsWithComma);
        this.pruningCount += allSuperSets.size() - highConfRules.size();

        getRules(itemsetsWithComma, mergedRules, allValuesSet, consequentSet, allSuperSets);

        return mergedRules;
    }

    private void getRules(String itemsetsWithComma, List<Rule> mergedRules, Set<String> allValuesSet, Set<String> consequentSet, List<String> allSuperSets) {
        for (String consequent : allSuperSets) {
            consequentSet.clear();
            consequentSet.addAll(Arrays.asList(consequent.split(",")));

            Rule rule = new Rule();
            rule.setEnd(consequent);
            rule.setEndCount(this.freqItemsetCount.get(consequent));

            String source = allValuesSet.stream()
                    .filter(s -> !consequentSet.contains(s))
                    .sorted()
                    .collect(Collectors.joining(","));

            rule.setSource(source);
            rule.setSourceCount(this.freqItemsetCount.get(source));
            rule.setSourceEndTogether(this.freqItemsetCount.get(itemsetsWithComma));

            if (this.ruleEvaluation.equals("Confidence")) {
                if (rule.getConfidence() > this.confidenceThreshold) {
                    mergedRules.add(rule);
                }
            } else {
                mergedRules.add(rule);
            }
        }
    }

    private List<String> getSuperSets(List<String> allOneConsequents, String itemsetsWithComma) {
        List<String> superSets = new ArrayList<>();

        int totalNumberOfSets = (int) Math.pow(2, allOneConsequents.size());

        for (int i = 0; i < totalNumberOfSets; i++) {
            List<String> internalList = new ArrayList<>();

            for (int j = 0; j < allOneConsequents.size(); j++) {
                if ((i & (1 << j)) > 0) {
                    internalList.add(allOneConsequents.get(j));
                }
            }
            String setString = String.join(",", internalList);

            int length = setString.split(",").length;
            if (!setString.isEmpty() && (length != itemsetsWithComma.split(",").length)) {
                superSets.add(setString);
            }
        }

        return superSets;

    }

    private List<Rule> getHighConfidenceRulesOfOneConsequent(String itemsetsWithComma) {
        List<Rule> rules = new ArrayList<>();

        String[] candidates = itemsetsWithComma.split(",");

        for (String candidate : candidates) {

            Rule rule = new Rule();
            rule.setEnd(candidate);
            rule.setEndCount(this.freqItemsetCount.get(candidate));

            String source = Arrays.stream(candidates)
                    .filter(string -> !string.equalsIgnoreCase(candidate))
                    .collect(Collectors.joining(","));
            rule.setSource(source);
            rule.setSourceCount(this.freqItemsetCount.get(source));
            rule.setSourceEndTogether(this.freqItemsetCount.get(itemsetsWithComma));

            if (rule.getConfidence() > this.confidenceThreshold) {
                rules.add(rule);

            }
        }

//        if (rules.size() > 0) {
//            this.pruningCount += (Math.pow(2, rules.size()) - 2);
//        }
        return rules;
    }

    public double getSavings() {
        return (this.bruteForceCount - this.pruningCount) * 100 / (double) this.bruteForceCount;
    }
}
