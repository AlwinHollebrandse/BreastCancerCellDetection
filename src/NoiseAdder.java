import java.awt.image.BufferedImage;
import java.util.Random;

public class NoiseAdder {

    private Utility utility = new Utility();

    @FunctionalInterface
    interface FuncInterface extends OverHeadInterface.FuncInterface {
        // An abstract function
        void function(BufferedImage originalImage, BufferedImage newImage, int x, int y, String color, double randomThreshold, String filterType, int filterWidth, int filterHeight, int[] weights, double scalar);
    }

    public NoiseAdder.FuncInterface fobj = (BufferedImage image, BufferedImage newImage, int x, int y, String color, double randomThreshold, String filterType, int filterWidth, int filterHeight, int[] weights, double scalar) -> {
        Random rand = new Random();
        double rand_dub1 = rand.nextDouble();
        int imagePixelRGB = image.getRGB(x, y);

        if (rand_dub1 <= randomThreshold) {// if the random threshold is met
            rand_dub1 = rand.nextDouble(); // 50/50 to see if the pixel will be white or black
            if (rand_dub1 > .5) {
                newImage.setRGB(x, y, utility.setSingleColor(255, "gray"));//utility.setGrayPixelColor(imagePixelRGB, 255));
            }
            else {
                newImage.setRGB(x, y, utility.setSingleColor(0, "gray"));//utility.setGrayPixelColor(imagePixelRGB, 0));
            }
        } else {
            newImage.setRGB(x, y, imagePixelRGB);
        }
    };

    public NoiseAdder.FuncInterface getFuncInterface () {
        return fobj;
    }


    public BufferedImage createSaltAndPepperNoise (BufferedImage image, double randomThreshold) throws IllegalArgumentException {
        // Parameter checking
        if (randomThreshold > 1 || randomThreshold < 0) {
            throw new IllegalArgumentException("randomThreshold must be between 0-1");
        }


        ParallelMatrix parallelMatrix = new ParallelMatrix();
//        ProgressBar bar = new ProgressBar("Converting to GrayScale", originalImage.getWidth() * originalImage.getHeight());
        BufferedImage saltAndPepperImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);

        // noiseAdder lambda does not use: color, filterWidth, filterType, filterHeight, weights, or scalar
        parallelMatrix.doInParallel(image, saltAndPepperImage, "Adding Salt and Pepper Noise",
                getFuncInterface(), null, 0.05, null, 0, 0, null,  0);
        return saltAndPepperImage;
    }
}
