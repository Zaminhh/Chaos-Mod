package dev.chaosclient.gui;

import dev.chaosclient.ConfigManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;

import java.util.List;

public final class SettingMode implements SettingWidget {
    private final String label, key;
    private final List<String> values;
    private int x, y, width;
    public SettingMode(String label, String key, String... values) { this.label = label; this.key = key; this.values = List.of(values); }
    @Override public int height() { return 19; }
    @Override public void render(GuiGraphicsExtractor g, Font font, int x, int y, int width, int mouseX, int mouseY, float alpha) {
        this.x = x; this.y = y; this.width = width;
        g.text(font, label, x, y + 5, 0xFFE0E0E0, false);
        String value = ConfigManager.mode(key);
        int boxWidth = Math.max(58, font.width(value) + 16);
        GuiDraw.rounded(g, x + width - boxWidth, y + 2, boxWidth, 15, 0xFF332D3F);
        g.text(font, "‹ " + value + " ›", x + width - boxWidth + 5, y + 5, 0xFFCBB8FF, false);
    }
    @Override public boolean mouseClicked(MouseButtonEvent event) {
        if (event.x() < x || event.x() > x + width || event.y() < y || event.y() > y + height()) return false;
        String current = ConfigManager.mode(key); int index = Math.max(0, values.indexOf(current));
        index = Math.floorMod(index + (event.button() == 1 ? -1 : 1), values.size());
        ConfigManager.setMode(key, values.get(index)); return true;
    }
}
