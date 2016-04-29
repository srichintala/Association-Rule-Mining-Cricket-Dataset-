import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by jay on 3/28/16.
 */
public class SparseMatrix {

    private HashMap<String, Integer> itemsetVsId;
    public DataSet dataSet;
    private HashMap<Integer, List<Integer>> idVsIsPresentMap;

    public SparseMatrix(DataSet dataSet) {
        this.dataSet = dataSet;
        this.itemsetVsId = new HashMap<>();
        this.idVsIsPresentMap = new HashMap<>();
    }


    public HashMap<Integer, List<Integer>> getIdVsIsPresentMap() {
        return idVsIsPresentMap;
    }

    public void setIdVsIsPresentMap(HashMap<Integer, List<Integer>> idVsIsPresentMap) {
        this.idVsIsPresentMap = idVsIsPresentMap;
    }

    public HashMap<String, Integer> getItemsetVsId() {
        return itemsetVsId;
    }

    public void setItemsetVsId(HashMap<String, Integer> itemsetVsId) {
        this.itemsetVsId = itemsetVsId;
    }

    public void makeMatrix() {
        this.itemsetVsId = this.dataSet.getDistinctItemsets();

        for (int i = 0; i < this.dataSet.getInstances().size(); i++) {
            String instanceLine = this.dataSet.getInstances().get(i);

            String[] splitValues = instanceLine.split(",");

            for (int valueIndex = 0; valueIndex < splitValues.length; valueIndex++) {
                String itemset;
                int itemSetId = 0;
                String type;

                if (valueIndex != splitValues.length - 1) {
                    type = this.dataSet.getAttributeTypes().get(valueIndex);

                    if (type.equalsIgnoreCase("categorical")) {
                        itemset = this.dataSet.getAttributeNames().get(valueIndex) + "_" + splitValues[valueIndex];
                        itemSetId = this.itemsetVsId.get(itemset);
                        insertInMap(i, itemSetId);
                    } else if (type.equalsIgnoreCase("binary")) {
                        if (splitValues[valueIndex].equals(String.valueOf(1))) {
                            itemset = this.dataSet.getAttributeNames().get(valueIndex);
                            itemSetId = this.itemsetVsId.get(itemset);
                            insertInMap(i, itemSetId);
                        }
                    }


                } else {
                    itemset = splitValues[valueIndex];
                    itemSetId = this.itemsetVsId.get(itemset);

                    insertInMap(i, itemSetId);
                }

            }
        }
        System.out.println("SparseMatrix Created");
    }

    private void insertInMap(int key, int itemSetId) {
        List<Integer> isPresentIds;

        if (this.idVsIsPresentMap.containsKey(key)) {
            isPresentIds = this.idVsIsPresentMap.get(key);
            isPresentIds.add(itemSetId);
        } else {
            isPresentIds = new ArrayList<>();
            isPresentIds.add(itemSetId);
            this.idVsIsPresentMap.put(key, isPresentIds);
        }
    }
}
