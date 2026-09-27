package dev.chaosclient.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class GuiDraw {
    private GuiDraw() {}

    public static void rounded(GuiGraphicsExtractor g, int x, int y, int w, int h, int color) {
        g.fill(x + 2, y, x + w - 2, y + h, color);
        g.fill(x, y + 2, x + w, y + h - 2, color);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, color);
    }

    public static void border(GuiGraphicsExtractor g, int x, int y, int w, int h, int color) {
        g.horizontalLine(x + 2, x + w - 3, y, color);
        g.horizontalLine(x + 2, x + w - 3, y + h - 1, color);
        g.verticalLine(x, y + 2, y + h - 3, color);
        g.verticalLine(x + w - 1, y + 2, y + h - 3, color);
    }
}
