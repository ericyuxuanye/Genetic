package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Solve using DP, so we get an idea of what the optimal solution should be
 */
public class DPApproach {
    private static final int numCities = 21;
    private static final int[][] distances = new int[numCities][numCities];

    /**
     * To store the state for the dynamic programming algorithm
     */
    private static final int[][] state = new int[numCities][1<<numCities];
    /**
     * To store the previous step for each state
     */
    private static final int[][] solution = new int[numCities][1<<numCities];

    public static void main(String[] args) throws IOException {
        // read in input
        try (BufferedReader br = new BufferedReader(new InputStreamReader(Objects.requireNonNull(Genetic.class.getResourceAsStream("/Data.txt"))))) {
            for (int i = 0; i < numCities; i++) {
                String line = br.readLine();
                StringTokenizer st = new StringTokenizer(line);
                for (int j = 0; j < numCities; j++) {
                    distances[i][j] = Integer.parseInt(st.nextToken());
                }
            }
        }
        // fill state with max value, so that any solution will be better and overwrite it
        for (int i = 0; i < numCities; i++) {
            Arrays.fill(state[i], Integer.MAX_VALUE);
        }
        // fill solution to -1, so that when program sees -1, we know to stop
        for (int i = 0; i < numCities; i++) {
            Arrays.fill(solution[i], -1);
        }
        int distance = solve(0, 1);
        System.out.println("Solution: " + getTour());
        System.out.println("Distance: " + distance);
    }

    /**
     * Solves traveling salesman problem in O(n²2ⁿ) using dynamic programming
     * <p>
     * The visited parameter is represented using a bitmask (for speed), starting from the right.
     * For example, the number 1010 (6 in decimal) means that the second and fourth city are visited
     * @param pos current city
     * @param visited for every bit, 1 if visited that city, 0 if not
     * @return minimal cost for solution
     */
    public static int solve(int pos, int visited) {
        // if we have visited the last city, return distance to first city
        if (visited == (1<<numCities) - 1) return distances[pos][0];
        // if we have previously solved for this state, return the result
        if (state[pos][visited] != Integer.MAX_VALUE) return state[pos][visited];

        int ans = Integer.MAX_VALUE;
        int bestSolution = 0;
        for (int i = 0; i < numCities; i++) {
            // next city cannot be current city, or an already visited city
            if (i == pos || (visited & (1<<i)) != 0) continue;
            // try visiting this city next and evaluate cost
            int distance = distances[pos][i] + solve(i, visited | (1<<i));
            // if distance is the current best, write that in solution
            if (distance < ans) {
                ans = distance;
                bestSolution = i;
            }
        }
        state[pos][visited] = ans;
        solution[pos][visited] = bestSolution;
        return ans;
    }

    public static String getTour() {
        String lookup = "XABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder result = new StringBuilder();
        int curr = 0;
        int visited = 1;
        do {
            result.append(lookup.charAt(curr));
            curr = solution[curr][visited];
            visited = visited | (1 << curr);
        } while (curr != -1);
        result.append('X');
        return result.toString();
    }
}
