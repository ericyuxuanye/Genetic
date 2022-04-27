package com.company;

import javax.swing.*;

public class FrameBuilder {

    public static void main(String args[]){
        makeFrame();
    }
    public static void makeFrame(){
        JFrame frame = new JFrame();
        frame.setContentPane(new Visualizer());
        frame.pack();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
