import java.io.BufferedWriter;
import java.io.File;
import java.awt.image.BufferedImage;
import java.io.FileWriter;
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

        print("imageFilesLocation: " + imageFilesLocation);
        print("instructions: " + instructions);

        // operation params
        String resultFileName;
        String color = "gray";
        int quantizationScale = -1;
        double saltAndPepperNoiseRandomThreshold = -1;
        double saltAndPepperNoiseMean = -1;
        double saltAndPepperNoiseSigma = -1;
        double gaussianNoiseRandomThreshold = -1;
        double gaussianNoiseMean = -1;
        double gaussianNoiseSigma = -1;
        int linearFilterWidth = -1;
        int linearFilterHeight = -1;
        int[] linearFilterWeights = null;
        int medianFilterWidth = -1;
        int medianFilterHeight = -1;
        int[] medianFilterWeights = null;
        int erosionFilterWidth = -1;
        int erosionFilterHeight = -1;
        int[] erosionFilterColors = null;
        int dilationFilterWidth = -1;
        int dilationFilterHeight = -1;
        int[] dilationFilterColors = null;

        int singleColorTime = 0;
        int quantizationTime = 0;
        int saltAndPepperTime = 0;
        int gaussianTime = 0;
        int linearFilterTime = 0;
        int medianFilterTime = 0;
        int histogramTime = 0;
        int equalizationTime = 0;
        int edgeDetectionTime = 0;
        int histogramThresholdingSegmentationTime = 0;
        int kMeansSegmentationTime = 0;
        int erosionTime = 0;
        int dilationTime = 0;
        int meanSquaredError = 0;
        int[] averageHistogram = new int[256];

        long realStartTime = System.nanoTime();

        // the file containing all of the images error prevention
        File path = new File(imageFilesLocation);
        if (!path.exists()) {
            print("Enter a valid path that holds the images");
            System.exit(1);
        }
        File [] files = path.listFiles();
        if (files.length <= 0) {
            print("The provided image folder has no images");
            System.exit(1);
        }

        // the file containing the instructions logic
        ArrayList<String> instructionList = new ArrayList<>();
        try {
            Scanner s = new Scanner(new File(instructions));
            while (s.hasNextLine()) {
                String currentLine = s.nextLine();

                if (currentLine.toLowerCase().contains("singlecolor")) {
                    String[] lineArray = currentLine.split(" ");
                    instructionList.add("SingleColor");
                    color = lineArray[1];
                }

                if (currentLine.toLowerCase().contains("quantization")) {
                    String[] lineArray = currentLine.split(" ");
                    instructionList.add("Quantization");
                    quantizationScale = Integer.parseInt(lineArray[1]);
                }

                if (currentLine.toLowerCase().contains("saltandpepper")) {
                    String[] lineArray = currentLine.split(" ");
                    instructionList.add("SaltAndPepper");
                    saltAndPepperNoiseRandomThreshold = Double.parseDouble(lineArray[1]);
                    saltAndPepperNoiseMean = Double.parseDouble(lineArray[2]);
                    saltAndPepperNoiseSigma = Double.parseDouble(lineArray[3]);
                }

                if (currentLine.toLowerCase().contains("gaussian")) {
                    String[] lineArray = currentLine.split(" ");
                    instructionList.add("Gaussian");
                    gaussianNoiseRandomThreshold = Double.parseDouble(lineArray[1]);
                    gaussianNoiseMean = Double.parseDouble(lineArray[2]);
                    gaussianNoiseSigma = Double.parseDouble(lineArray[3]);
                }

                if (currentLine.toLowerCase().contains("linearfilter")) {
                    String[] lineArray = currentLine.split(" ");
                    instructionList.add("LinearFilter");
                    linearFilterWidth = Integer.parseInt(lineArray[1]);
                    linearFilterHeight = Integer.parseInt(lineArray[2]);
                    linearFilterWeights = parseArray(lineArray);
                }

                if (currentLine.toLowerCase().contains("medianfilter")) {
                    String[] lineArray = currentLine.split(" ");
                    instructionList.add("MedianFilter");
                    medianFilterWidth = Integer.parseInt(lineArray[1]);
                    medianFilterHeight = Integer.parseInt(lineArray[2]);
                    medianFilterWeights = parseArray(lineArray);
                }

                if (currentLine.toLowerCase().contains("histogram")) {
                    instructionList.add("Histogram");
                }

                if (currentLine.toLowerCase().contains("histogramequalization")) {
                    instructionList.add("HistogramEqualization");
                }

                if (currentLine.toLowerCase().contains("edgedetection")) {
                    instructionList.add("EdgeDetection");
                }

                if (currentLine.toLowerCase().contains("histogramthresholdingsegmentation")) {
                    instructionList.add("HistogramThresholdingSegmentation");
                }

                if (currentLine.toLowerCase().contains("kmeanssegmentation")) {
                    instructionList.add("KMeansSegmentation");
                }

                if (currentLine.toLowerCase().contains("erosion")) {
                    String[] lineArray = currentLine.split(" ");
                    instructionList.add("Erosion");
                    erosionFilterWidth = Integer.parseInt(lineArray[1]);
                    erosionFilterHeight = Integer.parseInt(lineArray[2]);
                    erosionFilterColors = parseArray(lineArray);
                }

                if (currentLine.toLowerCase().contains("dilation")) {
                    String[] lineArray = currentLine.split(" ");
                    instructionList.add("Dilation");
                    dilationFilterWidth = Integer.parseInt(lineArray[1]);
                    dilationFilterHeight = Integer.parseInt(lineArray[2]);
                    dilationFilterColors = parseArray(lineArray);
                }
            }
            s.close();
        } catch (IOException ex) {
            print("Enter a valid path that has operation instructions");
            System.exit(1);
        }
        if (instructionList.size() <= 0) {
            print("The provided instruction file has no operation instructions");
            System.exit(1);
        }





        ProgressBar progressBar = new ProgressBar("Processing Images", files.length);
        // loop through all images and do each specified operation
        for (int i = 0; i < files.length; i++){

//            if (i >= 1)
//                break;

            if (files[i].isFile()) { //this line weeds out other directories/folders
//                print("\n\n" + files[i]);
                try {

                    // Make directory for the current cell image file.
                    int startOfPictureName = files[i].toString().lastIndexOf("/");
                    if (startOfPictureName == -1) {
                        startOfPictureName = files[i].toString().lastIndexOf("\\");
                    }
                    int endOfPictureName = files[i].toString().length() - 4; // remove the ".BMP" from the directory name
                    String directoryPath = "results/" + files[i].toString().substring(startOfPictureName + 1, endOfPictureName) + "/";
//                    print("directoryPath: " + directoryPath);

                    File imageResults = new File(directoryPath);

                    // (if needed) make a new output folder for the current imagee.
                    imageResults.mkdirs();
                    if (!new File(directoryPath).exists()) {
                        print("Could not create specified directory for image: " + files[i].toString() + ". Skipping this image.");
                        continue;
                    }

                    //set up global vars for all image operations
                    int[] histogram = null;
                    BufferedImage workingImage;
                    BufferedImage edgeMap;
                    BufferedImage segmentationImage;

                    // add the original image to the relevant result folder
                    final BufferedImage originalImage = ImageIO.read(files[i]);
                    workingImage = originalImage;
                    edgeMap = originalImage;
                    segmentationImage = originalImage;
                    File output_file = new File(directoryPath + "original.jpg");
                    ImageIO.write(originalImage, "jpg", output_file);


                    ///////////////////////////////////////////// PERFORM REQUESTED IMAGE OPERATIONS ////////////////////////////////////////////////////////

                    resultFileName = directoryPath + color + ".jpg";
                    if (instructionList.contains("SingleColor")) {
                        long startTime = System.nanoTime();
                        SingleColorScale singleColorScale = new SingleColorScale(originalImage, color);
                        workingImage = singleColorScale.convertToSingleColor();
                        long time = (System.nanoTime() - startTime) / 1000000;
                        singleColorTime += time;
                        output_file = new File(resultFileName);
                        ImageIO.write(workingImage, "jpg", output_file);
//                        print("Converting to GrayScale" + " Execution time in milliseconds : " + time);
                    } else {
                        // if the file was not needed, delete the file from the relevant result folder if it existed
                        File imageResult = new File(resultFileName);
                        // delete the image result folder if it already existed. This way, no remnants from old runs remain
                        if (imageResult.exists()) {
                            if (!deleteDir(imageResult)) {
                                print("\nCould not delete old SingleColor of: " + files[i].toString());
                            }
                        }
                    }


                    resultFileName = directoryPath + "quantization.jpg";
                    if (instructionList.contains("Quantization")) {
                        if (quantizationScale == -1) {
                            print("Quantization parameter was set incorrectly");
                            System.exit(1);
                        }
                        long startTime = System.nanoTime();
                        Quantization quantization = new Quantization(workingImage, quantizationScale, color); // NOTE works nicer with a scale that is a factor of 2
                        workingImage = quantization.quantization();
                        output_file = new File(resultFileName);
                        long time = (System.nanoTime() - startTime) / 1000000;
                        quantizationTime += time;
                        ImageIO.write(workingImage, "jpg", output_file);
                        meanSquaredError += quantization.getMeanSquaredError(workingImage);
//                        print("Quantization" + " Execution time in milliseconds : " + time);
                    } else {
                        // if the file was not needed, delete the file from the relevant result folder if it existed
                        File imageResult = new File(resultFileName);
                        // delete the image result folder if it already existed. This way, no remnants from old runs remain
                        if (imageResult.exists()) {
                            if (!deleteDir(imageResult)) {
                                print("\nCould not delete old Quantization of: " + files[i].toString());
                            }
                        }
                    }


                    resultFileName = directoryPath + "saltAndPepper.jpg";
                    if (instructionList.contains("SaltAndPepper")) {
                        long startTime = System.nanoTime();
                        NoiseAdder noiseAdder = new NoiseAdder(workingImage, "saltAndPepper", saltAndPepperNoiseRandomThreshold, saltAndPepperNoiseMean, saltAndPepperNoiseSigma);
                        workingImage = noiseAdder.addNoise();
                        long time = (System.nanoTime() - startTime) / 1000000;
                        saltAndPepperTime += time;
                        output_file = new File(resultFileName);
                        ImageIO.write(workingImage, "jpg", output_file);
//                        print("Adding Salt and Pepper Noise" + " Execution time in milliseconds : " + time);
                    } else {
                        // if the file was not needed, delete the file from the relevant result folder if it existed
                        File imageResult = new File(resultFileName);
                        // delete the image result folder if it already existed. This way, no remnants from old runs remain
                        if (imageResult.exists()) {
                            if (!deleteDir(imageResult)) {
                                print("\nCould not delete old SaltAndPepperNoise of: " + files[i].toString());
                            }
                        }
                    }


                    resultFileName = directoryPath + "gaussian.jpg";
                    if (instructionList.contains("Gaussian")) {
                        long startTime = System.nanoTime();
                        NoiseAdder noiseAdder = new NoiseAdder(workingImage, "gaussian", gaussianNoiseRandomThreshold, gaussianNoiseMean, gaussianNoiseSigma);
                        workingImage = noiseAdder.addNoise();
                        long time = (System.nanoTime() - startTime) / 1000000;
                        gaussianTime += time;
                        output_file = new File(resultFileName);
                        ImageIO.write(workingImage, "jpg", output_file);
//                        print("Adding Gaussian Noise" + " Execution time in milliseconds : " + time);
                    } else {
                        // if the file was not needed, delete the file from the relevant result folder if it existed
                        File imageResult = new File(resultFileName);
                        // delete the image result folder if it already existed. This way, no remnants from old runs remain
                        if (imageResult.exists()) {
                            if (!deleteDir(imageResult)) {
                                print("\nCould not delete old GaussianNoise of: " + files[i].toString());
                            }
                        }
                    }


                    resultFileName = directoryPath + "linear.jpg";
                    if (instructionList.contains("LinearFilter")) {
                        if (linearFilterWidth == -1 || linearFilterHeight == -1) {
                            print("linear filter parameters were set incorrectly");
                            System.exit(1);
                        }
                        long startTime = System.nanoTime();
                        Filter filter = new Filter(workingImage, "linear", linearFilterWidth, linearFilterHeight, linearFilterWeights);//new int[]{1, 2, 1, 2, 3, 2, 1, 2, 1}, (1/15));
                        workingImage = filter.filter();
                        long time = (System.nanoTime() - startTime) / 1000000;
                        linearFilterTime += time;
                        output_file = new File(resultFileName);
                        ImageIO.write(workingImage, "jpg", output_file);
//                        print("Linear Filter" + " Execution time in milliseconds : " + time);
                    } else {
                        // if the file was not needed, delete the file from the relevant result folder if it existed
                        File imageResult = new File(resultFileName);
                        // delete the image result folder if it already existed. This way, no remnants from old runs remain
                        if (imageResult.exists()) {
                            if (!deleteDir(imageResult)) {
                                print("\nCould not delete old LinearFilter of: " + files[i].toString());
                            }
                        }
                    }


                    resultFileName = directoryPath + "median.jpg";
                    if (instructionList.contains("MedianFilter")) {
                        if (medianFilterWidth == -1 || medianFilterHeight == -1) {
                            print("median filter parameters were set incorrectly");
                            System.exit(1);
                        }
                        long startTime = System.nanoTime();
                        Filter filter = new Filter(workingImage, "median", medianFilterWidth, medianFilterHeight, medianFilterWeights);//new int[]{1, 2, 1, 2, 3, 2, 1, 2, 1}, (1/15.0));
                        workingImage = filter.filter();
                        long time = (System.nanoTime() - startTime) / 1000000;
                        medianFilterTime += time;
                        output_file = new File(resultFileName);
                        ImageIO.write(workingImage, "jpg", output_file);
//                        print("Median Filter" + " Execution time in milliseconds : " + time);
                    } else {
                        // if the file was not needed, delete the file from the relevant result folder if it existed
                        File imageResult = new File(resultFileName);
                        // delete the image result folder if it already existed. This way, no remnants from old runs remain
                        if (imageResult.exists()) {
                            if (!deleteDir(imageResult)) {
                                print("\nCould not delete old MedianFilter of: " + files[i].toString());
                            }
                        }
                    }


                    String graphTitle = "histogram";
                    resultFileName = directoryPath + graphTitle + ".png";
                    if (instructionList.contains("Histogram")) {
                        long startTime = System.nanoTime();
                        GraphHistogram graphHistogram = new GraphHistogram(color);
                        histogram = graphHistogram.createHistogram(workingImage);
                        long time = (System.nanoTime() - startTime) / 1000000;
                        histogramTime += time;
//                        print("Histogram creation Execution time in milliseconds : " + time);

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
                                print("\nCould not delete old Histogram of: " + files[i].toString());
                            }
                        }
                    }


                    resultFileName = directoryPath + "equalizedImage.jpg";
                    graphTitle = "equalizedHistogram";
                    if (instructionList.contains("HistogramEqualization")) {
                        if (histogram == null) {
                            GraphHistogram graphHistogram = new GraphHistogram(color);
                            histogram = graphHistogram.createHistogram(workingImage);
                        }
                        long startTime = System.nanoTime();
                        HistogramFunctions histogramFunctions = new HistogramFunctions(workingImage, histogram);
                        workingImage = histogramFunctions.equalizedImage();
                        long time = (System.nanoTime() - startTime) / 1000000;
                        equalizationTime += time;
                        GraphHistogram graphHistogram = new GraphHistogram(color);
                        int[] equalizedHistogram = graphHistogram.createHistogram(workingImage);
                        output_file = new File(resultFileName);
                        ImageIO.write(workingImage, "jpg", output_file);
//                        print("histogram equalization" + " Execution time in milliseconds : " + time);

                        // print the equalized histogram to a file
                        JFreeChart equalizedHistogramGraph = graphHistogram.graphHistogram(equalizedHistogram, graphTitle);
                        ChartUtilities.saveChartAsPNG(new File(directoryPath + graphTitle + ".png"), equalizedHistogramGraph, 700, 500);
                    } else {
                        // if the file was not needed, delete the file from the relevant result folder if it existed
                        File imageResult = new File(resultFileName);
                        // delete the image result folder if it already existed. This way, no remnants from old runs remain
                        if (imageResult.exists()) {
                            if (!deleteDir(imageResult)) {
                                print("\nCould not delete old HistogramEqualization of: " + files[i].toString());
                            }
                        }

                        // need to delete the histogram too, and not only the equalized image
                        imageResult = new File(directoryPath + graphTitle + ".png");
                        // delete the image result folder if it already existed. This way, no remnants from old runs remain
                        if (imageResult.exists()) {
                            if (!deleteDir(imageResult)) {
                                print("\nCould not delete old HistogramEqualization of: " + files[i].toString());
                            }
                        }
                    }

                    resultFileName = directoryPath + "edgeDetection.jpg";
                    if (instructionList.contains("EdgeDetection")) {
                        long startTime = System.nanoTime();
                        EdgeDetection edgeDetection = new EdgeDetection();
                        edgeMap = edgeDetection.edgeDetection(workingImage);
                        long time = (System.nanoTime() - startTime) / 1000000;
                        edgeDetectionTime += time;
                        output_file = new File(resultFileName);
                        ImageIO.write(edgeMap, "jpg", output_file);
//                        print("Edge Detection" + " Execution time in milliseconds : " + time);
                    } else {
                        // if the file was not needed, delete the file from the relevant result folder if it existed
                        File imageResult = new File(resultFileName);
                        // delete the image result folder if it already existed. This way, no remnants from old runs remain
                        if (imageResult.exists()) {
                            if (!deleteDir(imageResult)) {
                                print("\nCould not delete old edgeDetection of: " + files[i].toString());
                            }
                        }
                    }

                    resultFileName = directoryPath + "histogramThresholdingSegmentation.jpg";
                    if (instructionList.contains("HistogramThresholdingSegmentation")) {
                        if (histogram == null) {
                            GraphHistogram graphHistogram = new GraphHistogram(color);
                            histogram = graphHistogram.createHistogram(workingImage);
                        }
                        long startTime = System.nanoTime();
                        ThresholdSegmentation thresholdSegmentation = new ThresholdSegmentation(workingImage, histogram);
                        segmentationImage = thresholdSegmentation.thresholdSegmentation();
                        long time = (System.nanoTime() - startTime) / 1000000;
                        histogramThresholdingSegmentationTime += time;
                        output_file = new File(resultFileName);
                        ImageIO.write(segmentationImage, "jpg", output_file);
//                        print("Edge Detection" + " Execution time in milliseconds : " + time);
                    } else {
                        // if the file was not needed, delete the file from the relevant result folder if it existed
                        File imageResult = new File(resultFileName);
                        // delete the image result folder if it already existed. This way, no remnants from old runs remain
                        if (imageResult.exists()) {
                            if (!deleteDir(imageResult)) {
                                print("\nCould not delete old histogram thresholding segmentation of: " + files[i].toString());
                            }
                        }
                    }

                    resultFileName = directoryPath + "kMeansSegmentation.jpg";
                    if (instructionList.contains("KMeansSegmentation")) {
                        if (histogram == null) {
                            GraphHistogram graphHistogram = new GraphHistogram(color);
                            histogram = graphHistogram.createHistogram(workingImage);
                        }
                        long startTime = System.nanoTime();
                        KMeansSegmentation kMeansSegmentation = new KMeansSegmentation(workingImage, histogram);
                        segmentationImage = kMeansSegmentation.kMeansSegmentation();
                        long time = (System.nanoTime() - startTime) / 1000000;
                        kMeansSegmentationTime += time;
                        output_file = new File(resultFileName);
                        ImageIO.write(segmentationImage, "jpg", output_file);
//                        print("Edge Detection" + " Execution time in milliseconds : " + time);
                    } else {
                        // if the file was not needed, delete the file from the relevant result folder if it existed
                        File imageResult = new File(resultFileName);
                        // delete the image result folder if it already existed. This way, no remnants from old runs remain
                        if (imageResult.exists()) {
                            if (!deleteDir(imageResult)) {
                                print("\nCould not delete old kmeans segmentation of: " + files[i].toString());
                            }
                        }
                    }

                    // TODO check that segmentation isnt just white. ie there was a segmentation operation called
                    resultFileName = directoryPath + "erosion.jpg";
                    if (instructionList.contains("Erosion")) {
                        long startTime = System.nanoTime();
                        MorphologicalFunctions morphologicalFunctions = new MorphologicalFunctions(segmentationImage, "erosion", erosionFilterWidth, erosionFilterHeight, erosionFilterColors);
                        segmentationImage = morphologicalFunctions.morphologicalFunctions();
                        long time = (System.nanoTime() - startTime) / 1000000;
                        erosionTime += time;
                        output_file = new File(resultFileName);
                        ImageIO.write(segmentationImage, "jpg", output_file);
//                        print("Edge Detection" + " Execution time in milliseconds : " + time);
                    } else {
                        // if the file was not needed, delete the file from the relevant result folder if it existed
                        File imageResult = new File(resultFileName);
                        // delete the image result folder if it already existed. This way, no remnants from old runs remain
                        if (imageResult.exists()) {
                            if (!deleteDir(imageResult)) {
                                print("\nCould not dilation of: " + files[i].toString());
                            }
                        }
                    }

                    // TODO check that segmentation isnt just white. ie there was a segmentation operation called
                    resultFileName = directoryPath + "dilation.jpg";
                    if (instructionList.contains("Dilation")) {
                        long startTime = System.nanoTime();
                        MorphologicalFunctions morphologicalFunctions = new MorphologicalFunctions(segmentationImage, "dilation", dilationFilterWidth, dilationFilterHeight, dilationFilterColors);
                        segmentationImage = morphologicalFunctions.morphologicalFunctions();
                        long time = (System.nanoTime() - startTime) / 1000000;
                        dilationTime += time;
                        output_file = new File(resultFileName);
                        ImageIO.write(segmentationImage, "jpg", output_file);
//                        print("Edge Detection" + " Execution time in milliseconds : " + time);
                    } else {
                        // if the file was not needed, delete the file from the relevant result folder if it existed
                        File imageResult = new File(resultFileName);
                        // delete the image result folder if it already existed. This way, no remnants from old runs remain
                        if (imageResult.exists()) {
                            if (!deleteDir(imageResult)) {
                                print("\nCould not dilation of: " + files[i].toString());
                            }
                        }
                    }

                } catch (Exception ex) {
                    print("\nThere was an error with image " + files[i].toString() + ": " + ex);
                    ex.printStackTrace();
                }
            }
            progressBar.next();
        }
        print("\n\nFinal Metrics:");
        getAverageHistogram(averageHistogram, files.length, color);
//            o Averaged processing time per image per each procedure // TODO what
        if (singleColorTime > 0) {// TODO write these to teh results final file?
            print("\nConverting to a single color processing time for the entire batch (ms): " + singleColorTime);
            print("Average converting to a single color processing time (ms): " + singleColorTime / files.length);
        }
        if (quantizationTime > 0) {
            print("\nQuantization processing time for the entire batch (ms): " + quantizationTime);
            print("Average quantization processing time (ms): " + quantizationTime / files.length);
            print("Average meanSquaredError: " + meanSquaredError / files.length); // TODO whats an acceptable/expected one
        }
        if (saltAndPepperTime > 0) {
            print("\nAdding salt and pepper noise processing time for the entire batch (ms): " + saltAndPepperTime);
            print("Average adding salt and pepper noise processing time (ms): " + saltAndPepperTime / files.length);
        }
        if (gaussianTime > 0) {
            print("\nAdding gaussian noise processing time for the entire batch (ms): " + gaussianTime);
            print("Average adding gaussian noise processing time (ms): " + gaussianTime / files.length);
        }
        if (linearFilterTime > 0) {
            print("\nLinear filter processing time for the entire batch (ms): " + linearFilterTime);
            print("Average linear filter processing time (ms): " + linearFilterTime / files.length);
        }
        if (medianFilterTime > 0) {
            print("\nMedian filter processing time for the entire batch (ms): " + medianFilterTime);
            print("Average median filter processing time (ms): " + medianFilterTime / files.length);
        }
        if (histogramTime > 0) {
            print("\nHistogram creation processing time for the entire batch (ms): " + histogramTime);
            print("Average histogram creation processing time (ms): " + histogramTime / files.length);
        }
        if (equalizationTime > 0) {
            print("\nEqualized histogram creation processing time for the entire batch (ms): " + equalizationTime);
            print("Average equalized histogram creation processing time (ms): " + equalizationTime / files.length);
        }
        if (edgeDetectionTime > 0) {
            print("\nEdge detection creation processing time for the entire batch (ms): " + edgeDetectionTime);
            print("Average edge detection processing time (ms): " + edgeDetectionTime / files.length);
        }
        if (histogramThresholdingSegmentationTime > 0) {
            print("\nHistogram thresholding segmentation time creation processing time for the entire batch (ms): " + histogramThresholdingSegmentationTime);
            print("Average histogram thresholding segmentation processing time (ms): " + histogramThresholdingSegmentationTime / files.length);
        }
        if (kMeansSegmentationTime > 0) {
            print("\nK means segmentation time creation processing time for the entire batch (ms): " + kMeansSegmentationTime);
            print("Average k means segmentation processing time (ms): " + kMeansSegmentationTime / files.length);
        }
        if (erosionTime > 0) {
            print("\nErosion time creation processing time for the entire batch (ms): " + erosionTime);
            print("Average k means segmentation processing time (ms): " + erosionTime / files.length);
        }
        if (dilationTime > 0) {
            print("\nDilation time creation processing time for the entire batch (ms): " + dilationTime);
            print("Average k means segmentation processing time (ms): " + dilationTime / files.length);
        }
        print("\nTotal RunTime (without image exporting) (s): " + ((singleColorTime + quantizationTime + saltAndPepperTime +
                gaussianTime + linearFilterTime + medianFilterTime + histogramTime + equalizationTime + edgeDetectionTime +
                histogramThresholdingSegmentationTime + kMeansSegmentationTime + erosionTime + dilationTime) / 1000));
        print("Real run time (s): " + (System.nanoTime() - realStartTime) / 1000000000);
    }


    private static void getAverageHistogram(int[] histogram, int divisor, String color) {
        for (int i = 0; i < histogram.length; i++) {
            histogram[i] /= divisor;
        }

        try {
            GraphHistogram graphHistogram = new GraphHistogram(color);
            String graphTitle = "averageHistogram";
            JFreeChart defaultHistogram = graphHistogram.graphHistogram(histogram, graphTitle);
            String directoryPath = "results/finalReport/";
            new File(directoryPath).mkdirs();
            File output_file = new File(directoryPath + graphTitle + ".png");
            ChartUtilities.saveChartAsPNG(output_file, defaultHistogram, 700, 500);
        } catch (Exception e) {
            print("Could not create specified directory for the average histogram. Skipping this operation. Error: " + e);
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


    // any errors thrown by this method will get caught in the instruction file reader
    private static int[] parseArray(String[] stringArray) {
        ArrayList<Integer> temp = new ArrayList<>();
        Boolean inArray = false;
        for (int i = 0; i < stringArray.length; i++) {
            if (stringArray[i].equalsIgnoreCase("]")) {
                break;
            } else if (stringArray[i].equalsIgnoreCase("[")) {
                inArray = true;
                continue;
            }

            if (inArray) {
                temp.add(Integer.parseInt(stringArray[i]));
            }
        }

        // convert to required array
        int[] result = new int[temp.size()];
        for (int i=0; i < result.length; i++)
        {
            result[i] = temp.get(i);
        }

        if (result.length <= 0) {
            return null;
        }
        return result;
    }

    public static void print(String s) {
        System.out.println(s);
        try {
            BufferedWriter out = new BufferedWriter(
                    new FileWriter("results/report.txt", true));
            out.write(s);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
