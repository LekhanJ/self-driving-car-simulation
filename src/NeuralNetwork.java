import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NeuralNetwork {
    private List<Level> levels;
    private int[] neuronCounts;

    public NeuralNetwork(int[] neuronCounts) {
        this.neuronCounts = neuronCounts;
        levels = new ArrayList<>();
        for (int i = 0; i < neuronCounts.length - 1; i++) {
            levels.add(new Level(neuronCounts[i], neuronCounts[i + 1]));
        }
    }

    public static double[] feedForward(double[] givenInputs, NeuralNetwork network) {
        double[] outputs = Level.feedForward(givenInputs, network.levels.get(0));
        for (int i = 1; i < network.levels.size(); i++) {
            outputs = Level.feedForward(outputs, network.levels.get(i));
        }

        return outputs;
    }

    public List<Level> getLevels() {
        return levels;
    }

    public static void mutate(NeuralNetwork network, double amount) {
        Random rand = new Random();
        for (Level level : network.getLevels()) {
            for (int i=0; i<level.getBiases().length; i++) {
                level.getBiases()[i] = Utils.lerp(
                        level.getBiases()[i],
                        rand.nextDouble() * 2 - 1,
                        amount
                );
            }
            for (int i=0; i<level.getWeights().length; i++) {
                for (int j=0; j<level.getWeights()[i].length; j++) {
                    level.getWeights()[i][j] = Utils.lerp(
                            level.getWeights()[i][j],
                            rand.nextDouble() * 2 - 1,
                            amount
                    );
                }
            }
        }
    }

    public int[] getNeuronCounts() {
        return neuronCounts;
    }
}
