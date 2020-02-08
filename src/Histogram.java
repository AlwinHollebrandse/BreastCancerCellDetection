import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.image.BufferedImage;

public class Histogram extends JFrame {

    public static double[] createHistogram (BufferedImage image) {
        double[] histogram = new double[256];//256 color values of gray
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int keyValue = (image.getRGB(x, y)) & 0xFF;
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
}
