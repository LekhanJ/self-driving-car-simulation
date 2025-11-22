import java.util.Random;

public class Level {
    private double[] inputs;
    private double[] outputs;
    private double[] biases;
    private double[][] weights;

    public Level(int inputCount, int outputCount) {
        inputs = new double[inputCount];
        outputs = new double[outputCount];
        biases = new double[outputCount];
        weights = new double[inputCount][outputCount];

        randomize();
    }

    private void randomize() {
        Random rand = new Random();
        for (int i = 0; i < weights.length; i++) {
            for (int j = 0; j < weights[i].length; j++) {
                weights[i][j] = rand.nextDouble() * 2 - 1;
            }
        }

        for (int i = 0; i < biases.length; i++) {
            biases[i] = rand.nextDouble() * 2 - 1;
        }
    }

    public static double[] feedForward(double[] givenInputs, Level level) {
        for (int i = 0; i < level.inputs.length; i++) {
            level.inputs[i] = givenInputs[i];
        }

        for (int i = 0; i < level.outputs.length; i++) {
            double sum = 0;
            for (int j = 0; j < level.inputs.length; j++) {
                sum += level.inputs[j] * level.weights[j][i];
            }

            if (sum > level.biases[i]) {
                level.outputs[i] = 1;
            } else {
                level.outputs[i] = 0;
            }
        }

        return level.outputs;
    }

    public double[] getOutputs() {
        return outputs;
    }

    public double[] getInputs() {
        return inputs;
    }

    public double[] getBiases() {
        return biases;
    }

    public double[][] getWeights() {
        return weights;
    }
}
