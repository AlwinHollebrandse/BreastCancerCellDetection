import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Utility {
    // TODO add color enums
    public int setSingleColorRBG (int pixelColor, String color) {
        pixelColor = normalizeColorInt(pixelColor);

        if ("gray".equalsIgnoreCase(color)) {
            int gray = (int)(pixelColor * 0.299) + (int)(pixelColor * 0.587) + (int)(pixelColor *0.114); // NOTE adds in the "gray multiplier" (according to tutorialspoint.com/java_dip/grayscale_conversion.htm)
            Color newColor = new Color(gray, gray, gray);
            return newColor.getRGB();
        } else if ("red".equalsIgnoreCase(color)) {
            Color newColor = new Color(pixelColor, 0, 0);
            return newColor.getRGB();
        } else if ("green".equalsIgnoreCase(color)) {
            Color newColor = new Color(0, pixelColor, 0);
            return newColor.getRGB();
        } else if ("blue".equalsIgnoreCase(color)) {
            Color newColor = new Color(0, 0, pixelColor);
            return newColor.getRGB();
        }
        throw new NullPointerException("something went wrong setting the colors");
    }

    public int getSingleColor (int pixelRGB, String color) {
        Color newColor = new Color(pixelRGB);
        if ("gray".equalsIgnoreCase(color)) {
            return newColor.getRed(); // all 3 colors are the same
        } else if ("red".equalsIgnoreCase(color)) {
            return newColor.getRed();
        } else if ("green".equalsIgnoreCase(color)) {
            return newColor.getGreen();
        } else if ("blue".equalsIgnoreCase(color)) {
            return newColor.getBlue();
        }
        throw new NullPointerException("something went wrong getting the colors");
    }

    public int normalizeColorInt (int pixelColor) {
        if (pixelColor > 255) {
            pixelColor = 255;
        } if (pixelColor < 0) {
            pixelColor = 0;
        }
        return pixelColor;
    }

    // NOTE this only supports odd rectangles with a center pixel. (ex: 3x3, 5x3, etc not 4x4)
    public ArrayList<Integer> getNeighborValues (BufferedImage originalImage, int originalX, int originalY, int filterHeight, int filterWidth) {
        ArrayList<Integer> neighborRGBValueArray = new ArrayList<>();

        // get the starting and ending valid coordinate values
        int startingX = originalX - filterWidth/2;
        int startingY = originalY - filterHeight/2;
        int endingX = originalX + filterWidth/2;
        int endingY = originalY + filterHeight/2;

        // get the pixels within the designated borders
        for (int x = startingX; x <= endingX; x++) {
            for (int y = startingY; y <= endingY; y++) {
                neighborRGBValueArray.add(originalImage.getRGB(x, y));
            }
        }

        return neighborRGBValueArray;
    }
}
