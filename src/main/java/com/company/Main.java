package com.company;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main {

    /**
     * Used to hold controls;
     */
    public static JPanel controlPanel;
    /**
     * The visualizer object
     */
    public static final Visualizer visualizer = new Visualizer();

    public static JButton usingCaseStudy;
    public static JButton randomCities;
    public static JButton clear;
    public static JSpinner matingPoolSize;
    public static JSpinner survival;
    public static JComboBox<String> selectionAlgorithm;
    public static final String[] selectionAlgorithmChoices = {"Roulette Wheel", "Tournament"};
    public static JSpinner numTournament;
    public static boolean rouletteSelection = true;
    public static JComboBox<String> crossoverAlgorithm;
    public static final String[] crossoverAlgorithmChoices = {"Partially Mapped", "Cycle", "Order"};
    public static int crossoverAlgorithmIndex;

    public static JComboBox<String> mutationAlgorithm;
    public static final String[] mutationAlgorithmChoices = {"Reverse", "Two Point"};
    public static int mutationAlgorithmIndex = 0;
    public static JSpinner mutationProbability;
    public static boolean reverseMutation = true;
    public static JLabel numCities;
    public static JLabel mouseLoc;
    public static JLabel generation;
    public static JLabel fitness;

    public static final Font DISPLAY_FONT = new Font("Arial", Font.BOLD, 20);

    public static JButton playPause;
    public static ImageIcon playIcon = new FlatSVGIcon("play.svg", 100, 100);
    public static ImageIcon pauseIcon = new FlatSVGIcon("pause.svg", 100, 100);
    public static JButton stop;
    public static ImageIcon stopIcon = new FlatSVGIcon("reset.svg", 100, 100);

    /**
     * Whether the simulation is running
     */
    public static boolean running = false;
    public static boolean isReset = true;
    public static final Object monitor = new Object();

    public static final Timer timer = new Timer(100, e -> {
        //Genetic.nextGen();
        visualizer.repaint();
        generation.setText(String.valueOf(Genetic.getGeneration()));
        fitness.setText(String.valueOf(Genetic.getBestFitness()));
    });

    public static void main(String[] args){
        SwingUtilities.invokeLater(Main::initComponents);
        Genetic.generateCities();
        while (true) {
            // wait for signal to start
            synchronized (monitor) {
                try {
                    monitor.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            while (running) Genetic.nextGen(rouletteSelection, crossoverAlgorithmIndex, reverseMutation);
        }
    }

    public static void initComponents() {
        FlatLightLaf.setup();
        JFrame frame = new JFrame("Genetic Algorithm Simulation");
        JPanel rootPanel = (JPanel) frame.getContentPane();
        rootPanel.setLayout(new FlowLayout());
        rootPanel.add(visualizer);

        // miglayout automatically wraps after 2 columns
        controlPanel = new JPanel(new MigLayout("wrap 2"));

        usingCaseStudy = new JButton("Case Study");
        usingCaseStudy.addActionListener(l -> {
            Genetic.numCities = 21;
            Genetic.isCaseStudy = true;
            Genetic.generateCities();
            visualizer.repaint();
            updateNumCities();
        });
        controlPanel.add(usingCaseStudy, "span 2, split 3");

        randomCities = new JButton("Random Cities");
        randomCities.addActionListener(new ActionListener() {
            static final int defaultValue = Genetic.numCities;
            final SpinnerNumberModel model = new SpinnerNumberModel(defaultValue, 1, 5000, 1);
            final JSpinner spinner = new JSpinner(model);
            final JPanel panel = new JPanel();

            {
                panel.add(new JLabel("Number of cities: "));
                panel.add(spinner);
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                model.setValue(defaultValue);
                if (JOptionPane.showConfirmDialog(frame, panel, "Enter number of cities", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                    Genetic.isCaseStudy = false;
                    Genetic.numCities = ((SpinnerNumberModel)spinner.getModel()).getNumber().intValue();
                    Genetic.generateCities();
                    visualizer.repaint();
                    updateNumCities();
                }
            }
        });
        controlPanel.add(randomCities);

        clear = new JButton("Clear");
        clear.addActionListener(l -> {
            Genetic.clear();
            Genetic.isCaseStudy = false;
            visualizer.repaint();
            updateNumCities();
        });
        controlPanel.add(clear);

        matingPoolSize = new JSpinner(new SpinnerNumberModel(Genetic.matingPoolSize, 1, 5000, 1));
        matingPoolSize.addChangeListener(e -> {
            Genetic.matingPoolSize = ((SpinnerNumberModel)matingPoolSize.getModel()).getNumber().intValue();
            survival.setModel(new SpinnerNumberModel(Genetic.matingPoolSize / 2, 1, 5000, 1));
            numTournament.setModel(new SpinnerNumberModel((Genetic.matingPoolSize - Genetic.survival) / 10, 1, Genetic.survival, 1));
        });
        controlPanel.add(new JLabel("Mating Pool Size"));
        controlPanel.add(matingPoolSize);

        survival = new JSpinner(new SpinnerNumberModel(Genetic.survival, 1, Genetic.matingPoolSize, 1));
        survival.addChangeListener(e -> {
            Genetic.survival = ((SpinnerNumberModel) survival.getModel()).getNumber().intValue();
            numTournament.setModel(new SpinnerNumberModel(Genetic.matingPoolSize - Genetic.survival / 10, 1, Genetic.matingPoolSize, 1));
        });
        controlPanel.add(new JLabel("Survival Number"));
        controlPanel.add(survival);

        selectionAlgorithm = new JComboBox<>(selectionAlgorithmChoices);
        selectionAlgorithm.addActionListener(l -> {
            rouletteSelection = selectionAlgorithm.getSelectedIndex() == 0;
            numTournament.setEnabled(!rouletteSelection);
        });
        controlPanel.add(new JLabel("Selection Algorithm"));
        controlPanel.add(selectionAlgorithm);

        numTournament = new JSpinner(new SpinnerNumberModel(Genetic.numTournament, 1, Genetic.matingPoolSize - Genetic.survival, 1));
        numTournament.addChangeListener(e -> Genetic.numTournament = ((SpinnerNumberModel) numTournament.getModel()).getNumber().intValue());
        numTournament.setEnabled(false);
        controlPanel.add(new JLabel("Tournament selection number"));
        controlPanel.add(numTournament);

        crossoverAlgorithm = new JComboBox<>(crossoverAlgorithmChoices);
        crossoverAlgorithm.setSelectedIndex(Genetic.ORDER);
        crossoverAlgorithm.addActionListener(l -> crossoverAlgorithmIndex = crossoverAlgorithm.getSelectedIndex());
        controlPanel.add(new JLabel("Crossover Algorithm"));
        controlPanel.add(crossoverAlgorithm);

        mutationAlgorithm = new JComboBox<>(mutationAlgorithmChoices);
        mutationAlgorithm.addActionListener(l -> mutationAlgorithmIndex = mutationAlgorithm.getSelectedIndex());
        controlPanel.add(new JLabel("Mutation Algorithm"));
        controlPanel.add(mutationAlgorithm);

        mutationProbability = new JSpinner(new SpinnerNumberModel(Math.round(Genetic.mutationProbability * 100), 0, 100, 1));
        mutationProbability.addChangeListener(l -> Genetic.mutationProbability = ((SpinnerNumberModel)mutationProbability.getModel()).getNumber().doubleValue() / 100);
        controlPanel.add(new JLabel("Mutation Probability"));
        controlPanel.add(mutationProbability, "split 2");
        controlPanel.add(new JLabel("%"));

        numCities = new JLabel(String.valueOf(Genetic.numCities));
        controlPanel.add(new JLabel("Number of cities"));
        controlPanel.add(numCities);

        mouseLoc = new JLabel("(,)");
        controlPanel.add(new JLabel("Mouse"));
        controlPanel.add(mouseLoc);

        controlPanel.add(Box.createVerticalStrut(30), "wrap");

        generation = new JLabel("0");
        generation.setFont(DISPLAY_FONT);
        JLabel generationLabel = new JLabel("Generation");
        generationLabel.setFont(DISPLAY_FONT);
        controlPanel.add(generationLabel);
        controlPanel.add(generation);

        fitness = new JLabel("");
        fitness.setFont(DISPLAY_FONT);
        JLabel fitnessLabel = new JLabel("Fitness");
        fitnessLabel.setFont(DISPLAY_FONT);
        controlPanel.add(fitnessLabel);
        controlPanel.add(fitness);

        controlPanel.add(Box.createVerticalStrut(30), "wrap");

        playPause = new JButton(playIcon);
        playPause.addActionListener(l -> {
            if (running) {
                // we want to pause
                playPause.setIcon(playIcon);
                running = false;
                timer.stop();
                survival.setEnabled(true);
                selectionAlgorithm.setEnabled(true);
                numTournament.setEnabled(!rouletteSelection);
                crossoverAlgorithm.setEnabled(true);
                mutationAlgorithm.setEnabled(true);
                mutationProbability.setEnabled(true);
                timer.stop();
            } else {
                // we want to play
                playPause.setIcon(pauseIcon);
                running = true;
                if (isReset) {
                    isReset = false;
                    Genetic.reset();
                }
                usingCaseStudy.setEnabled(false);
                randomCities.setEnabled(false);
                clear.setEnabled(false);
                matingPoolSize.setEnabled(false);
                survival.setEnabled(false);
                selectionAlgorithm.setEnabled(false);
                numTournament.setEnabled(false);
                crossoverAlgorithm.setEnabled(false);
                mutationAlgorithm.setEnabled(false);
                mutationProbability.setEnabled(false);
                synchronized (monitor) {
                    monitor.notify();
                }
                timer.start();
            }
        });
        controlPanel.add(playPause);

        stop = new JButton(stopIcon);
        stop.addActionListener(l -> {
            usingCaseStudy.setEnabled(true);
            randomCities.setEnabled(true);
            clear.setEnabled(true);
            matingPoolSize.setEnabled(true);
            survival.setEnabled(true);
            selectionAlgorithm.setEnabled(true);
            numTournament.setEnabled(!rouletteSelection);
            crossoverAlgorithm.setEnabled(true);
            mutationAlgorithm.setEnabled(true);
            mutationProbability.setEnabled(true);
            isReset = true;
            running = false;
            playPause.setIcon(playIcon);
            timer.stop();
            visualizer.repaint();
            Genetic.resetGenerationCount();
            fitness.setText("");
            generation.setText("0");
        });
        controlPanel.add(stop);


        rootPanel.add(controlPanel);
        frame.pack();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public static void setMouse(int x, int y) {
        mouseLoc.setText("(" + x + ", " + y + ")");
    }

    public static void updateNumCities() {
        numCities.setText(String.valueOf(Genetic.numCities));
    }
}
