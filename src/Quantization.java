import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Quantization {

    private BufferedImage originalImage;
    private String color;
    private ArrayList<Integer> quantizationArray = new ArrayList<>();

    @FunctionalInterface
    interface FuncInterface extends OverHeadInterface.FuncInterface {
        // An abstract function
        void function(BufferedImage newImage, int x, int y);
    }

    // based on https://www.tutorialspoint.com/java_dip/grayscale_conversion.htm
    public FuncInterface fobj = (BufferedImage newImage, int x, int y) -> {
        Boolean redDone = false;
        Boolean greenDone = false;
        Boolean blueDone = false;

        Color c = new Color(originalImage.getRGB(x, y));
        int red = c.getRed();
        int green = c.getGreen();
        int blue = c.getBlue();

        // sets the pixel value to the floor of valid color options. ex: quantizationArray =[0,10,20,30] pixel value enters at 19. 19 > 10 but 19 < 20, so pixel value gets set to 10.
        for (int i = 1; i < quantizationArray.size(); i++) {
            if (!redDone && red < quantizationArray.get(i)) {
                red = quantizationArray.get(i - 1);
                redDone = true;
            }
            if (!greenDone && green < quantizationArray.get(i)) {
                green = quantizationArray.get(i - 1);
                greenDone = true;
            }
            if (!blueDone && blue < quantizationArray.get(i)) {
                blue = quantizationArray.get(i - 1);
                blueDone = true;
            }


            if (redDone && greenDone && blueDone) {
                break;
            }
        }

        Color newColor = new Color(red, green, blue, c.getAlpha());
        newImage.setRGB(x, y, newColor.getRGB());
    };


    public FuncInterface getFuncInterface() {
        return fobj;
    }


    public Quantization (BufferedImage originalImage, int scale, String color) {
        this.originalImage = originalImage;
        this.color= color;

        int numberOfPixelsInScale = 256 / scale;
        for (int i = 0; i < 256; i += numberOfPixelsInScale) {
            quantizationArray.add(i);
        }
        // if there's any unaccounted for pixel ranges, add the max pixel value
        if (quantizationArray.get(quantizationArray.size() - 1) < 255) {
            quantizationArray.add(255);
        }
    }


    public BufferedImage quantization() {
        ParallelMatrix parallelMatrix = new ParallelMatrix();
        BufferedImage quantizationImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);

        parallelMatrix.doInParallel(quantizationImage, getFuncInterface());
        return quantizationImage;
    }


    public double getMeanSquaredError (BufferedImage quantizationImage) {
        double meanSquaredError = 0;

        for(int x = 0; x < originalImage.getWidth(); x++) {
            for (int y = 0; y < originalImage.getHeight(); y++) {

                Color cOriginal = new Color(originalImage.getRGB(x, y));
                int redOriginal = cOriginal.getRed();
                int greenOriginal = cOriginal.getGreen();
                int blueOriginal = cOriginal.getBlue();

                Color cNew = new Color(quantizationImage.getRGB(x, y));
                int redNew = cNew.getRed();
                int greenNew = cNew.getGreen();
                int blueNew = cNew.getBlue();

                int redError = (int) (Math.pow((redOriginal - redNew), 2));
                int greenError = (int) (Math.pow((greenOriginal - greenNew), 2));
                int blueError = (int) (Math.pow((blueOriginal - blueNew), 2));

                if ("gray".equalsIgnoreCase(color)) {
                    int gray = (int) (redError * 0.299) + (int) (greenError * 0.587) + (int) (blueError * 0.114);
                    meanSquaredError += gray;
                } else if ("red".equalsIgnoreCase(color)) {
                    meanSquaredError += redError;
                } else if ("green".equalsIgnoreCase(color)) {
                    meanSquaredError += greenError;
                } else if ("blue".equalsIgnoreCase(color)) {
                    meanSquaredError += blueError;
                } else {
                    throw new NullPointerException("please provide a valid color. You gave: " + color);
                }

            }
        }

        return meanSquaredError;
    }
}