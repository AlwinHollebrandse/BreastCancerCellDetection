import java.awt.image.BufferedImage;

public class FeatureExtraction {

    //possible features to use:
//    histogram stats, maybe mean, ,stnd dev, the standard deviation; the entropy; the median; percentiles etc.
//    area of largest object/permiter... or just area of all "objects"..aka white pixels
//    symmetry
//    max-min radii
    // TODO these arent weighted the same... like really skewed
    public double getHistogramMean(int[] histogram) {
        double mean = 0;
        int numberOfPixels = 0;
        for (int i = 0; i < histogram.length; i++) {
            mean += histogram[i] * i;
            numberOfPixels += histogram[i];
        }
        return mean / numberOfPixels;
    }

    public  double getHistogramStdDev(int[] histogram) {
        double mean = getHistogramMean(histogram);
        double totalSubMeanSquared = 0;
        int numberOfPixels = 0;
        for (int i = 0; i < histogram.length; i++) {
            totalSubMeanSquared += histogram[i] * Math.pow(i - mean, 2);
            numberOfPixels += histogram[i];
        }
        double tempMean = totalSubMeanSquared/numberOfPixels;
        return Math.sqrt(tempMean);
    }

    public double getObjectArea(BufferedImage segmentedImage) {
        double area = 0; // NOTE area == # of white pixels
        Utility utility = new Utility();
        for (int x = 0; x < segmentedImage.getWidth(); x++) {
            for (int y = 0; y < segmentedImage.getHeight(); y++) {
                int pixelColor = utility.getSingleColor(segmentedImage.getRGB(x, y), "gray");
                if (pixelColor != 0) {
                    area ++;
                }
            }
        }
        return area;
    }

//    Entropy is a statistical measure of randomness that can be used to characterize the texture of the input image.
//    Entropy is defined as -sum(p.*log2(p)), where p contains the normalized histogram counts returned from imhist.
    public double getImageEntropy(int[] histogram) {
        int numberOfPixels = 0;
        for (int i = 0; i < histogram.length; i++) {
            numberOfPixels += histogram[i];
        }

        double entropy = 0;
        for (int i = 0; i < histogram.length; i++) {
            double probOfPixel = histogram[i] / numberOfPixels;
            entropy += probOfPixel * (Math.log(probOfPixel) / Math.log(2));
        }

        return -entropy;
    }
}
