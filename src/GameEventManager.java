import java.util.ArrayList;
import java.util.List;

public class GameEventManager {
    private static GameEventManager instance;
    private final List<GameEventListener> listeners = new ArrayList<>();

    private GameEventManager() {}

    public static GameEventManager getInstance() {
        if (instance == null) {
            instance = new GameEventManager();
        }
        return instance;
    }

    public void addListener(GameEventListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(GameEventListener listener) {
        listeners.remove(listener);
    }

    public void clearListeners() {
        listeners.clear();
    }



    public void fireZombieDeath(NormalZombie zombie) {
        for (GameEventListener l : listeners) {
            l.onZombieDeath(zombie);
        }
    }

    public void firePlantDestroyed(Plant plant, int row, int col) {
        for (GameEventListener l : listeners) {
            l.onPlantDestroyed(plant, row, col);
        }
    }

    public void fireWaveCleared(int waveNumber) {
        for (GameEventListener l : listeners) {
            l.onWaveCleared(waveNumber);
        }
    }

    public void fireWaveStarted(int waveNumber, int zombieCount) {
        for (GameEventListener l : listeners) {
            l.onWaveStarted(waveNumber, zombieCount);
        }
    }
}
