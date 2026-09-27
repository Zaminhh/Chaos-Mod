package dev.chaosclient.gui;

import dev.chaosclient.ConfigManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;

public final class SettingToggle implements SettingWidget {
    private final String label, key;
    private int x, y, width;
    public SettingToggle(String label, String key) { this.label = label; this.key = key; }
    @Override public int height() { return 18; }
    @Override public void render(GuiGraphicsExtractor g, Font font, int x, int y, int width, int mouseX, int mouseY, float alpha) {
        this.x = x; this.y = y; this.width = width;
        boolean on = ConfigManager.bool(key);
        g.text(font, label, x, y + 4, 0xFFE0E0E0, false);
        GuiDraw.rounded(g, x + width - 25, y + 3, 24, 12, on ? 0xFF805AD5 : 0xFF3A3542);
        g.fill(x + width - (on ? 11 : 23), y + 5, x + width - (on ? 3 : 15), y + 13, 0xFFFFFFFF);
    }
    @Override public boolean mouseClicked(MouseButtonEvent event) {
        if (event.button() == 0 && event.x() >= x && event.x() <= x + width && event.y() >= y && event.y() <= y + height()) {
            ConfigManager.setBool(key, !ConfigManager.bool(key)); return true;
        }
        return false;
    }
}
