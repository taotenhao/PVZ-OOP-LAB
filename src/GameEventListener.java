public interface GameEventListener {

    default void onZombieDeath(NormalZombie zombie) {}

    default void onPlantDestroyed(Plant plant, int row, int col) {}

    default void onWaveCleared(int waveNumber) {}

    default void onWaveStarted(int waveNumber, int zombieCount) {}
}
