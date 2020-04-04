import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class NoiseAdder {

    private BufferedImage originalImage;
    private String noiseType; // TODO add enums
    private double randomThreshold;
    private double mean;
    private double sigma;// is standard deviation

    @FunctionalInterface
    interface FuncInterface extends OverHeadInterface.FuncInterface {
        // An abstract function
        void function(BufferedImage newImage, int x, int y);
    }

    public NoiseAdder.FuncInterface fobj = (BufferedImage newImage, int x, int y) -> {//}, String color, double randomThreshold, String filterType, int filterWidth, int filterHeight, int[] weights, double scalar) -> {
        Random rand = new Random();
        int imagePixelRGB = originalImage.getRGB(x, y);
        Utility utility = new Utility();

        if ("saltAndPepper".equalsIgnoreCase(noiseType)) {
            double rand_dub1 = rand.nextDouble();
            if (rand_dub1 <= randomThreshold) {// if the random threshold is met
                rand_dub1 = rand.nextDouble(); // 50/50 to see if the pixel will be white or black
                if (rand_dub1 > .5) {
                    newImage.setRGB(x, y, utility.setSingleColorRBG(255, "gray"));//utility.setGrayPixelColor(imagePixelRGB, 255));
                } else {
                    newImage.setRGB(x, y, utility.setSingleColorRBG(0, "gray"));//utility.setGrayPixelColor(imagePixelRGB, 0));
                }
            } else {
                newImage.setRGB(x, y, imagePixelRGB);
            }
        } else if ("gaussian".equalsIgnoreCase(noiseType)) {
            int gaussianNoise = (int)(rand.nextGaussian() * sigma + mean);

            Color c = new Color(originalImage.getRGB(x, y));

            int red = c.getRed();
            if (red != 0) {
                red = utility.normalizeColorInt(c.getRed() + gaussianNoise);
            }

            int green = c.getGreen();
            if (green != 0) {
                green = utility.normalizeColorInt(c.getGreen() + gaussianNoise);
            }

            int blue = c.getBlue();
            if (blue !=0) {
                blue = utility.normalizeColorInt(c.getBlue() + gaussianNoise);
            }


            Color newColor = new Color(red, green, blue, c.getAlpha());
            newImage.setRGB(x, y, newColor.getRGB());
        }
    };


    public NoiseAdder.FuncInterface getFuncInterface () {
        return fobj;
    }


    public NoiseAdder (BufferedImage originalImage, String noiseType, double randomThreshold, double mean, double sigma) {
        this.originalImage = originalImage;
        this.noiseType = noiseType;
        this.randomThreshold = randomThreshold;
        this.mean = mean;
        this.sigma = sigma;
    }


    public BufferedImage addNoise () {//BufferedImage image, double randomThreshold) throws IllegalArgumentException {
//    public BufferedImage createSaltAndPepperNoise (BufferedImage image, double randomThreshold) throws IllegalArgumentException {
        // Parameter checking
        if (randomThreshold > 1 || randomThreshold < 0) {
            throw new IllegalArgumentException("randomThreshold must be between 0-1");
        }

        ParallelMatrix parallelMatrix = new ParallelMatrix();
        BufferedImage noiseImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);

        parallelMatrix.doInParallel(noiseImage, getFuncInterface());
        return noiseImage;
    }
}
