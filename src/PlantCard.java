import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;

public class PlantCard {
    private int frameWidth, frameHeight;
    private BufferedImage sheet;

    private Plant.Type type;
    private Rectangle bounds;
    private boolean selected = false;
    private boolean hovered = false;
    private float hoverAnim = 0f;

    private final long cooldownMs;
    private long cooldownStart = -1;
    private int sunCost;
    private boolean frozen = true;

    public PlantCard(Plant.Type type, String path, int x, int y, int w, int h, int sunCost, long cooldownMs) {
        this.type = type;
        this.sunCost = sunCost;
        this.cooldownMs = cooldownMs;
        this.bounds = new Rectangle(x, y, w, h);
        try {
            File f = new File(path);
            sheet = ImageIO.read(f);
            this.frameWidth = sheet.getWidth();
            this.frameHeight = sheet.getHeight();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void update(float dt) {
        float target = hovered ? 1f : 0f;
        hoverAnim += (target - hoverAnim) * Math.min(1f, dt * 12f);
    }

    public void startCooldown() {
        cooldownStart = System.currentTimeMillis();
    }

    public boolean isOnCooldown() {
        if (cooldownStart < 0) return false;
        return System.currentTimeMillis() - cooldownStart < cooldownMs;
    }

    private float getCooldownRatio() {
        if (cooldownStart < 0) return 0f;
        long elapsed = System.currentTimeMillis() - cooldownStart;
        float ratio = 1f - (float) elapsed / cooldownMs;
        return Math.max(0f, Math.min(1f, ratio));
    }

    public int getSunCost() { return sunCost; }

    public void draw(Graphics2D g) {
        int x = bounds.x, y = bounds.y, w = bounds.width, h = bounds.height;
        float lift = hoverAnim * 4f;

        if (hoverAnim > 0.05f) {
            g.setColor(new Color(0, 0, 0, (int) (80 * hoverAnim)));
            g.fill(new RoundRectangle2D.Float(x + 3, y + 3 - lift + 4, w, h, 12, 12));
        }

        int srcX = 0;
        g.drawImage(sheet, x, y, x + w, y + h, srcX, 0, srcX + frameWidth, frameHeight, null);

        float cdRatio = frozen ? 1.0f : getCooldownRatio();
        if (cdRatio > 0f) {
            int darkH = (int) (h * cdRatio);
            Shape oldClip = g.getClip();
            g.setClip(new RoundRectangle2D.Float(x, y, w, h, 6, 6));
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(x, y, w, darkH);
            g.setClip(oldClip);
        }

        if (selected) {
            int cx = x + w / 2;
            int arrowTop = y + h + 3;
            int arrowH = 8;
            int arrowHalfW = 6;

            Polygon arrow = new Polygon(
                    new int[] { cx, cx - arrowHalfW, cx + arrowHalfW },
                    new int[] { arrowTop, arrowTop + arrowH, arrowTop + arrowH },
                    3);
            g.setColor(new Color(220, 30, 30));
            g.fillPolygon(arrow);
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(1f));
            g.drawPolygon(arrow);
        }
    }

    public boolean contains(int mx, int my) {
        return bounds.contains(mx, my);
    }

    public Plant.Type getType() {
        return type;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean s) {
        selected = s;
    }

    public void setFrozen(boolean f) {
        frozen = f;
    }

    public void setHovered(boolean h) {
        hovered = h;
    }

    public Rectangle getBounds() {
        return bounds;
    }
}
