import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

import java.io.File;
import java.util.Map;

public class Metrics {
    
    public void printMetrics(Map<String, Integer> timeDict, int[] averageHistogram, int numberOfFiles, String color, int meanSquaredError, long realStartTime) {
        Utility utility = new Utility();
        utility.print("\n\nFinal Time Metrics:");
        getAverageHistogram(averageHistogram, numberOfFiles, color);
        if (timeDict.get("singleColorTime") > 0) {
            utility.print("\nConverting to a single color processing time for the entire batch (ms): " + timeDict.get("singleColorTime"));
            utility.print("Average converting to a single color processing time (ms): " + timeDict.get("singleColorTime") / numberOfFiles);
        }
        if (timeDict.get("quantizationTime") > 0) {
            utility.print("\nQuantization processing time for the entire batch (ms): " + timeDict.get("quantizationTime"));
            utility.print("Average quantization processing time (ms): " + timeDict.get("quantizationTime") / numberOfFiles);
            utility.print("Average meanSquaredError: " + meanSquaredError / numberOfFiles);
        }
        if (timeDict.get("saltAndPepperTime") > 0) {
            utility.print("\nAdding salt and pepper noise processing time for the entire batch (ms): " + timeDict.get("saltAndPepperTime"));
            utility.print("Average adding salt and pepper noise processing time (ms): " + timeDict.get("saltAndPepperTime") / numberOfFiles);
        }
        if (timeDict.get("gaussianTime") > 0) {
            utility.print("\nAdding gaussian noise processing time for the entire batch (ms): " + timeDict.get("gaussianTime"));
            utility.print("Average adding gaussian noise processing time (ms): " + timeDict.get("gaussianTime") / numberOfFiles);
        }
        if (timeDict.get("linearFilterTime") > 0) {
            utility.print("\nLinear filter processing time for the entire batch (ms): " + timeDict.get("linearFilterTime"));
            utility.print("Average linear filter processing time (ms): " + timeDict.get("linearFilterTime") / numberOfFiles);
        }
        if (timeDict.get("medianFilterTime") > 0) {
            utility.print("\nMedian filter processing time for the entire batch (ms): " + timeDict.get("medianFilterTime"));
            utility.print("Average median filter processing time (ms): " + timeDict.get("medianFilterTime") / numberOfFiles);
        }
        if (timeDict.get("histogramTime") > 0) {
            utility.print("\nHistogram creation processing time for the entire batch (ms): " + timeDict.get("histogramTime"));
            utility.print("Average histogram creation processing time (ms): " + timeDict.get("histogramTime") / numberOfFiles);
        }
        if (timeDict.get("equalizationTime") > 0) {
            utility.print("\nEqualized histogram creation processing time for the entire batch (ms): " + timeDict.get("equalizationTime"));
            utility.print("Average equalized histogram creation processing time (ms): " + timeDict.get("equalizationTime") / numberOfFiles);
        }
        if (timeDict.get("edgeDetectionTime") > 0) {
            utility.print("\nEdge detection creation processing time for the entire batch (ms): " + timeDict.get("edgeDetectionTime"));
            utility.print("Average edge detection processing time (ms): " + timeDict.get("edgeDetectionTime") / numberOfFiles);
        }
        if (timeDict.get("histogramThresholdingSegmentationTime") > 0) {
            utility.print("\nHistogram thresholding segmentation time creation processing time for the entire batch (ms): " + timeDict.get("histogramThresholdingSegmentationTime"));
            utility.print("Average histogram thresholding segmentation processing time (ms): " + timeDict.get("histogramThresholdingSegmentationTime") / numberOfFiles);
        }
        if (timeDict.get("kMeansSegmentationTime") > 0) {
            utility.print("\nK means segmentation time creation processing time for the entire batch (ms): " + timeDict.get("kMeansSegmentationTime"));
            utility.print("Average k means segmentation processing time (ms): " + timeDict.get("kMeansSegmentationTime") / numberOfFiles);
        }
        if (timeDict.get("erosionTime") > 0) {
            utility.print("\nErosion time creation processing time for the entire batch (ms): " + timeDict.get("erosionTime"));
            utility.print("Average k means segmentation processing time (ms): " + timeDict.get("erosionTime") / numberOfFiles);
        }
        if (timeDict.get("dilationTime") > 0) {
            utility.print("\nDilation time creation processing time for the entire batch (ms): " + timeDict.get("dilationTime"));
            utility.print("Average k means segmentation processing time (ms): " + timeDict.get("dilationTime") / numberOfFiles);
        }
        if (timeDict.get("featureExtractionTime") > 0) {
            utility.print("\nFeature extraction processing time for the entire batch (ms): " + timeDict.get("featureExtractionTime"));
            utility.print("Average feature extraction processing time (ms): " + timeDict.get("featureExtractionTime") / numberOfFiles);
        }
        if (timeDict.get("machineLearningTime") > 0) {
            utility.print("\nMachine Learning processing time for the entire batch (ms): " + timeDict.get("machineLearningTime"));
        }
        utility.print("\nTotal RunTime for all operations (without image exporting) (s): " + ((timeDict.get("singleColorTime") + timeDict.get("quantizationTime") + timeDict.get("saltAndPepperTime") +
                timeDict.get("gaussianTime") + timeDict.get("linearFilterTime") + timeDict.get("medianFilterTime") + timeDict.get("histogramTime") + timeDict.get("equalizationTime") + timeDict.get("edgeDetectionTime") +
                timeDict.get("histogramThresholdingSegmentationTime") + timeDict.get("kMeansSegmentationTime") + timeDict.get("erosionTime") + timeDict.get("dilationTime") + timeDict.get("featureExtractionTime") +
                timeDict.get("machineLearningTime")) / 1000));
        utility.print("Real run time (s): " + (System.nanoTime() - realStartTime) / 1000000000);
    }

    private static void getAverageHistogram(int[] histogram, int divisor, String color) {
        Utility utility = new Utility();
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
            utility.print("Could not create specified directory for the average histogram. Skipping this operation. Error: " + e);
        }
    }
}
