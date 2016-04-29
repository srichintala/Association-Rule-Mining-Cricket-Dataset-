/**
 * Created by jay on 4/3/16.
 */
public class Rule{

    private String source;
    private String end;
    private int sourceCount;
    private int endCount;
    private int sourceEndTogether;

    public int getSourceEndTogether() {
        return sourceEndTogether;
    }

    public void setSourceEndTogether(int sourceEndTogether) {
        this.sourceEndTogether = sourceEndTogether;
    }

    public int getSourceCount() {
        return sourceCount;
    }

    public void setSourceCount(int sourceCount) {
        this.sourceCount = sourceCount;
    }

    public int getEndCount() {
        return endCount;
    }

    public void setEndCount(int endCount) {
        this.endCount = endCount;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public double getConfidence() {
        return (this.sourceEndTogether) / (double) this.sourceCount;
    }

    public String itemset() {
        return source + "," + end;
    }

    public double getLift() {
        return getConfidence() / this.endCount;
    }
}
