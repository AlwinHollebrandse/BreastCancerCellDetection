import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Array;
import java.util.Arrays;

public class Histogram extends JFrame {

    public double[] createHistogram (BufferedImage image) {
        double[] histogram = new double[256];//256 color values of gray
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
//                int keyValue = (image.getRGB(x, y)) & 0xFF;

                Color c = new Color(image.getRGB(x, y));
                int keyValue = (int)(c.getRed() * 0.299) + (int)(c.getGreen() * 0.587) + (int)(c.getBlue() *0.114);

                histogram[keyValue]++;
            }
        }
        return histogram;
    }

    public JFreeChart graphHistogram (double[] histogram, String graphTitle)  {
        JFreeChart histogramGraph = ChartFactory.createBarChart(graphTitle, "Category", "Frequency", createDataset(histogram), PlotOrientation.VERTICAL, true, true, false);

        ChartPanel chartPanel = new ChartPanel(histogramGraph);
        chartPanel.setPreferredSize(new java.awt.Dimension(700 , 500));
        setContentPane(chartPanel);

        return histogramGraph;
    }

    private static CategoryDataset createDataset(double[] histogram) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (int i = 0; i < histogram.length; i++) {
            String dataName = Integer.toString(i);
            dataset.addValue(histogram[i], "test", dataName);
        }

        return dataset;
    }


    // Using steps from http://terminalcoders.blogspot.com/2017/02/histogram-equalisation-in-java.html
    public double[] histogramEqualization(double[] histogram) {
        double[] equalizedHistogram = new double[256];
        equalizedHistogram[0] = histogram[0];

        for (int i = 1; i < histogram.length; i++) {
            equalizedHistogram[i] = equalizedHistogram[i-1] + histogram[i];
        }

        System.out.println("equalizedHistogram: " + Arrays.toString(equalizedHistogram));

        return equalizedHistogram;
    }


//    private float[] getThing (double[] equalizedHistogram, int numberOfPixels) {
//        float[] arr = new float[256];
//        for(int i = 0; i < 256; i++) {
//            arr[i] = (float)((equalizedHistogram[i]*255.0)/(float)numberOfPixels);
//        }
//
//        System.out.println("thing: " + Arrays.toString(arr));
//        return arr;
//    }


    public BufferedImage equalizedImage (BufferedImage originalImage, double[] equalizedHistogram) {
        int numberOfPixels = originalImage.getWidth() * originalImage.getHeight();
        float[] arr = new float[256];
        for(int i = 0; i < 256; i++) {
            arr[i] = (float)((equalizedHistogram[i]*255.0)/(float)numberOfPixels);
        }

        System.out.println("thing: " + Arrays.toString(arr));
//        return arr;

        BufferedImage equalizedImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), originalImage.getType());

        for (int x = 0; x < originalImage.getWidth(); x++) {
            for (int y = 0; y < originalImage.getHeight(); y++) {
//                int keyValue = (image.getRGB(x, y)) & 0xFF;

                Color c = new Color(originalImage.getRGB(x, y));
                int keyValue = (int)(c.getRed() * 0.299) + (int)(c.getGreen() * 0.587) + (int)(c.getBlue() *0.114);

                int nVal = (int) arr[keyValue];

                Color newColor = new Color(nVal, nVal, nVal);
                equalizedImage.setRGB(x, y, newColor.getRGB());

//                er.setSample(x, y, 0, nVal);
            }
        }

        return equalizedImage;
    }
//
//
//    BufferedImage equalize(BufferedImage src){
//        BufferedImage nImg = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
//        WritableRaster wr = src.getRaster();
//        WritableRaster er = nImg.getRaster();
//        int totpix= wr.getWidth()*wr.getHeight();
//        int[] histogram = new int[256];
// 
//        for (int x = 1; x < wr.getWidth(); x++) {
//            for (int y = 1; y < wr.getHeight(); y++) {
//                histogram[wr.getSample(x, y, 0)]++;
//            }
//        }
//         
//        int[] chistogram = new int[256];
//        chistogram[0] = histogram[0];
//        for(int i=1;i<256;i++){
//            chistogram[i] = chistogram[i-1] + histogram[i];
//        }
//         
//        float[] arr = new float[256];
//        for(int i=0;i<256;i++){
//            arr[i] =  (float)((chistogram[i]*255.0)/(float)totpix);
//        }
//         
//        for (int x = 0; x < wr.getWidth(); x++) {
//            for (int y = 0; y < wr.getHeight(); y++) {
//                int nVal = (int) arr[wr.getSample(x, y, 0)];
//                er.setSample(x, y, 0, nVal);
//            }
//        }
//        nImg.setData(er);
//        return nImg;
//    }


}
