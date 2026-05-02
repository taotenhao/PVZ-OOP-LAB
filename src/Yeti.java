import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class Yeti extends NormalZombie {
    private static BufferedImage w1, w2, e1, e2, dieSheet;
    private static int w1W, w1H, w2W, w2H, e1W, e1H, e2W, e2H, dieW, dieH;

    static {
        try {
            w1 = ImageIO.read(new File("Graphic/Yeti_walk.png"));
            w1W = w1.getWidth() / 6; w1H = w1.getHeight();

            e1 = ImageIO.read(new File("Graphic/Yeti_eat.png"));
            e1W = e1.getWidth() / 4; e1H = e1.getHeight();

            w2 = ImageIO.read(new File("Graphic/Yetihalf_walk.png"));
            w2W = w2.getWidth() / 6; w2H = w2.getHeight();

            e2 = ImageIO.read(new File("Graphic/Yetihalf_eat.png"));
            e2W = e2.getWidth() / 4; e2H = e2.getHeight();

            dieSheet = ImageIO.read(new File("Graphic/Yeti_die.png"));
            dieW = dieSheet.getWidth() / 10; dieH = dieSheet.getHeight();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Yeti(String path, int x, int y) {
        super(path, x, y);
        this.health = 900;
        this.liveScale = 1.035f;
        this.dieScale = 1.12f;
        this.drawOffsetX = 5;
        this.drawOffsetY = -5;
    }

    @Override
    public void update() {
        if (health <= 0) {
            overrideWalkSheet = null;
            overrideWalkFrames = -1;
            overrideEatSheet = null;
            overrideEatFrames = -1;

            overrideDieSheet = dieSheet;
            overrideDieFrameW = dieW;
            overrideDieFrameH = dieH;
            overrideDieFrames = 10;
            super.update();
            return;
        }

        if (health >= 450) {
            overrideWalkSheet = w1;
            overrideWalkFrameW = w1W;
            overrideWalkFrameH = w1H;
            overrideWalkFrames = 6;

            overrideEatSheet = e1;
            overrideEatFrameW = e1W;
            overrideEatFrameH = e1H;
            overrideEatFrames = 4;
        } else if (health > 0) {
            overrideWalkSheet = w2;
            overrideWalkFrameW = w2W;
            overrideWalkFrameH = w2H;
            overrideWalkFrames = 6;

            overrideEatSheet = e2;
            overrideEatFrameW = e2W;
            overrideEatFrameH = e2H;
            overrideEatFrames = 4;
        }
        super.update();
    }
}
