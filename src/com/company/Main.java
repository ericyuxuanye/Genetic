package com.company;

import javax.swing.*;

public class Main {

    public static void main(String[] args){
        JFrame frame = new JFrame("Genetic Algorithm (" + Genetic.numCities + " cities)");
        frame.setContentPane(new Visualizer());
        frame.pack();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);

        Timer t = new Timer(100, e -> {
            //Genetic.nextGen();
            frame.repaint();
        });

        t.start();
        while (true) {
            Genetic.nextGen();
        }


    }
}
