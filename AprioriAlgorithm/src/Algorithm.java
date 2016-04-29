import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by jay on 3/29/16.
 */
public class Algorithm {

    private HashMap<Integer, List<Integer>> sparseMap;
    private double supportThreshold;
    private HashMap<String, Integer> allCandidatesWithId;
    private String candidateGenerationType;
    private HashMap<String, Integer> freqItemsetCount;

    public Algorithm(SparseMatrix sparseMatrix, String candidateGenerationType, double supportThreshold) {
        DataSet dataSet = sparseMatrix.dataSet;
        this.sparseMap = sparseMatrix.getIdVsIsPresentMap();
        this.supportThreshold = supportThreshold;
        this.allCandidatesWithId = dataSet.getDistinctItemsets();
        this.candidateGenerationType = candidateGenerationType;
        this.freqItemsetCount = new HashMap<>();
    }

    public HashMap<String, Integer> getFreqItemsetCount() {
        return freqItemsetCount;
    }

    public List<Set<String>> run() {

        int totalFrequentSize = 0;
        int k = 1;
        int totalCandidateSize = 0;

        List<String> maximalItemsets = new ArrayList<>();
        List<String> closedItemsets = new ArrayList<>();
        List<Set<String>> allItemsets = new ArrayList<>();

        System.out.println("************ k = " + k + " ****************");
        System.out.println("Candidates = " + this.allCandidatesWithId.size());
        totalCandidateSize += this.allCandidatesWithId.size();
        List<String> freqItemsetsOfSizeOne = getFrequentItemsetsOfSize1(this.allCandidatesWithId.keySet(), k);
        System.out.println("Frequent = " + freqItemsetsOfSizeOne.size());
        totalFrequentSize += freqItemsetsOfSizeOne.size();

        ++k;
        Set<String> candidatesItemsetsFor2 = getCandidateItemsetsForSize2(freqItemsetsOfSizeOne, maximalItemsets, closedItemsets);
        System.out.println("************ k = " + k + " ****************");
        System.out.println("Candidates = " + candidatesItemsetsFor2.size());
        totalCandidateSize += candidatesItemsetsFor2.size();
        List<Set<String>> freqItemsetsHighK = getFrequentItemsets(candidatesItemsetsFor2, k);

        if (freqItemsetsHighK != null) {
            System.out.println("Frequent = " + freqItemsetsHighK.size());
            totalFrequentSize += freqItemsetsHighK.size();
            allItemsets.addAll(freqItemsetsHighK);

            while (true) {
                ++k;
                Set<String> candidateItemsets = getCandidateItemsets(freqItemsetsHighK, freqItemsetsOfSizeOne, maximalItemsets, closedItemsets, k);
                System.out.println("************ k = " + k + " ****************");

                System.out.println("Candidates = " + candidateItemsets.size());
                totalCandidateSize += candidateItemsets.size();

                List<Set<String>> tempItemsets = getFrequentItemsets(candidateItemsets, k);

                if (tempItemsets == null || tempItemsets.size() == 0) {
                    break;
                } else {
                    freqItemsetsHighK.clear();
                    freqItemsetsHighK.addAll(tempItemsets);
                    System.out.println("Frequent = " + freqItemsetsHighK.size());
                    totalFrequentSize += freqItemsetsHighK.size();
                    allItemsets.addAll(freqItemsetsHighK);
                }
            }
        }
        System.out.println("********************************");
        System.out.println("Total Maximal Frequent Itemsets = " + maximalItemsets.size());
        System.out.println("Total Closed Frequent Itemsets = " + closedItemsets.size());
        System.out.println("Total Number of Frequent Itemsets = " + totalFrequentSize);
        System.out.println("Total candidate set considered = " + totalCandidateSize);
//        System.out.println("Actual Frequent Itemsets used for Rule Generation = " + freqItemsetsHighK.size());

        return allItemsets;
    }

    private Set<String> getCandidateItemsetsForSize2(List<String> freqItemsetsOfSizeOne, List<String> maximalItemsets, List<String> closedItemsets) {
        Set<String> size2 = new HashSet<>();
        TreeSet<String> sortTree = new TreeSet<>();
        List<String> allSuperSets = new ArrayList<>();

        for (String outerString : freqItemsetsOfSizeOne) {
            allSuperSets.clear();
            List<String> superSets = freqItemsetsOfSizeOne.stream()
                    .filter(innerString -> outerString.compareToIgnoreCase(innerString) < 0)
                    .map(innerString -> String.join(",", outerString, innerString))
                    .collect(Collectors.toList());
            size2.addAll(superSets);

            allSuperSets.addAll(getAllSubsets(outerString, sortTree, this.allCandidatesWithId.keySet()));

            if (isMaximalFrequent(allSuperSets, 2)) {
                maximalItemsets.add(outerString);
            }

            if (isClosedFrequent(allSuperSets, outerString, 2)) {
                closedItemsets.add(outerString);
            }

        }
        return size2;
    }

    private List<String> getAllSubsets(String outerString, TreeSet<String> sortTree, Set<String> allKeys) {
        List<String> allSubSets = new ArrayList<>();
        sortTree.clear();

        List<String> allStrings = Arrays.asList(outerString.split(","));
        sortTree.addAll(allStrings);

        allKeys.stream().filter(allKey -> !allStrings.contains(allKey)).forEach(allKey -> {
            sortTree.add(allKey);
            String joinedString = String.join(",", sortTree);
            allSubSets.add(joinedString);
            sortTree.remove(allKey);
        });

        return allSubSets;
    }

    private boolean isClosedFrequent(List<String> superSets, String itemset, int k) {

        int supportCount = getSupportCount(itemset, k - 1);

        boolean closed = superSets.stream()
                .allMatch(string -> getSupportCount(string, k) != supportCount);

        return closed && (supportCount >= this.supportThreshold * this.sparseMap.size());
    }

    private boolean isMaximalFrequent(List<String> superSets, int k) {

        return superSets.stream()
                .allMatch(string -> getSupportCount(string, k) <= this.supportThreshold * this.sparseMap.size());
    }

    private List<String> getFrequentItemsetsOfSize1(Set<String> allCandidates, int k) {

        return allCandidates.
                stream()
                .filter(string -> getSupportCount(string, k) >= this.supportThreshold * this.sparseMap.size())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private Set<String> getCandidateItemsets(List<Set<String>> freqItemsets, List<String> freqItemsetsOfSizeOne, List<String> maximalItemsets, List<String> closedItemsets, int k) {

        if (this.candidateGenerationType.equals("1")) {
            return candidateKInto1(freqItemsets, freqItemsetsOfSizeOne, maximalItemsets, closedItemsets, k);
        } else {
            return candidateKIntoKMinus1(freqItemsets, maximalItemsets, closedItemsets, k);
        }
    }

    private Set<String> candidateKIntoKMinus1(List<Set<String>> freqItemsets, List<String> maximalItemsets, List<String> closedItemsets, int k) {
        Set<String> candidateItemsetsK = new HashSet<>();
        List<String> superSets = new ArrayList<>();

        TreeSet<String> sortTree = new TreeSet<>();
        List<String> allSuperSets = new ArrayList<>();

        for (Set<String> freqItemset : freqItemsets) {

            String freqItemsetsPatternOutside = String.join(",", freqItemset);
            String[] allCandidatesOutside = freqItemsetsPatternOutside.split(",");
            String totalMinusLastOutside = Arrays.stream(allCandidatesOutside)
                    .limit(allCandidatesOutside.length - 1)
                    .collect(Collectors.joining(","));

            String outside = allCandidatesOutside[allCandidatesOutside.length - 1];

            superSets.clear();

            for (Set<String> itemset : freqItemsets) {
                allSuperSets.clear();

                String freqItemsetsPatternInside = String.join(",", itemset);
                String[] allCandidatesInside = freqItemsetsPatternInside.split(",");
                String totalMinusLastInside = Arrays.stream(allCandidatesInside)
                        .limit(allCandidatesInside.length - 1)
                        .collect(Collectors.joining(","));

                String inside = allCandidatesInside[allCandidatesOutside.length - 1];


                if (totalMinusLastOutside.equalsIgnoreCase(totalMinusLastInside) && !outside.equalsIgnoreCase(inside)) {
                    if (inside.compareToIgnoreCase(outside) < 0) {
                        superSets.add(String.join(",", totalMinusLastInside, inside, outside));
                    } else {
                        superSets.add(String.join(",", totalMinusLastInside, outside, inside));
                    }
                }
            }

            candidateItemsetsK.addAll(superSets);
            allSuperSets.addAll(getAllSubsets(freqItemsetsPatternOutside, sortTree, this.allCandidatesWithId.keySet()));

            if (isMaximalFrequent(allSuperSets, k)) {
                maximalItemsets.add(freqItemsetsPatternOutside);
            }

            if (isClosedFrequent(allSuperSets, freqItemsetsPatternOutside, k)) {
                closedItemsets.add(freqItemsetsPatternOutside);
            }
        }

        return candidateItemsetsK;
    }

    private Set<String> candidateKInto1(List<Set<String>> freqItemsetsOfSizeK, List<String> freqItemsetsOfSize1, List<String> maximalItemsets, List<String> closedItemsets, int k) {

        Set<String> candidatesItemsetsK = new HashSet<>();
        TreeSet<String> sortTree = new TreeSet<>();
        List<String> allSuperSets = new ArrayList<>();

        for (Set<String> itemset : freqItemsetsOfSizeK) {
            allSuperSets.clear();

            String freqKItemsets = String.join(",", itemset);
            String[] allValues = freqKItemsets.split(",");
            String lastString = allValues[allValues.length - 1];

            List<String> superSets = freqItemsetsOfSize1.stream()
                    .filter(freq1Itemset -> lastString.compareToIgnoreCase(freq1Itemset) < 0)
                    .map(freq1Itemset -> String.join(",", freqKItemsets, freq1Itemset))
                    .collect(Collectors.toList());

            candidatesItemsetsK.addAll(superSets);

            allSuperSets.addAll(getAllSubsets(freqKItemsets, sortTree, this.allCandidatesWithId.keySet()));

            if (isMaximalFrequent(allSuperSets, k)) {
                maximalItemsets.add(freqKItemsets);
            }

            if (isClosedFrequent(allSuperSets, freqKItemsets, k)) {
                closedItemsets.add(freqKItemsets);
            }
        }

        return candidatesItemsetsK;
    }

    private List<Set<String>> getFrequentItemsets(Set<String> allCandidates, int k) {

        if (!allCandidates.isEmpty()) {
            Function<String, Set<String>> convertToSet = string -> {
                Set<String> sortedSet = new TreeSet<>();
                sortedSet.add(string);
                return sortedSet;
            };

            return allCandidates.
                    stream()
                    .filter(string -> getSupportCount(string, k) >= this.supportThreshold * this.sparseMap.size())
                    .map(convertToSet)
                    .collect(Collectors.toCollection(ArrayList::new));
        } else {
            return null;
        }
    }

    public int getSupportCount(String pattern, int k) {
        if (this.freqItemsetCount.containsKey(pattern)) {
            return this.freqItemsetCount.get(pattern);
        }

        String[] individualItemsets = pattern.split(",");
        int count = 0;
        int internalCount;


        for (Map.Entry<Integer, List<Integer>> transactionsWithId : this.sparseMap.entrySet()) {
            List<Integer> transaction = transactionsWithId.getValue();
            internalCount = 0;

            for (String itemset : individualItemsets) {
                if (transaction.contains(this.allCandidatesWithId.get(itemset))) {
                    internalCount++;
                }
            }
            if (internalCount == k) {
                count++;
            }
        }
        if (count >= this.supportThreshold * this.sparseMap.size()) {
            addToWordCountMap(pattern, count);
        }
        return count;
    }

    private void addToWordCountMap(String string, int count) {
        this.freqItemsetCount.put(string, count);
    }


}
