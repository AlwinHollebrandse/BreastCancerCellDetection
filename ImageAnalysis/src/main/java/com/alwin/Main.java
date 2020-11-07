package com.alwin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Main {

    //Takes in 2 command line args: the file containing all of the images, and the file containing the instructions
    public static void main(String args[]) {

        if (args.length != 2) {
            System.out.println("Did not provided the correct argts. <image folder> <instructions.txt>");
            System.exit(0);
        }

        String imageFilesLocation = args[0];
        String instructions = args[1];

        Utility utility = new Utility();
        utility.print("imageFilesLocation: " + imageFilesLocation);
        utility.print("instructions: " + instructions);

        // ***************************** Instruction Parsing Below *********************************
        // operation params
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

        Map<String, Integer> timeDict = new HashMap<>();
        timeDict.put("singleColorTime", 0);
        timeDict.put("quantizationTime", 0);
        timeDict.put("saltAndPepperTime", 0);
        timeDict.put("gaussianTime", 0);
        timeDict.put("linearFilterTime", 0);
        timeDict.put("medianFilterTime", 0);
        timeDict.put("histogramTime", 0);
        timeDict.put("equalizationTime", 0);
        timeDict.put("edgeDetectionTime", 0);
        timeDict.put("histogramThresholdingSegmentationTime", 0);
        timeDict.put("kMeansSegmentationTime", 0);
        timeDict.put("erosionTime", 0);
        timeDict.put("dilationTime", 0);
        timeDict.put("featureExtractionTime", 0);
        timeDict.put("machineLearningTime", 0);

        int meanSquaredError = 0;
        int[] averageHistogram = new int[256];

        ArrayList<String> csvDatasetArrayList = new ArrayList<>();
        ArrayList<CellObject> datasetArrayList = new ArrayList<>();

        long realStartTime = System.nanoTime();

        // the file containing all of the images error prevention
        File path = new File(imageFilesLocation);
        if (!path.exists()) {
            utility.print("Enter a valid path that holds the images");
            System.exit(1);
        }
        File [] files = path.listFiles();
        if (files.length <= 0) {
            utility.print("The provided image folder has no images");
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
            utility.print("Enter a valid path that has operation instructions");
            System.exit(1);
        }
        if (instructionList.size() <= 0) {
            utility.print("The provided instruction file has no operation instructions");
            System.exit(1);
        }


        // ***************************** OperationsCalls Below *********************************
        // TODO change params to a single "operationcallParam" custom type for cleaner code
        ImageOperationsCall imageOperationsCall = new ImageOperationsCall(
                utility, deletePreviousImages, usePreviousImages,
                color, quantizationScale, saltAndPepperNoiseRandomThreshold, saltAndPepperNoiseMean,
                saltAndPepperNoiseSigma, gaussianNoiseRandomThreshold, gaussianNoiseMean, gaussianNoiseSigma,
                linearFilterWidth, linearFilterHeight, linearFilterWeights, medianFilterWidth, medianFilterHeight,
                medianFilterWeights, erosionFilterWidth, erosionFilterHeight, erosionFilterColors, dilationFilterWidth,
                dilationFilterHeight, dilationFilterColors, timeDict, meanSquaredError, averageHistogram,
                csvDatasetArrayList, datasetArrayList, instructionList
        );


        ProgressBar progressBar = new ProgressBar("Processing Images", files.length);
        // loop through all images and do each specified operation
        for (int i = 0; i < files.length; i++){

//            if (i >= 1)
//                break;
            imageOperationsCall.imageOperationsCall(files[i]);
            progressBar.next();
        }

        createCSV(csvDatasetArrayList, deletePreviousImages);

        try {
            if (instructionList.contains("MachineLearning")) {
                boolean doML = true;
                if (datasetArrayList.size() <= 0) {
                    utility.print("There were cells found to classify");
                    doML = false;
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
                    utility.print("");
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
                    String foldAccuracyString = "Accuracy per fold: " + accuracyList.toString();
                    utility.print(foldAccuracyString);
                    String avgAccuracyString = "Average accuracy: " + accuracy;
                    utility.print(avgAccuracyString);
                    long time = (System.nanoTime() - startTime) / 1000000;
                    timeDict.put("machineLearningTime", (int)(timeDict.get("machineLearningTime") + time));
                }
            }
        } catch (Exception ex) {
            utility.print("\nThere was an error while doing Machine Learning: " + ex);
            ex.printStackTrace();
        }

        Metrics metrics = new Metrics();
        metrics.printMetrics(timeDict, averageHistogram, (files.length), color, meanSquaredError, realStartTime);
    }


    private static void createCSV(ArrayList<String> csvDatasetArrayList, boolean deletePreviousImages) {
        Utility utility = new Utility();
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
                utility.print(errorMessage);
            }
        } else if (deletePreviousImages){
            // if the file was not needed, delete the file from the relevant result folder if it existed
            File imageResult = new File(fileName);
            // delete the image result folder if it already existed. This way, no remnants from old runs remain
            if (imageResult.exists()) {
                if (!utility.deleteDir(imageResult)) {
                    utility.print("\nCould not delete cvs dataset");
                }
            }
        }
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
}
