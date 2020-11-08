// package com.alwin;

// import java.awt.*;
// import java.awt.image.BufferedImage;
// import java.util.ArrayList;
// import java.util.Collections;
// import java.util.concurrent.Semaphore;

// public class MpiThreadedFilter {

//     private Utility utility = new Utility();
//     private BufferedImage originalImage;
//     private String filterType;  // TODO add enums
//     private int filterWidth;
//     private int filterHeight;
//     private int[] weights;
//     private double scalar;

//     private int newImageWidth;
//     private int newImageHeight;
//     private int startingX;
//     private int endingX;


//     public MpiThreadedFilter(BufferedImage originalImage, String filterType, int filterWidth, int filterHeight, int[] weights, int newImageWidth, int newImageHeight, int startingX, int endingX) {
//         this.originalImage = originalImage;
//         this.filterType = filterType;
//         this.filterWidth = filterWidth;
//         this.filterHeight = filterHeight;
//         this.weights = weights;

//         this.newImageWidth = newImageWidth;
//         this.newImageHeight = newImageHeight;
//         this.startingX = startingX;
//         this.endingX = endingX;
//     }


//     // NOTE currently this only works for RGB (which includes black and white values, as those have rgb values, provided they are there)
//     // NOTE crops the image border that does not fit in the filter convolution
//     // NOTE assumes after accounting for weights, that the pixel color is still normalized TODO normalize after
//     public int[] filter () throws NullPointerException {
//         // TODO move param checking to image operations call?
//         // Parameter checking
//         // If there was no weights array specified, then use weights of 1.
//         if (weights == null) {
//             weights = setDefaultWeights();
//         }

//         scalar = setScalar(weights);

//         if (weights.length != filterHeight * filterWidth) {
//             throw new NullPointerException("weights array was not the size of the filter");
//         }

//         // this method only works if there is a border to the filter, or else it is a simple pixel operation function
//         if (filterHeight <= 2 || filterWidth <= 2) {
//             throw new NullPointerException("filter height and width must be greater than 0");
//         }

//         // this methods requires a clear center pixel in the filter, so both the filter height and width need to be odd
//         if ((filterHeight & 1) == 0 || (filterWidth & 1) == 0) {
//             throw new NullPointerException("filter height and width must be odd numbers");
//         }

//         int[] filterImagePortion = new int[(endingX - startingX) * newImageHeight]; // TODO doubles or ints?


//         ParallelMatrix parallelMatrix = new ParallelMatrix();
//         parallelMatrix.doInParallel(filterImage, getFuncInterface());

//         return filterImagePortion;
//     }

//     private int[] setDefaultWeights() {
//         int filterSize = filterHeight * filterWidth;
//         weights = new int[filterSize];
//         for (int i = 0; i < filterSize; i++) {
//             weights[i] = 1;
//         }
//         return weights;
//     }

//     private double setScalar(int[] weights) {
//         double absoluteSum = 0;
//         for (int i = 0; i < weights.length; i++) {
//             absoluteSum += Math.abs(weights[i]);
//         }
//         return 1/absoluteSum;
//     }

//     public int calcMedian (ArrayList<Integer> list, int[] weights) throws NullPointerException {
//         if (list.size() != weights.length) {
//             throw new NullPointerException("weights array was not the size of the filter");
//         }

//         //create and fill out a weighted medina list, where each pixel gets replicated by the associated weight value number of times. Median of medians is then used and the median is found.
//         ArrayList<Integer> weightedMedianList = new ArrayList<>();
//         for (int i = 0; i < list.size(); i++) {
//             for (int j = 0; j < weights[i]; j++) {
//                 Color c = new Color(list.get(i));
//                 int gray = (int) (c.getRed() * 0.299) + (int) (c.getGreen() * 0.587) + (int) (c.getBlue() * 0.114);
// //                Color newColor = new Color(gray, gray, gray, c.getAlpha());
//                 weightedMedianList.add(gray);
//             }
//         }

//         // NOTE there was a median of medians implementation, but this ended up being faster
//         Collections.sort(weightedMedianList);
//         int medianValue = weightedMedianList.get(weightedMedianList.size() / 2);

//         Utility utility = new Utility();
//         return utility.setSingleColorRBG(medianValue, "gray");
//     }


//     // does the above but for red, green, and blue at once
//     public static int calcAvgRGB (ArrayList<Integer> list, int[] weights, double scalar) throws NullPointerException {
//         if (list.size() != weights.length) {
//             throw new NullPointerException("weights array was not the size of the filter");
//         }

//         double redAvg = 0;
//         double greenAvg = 0;
//         double blueAvg = 0;
//         double alphaAvg = 0;
//         for (int i = 0; i < list.size(); i++) {
//             // add the respective RGB element to the correct color avg
//             Color c = new Color(list.get(i));
//             redAvg += c.getRed() * weights[i]; // red element * weight of pixel
//             greenAvg += c.getGreen() * weights[i]; // green element * weight of pixel
//             blueAvg += c.getBlue() * weights[i]; // blue element * weight of pixel
//             alphaAvg += c.getAlpha() * weights[i];
//         }

//         redAvg *= scalar;
//         greenAvg *= scalar;
//         blueAvg *= scalar;
//         alphaAvg *= scalar;

//         Utility utility = new Utility();
//         int finalRedAvg = utility.normalizeColorInt((int)redAvg);
//         int finalGreenAvg = utility.normalizeColorInt((int)greenAvg);
//         int finalBlueAvg = utility.normalizeColorInt((int)blueAvg);
//         int finalAlphaAvg = utility.normalizeColorInt((int)alphaAvg);

//         //combine each of the RGB elements into a single int
//         Color newColor = new Color(finalRedAvg, finalGreenAvg, finalBlueAvg, finalAlphaAvg);
//         return newColor.getRGB();
//     }

// }



// class MPIImageThread extends Thread {

//     //for thread objects
//     private int[] filterImagePortion; // TODO ints or doubles?
//     private Semaphore semaphore;
//     private int MAX_THREADS;
//     private int threadNumber;

//     // for code lambdas
//     private OverHeadInterface.FuncInterface code;

//     // store parameter for later user
//     public MPIImageThread(int[] filterImagePortion, Semaphore semaphore, int MAX_THREADS, int threadNumber) {
//         //for thread objects
//         this.filterImagePortion = filterImagePortion;
//         this.semaphore = semaphore;
//         this.MAX_THREADS = MAX_THREADS;
//         this.threadNumber = threadNumber;
//     }

//     public void run() {
//         try {
//             // Displaying the thread that is running
// //            System.out.println ("Thread " + Thread.currentThread().getId() + " is running"  + ", MAX_THREADS: " + MAX_THREADS + ", startingXCoordinate: " + startingXCoordinate + ", startingYCoordinate: " + startingYCoordinate);;//", xJumpSize: " + xJumpSize + ", yJumpSize: " + yJumpSize);

//             // for (int x = threadNumber; x < newImage.getWidth(); x += MAX_THREADS) {
//             //     for (int y = 0; y < newImage.getHeight(); y ++) {
//             //         code.function(newImage, semaphore, x, y);
//             //     }
//             // }

//             // TODO need new index vars for filling filterImagePortion as xy relate to og image?
//             int filterImagePortionIndex = 0; // TODO does this prevent the loops from being para? - could do outer loop in para that fills the x-startingX index of 2d array-which then gets converted to 1d
//             for (int x = startingX; x < endingX; x ++) { // TODO does this work? nothing really compiles
//                 for (int y = threadNumber; y < newImageHeight; y += MAX_THREADS) {
//                     ArrayList<Integer> neighborRGBValueArray = utility.getNeighborValues(originalImage, (x + filterWidth/2), (y + filterHeight/2), filterHeight, filterWidth);

//                     int newPixelValue = -1;
//                     if ("linear".equalsIgnoreCase(filterType)) {
//                         newPixelValue = calcAvgRGB(neighborRGBValueArray, weights, scalar);
//                     }

//                     else if("median".equalsIgnoreCase(filterType)) {
//                         newPixelValue = calcMedian(neighborRGBValueArray, weights);
//                     }
//                     filterImagePortionIndex = (x - startingX) * (endingX - startingX) + y;
//                     filterImagePortion[filterImagePortionIndex] = newPixelValue; // if newPixelValue== -1, there is an error // TODO throw an error?
//                     // filterImagePortionIndex++; // TODO backup if indexing calc is wrong
//                 }
//             }
//         }
//         catch (Exception e) {
//             // Throwing an exception
//             throw e;
//         }
//     }
// }