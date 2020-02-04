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
            avgImage = avgFilter(originalImage);

            System.out.println("Reading complete.");


        }
        catch(IOException e) {
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
    }

    // NOTE and TODO currently this only works for RGB (which includes black and white values, as those have rgb values, provided they are there)
    public static BufferedImage avgFilter (BufferedImage originalImage) {//, int size, String shape, double[] weights, String function) {
        int verticelNeighborCount = 1; // TODO make these params, along with the other options (options: int size, String shape, double[] weights, String function)
        int horizontalNeighborCount = 1; // NOTE a value of 1 for both of these means a 3x3 grid
        BufferedImage avgImage = new BufferedImage(originalImage.getWidth(),originalImage.getHeight(),originalImage.getType());

        int imageTotal = originalImage.getWidth() * originalImage.getHeight();
        ProgressBar bar = new ProgressBar("avgFilter", imageTotal);
        for (int x = 0; x < originalImage.getWidth(); x++) {
            for (int y = 0; y < originalImage.getHeight(); y++) {
                     ArrayList<Integer> neighborRGBValueArray = getNeighborRGBs(originalImage, x, y, verticelNeighborCount, horizontalNeighborCount);
                int avgRGBValue = calcAvgRGB(neighborRGBValueArray);
                avgImage.setRGB(x, y, avgRGBValue);
                bar.next();
            }
        }
        return avgImage;
    }

    // TODO As of rn, this only supports odd rectangles. (ex: 3x3, 5x3, etc not 4x4)
    public static ArrayList<Integer> getNeighborRGBs (BufferedImage originalImage, int originalX, int originalY, int verticelNeighborCount, int horizontalNeighborCount) {
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

    public static int calcAvgRGB (ArrayList<Integer> list) {
        int redAvg = 0;
        int greenAvg = 0;
        int blueAvg = 0;
        for (int i = 0; i < list.size(); i++) {
            // add the respective RGB element to the correct color avg
            int clr = list.get(i);
            redAvg += (clr & 0x00ff0000) >> 16;
            greenAvg += (clr & 0x0000ff00) >> 8;
            blueAvg += clr & 0x000000ff;
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