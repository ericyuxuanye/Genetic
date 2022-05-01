package com.company;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Genetic {
    public static int numCities = 21;
    public static boolean isCaseStudy = true;
    public static int matingPoolSize = 2000;

    public static final Random rand = new Random();
    public static Point[] cities;
    public static int[][] distances;

    public static final int width = 1200;
    public static final int height = 800;


    private static final int NUM_CORES = Runtime.getRuntime().availableProcessors();

    /**
     * Our thread pool that provides as many threads as there are cpu cores
     */
    private static final ExecutorService exc = Executors.newFixedThreadPool(NUM_CORES);

    /**
     * Probability of mutation
     */
    public static double mutationProbability = 0.3;

    /**
     * Number that survives each round
     */
    public static int survival = 1100;

    /**
     * The number of organisms that compete in the tournament
     */
    public static int numTournament = 7;

    /**
     * Stores position of each city. Used in cycleCrossover
     */
    private static final int[][] locations = new int[NUM_CORES][];
    private static final boolean[][] visited = new boolean[NUM_CORES][];

    private static class Organism {
        int[] path;
        int fitness;
    }
    private static Organism[] matingPool;
    private static int[] fitnessSum;
    private static long[] weights;

    private static int currentGen = 0;

    public static final int PARTIALLY_MAPPED = 0;
    public static final int CYCLE = 1;
    public static final int ORDER = 2;

    public static void clear() {
        cities = new Point[0];
        numCities = 0;
    }

    public static void addPoint(int x, int y) {
        isCaseStudy = false;
        Point[] temp = new Point[numCities + 1];
        System.arraycopy(cities, 0, temp, 0, numCities);
        temp[numCities] = new Point(x, y);
        numCities++;
        cities = temp;
        Main.updateNumCities();
    }

    /**
     * Deletes points within the specified rectangle
     * @param x1 the x coordinate of the top left corner of rectangle
     * @param y1 the y coordinate of the top left corner of rectangle
     * @param x2 the x coordinate of the bottom right corner of rectangle
     * @param y2 the y coordinate of the bottom right corner of rectangle
     */
    public static void deletePoints(int x1, int y1, int x2, int y2) {
        isCaseStudy = false;
        cities = Arrays.stream(cities)
                .filter((Point p) -> p.x < x1 || p.x > x2 || p.y < y1 || p.y > y2)
                .toArray(Point[]::new);
        numCities = cities.length;
        Main.updateNumCities();
    }

    public static void generateCities() {
        cities = new Point[numCities];
        if (isCaseStudy) {
            caseStudyCities();
        } else {
            randomCities();
        }
    }

    public static void resetGenerationCount() {
        currentGen = 0;
    }

    public static void reset() {
        distances = new int[numCities][numCities];
        if (isCaseStudy) {
            caseStudyDistances();
        } else {
            calculateDistances();
        }
        for (int i = 0; i < NUM_CORES; i++) {
            locations[i] = new int[numCities];
            visited[i] = new boolean[numCities];
        }
        matingPool = new Organism[matingPoolSize];
        fitnessSum = new int[survival + 1];
        weights = new long[survival + 1];
        //Pick random tours to make up mating pool
        matingPool = KRandomTours(matingPoolSize);
        Arrays.sort(matingPool, Comparator.comparingInt(x -> x.fitness));
    }

    public static void nextGen(boolean rouletteSelection, int crossoverAlgorithm, boolean reverseMutation) {
        currentGen++;
        if (rouletteSelection) {
            for (int j = 0; j < survival; j++) {
                fitnessSum[j + 1] = fitnessSum[j] + matingPool[j].fitness;
            }
            int sum = fitnessSum[survival];
            for (int j = 0; j < survival; j++) {
                weights[j + 1] = weights[j] + sum - fitnessSum[j];
            }
        }
        divideTasks((j, core) -> {
            // choose the best performer randomly
            int[] parent1, parent2;
            if (rouletteSelection) {
                parent1 = matingPool[rouletteSelect(weights)].path;
                parent2 = matingPool[rouletteSelect(weights)].path;
            } else {
                parent1 = matingPool[tournamentSelect()].path;
                parent2 = matingPool[tournamentSelect()].path;
            }
            switch (crossoverAlgorithm) {
                case PARTIALLY_MAPPED -> pmCrossover(parent1, parent2, matingPool[survival + j].path, core);
                case CYCLE -> cycleCrossover(parent1, parent2, matingPool[survival + j].path, core);
                case ORDER -> orderCrossover(parent1, parent2, matingPool[survival + j].path, core);
                default -> throw new IllegalStateException("Unexpected value: " + crossoverAlgorithm);
            }
            if (rand.nextDouble() < mutationProbability) {
                if (reverseMutation) reverseMutation(matingPool[survival + j].path);
                else twoPointMutation(matingPool[survival + j].path);
            }
            matingPool[survival + j].fitness = tourFitness(matingPool[survival + j].path);
        }, matingPoolSize - survival);
        Arrays.sort(matingPool, Comparator.comparingInt(o -> o.fitness));
        // radix sort would be faster if we had many more elements
        // radixSort(maxFitness);
    }

    public interface IntBinaryConsumer {
        void accept(int a, int b);
    }

    /**
     * Runs a task in parallel with arguments from 0 to bound - 1
     * @param f runs some task, accepting an index and the cpu core it is run on
     * @param bound number of times to loop
     */
    public static void divideTasks(IntBinaryConsumer f, int bound) {
        CountDownLatch latch = new CountDownLatch(NUM_CORES);
        final int taskSize = bound / NUM_CORES;
        // last core does a tiny bit more work if it isn't evenly divisible
        exc.execute(() -> {
            for (int i = (NUM_CORES - 1) * taskSize; i < bound; i++) {
                f.accept(i, NUM_CORES - 1);
            }
            latch.countDown();
        });
        // all but last core
        for (int i = 0; i < NUM_CORES - 1; i++) {
            int finalI = i;
            exc.execute(() -> {
                int start = finalI * taskSize;
                for (int j = start; j < start + taskSize; j++) {
                    f.accept(j, finalI);
                }
                latch.countDown();
            });
        }
        try {
            // wait for threads to finish
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    // stuff used for radix sort

    private static final int[] countArray = new int[256];
    private static Organism[] aux = new Organism[matingPoolSize];

    private static Organism[] array = new Organism[matingPoolSize];

    /**
     * Uses radix sort to sort the int array. It uses a bucket of size 256 because that is 1 byte
     * <p>
     * NOTE: INTEGERS ARE TREATED AS IF THEY ARE UNSIGNED!!!
     * @param maxOrganism maximum fitness of any organism
     */
    public static void radixSort(int maxOrganism) {
        System.arraycopy(matingPool, 0, array, 0, matingPoolSize);
        // calculate number of times we need to iterate
        int it = 0;
        for (int shift = 0; maxOrganism >= (1<<shift) && shift < 32; shift += 8) {
            it++;
        }
        for (int shift = 0; it > 0; shift += 8, it--) {
            Arrays.fill(countArray, 0);
            for (Organism o : array) {
                int val = o.fitness;
                // grab current value in base 256 (0-256)
                countArray[val >>> shift & 0xff]++;
            }
            // sum up count array
            for (int j = 1; j < 256; j++) {
                countArray[j] += countArray[j - 1];
            }
            for (int j = matingPoolSize - 1; j >= 0; j--) {
                // set aux of the count of the current element in base 256 to current element,
                // and decrement the count array.
                aux[--countArray[array[j].fitness >>> shift & 0xff]] = array[j];
            }
            // swap arrays
            Organism[] temp = aux;
            aux = array;
            array = temp;
        }
        matingPool = array;
    }

    public static int getGeneration() {
        return currentGen;
    }

    public static int[] getBest() {
        return matingPool[0].path;
    }

    public static int getBestFitness() {
        return matingPool[0].fitness;
    }

    public static Organism[] KRandomTours(int K) {
        Organism[] organisms = new Organism[K];
        for (int i = 0; i < K; i++) {
            Organism o = new Organism();
            o.path = randomTour();
            o.fitness = tourFitness(o.path);
            organisms[i] = o;
        }
        return organisms;
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
    public static void orderCrossover(int[] parent1, int[] parent2, int[] offspring, int core) {
        int start = rand.nextInt(numCities);
        int end = rand.nextInt(numCities);
        if (end < start) {
            // swap
            int temp = end;
            end = start;
            start = temp;
        }
        boolean[] vis = visited[core];
        Arrays.fill(vis, false);
        for (int i = start; i <= end; i++) {
            int num = parent1[i];
            vis[num] = true;
            offspring[i] = num;
        }
        // keep track of current index in parent 2
        int idx = (end + 1) % numCities;
        for (int i = end + 1; i < numCities; i++, idx = (idx + 1) % numCities) {
            while (vis[parent2[idx]]) idx = (idx + 1) % numCities;
            offspring[i] = parent2[idx];
        }
        for (int i = 0; i < start; i++, idx = (idx + 1) % numCities) {
            while (vis[parent2[idx]]) idx = (idx + 1) % numCities;
            offspring[i] = parent2[idx];
        }
    }

    /**
     * Reverses a random section of the list
     *
     * @param offspring the offspring array to mutate
     */
    public static void reverseMutation(int[] offspring) {
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

    /**
     * Selects two points on the list and swaps them
     * @param offspring the array to mutate
     */
    public static void twoPointMutation(int[] offspring) {
        int point1 = rand.nextInt(numCities);
        int point2 = rand.nextInt(numCities);
        // swap
        int temp = offspring[point1];
        offspring[point1] = offspring[point2];
        offspring[point2] = temp;
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
     * @return the selected parent
     */
    public static int tournamentSelect() {
        int best = Integer.MAX_VALUE;
        int result = -1;
        for (int i = 0; i < numTournament; i++) {
            int curr = matingPool[rand.nextInt(matingPoolSize)].fitness;
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
    public static void pmCrossover(int[] parent1, int[] parent2, int[] offspring, int core) {
        int start = rand.nextInt(numCities);
        int end = rand.nextInt(numCities);
        if (end < start) {
            // swap
            int temp = end;
            end = start;
            start = temp;
        }
        boolean[] vis = visited[core];
        Arrays.fill(vis, false);
        for (int i = start; i <= end; i++) {
            int num = parent1[i];
            vis[num] = true;
            offspring[i] = num;
        }
        // keep track of current index in parent 2
        int idx = 0;
        for (int i = 0; i < start; i++, idx++) {
            while (vis[parent2[idx]]) idx++;
            offspring[i] = parent2[idx];
        }
        for (int i = end + 1; i < numCities; i++, idx++) {
            while (vis[parent2[idx]]) idx++;
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
    public static void cycleCrossover(int[] parent1, int[] parent2, int[] offspring, int core) {
        // store the location of each position for faster lookup later
        int[] loc = locations[core];
        for (int i = 0; i < numCities; i++) {
            loc[parent1[i]] = i;
        }
        boolean[] visited = new boolean[numCities];
        int curr = 0;
        do {
            visited[curr] = true;
            offspring[curr] = parent1[curr];
            curr = loc[parent2[curr]];
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
                int x = p2.x - p1.x;
                int y = p2.y - p1.y;
                distances[i][j] = (int)Math.round(Math.sqrt(x * x + y * y));
            }
        }
    }

    public static void caseStudyCities() {
        // read in cities
        try (BufferedReader br = new BufferedReader(new InputStreamReader(Objects.requireNonNull(Genetic.class.getResourceAsStream("/Points.txt"))))) {
            for (int i = 0; i < numCities; i++) {
                StringTokenizer st = new StringTokenizer(br.readLine());
                int x = Integer.parseInt(st.nextToken()) * width / 490;
                int y = Integer.parseInt(st.nextToken()) * height / 334;
                cities[i] = new Point(x, y);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void caseStudyDistances() {
        // read in distances
        try (BufferedReader br = new BufferedReader(new InputStreamReader(Objects.requireNonNull(Genetic.class.getResourceAsStream("/Data.txt"))))) {
            for (int i = 0; i < numCities; i++) {
                StringTokenizer st = new StringTokenizer(br.readLine());
                for (int j = 0; j < numCities; j++) {
                    distances[i][j] = Integer.parseInt(st.nextToken());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
