import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class KMeansSegmentation {

    private Utility utility = new Utility();
    private BufferedImage originalImage;
    private int[] histogram;
    private String color = "gray";
    private int k = 2;
    private final int MAXITERATIONS = 100;
    private final int INSUFFIENTMEANCHANGE = 1;
    private ArrayList<Integer> clusterObject = new ArrayList<>();
    private ArrayList<Integer> clusterBackground = new ArrayList<>();

    @FunctionalInterface
    interface FuncInterface extends OverHeadInterface.FuncInterface {
        // An abstract function
        void function(BufferedImage newImage, int x, int y);
    }

    public KMeansSegmentation.FuncInterface fobj = (BufferedImage newImage, int x, int y) -> {
        int pixelColor = utility.getSingleColor(originalImage.getRGB(x, y), color);

        if (clusterObject.contains(pixelColor)) {
            int newColorRGB = utility.setSingleColorRBG(255, color); // white // TODO wrong value // TODO change color? is black or white object?
            newImage.setRGB(x, y, newColorRGB);
        }

        else {
            int newColorRGB = utility.setSingleColorRBG(0, color); // black // TODO wrong value
            newImage.setRGB(x, y, newColorRGB);
        }
    };

    public KMeansSegmentation.FuncInterface getFuncInterface () {
        return fobj;
    }


    // NOTE currently hardcoded to only support 1 dimensional kmeans from histograms
    public KMeansSegmentation(BufferedImage originalImage, int[] histogram) {
        this.originalImage = originalImage;
        this.histogram = histogram;
    }

    // NOTE currently this only supports single color images
    public BufferedImage kMeansSegmentation () throws NullPointerException {
        kMeans();

        BufferedImage segmentedImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), originalImage.getType());

        ParallelMatrix parallelMatrix = new ParallelMatrix();
        parallelMatrix.doInParallel(segmentedImage, getFuncInterface());
        return segmentedImage;
    }

    private void kMeans() {
        boolean finish = false;
        int iteration = 0;

        int[] initialMeans = initializeKPoints();
        double meanObject = initialMeans[0];
        double meanBackground = initialMeans[1];

        double prevMeanObject;
        double prevMeanBackground;

        // Add in new data, one at a time, recalculating centroids with each new one.
        while(!finish) {
            prevMeanObject = meanObject;
            prevMeanBackground = meanBackground;

            assignToCluster(clusterObject, clusterBackground, meanObject, meanBackground);
            meanObject = reCalcClusterMean(clusterObject);
            meanBackground = reCalcClusterMean(clusterBackground);

            //Calculates total distance between new and old Centroids
            double meanChange = distance(prevMeanObject, meanObject) + distance(prevMeanBackground, meanBackground);

            iteration++;

            if (iteration > MAXITERATIONS || meanChange < INSUFFIENTMEANCHANGE) {
                finish = true;
            }
        }
    }

    private void assignToCluster(ArrayList<Integer> clusterObject,  ArrayList<Integer> clusterBackground, double meanObject, double meanBackground) {
        // first clear the clusters
        clusterObject.clear();
        clusterBackground.clear();

        // calc distance from each point to each k mean and add said point to the closer cluster
        for (int i = 0; i < histogram.length; i++) {
            double distanceObject = distance(i, meanObject);
            double distanceBackground = distance(i, meanBackground);

            if (distanceObject > distanceBackground) {
                clusterObject.add(i);
            } else {
                clusterBackground.add(i);
            }
        }
    }

    private double reCalcClusterMean(ArrayList<Integer> cluster) {
        double mean = 0;
        int pointsUsed = 0;
        for (int i = 0; i < cluster.size(); i++) {
            int pixelValue = cluster.get(i);
            mean += pixelValue * histogram[pixelValue]; // multiply the current pixel value by the amount of times it is in the image
            pointsUsed += histogram[pixelValue];
        }
        mean /= pointsUsed;
        return mean;
    }

    private double distance(double p, double centroid) {
        return Math.sqrt(Math.pow((centroid - p), 2));
    }

    // the first means are located in the 2 densest areas. Hopefully this will result in less needed iterations // TODO ask about this approach or a better option
    private int[] initializeKPoints() {
        //firstMean is the pixel with the highest histogram value
        int firstMean = 0;
        for (int i =0; i < histogram.length; i++) {
            if (histogram[i] > histogram[firstMean]) {
                firstMean = i; //histogram[i];
            }
        }

        // secondMean is the existing pixel that is the furthest from the firstMean. Modified k means++ initialization
        int secondMean = 0;
        double maxDistance = 0;
        for (int i = 0; i < histogram.length; i++) {
            if (i != firstMean && histogram[i] > 0) {
                double distance = distance(i, firstMean);
                if (distance > maxDistance) {
                    maxDistance = distance;
                    secondMean = i;
                }
            }
        }

        return new int[]{firstMean, secondMean};
    }

}