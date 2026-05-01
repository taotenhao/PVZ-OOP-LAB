import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class JackBox extends NormalZombie {
    private static BufferedImage walk1;
    private static int w1W, w1H;

    private static BufferedImage walk2;
    private static int w2W, w2H;

    private static BufferedImage explodeSheet;
    private static int expW, expH;

    private static BufferedImage dieSheetNormal;
    private static int dieFW, dieFH;

    private boolean isExplodingBox = false;
    private boolean damageDealt = false;

    static {
        try {
            walk1 = ImageIO.read(new File("Graphic/JackInTheBox_1__walk.png"));
            w1W = walk1.getWidth() / 7;
            w1H = walk1.getHeight();
        } catch (Exception e) { e.printStackTrace(); }

        try {
            walk2 = ImageIO.read(new File("Graphic/JackInTheBox_2__walk.png"));
            w2W = walk2.getWidth() / 9;
            w2H = walk2.getHeight();
        } catch (Exception e) { e.printStackTrace(); }

        try {
            explodeSheet = ImageIO.read(new File("Graphic/JackInTheBox_Explode.png"));
            expW = explodeSheet.getWidth() / 9;
            expH = explodeSheet.getHeight();
        } catch (Exception e) { e.printStackTrace(); }

        try {
            dieSheetNormal = ImageIO.read(new File("Graphic/JackInTheBox_dead.png"));
            dieFW = dieSheetNormal.getWidth() / 7;
            dieFH = dieSheetNormal.getHeight();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public JackBox(String path, int x, int y) {
        super(path, x, y);
        this.health = 185;
        this.speedMultiplier = 1.5f;
        this.drawOffsetY = 4;
    }

    public void triggerExplosion() {
        if (!isExplodingBox && health > 0) {
            isExplodingBox = true;
            this.health = 0;
        }
    }

    public boolean checkExplodeDamage() {
        if (isExplodingBox && isDying && currentFrame == 3 && !damageDealt) {
            damageDealt = true;
            return true;
        }
        return false;
    }

    @Override
    public void update() {
        super.update();

        if (health >= 90) {
            overrideWalkSheet = walk1;
            overrideWalkFrameW = w1W;
            overrideWalkFrameH = w1H;
            overrideWalkFrames = 7;
        } else if (health > 0) {
            overrideWalkSheet = walk2;
            overrideWalkFrameW = w2W;
            overrideWalkFrameH = w2H;
            overrideWalkFrames = 9;
        } else {
            overrideWalkSheet = null;
            overrideWalkFrames = -1;

            if (isExplodingBox) {
                overrideDieSheet = explodeSheet;
                overrideDieFrameW = expW;
                overrideDieFrameH = expH;
                overrideDieFrames = 9;
                
                this.dieScale = 1.5f;
                this.dieFrameDuration = 80; 
                this.dieFadeDuration = 300;
            } else {
                overrideDieSheet = dieSheetNormal;
                overrideDieFrameW = dieFW;
                overrideDieFrameH = dieFH;
                overrideDieFrames = 7;
                
                this.deadOffsetX = 15;
                this.deadOffsetY = 15;
                
                this.dieScale = 1.0f;
                this.dieFrameDuration = 150;
                this.dieFadeDuration = 1000;
            }
        }
    }
}
