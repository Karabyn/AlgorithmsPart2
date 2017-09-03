package WordNet;

import edu.princeton.cs.algs4.*;

/**
 * Algorithms Part II by Princeton University
 * Programming assignment 1. WN.
 * Petro Karabyn.
 * 20-Aug-2017.
 * This class represents a shortest ancestral path between
 * any two given vertices in a directed graph.
 */

public class SAP {

    private final Digraph graph; // instance variable to store a graph

    /**
     * constructor takes a digraph (not necessarily a DAG).
     * DAG = directed acyclic graph.
     */
    public SAP(Digraph G) {
        if (G == null) throw new IllegalArgumentException();
        graph = new Digraph(G); // a defensive copy. Keep SAP immutable.
    }

    /**
     * @param v: vertex in a graph
     * @return Stack containing all ancestors of v.
     */
    private Stack getAncestors(int v) {
        return (Stack) new BreadthFirstDirectedPaths(graph, v).pathTo(0);
    }

    /**
     * @param v: vertex in a graph
     * @param w: vertex in a graph
     * @return length of shortest ancestral path between v and w; -1 if no such path
     */
    public int length(int v, int w) {
        if (v < 0 || w < 0 || v > graph.V() - 1 || w > graph.V() - 1) {
            throw new IllegalArgumentException();
        }
        int closestCommonAncestor = ancestor(v, w);
        if (closestCommonAncestor < 0) {
            return -1;
        }
        BreadthFirstDirectedPaths vBreadth = new BreadthFirstDirectedPaths(graph, v);
        BreadthFirstDirectedPaths wBreadth = new BreadthFirstDirectedPaths(graph, w);
        return vBreadth.distTo(closestCommonAncestor) + wBreadth.distTo(closestCommonAncestor);
    }

    /**
     * A common ancestor of v and w that participates in a shortest ancestral path; -1 if no such path.
     * Finds a common ancestor of v and w to which the total distance from v and w to the ancestor is the
     * shortest.
     * Executes at time proportional to E + V in the worst case.
     */
    public int ancestor(int v, int w) {
        if (v < 0 || w < 0 || v > graph.V() - 1 || w > graph.V() - 1) {
            throw new IllegalArgumentException();
        }
        BreadthFirstDirectedPaths vBreadth = new BreadthFirstDirectedPaths(graph, v);
        BreadthFirstDirectedPaths wBreadth = new BreadthFirstDirectedPaths(graph, w);
        return computeClosestCommonAncestor(vBreadth, wBreadth);
    }

    /**
     * Same logic as the method: int length(int v, int w).
     * @return length of shortest ancestral path between any vertex in v and any vertex in w; -1 if no such path
     */
    public int length(Iterable<Integer> v, Iterable<Integer> w) {
        if(v == null || w == null || !isArgValid(v) || !isArgValid(w)) {
            throw new IllegalArgumentException();
        }
        int closestCommonAncestor = ancestor(v, w);
        if (closestCommonAncestor < 0) {
            return -1;
        }
        BreadthFirstDirectedPaths vBreadth = new BreadthFirstDirectedPaths(graph, v);
        BreadthFirstDirectedPaths wBreadth = new BreadthFirstDirectedPaths(graph, w);
        return vBreadth.distTo(closestCommonAncestor) + wBreadth.distTo(closestCommonAncestor);
    }

    /**
     * Same logic as the method: int ancestor(int v, int w),
     * but uses a different constructor of BreadthFirstDirectedPaths that takes Iterable as an argument
     * instead of an int.
     * @return a common ancestor that participates in shortest ancestral path; -1 if no such path
     */
    public int ancestor(Iterable<Integer> v, Iterable<Integer> w) {
        if(v == null || w == null || !isArgValid(v) || !isArgValid(w)) {
            throw new IllegalArgumentException();
        }
        BreadthFirstDirectedPaths vBreadth = new BreadthFirstDirectedPaths(graph, v);
        BreadthFirstDirectedPaths wBreadth = new BreadthFirstDirectedPaths(graph, w);
        return computeClosestCommonAncestor(vBreadth, wBreadth);
    }

    /*
    * helper method for an ancestor() method
    */
    private int computeClosestCommonAncestor(BreadthFirstDirectedPaths v, BreadthFirstDirectedPaths w) {
        int closestCommonAncestor = -1;
        int shortestDistance = Integer.MAX_VALUE;
        int thisDistance;

        for (int vertex = 0; vertex < graph.V(); vertex++) {
            if (v.hasPathTo(vertex) && w.hasPathTo(vertex)) {
                thisDistance = v.distTo(vertex) + w.distTo(vertex);
                if(thisDistance < shortestDistance) {
                    closestCommonAncestor = vertex;
                    shortestDistance = thisDistance;
                }
            }
        }
        return closestCommonAncestor;
    }


    /*
     * check if any argument vertex is invalid—not between 0 and G.V() - 1
     */
    private boolean isArgValid(Iterable<Integer> vertices) {
        for (int vertex : vertices) {
            if(vertex < 0 || vertex > graph.V() - 1) {
                return false;
            }
        }
        return true;
    }

    /**
     * Test client.
     * The following test client takes the name of a digraph input file as as a command-line argument,
     * constructs the digraph, reads in vertex pairs from standard input,
     * and prints out the length of the shortest ancestral path between
     * the two vertices and a common ancestor that participates in that path:
     */
    private void test(String[] args) {
        In in = new In(args[0]);
        Digraph G = new Digraph(in);
        SAP sap = new SAP(G);
        while (!StdIn.isEmpty()) {
            int v = StdIn.readInt();
            int w = StdIn.readInt();
            int length   = sap.length(v, w);
            int ancestor = sap.ancestor(v, w);
            StdOut.printf("length = %d, ancestor = %d\n", length, ancestor);
        }
    }

    public static void main(String[] args) {
        In in = new In("digraph1.txt");
        Digraph digraph1 = new Digraph(in);
        System.out.println("digraph1: \n" + digraph1.toString());
        SAP sap = new SAP(digraph1);
        System.out.println("length: " + sap.length(3, 11)); // expected: 4
        System.out.println("common ancestor: " + sap.ancestor(1, 5)); // expected: 1

    }
}
