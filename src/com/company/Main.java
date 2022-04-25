package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class Main {
    static int numCities = 21;
    static int matingPoolSize = 4;
    static final Random rand = new Random();
    public static void main(String[] args) throws IOException {
        int[][] distance = new int[numCities][numCities];
        try (BufferedReader br = new BufferedReader(new InputStreamReader(Objects.requireNonNull(Main.class.getResourceAsStream("/Data.txt"))))) {
            for (int i = 0; i < numCities; i++) {
                String line = br.readLine();
                StringTokenizer st = new StringTokenizer(line);
                for (int j = 0; j < numCities; j++) {
                    distance[i][j] = Integer.parseInt(st.nextToken());
                }
            }
        }
        //method to pick random tour
        int[] randomTour = randomTour(numCities);
        System.out.println("Random Tour: " + getTour(randomTour));
        //method to evaluate fitness of a tour
        System.out.println("Tour Fitness: " + tourFitness(distance,
                randomTour));
        //Pick random tours to make up mating pool
        int[][] matingPool = KrandomTours(numCities, matingPoolSize);
    }

    public static void populateRandom(int[][] arr) {
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[0].length; j++) {
                arr[i][j] = (int) (Math.random() * 10);
            }
        }
    }

    public static int[] randomTour(int numCities) {
        int[] result = new int[numCities];
        // fill with random numbers
        for (int i = 0; i < numCities; i++) {
            result[i] = i;
        }
        // shuffle using fisher-yates
        for (int i = numCities - 1; i > 0; i--) {
            // random number from 0 (inclusive) to i (exclusive)
            int randIndex = rand.nextInt(i);
            // swap current with item at randIndex
            int temp = result[i];
            result[i] = result[randIndex];
            result[randIndex] = temp;
        }
        return result;
    }

    public static int tourFitness(int[][] distance, int[] tour) {
        int fitness = 0;
        for (int i = 0; i < tour.length - 1; i++) {
            int cityA = tour[i];
            int cityB = tour[i + 1];
            fitness += distance[cityA][cityB];
        }
        // add distance from last city to first city
        fitness += distance[tour[tour.length - 1]][tour[0]];
        return fitness;
    }

    public static int[][] KrandomTours(int numCities, int K) {
        int[][] ret = new int[K][numCities];
        HashSet<int[]> setOfRandomTours = new HashSet<>();
        while (setOfRandomTours.size() < K) {
            int[] newRandomTour = randomTour(numCities);
            //check if newRandomTour is in setOfRandomTours
            boolean newRandomTourInSet = false;
            for (int[] randomTour : setOfRandomTours) {
                if (Arrays.equals(newRandomTour, randomTour)) {
                    newRandomTourInSet = true;
                    break;
                }
            }
            //if newRandomTour not in setOfRandomTours, add it
            if (!newRandomTourInSet)
                setOfRandomTours.add(newRandomTour);
            System.out.println();
        }
        //transfer set to array
        int ind = 0;
        for (int[] tour : setOfRandomTours) {
            ret[ind] = tour;
            ind++;
        }
        return ret;
    }
    public static String getTour(int[] tour) {
        String lookup = "XABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder result = new StringBuilder();
        for (int i : tour) {
            result.append(lookup.charAt(i));
        }
        return result.toString();
    }
}