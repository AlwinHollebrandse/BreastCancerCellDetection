import java.awt.image.BufferedImage;

public class ParallelMatrix {

    public void doInParallel (BufferedImage newImage,
                              OverHeadInterface.FuncInterface code) {

        final int MAX_THREADS = 1;//Runtime.getRuntime().availableProcessors();

        ImageThread[] threadArray = new ImageThread[MAX_THREADS];

        for (int i = 0; i < MAX_THREADS; i++) {
            threadArray[i] = new ImageThread(newImage, MAX_THREADS, i,
                    code);
            threadArray[i].start();
        }

        try {
            //wait for all threads
            for (int i = 0; i < MAX_THREADS; i++) {
                threadArray[i].join();
            }
        } catch (InterruptedException e) {
            System.out.println("threads were interrupted!");
            e.printStackTrace();
            throw new NullPointerException("threads were interrupted!");
        }
    }
}

//ExecutorService executorService = Executors.newFixedThreadPool(2);

class ImageThread extends Thread {

    //for thread objects
    private BufferedImage newImage;
    private int MAX_THREADS;
    private int threadNumber;

    // for code lambdas
    private OverHeadInterface.FuncInterface code;

    // store parameter for later user
    public ImageThread(BufferedImage newImage, int MAX_THREADS, int threadNumber, //for thread objects
                       OverHeadInterface.FuncInterface code) { // for code lambdas

        //for thread objects
        this.newImage = newImage;
        this.MAX_THREADS = MAX_THREADS;
        this.threadNumber = threadNumber;

        // for code lambdas
        this.code = code;
    }

    public void run() {
        try {
            // Displaying the thread that is running
//            System.out.println ("Thread " + Thread.currentThread().getId() + " is running"  + ", MAX_THREADS: " + MAX_THREADS + ", startingXCoordinate: " + startingXCoordinate + ", startingYCoordinate: " + startingYCoordinate);;//", xJumpSize: " + xJumpSize + ", yJumpSize: " + yJumpSize);

            for (int x = threadNumber; x < newImage.getWidth(); x += MAX_THREADS) {
                for (int y = 0; y < newImage.getHeight(); y ++) {
                    code.function(newImage, x, y);
                }
            }
        }
        catch (Exception e) {
            // Throwing an exception
            throw e;
        }
    }
}