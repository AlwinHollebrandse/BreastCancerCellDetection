package com.alwin;

import mpi.*;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;

public class ImageOperationsCall {

    private Utility utility;
    private boolean deletePreviousImages;
    private boolean usePreviousImages;
    private String color;
    private int quantizationScale;
    private double saltAndPepperNoiseRandomThreshold;
    private double saltAndPepperNoiseMean;
    private double saltAndPepperNoiseSigma;
    private double gaussianNoiseRandomThreshold;
    private double gaussianNoiseMean;
    private double gaussianNoiseSigma;
    private int linearFilterWidth;
    private int linearFilterHeight;
    private int[] linearFilterWeights;
    private int medianFilterWidth;
    private int medianFilterHeight;
    private int[] medianFilterWeights;
    private int erosionFilterWidth;
    private int erosionFilterHeight;
    private int[] erosionFilterColors;
    private int dilationFilterWidth;
    private int dilationFilterHeight;
    private int[] dilationFilterColors;
    private Map<String, Integer> timeDict;
    private int meanSquaredError;
    private int[] averageHistogram;
    private ArrayList<String> csvDatasetArrayList;
    private ArrayList<CellObject> datasetArrayList;
    private ArrayList<String> instructionList;
    private int myrank;
    private int numberOfProcessors;

    // TODO change params to a single "operationcallParam" custom type for cleaner code
    public ImageOperationsCall(Utility utility, boolean deletePreviousImages,
            boolean usePreviousImages, String color, int quantizationScale,
            double saltAndPepperNoiseRandomThreshold, double saltAndPepperNoiseMean,
            double saltAndPepperNoiseSigma, double gaussianNoiseRandomThreshold,
            double gaussianNoiseMean, double gaussianNoiseSigma, int linearFilterWidth,
            int linearFilterHeight, int[] linearFilterWeights, int medianFilterWidth,
            int medianFilterHeight, int[] medianFilterWeights, int erosionFilterWidth,
            int erosionFilterHeight, int[] erosionFilterColors, int dilationFilterWidth,
            int dilationFilterHeight, int[] dilationFilterColors,
            Map<String, Integer> timeDict, int meanSquaredError, int[] averageHistogram,
            ArrayList<String> csvDatasetArrayList, ArrayList<CellObject> datasetArrayList,
            ArrayList<String> instructionList, int myrank, int numberOfProcessors) {
        this.utility = utility;
        this.deletePreviousImages = deletePreviousImages;
        this.usePreviousImages = usePreviousImages;
        this.color = color;
        this.quantizationScale = quantizationScale;
        this.saltAndPepperNoiseRandomThreshold = saltAndPepperNoiseRandomThreshold;
        this.saltAndPepperNoiseMean = saltAndPepperNoiseMean;
        this.saltAndPepperNoiseSigma = saltAndPepperNoiseSigma;
        this.gaussianNoiseRandomThreshold = gaussianNoiseRandomThreshold;
        this.gaussianNoiseMean = gaussianNoiseMean;
        this.gaussianNoiseSigma = gaussianNoiseSigma;
        this.linearFilterWidth = linearFilterWidth;
        this.linearFilterHeight = linearFilterHeight;
        this.linearFilterWeights = linearFilterWeights;
        this.medianFilterWidth = medianFilterWidth;
        this.medianFilterHeight = medianFilterHeight;
        this.medianFilterWeights = medianFilterWeights;
        this.erosionFilterWidth = erosionFilterWidth;
        this.erosionFilterHeight = erosionFilterHeight;
        this.erosionFilterColors = erosionFilterColors;
        this.dilationFilterWidth = dilationFilterWidth;
        this.dilationFilterHeight = dilationFilterHeight;
        this.dilationFilterColors = dilationFilterColors;
        this.timeDict = timeDict;
        this.meanSquaredError = meanSquaredError;
        this.averageHistogram = averageHistogram;
        this.csvDatasetArrayList = csvDatasetArrayList;
        this.datasetArrayList = datasetArrayList;
        this.instructionList = instructionList;
        this.myrank = myrank;
        this.numberOfProcessors = numberOfProcessors;
    }
    
    public void imageOperationsCall(File file) {
        String resultFileName;
        if (file.isFile()) { //this line weeds out other directories/folders
//                utility.print("\n\n" + file);
            try {
                // Make directory for the current cell image file.
                int startOfPictureName = file.toString().lastIndexOf("/");
                if (startOfPictureName == -1) {
                    startOfPictureName = file.toString().lastIndexOf("\\");
                }
                int endOfPictureName = file.toString().length() - 4; // remove the ".BMP" from the directory name
                String directoryPath = "results/" + file.toString().substring(startOfPictureName + 1, endOfPictureName) + "/";
//                    utility.print("directoryPath: " + directoryPath);

                File imageResults = new File(directoryPath);

                // (if needed) make a new output folder for the current imagee.
                imageResults.mkdirs();
                if (!new File(directoryPath).exists()) {
                    utility.print("Could not create specified directory for image: " + file.toString() + ". Skipping this image.");
                    return;
                }

                //set up global vars for all image operations
                int[] histogram = null;
                BufferedImage workingImage;
                BufferedImage edgeMap;
                BufferedImage segmentationImage;

                // add the original image to the relevant result folder
                final BufferedImage originalImage = ImageIO.read(file);
                // System.out.println("originalImage.getWidth(): " + originalImage.getWidth() + " originalImage.getHeight(): " + originalImage.getHeight());

                workingImage = originalImage;
                edgeMap = originalImage;
                segmentationImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), originalImage.getType());
                File output_file = new File(directoryPath + "original.jpg");
                ImageIO.write(originalImage, "jpg", output_file);


                ///////////////////////////////////////////// PERFORM REQUESTED IMAGE OPERATIONS ////////////////////////////////////////////////////////
                resultFileName = directoryPath + color + ".jpg";
                if (!usePreviousImages && instructionList.contains("SingleColor")) {
                    long startTime = System.nanoTime();
                    SingleColorScale singleColorScale = new SingleColorScale(originalImage, color);
                    workingImage = singleColorScale.convertToSingleColor();
                    long time = (System.nanoTime() - startTime) / 1000000;
                    timeDict.put("singleColorTime", (int)(timeDict.get("singleColorTime") + time));
                    output_file = new File(resultFileName);
                    if (myrank == 0) {
                        ImageIO.write(workingImage, "jpg", output_file);
                    }
//                        utility.print("Converting to GrayScale" + " Execution time in milliseconds : " + time);
                } else if (usePreviousImages) {
                    workingImage = ImageIO.read( new File(resultFileName));
                } else if (deletePreviousImages){
                    // if the file was not needed, delete the file from the relevant result folder if it existed
                    File imageResult = new File(resultFileName);
                    // delete the image result folder if it already existed. This way, no remnants from old runs remain
                    if (imageResult.exists()) {
                        if (!utility.deleteDir(imageResult)) {
                            utility.print("\nCould not delete old SingleColor of: " + file.toString());
                        }
                    }
                }


                resultFileName = directoryPath + "quantization.jpg";
                if (!usePreviousImages && instructionList.contains("Quantization")) {
                    if (quantizationScale == -1) {
                        utility.print("Quantization parameter was set incorrectly");
                        System.exit(1);
                    }
                    long startTime = System.nanoTime();
                    Quantization quantization = new Quantization(workingImage, quantizationScale, color); // NOTE works nicer with a scale that is a factor of 2
                    workingImage = quantization.quantization();
                    output_file = new File(resultFileName);
                    long time = (System.nanoTime() - startTime) / 1000000;
                    timeDict.put("quantizationTime", (int)(timeDict.get("quantizationTime") + time));
                    if (myrank == 0) {
                        ImageIO.write(workingImage, "jpg", output_file);
                    }
                    meanSquaredError += quantization.getMeanSquaredError(workingImage);
//                        utility.print("Quantization" + " Execution time in milliseconds : " + time);
                } else if (usePreviousImages && instructionList.contains("Quantization")) {
                    workingImage = ImageIO.read( new File(resultFileName));
                } else if (deletePreviousImages){
                    // if the file was not needed, delete the file from the relevant result folder if it existed
                    File imageResult = new File(resultFileName);
                    // delete the image result folder if it already existed. This way, no remnants from old runs remain
                    if (imageResult.exists()) {
                        if (!utility.deleteDir(imageResult)) {
                            utility.print("\nCould not delete old Quantization of: " + file.toString());
                        }
                    }
                }


                resultFileName = directoryPath + "saltAndPepper.jpg";
                if (!usePreviousImages && instructionList.contains("SaltAndPepper")) {
                    long startTime = System.nanoTime();
                    NoiseAdder noiseAdder = new NoiseAdder(workingImage, "saltAndPepper", saltAndPepperNoiseRandomThreshold, saltAndPepperNoiseMean, saltAndPepperNoiseSigma);
                    workingImage = noiseAdder.addNoise();
                    long time = (System.nanoTime() - startTime) / 1000000;
                    timeDict.put("saltAndPepperTime", (int)(timeDict.get("saltAndPepperTime") + time));
                    output_file = new File(resultFileName);
                    if (myrank == 0) {
                        ImageIO.write(workingImage, "jpg", output_file);
                    }
//                        utility.print("Adding Salt and Pepper Noise" + " Execution time in milliseconds : " + time);
                } else if (usePreviousImages && instructionList.contains("SaltAndPepper")) {
                    workingImage = ImageIO.read( new File(resultFileName));
                } else if (deletePreviousImages){
                    // if the file was not needed, delete the file from the relevant result folder if it existed
                    File imageResult = new File(resultFileName);
                    // delete the image result folder if it already existed. This way, no remnants from old runs remain
                    if (imageResult.exists()) {
                        if (!utility.deleteDir(imageResult)) {
                            utility.print("\nCould not delete old SaltAndPepperNoise of: " + file.toString());
                        }
                    }
                }


                resultFileName = directoryPath + "gaussian.jpg";
                if (!usePreviousImages && instructionList.contains("Gaussian")) {
                    long startTime = System.nanoTime();
                    NoiseAdder noiseAdder = new NoiseAdder(workingImage, "gaussian", gaussianNoiseRandomThreshold, gaussianNoiseMean, gaussianNoiseSigma);
                    workingImage = noiseAdder.addNoise();
                    long time = (System.nanoTime() - startTime) / 1000000;
                    timeDict.put("gaussianTime", (int)(timeDict.get("gaussianTime") + time));
                    output_file = new File(resultFileName);
                    if (myrank == 0) {
                        ImageIO.write(workingImage, "jpg", output_file);
                    }
//                        utility.print("Adding Gaussian Noise" + " Execution time in milliseconds : " + time);
                } else if (usePreviousImages && instructionList.contains("Gaussian")) {
                    workingImage = ImageIO.read( new File(resultFileName));
                } else if (deletePreviousImages){
                    // if the file was not needed, delete the file from the relevant result folder if it existed
                    File imageResult = new File(resultFileName);
                    // delete the image result folder if it already existed. This way, no remnants from old runs remain
                    if (imageResult.exists()) {
                        if (!utility.deleteDir(imageResult)) {
                            utility.print("\nCould not delete old GaussianNoise of: " + file.toString());
                        }
                    }
                }


                resultFileName = directoryPath + "linear.jpg";
                if (!usePreviousImages && instructionList.contains("LinearFilter")) {
                    if (linearFilterWidth == -1 || linearFilterHeight == -1) {
                        utility.print("linear filter parameters were set incorrectly");
                        System.exit(1);
                    }
                    long startTime = System.nanoTime();
                    Filter filter = new Filter(workingImage, "linear", linearFilterWidth, linearFilterHeight, linearFilterWeights);//new int[]{1, 2, 1, 2, 3, 2, 1, 2, 1}, (1/15));
                    workingImage = filter.filter();
                    long time = (System.nanoTime() - startTime) / 1000000;
                    timeDict.put("linearFilterTime", (int)(timeDict.get("linearFilterTime") + time));
                    output_file = new File(resultFileName);
                    if (myrank == 0) {
                        ImageIO.write(workingImage, "jpg", output_file);
                    }
//                        utility.print("Linear Filter" + " Execution time in milliseconds : " + time);
                } else if (usePreviousImages && instructionList.contains("LinearFilter")) {
                    workingImage = ImageIO.read( new File(resultFileName));
                } else if (deletePreviousImages){
                    // if the file was not needed, delete the file from the relevant result folder if it existed
                    File imageResult = new File(resultFileName);
                    // delete the image result folder if it already existed. This way, no remnants from old runs remain
                    if (imageResult.exists()) {
                        if (!utility.deleteDir(imageResult)) {
                            utility.print("\nCould not delete old LinearFilter of: " + file.toString());
                        }
                    }
                }


                resultFileName = directoryPath + "median.jpg";
                if (!usePreviousImages && instructionList.contains("MedianFilter")) {
                    if (medianFilterWidth == -1 || medianFilterHeight == -1) {
                        utility.print("median filter parameters were set incorrectly");
                        System.exit(1);
                    }
                    long startTime = System.nanoTime();
                    Filter filter = new Filter(workingImage, "median", medianFilterWidth, medianFilterHeight, medianFilterWeights);//new int[]{1, 2, 1, 2, 3, 2, 1, 2, 1}, (1/15.0));
                    workingImage = filter.filter();
                    long time = (System.nanoTime() - startTime) / 1000000;
                    timeDict.put("medianFilterTime", (int)(timeDict.get("medianFilterTime") + time));
                    output_file = new File(resultFileName);
                    if (myrank == 0) {
                        ImageIO.write(workingImage, "jpg", output_file);
                    }
//                        utility.print("Median Filter" + " Execution time in milliseconds : " + time);
                } else if (usePreviousImages && instructionList.contains("MedianFilter")) {
                    workingImage = ImageIO.read( new File(resultFileName));
                } else if (deletePreviousImages){
                    // if the file was not needed, delete the file from the relevant result folder if it existed
                    File imageResult = new File(resultFileName);
                    // delete the image result folder if it already existed. This way, no remnants from old runs remain
                    if (imageResult.exists()) {
                        if (!utility.deleteDir(imageResult)) {
                            utility.print("\nCould not delete old MedianFilter of: " + file.toString());
                        }
                    }
                }

                resultFileName = directoryPath + "serialMedian.jpg";
                if (!usePreviousImages && instructionList.contains("SerialMedianFilter")) {
                    if (medianFilterWidth == -1 || medianFilterHeight == -1) {
                        utility.print("serial median filter parameters were set incorrectly");
                        System.exit(1);
                    }
                    long startTime = System.nanoTime();
                    SerialFilter serialFilter = new SerialFilter(workingImage, "median", medianFilterWidth, medianFilterHeight, medianFilterWeights);//new int[]{1, 2, 1, 2, 3, 2, 1, 2, 1}, (1/15.0));
                    workingImage = serialFilter.filter();
                    long time = (System.nanoTime() - startTime) / 1000000;
                    timeDict.put("serialMedianFilterTime", (int)(timeDict.get("serialMedianFilterTime") + time));
                    output_file = new File(resultFileName);
                    if (myrank == 0) {
                        ImageIO.write(workingImage, "jpg", output_file);
                    }
//                        utility.print("Median Filter" + " Execution time in milliseconds : " + time);
                } else if (usePreviousImages && instructionList.contains("SerialMedianFilter")) {
                    workingImage = ImageIO.read( new File(resultFileName));
                } else if (deletePreviousImages){
                    // if the file was not needed, delete the file from the relevant result folder if it existed
                    File imageResult = new File(resultFileName);
                    // delete the image result folder if it already existed. This way, no remnants from old runs remain
                    if (imageResult.exists()) {
                        if (!utility.deleteDir(imageResult)) {
                            utility.print("\nCould not delete old SerialMedianFilter of: " + file.toString());
                        }
                    }
                }

                // all ranks call this
                resultFileName = directoryPath + "mpiMedian.jpg";
                if (!usePreviousImages && instructionList.contains("MpiMedianFilter")) {
                    if (medianFilterWidth == -1 || medianFilterHeight == -1) {
                        utility.print("mpi median filter parameters were set incorrectly");
                        System.exit(1);
                    }
                    long startTime = System.nanoTime();
                    
                    int newImageWidth = workingImage.getWidth() - ((medianFilterWidth/2) * 2);
                    int newImageHeight = workingImage.getHeight() - ((medianFilterHeight/2) * 2);

                    // each rank computes its starting and ending `x` (for double for loop)
                    int rowsPerProcess = (int)Math.ceil((double)newImageWidth / numberOfProcessors);
                    int pixelsPerProcess = rowsPerProcess * newImageHeight;
                    int startingX = rowsPerProcess * myrank; // TODO test correctness
                    int endingX = rowsPerProcess * (myrank + 1);
                    if (endingX > newImageWidth) {
                        endingX = newImageWidth;
                    }

                    MpiFilter mpiFilter = new MpiFilter(workingImage, "median", medianFilterWidth, medianFilterHeight, medianFilterWeights, newImageWidth, newImageHeight, pixelsPerProcess, startingX, endingX);
                    int[] filterImagePortion = mpiFilter.filter();

                    // NOTE the size is set to this value because pixelsPerProcess * numberOfProcessors > newImageWidth * newImageHeight
                    // This was done so that GATHER could get a constant amount of pixels and not break
                    int[] allFilterImageValues = new int[pixelsPerProcess * numberOfProcessors];
                    MPI.COMM_WORLD.Allgather(filterImagePortion, 0, pixelsPerProcess, MPI.INT, allFilterImageValues, 0, pixelsPerProcess, MPI.INT);

                    workingImage = mpiFilter.fillFilterImage(allFilterImageValues);
                    long time = (System.nanoTime() - startTime) / 1000000;
                    timeDict.put("mpiMedianFilterTime", (int)(timeDict.get("mpiMedianFilterTime") + time));
                    output_file = new File(resultFileName);

                    if (myrank == 0) {
                        ImageIO.write(workingImage, "jpg", output_file);
    //                        utility.print("Median Filter" + " Execution time in milliseconds : " + time);
                    }

                } else if (usePreviousImages && instructionList.contains("MpiMedianFilter") && myrank == 0) {
                    workingImage = ImageIO.read( new File(resultFileName));
                } else if (deletePreviousImages && myrank == 0){
                    // if the file was not needed, delete the file from the relevant result folder if it existed
                    File imageResult = new File(resultFileName);
                    // delete the image result folder if it already existed. This way, no remnants from old runs remain
                    if (imageResult.exists()) {
                        if (!utility.deleteDir(imageResult)) {
                            utility.print("\nCould not delete old MpiMedianFilter of: " + file.toString());
                        }
                    }
                }

                resultFileName = directoryPath + "mpiThreadedMedian.jpg";
                if (!usePreviousImages && instructionList.contains("MpiThreadedMedianFilter")) {
                    if (medianFilterWidth == -1 || medianFilterHeight == -1) {
                        utility.print("mpi threaded median filter parameters were set incorrectly");
                        System.exit(1);
                    }
                    long startTime = System.nanoTime();
                    
                    int newImageWidth = workingImage.getWidth() - ((medianFilterWidth/2) * 2);
                    int newImageHeight = workingImage.getHeight() - ((medianFilterHeight/2) * 2);

                    // each rank computes its starting and ending `x` (for double for loop)
                    int rowsPerProcess = (int)Math.ceil((double)newImageWidth / numberOfProcessors);
                    int pixelsPerProcess = rowsPerProcess * newImageHeight;
                    int startingX = rowsPerProcess * myrank;
                    int endingX = rowsPerProcess * (myrank + 1);
                    if (endingX > newImageWidth) {
                        endingX = newImageWidth;
                    }


                    Mpi_Threaded_Filter_potato_pie_5 mpiThreadedFilter = new Mpi_Threaded_Filter_potato_pie_5(workingImage, "median", medianFilterWidth, medianFilterHeight, medianFilterWeights, newImageWidth, newImageHeight, pixelsPerProcess, startingX, endingX);
                    int[] filterImagePortion = mpiThreadedFilter.filter();

                    // NOTE the size is set to this value because pixelsPerProcess * numberOfProcessors > newImageWidth * newImageHeight
                    // This was done so that GATHER could get a constant amount of pixels and not break
   
                    int[] allFilterImageValues = new int[pixelsPerProcess * numberOfProcessors];
                    MPI.COMM_WORLD.Allgather(filterImagePortion, 0, pixelsPerProcess, MPI.INT, allFilterImageValues, 0, pixelsPerProcess, MPI.INT);

                    workingImage = mpiThreadedFilter.fillFilterImage(allFilterImageValues);
                    long time = (System.nanoTime() - startTime) / 1000000;
                    timeDict.put("mpiThreadedMedianFilterTime", (int)(timeDict.get("mpiThreadedMedianFilterTime") + time));
                    output_file = new File(resultFileName);

                    if (myrank == 0) {
                        ImageIO.write(workingImage, "jpg", output_file);
    //                        utility.print("Median Filter" + " Execution time in milliseconds : " + time);
                    }
                } else if (usePreviousImages && instructionList.contains("MpiThreadedMedianFilter") && myrank == 0) {
                    workingImage = ImageIO.read( new File(resultFileName));
                } else if (deletePreviousImages && myrank == 0){
                    // if the file was not needed, delete the file from the relevant result folder if it existed
                    File imageResult = new File(resultFileName);
                    // delete the image result folder if it already existed. This way, no remnants from old runs remain
                    if (imageResult.exists()) {
                        if (!utility.deleteDir(imageResult)) {
                            utility.print("\nCould not delete old MpiThreadedMedianFilter of: " + file.toString());
                        }
                    }
                }


                // TODO add a "usePreviousImages" option for histogram
                String graphTitle = "histogram";
                resultFileName = directoryPath + graphTitle + ".png";
                if (!usePreviousImages && instructionList.contains("Histogram")) {
                    long startTime = System.nanoTime();
                    GraphHistogram graphHistogram = new GraphHistogram(color);
                    histogram = graphHistogram.createHistogram(workingImage);
                    long time = (System.nanoTime() - startTime) / 1000000;
                    timeDict.put("histogramTime", (int)(timeDict.get("histogramTime") + time));
//                        utility.print("Histogram creation Execution time in milliseconds : " + time);

                    // utility.print the starting histogram to a file
                    JFreeChart histogramGraph = graphHistogram.graphHistogram(histogram, graphTitle);
                    ChartUtilities.saveChartAsPNG(new File(resultFileName), histogramGraph, 700, 500);
                    HistogramFunctions histogramFunctions = new HistogramFunctions(workingImage, histogram);
                    averageHistogram = histogramFunctions.sumHistograms(averageHistogram);
                } else if (deletePreviousImages){
                    // if the file was not needed, delete the file from the relevant result folder if it existed
                    File imageResult = new File(resultFileName);
                    // delete the image result folder if it already existed. This way, no remnants from old runs remain
                    if (imageResult.exists()) {
                        if (!utility.deleteDir(imageResult)) {
                            utility.print("\nCould not delete old Histogram of: " + file.toString());
                        }
                    }
                }


                resultFileName = directoryPath + "equalizedImage.jpg";
                graphTitle = "equalizedHistogram";
                if (!usePreviousImages && instructionList.contains("HistogramEqualization")) {
                    if (histogram == null) {
                        GraphHistogram graphHistogram = new GraphHistogram(color);
                        histogram = graphHistogram.createHistogram(workingImage);
                    }
                    long startTime = System.nanoTime();
                    HistogramFunctions histogramFunctions = new HistogramFunctions(workingImage, histogram);
                    workingImage = histogramFunctions.equalizedImage();
                    long time = (System.nanoTime() - startTime) / 1000000;
                    timeDict.put("equalizationTime", (int)(timeDict.get("equalizationTime") + time));
                    GraphHistogram graphHistogram = new GraphHistogram(color);
                    int[] equalizedHistogram = graphHistogram.createHistogram(workingImage);
                    output_file = new File(resultFileName);
                    if (myrank == 0) {
                        ImageIO.write(workingImage, "jpg", output_file);
                    }
//                        utility.print("histogram equalization" + " Execution time in milliseconds : " + time);

                    // utility.print the equalized histogram to a file
                    JFreeChart equalizedHistogramGraph = graphHistogram.graphHistogram(equalizedHistogram, graphTitle);
                    ChartUtilities.saveChartAsPNG(new File(directoryPath + graphTitle + ".png"), equalizedHistogramGraph, 700, 500);
                } else if (usePreviousImages && instructionList.contains("HistogramEqualization")) {
                    workingImage = ImageIO.read( new File(resultFileName));
                } else if (deletePreviousImages){
                    // if the file was not needed, delete the file from the relevant result folder if it existed
                    File imageResult = new File(resultFileName);
                    // delete the image result folder if it already existed. This way, no remnants from old runs remain
                    if (imageResult.exists()) {
                        if (!utility.deleteDir(imageResult)) {
                            utility.print("\nCould not delete old HistogramEqualization of: " + file.toString());
                        }
                    }

                    // need to delete the histogram too, and not only the equalized image
                    imageResult = new File(directoryPath + graphTitle + ".png");
                    // delete the image result folder if it already existed. This way, no remnants from old runs remain
                    if (imageResult.exists()) {
                        if (!utility.deleteDir(imageResult)) {
                            utility.print("\nCould not delete old HistogramEqualization of: " + file.toString());
                        }
                    }
                }

                resultFileName = directoryPath + "edgeDetection.jpg";
                if (!usePreviousImages && instructionList.contains("EdgeDetection")) {
                    long startTime = System.nanoTime();
                    EdgeDetection edgeDetection = new EdgeDetection();
                    edgeMap = edgeDetection.edgeDetection(workingImage);
                    long time = (System.nanoTime() - startTime) / 1000000;
                    timeDict.put("edgeDetectionTime", (int)(timeDict.get("edgeDetectionTime") + time));
                    output_file = new File(resultFileName);
                    ImageIO.write(edgeMap, "jpg", output_file);
//                        utility.print("Edge Detection" + " Execution time in milliseconds : " + time);
                } else if (usePreviousImages && instructionList.contains("EdgeDetection")) {
                    edgeMap = ImageIO.read( new File(resultFileName));
                } else if (deletePreviousImages){
                    // if the file was not needed, delete the file from the relevant result folder if it existed
                    File imageResult = new File(resultFileName);
                    // delete the image result folder if it already existed. This way, no remnants from old runs remain
                    if (imageResult.exists()) {
                        if (!utility.deleteDir(imageResult)) {
                            utility.print("\nCould not delete old edgeDetection of: " + file.toString());
                        }
                    }
                }

                resultFileName = directoryPath + "histogramThresholdingSegmentation.jpg";
                if (!usePreviousImages && instructionList.contains("HistogramThresholdingSegmentation")) {
                    if (histogram == null) {
                        GraphHistogram graphHistogram = new GraphHistogram(color);
                        histogram = graphHistogram.createHistogram(workingImage);
                    }
                    long startTime = System.nanoTime();
                    ThresholdSegmentation thresholdSegmentation = new ThresholdSegmentation(workingImage, histogram);
                    segmentationImage = thresholdSegmentation.thresholdSegmentation();
                    long time = (System.nanoTime() - startTime) / 1000000;
                    timeDict.put("histogramThresholdingSegmentationTime", (int)(timeDict.get("histogramThresholdingSegmentationTime") + time));
                    output_file = new File(resultFileName);
                    ImageIO.write(segmentationImage, "jpg", output_file);
//                        utility.print("Edge Detection" + " Execution time in milliseconds : " + time);
                } else if (usePreviousImages && instructionList.contains("HistogramThresholdingSegmentation")) {
                    segmentationImage = ImageIO.read( new File(resultFileName));
                } else if (deletePreviousImages){
                    // if the file was not needed, delete the file from the relevant result folder if it existed
                    File imageResult = new File(resultFileName);
                    // delete the image result folder if it already existed. This way, no remnants from old runs remain
                    if (imageResult.exists()) {
                        if (!utility.deleteDir(imageResult)) {
                            utility.print("\nCould not delete old histogram thresholding segmentation of: " + file.toString());
                        }
                    }
                }

                resultFileName = directoryPath + "kMeansSegmentation.jpg";
                if (!usePreviousImages && instructionList.contains("KMeansSegmentation")) {
                    if (histogram == null) {
                        GraphHistogram graphHistogram = new GraphHistogram(color);
                        histogram = graphHistogram.createHistogram(workingImage);
                    }
                    long startTime = System.nanoTime();
                    KMeansSegmentation kMeansSegmentation = new KMeansSegmentation(workingImage, histogram);
                    segmentationImage = kMeansSegmentation.kMeansSegmentation();
                    long time = (System.nanoTime() - startTime) / 1000000;
                    timeDict.put("kMeansSegmentationTime", (int)(timeDict.get("kMeansSegmentationTime") + time));
                    output_file = new File(resultFileName);
                    ImageIO.write(segmentationImage, "jpg", output_file);
//                        utility.print("Edge Detection" + " Execution time in milliseconds : " + time);
                } else if (usePreviousImages && instructionList.contains("KMeansSegmentation")) {
                    segmentationImage = ImageIO.read( new File(resultFileName));
                } else if (deletePreviousImages){
                    // if the file was not needed, delete the file from the relevant result folder if it existed
                    File imageResult = new File(resultFileName);
                    // delete the image result folder if it already existed. This way, no remnants from old runs remain
                    if (imageResult.exists()) {
                        if (!utility.deleteDir(imageResult)) {
                            utility.print("\nCould not delete old kmeans segmentation of: " + file.toString());
                        }
                    }
                }

                resultFileName = directoryPath + "erosion.jpg";
                if (!usePreviousImages && instructionList.contains("Erosion")) {
                    if (utility.checkIfAllBlackImage(segmentationImage)) {
                        long startTime = System.nanoTime();
                        ThresholdSegmentation thresholdSegmentation = new ThresholdSegmentation(workingImage, histogram);
                        segmentationImage = thresholdSegmentation.thresholdSegmentation();
                        long time = (System.nanoTime() - startTime) / 1000000;
                        timeDict.put("histogramThresholdingSegmentationTime", (int)(timeDict.get("histogramThresholdingSegmentationTime") + time));
                        String segmentationFileName = directoryPath + "histogramThresholdingSegmentation.jpg";
                        output_file = new File(segmentationFileName);
                        ImageIO.write(segmentationImage, "jpg", output_file);
                    }

                    long startTime = System.nanoTime();
                    MorphologicalFunctions morphologicalFunctions = new MorphologicalFunctions(segmentationImage, "erosion", erosionFilterWidth, erosionFilterHeight, erosionFilterColors);
                    segmentationImage = morphologicalFunctions.morphologicalFunctions();
                    long time = (System.nanoTime() - startTime) / 1000000;
                    timeDict.put("erosionTime", (int)(timeDict.get("erosionTime") + time));
                    output_file = new File(resultFileName);
                    ImageIO.write(segmentationImage, "jpg", output_file);
//                        utility.print("Edge Detection" + " Execution time in milliseconds : " + time);
                } else if (usePreviousImages && instructionList.contains("Erosion")) {
                    segmentationImage = ImageIO.read( new File(resultFileName));
                } else if (deletePreviousImages){
                    // if the file was not needed, delete the file from the relevant result folder if it existed
                    File imageResult = new File(resultFileName);
                    // delete the image result folder if it already existed. This way, no remnants from old runs remain
                    if (imageResult.exists()) {
                        if (!utility.deleteDir(imageResult)) {
                            utility.print("\nCould not delete old erosion of: " + file.toString());
                        }
                    }
                }

                resultFileName = directoryPath + "dilation.jpg";
                if (!usePreviousImages && instructionList.contains("Dilation")) {
                    if (utility.checkIfAllBlackImage(segmentationImage)) {
                        long startTime = System.nanoTime();
                        ThresholdSegmentation thresholdSegmentation = new ThresholdSegmentation(workingImage, histogram);
                        segmentationImage = thresholdSegmentation.thresholdSegmentation();
                        long time = (System.nanoTime() - startTime) / 1000000;
                        timeDict.put("histogramThresholdingSegmentationTime", (int)(timeDict.get("histogramThresholdingSegmentationTime") + time));
                        String segmentationFileName = directoryPath + "histogramThresholdingSegmentation.jpg";
                        output_file = new File(segmentationFileName);
                        ImageIO.write(segmentationImage, "jpg", output_file);
                    }

                    long startTime = System.nanoTime();
                    MorphologicalFunctions morphologicalFunctions = new MorphologicalFunctions(segmentationImage, "dilation", dilationFilterWidth, dilationFilterHeight, dilationFilterColors);
                    segmentationImage = morphologicalFunctions.morphologicalFunctions();
                    long time = (System.nanoTime() - startTime) / 1000000;
                    timeDict.put("dilationTime", (int)(timeDict.get("dilationTime") + time));
                    output_file = new File(resultFileName);
                    ImageIO.write(segmentationImage, "jpg", output_file);
//                        utility.print("Edge Detection" + " Execution time in milliseconds : " + time);
                } else if (usePreviousImages && instructionList.contains("Dilation")) {
                    segmentationImage = ImageIO.read( new File(resultFileName));
                } else if (deletePreviousImages){
                    // if the file was not needed, delete the file from the relevant result folder if it existed
                    File imageResult = new File(resultFileName);
                    // delete the image result folder if it already existed. This way, no remnants from old runs remain
                    if (imageResult.exists()) {
                        if (!utility.deleteDir(imageResult)) {
                            utility.print("\nCould not delete old dilation of: " + file.toString());
                        }
                    }
                }

                // TODO add a "usePreviousImages" option for FeatureExtraction
                if (instructionList.contains("FeatureExtraction")) {

                    if (histogram == null) {
                        GraphHistogram graphHistogram = new GraphHistogram(color);
                        histogram = graphHistogram.createHistogram(workingImage);
                    }

                    if (utility.checkIfAllBlackImage(segmentationImage)) {
                        long startTime = System.nanoTime();
                        ThresholdSegmentation thresholdSegmentation = new ThresholdSegmentation(workingImage, histogram);
                        segmentationImage = thresholdSegmentation.thresholdSegmentation();
                        long time = (System.nanoTime() - startTime) / 1000000;
                        timeDict.put("histogramThresholdingSegmentationTime", (int)(timeDict.get("histogramThresholdingSegmentationTime") + time));
                        String segmentationFileName = directoryPath + "histogramThresholdingSegmentation.jpg";
                        output_file = new File(segmentationFileName);
                        ImageIO.write(segmentationImage, "jpg", output_file);
                    }

                    long startTime = System.nanoTime();

                    // Write features and label to CSV
                    FeatureExtraction featureExtraction = new FeatureExtraction();
                    double histogramMeanFeature = featureExtraction.getHistogramMean(histogram);
                    double histogramStdDevFeature = featureExtraction.getHistogramStdDev(histogram);
                    double imageEntropy = featureExtraction.getImageEntropy(histogram);
                    double areaFeature = featureExtraction.getObjectArea(segmentationImage);

                    LabelExtraction labelExtraction = new LabelExtraction();
                    String cellClassLabel = labelExtraction.getCellClassLabel(file.getName());

                    CellObject cellToAdd = new CellObject(new double[]{histogramMeanFeature, histogramStdDevFeature, imageEntropy, areaFeature}, cellClassLabel, null, -1);
                    datasetArrayList.add(cellToAdd);
                    csvDatasetArrayList.add(Double.toString(histogramMeanFeature) + "," + Double.toString(areaFeature) + "," + cellClassLabel+"\n");
                    long time = (System.nanoTime() - startTime) / 1000000;
                    timeDict.put("featureExtractionTime", (int)(timeDict.get("featureExtractionTime") + time));
                }
            } catch (Exception ex) {
                utility.print("\nThere was an error with image " + file.toString() + ": " + ex);
                ex.printStackTrace();
            }
        }
    }
}
