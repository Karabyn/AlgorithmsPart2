package BurrowsWheeler;

import edu.princeton.cs.algs4.BinaryStdIn;
import edu.princeton.cs.algs4.BinaryStdOut;

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
 * % javac-algs4 MoveToFront.java
 * % java-algs4 MoveToFront - < abra.txt | java-algs4 edu.princeton.cs.algs4.HexDump 16
 * % java-algs4 MoveToFront - < abra.txt | java-algs4 MoveToFront +
 *
 */

public class MoveToFront {

    // apply move-to-front encoding, reading from standard input and writing to standard output
    public static void encode() {
        int[] eASCII = constructAlphabet();
        while (!BinaryStdIn.isEmpty()) {
            char character = BinaryStdIn.readChar();
            for (int i = 0; i < eASCII.length; i++) {
                if (character == eASCII[i]) {
                    BinaryStdOut.write(i, 8);
                    System.arraycopy(eASCII, 0, eASCII, 1, i);
                    eASCII[0] = character;
                    break;
                }
            }
        }
        BinaryStdIn.close();
        BinaryStdOut.close();
    }

    // apply move-to-front decoding, reading from standard input and writing to standard output
    public static void decode() {
        int[] eASCII = constructAlphabet();
        while (!BinaryStdIn.isEmpty()) {
            int dec = BinaryStdIn.readChar();
            char character = (char) eASCII[dec];
            BinaryStdOut.write(character);
            if (dec != 0) {
                System.arraycopy(eASCII, 0, eASCII, 1, dec);
                eASCII[0] = character;
            }
        }
        BinaryStdIn.close();
        BinaryStdOut.close();
    }

    private static int[] constructAlphabet() {
        int[] eASCII = new int[256]; // R = 256 EASCII
        for (int i = 0; i < eASCII.length; i++)
            eASCII[i] = i;
        return eASCII;
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
