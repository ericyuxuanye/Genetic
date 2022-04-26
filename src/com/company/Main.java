package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class Main {
    static int numCities = 21;
    static int matingPoolSize = 50;
    static int generations = 1000;
    static final Random rand = new Random();
    static final int[][] distances = new int[numCities][numCities];

    /**
     * number of mutations per copy
     */
    static final int numMutations = 2;

    /**
     * Number to delete and add each round
     */
    static final int modifySize = generations / 2;

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
        int[][] matingPool = KrandomTours(matingPoolSize);
        int[][] temp = new int[matingPoolSize][numCities - 1];
        int[][] bestPerformers = new int[modifySize][];
        Arrays.sort(matingPool, Comparator.comparingInt(Main::tourFitness));
        for (int i = 0; i < generations; i++) {
            // print performance of generation
            int[] best = matingPool[0];
            System.out.println("\nGeneration " + (i + 1) + ":");
            System.out.println("Fitness: " + tourFitness(best));
            System.out.println("Tour: " + getTour(best));
            // crossover 4 best performers
            System.arraycopy(matingPool, 0, bestPerformers, 0, modifySize);
            System.arraycopy(matingPool, 0, temp, 0, matingPoolSize - modifySize);
            for (int j = 0; j < modifySize; j++) {
                // choose the best performer randomly
                int[] parent1 = bestPerformers[rand.nextInt(modifySize)];
                int[] parent2 = bestPerformers[rand.nextInt(modifySize)];
                temp[matingPoolSize - modifySize + j] = crossOver(parent1, parent2);
            }
            // swap temp and matingPool
            int[][] temp2 = temp;
            temp = matingPool;
            matingPool = temp2;
            Arrays.sort(matingPool, Comparator.comparingInt(Main::tourFitness));
        }
        int[] result = matingPool[0];
        System.out.println("\nFinal Fitness: " + tourFitness(result));
        System.out.println("Final Tour: " + getTour(result));
    }

    public static void populateRandom(int[][] arr) {
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[0].length; j++) {
                arr[i][j] = (int) (Math.random() * 10);
            }
        }
    }

    /**
     * Fisher Yates random shuffling algorithm
     * @param array input array that will be overwritten
     */
    public static void shuffle(int[] array) {
        for (int i = array.length - 1; i > 0; i--) {
            // random number from 0 (inclusive) to i (exclusive)
            int randIndex = rand.nextInt(i);
            // swap current with item at randIndex
            int temp = array[i];
            array[i] = array[randIndex];
            array[randIndex] = temp;
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

    public static int[][] KrandomTours(int K) {
        int[][] result = new int[K][numCities - 1];
        for (int i = 0; i < K; i++) {
            result[i] = randomTour();
        }
        return result;
    }
    public static String getTour(int[] tour) {
        StringBuilder result = new StringBuilder("X");
        for (int i : tour) {
            result.append((char)(i - 1 + 'A'));
        }
        result.append('X');
        return result.toString();
    }

    /**
     * Implements a partially mapped crossover
     * @param parent1 the first parent
     * @param parent2 the second parent
     * @return a new organism
     */
    public static int[] crossOver(int[] parent1, int[] parent2) {
        int[] result = new int[numCities - 1];
        int start = rand.nextInt(numCities - 1);
        int end = rand.nextInt(start, numCities - 1);
        boolean[] usedNumbers = new boolean[numCities];
        for (int i = start; i <= end; i++) {
            int num = parent1[i];
            usedNumbers[num] = true;
            result[i] = num;
        }
        // keep track of current index in parent 2
        int idx = 0;
        for (int i = 0; i < start; i++, idx++) {
            while (usedNumbers[parent2[idx]]) idx++;
            result[i] = parent2[idx];
        }
        for (int i = end + 1; i < numCities - 1; i++, idx++) {
            while (usedNumbers[parent2[idx]]) idx++;
            result[i] = parent2[idx];
        }
        // introduce some mutation
        for (int i = 0; i < numMutations; i++) {
            int idx1 = rand.nextInt(numCities - 1);
            int idx2 = rand.nextInt(numCities - 1);
            int temp = result[idx1];
            result[idx1] = result[idx2];
            result[idx2] = temp;
        }
        return result;
    }
}