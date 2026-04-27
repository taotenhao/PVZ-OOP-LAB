import java.awt.*;

abstract class Plant {
    public enum Type {
        PEASHOOTER, SUNFLOWER, WALLNUT, CHERRYBOMB, TWINSUNFLOWER, TALLNUT, SNOWPEA, TORCHWOOD, GATLING, WINTERMELON
    }
    protected int x, y, frameWidth, frameHeight, health;
    private Type type;
    private long flashEndTime = 0;

    public Plant(int x, int y, int health) {
        this.x = x;
        this.y = y;
        this.health = health;
    }

    /** Kích hoạt nháy sáng 150ms */
    public void flash() {
        flashEndTime = System.currentTimeMillis() + 150;
    }

    public boolean isFlashing() {
        return System.currentTimeMillis() < flashEndTime;
    }

    public void takeDamage(int dmg) {
        health -= dmg;
        flash();
    }

    public boolean isDead() {
        return health <= 0;
    }

    public int getHealth() { return health; }

    public abstract void update();

    public abstract void draw(Graphics g);

    public Rectangle getBounds() {
        // Thu hẹp hitbox bên trái 15px để tránh zombie vừa nhảy qua đã cắn ngược lại
        return new Rectangle(x + 15, y, Math.max(1, frameWidth - 15), frameHeight);
    }

    public int getX() { return x; }
    public int getY() { return y; }
}
