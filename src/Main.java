import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class Main {

    public static void main(String args[])throws IOException {

        BufferedImage avgImage = null;
        try {
            final File input_file = new File("black-and-white-tips.jpg"); //image file path

            final BufferedImage originalImage = ImageIO.read(input_file);
            avgImage = avgFilter(originalImage, 3, 3, new double[]{.5, .5, .5, .5, 1, .5, .5, .5, .5}, 1); // TODO ask why scalar, and how to normalize weights array + pixel

            System.out.println("Reading complete.");
        }
        catch(IOException e) {
            System.out.println("Error: "+e);
        }
        catch(NullPointerException e) {
            System.out.println("Error: "+e);
        }

         // WRITE IMAGE
         try
         {
             // Output file path
             File output_file = new File("avg-black-and-white-tips.jpg");

             // Writing to file taking type and path as
             ImageIO.write(avgImage, "jpg", output_file);

             System.out.println("Writing complete.");
         }
         catch(IOException e)
         {
             System.out.println("Error: "+e);
         }
         catch(IllegalArgumentException e) {
             System.out.println("Error: "+e);
         }
    }

    // NOTE and TODO currently this only works for RGB (which includes black and white values, as those have rgb values, provided they are there)
    // NOTE crops the image border that does not fit in the filter convolution
    // NOTE a value of 1 for both of these means a 3x3 grid
    // NOTE assumes after accounting for weights, that the pixel color is still normalized TODO normalize after
    public static BufferedImage avgFilter (BufferedImage originalImage, int filterHeight, int filterWidth, double[] weights, double scalar) throws NullPointerException {//, int size, String shape, double[] weights, String function) {
        // If there was no weights array specified, then use weights of 1.
        if (weights == null) {
            int filterSize = filterHeight * filterWidth;
            weights = new double[filterSize];
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

        // CROP BORDER VERSION
        BufferedImage avgImage = new BufferedImage(originalImage.getWidth() - filterWidth, originalImage.getHeight() - filterHeight, originalImage.getType());
        int imageTotal = (originalImage.getWidth() - filterWidth) * (originalImage.getHeight() - filterHeight);
        ProgressBar bar = new ProgressBar("avgFilter", imageTotal);

        int endingXCoordinate = originalImage.getWidth() - filterWidth;
        int endingYCoordinate = originalImage.getHeight() - filterHeight;

        for (int x = filterWidth/2; x <= endingXCoordinate; x++) {
            for (int y = filterHeight/2; y <= endingYCoordinate; y++) {
                ArrayList<Integer> neighborRGBValueArray = getNeighborRGBs(originalImage, x, y, filterHeight, filterWidth);
                int avgRGBValue = calcAvgRGB(neighborRGBValueArray, weights);
                avgImage.setRGB(x - filterWidth/2, y - filterHeight/2, avgRGBValue);
                bar.next();
            }
        }

        // WITH BORDER VERSION
//        BufferedImage avgImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), originalImage.getType());
//        int imageTotal = originalImage.getWidth() * originalImage.getHeight();
//        ProgressBar bar = new ProgressBar("avgFilter", imageTotal);

//        for (int x = 0; x < originalImage.getWidth(); x++) {
//            for (int y = 0; y < originalImage.getHeight(); y++) {
//                     ArrayList<Integer> neighborRGBValueArray = getNeighborRGBs(originalImage, x, y, filterHeight, filterWidth);
//                int avgRGBValue = calcAvgRGB(neighborRGBValueArray, weights);
//                avgImage.setRGB(x, y, avgRGBValue);
//                bar.next();
//            }
//        }

        // TODO multiple each pixel by the scalar.
        return avgImage;
    }

    // NOTE this only supports odd rectangles with a center pixel. (ex: 3x3, 5x3, etc not 4x4)
    public static ArrayList<Integer> getNeighborRGBs (BufferedImage originalImage, int originalX, int originalY, int filterHeight, int filterWidth) {
        ArrayList<Integer> neighborRGBValueArray = new ArrayList<>();

        // get the starting and ending valid coordinate values
        int startingX = (originalX - filterWidth/2 >= 0) ? (originalX - filterWidth/2) : 0;
        int startingY = (originalY - filterHeight/2 >= 0) ? (originalY - filterHeight/2) : 0;
        int endingX = (originalX + filterWidth/2 + 1 <= originalImage.getWidth()) ? (originalX + filterWidth/2 + 1) : originalImage.getWidth();
        int endingY = (originalY + filterHeight/2 + 1 <= originalImage.getHeight()) ? (originalY + filterHeight/2 + 1) : originalImage.getHeight();

        // get the pixels within the designated borders
        for (int x = startingX; x < endingX; x++) {
            for (int y = startingY; y < endingY; y++) {
                neighborRGBValueArray.add(originalImage.getRGB(x, y));
            }
        }

        return neighborRGBValueArray;
    }

    // NOTE this only supports odd rectangles with a center pixel. (ex: 3x3, 5x3, etc not 4x4)
    public static ArrayList<Integer> getNeighborRGBsWithBorder (BufferedImage originalImage, int originalX, int originalY, int verticelNeighborCount, int horizontalNeighborCount) {
        ArrayList<Integer> neighborRGBValueArray = new ArrayList<>();

        // get the starting and ending valid coordinate values
        int startingX = (originalX - horizontalNeighborCount >= 0) ? (originalX - horizontalNeighborCount) : 0;
        int startingY = (originalY - verticelNeighborCount >= 0) ? (originalY - verticelNeighborCount) : 0;
        int endingX = (originalX + horizontalNeighborCount + 1 <= originalImage.getWidth()) ? (originalX + horizontalNeighborCount + 1) : originalImage.getWidth();
        int endingY = (originalY + verticelNeighborCount + 1 <= originalImage.getHeight()) ? (originalY + verticelNeighborCount + 1) : originalImage.getHeight();

        // get the pixels within the designated borders
        for (int x = startingX; x < endingX; x++) {
            for (int y = startingY; y < endingY; y++) {
                neighborRGBValueArray.add(originalImage.getRGB(x, y));
            }
        }

        return neighborRGBValueArray;
    }

    public static int calcAvgRGB (ArrayList<Integer> list, double[] weights) throws NullPointerException {
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

        //combine each of the RGB elements into a single int
        int result = 0x00000000;
        result = (result | redAvg) << 8; // after this line, result is 0x0000(red)00
        result = (result | greenAvg) << 8; // after this line, result is 0x00(red)(green)00
        result = (result | blueAvg); // after this line, result is 0x00(red)(green)(blue)

        return result;
    }
}