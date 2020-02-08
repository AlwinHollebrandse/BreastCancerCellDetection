import java.awt.image.ColorConvertOp;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
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
                BufferedImage originalImage = null;
                try {
                    originalImage = ImageIO.read(files[i]);
//                    final BufferedImage originalImage = ImageIO.read(new File("black-and-white-tips.jpg"));

                    workingImage = convertToGrayScale(originalImage);
                    System.out.println("originalImage 0,0: " + originalImage.getRGB(0,0) + ", type: " + originalImage.getType() + ", workingImage: " + workingImage.getRGB(0,0) + ", type: " +workingImage.getType());
                    workingImage = filter(workingImage, "average", 3, 3, null, 1);//new double[]{.5, .5, .5, .5, 1, .5, .5, .5, .5}, 1);

                    System.out.println("Reading complete.");
                } catch (IOException e) {
                    System.out.println("Error: " + e);
                } catch (NullPointerException e) {
                    System.out.println("Error: " + e);
                }

                // WRITE IMAGE
                try {
                    // Output file path
                    File output_file = new File("original.jpg");//"avg - cell1Gray.jpg");
                    // Writing to file taking type and path as
                    ImageIO.write(originalImage, "jpg", output_file);

                    // Output file path
                    output_file = new File("result.jpg");//"avg - cell1Gray.jpg");
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
    }

    // NOTE and TODO currently this only works for RGB (which includes black and white values, as those have rgb values, provided they are there)
    // NOTE crops the image border that does not fit in the filter convolution
    // NOTE a value of 1 for both of these means a 3x3 grid
    // NOTE assumes after accounting for weights, that the pixel color is still normalized TODO normalize after
    // TODO enums for filtertype
    public static BufferedImage filter (BufferedImage originalImage, String filterType, int filterHeight, int filterWidth, int[] weights, double scalar) throws NullPointerException {//, int size, String shape, double[] weights, String function) {
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

        // CROP BORDER VERSION
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
        ProgressBar bar = new ProgressBar(barMessage, imageTotal);

        // loop through new cropped version size
        for (int x = 0; x < endingXCoordinate; x++) {
            for (int y = 0; y < endingYCoordinate; y++) {
                ArrayList<Integer> neighborRGBValueArray = getNeighborValues(originalImage, (x + filterWidth/2), (y + filterHeight/2), filterHeight, filterWidth);

                int newPixelValue = -1;
                if ("average".equalsIgnoreCase(filterType)) {
                    newPixelValue = calcAvgGray(neighborRGBValueArray, weights, scalar);
                }

                else if("median".equalsIgnoreCase(filterType)) {
                    newPixelValue = calcMedianGray(neighborRGBValueArray, weights);
                }
                
                filterImage.setRGB(x, y, newPixelValue); // if newPixelValue== -1, theres an error
                bar.next();
            }
        }

        // WITH BORDER VERSION
//        BufferedImage filterImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), originalImage.getType());
//        int imageTotal = originalImage.getWidth() * originalImage.getHeight();
//        ProgressBar bar = new ProgressBar("avgFilter", imageTotal);

//        for (int x = 0; x < originalImage.getWidth(); x++) {
//            for (int y = 0; y < originalImage.getHeight(); y++) {
//                     ArrayList<Integer> neighborRGBValueArray = getNeighborRGBs(originalImage, x, y, filterHeight, filterWidth);
//                int avgRGBValue = calcAvgRGB(neighborRGBValueArray, weights);
//                filterImage.setRGB(x, y, avgRGBValue);
//                bar.next();
//            }
//        }
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


    public static int calcAvgGray (ArrayList<Integer> list, int[] weights, double scalar) throws NullPointerException {
        if (list.size() != weights.length) {
            throw new NullPointerException("weights array was not the size of the filter");
        }

        int resultColor = 0;

        for (int i = 0; i < list.size(); i++) { // img.getRGB(x, y)& 0xFF;
            resultColor += (list.get(i) & 0xFF) * weights[i]; // element * weight of pixel
        }
        resultColor /= list.size();
        // TODO multiple pixel by the scalar.
        resultColor *= scalar;

        int centerPixelValue= list.get(list.size()/2); // gets everything about the center pixel, including intensity and such

        return setGrayPixelColor(centerPixelValue, resultColor);
    }

    public static int calcMedianGray (ArrayList<Integer> list, int[] weights) throws NullPointerException {
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
        int centerPixelValue= list.get(list.size()/2); // gets everything about the center pixel, including intensity and such
        return setGrayPixelColor(centerPixelValue, medianValue);
    }

    public static int setGrayPixelColor (int pixelRGB, int pixelColor) {
        int result;

        // TODO most of these are here for debugging
        int pixelRGBNonColor = pixelRGB & 0xFFFFFF00;
        int finalResult = (pixelRGB & 0xFFFFFF00) | pixelColor;//sets the grey color value to 0
        int temp = finalResult & 0xFFFFFF00;// should = centerPixelNonColor

        int realResultColor = pixelColor;
        int centerPixelColor = pixelRGB & 0xFF;
        int resultPixelColor = finalResult & 0xFF;

//        result = (pixelRGB & 0xFFFFFF00) | (centerPixelColor + 100);
        result = (pixelRGB & 0xFFFFFF00) | pixelColor; // keeps all of the intensity and such values and only changes the gray scale value


        // bug has something to do with the other bits of the gray, the leading ones... I think its because i
//        System.out.println("avg gray pixel value: " + (result));
        if (Math.abs(pixelRGB - result) > 100)
            System.out.println("resultPixelColor: " + resultPixelColor + ", real pixelColor: " + pixelColor + ", pixelRGB: " + pixelRGB + ", result: " + result);

        return  result;
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

    // OR this from: https://stackoverflow.com/questions/9131678/convert-a-rgb-image-to-grayscale-image-reducing-the-memory-in-java
//    ImageFilter filter = new GrayFilter(true, 50);
//    ImageProducer producer = new FilteredImageSource(colorImage.getSource(), filter);
//    Image mage = Toolkit.getDefaultToolkit().createImage(producer);

    //techincally, this would make it gray, but the problem is that it stays in rgb scale
    //tutorialspoint.com/java_dip/grayscale_conversion.htm
//    for(int i=0; i<height; i++) {
//
//        for(int j=0; j<width; j++) {
//
//            Color c = new Color(image.getRGB(j, i));
//            int red = (int)(c.getRed() * 0.299);
//            int green = (int)(c.getGreen() * 0.587);
//            int blue = (int)(c.getBlue() *0.114);
//            Color newColor = new Color(red+green+blue,
//
//                    red+green+blue,red+green+blue);
//
//            image.setRGB(j,i,newColor.getRGB());
//        }
//    }

    // NOTE randomThreshold must be between 0-1
    public static BufferedImage createSaltAndPepperNoise (BufferedImage image, double randomThreshold) throws IllegalArgumentException {
        if (randomThreshold > 1 || randomThreshold < 0) {
            throw new IllegalArgumentException("randomThreshold must be between 0-1");
        }

        BufferedImage saltAndPepperImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);  // TODO currently only supports grey images (type: TYPE_BYTE_GRAY)
        Random rand = new Random();

        for (int x = 0; x < saltAndPepperImage.getWidth(); x++) {
            for (int y = 0; y < saltAndPepperImage.getHeight(); y++) {
                double rand_dub1 = rand.nextDouble();
                int imagePixelRGB = image.getRGB(x, y);

                if (rand_dub1 <= randomThreshold) {// if the random threshold is met
                    rand_dub1 = rand.nextDouble(); // 50/50 to see if the pixel will be white or black
                    if (rand_dub1 > .5) {
                        saltAndPepperImage.setRGB(x, y, setGrayPixelColor(imagePixelRGB, 255));
                    }
                    else {
                        saltAndPepperImage.setRGB(x, y, setGrayPixelColor(imagePixelRGB, 0));
                    }
                } else {
                    saltAndPepperImage.setRGB(x, y, imagePixelRGB);
                }

            }
        }
        return saltAndPepperImage;
    }



    // TODO make the gaussian noise one
    // TODO Histogram calculation for each individual image.
//    Averaged histograms of pixel values for each class of images.
//    • Histogram equalization for each image.
//    • Selected image quantization technique for user-specified levels.
//    • Display the following performance measures
//      o Processing time for the entire batch per each procedure
//      o Averaged processing time per image per each procedure
//      o MSQE for image quantization levels

}