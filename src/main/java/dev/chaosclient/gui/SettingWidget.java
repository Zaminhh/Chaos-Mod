package dev.chaosclient.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

public interface SettingWidget {
    int height();
    void render(GuiGraphicsExtractor graphics, Font font, int x, int y, int width, int mouseX, int mouseY, float alpha);
    default boolean mouseClicked(MouseButtonEvent event) { return false; }
    default boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) { return false; }
    default boolean mouseReleased(MouseButtonEvent event) { return false; }
    default boolean keyPressed(KeyEvent event) { return false; }
    default boolean charTyped(CharacterEvent event) { return false; }
}
