import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class Football extends NormalZombie {
    private static BufferedImage walk1, eat1;
    private static int w1W, w1H, e1W, e1H;

    private static BufferedImage walk2, eat2;
    private static int w2W, w2H, e2W, e2H;

    private static BufferedImage walk3, eat3;
    private static int w3W, w3H, e3W, e3H;

    private static BufferedImage walk4, eat4;
    private static int w4W, w4H, e4W, e4H;

    private static BufferedImage walk5, eat5;
    private static int w5W, w5H, e5W, e5H;

    private static BufferedImage dieSheetCustom;
    private static int dieFW, dieFH;

    static {
        try {
            walk1 = ImageIO.read(new File("Graphic/FootBall_1__walk.png"));
            w1W = walk1.getWidth() / 8;
            w1H = walk1.getHeight();
        } catch (Exception e) { e.printStackTrace(); }
        
        try {
            eat1 = ImageIO.read(new File("Graphic/FootBall_1__eat.png"));
            e1W = eat1.getWidth() / 6;
            e1H = eat1.getHeight();
        } catch (Exception e) { e.printStackTrace(); }

        try {
            walk2 = ImageIO.read(new File("Graphic/FootBall_2__walk.png"));
            w2W = walk2.getWidth() / 8;
            w2H = walk2.getHeight();
        } catch (Exception e) { e.printStackTrace(); }

        try {
            eat2 = ImageIO.read(new File("Graphic/FootBall_2__eat.png"));
            e2W = eat2.getWidth() / 6;
            e2H = eat2.getHeight();
        } catch (Exception e) { e.printStackTrace(); }

        try {
            walk3 = ImageIO.read(new File("Graphic/FootBall_3__walk.png"));
            w3W = walk3.getWidth() / 8;
            w3H = walk3.getHeight();
        } catch (Exception e) { e.printStackTrace(); }

        try {
            eat3 = ImageIO.read(new File("Graphic/FootBall_3__eat.png"));
            e3W = eat3.getWidth() / 6;
            e3H = eat3.getHeight();
        } catch (Exception e) { e.printStackTrace(); }

        try {
            walk4 = ImageIO.read(new File("Graphic/FootBall_4__walk.png"));
            w4W = walk4.getWidth() / 8;
            w4H = walk4.getHeight();
        } catch (Exception e) { e.printStackTrace(); }

        try {
            eat4 = ImageIO.read(new File("Graphic/FootBall_4__eat.png"));
            e4W = eat4.getWidth() / 6;
            e4H = eat4.getHeight();
        } catch (Exception e) { e.printStackTrace(); }

        try {
            walk5 = ImageIO.read(new File("Graphic/FootBall_5__walk.png"));
            w5W = walk5.getWidth() / 8;
            w5H = walk5.getHeight();
        } catch (Exception e) { e.printStackTrace(); }

        try {
            eat5 = ImageIO.read(new File("Graphic/FootBall_5__eat.png"));
            e5W = eat5.getWidth() / 6;
            e5H = eat5.getHeight();
        } catch (Exception e) { e.printStackTrace(); }

        try {
            dieSheetCustom = ImageIO.read(new File("Graphic/FootBall_dead.png"));
            dieFW = dieSheetCustom.getWidth() / 7;
            dieFH = dieSheetCustom.getHeight();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public Football(String path, int x, int y) {
        super(path, x, y);
        this.health = 620;
        this.speedMultiplier = 2.5f;
        this.shadowOffsetY = -9;
    }

    @Override
    public void update() {
        super.update();

        if (health > 455) {
            this.speedMultiplier = 2.5f;
            overrideWalkSheet = walk1;
            overrideWalkFrameW = w1W;
            overrideWalkFrameH = w1H;
            overrideEatSheet = eat1;
            overrideEatFrameW = e1W;
            overrideEatFrameH = e1H;
            overrideWalkFrames = 8;
            overrideEatFrames = 6;
        } else if (health >= 285) { // 285 - 455
            this.speedMultiplier = 2.5f;
            overrideWalkSheet = walk2;
            overrideWalkFrameW = w2W;
            overrideWalkFrameH = w2H;
            overrideEatSheet = eat2;
            overrideEatFrameW = e2W;
            overrideEatFrameH = e2H;
            overrideWalkFrames = 8;
            overrideEatFrames = 6;
        } else if (health >= 100) { // 100 - 284
            this.speedMultiplier = 2.5f;
            overrideWalkSheet = walk3;
            overrideWalkFrameW = w3W;
            overrideWalkFrameH = w3H;
            overrideEatSheet = eat3;
            overrideEatFrameW = e3W;
            overrideEatFrameH = e3H;
            overrideWalkFrames = 8;
            overrideEatFrames = 6;
        } else if (health >= 50) { // 50 - 99
            this.speedMultiplier = 1.0f;
            overrideWalkSheet = walk4;
            overrideWalkFrameW = w4W;
            overrideWalkFrameH = w4H;
            overrideEatSheet = eat4;
            overrideEatFrameW = e4W;
            overrideEatFrameH = e4H;
            overrideWalkFrames = 8;
            overrideEatFrames = 6;
        } else if (health > 0) {
            this.speedMultiplier = 1.0f;
            overrideWalkSheet = walk5;
            overrideWalkFrameW = w5W;
            overrideWalkFrameH = w5H;
            overrideEatSheet = eat5;
            overrideEatFrameW = e5W;
            overrideEatFrameH = e5H;
            overrideWalkFrames = 8;
            overrideEatFrames = 6;
        } else {
            this.speedMultiplier = 1.0f;
            overrideWalkSheet = null;
            overrideEatSheet = null;
            overrideWalkFrames = -1;
            overrideEatFrames = -1;

            overrideDieSheet = dieSheetCustom;
            overrideDieFrameW = dieFW;
            overrideDieFrameH = dieFH;
            overrideDieFrames = 7;
        }
    }
}
