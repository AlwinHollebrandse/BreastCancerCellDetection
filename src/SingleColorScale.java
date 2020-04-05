import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.util.concurrent.Semaphore;

public class SingleColorScale {

    private Utility utility = new Utility();
    private BufferedImage originalImage;
    private String color;

    @FunctionalInterface
    interface FuncInterface extends OverHeadInterface.FuncInterface {
        // An abstract function
        void function(BufferedImage newImage, Semaphore semaphore, int x, int y);
    }

    // based on https://www.tutorialspoint.com/java_dip/grayscale_conversion.htm
    public FuncInterface fobj = (BufferedImage newImage, Semaphore semaphore, int x, int y) -> {
        Color c = new Color(originalImage.getRGB(x, y));
        if ("gray".equalsIgnoreCase(color)) {
            int gray = (int) (c.getRed() * 0.299) + (int) (c.getGreen() * 0.587) + (int) (c.getBlue() * 0.114);
            Color newColor = new Color(gray, gray, gray, c.getAlpha());
            newImage.setRGB(x, y, newColor.getRGB());
        } else if ("red".equalsIgnoreCase(color)) {
            Color newColor = new Color(c.getRed(), 0, 0, c.getAlpha());
            newImage.setRGB(x, y, newColor.getRGB());
        } else if ("green".equalsIgnoreCase(color)) {
            Color newColor = new Color(0, c.getGreen(), 0, c.getAlpha());
            newImage.setRGB(x, y, newColor.getRGB());
        } else if ("blue".equalsIgnoreCase(color)) {
            Color newColor = new Color(0, 0, c.getBlue(), c.getAlpha());
            newImage.setRGB(x, y, newColor.getRGB());
        } else {
            throw new NullPointerException("something went wrong getting the colors");
        }
    };

    public FuncInterface getFuncInterface() {
        return fobj;
    }


    public SingleColorScale (BufferedImage originalImage, String color) {
        this.originalImage = originalImage;
        this.color = color;
    }

    public BufferedImage convertToSingleColor() {
        ParallelMatrix parallelMatrix = new ParallelMatrix();
        BufferedImage grayImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        parallelMatrix.doInParallel(grayImage, getFuncInterface());
        return grayImage;
    }


    /**
     * convert a BufferedImage to RGB colorspace
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