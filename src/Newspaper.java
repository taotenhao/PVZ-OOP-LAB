import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class Newspaper extends NormalZombie {
    private static BufferedImage w1, w2, w3, w4, w5;
    private static int w1W, w1H, w2W, w2H, w3W, w3H, w4W, w4H, w5W, w5H;

    private static BufferedImage e1, e2, e3, e4, e5;
    private static int e1W, e1H, e2W, e2H, e3W, e3H, e4W, e4H, e5W, e5H;

    private static BufferedImage dieSheet;
    private static int dieW, dieH;

    private static BufferedImage transSheet;
    private static int tW, tH;

    private boolean isTransitioning = false;
    private boolean transitionDone = false;
    private long transitionStartTime = 0;

    static {
        try {
            w1 = ImageIO.read(new File("Graphic/Newspaper_1__walk.png"));
            w1W = w1.getWidth() / 7; w1H = w1.getHeight();

            w2 = ImageIO.read(new File("Graphic/Newspaper_2__walk.png"));
            w2W = w2.getWidth() / 7; w2H = w2.getHeight();

            w3 = ImageIO.read(new File("Graphic/Newspaper_3__walk.png"));
            w3W = w3.getWidth() / 7; w3H = w3.getHeight();

            w4 = ImageIO.read(new File("Graphic/Newspaper_4__walk.png"));
            w4W = w4.getWidth() / 7; w4H = w4.getHeight();

            w5 = ImageIO.read(new File("Graphic/Newspaper_5__walk.png"));
            w5W = w5.getWidth() / 7; w5H = w5.getHeight();

            e1 = ImageIO.read(new File("Graphic/Newspaper_1__eat.png"));
            e1W = e1.getWidth() / 7; e1H = e1.getHeight();

            e2 = ImageIO.read(new File("Graphic/Newspaper_2__eat.png"));
            e2W = e2.getWidth() / 7; e2H = e2.getHeight();

            e3 = ImageIO.read(new File("Graphic/Newspaper_3__eat.png"));
            e3W = e3.getWidth() / 7; e3H = e3.getHeight();

            e4 = ImageIO.read(new File("Graphic/Newspaper_4__eat.png"));
            e4W = e4.getWidth() / 7; e4H = e4.getHeight();

            e5 = ImageIO.read(new File("Graphic/Newspaper_5__eat.png"));
            e5W = e5.getWidth() / 7; e5H = e5.getHeight();

            transSheet = ImageIO.read(new File("Graphic/Newspaper_3.5.png"));
            tW = transSheet.getWidth() / 3; tH = transSheet.getHeight();

            dieSheet = ImageIO.read(new File("Graphic/Newspaper_die.png"));
            dieW = dieSheet.getWidth() / 7; dieH = dieSheet.getHeight();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Newspaper(String path, int x, int y) {
        super(path, x, y);
        this.health = 200;
        this.speedMultiplier = 1.0f;
        this.eatSpeedMultiplier = 1.0f;
    }

    @Override
    public void takeDamage(int amount) {
        if (isTransitioning) return;
        super.takeDamage(amount);
    }

    @Override
    public void update() {
        if (health > 0) {
            if (health < 100 && !transitionDone) {
                if (!isTransitioning) {
                    isTransitioning = true;
                    transitionStartTime = System.currentTimeMillis();
                    stopEating();
                    this.speedMultiplier = 0.0f;
                    this.eatSpeedMultiplier = 0.0f;
                }

                long elapsed = System.currentTimeMillis() - transitionStartTime;
                if (elapsed >= 2000) {
                    isTransitioning = false;
                    transitionDone = true;
                    this.speedMultiplier = 5.0f;
                    this.eatSpeedMultiplier = 5.0f;
                } else {
                    overrideWalkSheet = transSheet;
                    overrideWalkFrameW = tW;
                    overrideWalkFrameH = tH;
                    overrideWalkFrames = 3;
                    
                    overrideEatSheet = transSheet;
                    overrideEatFrameW = tW;
                    overrideEatFrameH = tH;
                    overrideEatFrames = 3;

                    int f = (int) ((elapsed * 3) / 2000);
                    if (f >= 3) f = 2;
                    this.currentFrame = f;
                }
            } else {
                if (health > 167) {
                    overrideWalkSheet = w1;
                    overrideWalkFrameW = w1W;
                    overrideWalkFrameH = w1H;
                    overrideWalkFrames = 7;

                    overrideEatSheet = e1;
                    overrideEatFrameW = e1W;
                    overrideEatFrameH = e1H;
                    overrideEatFrames = 7;
                } else if (health > 133) {
                    overrideWalkSheet = w2;
                    overrideWalkFrameW = w2W;
                    overrideWalkFrameH = w2H;
                    overrideWalkFrames = 7;

                    overrideEatSheet = e2;
                    overrideEatFrameW = e2W;
                    overrideEatFrameH = e2H;
                    overrideEatFrames = 7;
                } else if (health >= 100) {
                    overrideWalkSheet = w3;
                    overrideWalkFrameW = w3W;
                    overrideWalkFrameH = w3H;
                    overrideWalkFrames = 7;

                    overrideEatSheet = e3;
                    overrideEatFrameW = e3W;
                    overrideEatFrameH = e3H;
                    overrideEatFrames = 7;
                } else if (health >= 50) {
                    overrideWalkSheet = w4;
                    overrideWalkFrameW = w4W;
                    overrideWalkFrameH = w4H;
                    overrideWalkFrames = 7;

                    overrideEatSheet = e4;
                    overrideEatFrameW = e4W;
                    overrideEatFrameH = e4H;
                    overrideEatFrames = 7;
                } else {
                    overrideWalkSheet = w5;
                    overrideWalkFrameW = w5W;
                    overrideWalkFrameH = w5H;
                    overrideWalkFrames = 7;

                    overrideEatSheet = e5;
                    overrideEatFrameW = e5W;
                    overrideEatFrameH = e5H;
                    overrideEatFrames = 7;
                }
            }
        } else {
            overrideWalkSheet = null;
            overrideWalkFrames = -1;
            overrideEatSheet = null;
            overrideEatFrames = -1;
            
            overrideDieSheet = dieSheet;
            overrideDieFrameW = dieW;
            overrideDieFrameH = dieH;
            overrideDieFrames = 7;
        }

        super.update();
    }
}
