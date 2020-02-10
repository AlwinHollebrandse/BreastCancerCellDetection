import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.util.concurrent.Semaphore;

public class GrayScale {

    @FunctionalInterface
    interface FuncInterface extends OverHeadInterface.FuncInterface {
        // An abstract function
        void function(BufferedImage originalImage, BufferedImage newImage, int x, int y, String color, double randomThreshold);
    }

    public FuncInterface fobj = (BufferedImage originalImage, BufferedImage newImage, int x, int y, String color, double randomThreshold) -> {
        Color c = new Color(originalImage.getRGB(x, y));
        if ("gray".equalsIgnoreCase(color)) {
            int gray = (int)(c.getRed() * 0.299) + (int)(c.getGreen() * 0.587) + (int)(c.getBlue() *0.114);
//                    int gray = c.getRed() + c.getGreen() + c.getBlue(); // TODO check what gray to use
            Color newColor = new Color(gray, gray, gray);
            newImage.setRGB(x, y, newColor.getRGB());
        } else if ("red".equalsIgnoreCase(color)) {
            Color newColor = new Color(c.getRed(), 0, 0);
            newImage.setRGB(x, y, newColor.getRGB());
        } else if ("green".equalsIgnoreCase(color)) {
            Color newColor = new Color(0, c.getGreen(), 0);
            newImage.setRGB(x, y, newColor.getRGB());
        } else if ("blue".equalsIgnoreCase(color)) {
            Color newColor = new Color(0, 0, c.getBlue());
            newImage.setRGB(x, y, newColor.getRGB());
        } else {
            throw new NullPointerException("something went wrong getting the colors");
        }
    };

    public FuncInterface getFuncInterface () {
        return fobj;
    }


    /** * convert a BufferedImage to RGB colourspace */
    // from https://blog.idrsolutions.com/2009/10/converting-java-bufferedimage-between-colorspaces/
    public BufferedImage convertToGrayScale(BufferedImage originalImage) { // TODO is this allowed? pretty sure its not
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

    public BufferedImage convertToSingleColor (BufferedImage originalImage, String color) {
        ProgressBar bar = new ProgressBar("Converting to GrayScale", originalImage.getWidth() * originalImage.getHeight());
        BufferedImage newImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);

        for(int x = 0; x < originalImage.getWidth(); x++) {
            for(int y = 0; y < originalImage.getHeight(); y++) {

                Color c = new Color(originalImage.getRGB(x, y));
                if ("gray".equalsIgnoreCase(color)) {
                    int gray = (int)(c.getRed() * 0.299) + (int)(c.getGreen() * 0.587) + (int)(c.getBlue() *0.114);
//                    int gray = c.getRed() + c.getGreen() + c.getBlue();
                    Color newColor = new Color(gray, gray, gray);
                    newImage.setRGB(x, y, newColor.getRGB());
                } else if ("red".equalsIgnoreCase(color)) {
                    Color newColor = new Color(c.getRed(), 0, 0);
                    newImage.setRGB(x, y, newColor.getRGB());
                } else if ("green".equalsIgnoreCase(color)) {
                    Color newColor = new Color(0, c.getGreen(), 0);
                    newImage.setRGB(x, y, newColor.getRGB());
                } else if ("blue".equalsIgnoreCase(color)) {
                    Color newColor = new Color(0, 0, c.getBlue());
                    newImage.setRGB(x, y, newColor.getRGB());
                } else {
                    throw new NullPointerException("something went wrong getting the colors");
                }
                bar.next();
            }
        }
        return newImage;
    }

    // OR this from: https://stackoverflow.com/questions/9131678/convert-a-rgb-image-to-grayscale-image-reducing-the-memory-in-java
//    ImageFilter filter = new GrayFilter(true, 50);
//    ImageProducer producer = new FilteredImageSource(colorImage.getSource(), filter);
//    Image mage = Toolkit.getDefaultToolkit().createImage(producer);

    public BufferedImage convertToSingleColorParallel (BufferedImage originalImage, String color) {
        ProgressBar progressBar = new ProgressBar("Converting to GrayScale", originalImage.getWidth() * originalImage.getHeight());
        BufferedImage newImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);

        final int MAX_THREADS = Runtime.getRuntime().availableProcessors();

        GrayScaleThread[] threadArray = new GrayScaleThread[MAX_THREADS];
        Semaphore sem = new Semaphore(1);

        int[][] xycounter = new int[originalImage.getWidth()][originalImage.getHeight()]; // TODO delete

        for (int i = 0; i < MAX_THREADS; i++) {
            threadArray[i] = new GrayScaleThread(originalImage, newImage, sem, progressBar, color, i, MAX_THREADS, xycounter);
            threadArray[i].start();
        }

        long startTime = System.nanoTime();
        try {
            //wait for all threads
            for (int i = 0; i < MAX_THREADS; i++) {
                threadArray[i].join();
            }
            System.out.println("GrayScale Execution time in milliseconds : " + (System.nanoTime() - startTime) / 1000000);
        } catch (InterruptedException e) {
            System.out.println("threads were interrupted!"); // TODO what happens then?
        }

        // TODO delete
        int skippedPixelCount = 0;
        for (int x = 0; x < originalImage.getWidth(); x++) {
            for (int y = 0; y < originalImage.getHeight(); y++) {
                if (xycounter[x][y] == 0) {
                    skippedPixelCount++;
                    System.out.println("xycounter: " + xycounter[x][y] + ", x: " + x + ", y: " + y);
                }
            }
        }
        System.out.println("skippedPixelCount: " + skippedPixelCount);

        return newImage;
    }
}


class GrayScaleThread extends Thread {

    private BufferedImage originalImage;
    private BufferedImage newImage;
    private Semaphore sem;
    private ProgressBar progressBar;
    private String color;
    private int startingXCoordinate;
    private int MAX_THREADS;
    private int[][] xycounter;

    public GrayScaleThread(BufferedImage originalImage, BufferedImage newImage, Semaphore sem, ProgressBar progressBar, String color, int startingXCoordinate, int MAX_THREADS, int[][] xycounter) {
        // store parameter for later user
        this.originalImage = originalImage;
        this.newImage = newImage;
        this.sem = sem;
        this.progressBar = progressBar;
        this.color = color;
        this.startingXCoordinate = startingXCoordinate;
        this.MAX_THREADS = MAX_THREADS;
        this.xycounter = xycounter;
    }

    public void run() {
        try {
            // Displaying the thread that is running
//            System.out.println ("Thread " + Thread.currentThread().getId() + " is running"  + ", MAX_THREADS: " + MAX_THREADS + ", startingXCoordinate: " + startingXCoordinate + ", startingYCoordinate: " + startingYCoordinate);;//", xJumpSize: " + xJumpSize + ", yJumpSize: " + yJumpSize);

            for (int x = this.startingXCoordinate; x < originalImage.getWidth(); x += MAX_THREADS) { // TODO change to newImage?
                for (int y = 0; y < originalImage.getHeight(); y ++) {
                    xycounter[x][y]++;

                    Color c = new Color(originalImage.getRGB(x, y));
                    if ("gray".equalsIgnoreCase(color)) {
                        int gray = (int)(c.getRed() * 0.299) + (int)(c.getGreen() * 0.587) + (int)(c.getBlue() *0.114);
//                    int gray = c.getRed() + c.getGreen() + c.getBlue(); // TODO check what gray to use
                        Color newColor = new Color(gray, gray, gray);
                        newImage.setRGB(x, y, newColor.getRGB());
                    } else if ("red".equalsIgnoreCase(color)) {
                        Color newColor = new Color(c.getRed(), 0, 0);
                        newImage.setRGB(x, y, newColor.getRGB());
                    } else if ("green".equalsIgnoreCase(color)) {
                        Color newColor = new Color(0, c.getGreen(), 0);
                        newImage.setRGB(x, y, newColor.getRGB());
                    } else if ("blue".equalsIgnoreCase(color)) {
                        Color newColor = new Color(0, 0, c.getBlue());
                        newImage.setRGB(x, y, newColor.getRGB());
                    } else {
                        throw new NullPointerException("something went wrong getting the colors");
                    }
//                    sem.acquire(); // TODO adding this makes the speed go from 61 to 6138ms...
//                    progressBar.next();
//                    sem.release();
                }
            }
        }
        catch (Exception e) {
            // Throwing an exception
            System.out.println ("Exception is caught");
        }
    }
}