import java.awt.*;

class ShadowRenderer {

    static void draw(Graphics2D g, int cx, int bottom, int w, float scale, float alpha) {
        int shadowW = (int) (w * scale);
        int shadowH = Math.max(4, (int) (shadowW * 0.28f));
        int sx = cx - shadowW / 2;
        int sy = bottom - shadowH / 2;

        g.setColor(new Color(0, 0, 0, (int) (45 * alpha)));
        g.fillOval(sx - 2, sy, shadowW + 4, shadowH + 2);

        g.setColor(new Color(0, 0, 0, (int) (80 * alpha)));
        g.fillOval(sx + 3, sy + 1, shadowW - 6, shadowH - 2);
    }

    static void draw(Graphics2D g, int cx, int bottom, int w, float scale) {
        draw(g, cx, bottom, w, scale, 1.0f);
    }
}
