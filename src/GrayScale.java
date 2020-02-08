import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;

public class GrayScale {

    /** * convert a BufferedImage to RGB colourspace */
    // from https://blog.idrsolutions.com/2009/10/converting-java-bufferedimage-between-colorspaces/
    public static BufferedImage convertToGrayScale(BufferedImage originalImage) { // TODO is this allowed? pretty sure its not
        ProgressBar bar = new ProgressBar("Converting to GrayScale", 1);

        BufferedImage grayScaleImage = null;
        try {
            grayScaleImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
            ColorConvertOp xformOp = new ColorConvertOp(null);
            xformOp.filter(originalImage, grayScaleImage);
            bar.next();
        } catch (Exception e) {
            System.out.println("Exception " + e + " converting image");
        }
        return grayScaleImage;
    }

    // OR this from: https://stackoverflow.com/questions/9131678/convert-a-rgb-image-to-grayscale-image-reducing-the-memory-in-java
//    ImageFilter filter = new GrayFilter(true, 50);
//    ImageProducer producer = new FilteredImageSource(colorImage.getSource(), filter);
//    Image mage = Toolkit.getDefaultToolkit().createImage(producer);

    //techincally, this would make it gray, but the problem is that it stays in rgb scale
    //tutorialspoint.com/java_dip/grayscale_conversion.htm
//    for(int i=0; i<height; i++) {
//
//        for(int j=0; j<width; j++) {
//
//            Color c = new Color(image.getRGB(j, i));
//            int red = (int)(c.getRed() * 0.299);
//            int green = (int)(c.getGreen() * 0.587);
//            int blue = (int)(c.getBlue() *0.114);
//            Color newColor = new Color(red+green+blue,
//
//                    red+green+blue,red+green+blue);
//
//            image.setRGB(j,i,newColor.getRGB());
//        }
//    }
}