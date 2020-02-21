import java.io.File;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
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

        String color = "gray";
        int meanSquaredError = 0;
        int[] averageHistogram = new int[256];
        File path = new File(imageFilesLocation); // TODO like a try catch or such
        File [] files = path.listFiles();

        for (int i = 0; i < files.length; i++){

            if (i >= 1)
                break;

            if (files[i].isFile()) { //this line weeds out other directories/folders
                System.out.println(files[i]);
                try {

                    // Make directory for the current cell image file.
                    int startOfPictureName = files[i].toString().lastIndexOf("/");
                    if (startOfPictureName == -1) {
                        startOfPictureName = files[i].toString().lastIndexOf("\\");
                    }
                    int endOfPictureName = files[i].toString().length() - 4; // remove the ".BMP" from the directory name
                    String directoryPath = "results/" + files[i].toString().substring(startOfPictureName + 1,endOfPictureName) + "/";
//                    System.out.println("directoryPath: " + directoryPath);
                    new File(directoryPath).mkdirs();

                    if (!new File(directoryPath).exists()) {
                        System.out.println("Could not create specified directory for image: " + files[i].toString() + ". Skipping this image.");
                        continue;
                    }


                    final BufferedImage originalImage = ImageIO.read(files[i]);
                    File output_file = new File(directoryPath + "original.jpg");//"avg - cell1Gray.jpg");
                    ImageIO.write(originalImage, "jpg", output_file);

                    // TODO which is better gray?
//                    BufferedImage singleColorImage = grayScale.convertToGrayScale(originalImage);
//                    output_file = new File("grayscale.jpg");//"avg - cell1Gray.jpg");
//                    ImageIO.write(singleColorImage, "jpg", output_file);

                    GrayScale grayScale = new GrayScale(originalImage, color);
                    BufferedImage singleColorImage = grayScale.convertToSingleColor();
                    output_file = new File(directoryPath + color + ".jpg");//"avg - cell1Gray.jpg");
                    ImageIO.write(singleColorImage, "jpg", output_file);



                    NoiseAdder noiseAdder = new NoiseAdder(singleColorImage, "saltAndPepper",0.05, 0, 0);
                    BufferedImage saltAndPepperImage = noiseAdder.addNoise();
                    output_file = new File(directoryPath + "saltAndPepper.jpg");//"avg - cell1Gray.jpg");
                    ImageIO.write(saltAndPepperImage, "jpg", output_file);

                    noiseAdder = new NoiseAdder(singleColorImage, "gaussian", 0, 0, 5);
                    BufferedImage gaussianImage = noiseAdder.addNoise();
                    output_file = new File(directoryPath + "gaussian.jpg");//"avg - cell1Gray.jpg");
                    ImageIO.write(gaussianImage, "jpg", output_file);



                    long startTime = System.nanoTime();
                    GraphHistogram graphHistogram = new GraphHistogram();
                    int[] histogram = graphHistogram.createHistogram(singleColorImage);
                    System.out.println("Histogram creation Execution time in milliseconds : " + (System.nanoTime() - startTime) / 1000000);

                    HistogramFunctions histogramFunctions = new HistogramFunctions(singleColorImage, histogram);
                    BufferedImage equalizedImage = histogramFunctions.equalizedImage();
                    output_file = new File(directoryPath + "equalizedImage.jpg");//"avg - cell1Gray.jpg");
                    ImageIO.write(equalizedImage, "jpg", output_file);

                    // print the starting histogram to a file
                    String graphTitle = "histogram";//files[i].toString(); // TODO looks shitty rn
                    JFreeChart defaultHistogram = graphHistogram.graphHistogram(histogram, graphTitle);
                    String pathName = graphTitle + ".png";
                    ChartUtilities.saveChartAsPNG(new File(directoryPath + pathName), defaultHistogram, 700 , 500 );

                    averageHistogram = histogramFunctions.sumHistograms(averageHistogram);

                    startTime = System.nanoTime();
                    histogram = graphHistogram.createHistogram(equalizedImage);
                    System.out.println("Equalized Histogram creation Execution time in milliseconds : " + (System.nanoTime() - startTime) / 1000000);

                    // print the equalized histogram to a file
                    graphTitle = "equalizedHistogram";//files[i].toString(); // TODO looks shitty rn
                    defaultHistogram = graphHistogram.graphHistogram(histogram, graphTitle);
                    pathName = graphTitle + ".png";
                    ChartUtilities.saveChartAsPNG(new File(directoryPath + pathName), defaultHistogram, 700 , 500 );



                    Filter filter = new Filter(singleColorImage, "average", 3, 3, null, 1);//new int[]{1, 2, 1, 2, 3, 2, 1, 2, 1}, (1/15));
                    BufferedImage avgFilterImage = filter.filter();
                    output_file = new File(directoryPath + "average.jpg");//"avg - cell1Gray.jpg");
                    ImageIO.write(avgFilterImage, "jpg", output_file);

                    filter = new Filter(singleColorImage, "median", 3, 3, null, 1);//new int[]{1, 2, 1, 2, 3, 2, 1, 2, 1}, (1/15));
                    BufferedImage medFilterImage = filter.filter();
                    output_file = new File(directoryPath + "median.jpg");//"avg - cell1Gray.jpg");
                    ImageIO.write(medFilterImage, "jpg", output_file);


                    Quantization quantization = new Quantization(singleColorImage, 16, color); // NOTE works nicer with a scale that is a factor of 2
                    BufferedImage quantizationImage = quantization.quantization();
                    output_file = new File(directoryPath + "quantization.jpg");//"avg - cell1Gray.jpg");
                    ImageIO.write(quantizationImage, "jpg", output_file);
                    meanSquaredError += quantization.getMeanSquaredError(quantizationImage);

                } catch (Exception e) {
                    System.out.println("Error: " + e);
                }
            }
            getAverageHistogram(averageHistogram, files.length);
            System.out.println("total meanSquaredError: " + meanSquaredError);
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

//    Averaged histograms of pixel values for each class of images.
//    • Histogram equalization for each image.
//    • Selected image quantization technique for user-specified levels.
//    • Display the following performance measures
//      o Processing time for the entire batch per each procedure
//      o Averaged processing time per image per each procedure
//      o MSQE for image quantization levels
}