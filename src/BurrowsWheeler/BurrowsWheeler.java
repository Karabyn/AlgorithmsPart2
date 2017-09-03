package BurrowsWheeler;

import edu.princeton.cs.algs4.BinaryStdIn;
import edu.princeton.cs.algs4.BinaryStdOut;

import java.util.Arrays;

/**
 * Algorithms Part II by Princeton University
 * BurrowsWheeler.BurrowsWheeler
 * Petro Karabyn.
 *
 * EXTRACT ALL BurrowsWheeler FILES FROM THE PACKAGE BEFORE EXECUTION.
 *
 * This file takes arguments from the Standard Input and requires to be executed from the cmd.
 * Please, read the instructions.
 * In order to execute correctly:
 * Requires algs4.jar to be installed and everything else to be set up correctly as specified in the course materials:
 * http://algs4.cs.princeton.edu/windows/
 * http://algs4.cs.princeton.edu/code/
 * Compile and execute example:
 * % java-algs4 BurrowsWheeler - < abra.txt
 * Code cannot be in a named package in order for java-algs4 to detect it.
 * If this file and any other files used in BurrowsWheeler assignment are in a package, please extract them beforehand
 * in order to compile and execute. Src folder is good.
 *
 * Weekend Example: https://www.youtube.com/watch?v=xff44-3mAhE&feature=youtu.be
 */

public class BurrowsWheeler {

    // apply Burrows-Wheeler encode, reading from standard input and writing to standard output
    public static void encode() {
        // http://algs4.cs.princeton.edu/code/javadoc/edu/princeton/cs/algs4/BinaryStdIn.html
        String input = BinaryStdIn.readString(); // get input
        BinaryStdIn.close();
        // System.out.println("Received input: " + input);
        CircularSuffixArray csa = new CircularSuffixArray(input); // construct BurrowsWheeler.CircularSuffixArray
        // 1) find first (the row number first in which the original string ends up._
        for (int i = 0; i < csa.length(); i++) {
            if (csa.index(i) == 0) {
                // System.out.println("first: " + i);
                BinaryStdOut.write(i); // first
                break;
            }
        }
        // 2) find Burrows Wheeler encode
        for (int i = 0; i < csa.length(); i++) {
            int originalIndex = csa.index(i); // get the original indexes from sorted suffixes array
            if (originalIndex == 0) { // circular. Prevent out of bounds.
                originalIndex = csa.length();
            }
            // System.out.print(input.charAt(originalIndex - 1));
            BinaryStdOut.write(input.charAt(originalIndex - 1), 8);
        }
        BinaryStdOut.close();
    }

    // apply Burrows-Wheeler inverse encode, reading from standard input and writing to standard output
    public static void decode() {
        int first = BinaryStdIn.readInt();
        String in = BinaryStdIn.readString();
        BinaryStdIn.close();
        char[] t = in.toCharArray(); // the last column t[] of the sorted suffixes. Output of the encode().
        char[] sortedSuffixes = t.clone(); // first column of the sorted suffixes.
        Arrays.sort(sortedSuffixes);
        int[] next = constructNextArray(t);
        int originalIndex = first;
        for (int i = 0; i < next.length; i++) {
            // System.out.print(sortedSuffixes[originalIndex]);
            BinaryStdOut.write(sortedSuffixes[originalIndex]);
            originalIndex = next[originalIndex];
        }
        BinaryStdOut.close();
    }

    /*
    * Key-indexed counting algorithms from 5.1 String Sorts; key-indexed counting applied.
    * Can be implemented as brute force iterating through sortedSuffixes for each
    * char in t[], but results in an unacceptable n^2 complexity.
    */
    private static int[] constructNextArray(char[] t) {
        int length = t.length;
        int R = 256;
        int[] count = new int[R + 1]; // Radix of EXTENDED_ASII + offset
        int[] next = new int[length];
        // Count frequencies of each letter using key as index.
        for (int i = 0; i < length; i++) {
            count[t[i] + 1]++;
        }
        // Compute frequency cumulates which specify destinations.
        for (int r = 0; r < R; r++) {
            count[r+1] += count[r];
        }
        // move items, construct next
        for (int i = 0; i < length; i++) {
            next[count[t[i]]++] = i;
        }
        return next;
    }

    public static void main(String[] args) {
        if (args == null) { throw new IllegalArgumentException("Provide an argument"); }
        if (args[0].equals("-")) {
            encode();
        }
        if (args[0].equals("+")) {
            decode();
        }
    }
}
