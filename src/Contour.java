import java.awt.image.BufferedImage;
import java.util.ArrayList;

// TODO is this file pointless?
public class Contour {

    private final int UNUSEDEDGE = 255; // TODO get the real values
    private final int USEDEDGE = 254;
    private Utility utility = new Utility();
    private BufferedImage originalEdgeMap;
    private String color;
    private int filterWidth;
    private int filterHeight;

    public Contour(BufferedImage originalEdgeMap, String color, int filterWidth, int filterHeight) {
        this.originalEdgeMap = originalEdgeMap;
        this.color = color;
        this.filterWidth = filterWidth;
        this.filterHeight = filterHeight;
    }

    // TODO what to return
    public void contourDetection() {
        BufferedImage contourMap = new BufferedImage(originalEdgeMap.getWidth(), originalEdgeMap.getHeight(), originalEdgeMap.getType()); // TODO use a deep copy instead?

        // TODO can this be parallelized without race conditions?
        for (int x = 0; x < contourMap.getWidth(); x++) {
            for (int y = 0; y < contourMap.getHeight(); y++) {
                int pixelColor = utility.getSingleColorRGB(originalEdgeMap.getRGB(x, y), color);
                if (pixelColor == UNUSEDEDGE) {
                    incrementContour(x, y);
                }
            }
        }

        // TODO fill in contours
    }

    private void incrementContour(int x, int y) {
        ArrayList<Integer> neighborRGBValueArray = utility.getNeighborValues(originalEdgeMap, (x + filterWidth/2), (y + filterHeight/2), filterHeight, filterWidth);

    }

    // TODO so far this only works if both filter width and height are 3
    private void getFirstUnusedEdgeInReach(ArrayList<Integer> neighborRGBValueArray) {
        for (int i = 0; i < neighborRGBValueArray.size(); i++) {
            int pixelColor = utility.getSingleColorRGB(neighborRGBValueArray.get(i), color);
            if (pixelColor == UNUSEDEDGE) {
                int temp;
//                incrementContour(x, y);
            }
        }
    }
}
