package com.company;
import java.util.*;
public class Main {
    static int numCities = 4;
    static int matingPoolSize = 4;
    public static void main(String[] args) {
        int[][] distance = new int[numCities][numCities];
        populateRandom(distance);
        System.out.println("Random distances: " +
                Arrays.deepToString(distance));
//method to pick random tour
        int[] randomTour = randomTour(numCities);
        System.out.println("Random Tour: " + Arrays.toString(randomTour));
//method to evaluate fitness of a tour
        System.out.println("Tour Fitness: " + tourFitness(distance,
                randomTour));
//Pick random tours to make up mating pool
        int[][] matingPool = KrandomTours(numCities, matingPoolSize);
    }
    public static void populateRandom(int[][] arr){
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[0].length; j++) {
                arr[i][j] = (int) (Math.random()*10);
            }
        }
    }
    public static int[] randomTour(int numCities){
        ArrayList<Integer> randomTourInList = randomTour(numCities, new
                ArrayList<>());
        int[] ret = new int[numCities];
        for (int i = 0; i < numCities; i++) {
            ret[i] = randomTourInList.get(i);
        }
        return ret;
    }
    public static ArrayList<Integer> randomTour(int numCities,
                                                ArrayList<Integer> currTour){
        ArrayList<Integer> citiesNotInTour = new ArrayList<>();
        for (int i = 0; i < numCities; i++) {
            if(!currTour.contains(i)) citiesNotInTour.add(i);
        }
        if(citiesNotInTour.size()==0){
            return currTour;
        }
        else{
            int randIndex = (int)
                    (Math.random()*(citiesNotInTour.size()));
            int cityToAddToTour = citiesNotInTour.get(randIndex);
            currTour.add(cityToAddToTour);
            randomTour(numCities, currTour);
        }
        return currTour; //never reached
    }
    public static int tourFitness(int[][] distance, int[] tour){
        int fitness = 0;
        for (int i = 0; i < tour.length-1; i++) {
            int cityA = tour[i];
            int cityB = tour[i+1];
            fitness += distance[cityA][cityB];
        }
        return fitness;
    }
    public static int[][] KrandomTours(int numCities, int K){
        int[][] ret = new int[K][numCities];
        HashSet<int[]> setOfRandomTours = new HashSet<>();
        while (setOfRandomTours.size() < K){
            int[] newRandomTour = randomTour(numCities);
//check if newRandomTour is in setOfRandomTours
            boolean newRandomTourInSet = false;
            for(int[] randomTour : setOfRandomTours){
                if(Arrays.equals(newRandomTour, randomTour))
                    newRandomTourInSet=true;
            }
//if newRandomTour not in setOfRandomTours, add it
            if(newRandomTourInSet == false)
                setOfRandomTours.add(newRandomTour);
            System.out.println();
        }
//transfer set to array
        int ind=0;
        for(int[] tour : setOfRandomTours){
            ret[ind] = tour;
            ind++;
        }
        return ret;
    }
}