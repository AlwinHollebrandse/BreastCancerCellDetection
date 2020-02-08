public class Utility {
    public static int setGrayPixelColor (int pixelRGB, int pixelColor) {
        int result;

        // TODO most of these are here for debugging
        int pixelRGBNonColor = pixelRGB & 0xFFFFFF00;
        int finalResult = (pixelRGB & 0xFFFFFF00) | pixelColor;//sets the grey color value to 0
        int temp = finalResult & 0xFFFFFF00;// should = centerPixelNonColor

        int realResultColor = pixelColor;
        int centerPixelColor = pixelRGB & 0xFF;
        int resultPixelColor = finalResult & 0xFF;

//        result = (pixelRGB & 0xFFFFFF00) | (centerPixelColor + 100);
        result = (pixelRGB & 0xFFFFFF00) | pixelColor; // keeps all of the intensity and such values and only changes the gray scale value


        // bug has something to do with the other bits of the gray, the leading ones... I think its because i
//        System.out.println("avg gray pixel value: " + (result));
//        if (Math.abs(pixelRGB - result) > 100)
//            System.out.println("resultPixelColor: " + resultPixelColor + ", real pixelColor: " + pixelColor + ", pixelRGB: " + pixelRGB + ", result: " + result);

        return  result;
    }
}
