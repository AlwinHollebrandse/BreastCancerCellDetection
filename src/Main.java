import java.io.File;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import javax.imageio.ImageIO;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartUtilities;

public class Main {

    //Takes in 2 command line args: the file containing all of the images, and the file containing the instructions
    public static void main(String args[]) {

        String imageFilesLocation = args[0];
        String instructions = args[1];

        System.out.println("imageFilesLocation: " + imageFilesLocation);
        System.out.println("instructions: " + instructions);

        String resultFileName;
        String color = "gray";
        int singleColorTime = 0;
        int quantizationTime = 0;
        int saltAndPepperTime = 0;
        int gaussianTime = 0;
        int linearFilterTime = 0;
        int medianFilterTime = 0;
        int histogramTime = 0;
        int equalizationTime = 0;
        int meanSquaredError = 0;
        int[] averageHistogram = new int[256];

        long realStartTime = System.nanoTime();

        // the file containing all of the images error prevention
        File path = new File(imageFilesLocation);
        if (!path.exists()) {
            System.out.println("Enter a valid path that holds the images");
            System.exit(1);
        }
        File [] files = path.listFiles();
        if (files.length <= 0) {
            System.out.println("The provided image folder has no images");
            System.exit(1);
        }

        // the file containing the instructions logic
        ArrayList<String> instructionList = new ArrayList<>();
        try {
            Scanner s = new Scanner(new File(instructions));
            while (s.hasNext()) {
                instructionList.add(s.next());
            }
            s.close();
        } catch (IOException ex) {
            System.out.println("Enter a valid path that has operation instructions");
            System.exit(1);
        }
        if (instructionList.size() <= 0) {
            System.out.println("The provided instruction file has no operation instructions");
            System.exit(1);
        }

        ProgressBar progressBar = new ProgressBar("Processing Images", files.length);
        // loop through all images and do each specified operation
        for (int i = 0; i < files.length; i++){

//            if (i >= 1)
//                break;

            if (files[i].isFile()) { //this line weeds out other directories/folders
//                System.out.println("\n\n" + files[i]);
                try {

                    // Make directory for the current cell image file.
                    int startOfPictureName = files[i].toString().lastIndexOf("/");
                    if (startOfPictureName == -1) {
                        startOfPictureName = files[i].toString().lastIndexOf("\\");
                    }
                    int endOfPictureName = files[i].toString().length() - 4; // remove the ".BMP" from the directory name
                    String directoryPath = "results/" + files[i].toString().substring(startOfPictureName + 1,endOfPictureName) + "/";
//                    System.out.println("directoryPath: " + directoryPath);

                    File imageResults = new File(directoryPath);

                    // (if needed) make a new output folder for the current imagee.
                    imageResults.mkdirs();
                    if (!new File(directoryPath).exists()) {
                        System.out.println("Could not create specified directory for image: " + files[i].toString() + ". Skipping this image.");
                        continue;
                    }

                    //set up global vars for all image operations
                    int[] histogram = null;
                    BufferedImage workingImage;

                    // add the original image to the relevant result folder
                    final BufferedImage originalImage = ImageIO.read(files[i]);
                    workingImage = originalImage;// TODO does this pass by refrecen?
                    File output_file = new File(directoryPath + "original.jpg");
                    ImageIO.write(originalImage, "jpg", output_file);


                    ///////////////////////////////////////////// PERFORM REQUESTED IMAGE OPERATIONS ////////////////////////////////////////////////////////

                    resultFileName = directoryPath + color + ".jpg";
                    if (instructionList.contains("SingleColor")) {
                        // TODO which is better gray?
//                    BufferedImage singleColorImage = grayScale.convertToGrayScale(originalImage);
//                    output_file = new File("grayscale.jpg");
//                    ImageIO.write(singleColorImage, "jpg", output_file);

                        long startTime = System.nanoTime();
                        SingleColorScale singleColorScale = new SingleColorScale(originalImage, color);
                        workingImage = singleColorScale.convertToSingleColor();
                        long time = (System.nanoTime() - startTime) / 1000000;
                        singleColorTime += time;
                        output_file = new File(resultFileName);
                        ImageIO.write(workingImage, "jpg", output_file);
//                        System.out.println("Converting to GrayScale" + " Execution time in milliseconds : " + time);
                    } else {
                        // if the file was not needed, delete the file from the relevant result folder if it existed
                        File imageResult = new File(resultFileName);
                        // delete the image result folder if it already existed. This way, no remnants from old runs remain
                        if (imageResult.exists()) {
                            if (!deleteDir(imageResult)) {
                                System.out.println("\nCould not delete old SingleColor of: " + files[i].toString());
                            }
                        }
                    }


                    resultFileName = directoryPath + "quantization.jpg";
                    if (instructionList.contains("Quantization")) {
                        long startTime = System.nanoTime();
                        Quantization quantization = new Quantization(workingImage, 16, color); // NOTE works nicer with a scale that is a factor of 2
                        workingImage = quantization.quantization();
                        output_file = new File(resultFileName);
                        long time = (System.nanoTime() - startTime) / 1000000;
                        quantizationTime += time;
                        ImageIO.write(workingImage, "jpg", output_file);
                        meanSquaredError += quantization.getMeanSquaredError(workingImage);
//                        System.out.println("Quantization" + " Execution time in milliseconds : " + time);
                    } else {
                        // if the file was not needed, delete the file from the relevant result folder if it existed
                        File imageResult = new File(resultFileName);
                        // delete the image result folder if it already existed. This way, no remnants from old runs remain
                        if (imageResult.exists()) {
                            if (!deleteDir(imageResult)) {
                                System.out.println("\nCould not delete old Quantization of: " + files[i].toString());
                            }
                        }
                    }


                    resultFileName = directoryPath + "saltAndPepper.jpg";
                    if (instructionList.contains("SaltAndPepper")) {
                        long startTime = System.nanoTime();
                        NoiseAdder noiseAdder = new NoiseAdder(workingImage, "saltAndPepper", 0.05, 0, 0); // TODO do these params come from the instruction file?
                        workingImage = noiseAdder.addNoise();
                        long time = (System.nanoTime() - startTime) / 1000000;
                        saltAndPepperTime += time;
                        output_file = new File(resultFileName);
                        ImageIO.write(workingImage, "jpg", output_file);
//                        System.out.println("Adding Salt and Pepper Noise" + " Execution time in milliseconds : " + time);
                    } else {
                        // if the file was not needed, delete the file from the relevant result folder if it existed
                        File imageResult = new File(resultFileName);
                        // delete the image result folder if it already existed. This way, no remnants from old runs remain
                        if (imageResult.exists()) {
                            if (!deleteDir(imageResult)) {
                                System.out.println("\nCould not delete old SaltAndPepperNoise of: " + files[i].toString());
                            }
                        }
                    }


                    resultFileName = directoryPath + "gaussian.jpg";
                    if (instructionList.contains("Gaussian")) {
                        long startTime = System.nanoTime();
                        NoiseAdder noiseAdder = new NoiseAdder(workingImage, "gaussian", 0, 0, 5);
                        workingImage = noiseAdder.addNoise();
                        long time = (System.nanoTime() - startTime) / 1000000;
                        gaussianTime += time;
                        output_file = new File(resultFileName);
                        ImageIO.write(workingImage, "jpg", output_file);
//                        System.out.println("Adding Gaussian Noise" + " Execution time in milliseconds : " + time);
                    } else {
                        // if the file was not needed, delete the file from the relevant result folder if it existed
                        File imageResult = new File(resultFileName);
                        // delete the image result folder if it already existed. This way, no remnants from old runs remain
                        if (imageResult.exists()) {
                            if (!deleteDir(imageResult)) {
                                System.out.println("\nCould not delete old GaussianNoise of: " + files[i].toString());
                            }
                        }
                    }


                    resultFileName = directoryPath + "linear.jpg";
                    if (instructionList.contains("LinearFilter")) {
                        long startTime = System.nanoTime();
                        Filter filter = new Filter(workingImage, "linear", 3, 3, null, 1);//new int[]{1, 2, 1, 2, 3, 2, 1, 2, 1}, (1/15));
                        workingImage = filter.filter();
                        long time = (System.nanoTime() - startTime) / 1000000;
                        linearFilterTime += time;
                        output_file = new File(resultFileName);
                        ImageIO.write(workingImage, "jpg", output_file);
//                        System.out.println("Average Filter" + " Execution time in milliseconds : " + time);
                    } else {
                        // if the file was not needed, delete the file from the relevant result folder if it existed
                        File imageResult = new File(resultFileName);
                        // delete the image result folder if it already existed. This way, no remnants from old runs remain
                        if (imageResult.exists()) {
                            if (!deleteDir(imageResult)) {
                                System.out.println("\nCould not delete old LinearFilter of: " + files[i].toString());
                            }
                        }
                    }


                    resultFileName = directoryPath + "median.jpg";
                    if (instructionList.contains("MedianFilter")) {
                        long startTime = System.nanoTime();
                        Filter filter = new Filter(workingImage, "median", 3, 3, null, 1);//new int[]{1, 2, 1, 2, 3, 2, 1, 2, 1}, (1/15));
                        workingImage = filter.filter();
                        long time = (System.nanoTime() - startTime) / 1000000;
                        medianFilterTime += time;
                        output_file = new File(resultFileName);
                        ImageIO.write(workingImage, "jpg", output_file);
//                        System.out.println("Median Filter" + " Execution time in milliseconds : " + time);
                    } else {
                        // if the file was not needed, delete the file from the relevant result folder if it existed
                        File imageResult = new File(resultFileName);
                        // delete the image result folder if it already existed. This way, no remnants from old runs remain
                        if (imageResult.exists()) {
                            if (!deleteDir(imageResult)) {
                                System.out.println("\nCould not delete old MedianFilter of: " + files[i].toString());
                            }
                        }
                    }


                    String graphTitle = "histogram";
                    resultFileName = directoryPath + graphTitle + ".png";
                    if (instructionList.contains("Histogram")) {
                        long startTime = System.nanoTime();
                        GraphHistogram graphHistogram = new GraphHistogram();
                        histogram = graphHistogram.createHistogram(workingImage);
                        long time = (System.nanoTime() - startTime) / 1000000;
                        histogramTime += time;
//                        System.out.println("Histogram creation Execution time in milliseconds : " + time);

                        // print the starting histogram to a file
                        JFreeChart histogramGraph = graphHistogram.graphHistogram(histogram, graphTitle);
                        ChartUtilities.saveChartAsPNG(new File(resultFileName), histogramGraph, 700, 500);
                        HistogramFunctions histogramFunctions = new HistogramFunctions(workingImage, histogram);
                        averageHistogram = histogramFunctions.sumHistograms(averageHistogram);
                    } else {
                        // if the file was not needed, delete the file from the relevant result folder if it existed
                        File imageResult = new File(resultFileName);
                        // delete the image result folder if it already existed. This way, no remnants from old runs remain
                        if (imageResult.exists()) {
                            if (!deleteDir(imageResult)) {
                                System.out.println("\nCould not delete old Histogram of: " + files[i].toString());
                            }
                        }
                    }


                    resultFileName = directoryPath + "equalizedImage.jpg";
                    graphTitle = "equalizedHistogram";
                    if (instructionList.contains("HistogramEqualization")) {
                        if (histogram == null) {
                            GraphHistogram graphHistogram = new GraphHistogram();
                            histogram = graphHistogram.createHistogram(workingImage);
                        }
                        long startTime = System.nanoTime();
                        HistogramFunctions histogramFunctions = new HistogramFunctions(workingImage, histogram);
                        workingImage = histogramFunctions.equalizedImage();
                        long time = (System.nanoTime() - startTime) / 1000000;
                        equalizationTime += time;
                        GraphHistogram graphHistogram = new GraphHistogram();
                        int[] equalizedHistogram = graphHistogram.createHistogram(workingImage);
                        output_file = new File(resultFileName);
                        ImageIO.write(workingImage, "jpg", output_file);
//                        System.out.println("histogram equalization" + " Execution time in milliseconds : " + time);

                        // print the equalized histogram to a file
                        JFreeChart equalizedHistogramGraph = graphHistogram.graphHistogram(equalizedHistogram, graphTitle);
                        ChartUtilities.saveChartAsPNG(new File(directoryPath + graphTitle + ".png"), equalizedHistogramGraph, 700, 500);
                    } else {
                        // if the file was not needed, delete the file from the relevant result folder if it existed
                        File imageResult = new File(resultFileName);
                        // delete the image result folder if it already existed. This way, no remnants from old runs remain
                        if (imageResult.exists()) {
                            if (!deleteDir(imageResult)) {
                                System.out.println("\nCould not delete old HistogramEqualization of: " + files[i].toString());
                            }
                        }

                        // need to delete the histogram too, and not only the equalized image
                        imageResult = new File(directoryPath + graphTitle + ".png");
                        // delete the image result folder if it already existed. This way, no remnants from old runs remain
                        if (imageResult.exists()) {
                            if (!deleteDir(imageResult)) {
                                System.out.println("\nCould not delete old HistogramEqualization of: " + files[i].toString());
                            }
                        }
                    }

                } catch (Exception ex) {
                    System.out.println("\nThere was an error with image " + files[i].toString() + ": " + ex);
                    ex.printStackTrace();
                }
            }
            progressBar.next();
        }
        System.out.println("\n\nFinal Metrics:");
        getAverageHistogram(averageHistogram, files.length);
//            o Averaged processing time per image per each procedure // TODO what
        if (singleColorTime > 0) {// TODO write these to teh results final file?
            System.out.println("Converting to a single color processing time for the entire batch (ms): " + singleColorTime);
//                System.out.println("Converting to a single color processing time for the entire batch: " + singleColorTime); // TODO something with the other metric?
        }
        if (quantizationTime > 0) {
            System.out.println("Quantization processing time for the entire batch (ms): " + quantizationTime);
//                System.out.println("Converting to a single color processing time for the entire batch: " + quantizationTime); // TODO something with the other metric?\
            System.out.println("total meanSquaredError: " + meanSquaredError); // TODO whats an acceptable/expected one
        }
        if (saltAndPepperTime > 0) {
            System.out.println("Adding salt and pepper noise processing time for the entire batch (ms): " + saltAndPepperTime);
//                System.out.println("Converting to a single color processing time for the entire batch: " + saltAndPepperTime); // TODO something with the other metric?
        }
        if (gaussianTime > 0) {
            System.out.println("Adding gaussian noise processing time for the entire batch (ms): " + gaussianTime);
//                System.out.println("Converting to a single color processing time for the entire batch: " + gaussianTime); // TODO something with the other metric?
        }
        if (linearFilterTime > 0) {
            System.out.println("Linear filter processing time for the entire batch (ms): " + linearFilterTime);
//                System.out.println("Converting to a single color processing time for the entire batch: " + linearFilterTime); // TODO something with the other metric?
        }
        if (medianFilterTime > 0) {
            System.out.println("Median filter processing time for the entire batch (ms): " + medianFilterTime);
//                System.out.println("Converting to a single color processing time for the entire batch: " + medianFilterTime); // TODO something with the other metric?
        }
        if (histogramTime > 0) {
            System.out.println("Histogram creation processing time for the entire batch (ms): " + histogramTime);
//                System.out.println("Converting to a single color processing time for the entire batch: " + histogramTime); // TODO something with the other metric?
        }
        if (equalizationTime > 0) {
            System.out.println("Equalized histogram creation processing time for the entire batch (ms): " + equalizationTime);
//                System.out.println("Converting to a single color processing time for the entire batch: " + eqaulizationTime); // TODO something with the other metric?
        }
        System.out.println("Total RunTime (without image exporting): " + (singleColorTime + quantizationTime + saltAndPepperTime + gaussianTime + linearFilterTime + medianFilterTime + histogramTime + equalizationTime));
        System.out.println("Real run time: " + (System.nanoTime() - realStartTime) / 1000000);
    }


    private static void getAverageHistogram(int[] histogram, int divisor) {
        for (int i = 0; i < histogram.length; i++) {
            histogram[i] /= divisor;
        }

        try {
            GraphHistogram graphHistogram = new GraphHistogram();
            String graphTitle = "averageHistogram";
            JFreeChart defaultHistogram = graphHistogram.graphHistogram(histogram, graphTitle);
            String directoryPath = "results/finalReport/";
            new File(directoryPath).mkdirs();
            File output_file = new File(directoryPath + graphTitle + ".png");
            ChartUtilities.saveChartAsPNG(output_file, defaultHistogram, 700, 500);
        } catch (Exception e) {
            System.out.println("Could not create specified directory for the average histogram. Skipping this operation. Error: " + e);
        }
    }

    private static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            try {
                for (File myFile : files) {
                    if (myFile.isDirectory()) {
                        deleteDir(myFile);
                    }
                    myFile.delete();

                }
            } catch (Exception ex) {
                return false;
            }
            return true;
        }
        return dir.delete();
    }
}