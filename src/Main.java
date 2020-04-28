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
        boolean deletePreviousImages = true;
        boolean usePreviousImages = false;
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
        int k = -1;
        int numberOfFolds = -1;

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
        int featureExtractionTime = 0;
        int machineLearningTime = 0;
        int meanSquaredError = 0;
        int[] averageHistogram = new int[256];

        ArrayList<String> csvDatasetArrayList = new ArrayList<>();
        ArrayList<CellObject> datasetArrayList = new ArrayList<>();

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

                if (currentLine.toLowerCase().contains("deletepreviousimages")) {
                    String[] lineArray = currentLine.split(" ");
                    deletePreviousImages = Boolean.parseBoolean(lineArray[1]);
                }

                if (currentLine.toLowerCase().contains("usepreviousimage")) {
                    String[] lineArray = currentLine.split(" ");
                    usePreviousImages = Boolean.parseBoolean(lineArray[1]);
                }

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

                if (currentLine.toLowerCase().contains("featureextraction")) {
                    instructionList.add("FeatureExtraction");
                }

                if (currentLine.toLowerCase().contains("machinelearning")) {
                    String[] lineArray = currentLine.split(" ");
                    instructionList.add("MachineLearning");
                    k = Integer.parseInt(lineArray[1]);
                    numberOfFolds = Integer.parseInt(lineArray[2]);
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
                        singleColorTime += time;
                        output_file = new File(resultFileName);
                        ImageIO.write(workingImage, "jpg", output_file);
//                        print("Converting to GrayScale" + " Execution time in milliseconds : " + time);
                    } else if (usePreviousImages) {
                        workingImage = ImageIO.read( new File(resultFileName));
                    } else if (deletePreviousImages){
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
                    if (!usePreviousImages && instructionList.contains("Quantization")) {
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
                    } else if (usePreviousImages && instructionList.contains("Quantization")) {
                        workingImage = ImageIO.read( new File(resultFileName));
                    } else if (deletePreviousImages){
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
                    if (!usePreviousImages && instructionList.contains("SaltAndPepper")) {
                        long startTime = System.nanoTime();
                        NoiseAdder noiseAdder = new NoiseAdder(workingImage, "saltAndPepper", saltAndPepperNoiseRandomThreshold, saltAndPepperNoiseMean, saltAndPepperNoiseSigma);
                        workingImage = noiseAdder.addNoise();
                        long time = (System.nanoTime() - startTime) / 1000000;
                        saltAndPepperTime += time;
                        output_file = new File(resultFileName);
                        ImageIO.write(workingImage, "jpg", output_file);
//                        print("Adding Salt and Pepper Noise" + " Execution time in milliseconds : " + time);
                    } else if (usePreviousImages && instructionList.contains("SaltAndPepper")) {
                        workingImage = ImageIO.read( new File(resultFileName));
                    } else if (deletePreviousImages){
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
                    if (!usePreviousImages && instructionList.contains("Gaussian")) {
                        long startTime = System.nanoTime();
                        NoiseAdder noiseAdder = new NoiseAdder(workingImage, "gaussian", gaussianNoiseRandomThreshold, gaussianNoiseMean, gaussianNoiseSigma);
                        workingImage = noiseAdder.addNoise();
                        long time = (System.nanoTime() - startTime) / 1000000;
                        gaussianTime += time;
                        output_file = new File(resultFileName);
                        ImageIO.write(workingImage, "jpg", output_file);
//                        print("Adding Gaussian Noise" + " Execution time in milliseconds : " + time);
                    } else if (usePreviousImages && instructionList.contains("Gaussian")) {
                        workingImage = ImageIO.read( new File(resultFileName));
                    } else if (deletePreviousImages){
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
                    if (!usePreviousImages && instructionList.contains("LinearFilter")) {
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
                    } else if (usePreviousImages && instructionList.contains("LinearFilter")) {
                        workingImage = ImageIO.read( new File(resultFileName));
                    } else if (deletePreviousImages){
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
                    if (!usePreviousImages && instructionList.contains("MedianFilter")) {
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
                    } else if (usePreviousImages && instructionList.contains("MedianFilter")) {
                        workingImage = ImageIO.read( new File(resultFileName));
                    } else if (deletePreviousImages){
                        // if the file was not needed, delete the file from the relevant result folder if it existed
                        File imageResult = new File(resultFileName);
                        // delete the image result folder if it already existed. This way, no remnants from old runs remain
                        if (imageResult.exists()) {
                            if (!deleteDir(imageResult)) {
                                print("\nCould not delete old MedianFilter of: " + files[i].toString());
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
                        histogramTime += time;
//                        print("Histogram creation Execution time in milliseconds : " + time);

                        // print the starting histogram to a file
                        JFreeChart histogramGraph = graphHistogram.graphHistogram(histogram, graphTitle);
                        ChartUtilities.saveChartAsPNG(new File(resultFileName), histogramGraph, 700, 500);
                        HistogramFunctions histogramFunctions = new HistogramFunctions(workingImage, histogram);
                        averageHistogram = histogramFunctions.sumHistograms(averageHistogram);
                    } else if (deletePreviousImages){
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
                    if (!usePreviousImages && instructionList.contains("HistogramEqualization")) {
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
                    } else if (usePreviousImages && instructionList.contains("HistogramEqualization")) {
                        workingImage = ImageIO.read( new File(resultFileName));
                    } else if (deletePreviousImages){
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
                    if (!usePreviousImages && instructionList.contains("EdgeDetection")) {
                        long startTime = System.nanoTime();
                        EdgeDetection edgeDetection = new EdgeDetection();
                        edgeMap = edgeDetection.edgeDetection(workingImage);
                        long time = (System.nanoTime() - startTime) / 1000000;
                        edgeDetectionTime += time;
                        output_file = new File(resultFileName);
                        ImageIO.write(edgeMap, "jpg", output_file);
//                        print("Edge Detection" + " Execution time in milliseconds : " + time);
                    } else if (usePreviousImages && instructionList.contains("EdgeDetection")) {
                        edgeMap = ImageIO.read( new File(resultFileName));
                    } else if (deletePreviousImages){
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
                    if (!usePreviousImages && instructionList.contains("HistogramThresholdingSegmentation")) {
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
                    } else if (usePreviousImages && instructionList.contains("HistogramThresholdingSegmentation")) {
                        segmentationImage = ImageIO.read( new File(resultFileName));
                    } else if (deletePreviousImages){
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
                    if (!usePreviousImages && instructionList.contains("KMeansSegmentation")) {
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
                    } else if (usePreviousImages && instructionList.contains("KMeansSegmentation")) {
                        segmentationImage = ImageIO.read( new File(resultFileName));
                    } else if (deletePreviousImages){
                        // if the file was not needed, delete the file from the relevant result folder if it existed
                        File imageResult = new File(resultFileName);
                        // delete the image result folder if it already existed. This way, no remnants from old runs remain
                        if (imageResult.exists()) {
                            if (!deleteDir(imageResult)) {
                                print("\nCould not delete old kmeans segmentation of: " + files[i].toString());
                            }
                        }
                    }

                    resultFileName = directoryPath + "erosion.jpg";
                    if (!usePreviousImages && instructionList.contains("Erosion")) {
                        Utility utility = new Utility();
                        if (utility.checkIfAllBlackImage(segmentationImage)) {
                            long startTime = System.nanoTime();
                            ThresholdSegmentation thresholdSegmentation = new ThresholdSegmentation(workingImage, histogram);
                            segmentationImage = thresholdSegmentation.thresholdSegmentation();
                            histogramThresholdingSegmentationTime += (System.nanoTime() - startTime) / 1000000;
                            String segmentationFileName = directoryPath + "histogramThresholdingSegmentation.jpg";
                            output_file = new File(segmentationFileName);
                            ImageIO.write(segmentationImage, "jpg", output_file);
                        }

                        long startTime = System.nanoTime();
                        MorphologicalFunctions morphologicalFunctions = new MorphologicalFunctions(segmentationImage, "erosion", erosionFilterWidth, erosionFilterHeight, erosionFilterColors);
                        segmentationImage = morphologicalFunctions.morphologicalFunctions();
                        long time = (System.nanoTime() - startTime) / 1000000;
                        erosionTime += time;
                        output_file = new File(resultFileName);
                        ImageIO.write(segmentationImage, "jpg", output_file);
//                        print("Edge Detection" + " Execution time in milliseconds : " + time);
                    } else if (usePreviousImages && instructionList.contains("Erosion")) {
                        segmentationImage = ImageIO.read( new File(resultFileName));
                    } else if (deletePreviousImages){
                        // if the file was not needed, delete the file from the relevant result folder if it existed
                        File imageResult = new File(resultFileName);
                        // delete the image result folder if it already existed. This way, no remnants from old runs remain
                        if (imageResult.exists()) {
                            if (!deleteDir(imageResult)) {
                                print("\nCould not delete old erosion of: " + files[i].toString());
                            }
                        }
                    }

                    resultFileName = directoryPath + "dilation.jpg";
                    if (!usePreviousImages && instructionList.contains("Dilation")) {
                        Utility utility = new Utility();
                        if (utility.checkIfAllBlackImage(segmentationImage)) {
                            long startTime = System.nanoTime();
                            ThresholdSegmentation thresholdSegmentation = new ThresholdSegmentation(workingImage, histogram);
                            segmentationImage = thresholdSegmentation.thresholdSegmentation();
                            histogramThresholdingSegmentationTime += (System.nanoTime() - startTime) / 1000000;
                            String segmentationFileName = directoryPath + "histogramThresholdingSegmentation.jpg";
                            output_file = new File(segmentationFileName);
                            ImageIO.write(segmentationImage, "jpg", output_file);
                        }

                        long startTime = System.nanoTime();
                        MorphologicalFunctions morphologicalFunctions = new MorphologicalFunctions(segmentationImage, "dilation", dilationFilterWidth, dilationFilterHeight, dilationFilterColors);
                        segmentationImage = morphologicalFunctions.morphologicalFunctions();
                        long time = (System.nanoTime() - startTime) / 1000000;
                        dilationTime += time;
                        output_file = new File(resultFileName);
                        ImageIO.write(segmentationImage, "jpg", output_file);
//                        print("Edge Detection" + " Execution time in milliseconds : " + time);
                    } else if (usePreviousImages && instructionList.contains("Dilation")) {
                        segmentationImage = ImageIO.read( new File(resultFileName));
                    } else if (deletePreviousImages){
                        // if the file was not needed, delete the file from the relevant result folder if it existed
                        File imageResult = new File(resultFileName);
                        // delete the image result folder if it already existed. This way, no remnants from old runs remain
                        if (imageResult.exists()) {
                            if (!deleteDir(imageResult)) {
                                print("\nCould not delete old dilation of: " + files[i].toString());
                            }
                        }
                    }

                    // TODO add a "usePreviousImages" option for FeatureExtraction
                    if (instructionList.contains("FeatureExtraction")) {

                        if (histogram == null) {
                            GraphHistogram graphHistogram = new GraphHistogram(color);
                            histogram = graphHistogram.createHistogram(workingImage);
                        }

                        Utility utility = new Utility();
                        if (utility.checkIfAllBlackImage(segmentationImage)) {
                            long startTime = System.nanoTime();
                            ThresholdSegmentation thresholdSegmentation = new ThresholdSegmentation(workingImage, histogram);
                            segmentationImage = thresholdSegmentation.thresholdSegmentation();
                            histogramThresholdingSegmentationTime += (System.nanoTime() - startTime) / 1000000;
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
                        String cellClassLabel = labelExtraction.getCellClassLabel(files[i].getName());

                        CellObject cellToAdd = new CellObject(new double[]{histogramMeanFeature, histogramStdDevFeature, imageEntropy, areaFeature}, cellClassLabel, null, -1);
                        datasetArrayList.add(cellToAdd);
                        csvDatasetArrayList.add(Double.toString(histogramMeanFeature) + "," + Double.toString(areaFeature) + "," + cellClassLabel+"\n");
                        featureExtractionTime += (System.nanoTime() - startTime) / 1000000;
                    }
                } catch (Exception ex) {
                    print("\nThere was an error with image " + files[i].toString() + ": " + ex);
                    ex.printStackTrace();
                }
            }
            progressBar.next();
        }

        createCSV(csvDatasetArrayList, deletePreviousImages);

        try {
            if (instructionList.contains("MachineLearning")) {
                boolean doML = true;
                if (datasetArrayList.size() <= 0) {
                    print("There were cells found to classify");
                    doML = false;
                    machineLearningTime = 0;
                }

                if (doML) {
                    long startTime = System.nanoTime();
                    KNN knn = new KNN(k, datasetArrayList);

                    // normalize dataset
                    knn.normalizeDataset(datasetArrayList);

                    // split dataset
                    ArrayList<ArrayList<ArrayList<CellObject>>> splitKFoldSets = knn.getKFolds(numberOfFolds, datasetArrayList);
                    ArrayList<ArrayList<CellObject>> trainSets = splitKFoldSets.get(0);
                    ArrayList<ArrayList<CellObject>> testSets = splitKFoldSets.get(1);

                    //k fold
                    print("");
                    ArrayList<Double> accuracyList = new ArrayList<>();
                    for (int fold = 0; fold < numberOfFolds; fold++) {
                        knn = new KNN(k, trainSets.get(fold));
                        knn.classifyTestSet(testSets.get(fold));
                        double accuracy = knn.calcAccuracy(testSets.get(fold));
                        accuracyList.add(accuracy);
                    }

                    double accuracy = 0;
                    for (int i = 0; i < accuracyList.size(); i++) {
                        accuracy += accuracyList.get(i);
                    }
                    accuracy = accuracy / accuracyList.size();
                    String foldAccuracyString = "accuracy per fold: " + accuracyList.toString();
                    print(foldAccuracyString);
                    String avgAccuracyString = "average accuracy: " + accuracy;
                    print(avgAccuracyString);

                    machineLearningTime += (System.nanoTime() - startTime) / 1000000;
                }
            }
        } catch (Exception ex) {
            print("\nThere was an error while doing Machine Learning: " + ex);
            ex.printStackTrace();
        }

        print("\n\nFinal Metrics:");
        getAverageHistogram(averageHistogram, files.length, color);
        if (singleColorTime > 0) {
            print("\nConverting to a single color processing time for the entire batch (ms): " + singleColorTime);
            print("Average converting to a single color processing time (ms): " + singleColorTime / files.length);
        }
        if (quantizationTime > 0) {
            print("\nQuantization processing time for the entire batch (ms): " + quantizationTime);
            print("Average quantization processing time (ms): " + quantizationTime / files.length);
            print("Average meanSquaredError: " + meanSquaredError / files.length);
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
        if (featureExtractionTime > 0) {
            print("\nFeature extraction  processing time for the entire batch (ms): " + featureExtractionTime);
            print("Average feature extraction processing time (ms): " + featureExtractionTime / files.length);
        }
        if (machineLearningTime > 0) {
            print("\nmachineLearningTime  processing time for the entire batch (ms): " + machineLearningTime);
        }
        print("\nTotal RunTime for all operations (without image exporting) (s): " + ((singleColorTime + quantizationTime + saltAndPepperTime +
                gaussianTime + linearFilterTime + medianFilterTime + histogramTime + equalizationTime + edgeDetectionTime +
                histogramThresholdingSegmentationTime + kMeansSegmentationTime + erosionTime + dilationTime + featureExtractionTime +
                machineLearningTime) / 1000));
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


    private static void createCSV(ArrayList<String> csvDatasetArrayList, boolean deletePreviousImages) {
//        System.out.println("\n" + csvDatasetArrayList.toString());
        String fileName = "Dataset.csv";
        if (csvDatasetArrayList.size() > 0) {
            try {
                File file = new File(fileName);
                FileWriter output = new FileWriter(file);

                String headerLine = "Mean,Area,Label\n";
                output.append(headerLine);

                for (int i = 0; i < csvDatasetArrayList.size(); i++) {
                    output.append(csvDatasetArrayList.get(i));
                }
                output.close();
            } catch (IOException ex) {
                String errorMessage = "Could not create the dataset CSV: " + ex;
                print(errorMessage);
            }
        } else if (deletePreviousImages){
            // if the file was not needed, delete the file from the relevant result folder if it existed
            File imageResult = new File(fileName);
            // delete the image result folder if it already existed. This way, no remnants from old runs remain
            if (imageResult.exists()) {
                if (!deleteDir(imageResult)) {
                    print("\nCould not delete cvs dataset");
                }
            }
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
