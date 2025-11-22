import com.google.gson.Gson;
import java.io.*;

public class BrainStorage {
    private static final String FILE_PATH = "best_brain.json";
    private static final Gson gson = new Gson();

    public static void save(NeuralNetwork brain) {
        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            gson.toJson(brain, writer);
            System.out.println("✅ Best brain saved successfully!");
        } catch (IOException e) {
            System.err.println("❌ Failed to save brain: " + e.getMessage());
        }
    }

    public static NeuralNetwork load() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            System.out.println("⚠️ No saved brain found.");
            return null;
        }

        try (FileReader reader = new FileReader(FILE_PATH)) {
            NeuralNetwork brain = gson.fromJson(reader, NeuralNetwork.class);
            System.out.println("✅ Loaded best brain successfully!");
            return brain;
        } catch (IOException e) {
            System.err.println("❌ Failed to load brain: " + e.getMessage());
            return null;
        }
    }

    public static void discard() {
        File file = new File(FILE_PATH);
        if (file.exists() && file.delete()) {
            System.out.println("🗑️ Deleted saved brain.");
        } else {
            System.out.println("⚠️ No saved brain to delete.");
        }
    }
}
