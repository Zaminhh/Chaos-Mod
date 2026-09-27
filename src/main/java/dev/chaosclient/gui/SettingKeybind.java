package dev.chaosclient.gui;

import com.mojang.blaze3d.platform.InputConstants;
import dev.chaosclient.ConfigManager;
import dev.chaosclient.Module;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.lwjgl.glfw.GLFW;

public final class SettingKeybind implements SettingWidget {
    private final Module module;
    private int x, y, width;
    private boolean listening;
    public SettingKeybind(Module module) { this.module = module; }
    @Override public int height() { return 19; }
    @Override public void render(GuiGraphicsExtractor g, Font font, int x, int y, int width, int mouseX, int mouseY, float alpha) {
        this.x = x; this.y = y; this.width = width;
        int key = ConfigManager.keybind(module);
        String value = listening ? "PRESS KEY" : key < 0 ? "NONE" : InputConstants.Type.KEYSYM.getOrCreate(key).getDisplayName().getString();
        g.text(font, "Keybind", x, y + 5, 0xFFE0E0E0, false);
        int boxWidth = Math.max(52, font.width(value) + 10);
        GuiDraw.rounded(g, x + width - boxWidth, y + 2, boxWidth, 15, listening ? 0xFF6741A8 : 0xFF332D3F);
        g.text(font, value, x + width - boxWidth + 5, y + 5, 0xFFCBB8FF, false);
    }
    @Override public boolean mouseClicked(MouseButtonEvent event) {
        if (event.button() == 0 && event.x() >= x && event.x() <= x + width && event.y() >= y && event.y() <= y + height()) { listening = true; return true; }
        return false;
    }
    @Override public boolean keyPressed(KeyEvent event) {
        if (!listening) return false;
        ConfigManager.setKeybind(module, event.key() == GLFW.GLFW_KEY_ESCAPE || event.key() == GLFW.GLFW_KEY_DELETE ? -1 : event.key());
        listening = false; return true;
    }
}
