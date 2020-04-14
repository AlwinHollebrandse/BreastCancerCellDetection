import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class Filter {

    private Utility utility = new Utility();
    private BufferedImage originalImage;
    private String filterType;  // TODO add enums
    private int filterWidth;
    private int filterHeight;
    private int[] weights;
    private double scalar;
    private int[][] tempPrePorportionalNormImageValues;

    @FunctionalInterface
    interface FuncInterface extends OverHeadInterface.FuncInterface {
        // An abstract function
        void function(BufferedImage newImage, Semaphore semaphore, int x, int y);
    }

    public Filter.FuncInterface fobj = (BufferedImage newImage, Semaphore semaphore, int x, int y) -> {
        ArrayList<Integer> neighborRGBValueArray = utility.getNeighborValues(originalImage, (x + filterWidth/2), (y + filterHeight/2), filterHeight, filterWidth);

        int newPixelValue = -1;
        if ("linear".equalsIgnoreCase(filterType)) {
            newPixelValue = calcAvgRGB(neighborRGBValueArray, weights, scalar);
            tempPrePorportionalNormImageValues[x][y] = newPixelValue;
        }

        else if("median".equalsIgnoreCase(filterType)) {
            newPixelValue = calcMedian(neighborRGBValueArray, weights);
        }

        newImage.setRGB(x, y, newPixelValue); // if newPixelValue== -1, there is an error // TODO throw an error?
    };

    public Filter.FuncInterface getFuncInterface () {
        return fobj;
    }



    public Filter(BufferedImage originalImage, String filterType, int filterWidth, int filterHeight, int[] weights) {
        this.originalImage = originalImage;
        this.filterType = filterType;
        this.filterWidth = filterWidth;
        this.filterHeight = filterHeight;
        this.weights = weights;
    }


    // NOTE currently this only works for RGB (which includes black and white values, as those have rgb values, provided they are there)
    // NOTE crops the image border that does not fit in the filter convolution
    // NOTE assumes after accounting for weights, that the pixel color is still normalized TODO normalize after
    public BufferedImage filter () throws NullPointerException {
        // Parameter checking
        // If there was no weights array specified, then use weights of 1.
        if (weights == null) {
            weights = setDefaultWeights();
        }

        scalar = setScalar(weights);

        if (weights.length != filterHeight * filterWidth) {
            throw new NullPointerException("weights array was not the size of the filter");
        }

        // this method only works if there is a border to the filter, or else it is a simple pixel operation function
        if (filterHeight <= 2 || filterWidth <= 2) {
            throw new NullPointerException("filter height and width must be greater than 0");
        }

        // this methods requires a clear center pixel in the filter, so both the filter height and width need to be odd
        if ((filterHeight & 1) == 0 || (filterWidth & 1) == 0) {
            throw new NullPointerException("filter height and width must be odd numbers");
        }
        int newImageWidth = originalImage.getWidth() - ((filterWidth/2) * 2);
        int newImageHeight = originalImage.getHeight() - ((filterHeight/2) * 2);

        tempPrePorportionalNormImageValues = new int[newImageWidth][newImageHeight];

//        originalImage = new BufferedImage(4, 4, originalImage.getType());

        BufferedImage filterImage = new BufferedImage(newImageWidth, newImageHeight, originalImage.getType());

        ParallelMatrix parallelMatrix = new ParallelMatrix();
        parallelMatrix.doInParallel(filterImage, getFuncInterface());

        int[] minMax = getOldImageMinAndMax();
        int oldMin = minMax[0];
        int oldMax = minMax[1];

        return normalizeFilterImage(filterImage, oldMin, oldMax);
//        return filterImage;
    }

    private BufferedImage normalizeFilterImage(BufferedImage filterImage, int oldMin, int oldMax) {
        BufferedImage normalizedFilterImage = new BufferedImage(filterImage.getWidth(), filterImage.getHeight(), originalImage.getType());
        for (int x = 0; x < normalizedFilterImage.getWidth(); x++) {
            for (int y = 0; y < normalizedFilterImage.getHeight(); y++) {
                int oldPixelValue = utility.getSingleColor(tempPrePorportionalNormImageValues[x][y], "gray");
                int newPixelValue = utility.normalizeColorIntPorportional(oldPixelValue, oldMin, oldMax);
                normalizedFilterImage.setRGB(x, y, utility.setSingleColorRBG(newPixelValue, "gray"));
            }
        }
        return normalizedFilterImage;
    }

    private int[] getOldImageMinAndMax () {
        int min = 999;
        int max = -1;
        for (int x = 0; x < originalImage.getWidth(); x++) { // NOTE technically this includes the crops, which it shouldnt
            for (int y = 0; y < originalImage.getHeight(); y++) {
                int pixelValue = utility.getSingleColor(originalImage.getRGB(x, y), "gray");
                if (pixelValue < min) {
                    min = pixelValue;
                }
                if (pixelValue > max) {
                    max = pixelValue;
                }
            }
        }
        return new int[] {min, max};
    }

    private int[] setDefaultWeights() {
        int filterSize = filterHeight * filterWidth;
        weights = new int[filterSize];
        for (int i = 0; i < filterSize; i++) {
            weights[i] = 1;
        }
        return weights;
    }

    private double setScalar(int[] weights) {
        double absoluteSum = 0;
        for (int i = 0; i < weights.length; i++) {
            absoluteSum += Math.abs(weights[i]);
        }
        return 1/absoluteSum;
    }

    public int calcMedian (ArrayList<Integer> list, int[] weights) throws NullPointerException {
        if (list.size() != weights.length) {
            throw new NullPointerException("weights array was not the size of the filter");
        }

        //create and fill out a weighted medina list, where each pixel gets replicated by the associated weight value number of times. Median of medians is then used and the median is found.
        ArrayList<Integer> weightedMedianList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < weights[i]; j++) {
                Color c = new Color(list.get(i));
                int gray = (int) (c.getRed() * 0.299) + (int) (c.getGreen() * 0.587) + (int) (c.getBlue() * 0.114);
//                Color newColor = new Color(gray, gray, gray, c.getAlpha());
                weightedMedianList.add(gray);
            }
        }

        // TODO speed test
//        Collections.sort(weightedMedianList);
//        int medianValue = weightedMedianList.get(weightedMedianList.size() / 2);

        MedianOfMedians medianOfMedians = new MedianOfMedians();
        int medianValue = medianOfMedians.findMedian(weightedMedianList,(weightedMedianList.size())/2 + 1,0,weightedMedianList.size() - 1);

        Utility utility = new Utility();
        return utility.setSingleColorRBG(medianValue, "gray");
    }


    // does the above but for red, green, and blue at once
    public static int calcAvgRGB (ArrayList<Integer> list, int[] weights, double scalar) throws NullPointerException {
        if (list.size() != weights.length) {
            throw new NullPointerException("weights array was not the size of the filter");
        }

        double redAvg = 0;
        double greenAvg = 0;
        double blueAvg = 0;
        double alphaAvg = 0;
        for (int i = 0; i < list.size(); i++) {
            // add the respective RGB element to the correct color avg
            Color c = new Color(list.get(i));
            redAvg += c.getRed() * weights[i]; // red element * weight of pixel
            greenAvg += c.getGreen() * weights[i]; // green element * weight of pixel
            blueAvg += c.getBlue() * weights[i]; // blue element * weight of pixel
            alphaAvg += c.getAlpha() * weights[i];
        }

        redAvg *= scalar;
        greenAvg *= scalar;
        blueAvg *= scalar;
        alphaAvg *= scalar;

        Utility utility = new Utility();
        int finalRedAvg = utility.normalizeColorInt((int)redAvg);
        int finalGreenAvg = utility.normalizeColorInt((int)greenAvg);
        int finalBlueAvg = utility.normalizeColorInt((int)blueAvg);
        int finalAlphaAvg = utility.normalizeColorInt((int)alphaAvg);

        //combine each of the RGB elements into a single int
        Color newColor = new Color(finalRedAvg, finalGreenAvg, finalBlueAvg, finalAlphaAvg);
        return newColor.getRGB();
    }

}
