import java.util.*;

public class KNN {
    private int k;
    private ArrayList<CellObject> dataset;

    public KNN(int k, ArrayList<CellObject> dataset) {
        this.k = k;
        this.dataset = dataset;
    }

    // **************** Below is used for cross validation and scoring ********************
    public double calcAccuracy(ArrayList<CellObject> testCells) {
        double correctCount = 0;
        for (int i = 0; i < testCells.size(); i++) {
            if (testCells.get(i).getActualCellLabel().equalsIgnoreCase(testCells.get(i).getPredictedCellLabel())) {
                correctCount++;
            }
        }
        return correctCount/testCells.size();
    }

    public ArrayList<ArrayList<ArrayList<CellObject>>> getKFolds(int numberOfFolds, ArrayList<CellObject> cells) {
        shuffleArray(cells);
        ArrayList<ArrayList<CellObject>> trainSets = new ArrayList<>();
        ArrayList<ArrayList<CellObject>> testSets = new ArrayList<>();

        for (int fold = 0; fold < numberOfFolds; fold++) {
            int numberOfObjectsInTestFold = cells.size() / numberOfFolds;
            int foldStartingIndex = numberOfObjectsInTestFold * fold;
            int foldEndingIndex = foldStartingIndex + numberOfObjectsInTestFold;

            ArrayList<CellObject> trainSet = new ArrayList<>();
            ArrayList<CellObject> testSet = new ArrayList<>();

            for (int i = 0; i < cells.size(); i++) {
                if (i >= foldStartingIndex && i < foldEndingIndex) {
                    testSet.add(cells.get(i));
                } else {
                    trainSet.add(cells.get(i));
                }
            }
            trainSets.add(trainSet);
            testSets.add(testSet);
        }

        ArrayList<ArrayList<ArrayList<CellObject>>> result = new ArrayList<>();
        result.add(trainSets);
        result.add(testSets);

        return result;
    }

    private static void shuffleArray(ArrayList<CellObject> cells) {
        Random random = new Random();
        random.nextInt();
        for (int i = 0; i < cells.size(); i++) {
            int change = i + random.nextInt(cells.size() - i);
            swap(cells, i, change);
        }
    }

    private static void swap(ArrayList<CellObject> cells, int i, int change) {
        CellObject helper = cells.get(i);
        cells.set(i, cells.get(change)) ;
        cells.set(change, helper);

    }

    // **************** Below is used for normalization ********************
    public void normalizeDataset(ArrayList<CellObject> cells) {
        ArrayList<Double> maxList = new ArrayList<>();
        ArrayList<Double> minList = new ArrayList<>();
        for (int feature = 0; feature < cells.get(0).getFeatures().length; feature++) {
            maxList.add(cells.get(0).getFeatures()[feature]); // used to track max
            minList.add(cells.get(0).getFeatures()[feature]); // used to track min
        }

        // compute current min/max of each feature
        for (int i = 0; i < cells.size(); i++) {
            double[] features = cells.get(i).getFeatures();
            for (int feature = 0; feature < features.length; feature++) {
                double currentFeature = features[feature];
                if (currentFeature > maxList.get(feature)) {
                    maxList.set(feature, currentFeature);
                }
                if (currentFeature < minList.get(feature)) {
                    minList.set(feature, currentFeature);
                }
            }
        }

        // normalize each feature
        Utility utility = new Utility();
        for (int i = 0; i < cells.size(); i++) {
            double[] features = cells.get(i).getFeatures();
            for (int feature = 0; feature < features.length; feature++) {
                double currentFeature = features[feature];
                double normalizedFeature = utility.normalizePorportional(currentFeature, 0, 1, minList.get(feature), maxList.get(feature));
                features[feature] = normalizedFeature;
            }
        }
    }



    // **************** Below is used for classification ********************
    public void classifyTestSet(ArrayList<CellObject> cells) {
        final int MAX_THREADS = Math.max(1, Math.min(Runtime.getRuntime().availableProcessors(), cells.size()));

        int maxCellsPerThread = cells.size() / MAX_THREADS;
        ArrayList<ArrayList<CellObject>> cellsForGivenThreads = new ArrayList<>();

        int count = 0;
        ArrayList<CellObject> cellsToClassify = new ArrayList<>();
        for (int i = 0; i < cells.size(); i++) {
            if (count < maxCellsPerThread) {
                cellsToClassify.add(cells.get(i));
                count++;
            } else {
                cellsForGivenThreads.add(cellsToClassify);
                cellsToClassify = new ArrayList<>();
                count = 0;
                i--;
            }
        }
        // This accounts for the final thread, which might have less cells meaning it never entered the above "else"
        if (cellsToClassify.size() > 0) {
            cellsForGivenThreads.add(cellsToClassify);
        }

        ClassificationThread[] threadArray = new ClassificationThread[MAX_THREADS];
        for (int i = 0; i < MAX_THREADS; i++) {
            threadArray[i] = new ClassificationThread(k, cellsForGivenThreads.get(i), dataset);
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

class ClassificationThread extends Thread {

    private int k;
    private ArrayList<CellObject> cellsToClassify;
    private ArrayList<CellObject> dataset;

    public ClassificationThread(int k, ArrayList<CellObject> cellsToClassify, ArrayList<CellObject> dataset) {
        this.k = k;
        this.cellsToClassify = cellsToClassify;
        this.dataset = dataset;
    }

    public void run() {
        try {
            // Displaying the thread that is running
//            System.out.println ("Thread " + Thread.currentThread().getId() + " is running"  + ", MAX_THREADS: " + MAX_THREADS + ", startingXCoordinate: " + startingXCoordinate + ", startingYCoordinate: " + startingYCoordinate);;//", xJumpSize: " + xJumpSize + ", yJumpSize: " + yJumpSize);

            for (int i = 0; i < cellsToClassify.size(); i++) {
                classify(cellsToClassify.get(i));
            }
        }
        catch (Exception e) {
            // Throwing an exception
            throw e;
        }
    }

    private void classify(CellObject object) {
        ArrayList<CellObject> distanceArrayList = new ArrayList<>();
        for (int i = 0; i < dataset.size(); i++) {
            double distance = getEuclideanDistanceBetween(object.getFeatures(), dataset.get(i).getFeatures());
            CellObject cellToAdd = new CellObject(null, object.getActualCellLabel(), null, distance);
            distanceArrayList.add(cellToAdd);
        }

        Collections.sort(distanceArrayList, new Comparator<CellObject>() {
            @Override
            public int compare(CellObject o1, CellObject o2) {
                double o1Distance = o1.getDistance();
                double o2Distance = o2.getDistance();
                return Double.compare(o1Distance, o2Distance);
            }
        });

        HashMap<String, Integer> neighborCounts = new HashMap<String, Integer>();

        for (int i = 0; i < distanceArrayList.size(); i++) {
            if (i >= k) {
                break;
            }
            String CellLabel = distanceArrayList.get(i).getActualCellLabel();
            increment(neighborCounts, CellLabel);
        }

        object.setPredictedCellLabel(Collections.max(neighborCounts.entrySet(), Comparator.comparingInt(Map.Entry::getValue)).getKey());
    }

    private static<K> void increment(Map<K, Integer> map, K key) {
        map.putIfAbsent(key, 0);
        map.put(key, map.get(key) + 1);
    }

    private double getEuclideanDistanceBetween(double[] dataElement1, double[] dataElement2) {
        if (dataElement1.length != dataElement2.length) {
            throw new IllegalArgumentException("objects were not the same size");
        }

        double distance = 0;
        for (int i = 0; i < dataElement1.length; i ++) {
            distance += Math.pow((dataElement1[i] - dataElement2[i]), 2);
        }
        return Math.sqrt(distance);
    }
}
