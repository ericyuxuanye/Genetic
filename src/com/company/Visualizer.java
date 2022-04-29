package com.company;

import javax.swing.*;
import java.awt.*;

public class Visualizer extends JPanel {

    private static final int width = Genetic.width;
    private static final int height = Genetic.height;

    private static final String caseStudyLettering = "XABCDEFGHIJKLMNOPQRST";
    private final Point[] cities = Genetic.cities;

    public Visualizer() {
        super();
        setPreferredSize(new Dimension(width, height));
    }

    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawBestPath(g, Genetic.getBest());

        g.setColor(Color.BLACK);
        for (Point p : cities) {
            g.fillOval(p.x-6, p.y-6, 13, 13);
        }
        // draw text on top of points
        g.setColor(Color.GRAY);
        for (int i = 0; i < Genetic.numCities; i++) {
            Point p = cities[i];
            if (Genetic.isCaseStudy) {
                g.drawString(String.valueOf(caseStudyLettering.charAt(i)), p.x, p.y - 10);
            } else {
                g.drawString(numToLetters(i), p.x, p.y - 10);
            }
        }
        g.setColor(Color.BLACK);
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


    public static String numToLetters(int num){
        StringBuilder sb = new StringBuilder();
        char letter = (char)((num % 26) + 'A');
        sb.append(letter);
        num /= 26;

        while(num > 0){
            num--;
            letter = (char)((num % 26) + 'A');
            sb.append(letter);
            num /= 26;
        }

        sb.reverse();
        return sb.toString();
    }


}