package com.company;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class Visualizer extends JPanel {

    private static final int width = 490;
    private static final int height = 334;
    private final Point[] cities = new Point[Genetic.numCities];

    public Visualizer() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(Objects.requireNonNull(Genetic.class.getResourceAsStream("/Points.txt"))))) {
            for (int i = 0; i < Genetic.numCities; i++) {
                StringTokenizer st = new StringTokenizer(br.readLine());
                cities[i] = new Point(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        setPreferredSize(new Dimension(width, height));
    }

    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawBestPath(g, Genetic.getBest());

        g.setColor(Color.BLACK);
        String lookup = "XABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for (int i = 0; i < Genetic.numCities; i++) {
            Point p = cities[i];
            g.fillOval(p.x-6, p.y-6, 13, 13);
            g.drawString(String.valueOf(lookup.charAt(i)), p.x + 10, p.y + 10);
        }
        g.drawString("Generation: " + Genetic.getGeneration(), 2, 12);
        g.drawString("Fitness: " + Genetic.tourFitness(Genetic.getBest()), 2, 25);
    }

    private void drawBestPath(Graphics g, int[] bestPath){
        g.setColor(Color.RED);
        for (int i = 1; i < Genetic.numCities; i++) {
            Point prev = cities[bestPath[i - 1]];
            Point current = cities[bestPath[i]];
            g.drawLine(prev.x, prev.y, current.x, current.y);
        }

        Point first = cities[bestPath[0]];
        Point last = cities[bestPath[Genetic.numCities - 1]];
        g.drawLine(last.x, last.y, first.x, first.y);
    }



}
