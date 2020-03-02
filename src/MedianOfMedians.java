import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;


//modified code from  https://github.com/email4rohit/interview-java-algo/blob/master/MedianOfMedians.java  to use arraylists
public class MedianOfMedians {

    public int findMedian(ArrayList<Integer> list, int k,int low,int high) {
        // Uncomment this if you want to print the current subArray being processed/searched
        //printArray(arr, low, high);

        if(low == high) {
            return list.get(low);
        }

        // sort the mth largest element in the given list
        int m = partition(list,low,high);

        // Adjust position relative to the current subarray being processed
        int length = m - low + 1;

        // If mth element is the median, return it
        if(length == k) {
            return list.get(m);
        }

        // If mth element is greater than median, search in the left subarray
        if(length > k) {
            return findMedian(list,k,low,m-1);
        }
        // otherwise search in the right subArray
        else {
            return findMedian(list,k-length,m+1,high);
        }

    }


    private static int partition(ArrayList<Integer> list, int low, int high) {
        // Get pivotvalue by finding median of medians
        int pivotValue = getPivotValue(list, low, high);

        // Find the sorted position for pivotVale and return it's index
        while(low < high) {
            while(list.get(low) < pivotValue) {
                low ++;
            }

            while(list.get(high) > pivotValue) {
                high--;
            }

            if(list.get(low) == list.get(high)) {
                low ++;
            }
            else if(low < high) {
                int temp = list.get(low);
                list.set(low, list.get(high));
                list.set(high, temp);
            }

        }
        return high;
    }

    // Find pivot value, such the it is always 'closer' to the actual median
    private static int getPivotValue(ArrayList<Integer> list, int low,int high) {
        // If number of elements in the array are small, return the actual median
        if(high-low+1 <= 9) {
            Collections.sort(list);
            return list.get(list.size()/2);
        }

        //Otherwise divide the array into subarray of 5 elements each, and recursively find the median

        // Array to hold '5 element' subArray, last subArray may have less than 5 elements
        int temp[] = null;

        // Array to hold the medians of all '5-element SubArrays'
        int medians[] = new int[(int)Math.ceil((double)(high-low+1)/5)];
        int medianIndex = 0;

        while(low <= high) {
            // get size of the next element, it can be less than 5
            temp = new int[Math.min(5,high-low+1)];

            // copy next 5 (or less) elements, in the subarray
            for(int j=0;j<temp.length && low <= high;j++) {
                temp[j] = list.get(low);
                low++;
            }

            // sort subArray
            Arrays.sort(temp);

            // find mean and store it in median Array
            medians[medianIndex] = temp[temp.length/2];
            medianIndex++;
        }

        // Call recursively to find median of medians
        return getPivotValue(list,0,medians.length-1);
    }
}