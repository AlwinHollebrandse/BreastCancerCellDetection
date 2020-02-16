import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class GraphHistogram extends JFrame {

    public int[] createHistogram (BufferedImage image) { // TODO move to histogram functions?
        int[] histogram = new int[256];//256 color values of gray
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Color c = new Color(image.getRGB(x, y));
                int keyValue = (int)(c.getRed() * 0.299) + (int)(c.getGreen() * 0.587) + (int)(c.getBlue() *0.114);
                histogram[keyValue]++;
            }
        }
        return histogram;
    }

    public JFreeChart graphHistogram (int[] histogram, String graphTitle) {
        JFreeChart histogramGraph = ChartFactory.createBarChart(graphTitle, "Category", "Frequency", createDataset(histogram), PlotOrientation.VERTICAL, false, false, false);

        ChartPanel chartPanel = new ChartPanel(histogramGraph);
        chartPanel.setPreferredSize(new java.awt.Dimension(700 , 500));
        setContentPane(chartPanel);

        return histogramGraph;
    }


    private static CategoryDataset createDataset(int[] histogram) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (int i = 0; i < histogram.length; i++) {
            String dataName = Integer.toString(i);
            dataset.addValue(histogram[i], "test", dataName);
        }

        return dataset;
    }
}
