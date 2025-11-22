import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class Visualizer {
    // Store smoothed activations per connection to achieve temporal smoothing
    private static final Map<Level, double[][]> SMOOTH_ACT = new WeakHashMap<>();
    private static final double SMOOTHING = 0.2; // 0..1, higher = more responsive
    public static void drawNetwork(Graphics2D g2d, NeuralNetwork network, int width, int height) {
        int margin = 40;
        int top = margin;
        int left = margin;
        int bottom = height - margin;
        int right = width - margin;

        // Smooth visuals
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        List<Level> levels = network.getLevels();
        int levelHeight = (height - 2 * margin) / levels.size();

        for (int i = levels.size() - 1; i >= 0; i--) {
            int levelTop = top + (int) lerp(
                    (height - margin * 2 - levelHeight),
                    0,
                    (levels.size() == 1) ? 0.5 : (double) i / (levels.size() - 1)
            );

            drawLevel(g2d, levels.get(i), left, levelTop, right - left, levelHeight,
                    i == levels.size() - 1 ? new String[]{"↑", "←", "→", "↓"} : null);
        }
    }

    private static void drawLevel(Graphics2D g2d, Level level, int left, int top, int width, int height, String[] outputLabels) {
        int right = left + width;
        int bottom = top + height;
        double[] inputs = level.getInputs();
        double[] outputs = level.getOutputs();
        double[][] weights = level.getWeights();
        double[] biases = level.getBiases();

        // Draw connections with activation-based styling
        double[][] smooth = SMOOTH_ACT.computeIfAbsent(level, k -> new double[inputs.length][outputs.length]);
        // Resize cache if layer size changes (defensive)
        if (smooth.length != inputs.length || (outputs.length > 0 && smooth[0].length != outputs.length)) {
            double[][] ns = new double[inputs.length][outputs.length];
            int minI = Math.min(smooth.length, ns.length);
            int minJ = (minI > 0) ? Math.min(smooth[0].length, ns[0].length) : 0;
            for (int i = 0; i < minI; i++) {
                System.arraycopy(smooth[i], 0, ns[i], 0, minJ);
            }
            smooth = ns;
            SMOOTH_ACT.put(level, smooth);
        }
        for (int i = 0; i < inputs.length; i++) {
            for (int j = 0; j < outputs.length; j++) {
                int x1 = getNodeX(inputs.length, i, left, right);
                int y1 = bottom;
                int x2 = getNodeX(outputs.length, j, left, right);
                int y2 = top;

                double weight = weights[i][j];
                double inputVal = inputs[i];
                double outputVal = outputs[j];

                // Activation is the positive contribution flowing through this connection
                double activation = Math.max(0, inputVal * weight) * Math.max(0, outputVal);
                activation = clamp01(activation); // 0..1
                // Low-pass filter for smoother animation
                double prev = smooth[i][j];
                double eased = prev + (activation - prev) * SMOOTHING;
                smooth[i][j] = eased;

                Color base = getColorForWeight(weight);

                if (eased > 0.001) {
                    // Subtle glow pass
                    float glowWidth = (float) (4f + 6f * eased);
                    g2d.setStroke(new BasicStroke(glowWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2d.setColor(withAlpha(base, (int) (80 + 100 * eased)));
                    g2d.drawLine(x1, y1, x2, y2);

                    // Main active stroke
                    float mainWidth = (float) (2f + 2f * eased);
                    g2d.setStroke(new BasicStroke(mainWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    Color bright = mix(base, Color.WHITE, (float) (0.25 + 0.55 * eased));
                    g2d.setColor(withAlpha(bright, (int) (160 + 80 * eased)));
                    g2d.drawLine(x1, y1, x2, y2);
                } else {
                    // Inactive/low-activation connection
                    g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2d.setColor(withAlpha(base, 90));
                    g2d.drawLine(x1, y1, x2, y2);
                }
            }
        }

        int nodeRadius = 22;

        // Draw input nodes
        for (int i = 0; i < inputs.length; i++) {
            int x = getNodeX(inputs.length, i, left, right);
            drawNode(g2d, x, bottom, nodeRadius, getColorForActivation(inputs[i]));
        }

        // Draw output nodes
        for (int i = 0; i < outputs.length; i++) {
            int x = getNodeX(outputs.length, i, left, right);
            drawNode(g2d, x, top, nodeRadius, getColorForActivation(outputs[i]));

            // Draw bias circle
            g2d.setColor(getColorForBias(biases[i]));
            g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1, new float[]{3, 3}, 0));
            g2d.drawOval(x - nodeRadius / 2, top - nodeRadius / 2, nodeRadius, nodeRadius);

            // Draw label inside node if available
            if (outputLabels != null && i < outputLabels.length) {
                String label = outputLabels[i];
                Font font = new Font("Arial", Font.BOLD, 16);
                g2d.setFont(font);
                FontMetrics fm = g2d.getFontMetrics(font);
                int textWidth = fm.stringWidth(label);
                int tx = x - textWidth / 2;
                int ty = top + (fm.getAscent() - fm.getDescent()) / 2;
                g2d.setColor(Color.WHITE);
                g2d.drawString(label, tx, ty);
            }
        }
    }

    private static void drawNode(Graphics2D g2d, int x, int y, int radius, Color color) {
        g2d.setColor(Color.BLACK);
        g2d.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        g2d.setColor(color);
        g2d.fillOval(x - (int)(radius * 0.6), y - (int)(radius * 0.6), (int)(radius * 1.2), (int)(radius * 1.2));
    }

    private static int getNodeX(int count, int index, int left, int right) {
        if (count == 1) return (left + right) / 2;
        return (int) lerp(left, right, (double) index / (count - 1));
    }

    private static double lerp(double A, double B, double t) {
        return A + (B - A) * t;
    }

    private static Color getColorForWeight(double value) {
        // Negative = blue, Positive = orange
        int alpha = 180;
        if (value > 0)
            return new Color(255, 150, 0, alpha);
        else
            return new Color(0, 120, 255, alpha);
    }

    private static Color getColorForActivation(double value) {
        // From dark gray (inactive) to green (active)
        value = Math.max(0, Math.min(1, value));
        return new Color((float)(0.2 * (1 - value)), (float)(0.8 * value), 0.2f);
    }

    private static Color getColorForBias(double bias) {
        return bias > 0 ? Color.MAGENTA : Color.CYAN;
    }

    private static double clamp01(double v) {
        return Math.max(0, Math.min(1, v));
    }

    private static Color withAlpha(Color c, int alpha) {
        alpha = Math.max(0, Math.min(255, alpha));
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }

    private static Color mix(Color a, Color b, float t) {
        t = (float) clamp01(t);
        int r = (int) (a.getRed() * (1 - t) + b.getRed() * t);
        int g = (int) (a.getGreen() * (1 - t) + b.getGreen() * t);
        int bl = (int) (a.getBlue() * (1 - t) + b.getBlue() * t);
        int al = (int) (a.getAlpha() * (1 - t) + b.getAlpha() * t);
        return new Color(r, g, bl, al);
    }
}
