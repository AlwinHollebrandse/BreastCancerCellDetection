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

        String color = "gray";
        int singleColorTime = 0;
        int saltAndPepperTime = 0;
        int gaussianTime = 0;
        int histogramTime = 0;
        int eqaulizationTime = 0;
        int linearFilterTime = 0;
        int medianFilterTime = 0;
        int quantizationTime = 0;

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
                    File output_file = new File(directoryPath + "original.jpg");
                    ImageIO.write(originalImage, "jpg", output_file);

                    // TODO which is better gray?
//                    BufferedImage singleColorImage = grayScale.convertToGrayScale(originalImage);
//                    output_file = new File("grayscale.jpg");//"avg - cell1Gray.jpg");
//                    ImageIO.write(singleColorImage, "jpg", output_file);

                    long startTime = System.nanoTime();
                    GrayScale grayScale = new GrayScale(originalImage, color);
                    BufferedImage singleColorImage = grayScale.convertToSingleColor();
                    long time = (System.nanoTime() - startTime) / 1000000;
                    singleColorTime += time;
                    output_file = new File(directoryPath + color + ".jpg");
                    ImageIO.write(singleColorImage, "jpg", output_file);
                    System.out.println("Converting to GrayScale" + " Execution time in milliseconds : " + time);



                    long startTime = System.nanoTime();
                    NoiseAdder noiseAdder = new NoiseAdder(singleColorImage, "saltAndPepper",0.05, 0, 0);
                    BufferedImage saltAndPepperImage = noiseAdder.addNoise();
                    long time = (System.nanoTime() - startTime) / 1000000;
                    saltAndPepperTime += time;
                    output_file = new File(directoryPath + "saltAndPepper.jpg");
                    ImageIO.write(saltAndPepperImage, "jpg", output_file);
                    System.out.println("Adding Salt and Pepper Noise" + " Execution time in milliseconds : " + time);

                    long startTime = System.nanoTime();
                    noiseAdder = new NoiseAdder(singleColorImage, "gaussian", 0, 0, 5);
                    BufferedImage gaussianImage = noiseAdder.addNoise();
                    long time = (System.nanoTime() - startTime) / 1000000;
                    gaussianTime += time;
                    output_file = new File(directoryPath + "gaussian.jpg");
                    ImageIO.write(gaussianImage, "jpg", output_file);
                    System.out.println("Adding Gaussian Noise" + " Execution time in milliseconds : " + time);




                    long startTime = System.nanoTime();
                    GraphHistogram graphHistogram = new GraphHistogram();
                    int[] histogram = graphHistogram.createHistogram(singleColorImage);
                    long time = (System.nanoTime() - startTime) / 1000000;
                    histogramTime += time;
                    System.out.println("Histogram creation Execution time in milliseconds : " + time);

                    // print the starting histogram to a file
                    String graphTitle = "histogram";
                    JFreeChart defaultHistogram = graphHistogram.graphHistogram(histogram, graphTitle);
                    String pathName = graphTitle + ".png";
                    ChartUtilities.saveChartAsPNG(new File(directoryPath + pathName), defaultHistogram, 700 , 500 );
                    HistogramFunctions histogramFunctions = new HistogramFunctions(singleColorImage, histogram);
                    averageHistogram = histogramFunctions.sumHistograms(averageHistogram);


                    long startTime = System.nanoTime();
                    HistogramFunctions histogramFunctions = new HistogramFunctions(singleColorImage, histogram);
                    BufferedImage equalizedImage = histogramFunctions.equalizedImage();
                    long time = (System.nanoTime() - startTime) / 1000000;
                    eqaulizationTime += time;
                    GraphHistogram graphHistogram = new GraphHistogram();
                    histogram = graphHistogram.createHistogram(equalizedImage);
                    output_file = new File(directoryPath + "equalizedImage.jpg");
                    ImageIO.write(equalizedImage, "jpg", output_file);
                    System.out.println("histogram equalization" + " Execution time in milliseconds : " + time);

                    // print the equalized histogram to a file
                    String graphTitle = "equalizedHistogram";
                    JFreeChart defaultHistogram = graphHistogram.graphHistogram(histogram, graphTitle);
                    String pathName = graphTitle + ".png";
                    ChartUtilities.saveChartAsPNG(new File(directoryPath + pathName), defaultHistogram, 700 , 500 );


                    long startTime = System.nanoTime();
                    Filter filter = new Filter(singleColorImage, "average", 3, 3, null, 1);//new int[]{1, 2, 1, 2, 3, 2, 1, 2, 1}, (1/15));
                    BufferedImage avgFilterImage = filter.filter();
                    long time = (System.nanoTime() - startTime) / 1000000;
                    linearFilterTime += time;
                    output_file = new File(directoryPath + "average.jpg");
                    ImageIO.write(avgFilterImage, "jpg", output_file);
                    System.out.println("Average Filter" + " Execution time in milliseconds : " + time);


                    long startTime = System.nanoTime();
                    filter = new Filter(singleColorImage, "median", 3, 3, null, 1);//new int[]{1, 2, 1, 2, 3, 2, 1, 2, 1}, (1/15));
                    BufferedImage medFilterImage = filter.filter();
                    long time = (System.nanoTime() - startTime) / 1000000;
                    medianFilterTime += time;
                    output_file = new File(directoryPath + "median.jpg");
                    ImageIO.write(medFilterImage, "jpg", output_file);
                    System.out.println("Median Filter" + " Execution time in milliseconds : " + time);




                    long startTime = System.nanoTime();
                    Quantization quantization = new Quantization(singleColorImage, 16, color); // NOTE works nicer with a scale that is a factor of 2
                    BufferedImage quantizationImage = quantization.quantization();
                    output_file = new File(directoryPath + "quantization.jpg");
                    long time = (System.nanoTime() - startTime) / 1000000;
                    quantizationTime += time;
                    ImageIO.write(quantizationImage, "jpg", output_file);
                    meanSquaredError += quantization.getMeanSquaredError(quantizationImage);
                    System.out.println("Quantization" + " Execution time in milliseconds : " + time);

                } catch (Exception e) {
                    System.out.println("Error: " + e);
                }
            }
            getAverageHistogram(averageHistogram, files.length);
            System.out.println("total meanSquaredError: " + meanSquaredError);

//            o Averaged processing time per image per each procedure // TODO what
            if (singleColorTime > 0) {
                System.out.println("Converting to a single color processing time for the entire batch: " + singleColorTime);
//                System.out.println("Converting to a single color processing time for the entire batch: " + singleColorTime); // TODO something with the other metric?
            }
            if (saltAndPepperTime > 0) {
                System.out.println("Adding salt and pepper noise processing time for the entire batch: " + saltAndPepperTime);
//                System.out.println("Converting to a single color processing time for the entire batch: " + saltAndPepperTime); // TODO something with the other metric?
            }
            if (gaussianTime > 0) {
                System.out.println("Adding gaussian noise processing time for the entire batch: " + gaussianTime);
//                System.out.println("Converting to a single color processing time for the entire batch: " + gaussianTime); // TODO something with the other metric?
            }
            if (histogramTime > 0) {
                System.out.println("Histogram creation processing time for the entire batch: " + histogramTime);
//                System.out.println("Converting to a single color processing time for the entire batch: " + histogramTime); // TODO something with the other metric?
            }
            if (eqaulizationTime > 0) {
                System.out.println("Equalized histogram creation processing time for the entire batch: " + eqaulizationTime);
//                System.out.println("Converting to a single color processing time for the entire batch: " + eqaulizationTime); // TODO something with the other metric?
            }
            if (linearFilterTime > 0) {
                System.out.println("Linear filter processing time for the entire batch: " + linearFilterTime);
//                System.out.println("Converting to a single color processing time for the entire batch: " + linearFilterTime); // TODO something with the other metric?
            }
            if (medianFilterTime > 0) {
                System.out.println("Median filter processing time for the entire batch: " + medianFilterTime);
//                System.out.println("Converting to a single color processing time for the entire batch: " + medianFilterTime); // TODO something with the other metric?
            }
            if (quantizationTime > 0) {
                System.out.println("Quantization processing time for the entire batch: " + quantizationTime);
//                System.out.println("Converting to a single color processing time for the entire batch: " + quantizationTime); // TODO something with the other metric?
            }

            // TODO add error handling if not a file

        }
    }


    private static void getAverageHistogram(int[] histogram, int divisor) {
        for (int i = 0; i < histogram.length; i++) {
            histogram[i] /= divisor;
        }

        try {
            GraphHistogram graphHistogram = new GraphHistogram();
            String graphTitle = "averageHistogram";
            JFreeChart defaultHistogram = graphHistogram.graphHistogram(histogram, graphTitle);
            String pathName = "results/finalReport/" + graphTitle + ".png";
            ChartUtilities.saveChartAsPNG(new File(pathName), defaultHistogram, 700, 500);
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }
}