import java.awt.*;

abstract class Zombie implements Comparable<Zombie> {
    protected int x, y, frameWidth, frameHeight, health;
    protected int boundsOffsetX = 0;
    private long flashEndTime = 0;

    public Zombie(int x, int y, int health) {
        this.x = x;
        this.y = y;
        this.health = health;
    }

    public void flash() {
        flashEndTime = System.currentTimeMillis() + 150;
    }

    public boolean isFlashing() {
        return System.currentTimeMillis() < flashEndTime;
    }

    public void takeDamage(int amount) {
        this.health -= amount;
        flash();
    }

    public void multiplyHealth(float multiplier) {
        this.health = (int) (this.health * multiplier);
    }

    public abstract void update();

    public abstract void draw(Graphics g);

    public boolean isFinished() { return health <= 0; }

    public int getX() { return x; }
    public int getY() { return y; }

    public int compareTo(Zombie other) {
        return Integer.compare(this.y, other.y);
    }

    public Rectangle getBounds() {
        return new Rectangle(x + 50 + boundsOffsetX, y + 20, 2 * frameWidth / 3, 2 * frameHeight / 3);
    }

    public Rectangle getEatingBounds() {
        return new Rectangle(x + 50 + boundsOffsetX, y + 20, 20, 2 * frameHeight / 3);
    }
}
