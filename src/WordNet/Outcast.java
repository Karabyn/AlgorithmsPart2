package WordNet;

import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

/*
 * Algorithms Part II by Princeton University
 * Programming assignment 1. WordNet.
 * Petro Karabyn.
 * 26-Aug-2017.
 * Measuring the semantic relatedness of two nouns.
 * Given a list of wordnet nouns A1, A2, ..., An, which noun is the least related to the others?
 * To identify an outcast, compute the sum of the distances between each noun and every other one:
  * di   =   dist(Ai, A1)   +   dist(Ai, A2)   +   ...   +   dist(Ai, An)
 */

public class Outcast {

    private final WordNet wordNet;

    // constructor takes a WordNet object
    public Outcast(WordNet wordnet) {
        this.wordNet = wordnet;
    }

    // given an array of WordNet nouns, return an outcast
    public String outcast(String[] nouns) {
        // System.out.println("HEY");
        int thisDistance = 0;
        int maxDistance = 0;
        String outcast = "";
        for (String thisNoun : nouns) {
            // System.out.println("\n " + thisNoun + "\n");
            int n = 0;
            for(String otherNoun : nouns) {
                n++;
                thisDistance += wordNet.distance(thisNoun, otherNoun);
                // System.out.println(n + ". " + otherNoun + " " + thisDistance);
            }
            if (thisDistance > maxDistance) {
                outcast = thisNoun;
                maxDistance = thisDistance;
            }
            thisDistance = 0;
        }
        return outcast;
    }

    public static void main(String[] args) {
        // args: synsets.txt hypernyms.txt outcast5.txt outcast8.txt outcast11.txt
        WordNet wordnet = new WordNet(args[0], args[1]);
        Outcast outcast = new Outcast(wordnet);
        for (int t = 2; t < args.length; t++) {
            In in = new In(args[t]);
            String[] nouns = in.readAllStrings();
            StdOut.println(args[t] + ": " + outcast.outcast(nouns));
        }

        /*
        Here is a sample execution:
        % more outcast5.txt
        horse zebra cat bear table

        % more outcast8.txt
        water soda bed orange_juice milk apple_juice tea coffee

        % more outcast11.txt
        apple pear peach banana lime lemon blueberry strawberry mango watermelon potato


        % java Outcast synsets.txt hypernyms.txt outcast5.txt outcast8.txt outcast11.txt
        outcast5.txt: table
        outcast8.txt: bed
        outcast11.txt: potato
         */
    }
}
