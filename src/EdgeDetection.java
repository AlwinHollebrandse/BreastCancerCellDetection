import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Semaphore;

public class EdgeDetection {

    private BufferedImage sharpenedImage;
    private int filterWidth = 3; // All compass filters are 3x3
    private int filterHeight = 3;
    private boolean stop = false; // TODO delete

    @FunctionalInterface
    interface FuncInterface extends OverHeadInterface.FuncInterface {
        // An abstract function
        void function(BufferedImage newImage, Semaphore semaphore, int x, int y);
    }

    public EdgeDetection.FuncInterface fobj = (BufferedImage newImage, Semaphore semaphore, int x, int y) -> {
        ArrayList<Integer> neighborRGBValueArray = getNeighborValues(sharpenedImage, (x + filterWidth/2), (y + filterHeight/2), filterHeight, filterWidth);

        // define edge value in each direction. NOTE that only 4 are defined here, this is because the other 4 can be defined as the - result of these 4
        int horizontalEdge = calcEdgeStrength(neighborRGBValueArray, new int[]{-1, 0, 1, -2, 0, 2, -1, 0, 1}, 1);//(1/8.0)); // TODO check scalar
        int negDiagEdge = calcEdgeStrength(neighborRGBValueArray, new int[]{-2, -1, 0, -1, 0, 1, 0, 1, 2}, 1);//(1/8.0)); // TODO check scalar
        int verticalEdge = calcEdgeStrength(neighborRGBValueArray, new int[]{-1, -2, -1, 0, 0, 0, 1, 2, 1}, 1);//(1/8.0)); // TODO check scalar
        int posDiagEdge = calcEdgeStrength(neighborRGBValueArray, new int[]{0, -1, -2, 1, 0, -1, 2, 1, 0}, 1);//(1/8.0)); // TODO check scalar

        Integer[] allEdgeStrengths = {horizontalEdge, -horizontalEdge, negDiagEdge, -negDiagEdge, verticalEdge, -verticalEdge, posDiagEdge, -posDiagEdge};
        int edgeStrength = Collections.max(Arrays.asList(allEdgeStrengths));

        Utility utility = new Utility();
        if (edgeStrength >= 1) { // TODO is there a threshold for this? or just >= 1?
            int colorValue = utility.setSingleColorRBG(255, "gray"); // white
            newImage.setRGB(x, y, colorValue);
        } else {
            stop = true;
            int colorValue = utility.setSingleColorRBG(0, "gray"); // black
            newImage.setRGB(x, y, colorValue);
        }
    };

    public EdgeDetection.FuncInterface getFuncInterface () {
        return fobj;
    }


    public EdgeDetection() {}


    // performs laplace filter (sharpens edges then applies compass edge detection)
    public BufferedImage edgeDetection (BufferedImage originalImage) throws NullPointerException {
        this.stop = false;

        // apply Laplace Filter to sharpen edges TODO do it
        Filter filter = new Filter(originalImage, "linear", 3, 3, new int[]{1,1,1,1,1,1,1,1,1}, 1);//1/9.0);
//        Filter filter = new Filter(originalImage, "linear", 3, 3, new int[]{0, 1, 0, 1, -4, 1, 0, 1, 0}, 1/8.0);
////        Filter filter = new Filter(originalImage, "median", 3, 3, new int[]{0, 1, 0, 1, -4, 1, 0, 1, 0}, 1);//(1/8.0)); // TODO check scalar
        this.sharpenedImage = filter.filter();
//
////        Utility utility = new Utility();
////        for (int x = 200; x < 300; x ++) {
////            for (int y = 0; y < sharpenedImage.getHeight(); y ++) {
////                sharpenedImage.setRGB(x, y, utility.setSingleColorRBG(255, "gray"));
////            }
////        }
//
//        File output_file = new File("sharpenedImage.jpg");
//        try {
//            ImageIO.write(sharpenedImage, "jpg", output_file);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        BufferedImage edgeMapImage = new BufferedImage(sharpenedImage.getWidth() - ((filterWidth/2) * 2), sharpenedImage.getHeight() - ((filterHeight/2) * 2), sharpenedImage.getType()); // NOTE all Compass directions are 3x3

        ParallelMatrix parallelMatrix = new ParallelMatrix();
        parallelMatrix.doInParallel(edgeMapImage, getFuncInterface());
        return edgeMapImage;
    }


    // NOTE currently this only works for RGB (which includes black and white values, as those have rgb values, provided they are there)
    // NOTE crops the image border that does not fit in the filter convolution
    // NOTE assumes after accounting for weights, that the pixel color is still normalized TODO normalize after


    // NOTE this only supports odd rectangles with a center pixel. (ex: 3x3, 5x3, etc not 4x4)
    public ArrayList<Integer> getNeighborValues (BufferedImage originalImage, int originalX, int originalY, int filterHeight, int filterWidth) {
        ArrayList<Integer> neighborRGBValueArray = new ArrayList<>();
        ArrayList<Color> temp = new ArrayList<>();


        // get the starting and ending valid coordinate values
        int startingX = originalX - filterWidth/2;
        int startingY = originalY - filterHeight/2;
        int endingX = originalX + filterWidth/2;
        int endingY = originalY + filterHeight/2;

        // get the pixels within the designated borders
        for (int x = startingX; x <= endingX; x++) {
            for (int y = startingY; y <= endingY; y++) {
                neighborRGBValueArray.add(originalImage.getRGB(x, y));
                temp.add(new Color(originalImage.getRGB(x, y)));
            }
        }

        if (this.stop) {
            int blah = 1;
        }
        return neighborRGBValueArray;
    }

    // does the above but for red, green, and blue at once
    public static int calcEdgeStrength (ArrayList<Integer> list, int[] weights, double scalar) throws NullPointerException {
        if (list.size() != weights.length) {
            throw new NullPointerException("weights array was not the size of the filter");
        }

        int redAvg = 0;
        int greenAvg = 0;
        int blueAvg = 0;
        for (int i = 0; i < list.size(); i++) {
            // add the respective RGB element to the correct color avg
            Color c = new Color(list.get(i));
            redAvg += c.getRed() * weights[i]; // red element * weight of pixel
            greenAvg += c.getGreen() * weights[i]; // green element * weight of pixel
            blueAvg += c.getBlue() * weights[i]; // blue element * weight of pixel
        }
        redAvg /= list.size();
        greenAvg /= list.size();
        blueAvg /= list.size();

        redAvg *= scalar;
        greenAvg *= scalar;
        blueAvg *= scalar;

        // TODO is there a bug here with the scalar being a double, but the results is an int? like int division. because color cons needs ints.... if scalar < 1

        int gray = (int) (redAvg * 0.299) + (int) (greenAvg * 0.587) + (int) (blueAvg * 0.114);
        return gray;
    }
}