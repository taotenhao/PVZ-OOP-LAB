import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WaveManager {

    public enum ZombieType {
        NORMAL(1),
        CONEHEAD(3),
        NEWSPAPER(4),
        POLE_VAULTING(6),
        BUCKETHEAD(8),
        FOOTBALL(15),
        JACK_IN_THE_BOX(20),
        YETI(50);

        private final int weight;

        ZombieType(int weight) {
            this.weight = weight;
        }

        public int getWeight() {
            return weight;
        }
    }

    private final Random rand = new Random();

    public WaveManager() {}

    private float currentHealthMultiplier = 1.0f;
    private int currentBudget = 0;
    private int lastProcessedWave = 0;

    private final int[] PRESET_BUDGETS = {
        0,
        2, 4, 6, 8, 14,
        18, 25, 35, 44, 62, 85, 120, 170, 230, 300,
        380, 470, 570, 680, 800
    };

    private void computeWaveParameters(int wave) {
        if (wave <= lastProcessedWave) return;
        
        if (wave == 1) {
            currentHealthMultiplier = 1.0f;
            lastProcessedWave = 0;
        }

        if (wave <= 20) {
            currentBudget = PRESET_BUDGETS[wave];
        } else {
            int choice = rand.nextInt(2);
            if (choice == 0) {
                int wDiff = wave - 20;
                currentBudget = 800 + 100 * wDiff + 5 * wDiff * wDiff;
            } else {
                if (currentBudget == 0) {
                    currentBudget = 800;
                }
                currentHealthMultiplier += 0.1f;
            }
        }
        lastProcessedWave = wave;
    }

    public float getHealthMultiplier() {
        return currentHealthMultiplier;
    }

    public void reset() {
        currentHealthMultiplier = 1.0f;
        currentBudget = 0;
        lastProcessedWave = 0;
    }

    private List<ZombieType> getAvailableTypes(int wave) {
        List<ZombieType> types = new ArrayList<>();
        
        types.add(ZombieType.NORMAL);

        if (wave >= 2) {
            types.add(ZombieType.CONEHEAD);
        }

        if (wave >= 3) {
            types.add(ZombieType.NEWSPAPER);
        }

        if (wave >= 6) {
            types.add(ZombieType.POLE_VAULTING);
            types.add(ZombieType.BUCKETHEAD);
        }

        if (wave >= 16) {
            types.add(ZombieType.FOOTBALL);
            types.add(ZombieType.FOOTBALL); 
            types.add(ZombieType.JACK_IN_THE_BOX);
            types.add(ZombieType.JACK_IN_THE_BOX);
            types.add(ZombieType.YETI);
        }

        return types;
    }

    public List<ZombieType> generateWave(int waveNumber) {
        computeWaveParameters(waveNumber);
        int totalBudget = currentBudget;
        List<ZombieType> availableTypes = getAvailableTypes(waveNumber);
        List<ZombieType> waveList = new ArrayList<>();

        int minWeight = ZombieType.NORMAL.getWeight();

        while (totalBudget >= minWeight) {
            ZombieType selected = availableTypes.get(rand.nextInt(availableTypes.size()));

            if (selected.getWeight() <= totalBudget) {
                waveList.add(selected);
                totalBudget -= selected.getWeight();
            }
        }

        return waveList;
    }
}
