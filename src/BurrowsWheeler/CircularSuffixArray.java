package BurrowsWheeler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Algorithms Part II by Princeton University
 * BurrowsWheeler
 * Petro Karabyn.
 */

public class CircularSuffixArray {

    private int length;
    private List<Character> originalSuffixes;
    private List<Integer> sortedIndexes; // this is a collection of original indexes of original suffixes in a
    // sorted suffixes order

    // circular suffix array of s. takes time proportional to n log n.
    public CircularSuffixArray(String s) {
        if (s == null) { throw new IllegalArgumentException("Null Argument"); }
        length = s.length();
        originalSuffixes = new ArrayList<>();
        sortedIndexes = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            originalSuffixes.add(s.charAt(i));
            sortedIndexes.add(i);
        }
        // https://docs.oracle.com/javase/7/docs/api/java/util/Collections.html
        // performs a stable, adaptive, iterative mergesort in time proportional to n log n
        Collections.sort(sortedIndexes, new SuffixArrayComparator());
    }

    // length of s
    public int length() {
        return length;
    }

    // returns index of ith sorted suffix
    public int index(int i) {
        if (i < 0 || i > length - 1) { throw new IllegalArgumentException("Out of range argument"); }
        return sortedIndexes.get(i);
}

    /**
     * This class is used to order indexes of original suffixes in order that corresponds to the order of
     * sorted suffixes.
     */
    private class SuffixArrayComparator implements Comparator<Integer> {

        @Override
        // order indexes by comparing the original suffixes
        public int compare(Integer index1, Integer index2) {
            // System.out.println("sort(). i: " + index1 + ", j: " + index2);
            int i = index1;
            int j = index2;
            for (int k = 0; k < length; k++) {
                // System.out.println("k: " + k);
                if (originalSuffixes.get(i) < originalSuffixes.get(j)) {
                    // System.out.println(originalSuffixes.get(i) + "<" + originalSuffixes.get(j));
                    return -1;
                }
                else if (originalSuffixes.get(i) > originalSuffixes.get(j)) {
                    // System.out.println(originalSuffixes.get(i) + ">" + originalSuffixes.get(j));
                    return 1;
                }
                else { // equal. compare the next ones in circular order.
                    // System.out.println(originalSuffixes.get(i) + "=" + originalSuffixes.get(j));
                    if (i == length - 1) i = 0;
                    else i++;
                    if (j == length - 1) j = 0;
                    else j++;
                }
            }
            return 0;
        }
    }

    public static void main(String[] args) {
        CircularSuffixArray csa = new CircularSuffixArray("ABRACADABRA!");
        System.out.println(csa.originalSuffixes);
        System.out.println(csa.sortedIndexes + " Expected: 11, 10, 7, 0, 3, 5, 8, 1, 4, 6, 9, 2");
        for(int i : csa.sortedIndexes) {
            System.out.print(csa.originalSuffixes.get(i) + " ");
        }

        /*
        System.out.println("\n");

        csa = new BurrowsWheeler.CircularSuffixArray("BRACADABRA!A");
        System.out.println(csa.originalSuffixes);
        System.out.println(csa.sortedIndexes);
        for(int i : csa.sortedIndexes) {
            System.out.print(csa.originalSuffixes.get(i) + " ");
        }

        System.out.println("\n");

        csa = new BurrowsWheeler.CircularSuffixArray("RACADABRA!AB");
        System.out.println(csa.originalSuffixes);
        System.out.println(csa.sortedIndexes);
        for(int i : csa.sortedIndexes) {
            System.out.print(csa.originalSuffixes.get(i) + " ");
        }

        System.out.println("\n");

        csa = new BurrowsWheeler.CircularSuffixArray("ACADABRA!ABR");
        System.out.println(csa.originalSuffixes);
        System.out.println(csa.sortedIndexes);
        for(int i : csa.sortedIndexes) {
            System.out.print(csa.originalSuffixes.get(i) + " ");
        }
        */
    }

}
