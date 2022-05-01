package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class Visualizer extends JPanel implements MouseListener, MouseMotionListener {

    private static final int width = Genetic.width;
    private static final int height = Genetic.height;

    private static final String caseStudyLettering = "XABCDEFGHIJKLMNOPQRST";

    /**
     * X Coordinate when mouse first pressed down
     */
    private int startX;
    /**
     * Y Coordinate when mouse first pressed down
     */
    private int startY;
    /**
     * The x coordinate of the deletion rectangle
     */
    private int rectX;
    /**
     * The y coordinate of the delection rectangle
     */
    private int rectY;

    /**
     * The width of the deletion rectangle
     */
    private int rectWidth;
    /**
     * The height of the deletion rectangle
     */
    private int rectHeight;
    /**
     * Whether we are currently drawing a deletion rectangle
     */
    private boolean drawingRect = false;

    public Visualizer() {
        super();
        addMouseListener(this);
        addMouseMotionListener(this);
        setPreferredSize(new Dimension(width, height));
    }

    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        setBackground(Color.WHITE);
        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (!Main.isReset) drawBestPath(g, Genetic.getBest());
        else if (drawingRect) drawDeletionRect(g);

        g.setColor(Color.BLACK);
        for (Point p : Genetic.cities) {
            g.fillOval(p.x-6, p.y-6, 13, 13);
        }
        // draw text on top of points
        g.setColor(Color.GRAY);
        for (int i = 0; i < Genetic.numCities; i++) {
            Point p = Genetic.cities[i];
            if (Genetic.isCaseStudy) {
                g.drawString(String.valueOf(caseStudyLettering.charAt(i)), p.x, p.y - 10);
            } else {
                g.drawString(numToLetters(i), p.x, p.y - 10);
            }
        }
    }

    private void drawBestPath(Graphics g, int[] bestPath){
        g.setColor(Color.RED);
        for (int i = 1; i < Genetic.numCities; i++) {
            Point prev = Genetic.cities[bestPath[i - 1]];
            Point current = Genetic.cities[bestPath[i]];
            g.drawLine(prev.x, prev.y, current.x, current.y);
        }

        Point first = Genetic.cities[bestPath[0]];
        Point last = Genetic.cities[bestPath[Genetic.numCities - 1]];
        g.drawLine(last.x, last.y, first.x, first.y);
    }

    private void drawDeletionRect(Graphics g) {
        g.setColor(Color.RED);
        g.drawRect(rectX, rectY, rectWidth, rectHeight);
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


    @Override
    public void mouseClicked(MouseEvent e) {
        if (!Main.isReset) return;
        Genetic.addPoint(e.getX(), e.getY());
        repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (!Main.isReset) return;
        startX = e.getX();
        startY = e.getY();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        drawingRect = false;
        Genetic.deletePoints(rectX, rectY, rectX + rectWidth, rectY + rectHeight);
        repaint();
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        Main.setMouse(x, y);
        if (!Main.isReset) return;
        drawingRect = true;
        rectX = startX;
        rectY = startY;
        if (x < rectX) {
            // swap
            int temp = x;
            x = rectX;
            rectX = temp;
        }
        if (y < rectY) {
            // swap
            int temp = y;
            y = rectY;
            rectY = temp;
        }
        rectWidth = x - rectX;
        rectHeight = y - rectY;
        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        Main.setMouse(e.getX(), e.getY());
    }
}