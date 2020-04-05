import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class MorphologicalFunctions {

    private Utility utility = new Utility();
    private BufferedImage originalImage;
    private String morphologicalType;  // TODO add enums
    private int filterWidth;
    private int filterHeight;
    private int[] colors;
    private String color = "gray";

    // NOTE this image is needed to avoid out of bound errors. The threads loop through a cropped version, but access all pixels in the
    // "cropped" part of the original image. This result image is needed as a placeholder/copy of the original so that the original image is unchanged
    private BufferedImage resultImage;

    @FunctionalInterface
    interface FuncInterface extends OverHeadInterface.FuncInterface {
        // An abstract function
        void function(BufferedImage newImage, int x, int y);
    }

    public MorphologicalFunctions.FuncInterface fobj = (BufferedImage newImage, int x, int y) -> {
        if ("dilation".equalsIgnoreCase(morphologicalType)) {
            dilation((x + filterWidth/2), (y + filterHeight/2));
        }

        else if("erosion".equalsIgnoreCase(morphologicalType)) {
            erosion((x + filterWidth/2), (y + filterHeight/2));
        }
    };

    public MorphologicalFunctions.FuncInterface getFuncInterface () {
        return fobj;
    }



    public MorphologicalFunctions(BufferedImage originalImage, String morphologicalType, int filterWidth, int filterHeight, int[] colors) {
        this.originalImage = originalImage;
        this.morphologicalType = morphologicalType;
        this.filterWidth = filterWidth;
        this.filterHeight = filterHeight;
        this.colors = colors;
    }


    // NOTE currently this only works for RGB (which includes black and white values, as those have rgb values, provided they are there)
    // NOTE crops the image border that does not fit in the filter convolution
    // NOTE assumes after accounting for weights, that the pixel color is still normalized TODO normalize after
    public BufferedImage morphologicalFunctions () throws NullPointerException {
        // Parameter checking
        // If there was no weights array specified, then use weights of 1.
        if (colors == null) {
            int filterSize = filterHeight * filterWidth;
            colors = new int[filterSize];
            for (int i = 0; i < filterSize; i++) {
                colors[i] = 1;
            }
        }

        if (colors.length != filterHeight * filterWidth) {
            throw new NullPointerException("weights array was not the size of the filter");
        }

        // this method only works if there is a border to the filter, or else it is a simple pixel operation function
        if (filterHeight <= 2 || filterWidth <= 2) {
            throw new NullPointerException("filter height and width must be greater than 0");
        }

        // this methods requires a clear center pixel in the filter, so both the filter height and width need to be odd
        if ((filterHeight & 1) == 0 || (filterWidth & 1) == 0) {
            throw new NullPointerException("filter height and width must be odd numbers");
        }

        resultImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), originalImage.getType());
        BufferedImage changedImage = new BufferedImage(originalImage.getWidth() - ((filterWidth/2) * 2), originalImage.getHeight() - ((filterHeight/2) * 2), originalImage.getType());

        ParallelMatrix parallelMatrix = new ParallelMatrix();
        parallelMatrix.doInParallel(changedImage, getFuncInterface());
        return resultImage;
    }

    private void dilation(int originalX, int originalY) {
        // get the starting and ending valid coordinate values
        int startingX = originalX - filterWidth/2;
        int startingY = originalY - filterHeight/2;
        int endingX = originalX + filterWidth/2;
        int endingY = originalY + filterHeight/2;
        int currentColorIndex = 0;
        boolean growPixel = false;

        if ((utility.getSingleColor(originalImage.getRGB(originalX, originalY), color)) != 0) { // NOTE due to how graying works, when the pixel gets set to 255 on a gray scale, the returned value is 254) {
            growPixel = true;
        }

        if (growPixel) {
            // get the pixels within the designated borders
            for (int x = startingX; x <= endingX; x++) {
                for (int y = startingY; y <= endingY; y++) {
                    if (colors[currentColorIndex] != 0) {
                        resultImage.setRGB(x, y, utility.setSingleColorRBG(255, color));
                    }
                    currentColorIndex++;
                }
            }
        }
    }

    // will use an overlapping approach because that's more paralizable
    private void erosion( int originalX, int originalY) {
        // get the starting and ending valid coordinate values
        int startingX = originalX - filterWidth/2;
        int startingY = originalY - filterHeight/2;
        int endingX = originalX + filterWidth/2;
        int endingY = originalY + filterHeight/2;
        int currentColorIndex = 0;
        boolean addShape = true;

        // check if the current image window matches the filter (ignoring where colors is 0)
        for (int x = startingX; x <= endingX; x++) {
            for (int y = startingY; y <= endingY; y++) {
                int currentPixelColor = utility.getSingleColor(originalImage.getRGB(x, y), color);
                int currentFilterColor = colors[currentColorIndex];

                // TODO which is better. relates to: NOTE due to how graying works, when the pixel gets set to 255 on a gray scale, the returned value is 254) {
//                if (!(currentPixelColor == currentFilterColor)) {
                if (currentFilterColor != 0 && currentPixelColor == 0) {
                    addShape = false;
                    break;
                }

                currentColorIndex++;
            }
            if (!addShape) {
                break;
            }
        }

        if (addShape) {
            currentColorIndex = 0;
            // get the pixels within the designated borders
            for (int x = startingX; x <= endingX; x++) {
                for (int y = startingY; y <= endingY; y++) {
                    if (colors[currentColorIndex] != 0) {
                        resultImage.setRGB(x, y, utility.setSingleColorRBG(255, color));
                    }
                    currentColorIndex++;
                }
            }
        }
    }
}
