package com.company;

import javax.swing.*;
import java.awt.*;

public class Visualizer extends JPanel {

    private int width = Main.width;
    private int height = Main.height;
    private Point[] cities;

    public Visualizer() {
        cities = Main.cities;
        setPreferredSize(new Dimension(width, height));
    }

    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawBestPath(g, Main.getBest());

        g.setColor(Color.BLACK);
        String lookup = "XABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for (int i = 0; i < Main.numCities; i++) {
            Point p = cities[i];
            g.fillOval(p.x-6, p.y-6, 13, 13);
            g.drawString(String.valueOf(lookup.charAt(i)), p.x + 10, p.y + 10);
        }

        g.drawString("Best Fitness: " + Main.tourFitness(Main.getBest()), 2, height - 2);
        g.drawString("Generation: " + Main.currentGen, 2, height - 14);
    }

    private void drawBestPath(Graphics g, int[] bestPath){
        g.setColor(Color.RED);
        Point prev = cities[0];
        Point current = cities[bestPath[0]];
        g.drawLine(prev.x, prev.y, current.x, current.y);

        for (int i = 1; i < Main.numCities - 1; i++) {
            prev = cities[bestPath[i - 1]];
            current = cities[bestPath[i]];
            g.drawLine(prev.x, prev.y, current.x, current.y);
        }

        Point first = cities[0];
        Point last = cities[bestPath[Main.numCities - 2]];
        g.drawLine(last.x, last.y, first.x, first.y);
    }



}
