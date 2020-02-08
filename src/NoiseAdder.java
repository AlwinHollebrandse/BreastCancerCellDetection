import java.awt.image.BufferedImage;
import java.util.Random;

public class NoiseAdder {

    private Utility utility = new Utility();

    // NOTE randomThreshold must be between 0-1
    public  BufferedImage createSaltAndPepperNoise (BufferedImage image, double randomThreshold) throws IllegalArgumentException {
        if (randomThreshold > 1 || randomThreshold < 0) {
            throw new IllegalArgumentException("randomThreshold must be between 0-1");
        }

        int imageTotal = image.getWidth() * image.getHeight();
        ProgressBar bar = new ProgressBar("Adding Salt and Pepper Noise", imageTotal);
        BufferedImage saltAndPepperImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);  // TODO currently only supports grey images (type: TYPE_BYTE_GRAY)
        Random rand = new Random();

        for (int x = 0; x < saltAndPepperImage.getWidth(); x++) {
            for (int y = 0; y < saltAndPepperImage.getHeight(); y++) {
                double rand_dub1 = rand.nextDouble();
                int imagePixelRGB = image.getRGB(x, y);

                if (rand_dub1 <= randomThreshold) {// if the random threshold is met
                    rand_dub1 = rand.nextDouble(); // 50/50 to see if the pixel will be white or black
                    if (rand_dub1 > .5) {
                        saltAndPepperImage.setRGB(x, y, utility.setGrayPixelColor(imagePixelRGB, 255));
                    }
                    else {
                        saltAndPepperImage.setRGB(x, y, utility.setGrayPixelColor(imagePixelRGB, 0));
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
