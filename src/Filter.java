import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;

public class Filter {

    private BufferedImage originalImage;
    private String filterType;  // TODO add enums
    private int filterWidth;
    private int filterHeight;
    private int[] weights;
    private double scalar;

    @FunctionalInterface
    interface FuncInterface extends OverHeadInterface.FuncInterface {
        // An abstract function
        void function(BufferedImage newImage, int x, int y);
    }

    public Filter.FuncInterface fobj = (BufferedImage newImage, int x, int y) -> {
        ArrayList<Integer> neighborRGBValueArray = getNeighborValues(originalImage, (x + filterWidth/2), (y + filterHeight/2), filterHeight, filterWidth);

        int newPixelValue = -1;
        if ("linear".equalsIgnoreCase(filterType)) {
            newPixelValue = calcAvgRGB(neighborRGBValueArray, weights, scalar);// calcAvgGray(neighborRGBValueArray, weights, scalar);
        }

        else if("median".equalsIgnoreCase(filterType)) {
            newPixelValue = calcMedian(neighborRGBValueArray, weights);
        }

        newImage.setRGB(x, y, newPixelValue); // if newPixelValue== -1, theres an error
    };

    public Filter.FuncInterface getFuncInterface () {
        return fobj;
    }



    public Filter(BufferedImage originalImage, String filterType, int filterWidth, int filterHeight, int[] weights, double scalar) {
        this.originalImage = originalImage;
        this.filterType = filterType;
        this.filterWidth = filterWidth;
        this.filterHeight = filterHeight;
        this.weights = weights;
        this.scalar = scalar;
    }


    // NOTE currently this only works for RGB (which includes black and white values, as those have rgb values, provided they are there)
    // NOTE crops the image border that does not fit in the filter convolution
    // NOTE assumes after accounting for weights, that the pixel color is still normalized TODO normalize after
    public BufferedImage filter () throws NullPointerException {
        // Parameter checking
        // If there was no weights array specified, then use weights of 1.
        if (weights == null) {
            int filterSize = filterHeight * filterWidth;
            weights = new int[filterSize];
            for (int i = 0; i < filterSize; i++) {
                weights[i] = 1;
            }
        }

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

        BufferedImage filterImage = new BufferedImage(originalImage.getWidth() - filterWidth, originalImage.getHeight() - filterHeight, originalImage.getType());

        ParallelMatrix parallelMatrix = new ParallelMatrix();
        parallelMatrix.doInParallel(filterImage, getFuncInterface());
        return filterImage;
    }


    // NOTE this only supports odd rectangles with a center pixel. (ex: 3x3, 5x3, etc not 4x4)
    public static ArrayList<Integer> getNeighborValues (BufferedImage originalImage, int originalX, int originalY, int filterHeight, int filterWidth) {
        ArrayList<Integer> neighborRGBValueArray = new ArrayList<>();

        // get the starting and ending valid coordinate values
        int startingX = originalX - filterWidth/2;
        int startingY = originalY - filterHeight/2;
        int endingX = originalX + filterWidth/2;
        int endingY = originalY + filterHeight/2;

        // get the pixels within the designated borders
        for (int x = startingX; x <= endingX; x++) {
            for (int y = startingY; y <= endingY; y++) {
                neighborRGBValueArray.add(originalImage.getRGB(x, y));
            }
        }

        return neighborRGBValueArray;
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

        MedianOfMedians medianOfMedians = new MedianOfMedians();
        int medianValue = medianOfMedians.findMedian(weightedMedianList,(weightedMedianList.size())/2 + 1,0,weightedMedianList.size() - 1);

        Utility utility = new Utility();
        return utility.setSingleColor(medianValue, "gray");
    }


    // does the above but for red, green, and blue at once
    public static int calcAvgRGB (ArrayList<Integer> list, int[] weights, double scalar) throws NullPointerException {
        if (list.size() != weights.length) {
            throw new NullPointerException("weights array was not the size of the filter");
        }

        int redAvg = 0;
        int greenAvg = 0;
        int blueAvg = 0;
        int alphaAvg = 0;
        for (int i = 0; i < list.size(); i++) {
            // add the respective RGB element to the correct color avg
            Color c = new Color(list.get(i));
            redAvg += c.getRed() * weights[i]; // red element * weight of pixel
            greenAvg += c.getGreen() * weights[i]; // green element * weight of pixel
            blueAvg += c.getBlue() * weights[i]; // blue element * weight of pixel
            alphaAvg += c.getAlpha() * weights[i];
        }
        redAvg /= list.size();
        greenAvg /= list.size();
        blueAvg /= list.size();
        alphaAvg /= list.size();

        redAvg *= scalar;
        greenAvg *= scalar;
        blueAvg *= scalar;
        alphaAvg *= scalar;

        //combine each of the RGB elements into a single int
        Color newColor = new Color(redAvg, greenAvg, blueAvg, alphaAvg);
        return newColor.getRGB();
    }

}
