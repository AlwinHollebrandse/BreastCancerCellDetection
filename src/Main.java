import java.io.File;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartUtilities;

public class Main {

    //Takes in 2 command line args: the file containing all of the images, and the file containing the instructions
    public static void main(String args[]) {

        String imageFilesLocation = args[0];
        String instructions = args[1];
        // TODO add error handling

        System.out.println("imageFilesLocation: " + imageFilesLocation);
        System.out.println("instructions: " + instructions);

        int[] averageHistogram = new int[256];
        File path = new File(imageFilesLocation); // TODO like a try catch or such
        File [] files = path.listFiles();

        for (int i = 0; i < files.length; i++){

            if (i >= 1)
                break;

            if (files[i].isFile()) { //this line weeds out other directories/folders
                System.out.println(files[i]);
                try {
                    final BufferedImage originalImage = ImageIO.read(files[i]);
//                    final BufferedImage originalImage = ImageIO.read(new File("black-and-white-tips.jpg"));
                    File output_file = new File("original.jpg");//"avg - cell1Gray.jpg");
                    ImageIO.write(originalImage, "jpg", output_file);

                    // TODO which is better gray?
//                    BufferedImage grayImage = grayScale.convertToGrayScale(originalImage);
//                    output_file = new File("grayscale.jpg");//"avg - cell1Gray.jpg");
//                    ImageIO.write(grayImage, "jpg", output_file);

                    GrayScale grayScale = new GrayScale(originalImage, "gray");
                    BufferedImage grayImage = grayScale.convertToSingleColor();
                    output_file = new File("grayImage.jpg");//"avg - cell1Gray.jpg");
                    ImageIO.write(grayImage, "jpg", output_file);


//                    NoiseAdder noiseAdder = new NoiseAdder(grayImage, 0.05);
//                    BufferedImage saltAndPepperImage = noiseAdder.createSaltAndPepperNoise();
//                    output_file = new File("saltAndPepper.jpg");//"avg - cell1Gray.jpg");
//                    ImageIO.write(saltAndPepperImage, "jpg", output_file);



                    long startTime = System.nanoTime();
                    GraphHistogram graphHistogram = new GraphHistogram();
                    int[] histogram = graphHistogram.createHistogram(grayImage);
                    System.out.println("Histogram creation Execution time in milliseconds : " + (System.nanoTime() - startTime) / 1000000);

                    HistogramFunctions histogramFunctions = new HistogramFunctions(grayImage, histogram);
                    BufferedImage equalizedImage = histogramFunctions.equalizedImage();
                    output_file = new File("equalizedImage.jpg");//"avg - cell1Gray.jpg");
                    ImageIO.write(equalizedImage, "jpg", output_file);

                    String graphTitle = "histogram";//files[i].toString(); // TODO looks shitty rn
                    // TODO show the equalized histogram too?
                    JFreeChart defaultHistogram = graphHistogram.graphHistogram(histogram, graphTitle);
                    String pathName = graphTitle + ".png";
                    ChartUtilities.saveChartAsPNG(new File(pathName), defaultHistogram, 700 , 500 );

                    averageHistogram = histogramFunctions.sumHistograms(averageHistogram);


//                    Filter filter = new Filter(grayImage, "average", 3, 3, null, 1);//new int[]{1, 2, 1, 2, 3, 2, 1, 2, 1}, (1/15));
//                    BufferedImage avgFilterImage = filter.filter();
//                    output_file = new File("average.jpg");//"avg - cell1Gray.jpg");
//                    ImageIO.write(avgFilterImage, "jpg", output_file);
//
//
//                    filter = new Filter(grayImage, "median", 3, 3, null, 1);//new int[]{1, 2, 1, 2, 3, 2, 1, 2, 1}, (1/15));
//                    BufferedImage medFilterImage = filter.filter();
//                    output_file = new File("median.jpg");//"avg - cell1Gray.jpg");
//                    ImageIO.write(medFilterImage, "jpg", output_file);

                } catch (Exception e) {
                    System.out.println("Error: " + e);
                }
            }
            getAverageHistogram(averageHistogram, files.length);
            // TODO add error handling if not a file

        }
    }

    private static void getAverageHistogram(int[] histogram, int divisor) {
        for (int i = 0; i < histogram.length; i++) {
            histogram[i] /= divisor;
        }

        try {
            GraphHistogram graphHistogram = new GraphHistogram();
            String graphTitle = "averageHistogram";//files[i].toString(); // TODO looks shitty rn
            JFreeChart defaultHistogram = graphHistogram.graphHistogram(histogram, graphTitle);
            String pathName = graphTitle + ".png";
            ChartUtilities.saveChartAsPNG(new File(pathName), defaultHistogram, 700, 500);
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
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