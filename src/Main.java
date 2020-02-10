import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartUtilities;

public class Main {

    //Takes in 2 command line args: the file containing all of the images, and the file containing the instructions
    public static void main(String args[])throws IOException {

        String imageFilesLocation = args[0];
        String instructions = args[1];
        // TODO add error handling

        System.out.println("imageFilesLocation: " + imageFilesLocation);
        System.out.println("instructions: " + instructions);

        Filter filter = new Filter();
        GrayScale grayScale = new GrayScale();
        NoiseAdder noiseAdder = new NoiseAdder();

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

                    // TODO which is better?
//                    BufferedImage grayImage = grayScale.convertToGrayScale(originalImage);
//                    output_file = new File("grayscale.jpg");//"avg - cell1Gray.jpg");
//                    ImageIO.write(grayImage, "jpg", output_file);

                    long startTime = System.nanoTime();
                    BufferedImage grayImage = grayScale.convertToSingleColor(originalImage, "gray");
                    System.out.println("Graying Execution time in milliseconds : " + (System.nanoTime() - startTime) / 1000000);
                    output_file = new File("grayscale1.jpg");//"avg - cell1Gray.jpg");
                    ImageIO.write(grayImage, "jpg", output_file);

                    ParallelMatrix parallelMatrix = new ParallelMatrix();
//                    GrayScale grayscale = new GrayScale();
                    GrayScale.FuncInterface grayScaleCode = grayScale.getFuncInterface();
                    BufferedImage grayImage2 = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
                    parallelMatrix.doInParallel (originalImage, grayImage2, "gray", 0.05, grayScaleCode, "Converting to GrayScale");
//                    BufferedImage grayImage2 = grayScale.convertToSingleColor(originalImage, "gray");
                    output_file = new File("grayscale2.jpg");//"avg - cell1Gray.jpg");
                    ImageIO.write(grayImage2, "jpg", output_file);

                    BufferedImage grayImage3 = grayScale.convertToSingleColorParallel(originalImage, "gray");
                    output_file = new File("grayscale3.jpg");//"avg - cell1Gray.jpg");
                    ImageIO.write(grayImage3, "jpg", output_file);



//                    startTime = System.nanoTime();
//                    BufferedImage saltAndPepperImage = noiseAdder.createSaltAndPepperNoise(grayImage, 0.02);
//                    System.out.println("S&P Execution time in milliseconds : " + (System.nanoTime() - startTime) / 1000000);
//                    output_file = new File("saltAndPepper.jpg");//"avg - cell1Gray.jpg");
//                    ImageIO.write(saltAndPepperImage, "jpg", output_file);
//
//                    parallelMatrix = new ParallelMatrix();
////                    NoiseAdder noiseAdder = new NoiseAdder();
//                    NoiseAdder.FuncInterface noiseAdderCode = noiseAdder.getFuncInterface();
//                    BufferedImage saltAndPepperImage2 = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
//                    parallelMatrix.doInParallel (originalImage, saltAndPepperImage2, "gray", 0.05, noiseAdderCode, "S&P noise");
//                    output_file = new File("saltAndPepper2.jpg");//"avg - cell1Gray.jpg");
//                    ImageIO.write(grayImage2, "jpg", output_file);



//                    startTime = System.nanoTime();
//                    Histogram histogram=new Histogram();
//                    double[] histogramArray = histogram.createHistogram(grayImage);
//                    System.out.println("Histogram Execution time in milliseconds : " + (System.nanoTime() - startTime) / 1000000);
//                    String graphTitle = "histogram";//files[i].toString(); // TODO looks shitty rn
//                    JFreeChart result = histogram.graphHistogram(histogramArray, graphTitle);
//                    String pathName = graphTitle + ".png";
//                    ChartUtilities.saveChartAsPNG(new File(pathName), result, 600, 300 );
//
//                    startTime = System.nanoTime();
//                    BufferedImage workingImage = filter.filter(grayImage, "average", 3, 3, null, 1);//new int[]{1, 2, 1, 2, 3, 2, 1, 2, 1}, (1/15));
//                    System.out.println("avgerage Execution time in milliseconds : " + (System.nanoTime() - startTime) / 1000000);
//                    output_file = new File("average.jpg");//"avg - cell1Gray.jpg");
//                    ImageIO.write(workingImage, "jpg", output_file);
//
//                    startTime = System.nanoTime();
//                    workingImage = filter.filter(grayImage, "median", 3, 3, null, 1);//new int[]{1, 2, 1, 2, 3, 2, 1, 2, 1}, (1/15));
//                    System.out.println("median Execution time in milliseconds : " + (System.nanoTime() - startTime) / 1000000);
//                    output_file = new File("median.jpg");//"avg - cell1Gray.jpg");
//                    ImageIO.write(workingImage, "jpg", output_file);

                } catch (Exception e) {
                    System.out.println("Error: " + e);
                }
            }
            // TODO add error handling if not a file
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