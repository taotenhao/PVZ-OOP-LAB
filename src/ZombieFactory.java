public class ZombieFactory {

    public static NormalZombie create(WaveManager.ZombieType type, String basePath, int x, int y) {
        String zombiePath = basePath + "zombie.png";

        return switch (type) {
            case NORMAL       -> new NormalZombie(zombiePath, x, y);
            case CONEHEAD     -> new Conehead(zombiePath, x, y);
            case BUCKETHEAD   -> new Buckethead(zombiePath, x, y);
            case FOOTBALL     -> new Football(zombiePath, x, y);
            case JACK_IN_THE_BOX -> new JackBox(zombiePath, x, y);
            case NEWSPAPER    -> new Newspaper(zombiePath, x, y);
            case POLE_VAULTING -> new Pole(zombiePath, x, y);
            case YETI         -> new Yeti(zombiePath, x, y);
        };
    }
}
