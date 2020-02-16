import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;

public class GrayScale {

    @FunctionalInterface
    interface FuncInterface extends OverHeadInterface.FuncInterface {
        // An abstract function
        void function(BufferedImage originalImage, BufferedImage newImage, int x, int y, String color, double randomThreshold, String filterType, int filterWidth, int filterHeight, int[] weights, double scalar);
    }

    // based on https://www.tutorialspoint.com/java_dip/grayscale_conversion.htm
    public FuncInterface fobj = (BufferedImage originalImage, BufferedImage newImage, int x, int y, String color, double randomThreshold, String filterType, int filterWidth, int filterHeight, int[] weights, double scalar) -> {
        Color c = new Color(originalImage.getRGB(x, y));
        if ("gray".equalsIgnoreCase(color)) {
            int gray = (int) (c.getRed() * 0.299) + (int) (c.getGreen() * 0.587) + (int) (c.getBlue() * 0.114);
//                    int gray = c.getRed() + c.getGreen() + c.getBlue(); // TODO check what gray to use
            Color newColor = new Color(gray, gray, gray);
            newImage.setRGB(x, y, newColor.getRGB());
        } else if ("red".equalsIgnoreCase(color)) {
            Color newColor = new Color(c.getRed(), 0, 0);
            newImage.setRGB(x, y, newColor.getRGB());
        } else if ("green".equalsIgnoreCase(color)) {
            Color newColor = new Color(0, c.getGreen(), 0);
            newImage.setRGB(x, y, newColor.getRGB());
        } else if ("blue".equalsIgnoreCase(color)) {
            Color newColor = new Color(0, 0, c.getBlue());
            newImage.setRGB(x, y, newColor.getRGB());
        } else {
            throw new NullPointerException("something went wrong getting the colors");
        }
    };

    public FuncInterface getFuncInterface() {
        return fobj;
    }


    public BufferedImage convertToSingleColor(BufferedImage originalImage, String color) {
        ParallelMatrix parallelMatrix = new ParallelMatrix();
//        ProgressBar bar = new ProgressBar("Converting to GrayScale", originalImage.getWidth() * originalImage.getHeight());
        BufferedImage grayImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);

        // grayScale lambda does not use: randomThreshold, filterType, filterWidth, filterHeight, weights, or scalar
        parallelMatrix.doInParallel(originalImage, grayImage, "Converting to GrayScale",
                getFuncInterface(), color, 0, null, 0, 0, null,  0);
        return grayImage;
    }


    /**
     * convert a BufferedImage to RGB colourspace
     */
    // from https://blog.idrsolutions.com/2009/10/converting-java-bufferedimage-between-colorspaces/
    public BufferedImage convertToGrayScale(BufferedImage originalImage) { // TODO is this allowed? pretty sure its not
        ProgressBar bar = new ProgressBar("Converting to GrayScale", 1);

        BufferedImage grayScaleImage = null;
        try {
            grayScaleImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
            ColorConvertOp xformOp = new ColorConvertOp(null);
            xformOp.filter(originalImage, grayScaleImage);
            bar.next();
        } catch (Exception e) {
            System.out.println("Exception " + e + " converting image");
        }
        return grayScaleImage;
    }

    // OR this from: https://stackoverflow.com/questions/9131678/convert-a-rgb-image-to-grayscale-image-reducing-the-memory-in-java
//    ImageFilter filter = new GrayFilter(true, 50);
//    ImageProducer producer = new FilteredImageSource(colorImage.getSource(), filter);
//    Image mage = Toolkit.getDefaultToolkit().createImage(producer);
}