import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;

public class FallingBucket {
    private static BufferedImage bucketImg;
    private float x, y;
    private int drawW, drawH;
    private float rotation = 0f;
    private float startY;
    private float targetY;
    private long startTime;
    private long fallDuration = 1800;
    private boolean finished = false;
    private float alpha = 1.0f;

    static {
        try {
            bucketImg = ImageIO.read(new File("Graphic/Bucket.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public FallingBucket(float x, float y, int zombieW, int zombieH) {
        this.x = x + zombieW / 2;
        this.y = y - 20;
        this.startY = this.y;
        this.targetY = y + zombieH - 20;
        
        this.drawW = zombieW / 3;
        this.drawH = zombieH / 3;
        
        if (bucketImg != null) {
            float ratio = (float) bucketImg.getWidth() / bucketImg.getHeight();
            this.drawH = (int) (this.drawW / ratio);
        }
        
        this.startTime = System.currentTimeMillis();
    }

    public boolean isFinished() {
        return finished;
    }

    public void update() {
        if (finished) return;
        long elapsed = System.currentTimeMillis() - startTime;
        if (elapsed > fallDuration + 1000) {
            finished = true;
            return;
        }

        float progress = Math.min(1.0f, (float) elapsed / fallDuration);
        
        y = startY + (targetY - startY) * progress;
        rotation = progress * 90f;

        if (progress >= 1.0f) {
            long fadeElapsed = elapsed - fallDuration;
            alpha = Math.max(0f, 1.0f - (fadeElapsed / 1000f));
        }
    }

    public void draw(Graphics g) {
        if (finished || bucketImg == null) return;
        Graphics2D g2 = (Graphics2D) g;
        Composite oldComp = g2.getComposite();
        if (alpha < 1.0f) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        }
        
        AffineTransform oldTrans = g2.getTransform();
        g2.translate(x + drawW / 2, y + drawH / 2);
        g2.rotate(Math.toRadians(rotation));
        g2.translate(-drawW / 2, -drawH / 2);
        
        g2.drawImage(bucketImg, 0, 0, drawW, drawH, null);
        
        g2.setTransform(oldTrans);
        g2.setComposite(oldComp);
    }
}
