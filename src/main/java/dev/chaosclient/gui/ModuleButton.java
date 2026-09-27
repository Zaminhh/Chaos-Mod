package dev.chaosclient.gui;

import dev.chaosclient.ConfigManager;
import dev.chaosclient.Module;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

import java.util.ArrayList;
import java.util.List;

public final class ModuleButton {
    public static final int HEADER_HEIGHT = 22;
    private final Module module;
    private final List<SettingWidget> settings = new ArrayList<>();
    private int x, y, width;

    public ModuleButton(Module module) {
        this.module = module;
        switch (module) {
            case ESP -> { settings.add(new SettingMode("Box", "espBox", "OUTLINE", "2D", "BOTH")); settings.add(new SettingToggle("Players", "espPlayers")); settings.add(new SettingToggle("Mobs", "espMobs")); settings.add(new SettingToggle("Items", "espItems")); settings.add(new SettingToggle("Storage", "espStorage")); settings.add(new SettingColor(module)); }
            case XRAY -> settings.add(new SettingColor(module));
            case FLY -> settings.add(new SettingSlider("Speed", "flySpeed", 0.1, 3.0, 0.05));
            case SPEED -> settings.add(new SettingSlider("Multiplier", "speedMultiplier", 1.0, 3.0, 0.05));
            case KILLAURA -> { settings.add(new SettingSlider("Reach", "auraReach", 3.0, 6.0, 0.1)); settings.add(new SettingSlider("CPS", "auraCps", 1, 20, 1)); settings.add(new SettingSlider("Targets", "auraTargets", 1, 8, 1)); settings.add(new SettingMode("Sort", "auraSort", "DISTANCE", "HEALTH", "ANGLE")); }
            case REACH -> settings.add(new SettingSlider("Distance", "reachDistance", 3.0, 6.0, 0.1));
            default -> {}
        }
        settings.add(new SettingKeybind(module));
    }

    public Module module() { return module; }
    public int height() { return HEADER_HEIGHT + (ConfigManager.expanded(module) ? settings.stream().mapToInt(SettingWidget::height).sum() + 6 : 0); }

    public void render(GuiGraphicsExtractor g, Font font, int x, int y, int width, int mouseX, int mouseY, float alpha) {
        this.x = x; this.y = y; this.width = width;
        int background = ConfigManager.enabled(module) ? 0xE2472F66 : 0xD624202B;
        GuiDraw.rounded(g, x, y, width, HEADER_HEIGHT, background);
        GuiDraw.border(g, x, y, width, HEADER_HEIGHT, ConfigManager.enabled(module) ? 0xFF8B5CF6 : 0xFF403949);
        g.text(font, module.name(), x + 8, y + 7, ConfigManager.enabled(module) ? 0xFFFFFFFF : 0xFFC4BEC9, false);
        g.text(font, ConfigManager.expanded(module) ? "▼" : "▶", x + width - 13, y + 7, 0xFFCBB8FF, false);
        if (!ConfigManager.expanded(module)) return;
        int sy = y + HEADER_HEIGHT + 4;
        g.fill(x + 7, sy, x + 8, y + height() - 3, 0xFF5A476F);
        for (SettingWidget setting : settings) { setting.render(g, font, x + 14, sy, width - 20, mouseX, mouseY, alpha); sy += setting.height(); }
    }

    public boolean hovered(double mx, double my) { return mx >= x && mx <= x + width && my >= y && my <= y + HEADER_HEIGHT; }
    public boolean mouseClicked(MouseButtonEvent event) {
        if (hovered(event.x(), event.y())) {
            if (event.button() == 1 || event.x() > x + width - 25) ConfigManager.setExpanded(module, !ConfigManager.expanded(module));
            else if (event.button() == 0) ConfigManager.toggle(module);
            return true;
        }
        if (ConfigManager.expanded(module)) for (SettingWidget setting : settings) if (setting.mouseClicked(event)) return true;
        return false;
    }
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) { for (SettingWidget setting : settings) if (setting.mouseDragged(event, dx, dy)) return true; return false; }
    public boolean mouseReleased(MouseButtonEvent event) { for (SettingWidget setting : settings) if (setting.mouseReleased(event)) return true; return false; }
    public boolean keyPressed(KeyEvent event) { for (SettingWidget setting : settings) if (setting.keyPressed(event)) return true; return false; }
    public boolean charTyped(CharacterEvent event) { for (SettingWidget setting : settings) if (setting.charTyped(event)) return true; return false; }
}
