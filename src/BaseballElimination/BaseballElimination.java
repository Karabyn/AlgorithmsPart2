package BaseballElimination;

import edu.princeton.cs.algs4.FlowEdge;
import edu.princeton.cs.algs4.FlowNetwork;
import edu.princeton.cs.algs4.FordFulkerson;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

import java.util.ArrayList;
import java.util.List;

/**
 * Algorithms Part II by Princeton University
 * BaseballElimination
 * Petro Karabyn.
 *
 * Can be executed in IDE and tested in main.
 * In case of executing from cmd extract the file from the named package and
 * follow the instructions described in BurrowsWheeler documentation.
 */

public class BaseballElimination {

    private final int numberOfTeams;
    private final List<String> teams;
    private final int[] wins;
    private final int[] losses;
    private final int[] remaining;
    private final int[][] gameMatrix;

    // create a baseball division from given filename in format specified below
    public BaseballElimination(String filename) {
        In in = new In(filename);

        numberOfTeams = in.readInt();
        teams = new ArrayList<>();
        wins = new int[numberOfTeams];
        losses = new int[numberOfTeams];
        remaining = new int[numberOfTeams];
        gameMatrix = new int[numberOfTeams][numberOfTeams];

        extractContents(in);
    }

    /**
     * Helper method to retrieve and save data from the file input.
     * @param in input from file
     */
    private void extractContents(In in) {
        for (int i = 0; i < numberOfTeams; i++) {
            teams.add(in.readString());
            wins[i] = in.readInt();
            losses[i] = in.readInt();
            remaining[i] = in.readInt();
            for (int j = 0; j < numberOfTeams; j++) {
                gameMatrix[i][j] = in.readInt();
            }
        }
    }

    /**
     * Helper method for testing.
     */
    private void printContents() {
        System.out.println("Teams: " + teams);
        System.out.print("wins: ");
        for (int n : wins)
            System.out.print(n + " ");
        System.out.println();
        System.out.print("losses: ");
        for (int n : losses)
            System.out.print(n + " ");
        System.out.println();
        System.out.print("remaining: ");
        for (int n : remaining)
            System.out.print(n + " ");
        System.out.println();
        System.out.println("gameMatrix: ");
        for (int i = 0; i < numberOfTeams; i++) {
            for (int j = 0; j < numberOfTeams; j++) {
                System.out.print(gameMatrix[i][j] + "  ");
            }
            System.out.println();
        }
    }

    // number of teams
    public int numberOfTeams() {
        return numberOfTeams;
    }

    // all teams
    public Iterable<String> teams() {
        return teams;
    }

    // number of wins for given team
    public int wins(String team) {
       if (!teams.contains(team)) throw  new IllegalArgumentException("Incorrect argument");
       return wins[teams.indexOf(team)];
    }

    // number of losses for given team
    public int losses(String team)   {
        if (!teams.contains(team)) throw  new IllegalArgumentException("Incorrect argument");
        return losses[teams.indexOf(team)];
    }

    // number of remaining games for given team
    public int remaining(String team) {
        if (!teams.contains(team)) throw  new IllegalArgumentException("Incorrect argument");
        return remaining[teams.indexOf(team)];
    }

    // number of remaining games between team1 and team2
    public int against(String team1, String team2) {
        if (!teams.contains(team1) || !teams.contains(team2)) throw  new IllegalArgumentException("Incorrect argument");
        return gameMatrix[teams.indexOf(team1)][teams.indexOf(team2)];
    }

    // is given team eliminated?
    public boolean isEliminated(String team) {
        if (!teams.contains(team)) throw  new IllegalArgumentException("Incorrect argument");
        // if subset R of teams that eliminates given team is empty -> is not eliminated.
        return certificateOfElimination(team).iterator().hasNext();
    }

    // subset R of teams that eliminates given team; null if not eliminated
    public Iterable<String> certificateOfElimination(String team) {
        if (!teams.contains(team)) throw  new IllegalArgumentException("Incorrect argument");

        // perform the trivial elimination first
        List<String> trivialElimination = trivialElimination(team);
        if (trivialElimination != null) return trivialElimination;

        // if a team is not trivially eliminated, perform nontrivial elimination.
        //  solve a maxflow problem, build a flow network.
        int teamId = teams.indexOf(team);
        int gameVertices = ( (numberOfTeams - 1) * (numberOfTeams - 2) ) / 2;
        // team vertices + s + t + game vertices
        int vertices = numberOfTeams + gameVertices + 2;   //getNumberOfVertices(teamId);
        int s = vertices - 2;
        int t = vertices - 1;
        FlowNetwork flowNetwork = constructNetwork(vertices, s, t, teamId);

        FordFulkerson fordFulkerson = new FordFulkerson(flowNetwork, s, t);
        List<String> eliminationTeamsSubset = new ArrayList<>();
        for (int i = 0; i < numberOfTeams; i++) {
            if (fordFulkerson.inCut(i)) {
                eliminationTeamsSubset.add(teams.get(i));
            }
        }
        return eliminationTeamsSubset;
    }

    /**
     * If the maximum number of games team x can win is less than the number of wins of some other team i,
     * then team x is trivially eliminated.
     * That is, if w[x] + r[x] < w[i], then team x is mathematically eliminated.
     * @param team name of the team
     * @return ArrayList containing the name of the team eliminator or null if the team can still possibly
     * take the 1st place.
     */
    private List<String> trivialElimination(String team) {
        int teamIndex = teams.indexOf(team);
        int maxPossiblePoints = wins[teamIndex] + remaining[teamIndex];
        for (int i = 0; i < numberOfTeams; i++) {
            if (maxPossiblePoints < wins[i]) {
                List<String> teamEliminator = new ArrayList<>();
                teamEliminator.add(teams.get(i));
                return teamEliminator;
            }
        }
        return null;
    }

    private FlowNetwork constructNetwork(int vertices, int s, int t, int teamId) {
        FlowNetwork flowNetwork = new FlowNetwork(vertices);
        // create connections
        // s to games. games to teams.
        for (int i = 0, gameVertex = numberOfTeams; i < numberOfTeams; i++) {
            if (i == teamId) {
                continue;
            }
            for (int j = i + 1; j < numberOfTeams; j++) {
                if (j == teamId) {
                    continue;
                }
                flowNetwork.addEdge(new FlowEdge(s, gameVertex, gameMatrix[i][j]));
                flowNetwork.addEdge(new FlowEdge(gameVertex, i, gameMatrix[i][j]));
                flowNetwork.addEdge(new FlowEdge(gameVertex, j, gameMatrix[i][j]));
                gameVertex++;
            }
        }
        //teams to t
        for (int i = 0; i < numberOfTeams; i++) {
            if (i != teamId) {
                flowNetwork.addEdge(new FlowEdge(i, t, wins[teamId] + remaining[teamId] - wins[i]));
            }
        }
        return flowNetwork;
    }

    // TESTING. Uncomment to see the execution.
    public static void main(String[] args) {

        // pass teams4.txt as an argument. args[0] == teams4.txt
        System.out.println("Testing correctness of methods using teams4.txt: ");
        BaseballElimination be = new BaseballElimination(args[0]);
        be.printContents();
        System.out.println("be.wins(\"New_York\"): " + be.wins("New_York"));
        System.out.println("be.against(\"Montreal\", \"Atlanta\"): " + be.against("Montreal", "Atlanta"));
        System.out.println("trivialElimination('Montreal'): " +  be.trivialElimination("Montreal")
                + "; Expected: Atlanta");
        System.out.println("trivialElimination('New_York'): " +  be.trivialElimination("New_York")
                + "; Expected: null");

        System.out.println();

        System.out.println("Assignment defined output format: ");
        BaseballElimination division = new BaseballElimination(args[0]);
        for (String team : division.teams()) {
            if (division.isEliminated(team)) {
                StdOut.print(team + " is eliminated by the subset R = { ");
                for (String t : division.certificateOfElimination(team)) {
                    StdOut.print(t + " ");
                }
                StdOut.println("}");
            }
            else {
                StdOut.println(team + " is not eliminated");
            }
        }

    }
}
