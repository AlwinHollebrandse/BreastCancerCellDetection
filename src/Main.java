import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class Main {

    public static void main(String args[])throws IOException {

        try {
            final File input_file = new File("black-and-white-tips.jpg"); //image file path

            final BufferedImage originalImage = ImageIO.read(input_file);
            BufferedImage avgImage = new BufferedImage(originalImage.getWidth(),originalImage.getHeight(),originalImage.getType());

            System.out.println("Reading complete." + originalImage.getColorModel());

            int imageTotal = originalImage.getWidth() * originalImage.getHeight();
            ProgressBar bar = new ProgressBar(imageTotal);

            for (int x = 0; x < originalImage.getWidth(); x++) {
                for (int y = 0; y < originalImage.getHeight(); y++) {
                    final int clr = originalImage.getRGB(x, y);
                    final int red = (clr & 0x00ff0000) >> 16;
                    final int green = (clr & 0x0000ff00) >> 8;
                    final int blue = clr & 0x000000ff;

                    bar.next();

                    // Color Red get coordinates
                    if (red == 255) {
//                        System.out.println(String.format("Coordinate %d %d", x, y));
                    } else {
                        //nothing
//                        System.out.println("Red Color value = " + red);
//                        System.out.println("Green Color value = " + green);
//                        System.out.println("Blue Color value = " + blue);
                    }
                }
            }
        }
        catch(IOException e) {
            System.out.println("Error: "+e);
        }

        // // WRITE IMAGE
        // try
        // {
        //     // Output file path
        //     File output_file = new File("temp.jpg");

        //     // Writing to file taking type and path as
        //     ImageIO.write(image, "jpg", output_file);

        //     System.out.println("Writing complete.");
        // }
        // catch(IOException e)
        // {
        //     System.out.println("Error: "+e);
        // }
    }
}