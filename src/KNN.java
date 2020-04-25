import java.util.*;

public class KNN {
    private int k;
    private ArrayList<CellObject> dataset;

    public KNN(int k, ArrayList<CellObject> dataset) {
        this.k = k;
        this.dataset = dataset;
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

    // TODO move this to a thread and move the test loop in here... but cant follow the same pattern due to this not using an image...
    // TODO paralize
    public void classify(CellObject object) {
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
//                return o1Distance.compareTo(o2Distance);
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

//        Collections.max(neighborCounts.entrySet(), (entry1, entry2) -> entry1.getValue() - entry2.getValue()).getKey();
        object.setPredictedCellLabel(Collections.max(neighborCounts.entrySet(), Comparator.comparingInt(Map.Entry::getValue)).getKey());
    }

    private static<K> void increment(Map<K, Integer> map, K key) {
        map.putIfAbsent(key, 0);
        map.put(key, map.get(key) + 1);
    }

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
        ArrayList<ArrayList<CellObject>> trainSets = new ArrayList<ArrayList<CellObject>>();
        ArrayList<ArrayList<CellObject>> testSets = new ArrayList<ArrayList<CellObject>>();

        for (int fold = 0; fold < numberOfFolds; fold++) {
            int numberOfObjectsInTestFold = cells.size() / numberOfFolds;
            int foldStartingIndex = numberOfObjectsInTestFold * fold;
            int foldEndingIndex = foldStartingIndex + numberOfObjectsInTestFold;

            ArrayList<CellObject> trainSet = new ArrayList<CellObject>();
            ArrayList<CellObject> testSet = new ArrayList<CellObject>();

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

        ArrayList<ArrayList<ArrayList<CellObject>>> result = new ArrayList<ArrayList<ArrayList<CellObject>>>();
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
}
