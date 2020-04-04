import java.awt.image.BufferedImage;
import java.util.*;

public class ThresholdSegmentation {

    private Utility utility = new Utility();
    private BufferedImage originalImage;
    private int[] histogram;
    private String color = "gray";
    private int threshold;

    @FunctionalInterface
    interface FuncInterface extends OverHeadInterface.FuncInterface {
        // An abstract function
        void function(BufferedImage newImage, int x, int y);
    }

    public ThresholdSegmentation.FuncInterface fobj = (BufferedImage newImage, int x, int y) -> {
        int pixelColor = utility.getSingleColor(originalImage.getRGB(x, y), color);

        if (pixelColor >= threshold) {
            int newColorRGB = utility.setSingleColorRBG(255, color); // white // TODO wrong value // TODO change color?
            newImage.setRGB(x, y, newColorRGB);
        }

        else {
            int newColorRGB = utility.setSingleColorRBG(0, color); //black // TODO wrong value
            newImage.setRGB(x, y, newColorRGB);
        }
    };

    public ThresholdSegmentation.FuncInterface getFuncInterface () {
        return fobj;
    }


    public ThresholdSegmentation(BufferedImage originalImage, int[] histogram) {
        this.originalImage = originalImage;
        this.histogram = histogram;
    }

    // NOTE currently this only supports single color images
    public BufferedImage thresholdSegmentation () throws NullPointerException {
        threshold = calcThreshold();

        BufferedImage segmentedImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), originalImage.getType());

        ParallelMatrix parallelMatrix = new ParallelMatrix();
        parallelMatrix.doInParallel(segmentedImage, getFuncInterface());
        return segmentedImage;
    }

    private int calcThreshold() {
        int numberOfPixelsInImage = originalImage.getWidth() * originalImage.getHeight();
        Map<Integer, Double> thresholdVarianceMap = new HashMap<>();


        // TODO can this be parallelized without race conditions?
        for (int i = 0; i < histogram.length; i++) { // NOTE histogram.length should be 256
            double thresholdVariance = getThresholdVariance(i, numberOfPixelsInImage); // TODO lots of NANs here BUG
            thresholdVarianceMap.put(i, thresholdVariance);
        }



        Map.Entry<Integer, Double> min = null;
        for (Map.Entry<Integer, Double> entry : thresholdVarianceMap.entrySet()) {
            if (min == null || min.getValue() > entry.getValue()) {
                min = entry;
            }
        }
        int bestThreshold = min.getKey(); // TODO might throw an error
//        Double bestThreshold = Collections.min(thresholdVarianceMap.values());
        System.out.println("bestThreshold: " + bestThreshold);
        return bestThreshold;

//        for (int x = 0; x < originalImage.getWidth(); x++) {
//            for (int y = 0; y < originalImage.getHeight(); y++) {
//                int pixelColor = utility.getSingleColor(originalImage.getRGB(x, y), color);
//                int thresholdVariance = getThresholdVariance(pixelColor, numberOfPixelsInImage);
//
//            }
//        }

    }

    private double getThresholdVariance(int pixelColor, int numberOfPixelsInImage) {
        return (getVarianceOfObject(pixelColor, numberOfPixelsInImage) * getProbOfPixelValueInObject(pixelColor, numberOfPixelsInImage))
                + (getVarianceOfBackground(pixelColor, numberOfPixelsInImage) * getProbOfPixelValueInBackground(pixelColor, numberOfPixelsInImage));
    }

    private int getProbOfPixelValueInImage(int pixelColor, int numberOfPixelsInImage) {
        return histogram[pixelColor] / numberOfPixelsInImage;
    }

    private double getProbOfPixelValueInObject(int currentThreshold, int numberOfPixelsInImage) {
        double probOfPixelValueInObject = 0;
        for (int i = 0; i <= currentThreshold; i++) {
            probOfPixelValueInObject += getProbOfPixelValueInImage(i, numberOfPixelsInImage);
        }
        return probOfPixelValueInObject;
    }

    private double getMeanOfObject(int currentThreshold, int numberOfPixelsInImage) {
        double meanOfObject = 0;
        for (int i = 0; i <= currentThreshold; i++) {
            meanOfObject += (i * getProbOfPixelValueInImage(i, numberOfPixelsInImage));
        }
        meanOfObject /= getProbOfPixelValueInObject(currentThreshold, numberOfPixelsInImage);
        return meanOfObject;
    }

    private double getVarianceOfObject(int currentThreshold, int numberOfPixelsInImage) {
        double varianceOfObject = 0;
        for (int i = 0; i <= currentThreshold; i++) {
            varianceOfObject += Math.pow((i - getMeanOfObject(currentThreshold, numberOfPixelsInImage)), 2) * getProbOfPixelValueInImage(i, numberOfPixelsInImage);
        }
        varianceOfObject /= getProbOfPixelValueInObject(currentThreshold, numberOfPixelsInImage);
        return varianceOfObject;
    }

    private double getProbOfPixelValueInBackground(int currentThreshold, int numberOfPixelsInImage) {
        double probOfPixelValueInBackground = 0;
        for (int i = currentThreshold + 1; i < 255; i++) {
            probOfPixelValueInBackground += getProbOfPixelValueInImage(i, numberOfPixelsInImage);
        }
        return probOfPixelValueInBackground;
    }

    private double getMeanOfBackground(int currentThreshold, int numberOfPixelsInImage) {
        double meanOfBackground = 0;
        for (int i = currentThreshold + 1; i < 255; i++) {
            meanOfBackground += (i * getProbOfPixelValueInImage(i, numberOfPixelsInImage));
        }
        meanOfBackground /= getProbOfPixelValueInBackground(currentThreshold, numberOfPixelsInImage);
        return meanOfBackground;

    }

    private double getVarianceOfBackground(int currentThreshold, int numberOfPixelsInImage) {
        double varianceOfBackground = 0;
        for (int i = currentThreshold + 1; i < 255; i++) {
            varianceOfBackground += Math.pow((i - getMeanOfBackground(currentThreshold, numberOfPixelsInImage)), 2) * getProbOfPixelValueInImage(i, numberOfPixelsInImage);
        }
        varianceOfBackground /= getProbOfPixelValueInBackground(currentThreshold, numberOfPixelsInImage);
        return varianceOfBackground;
    }
}
