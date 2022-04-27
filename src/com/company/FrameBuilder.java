package com.company;

import javax.swing.*;

public class FrameBuilder {

    public static void main(String args[]){
        JFrame frame = new JFrame();
        frame.setContentPane(new Visualizer());
        frame.pack();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);

        Timer t = new Timer(50, e -> {
            Main.nextGen();
            frame.repaint();

        });

        t.start();

    }
}
