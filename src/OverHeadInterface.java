import java.awt.image.BufferedImage;

public class OverHeadInterface {
    public interface FuncInterface {
        void function(BufferedImage originalImage, BufferedImage newImage, int x, int y, String color, double randomThreshold, String filterType, int filterWidth, int filterHeight, int[] weights, double scalar);
    }
}
