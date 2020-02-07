import java.awt.image.ColorConvertOp;
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class Main {

    //Takes in 2 command line args: the file containing all of the images, and the file containing the instructions
    public static void main(String args[])throws IOException {

        String imageFilesLocation = args[0];
        String instructions = args[1];
        // TODO add error handling

        System.out.println("imageFilesLocation: " + imageFilesLocation);
        System.out.println("instructions: " + instructions);


        File path = new File(imageFilesLocation); // TODO like a try catch or such
        File [] files = path.listFiles();

        for (int i = 0; i < files.length; i++){

            if (i >= 1)
                break;

            if (files[i].isFile()) { //this line weeds out other directories/folders
                System.out.println(files[i]);
                BufferedImage workingImage = null;
                try {
                    final BufferedImage originalImage = ImageIO.read(files[i]);
                    workingImage = convertToGrayScale(originalImage);
                    workingImage = avgFilter(workingImage, 5, 5, null, 1);//new double[]{.5, .5, .5, .5, 1, .5, .5, .5, .5}, 1); // TODO ask why scalar, and how to normalize weights array + pixel

                    System.out.println("Reading complete.");
                } catch (IOException e) {
                    System.out.println("Error: " + e);
                } catch (NullPointerException e) {
                    System.out.println("Error: " + e);
                }

                // WRITE IMAGE
                try {
                    // Output file path
                    File output_file = new File("avg - cell1Gray.jpg");

                    // Writing to file taking type and path as
                    ImageIO.write(workingImage, "jpg", output_file);

                    System.out.println("Writing complete.");
                } catch (IOException e) {
                    System.out.println("Error: " + e);
                } catch (IllegalArgumentException e) {
                    System.out.println("Error: " + e);
                } catch (NullPointerException e) {
                    System.out.println("Error: " + e);
                }
            }
            // TODO add error handling if not a file
        }

//        BufferedImage workingImage = null;
//        try {
//            final File input_file = new File("volcano.jpg"); //image file path
//
//            final BufferedImage originalImage = ImageIO.read(input_file);
//            System.out.println("original x: " + originalImage.getWidth() + ", y: " + originalImage.getHeight()); // 350x211
//            workingImage = convertToGrayScale(originalImage);
//            workingImage = avgFilter(workingImage, 5, 5, null, 1);//new double[]{.5, .5, .5, .5, 1, .5, .5, .5, .5}, 1); // TODO ask why scalar, and how to normalize weights array + pixel
//
//            System.out.println("Reading complete.");
//        }
//        catch(IOException e) {
//            System.out.println("Error: "+e);
//        }
//        catch(NullPointerException e) {
//            System.out.println("Error: "+e);
//        }
//
//         // WRITE IMAGE
//         try
//         {
//             // Output file path
//             File output_file = new File("avg-volcano.jpg");
//
//             // Writing to file taking type and path as
//             ImageIO.write(workingImage, "jpg", output_file);
//
//             System.out.println("Writing complete.");
//         }
//         catch(IOException e)
//         {
//             System.out.println("Error: "+e);
//         }
//         catch(IllegalArgumentException e) {
//             System.out.println("Error: "+e);
//         }
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

        // endingX and Y need to be avgImage.getWidth() and y version - 1.
        int endingXCoordinate = avgImage.getWidth() - 1;
        int endingYCoordinate = avgImage.getHeight() - 1;

        int imageTotal = endingXCoordinate * endingYCoordinate;
        ProgressBar bar = new ProgressBar("avgFilter", imageTotal);

        // loop through new cropped version size
        for (int x = 0; x < endingXCoordinate; x++) {
            for (int y = 0; y < endingYCoordinate; y++) {
                ArrayList<Integer> neighborRGBValueArray = getNeighborValues(originalImage, (x + filterWidth/2), (y + filterHeight/2), filterHeight, filterWidth);
                int avgRGBValue = calcAvgGray(neighborRGBValueArray, weights);
                // TODO multiple each pixel by the scalar.
                avgImage.setRGB(x, y, avgRGBValue);
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

        return avgImage;
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


    public static int calcAvgGray (ArrayList<Integer> list, double[] weights) throws NullPointerException {
        if (list.size() != weights.length) {
            throw new NullPointerException("weights array was not the size of the filter");
        }

        int result = 0;

        for (int i = 0; i < list.size(); i++) { // img.getRGB(x, y)& 0xFF;
            result += (list.get(i) & 0xFF) * weights[i]; // element * weight of pixel
        }
        System.out.println();
//        System.out.println(list.toString());
//        System.out.println("avg gray pixel value: " + result);
        result /= list.size();
//        System.out.println("avg gray pixel value: " + result);
        System.out.println("center gray pixel value: " + (list.get(list.size()/2)  & 0xFF));

        int centerPixelValue= list.get(list.size()/2); // gets everything about the center pixel, including intensity and such
        result = (centerPixelValue & 0xFFFFFF00) | result; // keeps all of the intensity and suck values and only changes the gray scale value
        // bug has something to do with the other bits of the gray, the leading ones... I think its because i
//        System.out.println("avg gray pixel value: " + (result));
        System.out.println("avg gray pixel value: " + (result  & 0xFF));



        return result;
    }


    // TODO could add color filter here (RGB intensity)
    // does the above but for red, green, and blue at once
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


                    // from https://stackoverflow.com/questions/9131678/convert-a-rgb-image-to-grayscale-image-reducing-the-memory-in-java


    /** * convert a BufferedImage to RGB colourspace */
    // from https://blog.idrsolutions.com/2009/10/converting-java-bufferedimage-between-colorspaces/
    public static BufferedImage convertToGrayScale(BufferedImage originalImage) { // TODO is this allowed? pretty sure its not
        BufferedImage grayScaleImage = null;
        try {
            grayScaleImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
            ColorConvertOp xformOp = new ColorConvertOp(null);
            xformOp.filter(originalImage, grayScaleImage);
        } catch (Exception e) {
            System.out.println("Exception " + e + " converting image");
        }
        return grayScaleImage;
    }
}