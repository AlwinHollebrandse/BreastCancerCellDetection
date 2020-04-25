import java.util.Arrays;

public class CellObject {
    
    private double[] features;
    private String actualCellLabel;
    private String predictedCellLabel;
    private double distance;

    public CellObject(double[] features, String actualCellLabel, String predictedCellLabel, double distance) {
        this.features = features;
        this.actualCellLabel = actualCellLabel;
        this.predictedCellLabel = predictedCellLabel;
        this.distance = distance;
    }

    public double[] getFeatures() {
        return features;
    }

    public void setFeatures(double[] features) {
        this.features = features;
    }

    public String getActualCellLabel() {
        return actualCellLabel;
    }

    public void setActualCellLabel(String actualCellLabel) {
        this.actualCellLabel = actualCellLabel;
    }

    public String getPredictedCellLabel() {
        return predictedCellLabel;
    }

    public void setPredictedCellLabel(String predictedCellLabel) {
        this.predictedCellLabel = predictedCellLabel;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    @Override
    public String toString() {
        return "CellObject{" +
                "features=" + Arrays.toString(features) +
                ", actualCellLabel='" + actualCellLabel + '\'' +
                ", predictedCellLabel='" + predictedCellLabel + '\'' +
                ", distance=" + distance +
                '}';
    }
}
