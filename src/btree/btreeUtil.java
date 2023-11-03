package btree;

import java.util.List;

public class btreeUtil {

    public static int binarySearchChildren (List<Integer> list, int key) {
        int low = 0;
        int high = list.size() - 1;

        while (low <= high) {
            int mid = low + (high - low) / 2;

            if (list.get(mid) == key) {
                return mid+1;
            } else if (list.get(mid) < key) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }

        return low; // return the insert point
    }

    public static int binarySearch(List<Integer> list, int key) {
        int low = 0;
        int high = list.size() - 1;

        while (low <= high) {
            int mid = low + (high - low) / 2;

            if (list.get(mid) == key) {
                return mid;
            } else if (list.get(mid) < key) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }

        return -1; // return the insert point
    }
}
