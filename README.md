# ImageAnalysis
Alwin Hollebrandse

## Running:
A terminal with Maven installed is required to run this code. Provided that requirement is met, run `mvn clean package` in the terminal within the correct maven directory (ImageAnalysis). If the buid was successful, then run the following command to run the project:
`~/MPJ-User/mpj-v0_44/bin/mpjrun.sh -np 2 -jar target/ImageAnalysis-1.0-jar-with-dependencies.jar`
See below for more information about the two internal parameters.

## Adding MPJ to the Project
Inorder to add MPJ (Java's MPI) to the project, this [link](https://github.com/victorskl/mpj-maven) was used. Along with the official docs found [here](http://mpj-express.org/docs/guides/linuxguide.pdf).

## Internal Arguements:
Because I was not sure how get MPJ command line to take a jar and command line arguements, I hardcoded the following 2 parameters. A user will need to make a `images` folder outside of `src`. There will also need to be an `instructions` file at the same file level.

# Image File location Arg:
This is the first internal argument of the program. It should contain a path to where all of the images are located. In thoery, any images that Java’s Buffered Image supports would be supported by my code. An example image can found in the results section under “original”.

# Instruction File arg:
This is the second internal argument for the program. It should be a txt file. It is used to build the operation arraylist (discussed under Implentation/General). Accepted Strings that will cause the actions of interest are (ignoring case): DeletePreviousImages, UsePreviousImage, SingleColor, Quantization, SaltAndPepper, Gaussian, LinearFilter, MedianFilter, Histogram, HistogramEqualization, EdgeDetection, HistogramThresholdingSegmentation, KMeansSegmentation, Erosion, Dilation, FeatureExtraction, and MachineLearning. These can be found in the all_Instructions.txt file with their respective parameters. Note that SingleColor is not a required operation, but without it, all other operations default to a “gray” color operation use.  Note that each of the params must be separated by spaces, and array params are denoted by having each element be separated by a space and the characters: [ and ].
DeletePreviousImages takes in 1 param: a Boolean
UsePreviousImage takes in 1 param: a Boolean
SingleColor takes in 1 param in the file: the color to convert to. Current options are: Gray, Red, Green, and Blue
Quantization takes in 1 param in the file: the scale to scale to. Ex:16
SaltAndPepper takes in 3 param in the file; random threshold, mean, and sigma. Ex: 0.05 0 0
Gaussian takes in 3 param in the file; random threshold, mean, and sigma. Ex: 0.05 0 00 0 5
LinearFilter takes in 4 param in the file; filter width, filter height, and the weights (in the afore mentioned array form). Ex: 0.05 0 03 3 [ 0 0 0 0 0 0 0 0 0 ]
MedianFilter takes in 4 param in the file; filter width, filter height, the weights (in the afore mentioned array form)(can also be null) EX: 3 3 null
SerialMedianFilter takes in 4 param in the file; filter width, filter height, the weights (in the afore mentioned array form)(can also be null) EX: 3 3 null
MPIMedianFilter takes in 4 param in the file; filter width, filter height, the weights (in the afore mentioned array form)(can also be null) EX: 3 3 null
MPIThreadedMedianFilter takes in 4 param in the file; filter width, filter height, the weights (in the afore mentioned array form)(can also be null) EX: 3 3 null
Histogram takes no params.
HistogramEqualization takes no params.
EdgeDetection takes no params.
HistogramThresholdingSegmentation takes no params.
KMeansSegmentation takes no params.
Erosion takes in 3 params: filter width, filter height, colors
Dilation takes in 3 params: filter width, filter height, colors
FeatureExtraction takes no params
MachineLearning takes in 2 params: k, and numberOfFolds



## Example Instruction File Contents:

DeletePreviousImages true

UsePreviousImage false

SingleColor Gray

Quantization 16

SaltAndPepper 0.05 0 0

Gaussian 0 0 5

LinearFilter 3 3 [ 0 0 0 0 0 0 0 0 0 ]

MedianFilter 3 3 null 1

SerialMedianFilter 3 3 null

MPIMedianFilter 3 3 null

Histogram

HistogramEqualization

EdgeDetection

HistogramThresholdingSegmentation

KMeansSegmentation

Erosion 3 3 [ 255 0 255 0 255 0 255 0 255 ]

Dilation 3 3 [ 255 0 255 0 255 0 255 0 255 ]

FeatureExtraction

MachineLearning 5 10


# Implentation:
## General:
This program does the following: first it validates the users command line args. It then parses the instructions.txt file into an arraylist that holds the found Strings that match those and explained in the Instrcution File arg section. This code then loops through all of the images found in the first command line argument.  It then performs the user requested operations in the following order (skipping operations not requested): SingleColor, Quantization, SaltAndPepper, Gaussian, LinearFilter, MedianFilter, Histogram, HistogramEqualization, EdgeDetection, HistogramThresholdingSegmentation, KMeansSegmentation, Erosion, Dilation, FeatureExtraction, and MachineLearning. If there is an error within one of these operations, the operation will be skipped (or if it severe enough, the whole image will be skipped). If the operation was not requested, the code will check if there are any output files related to said operation already in the results folder for the current image. If there is, it will be deleted. This was implanted such that there will be no confusion when checking results. If the operation was requested, it will be completed and added to ./results/{{current image’s name}}/{{operation output}}. Each of these operations works on the outcome of the previous specified operation. Each operation loops through all pixels in the image, which is performed in parallel. This was accomplished by creating a ParallelMatrix File. This file creates how ever many threads the system can support and divides the image pixel matrix into segments of equal size. This was done by looping through all the width pixels but jumping by MAX_THREADS and by looping through all height pixels per width iteration. Each thread starts at an incremented x value. Each thread then calls a code lambda defined in the respective operation’s java file for the current width and height value of the pixel matrix.
The end of the program prints out time metrics collected through the code’s run. These metrics also appear under “results/report.txt”.


## DeletePreviousImages:
This param makes it optional to delete the output of previous runs (if the current instruction file does not cause the files in question to be overridden. For example, the first run contained a “Histogram” call but the second run did not. This param determines if the old “histogram.png” file will be present under the results file of the given image.


## UsePreviousImage:
This param determines if old image.png files that came from previous run’s operation calls should be used instead of the operations from the current call. This still works in combination with the operations below. This means that if the instruction file has this param set to true and contains “KMeansSegmentation” then the old “kMeansSegmentation.png” file will be used to create the programs “segmentationImage.” This does require that the previous file exists though. This param can be used to speed up subsequent program calls to prevent redundant operations that will produce identical results to what already exists.


## SingleColor:
This code is found under SingleColorScale.java. It takes in the original image and the desired color of the image. Accepted color values are (ignoring case): gray, red, green, and blue. The code calls ParallelMatrix with the following lambda: depending on what color the param was, remove unneeded channels (though the image remains in RBG format). It should be noted that research showed that gray pixels should not all be equal. The following formula was used for gray conversion: (int) (c.getRed() * 0.299) + (int) (c.getGreen() * 0.587) + (int) (c.getBlue() * 0.114);. The output is the new image using only the specified channel. The output is saved as {{user specified color}}.jpg


## Quantization:
This code (Quantization.java) takes in the current image, scale, and color (color is used to compute the mean squared error). This code then computes the valid pixel ranges for the given scale.  This is done by performing 256/scale, and adding a running count of that number until 256 is met (forming quantizationArray). This code then passes the following lambda to ParallelMatrix: for the provided pixel loop through quantizationArray starting at position 1. For each used number, check if the current color value is less than the current quantizationArray vlue. If it is, set the color to the value of quantizationArray[i-1]. A use of Booleans prevents this from happening more than once per color. The output is saved as Quantization.jpg.


## SaltAndPepper:
This section is found under NoiseAdder.java. It accepts the following parameters: current image, noiseType, randomThreshold, mean, and sigma. After validating each param, this code provides this lambda: for the given pixel, check which type of noise is being added. If it is “saltAndPepper”, generate a random number. If this is less than or greater than the random threshold, generate another number. If this number is greater than 0.5, set the pixel color to black. Otherwise set it white. The output is saved as saltAndPepper.jpg.


## Gaussian
This section is found under NoiseAdder.java. It accepts the following parameters: current image, noiseType, randomThreshold, mean, and sigma. After validating each param, this code provides this lambda: for the given pixel, check which type of noise is being added. If it is “gaussian”, generate a random gaussian value using the provided mean and sigma. This value is added to each relevant channel and normalized. The output is saved as gaussian.jpg.


## LinearFilter
This section is found under Filter.java. This filter methods crops of borders of images that do not have a large enough border for an edge pixel. It accepts the following parameters: current image, filterType, filterWidth, filterHeight, and weights. After validating each param, this code provides this lambda: for the given pixel, check which type of filter is being applied. If it is “linear”, take the user’s filter mask size and compute the average of each pixel in that filter (where each pixel is first multiplied by the corresponding weight value). The center pixel of this filter is set to that average value. Note that each channel of the pixel is computed independently. The output of this method is saved as linear.jpg.

## MedianFilter
This section is found under Filter.java. This filter methods crops of borders of images that do not have a large enough border for an edge pixel. It accepts the following parameters: current image, filterType, filterWidth, filterHeight, and weights. After validating each param, this code provides this lambda: for the given pixel, check which type of filter is being applied. If it is “median”, take the user’s filter mask size and adds each pixel to a list by the corresponding weight amount of times. The median of this list is then computed. That becomes the value of the filter’s center pixel. The output of this method is saved as median.jpg.

## SerialMedianFilter
Performs the median filter except in serial instead of the lambda.

## MPIMedianFilter
Performs the median filter except in MPJ instead of the lambda.

## MPIThreadedMedianFilter
Performs the median filter except in MPJ that includes threading within each created process.

## Histogram
This section is found under GraphHistogram.java. It accepts the following parameter: current image. In order to create a histogram, each pixel is looked at (not in parallel since the required semaphore would slow the process to slower than a sequential version) and the gray color value ((int) (c.getRed() * 0.299) + (int) (c.getGreen() * 0.587) + (int) (c.getBlue() * 0.114)) is added to an int array of size 256 in the correct position. This histogram is then saved to histogram.png.


## HistogramEqualization
This section is found under HistogramFunctions.java. It accepts the following parameter: current image and histogram. In order to equalize the histogram, a look up table is created by creating an array of size 256. For each value of this array, the value is the histogram value at the same position plus the previous look up value. This lookup table is given to the lambda which does the following: for each pixel, get the current gray value and use that as the key for the lookup table. That is the new value of the pixel. This code has two outputs: equalizedHistogram.png and equalizedImage.jpg.


## EdgeDetection
This section is found under EdgeDetection.java. It has no parameters. This operation begins by performing the LaPlace filter on the given image. This sharpened image then has the compass edge detection performed on it. “Edge filters” in four directions (performed through the filter operations and different weight parameters). The EdgeMap image gets a white pixel if any of the four calculated edge strengths (or their negative counter parts, which accounts for the other four directions) is greater than 1. This code returns the an EdgeMap for the provided image. This image can be found at edgeDetection.jpg.


## HistogramThresholdingSegmentation
This section is found under ThresholdSegmentation.java. It accepts a histogram as an input. This operation begins by calculating the optimal pixel threshold. This is done by calculating the variance associated with a given pixel if it were the threshold. The pixel value with the minimum calculated value is selected. Then for every pixel, the pixel turns white if it less than the threshold and black otherwise. Detected objects are white greater than 0. This code returns a segmented version of the provided image. This image can be found at histogramThresholdingSegmentation.jpg.


## KMeansSegmentation
This section is found under KMeansSegmentation.java. It accepts a histogram as an input. This operation performs a modified K++ algorithm on the histogram. The initial K points are selected as follows: place a point at the histogram index that has the highest occurrence value, and then place a point at the furthest histogram index from the first index while having a non-zero histogram value. The standard k means algorithm takes over from that point and results in 2 final clusters. Then for every pixel, the pixel turns white if its value is located in the “object” cluster and black otherwise. Detected objects are white greater than 0. This code returns a segmented version of the provided image. This image can be found at kMeansSegmentation.jpg.


## Erosion
This section is found under MorphologicalFunctions.java. It accepts the following parameters: filterWidth, filterHeight, colors, and morphologicalType. If there was no segmentation instruction, then this operation first performs HistogramThresholdingSegmentation. This operation occurs when morphologicalType is set to “erosion.” For each non-cropped pixel, the following happens. Check if each pixel in the current image filtered window matches the associated value in the colors array. If Each does, keep the pixel. Otherwise, remove the pixel by setting it to black. This image can be found at Erosion.jpg.


## Dilation
This section is found under MorphologicalFunctions.java. It accepts the following parameters: filterWidth, filterHeight, colors, and morphologicalType. If there was no segmentation instruction, then this operation first performs HistogramThresholdingSegmentation. This operation occurs when morphologicalType is set to “dilation.” For each non-cropped pixel, the following happens. Check if the current pixel in the provided image has a non-zero value and is “object.” If it is, set each associated pixel in the current filter window to “object” color if the associated colors value is non-zero.  Otherwise, keep the pixel the same as it was. This image can be found at Dilation.jpg.


## FeatureExtraction:
Provided a “segmentationImage” was set (if not, histogramThresholding is called) and a histogram exists (if not, one is created), then this operation computes the following features:
•	The mean of the histogram
•	The standard deviation of the histogram
•	The area of the objects in the segmented image (a count of the white pixels in the “segmentationImage”)
•	The entropy of the image (where “Entropy is a statistical measure of randomness that can be used to characterize the texture of the input image.” And is defined as -sum(p.*log2(p))

## MachineLearning:
This code uses two parameters: k and numberOfFolds. This code first normalizes each feature set independently proportionally between 0 and 1. Then the dataset is taken and shuffled. This mixed data is the split into “numberOfFolds” segments. These segments are used to craft the training and testing sets used in cross validation. For each training set and associated test set (these two data sets are mutually exclusive and describe the entire dataset), a kNN classifier is used. This classifier calculates the Euclidean Distance of the current test Cell to classify to each cell in the training set. The top “k” closest cells are then taken. The cell to be classified is assigned to the class/label of which ever label appears more in the closest “k” cells from the training set. Each fold reports its own accuracy. Then a final combination accuracy is computed and reported. This score is a combination of all other folds and is calculated by taken the mean of all folds’ accuracies.



## Example Metrics
Accuracy per fold: [0.14285714285714285, 0.2653061224489796, 0.20408163265306123, 0.22448979591836735, 0.12244897959183673, 0.16326530612244897, 0.1836734693877551, 0.16326530612244897, 0.24489795918367346, 0.24489795918367346]
Average accuracy: 0.1959183673469388

Final Time Metrics:


Converting to a single color processing time for the entire batch (ms): 23118
Average converting to a single color processing time (ms): 46

Quantization processing time for the entire batch (ms): 23049
Average quantization processing time (ms): 46
total meanSquaredError: 2147483647

Adding salt and pepper noise processing time for the entire batch (ms): 25213
Average adding salt and pepper noise processing time (ms): 50

Adding gaussian noise processing time for the entire batch (ms): 31483
Average adding gaussian noise processing time (ms): 62

Linear filter processing time for the entire batch (ms): 31555
Average linear filter processing time (ms): 63

Median filter processing time for the entire batch (ms): 45897
Average median filter processing time (ms): 91

Histogram creation processing time for the entire batch (ms): 4724
Average histogram creation processing time (ms): 9

Equalized histogram creation processing time for the entire batch (ms): 15526
Average equalized histogram creation processing time (ms): 31

Edge detection creation processing time for the entire batch (ms): 131924
Average edge detection processing time (ms): 263

Histogram thresholding segmentation time creation processing time for the entire batch (ms): 27517
Average histogram thresholding segmentation processing time (ms): 55

K means segmentation time creation processing time for the entire batch (ms): 20766
Average k means segmentation processing time (ms): 41

Erosion time creation processing time for the entire batch (ms): 10535
Average k means segmentation processing time (ms): 21

Dilation time creation processing time for the entire batch (ms): 7038
Average k means segmentation processing time (ms): 14

Feature extraction processing time for the entire batch (ms): 3008
Average feature extraction processing time (ms): 6

Machine Learning processing time for the entire batch (ms): 54

Total RunTime (without image exporting) (s): 365
Real run time (s): 576
