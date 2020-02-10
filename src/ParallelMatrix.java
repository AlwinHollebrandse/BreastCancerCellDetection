import java.awt.image.BufferedImage;
import java.util.concurrent.Semaphore;

public class ParallelMatrix {

    public void lamabaUseInParaTest () {
        BufferedImage originalImage = null;
        BufferedImage newImage = null;
        int x = 0;
        int y = 0;
        String color = null;
        GrayScale grayscale = new GrayScale();
        GrayScale.FuncInterface grayScaleCode = grayscale.getFuncInterface();
        grayScaleCode.function(originalImage, newImage, x, y, color);
    }

    public void doInParallel (BufferedImage originalImage, BufferedImage newImage, String color, OverHeadInterface.FuncInterface code, String barMessage) { // TODO add progress bar somehow
        final int MAX_THREADS = Runtime.getRuntime().availableProcessors();

        MultithreadingDemo[] threadArray = new MultithreadingDemo[MAX_THREADS];
        Semaphore sem = new Semaphore(1);
        ProgressBar progressBar = new ProgressBar(barMessage, (originalImage.getWidth() * originalImage.getHeight()));

        int[][] xycounter = new int[originalImage.getWidth()][originalImage.getHeight()]; // TODO delete

        for (int i = 0; i < MAX_THREADS; i++) {
            threadArray[i] = new MultithreadingDemo(originalImage, newImage, sem, progressBar, color, code, i, MAX_THREADS, xycounter);
            threadArray[i].start();
        }

        long startTime = System.nanoTime();
        try {
            //wait for all threads
            for (int i = 0; i < MAX_THREADS; i++) {
                threadArray[i].join();
            }
            System.out.println(barMessage + " Execution time in milliseconds : " + (System.nanoTime() - startTime) / 1000000);
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
    }
}

//ExecutorService executorService = Executors.newFixedThreadPool(2);

class MultithreadingDemo extends Thread {

    private BufferedImage originalImage;
    private BufferedImage newImage;
    private Semaphore sem;
    private ProgressBar progressBar;
    private String color;
    private OverHeadInterface.FuncInterface code; // TODO change to lambda object?
    private int startingXCoordinate;
    private int MAX_THREADS;
    private int[][] xycounter;

    public MultithreadingDemo(BufferedImage originalImage, BufferedImage newImage, Semaphore sem, ProgressBar progressBar, String color, OverHeadInterface.FuncInterface code, int startingXCoordinate, int MAX_THREADS, int[][] xycounter) {
        // store parameter for later user
        this.originalImage = originalImage;
        this.newImage = newImage;
        this.sem = sem;
        this.progressBar = progressBar;
        this.color = color;
        this.code = code;
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
                    code.function(originalImage, newImage, x, y, color);
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