import java.awt.image.BufferedImage;
import java.util.concurrent.Semaphore;

public class ParallelMatrix {

    public void doInParallel (BufferedImage originalImage, BufferedImage newImage, String barMessage, // TODO add progress bar somehow
                              OverHeadInterface.FuncInterface code, String color, double randomThreshold, String filterType, int filterWidth, int filterHeight, int[] weights, double scalar) {

        final int MAX_THREADS = Runtime.getRuntime().availableProcessors();

        ImageThread[] threadArray = new ImageThread[MAX_THREADS];
        Semaphore sem = new Semaphore(1);
        ProgressBar progressBar = new ProgressBar(barMessage, (newImage.getWidth() * newImage.getHeight()));

        int[][] xycounter = new int[newImage.getWidth()][newImage.getHeight()]; // TODO delete

        for (int i = 0; i < MAX_THREADS; i++) {
            threadArray[i] = new ImageThread(originalImage, newImage, sem, progressBar, MAX_THREADS, i, xycounter,
                    code, color, randomThreshold, filterType, filterWidth, filterHeight, weights, scalar);
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
        for (int x = 0; x < newImage.getWidth(); x++) {
            for (int y = 0; y < newImage.getHeight(); y++) {
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

class ImageThread extends Thread {

    //for thread objects
    private BufferedImage originalImage;
    private BufferedImage newImage;
    private Semaphore sem;
    private ProgressBar progressBar;
    private int MAX_THREADS;
    private int threadNumber;
    private int[][] xycounter;

    // for code lambdas
    private OverHeadInterface.FuncInterface code; // TODO change to lambda object?
    private String color;
    private double randomThreshold;
    private String filterType;
    private int filterWidth;
    private int filterHeight;
    private int[] weights;
    private double scalar;

    // store parameter for later user
    public ImageThread(BufferedImage originalImage, BufferedImage newImage, Semaphore sem, ProgressBar progressBar, int MAX_THREADS, int threadNumber, int[][] xycounter, //for thread objects
                       OverHeadInterface.FuncInterface code, String color, double randomThreshold, String filterType, int filterWidth, int filterHeight, int[] weights, double scalar) { // for code lambdas

        //for thread objects
        this.originalImage = originalImage;
        this.newImage = newImage;
        this.sem = sem;
        this.progressBar = progressBar;
        this.MAX_THREADS = MAX_THREADS;
        this.threadNumber = threadNumber;
        this.xycounter = xycounter;

        // for code lambdas
        this.code = code;
        this.color = color;
        this.randomThreshold = randomThreshold;
        this.filterType = filterType;
        this.filterWidth = filterWidth;
        this.filterHeight = filterHeight;
        this.weights = weights;
        this.scalar = scalar;
    }

    public void run() {
        try {
            // Displaying the thread that is running
//            System.out.println ("Thread " + Thread.currentThread().getId() + " is running"  + ", MAX_THREADS: " + MAX_THREADS + ", startingXCoordinate: " + startingXCoordinate + ", startingYCoordinate: " + startingYCoordinate);;//", xJumpSize: " + xJumpSize + ", yJumpSize: " + yJumpSize);

            for (int x = threadNumber; x < newImage.getWidth(); x += MAX_THREADS) { // TODO change to newImage?
                for (int y = 0; y < newImage.getHeight(); y ++) {
                    xycounter[x][y]++;
                    code.function(originalImage, newImage, x, y, color, randomThreshold, filterType, filterWidth, filterHeight, weights, scalar);
//                    sem.acquire(); // TODO adding this makes the speed go from 61 to 6138ms...
//                    progressBar.next();
//                    sem.release();
                }
            }
        }
        catch (Exception e) {
            // Throwing an exception
            System.out.println ("Exception is caught " + e);
        }
    }
}