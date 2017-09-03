package Boggle;

import edu.princeton.cs.algs4.In;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Collections;


/**
 * Algorithms Part II by Princeton University
 * Boggle
 * Petro Karabyn.
 */

public class BoggleSolver
{
    private final BoggleTrieSET trieSetDict;

    // Initializes the data structure using the given array of strings as the trieSetDict.
    // (You can assume each word in the trieSetDict contains only the uppercase letters A through Z.)
    public BoggleSolver(String[] dictionary) {
        trieSetDict = new BoggleTrieSET();
        for (String word : dictionary)
            if (word.length() > 2) // A valid word must contain at least 3 letters.
                trieSetDict.add(word);
    }

    // Returns the set of all valid words in the given Boggle board, as an Iterable.
    public Iterable<String> getAllValidWords(BoggleBoard board) {
        Set<String> validWordsSet = new HashSet<>();
        boolean[][] visited = new boolean[board.rows()][board.cols()];
        for (int row = 0; row < board.rows(); row++) {
            for (int col = 0; col < board.cols(); col++) {
                searchWords(board, row, col, visited, "", validWordsSet);
            }
        }
        return validWordsSet;
    }

    // Returns the score of the given word if it is in the trieSetDict, zero otherwise.
    // (You can assume the word contains only the uppercase letters A through Z.)
    public int scoreOf(String word) {
        if (!trieSetDict.contains(word)) return 0;

        int length = word.length();
        if (length == 3 || length == 4) return 1;
        else if (length == 5) return 2;
        else if (length == 6) return 3;
        else if (length == 7) return 5;
        else return 11;
    }

    /*
     * Helper method that searches for valid words in the Board
     * and adds them to the collection.
     */
    private void searchWords(BoggleBoard board, int row, int col, boolean[][] visited, String prefix, Set<String> wordsSet) {
        if (visited[row][col]) return;
        char letter = board.getLetter(row, col);
        String word = prefix;
        word += letter;
        // The Qu special case
        if (letter == 'Q') word += "U";
        // Optimization: check if the prefix exists. If not, no point in continuing search.
        BoggleTrieSET.Node node = trieSetDict.get(trieSetDict.root, word, 0);
        if (node == null) {
            return;
        }
        // Check if the prefix is a word. If so, add it to the valid words.
        // Optimization: Instead of calling contains() that has an inside call to get(), which we've already performed
        // a couple lines above and retrieved a Node use the node reference and isSting field. 5-10% speed up!
        if (node.isString) {
            wordsSet.add(word);
        }
        visited[row][col] = true;
        // possible moves: up, down, left, right.
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int newRow = row + i, newCol = col + j; // get new positions
                // continue searching if the position is correct. Change position otherwise.
                if (isInBounds(board, newRow, newCol) && !(newRow == row && newCol == col)) {
                    searchWords(board, row + i, col + j, visited, word, wordsSet);
                }
            }
        }
        visited[row][col] = false;
    }
    /*
     * Check if the new position is a correct one inside the board bounds.
     */
    private boolean isInBounds(BoggleBoard board, int row, int col) {
        return (row >= 0 && row < board.rows()) && (col >= 0 && col < board.cols());
    }

    /*
     * Shuffles an array of strings and returns a List<String>.
     */
    private List<String> shuffleDictionary(String[] dictionary) {
        List<String> shuffledDict = Arrays.asList(dictionary);
        Collections.shuffle(shuffledDict);
        return shuffledDict;
    }

    /*
    * A custom data structure for solving Boggle.
    * Simplified version of a TrieSET developed by Robert Sedgewick and Kevin Wayne
    * modified for Boggle assignment needs and requirements
    * Algs-4 TrieSET: http://algs4.cs.princeton.edu/code/edu/princeton/cs/algs4/TrieSET.java.html
    */
    private static class BoggleTrieSET {
        private static final int R = 26;        // extended ASCII

        private Node root;      // root of trie
        private int N;          // number of keys in trie

        // R-way trie node
        private static class Node {
            private Node[] next = new Node[R];
            private boolean isString;
        }

        /**
         * Does the set contain the given key?
         *
         * @param key the key
         * @return <tt>true</tt> if the set contains <tt>key</tt> and
         * <tt>false</tt> otherwise
         * @throws NullPointerException if <tt>key</tt> is <tt>null</tt>
         */
        public boolean contains(String key) {
            Node x = get(root, key, 0);
            if (x == null) return false;
            return x.isString;
        }

        private Node get(Node x, String key, int d) {
            if (x == null) return null;
            if (d == key.length()) return x;
            char c = key.charAt(d);
            return get(x.next[c - 'A'], key, d + 1);
        }

        /**
         * Adds the key to the set if it is not already present.
         *
         * @param key the key to add
         * @throws NullPointerException if <tt>key</tt> is <tt>null</tt>
         */
        public void add(String key) {
            root = add(root, key, 0);
        }

        private Node add(Node x, String key, int d) {
            if (x == null) x = new Node();
            if (d == key.length()) {
                if (!x.isString) N++;
                x.isString = true;
            } else {
                char c = key.charAt(d);
                x.next[c - 'A'] = add(x.next[c - 'A'], key, d + 1);
            }
            return x;
        }

        /**
         * Returns the number of strings in the set.
         *
         * @return the number of strings in the set
         */
        public int size() {
            return N;
        }

        public Node getRoot() {
            return root;
        }

        public boolean hasPrefix(String query) {
            Node x = get(root, query, 0);
            return x != null;
        }
    }

    public static void main(String[] args)
    {
        // CORRECTNESS TEST

        In in = new In(args[0]);
        String[] dictionary = in.readAllStrings();
        BoggleSolver solver = new BoggleSolver(dictionary);
        BoggleBoard board = new BoggleBoard(args[1]);
        int score = 0;
        for (String word : solver.getAllValidWords(board)) {
            System.out.println(word);
            score += solver.scoreOf(word);
        }
        System.out.println("Score = " + score);


        // PERFORMANCE STRESS TEST
        //In in = new In(args[0]);
        //String[] dictionary = in.readAllStrings();
        //Boggle.BoggleSolver solver = new Boggle.BoggleSolver(dictionary);
        long startTime = System.currentTimeMillis();
        int n = 25000;
        for(int i = 0; i < n; i++) {
            Boggle.BoggleBoard board2 = new Boggle.BoggleBoard(4, 4);
            int score2 = 0;
            for (String word : solver.getAllValidWords(board2))
            {
                score2 += solver.scoreOf(word);
            }
            // StdOut.println("Score = " + score2);
        }
        System.out.println("*** Performance timing and output ***");
        System.out.println("Dictionary: " + args[0]);
        long endTime = System.currentTimeMillis();
        double executionTimeMillis = endTime - startTime;
        double executionTimeSeconds = executionTimeMillis / 1000.0;
        double boardsPerMilli = n / executionTimeMillis;
        double boardsPerSecond = n / executionTimeSeconds;

        System.out.println("Solved " + n + " boards in " + executionTimeMillis + " milliseconds");
        System.out.println("Speed: " + boardsPerMilli + " boards/ms");
        System.out.println("Speed: " + boardsPerSecond + " boards/s");


    }
}
