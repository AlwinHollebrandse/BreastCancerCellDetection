import java.awt.image.BufferedImage;
import java.util.Random;

public class NoiseAdder {

    private Utility utility = new Utility();

    @FunctionalInterface
    interface FuncInterface extends OverHeadInterface.FuncInterface {
        // An abstract function
        void function(BufferedImage originalImage, BufferedImage newImage, int x, int y, String color, double randomThreshold);
    }

    public NoiseAdder.FuncInterface fobj = (BufferedImage image, BufferedImage newImage, int x, int y, String color, double randomThreshold) -> {
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


    public BufferedImage createSaltAndPepperNoise (BufferedImage image, double randomThreshold) {
        // Parameter checking
        if (randomThreshold > 1 || randomThreshold < 0) {
            throw new IllegalArgumentException("randomThreshold must be between 0-1");
        }


        ParallelMatrix parallelMatrix = new ParallelMatrix();
//        ProgressBar bar = new ProgressBar("Converting to GrayScale", originalImage.getWidth() * originalImage.getHeight());
        BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        parallelMatrix.doInParallel(image, newImage, null, 0.05, getFuncInterface(), "Adding Salt and Pepper Noise"); // randomThreshold (0.05) isnt used by grayScale
        return newImage;
    }

    // NOTE randomThreshold must be between 0-1
    public  BufferedImage createSaltAndPepperNoise2 (BufferedImage image, double randomThreshold) throws IllegalArgumentException {
        if (randomThreshold > 1 || randomThreshold < 0) {
            throw new IllegalArgumentException("randomThreshold must be between 0-1");
        }

        int imageTotal = image.getWidth() * image.getHeight();
        ProgressBar bar = new ProgressBar("S&P Noise", imageTotal);
        BufferedImage saltAndPepperImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());  // TODO currently only supports grey images (type: TYPE_BYTE_GRAY)
        Random rand = new Random();

        for (int x = 0; x < saltAndPepperImage.getWidth(); x++) {
            for (int y = 0; y < saltAndPepperImage.getHeight(); y++) {
                double rand_dub1 = rand.nextDouble();
                int imagePixelRGB = image.getRGB(x, y);

                if (rand_dub1 <= randomThreshold) {// if the random threshold is met
                    rand_dub1 = rand.nextDouble(); // 50/50 to see if the pixel will be white or black
                    if (rand_dub1 > .5) {
                        saltAndPepperImage.setRGB(x, y, utility.setSingleColor(255, "gray"));//utility.setGrayPixelColor(imagePixelRGB, 255));
                    }
                    else {
                        saltAndPepperImage.setRGB(x, y, utility.setSingleColor(0, "gray"));//utility.setGrayPixelColor(imagePixelRGB, 0));
                    }
                } else {
                    saltAndPepperImage.setRGB(x, y, imagePixelRGB);
                }

                bar.next();
            }
        }
        return saltAndPepperImage;
    }
}
