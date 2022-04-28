package com.company;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class Genetic {
    public static final int numCities = 50;
    public static final int matingPoolSize = 2000;
    public static final Random rand = new Random();
    public static final Point[] cities = new Point[numCities];
    public static final int[][] distances = new int[numCities][numCities];

    public static final int width = 1600;
    public static final int height = 900;

    /**
     * Probability of mutation
     */
    public static final double mutationProbability = 0.3;

    /**
     * Number that survives each round
     */
    public static final int survival = 1100;

    /**
     * The number of organisms that compete in the tournament
     */
    public static final int numTournament = 7;

    /**
     * Used multiple times as the visited array
     */
    private static final boolean[] visited = new boolean[numCities];
    /**
     * Stores position of each city. Used in cycleCrossover
     */
    private final static int[] locations = new int[numCities];

    private static final int[][] matingPool;
    private static final int[] fitnessSum = new int[survival + 1];
    private static final long[] weights = new long[survival + 1];

    private static int currentGen = 0;

    static {
        randomCities();
        calculateDistances();

        //Pick random tours to make up mating pool
        matingPool = KRandomTours(matingPoolSize);
        Arrays.sort(matingPool, Comparator.comparingInt(Genetic::tourFitness));
    }

    public static void nextGen() {
        currentGen++;
        for (int j = 0; j < survival; j++) {
            fitnessSum[j + 1] = fitnessSum[j] + tourFitness(matingPool[j]);
        }
        int sum = fitnessSum[survival];
        for (int j = 0; j < survival; j++) {
            weights[j + 1] = weights[j] + sum - fitnessSum[j];
        }

        for (int j = 0; j < matingPoolSize - survival; j++) {
            // choose the best performer randomly
            int[] parent1 = matingPool[rouletteSelect(weights)];
            int[] parent2 = matingPool[rouletteSelect(weights)];
            orderCrossover(parent1, parent2, matingPool[survival + j]);
            // by chance, mutate
            if (rand.nextDouble() <= mutationProbability) mutate(matingPool[survival + j]);
        }
        Arrays.sort(matingPool, Comparator.comparingInt(Genetic::tourFitness));

    }

    public static int getGeneration() {
        return currentGen;
    }

    public static int[] getBest() {
        return matingPool[0];
    }

    public static int[][] KRandomTours(int K) {
        int[][] result = new int[K][];
        for (int i = 0; i < K; i++) {
            result[i] = randomTour();
        }
        return result;
    }

    public static int tourFitness(int[] tour) {
        int fitness = 0;
        for (int i = 0; i < numCities - 1; i++) {
            int cityA = tour[i];
            int cityB = tour[i + 1];
            fitness += distances[cityA][cityB];
        }
        // add distance from last city to first city
        fitness += distances[tour[numCities - 1]][tour[0]];
        return fitness;
    }

    public static String getTour(int[] tour) {
        // find index of first x
        int idx = 0;
        while (tour[idx] != 0) {
            idx++;
        }
        String lookup = "XABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder result = new StringBuilder();
        for (int i = idx; i < numCities; i++) {
            result.append(lookup.charAt(tour[i]));
        }
        for (int i = 0; i <= idx; i++) {
            result.append(lookup.charAt(tour[i]));
        }
        return result.toString();
    }

    /**
     * Selects using the roulette method, where organisms with lower fitness values are more likely to be selected
     *
     * @param weights weights for each organism
     * @return the chosen one
     */
    public static int rouletteSelect(long[] weights) {
        long sum = weights[survival];
        long chosen = rand.nextLong(sum + 1);
        // binary search
        int high = survival - 1;
        int low = 0;
        while (low < high) {
            int mid = (low + high + 1) / 2;
            if (chosen >= weights[mid]) {
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
        int start = rand.nextInt(numCities);
        int end = rand.nextInt(numCities);
        if (end < start) {
            // swap
            int temp = end;
            end = start;
            start = temp;
        }
        Arrays.fill(visited, false);
        for (int i = start; i <= end; i++) {
            int num = parent1[i];
            visited[num] = true;
            offspring[i] = num;
        }
        // keep track of current index in parent 2
        int idx = (end + 1) % numCities;
        for (int i = end + 1; i < numCities; i++, idx = (idx + 1) % numCities) {
            while (visited[parent2[idx]]) idx = (idx + 1) % numCities;
            offspring[i] = parent2[idx];
        }
        for (int i = 0; i < start; i++, idx = (idx + 1) % numCities) {
            while (visited[parent2[idx]]) idx = (idx + 1) % numCities;
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
        int start = rand.nextInt(numCities);
        int end = rand.nextInt(numCities);
        if (end < start) {
            // swap
            int temp = end;
            end = start;
            start = temp;
        }
        for (; start < end; start++, end--) {
            int temp = offspring[start];
            offspring[start] = offspring[end];
            offspring[end] = temp;
        }
    }

    public static int[] randomTour() {
        int[] result = new int[numCities];
        // fill with random numbers
        for (int i = 0; i < numCities; i++) {
            result[i] = i;
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
        int start = rand.nextInt(numCities);
        int end = rand.nextInt(numCities);
        if (end < start) {
            // swap
            int temp = end;
            end = start;
            start = temp;
        }
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
        for (int i = end + 1; i < numCities; i++, idx++) {
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
        for (int i = 0; i < numCities; i++) {
            locations[parent1[i]] = i;
        }
        Arrays.fill(visited, false);
        int curr = 0;
        do {
            visited[curr] = true;
            offspring[curr] = parent1[curr];
            curr = locations[parent2[curr]];
        } while (!visited[curr]);
        for (int i = 0; i < numCities; i++) {
            if (!visited[i]) offspring[i] = parent2[i];
        }
    }

    public static void randomCities(){
        Random rand = new Random();
        for (int i = 0; i < numCities; i++) {
            cities[i] = new Point(rand.nextInt(width - 40) + 20, rand.nextInt(height - 40) + 20);
        }
    }

    public static void calculateDistances(){
        for (int i = 0; i < numCities; i++) {
            Point p1 = cities[i];
            for (int j = 0; j < numCities; j++) {
                Point p2 = cities[j];
                distances[i][j] = (int) Math.sqrt(Math.pow(p1.x-p2.x, 2) + Math.pow(p1.y-p2.y, 2));
            }
        }
    }
}