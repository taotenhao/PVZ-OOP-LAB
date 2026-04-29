import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class Buckethead extends NormalZombie {
    private static BufferedImage walk1, eat1;
    private static int w1W, w1H, e1W, e1H;

    private static BufferedImage walk2, eat2;
    private static int w2W, w2H, e2W, e2H;

    private static BufferedImage walk3, eat3;
    private static int w3W, w3H, e3W, e3H;

    static {
        try {
            walk1 = ImageIO.read(new File("Graphic/BucketHead_1__walk.png"));
            w1W = walk1.getWidth() / 7;
            w1H = walk1.getHeight();
        } catch (Exception e) { e.printStackTrace(); }
        
        try {
            eat1 = ImageIO.read(new File("Graphic/BucketHead_1__eat.png"));
            e1W = eat1.getWidth() / 7;
            e1H = eat1.getHeight();
        } catch (Exception e) { e.printStackTrace(); }

        try {
            walk2 = ImageIO.read(new File("Graphic/BucketHead_2__walk.png"));
            w2W = walk2.getWidth() / 7;
            w2H = walk2.getHeight();
        } catch (Exception e) { e.printStackTrace(); }

        try {
            eat2 = ImageIO.read(new File("Graphic/BucketHead_2__eat.png"));
            e2W = eat2.getWidth() / 7;
            e2H = eat2.getHeight();
        } catch (Exception e) { e.printStackTrace(); }

        try {
            walk3 = ImageIO.read(new File("Graphic/BucketHead_3__walk.png"));
            w3W = walk3.getWidth() / 7;
            w3H = walk3.getHeight();
        } catch (Exception e) { e.printStackTrace(); }

        try {
            eat3 = ImageIO.read(new File("Graphic/BucketHead_3__eat.png"));
            e3W = eat3.getWidth() / 7;
            e3H = eat3.getHeight();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public Buckethead(String path, int x, int y) {
        super(path, x, y);
        this.health = 510;
    }

    private boolean bucketFell = false;
    public boolean hasBucketFell() { return bucketFell; }
    public void markBucketFell() { bucketFell = true; }

    @Override
    public void update() {
        super.update();

        if (health > 375) {
            overrideWalkSheet = walk1;
            overrideWalkFrameW = w1W;
            overrideWalkFrameH = w1H;
            overrideEatSheet = eat1;
            overrideEatFrameW = e1W;
            overrideEatFrameH = e1H;
        } else if (health >= 235) {
            overrideWalkSheet = walk2;
            overrideWalkFrameW = w2W;
            overrideWalkFrameH = w2H;
            overrideEatSheet = eat2;
            overrideEatFrameW = e2W;
            overrideEatFrameH = e2H;
        } else if (health >= 100) {
            overrideWalkSheet = walk3;
            overrideWalkFrameW = w3W;
            overrideWalkFrameH = w3H;
            overrideEatSheet = eat3;
            overrideEatFrameW = e3W;
            overrideEatFrameH = e3H;
        } else {
            overrideWalkSheet = null;
            overrideEatSheet = null;
        }
    }
}
