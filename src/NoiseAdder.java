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
        void function(BufferedImage newImage, int x, int y); //   TODO why pass in og image here?
    }

    public NoiseAdder.FuncInterface fobj = (BufferedImage newImage, int x, int y) -> {//}, String color, double randomThreshold, String filterType, int filterWidth, int filterHeight, int[] weights, double scalar) -> {
        Random rand = new Random();
        double rand_dub1 = rand.nextDouble();
        int imagePixelRGB = originalImage.getRGB(x, y);
        Utility utility = new Utility();

        if ("saltAndPepper".equalsIgnoreCase(noiseType)) {
            if (rand_dub1 <= randomThreshold) {// if the random threshold is met
                rand_dub1 = rand.nextDouble(); // 50/50 to see if the pixel will be white or black
                if (rand_dub1 > .5) {
                    newImage.setRGB(x, y, utility.setSingleColor(255, "gray"));//utility.setGrayPixelColor(imagePixelRGB, 255));
                } else {
                    newImage.setRGB(x, y, utility.setSingleColor(0, "gray"));//utility.setGrayPixelColor(imagePixelRGB, 0));
                }
            } else {
                newImage.setRGB(x, y, imagePixelRGB);
            }
        } else if ("gaussian".equalsIgnoreCase(noiseType)) {
            Random r = new Random();
            int gaussianNoise = (int)(r.nextGaussian() * sigma + mean);

            Color c = new Color(originalImage.getRGB(x, y));

            int red = utility.normalizeColorInt(c.getRed() + gaussianNoise);
            int green = utility.normalizeColorInt(c.getGreen() + gaussianNoise);
            int blue = utility.normalizeColorInt(c.getBlue() + gaussianNoise);

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

        String barMessage = "error bar message";
        if ("saltAndPepper".equalsIgnoreCase(noiseType)) {
            barMessage = "Adding Salt and Pepper Noise";
        } else if("gaussian".equalsIgnoreCase(noiseType)) {
            barMessage = "Adding Gaussian Noise";
        }

        ParallelMatrix parallelMatrix = new ParallelMatrix();
        BufferedImage saltAndPepperImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);

        parallelMatrix.doInParallel(saltAndPepperImage, barMessage, getFuncInterface());
        return saltAndPepperImage;
    }
}
