import javax.swing.*;
import java.awt.*;

public class MainScreen extends JFrame {
    private int WIDTH = 1000;
    private int HEIGHT = 700;

    public MainScreen() {
        setTitle("Car Simulation");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setResizable(false);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton saveButton = new JButton("💾");
        JButton deleteButton = new JButton("🗑️");

        buttonPanel.add(saveButton);
        buttonPanel.add(deleteButton);

        add(buttonPanel, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy = 0;

        Road road = new Road(WIDTH / 5 - 5, HEIGHT / 2, 5);
        gbc.gridx = 0;
        gbc.weightx = 0.4;
        gbc.weighty = 1.0;
        mainPanel.add(road, gbc);

        NeuralNetworkVisualization nnPanel = new NeuralNetworkVisualization(road.getCar().getBrain());
        gbc.gridx = 1;
        gbc.weightx = 0.6;
        mainPanel.add(nnPanel, gbc);

        road.setNeuralNetworkPanel(nnPanel);
        add(mainPanel, BorderLayout.CENTER);

        saveButton.addActionListener(e -> {
            if (road.getBestCar() != null) {
                BrainStorage.save(road.getBestCar().getBrain());
                JOptionPane.showMessageDialog(this, "✅ Best Brain Saved Successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "⚠️ No best car found yet!");
            }
        });

        deleteButton.addActionListener(e -> {
            BrainStorage.discard();
            JOptionPane.showMessageDialog(this, "🗑️ Saved Brain Deleted!");
        });

        setVisible(true);
    }
}
