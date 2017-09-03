package WordNet;

import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.DirectedCycle;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.*;
import java.util.stream.Stream;

/**
 * Algorithms Part II by Princeton University
 * Programming assignment 1. WordNet.
 * Petro Karabyn.
 * 21-Aug-2017.
 */

public class WordNet {

    private final Map<Integer, List<String>> synsetsMap; // store synsetID : {nouns}.
    private final Map<Integer, List<Integer>> hypernymsMap; // store hypernym relations to construct a graph
    private final Set<String> nounsSet; // store nouns. HashSet will provide add(), contains() and size() operations
    // with O(1) complexity and won't allow any duplicates.

    private final SAP sap;

    /**
     * constructor takes the name of the two input files
     * Extracts synsets and hypenyms.
     * Constructs a Digraph.
     * Throws a java.lang.IllegalArgumentException if any argument is null.
     * Throws a java.lang.IllegalArgumentException if the input does not correspond to a rooted DAG
     * (Directed Acyclic Graph)
     */
    public WordNet(String synsets, String hypernyms) {
        if (synsets == null || hypernyms == null) {
            throw new IllegalArgumentException();
        }

        this.synsetsMap = new HashMap<>();
        this.hypernymsMap = new HashMap<>();
        this.nounsSet = new HashSet<>();

        parseSynsets(synsets);
        parseHypernyms(hypernyms);

        Digraph wordNetDigraph = new Digraph(synsetsMap.size());
        constructGraph(wordNetDigraph);
        // check if the graph is a rooted DAG
        if (new DirectedCycle(wordNetDigraph).hasCycle() || !isRootedDAG(wordNetDigraph)){
            throw new IllegalArgumentException();
        }


        this.sap = new SAP(wordNetDigraph);

    }

    private void parseSynsets(String synsets) {
        // read file
        Path file = Paths.get(synsets);
        List<String> nounEntries;
        try {
            Stream<String> lines = Files.lines(file, StandardCharsets.UTF_8);
            for (String l : (Iterable<String>) lines::iterator) {
                // parse lines
                String[] line = l.split(","); // split into line[0] = ids; line[1] = synsets; line[2] = gloss.
                // Case 1: more than 1 noun in a synset
                // call split() on a string of synsets only when it contains more than 1 noun.
                if (line[1].contains(" ")) {
                    nounEntries =  Arrays.asList(line[1].split(" "));
                    synsetsMap.put(Integer.parseInt(line[0]), nounEntries);
                    nounEntries.forEach(nounsSet::add);
                }
                // Case 2: only 1 noun in a synset.
                else {
                    synsetsMap.put(Integer.parseInt(line[0]), Arrays.asList(line[1]));
                    nounsSet.add(line[1]);
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        // System.out.println(synsetsMap.toString());
    }

    private void parseHypernyms(String hypernyms) {
        // read file
        Path file = Paths.get(hypernyms);
        try {
            Stream<String> lines = Files.lines(file, StandardCharsets.UTF_8);
            for (String l : (Iterable<String>) lines::iterator) {
                // parse lines
                String[] line = l.split(",");
                List<Integer> ancestors = new ArrayList<>();
                for (int i = 1; i < line.length; i++) {
                    ancestors.add(Integer.parseInt(line[i]));
                }
                hypernymsMap.put(Integer.parseInt(line[0]), ancestors); // add hypernym relations to a data structure.
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void constructGraph(Digraph wordNetDigraph) {
        for (Map.Entry<Integer, List<Integer>> entry : hypernymsMap.entrySet()) {
            int key = entry.getKey();
            entry.getValue().forEach(hypernymId -> wordNetDigraph.addEdge(key, hypernymId));
        }
        // System.out.println(wordNetDigraph.toString());
    }

    private boolean isRootedDAG(Digraph wordNetDigraph) {
        int roots = 0;
        for (int i = 0; i < wordNetDigraph.V(); i++) {
            if (!wordNetDigraph.adj(i).iterator().hasNext()) {
                roots++;
            }
            if (roots > 1) {
                return false;
            }
        }
        return roots == 1;
    }

    // returns all WornNet nouns
    public Iterable<String> nouns() {
        return nounsSet;
    }

    // is the word a WordNet noun?
    public boolean isNoun(String word) {
        if (word == null) {
            throw new IllegalArgumentException();
        }
        return nounsSet.contains(word);
    }

    /**
    * runs in time linear in the size of the WordNet digraph
    * 0(n)
    * @param nounA searched noun in a WordNet
    * @param nounB searched noun in a WordNet
    * @return a synset (second field of synsets.txt) that is the common ancestor of nounA and nounB
    * in a shortest ancestral path
    */
    public String sap(String nounA, String nounB) {
        if (!isNoun(nounA) || !isNoun(nounB)) {
            throw new IllegalArgumentException();
        }
        //System.out.println("sap() call. " + "arguments: " + nounA + ", " + nounB);
        ArrayList<ArrayList<Integer>> ids = getNounIds(nounA, nounB);
        List<String> synset = synsetsMap.get(sap.ancestor(ids.get(0), ids.get(1)));
        String ancestor = "";
        if(synset.size() == 1) {
            ancestor = synset.get(0);
        }
        else {
            for (String aSynset : synset) ancestor += aSynset + " ";
            ancestor = ancestor.substring(0, ancestor.length() - 1);
        }
        return ancestor;
    }

    // distance between nounA and nounB
    public int distance(String nounA, String nounB) {
        if (!isNoun(nounA) || !isNoun(nounB)) {
            throw new IllegalArgumentException();
        }
        ArrayList<ArrayList<Integer>> ids = getNounIds(nounA, nounB);
        return sap.length(ids.get(0), ids.get(1));
    }

    /**
     * runs in time linear in the size of the WordNet digraph
     * 0(n)
     * @param nounA searched noun in a WordNet
     * @param nounB searched noun in a WordNet
     * @return a list of lists containing ids of all synsets whete a given noun occurs
     */
    private ArrayList<ArrayList<Integer>> getNounIds(String nounA, String nounB) {
        List<String> value;
        ArrayList<Integer> synsetAIds = new ArrayList<>();
        ArrayList<Integer> synsetBIds = new ArrayList<>();
        for(Map.Entry<Integer, List<String>> entry : synsetsMap.entrySet()) {
            value = entry.getValue();
            if(value.contains(nounA)) {
                synsetAIds.add(entry.getKey());
            }
            if(value.contains(nounB)) {
                synsetBIds.add(entry.getKey());
            }
        }
        return new ArrayList<ArrayList<Integer>>(){{add(synsetAIds); add(synsetBIds);}};
    }

    public static void main(String[] args) {
        WordNet wordNet = new WordNet("synsets.txt", "hypernyms.txt");

        System.out.println("wordNet.nounsSet.size(): " + wordNet.nounsSet.size() + "; Expected: 119188");

        System.out.println("isNoun(\"Vasya\"): " + wordNet.isNoun("Vasya") + "; Expected: false");
        System.out.println("isNoun(\"gold\"): " + wordNet.isNoun("gold") + "; Expected: true");
        System.out.println("isNoun(\"transition\"): " + wordNet.isNoun("transition") + "; Expected: true");
        System.out.println("isNoun(\"change\"): " + wordNet.isNoun("change") + "; Expected: true");

        System.out.println("sap(\"sprint\", \"locomotion\"): " +
                wordNet.sap("sprint", "locomotion") + "; Expected: locomotion travel.");

        System.out.println("sap(\"worm\", \"bird\"): " +
                wordNet.sap("worm", "bird") + "; Expected: animal animate_being beast brute creature fauna.");

        System.out.println("sap(\"leap\", \"impairment\"): " +
                wordNet.sap("leap", "impairment") + "; Expected: change alteration modification.");

        System.out.println("sap(\"demotion\", \"sacrifice\"): " +
                wordNet.sap("demotion", "sacrifice") + "; Expected: act deed human_action human_activity.");

        System.out.println("wordNet.distance(\"sprint\", \"locomotion\"): " +
                wordNet.distance("sprint", "locomotion") + "; Expected: 2");

        System.out.println("wordNet.distance(\"white_marlin\", \"mileage\"): " +
                wordNet.distance("white_marlin", "mileage") + "; Expected: 23");

    }
}