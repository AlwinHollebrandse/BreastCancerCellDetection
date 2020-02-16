import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;

public class Filter {

    private BufferedImage originalImage;
    private String filterType;
    private int filterWidth;
    private int filterHeight;
    private int[] weights;
    private double scalar;

    @FunctionalInterface
    interface FuncInterface extends OverHeadInterface.FuncInterface {
        // An abstract function
        void function(BufferedImage originalImage, BufferedImage newImage, int x, int y);
    }

    public Filter.FuncInterface fobj = (BufferedImage originalImage, BufferedImage newImage, int x, int y) -> {
        ArrayList<Integer> neighborRGBValueArray = getNeighborValues(originalImage, (x + filterWidth/2), (y + filterHeight/2), filterHeight, filterWidth);

        int newPixelValue = -1;
        if ("average".equalsIgnoreCase(filterType)) {
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


    // NOTE and TODO currently this only works for RGB (which includes black and white values, as those have rgb values, provided they are there)
    // NOTE crops the image border that does not fit in the filter convolution
    // NOTE a value of 1 for both of these means a 3x3 grid
    // NOTE assumes after accounting for weights, that the pixel color is still normalized TODO normalize after
    // TODO enums for filtertype
//    public BufferedImage filter (BufferedImage image, String filterType, int filterWidth, int filterHeight,int[] weights, double scalar) throws NullPointerException {
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

        // endingX and Y need to be filterImage.getWidth() and y version - 1.
        int endingXCoordinate = filterImage.getWidth() - 1;
        int endingYCoordinate = filterImage.getHeight() - 1;

        int imageTotal = endingXCoordinate * endingYCoordinate;
        String barMessage = "error bar message";
        if ("average".equalsIgnoreCase(filterType)) {
            barMessage = "Average Filter";
        } else if("median".equalsIgnoreCase(filterType)) {
            barMessage = "Median Filter";
        }
//        ProgressBar bar = new ProgressBar(barMessage, imageTotal);

        ParallelMatrix parallelMatrix = new ParallelMatrix();
//        BufferedImage filterImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);

        parallelMatrix.doInParallel(originalImage, filterImage, barMessage, getFuncInterface());
        return filterImage;
    }

    // NOTE this only supports odd rectangles with a center pixel. (ex: 3x3, 5x3, etc not 4x4) // TODO delte this method. its the same as the RGB one
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

        //create and fill out a weighted medina list, where each pixel gets replicated by the associated weight value number of times. This is then sorted and the median is found.
        ArrayList<Integer> weightedMedianList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < weights[i]; j++) {
//                int listValue = list.get(i);
                int listValue = list.get(i) & 0xFF;
                weightedMedianList.add(listValue);
            }
        }

        Collections.sort(weightedMedianList);

        int medianValue = weightedMedianList.get(weightedMedianList.size()/2);
        Utility utility = new Utility();
        return utility.setSingleColor(medianValue, "gray");
    }

    // TODO could add color filter here (RGB intensity)
    // does the above but for red, green, and blue at once
    public static int calcAvgRGB (ArrayList<Integer> list, int[] weights, double scalar) throws NullPointerException { // TODO this one works, but the "gray" version doesnt
        if (list.size() != weights.length) {
            throw new NullPointerException("weights array was not the size of the filter");
        }

        int redAvg = 0;
        int greenAvg = 0;
        int blueAvg = 0;
        for (int i = 0; i < list.size(); i++) {
            // add the respective RGB element to the correct color avg
            int clr = list.get(i);
            redAvg += ((clr & 0x00ff0000) >> 16) * weights[i]; // red element * weight of pixel
            greenAvg += ((clr & 0x0000ff00) >> 8) * weights[i]; // green element * weight of pixel
            blueAvg += (clr & 0x000000ff) * weights[i]; // blue element * weight of pixel
        }
        redAvg /= list.size();
        greenAvg /= list.size();
        blueAvg /= list.size();

        redAvg *= scalar;
        greenAvg *= scalar;
        blueAvg *= scalar;

        //combine each of the RGB elements into a single int
        int result = 0x00000000;
        result = (result | redAvg) << 8; // after this line, result is 0x0000(red)00
        result = (result | greenAvg) << 8; // after this line, result is 0x00(red)(green)00
        result = (result | blueAvg); // after this line, result is 0x00(red)(green)(blue)

        return result;
    }

}
