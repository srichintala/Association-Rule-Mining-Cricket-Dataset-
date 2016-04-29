import java.util.*;

/**
 * Created by jay on 3/28/16.
 */
public class DataSet {

    private int totalAttributes;
    private List<String> attributeNames;
    private List<String> attributeTypes;
    private Set<String> classLabels;
    private HashMap<String, Set<String>> distinctValuesPerColumn;
    private String pathToFile;
    private List<String> instances;
    private Double confidenceThreshold;
    private Double supportThreshold;
    private String ruleEvaluation;
    private String classLabelType;

    public DataSet() {
        this.distinctValuesPerColumn = new HashMap<>();
        this.classLabels = new HashSet<>();
        this.instances = new ArrayList<>();
    }

    public Double getConfidenceThreshold() {
        return confidenceThreshold;
    }

    public void setConfidenceThreshold(Double confidenceThreshold) {
        this.confidenceThreshold = confidenceThreshold;
    }

    public Double getSupportThreshold() {
        return supportThreshold;
    }

    public void setSupportThreshold(Double supportThreshold) {
        this.supportThreshold = supportThreshold;
    }

    public List<String> getInstances() {
        return instances;
    }

    public void setInstances(List<String> instances) {
        this.instances = instances;
    }

    public int getTotalAttributes() {
        return totalAttributes;
    }

    public String getPathToFile() {
        return pathToFile;
    }

    public void setPathToFile(String pathToFile) {
        this.pathToFile = pathToFile;
    }

    public void setTotalAttributes(int totalAttributes) {
        this.totalAttributes = totalAttributes;
    }

    public List<String> getAttributeNames() {
        return attributeNames;
    }

    public void setAttributeNames(List<String> attributeNames) {
        this.attributeNames = attributeNames;
    }

    public List<String> getAttributeTypes() {
        return attributeTypes;
    }

    public void setAttributeTypes(List<String> attributeTypes) {
        this.attributeTypes = attributeTypes;
    }

    public Set<String> getClassLabels() {
        return classLabels;
    }

    public void setClassLabels(Set<String> classLabels) {
        this.classLabels = classLabels;
    }

    public HashMap<String, Set<String>> getDistinctValuesPerColumn() {
        return distinctValuesPerColumn;
    }

    public void setDistinctValuesPerColumn(HashMap<String, Set<String>> distinctValuesPerColumn) {
        this.distinctValuesPerColumn = distinctValuesPerColumn;
    }

    public HashMap<String, Integer> getDistinctItemsets() {
        HashMap<String, Integer> itemsetsVsId = new HashMap<>();

        int id = 1;

        for (String attributeName : attributeNames) {
            String type = this.attributeTypes.get(id - 1);
            if (type.equalsIgnoreCase("categorical")) {
                Set<String> distinctValues = this.getDistinctValuesPerColumn().get(attributeName);
                for (String distinctValue : distinctValues) {
                    itemsetsVsId.put(attributeName + "_" + distinctValue, id);
                    id++;
                }
            } else if (type.equalsIgnoreCase("binary")) {
                itemsetsVsId.put(attributeName, id);
                id++;
            }
        }

        for (String classLabel : this.classLabels) {
            itemsetsVsId.put(classLabel, id);
            id++;
        }

        return itemsetsVsId;
    }

    public int size() {
        return this.instances.size();
    }

    public String getRuleEvaluation() {
        return ruleEvaluation;
    }

    public void setRuleEvaluation(String ruleEvaluation) {
        this.ruleEvaluation = ruleEvaluation;
    }

    public void setClassLabelType(String classLabelType) {
        this.classLabelType = classLabelType;
    }

    public String getClassLabelType() {
        return classLabelType;
    }
}
