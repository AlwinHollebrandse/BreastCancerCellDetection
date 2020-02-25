import java.awt.*;

public class Utility {
    // TODO add color enums
    public int setSingleColor (int pixelColor, String color) {
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
}
