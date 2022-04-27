package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class Main {
    public static final int numCities = 21;
    public static final int matingPoolSize = 120;
    public static final int generations = 100;
    public static final Random rand = new Random();
    public static final int[][] distances = new int[numCities][numCities];

    /**
     * Probability of mutation
     */
    public static final double mutationProbability = 0.3;

    /**
     * Number that survives each round
     */
    public static final int survival = 60;

    /**
     * The number of organisms that compete in the tournament
     */
    public static final int numTournament = 7;

    /**
     * Used multiple times as the visited array
     */
    static final boolean[] visited = new boolean[numCities];
    /**
     * Stores position of each city. Used in cycleCrossover
     */
    private final static int[] locations = new int[numCities];

    public static void main(String[] args) throws IOException {

        try (BufferedReader br = new BufferedReader(new InputStreamReader(Objects.requireNonNull(Main.class.getResourceAsStream("/Data.txt"))))) {
            for (int i = 0; i < numCities; i++) {
                String line = br.readLine();
                StringTokenizer st = new StringTokenizer(line);
                for (int j = 0; j < numCities; j++) {
                    distances[i][j] = Integer.parseInt(st.nextToken());
                }
            }
        }
        //Pick random tours to make up mating pool
        int[][] matingPool = KRandomTours(matingPoolSize);
        // to store the fitness of the species as a prefix sum
        int[] fitnessSum = new int[survival + 1];
        int[] weights = new int[survival + 1];
        Arrays.sort(matingPool, Comparator.comparingInt(Main::tourFitness));
        for (int i = 0; i < generations; i++) {
            // print performance of generation
            int[] best = matingPool[0];
            System.out.println("\nGeneration " + i + ":");
            System.out.println("Fitness: " + tourFitness(best));
            System.out.println("Tour: " + getTour(best));

            for (int j = 0; j < survival; j++) {
                fitnessSum[j + 1] = fitnessSum[j] + tourFitness(matingPool[j]);
            }

            int sum = fitnessSum[survival];
            for (int j = 0; j < survival; j++) {
                weights[j + 1] = weights[j] + sum - fitnessSum[j];

            }

            for (int j = 0; j < survival; j++) {
                // choose the best performer randomly
                int[] parent1 = matingPool[rouletteSelect(weights)];
                int[] parent2 = matingPool[rouletteSelect(weights)];
                orderCrossover(parent1, parent2, matingPool[matingPoolSize - survival + j]);
                // by chance, mutate
                if (rand.nextDouble() < mutationProbability) mutate(matingPool[matingPoolSize - survival + j]);
            }
            Arrays.sort(matingPool, Comparator.comparingInt(Main::tourFitness));
        }
        int[] result = matingPool[0];
        System.out.println("\nFinal Fitness: " + tourFitness(result));
        System.out.println("Final Tour: " + getTour(result));
    }

    public static int[][] KRandomTours(int K) {
        int[][] result = new int[K][numCities - 1];
        for (int i = 0; i < K; i++) {
            result[i] = randomTour();
        }
        return result;
    }

    public static int tourFitness(int[] tour) {
        int fitness = 0;
        fitness += distances[0][tour[0]];
        for (int i = 0; i < tour.length - 1; i++) {
            int cityA = tour[i];
            int cityB = tour[i + 1];
            fitness += distances[cityA][cityB];
        }
        // add distance from last city to first city
        fitness += distances[tour[tour.length - 1]][0];
        return fitness;
    }

    public static String getTour(int[] tour) {
        StringBuilder result = new StringBuilder("X");
        for (int i : tour) {
            result.append((char) (i - 1 + 'A'));
        }
        result.append('X');
        return result.toString();
    }

    /**
     * Selects using the roulette method, where organisms with lower fitness values are more likely to be selected
     *
     * @param fitnessSum the sum of the fitness values
     * @return the chosen one
     */
    public static int rouletteSelect(int[] fitnessSum) {
        int sum = fitnessSum[survival];
        int chosen = rand.nextInt(sum + 1);
        // binary search
        int high = survival - 1;
        int low = 0;
        while (low < high) {
            int mid = (low + high + 1) / 2;
            if (chosen >= fitnessSum[mid]) {
                low = mid;
            } else {
                high = mid - 1;
            }
        }
        return low;
    }

    /**
     * Implements the order crossover. This preserves the relative order of the remaining cities
     *
     * @param parent1   the first parent
     * @param parent2   the second parent
     * @param offspring the baby to write to
     */
    public static void orderCrossover(int[] parent1, int[] parent2, int[] offspring) {
        int start = rand.nextInt(numCities - 1);
        int end = rand.nextInt(start, numCities - 1);
        Arrays.fill(visited, false);
        for (int i = start; i <= end; i++) {
            int num = parent1[i];
            visited[num] = true;
            offspring[i] = num;
        }
        // keep track of current index in parent 2
        int idx = (end + 1) % (numCities - 1);
        for (int i = end + 1; i < numCities - 1; i++, idx = (idx + 1) % (numCities - 1)) {
            while (visited[parent2[idx]]) idx = (idx + 1) % (numCities - 1);
            offspring[i] = parent2[idx];
        }
        for (int i = 0; i < start; i++, idx = (idx + 1) % (numCities - 1)) {
            while (visited[parent2[idx]]) idx = (idx + 1) % (numCities - 1);
            offspring[i] = parent2[idx];
        }
    }

    /**
     * Reverses a random section of the list
     *
     * @param offspring the offspring array to mutate
     */
    public static void mutate(int[] offspring) {
        // introduce some mutation
        int start = rand.nextInt(numCities - 1);
        int end = rand.nextInt(numCities - 1);
        if (end < start) {
            // swap
            int temp = end;
            end = start;
            start = temp;
        }
        for (int i = 0; i < (end - start) / 2 + 1; i++) {
            int temp = offspring[i + start];
            offspring[i + start] = offspring[end - i];
            offspring[end - i] = temp;
        }
    }

    public static int[] randomTour() {
        int[] result = new int[numCities - 1];
        // fill with random numbers
        for (int i = 0; i < numCities - 1; i++) {
            result[i] = i + 1;
        }
        // shuffle using fisher-yates
        shuffle(result);
        return result;
    }

    /**
     * Fisher Yates random shuffling algorithm
     *
     * @param array input array that will be overwritten
     */
    public static void shuffle(int[] array) {
        for (int i = array.length - 1; i > 0; i--) {
            // random number from 0 (inclusive) to i (inclusive)
            int randIndex = rand.nextInt(i + 1);
            // swap current with item at randIndex
            int temp = array[i];
            array[i] = array[randIndex];
            array[randIndex] = temp;
        }
    }

    /**
     * Randomly selects a number of organisms (chosen by numTournament) and returns the best
     *
     * @param matingPool the mating pool
     * @return the selected parent
     */
    public static int tournamentSelect(int[][] matingPool) {
        int best = Integer.MAX_VALUE;
        int result = -1;
        for (int i = 0; i < numTournament; i++) {
            int curr = tourFitness(matingPool[rand.nextInt(matingPoolSize)]);
            if (curr < best) {
                best = curr;
                result = i;
            }
        }
        return result;
    }

    /**
     * Implements a partially mapped crossover
     *
     * @param parent1   the first parent
     * @param parent2   the second parent
     * @param offspring the baby to write to
     */
    public static void pmCrossover(int[] parent1, int[] parent2, int[] offspring) {
        int start = rand.nextInt(numCities - 1);
        int end = rand.nextInt(start, numCities - 1);
        Arrays.fill(visited, false);
        for (int i = start; i <= end; i++) {
            int num = parent1[i];
            visited[num] = true;
            offspring[i] = num;
        }
        // keep track of current index in parent 2
        int idx = 0;
        for (int i = 0; i < start; i++, idx++) {
            while (visited[parent2[idx]]) idx++;
            offspring[i] = parent2[idx];
        }
        for (int i = end + 1; i < numCities - 1; i++, idx++) {
            while (visited[parent2[idx]]) idx++;
            offspring[i] = parent2[idx];
        }
    }

    /**
     * Implements a cycle crossover. This one makes sure that every city maintains the position it had in one of the
     * cities
     *
     * @param parent1   the first parent
     * @param parent2   the second parent
     * @param offspring the baby to write to
     */
    public static void cycleCrossover(int[] parent1, int[] parent2, int[] offspring) {
        // store the location of each position for faster lookup later
        for (int i = 0; i < numCities - 1; i++) {
            locations[parent1[i]] = i;
        }
        Arrays.fill(visited, false);
        int curr = 0;
        do {
            visited[curr] = true;
            offspring[curr] = parent1[curr];
            curr = locations[parent2[curr]];
        } while (!visited[curr]);
        for (int i = 0; i < numCities - 1; i++) {
            if (!visited[i]) offspring[i] = parent2[i];
        }
    }
}