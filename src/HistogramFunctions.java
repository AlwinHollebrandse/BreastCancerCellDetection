import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.Semaphore;

public class HistogramFunctions {

    private BufferedImage originalImage;
    private int[] histogram;
    private int[] imageLUT;

    @FunctionalInterface
    interface FuncInterface extends OverHeadInterface.FuncInterface {
        // An abstract function
        void function(BufferedImage newImage, Semaphore semaphore, int x, int y);
    }

    public HistogramFunctions.FuncInterface fobj = (BufferedImage newImage, Semaphore semaphore, int x, int y) -> {
        //get original pixel color
        Color c = new Color(originalImage.getRGB(x, y));
        int alpha = c.getAlpha();
        int gray = (int) (c.getRed() * 0.299) + (int) (c.getGreen() * 0.587) + (int) (c.getBlue() * 0.114);

        // Use the look up table to get the equalized value
        int nVal = imageLUT[gray];

        // set the new value
        Color newColor = new Color(nVal, nVal, nVal, alpha);
        newImage.setRGB(x, y, newColor.getRGB());
    };

    public HistogramFunctions.FuncInterface getFuncInterface() {
        return fobj;
    }


    public HistogramFunctions(BufferedImage originalImage, int[] histogram) {
        this.originalImage = originalImage;
        this.histogram = histogram;
    }

    // created by modifying https://github.com/schauhan19/Histogram-Equalization/blob/master/HistogramEQ.java
    public BufferedImage equalizedImage() {
        // Creates the look up table that is used to reassign pixels
        double scaleFactor = 255.0 / (double) (originalImage.getWidth() * originalImage.getHeight());
        int sum = 0;
        imageLUT = new int[256];
        for (int i = 0; i < 256; i++) {
            sum += histogram[i];
            int valr = (int) (sum * scaleFactor);
            if (valr > 255) {
                imageLUT[i] = 255;
            } else imageLUT[i] = valr;
        }
//        System.out.println("imageLUT: " + Arrays.toString(imageLUT));

        BufferedImage equalizedImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), originalImage.getType());

        ParallelMatrix parallelMatrix = new ParallelMatrix();
        parallelMatrix.doInParallel(equalizedImage, getFuncInterface());
        return equalizedImage;
    }

    public int[] sumHistograms (int[] totalHistogram) {
        int[] sum = new int[256];

        for (int i = 0; i < sum.length; i++) {
            sum[i] = totalHistogram[i] + histogram[i];
        }

        return sum;
    }
}