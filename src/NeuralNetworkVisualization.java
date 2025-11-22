import javax.swing.*;
import java.awt.*;

public class NeuralNetworkVisualization extends JPanel {
    private NeuralNetwork network;

    public NeuralNetworkVisualization(NeuralNetwork network) {
        this.network = network;
        setPreferredSize(new Dimension(400, 700)); // Wider NN panel
        setBackground(Color.BLACK);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (network != null) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Visualizer.drawNetwork(g2d, network, getWidth(), getHeight());
        }
    }

    public void setNetwork(NeuralNetwork network) {
        this.network = network;
    }
}
