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
        }

        else if("median".equalsIgnoreCase(filterType)) {
            newPixelValue = calcMedian(neighborRGBValueArray, weights);
        }

        newImage.setRGB(x, y, newPixelValue); // if newPixelValue== -1, there is an error // TODO throw an error?
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

//        originalImage = new BufferedImage(4, 4, originalImage.getType());

        BufferedImage filterImage = new BufferedImage(originalImage.getWidth() - ((filterWidth/2) * 2), originalImage.getHeight() - ((filterHeight/2) * 2), originalImage.getType());

        ParallelMatrix parallelMatrix = new ParallelMatrix();
        parallelMatrix.doInParallel(filterImage, getFuncInterface());
        return filterImage;
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

//        ArrayList<Integer> redArrayList = new ArrayList<>();
        double redAvg = 0;
        double greenAvg = 0;
        double blueAvg = 0;
        double alphaAvg = 0;
        for (int i = 0; i < list.size(); i++) {
            // add the respective RGB element to the correct color avg
            Color c = new Color(list.get(i));
//            redArrayList.add(c.getRed());
            redAvg += c.getRed() * weights[i]; // red element * weight of pixel
            greenAvg += c.getGreen() * weights[i]; // green element * weight of pixel
            blueAvg += c.getBlue() * weights[i]; // blue element * weight of pixel
            alphaAvg += c.getAlpha() * weights[i];
        }
//        System.out.println(redArrayList.toString());
        redAvg /= list.size();
        greenAvg /= list.size();
        blueAvg /= list.size();
        alphaAvg /= list.size();
//        System.out.println("pre scalar red avg (aka actual avg): " + redAvg);

        redAvg *= scalar;
        greenAvg *= scalar;
        blueAvg *= scalar;
        alphaAvg *= scalar;
//        System.out.println("post scalar red avg: " + redAvg);

        Utility utility = new Utility();
        int finalRedAvg = utility.normalizeColorInt((int)redAvg);
        int finalGreenAvg = utility.normalizeColorInt((int)greenAvg);
        int finalBlueAvg = utility.normalizeColorInt((int)blueAvg);
        int finalAlphaAvg = utility.normalizeColorInt((int)alphaAvg);

        //combine each of the RGB elements into a single int
        Color newColor = new Color(finalRedAvg, finalGreenAvg, finalBlueAvg, finalAlphaAvg);
        return newColor.getRGB();

//        Using weights = new int[]{1,1,1,1,1,1,1,1,1}, scalar = 1/9.0)
//        [187, 141, 25, 186, 141, 27, 186, 141, 22]
//        pre scalar red avg (aka actual avg): 117.33333333333333
//        post scalar red avg: 13.037037037037036

//        Using weights = new int[]{1,1,1,1,1,1,1,1,1}, scalar = 1)
//        [187, 141, 25, 186, 141, 27, 186, 141, 22]
//        pre scalar red avg (aka actual avg): 117.33333333333333
//        post scalar red avg: 117.33333333333333

//        new int[]{0, 1, 0, 1, -4, 1, 0, 1, 0}, 1
//        [187, 141, 25, 186, 141, 27, 186, 141, 22]
//        pre scalar red avg (aka actual avg): -7.666666666666667
//        post scalar red avg: -7.666666666666667

//        new int[]{0, 1, 0, 1, -4, 1, 0, 1, 0}, 1/8.0
//        [187, 141, 25, 186, 141, 27, 186, 141, 22]
//        pre scalar red avg (aka actual avg): -7.666666666666667
//        post scalar red avg: -0.9583333333333334

    }

}
