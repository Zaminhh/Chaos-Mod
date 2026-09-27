package dev.chaosclient.gui;

import dev.chaosclient.ConfigManager;
import dev.chaosclient.Module;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.lwjgl.glfw.GLFW;

public final class SettingColor implements SettingWidget {
    private final Module module;
    private int x, y, width;
    private boolean open, editingHex;
    private String hex = "";
    public SettingColor(Module module) { this.module = module; }
    @Override public int height() { return open ? 76 : 19; }
    @Override public void render(GuiGraphicsExtractor g, Font font, int x, int y, int width, int mouseX, int mouseY, float alpha) {
        this.x = x; this.y = y; this.width = width;
        int color = ConfigManager.color(module);
        g.text(font, "Color", x, y + 5, 0xFFE0E0E0, false);
        g.fill(x + width - 31, y + 3, x + width - 16, y + 16, color);
        g.text(font, open ? "▲" : "▼", x + width - 10, y + 5, 0xFFCBB8FF, false);
        if (!open) return;
        int[] rgb = {(color >> 16) & 255, (color >> 8) & 255, color & 255};
        String[] names = {"R", "G", "B"}; int[] channelColors = {0xFFFF5555, 0xFF55FF55, 0xFF5599FF};
        for (int i = 0; i < 3; i++) {
            int rowY = y + 21 + i * 14;
            g.text(font, names[i], x, rowY + 2, 0xFFE0E0E0, false);
            int sx = x + 14, sw = width - 16;
            g.fill(sx, rowY + 5, sx + sw, rowY + 8, 0xFF2F2B36);
            g.fill(sx, rowY + 5, sx + (sw * rgb[i] / 255), rowY + 8, channelColors[i]);
        }
        String shown = editingHex ? hex : String.format("#%06X", color & 0xFFFFFF);
        GuiDraw.rounded(g, x + width - 68, y + 62, 68, 14, editingHex ? 0xFF6741A8 : 0xFF332D3F);
        g.text(font, shown, x + width - 64, y + 65, 0xFFFFFFFF, false);
    }
    @Override public boolean mouseClicked(MouseButtonEvent event) {
        if (event.x() < x || event.x() > x + width) return false;
        if (event.y() >= y && event.y() <= y + 19) { open = !open; editingHex = false; return true; }
        if (!open) return false;
        if (event.y() >= y + 62 && event.y() <= y + 76) { editingHex = true; hex = String.format("%06X", ConfigManager.color(module) & 0xFFFFFF); return true; }
        for (int i = 0; i < 3; i++) {
            int rowY = y + 21 + i * 14;
            if (event.y() >= rowY && event.y() <= rowY + 12) { setChannel(i, event.x()); return true; }
        }
        return false;
    }
    @Override public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (!open) return false;
        for (int i = 0; i < 3; i++) { int rowY = y + 21 + i * 14; if (event.y() >= rowY - 4 && event.y() <= rowY + 14) { setChannel(i, event.x()); return true; } }
        return false;
    }
    private void setChannel(int channel, double mouseX) {
        int sx = x + 14, sw = width - 16;
        int value = (int) Math.round(Math.max(0, Math.min(1, (mouseX - sx) / sw)) * 255);
        int color = ConfigManager.color(module); int shift = 16 - channel * 8;
        ConfigManager.setColor(module, (color & ~(255 << shift)) | (value << shift));
    }
    @Override public boolean keyPressed(KeyEvent event) {
        if (!editingHex) return false;
        if (event.key() == GLFW.GLFW_KEY_BACKSPACE && !hex.isEmpty()) hex = hex.substring(0, hex.length() - 1);
        if (event.key() == GLFW.GLFW_KEY_ENTER) { applyHex(); editingHex = false; }
        if (event.key() == GLFW.GLFW_KEY_ESCAPE) editingHex = false;
        return true;
    }
    @Override public boolean charTyped(CharacterEvent event) {
        if (!editingHex || hex.length() >= 6 || Character.digit(event.codepoint(), 16) < 0) return false;
        hex += Character.toUpperCase((char) event.codepoint()); return true;
    }
    private void applyHex() { try { ConfigManager.setColor(module, 0xFF000000 | Integer.parseInt(hex, 16)); } catch (NumberFormatException ignored) {} }
}
