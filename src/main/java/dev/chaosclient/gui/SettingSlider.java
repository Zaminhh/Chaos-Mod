package dev.chaosclient.gui;

import dev.chaosclient.ConfigManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;

import java.util.Locale;

public final class SettingSlider implements SettingWidget {
    private final String label;
    private final String key;
    private final double min;
    private final double max;
    private final double step;
    private int trackX, trackY, trackWidth, knobX;
    private boolean dragging;
    private double grabOffset;

    public SettingSlider(String label, String key, double min, double max, double step) {
        this.label = label; this.key = key; this.min = min; this.max = max; this.step = step;
    }

    @Override public int height() { return 25; }

    @Override
    public void render(GuiGraphicsExtractor g, Font font, int x, int y, int width, int mouseX, int mouseY, float alpha) {
        double value = ConfigManager.number(key);
        g.text(font, label, x, y + 2, 0xFFE0E0E0, false);
        String text = step >= 1.0 ? Integer.toString((int) Math.round(value)) : String.format(Locale.ROOT, "%.2f", value);
        g.text(font, text, x + width - font.width(text), y + 2, 0xFFB69CFF, false);
        trackX = x; trackY = y + 16; trackWidth = width;
        double ratio = (value - min) / (max - min);
        knobX = trackX + (int) Math.round(ratio * trackWidth);
        g.fill(trackX, trackY, trackX + trackWidth, trackY + 3, 0xFF302B3B);
        g.fill(trackX, trackY, knobX, trackY + 3, 0xFF8B5CF6);
        g.fill(knobX - 2, trackY - 2, knobX + 3, trackY + 6, 0xFFE7DEFF);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event) {
        if (event.button() != 0 || event.x() < trackX - 3 || event.x() > trackX + trackWidth + 3 || event.y() < trackY - 5 || event.y() > trackY + 8) return false;
        dragging = true;
        grabOffset = event.x() - knobX;
        return true;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (!dragging) return false;
        double ratio = Math.max(0.0, Math.min(1.0, (event.x() - grabOffset - trackX) / trackWidth));
        double raw = min + ratio * (max - min);
        ConfigManager.setNumber(key, Math.max(min, Math.min(max, Math.round(raw / step) * step)));
        return true;
    }

    @Override public boolean mouseReleased(MouseButtonEvent event) { boolean was = dragging; dragging = false; return was; }
}
