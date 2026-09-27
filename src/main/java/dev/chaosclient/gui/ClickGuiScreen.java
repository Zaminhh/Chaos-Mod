package dev.chaosclient.gui;

import dev.chaosclient.ConfigManager;
import dev.chaosclient.Module;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class ClickGuiScreen extends Screen {
    private static final int PANEL_WIDTH = 430;
    private static final int PANEL_HEIGHT = 310;
    private static final int HEADER_HEIGHT = 28;
    private static final int SIDEBAR_WIDTH = 96;
    private static final int SEARCH_HEIGHT = 24;
    private final List<ModuleButton> buttons = new ArrayList<>();
    private final Set<String> categories = new LinkedHashSet<>();
    private final long openedAt = System.nanoTime();
    private int panelX, panelY;
    private double scroll;
    private double maxScroll;
    private boolean draggingPanel;
    private double panelGrabX, panelGrabY;
    private boolean searchFocused;
    private String search = "";

    public ClickGuiScreen() {
        super(Component.literal("ChaosClient"));
        panelX = ConfigManager.guiX();
        panelY = ConfigManager.guiY();
        for (Module module : Module.values()) { buttons.add(new ModuleButton(module)); categories.add(module.category); }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);
        panelX = Math.max(4, Math.min(panelX, width - PANEL_WIDTH - 4));
        panelY = Math.max(4, Math.min(panelY, height - PANEL_HEIGHT - 4));
        float alpha = Math.min(1.0f, (System.nanoTime() - openedAt) / 150_000_000.0f);

        GuiDraw.rounded(g, panelX + 4, panelY + 5, PANEL_WIDTH, PANEL_HEIGHT, withAlpha(0x72000000, alpha));
        GuiDraw.rounded(g, panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT, withAlpha(0xF016131C, alpha));
        GuiDraw.border(g, panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT, withAlpha(0xFF544764, alpha));
        GuiDraw.rounded(g, panelX, panelY, PANEL_WIDTH, HEADER_HEIGHT, withAlpha(0xFF211B2A, alpha));
        g.text(font, "CHAOSCLIENT", panelX + 10, panelY + 10, withAlpha(0xFFE6DCFF, alpha), true);

        int sidebarX = panelX + 7;
        int sidebarY = panelY + HEADER_HEIGHT + 8;
        int categoryIndex = 0;
        for (String category : categories) {
            int tabY = sidebarY + categoryIndex++ * 25;
            boolean selected = category.equals(ConfigManager.selectedCategory());
            if (selected) GuiDraw.rounded(g, sidebarX, tabY, SIDEBAR_WIDTH - 12, 21, withAlpha(0xFF56397A, alpha));
            g.text(font, category, sidebarX + 8, tabY + 7, withAlpha(selected ? 0xFFFFFFFF : 0xFF9E96A8, alpha), false);
        }

        int listX = panelX + SIDEBAR_WIDTH;
        int listWidth = PANEL_WIDTH - SIDEBAR_WIDTH - 10;
        int searchY = panelY + HEADER_HEIGHT + 6;
        GuiDraw.rounded(g, listX, searchY, listWidth, SEARCH_HEIGHT - 4, withAlpha(searchFocused ? 0xFF342743 : 0xFF26212D, alpha));
        GuiDraw.border(g, listX, searchY, listWidth, SEARCH_HEIGHT - 4, withAlpha(searchFocused ? 0xFF8B5CF6 : 0xFF453B50, alpha));
        String searchText = search.isEmpty() && !searchFocused ? "Search modules…" : search + (searchFocused ? "_" : "");
        g.text(font, searchText, listX + 8, searchY + 6, withAlpha(search.isEmpty() ? 0xFF756E7D : 0xFFEAE5EF, alpha), false);

        int clipTop = searchY + SEARCH_HEIGHT;
        int clipBottom = panelY + PANEL_HEIGHT - 8;
        g.enableScissor(listX, clipTop, listX + listWidth, clipBottom);
        int moduleY = clipTop + 2 - (int) scroll;
        ModuleButton hovered = null;
        String selectedCategory = ConfigManager.selectedCategory();
        String needle = search.toLowerCase(Locale.ROOT);
        for (ModuleButton button : buttons) {
            if (!needle.isEmpty() ? !button.module().name().toLowerCase(Locale.ROOT).contains(needle)
                    : !button.module().category.equals(selectedCategory)) continue;
            button.render(g, font, listX, moduleY, listWidth, mouseX, mouseY, alpha);
            if (button.hovered(mouseX, mouseY) && mouseY >= clipTop && mouseY <= clipBottom) hovered = button;
            moduleY += button.height() + 4;
        }
        g.disableScissor();
        int contentHeight = moduleY + (int) scroll - clipTop;
        maxScroll = Math.max(0, contentHeight - (clipBottom - clipTop));
        scroll = Math.max(0, Math.min(scroll, maxScroll));

        if (hovered != null) {
            String tip = hovered.module().description;
            int tipWidth = font.width(tip) + 10;
            int tipX = Math.min(mouseX + 9, width - tipWidth - 2);
            GuiDraw.rounded(g, tipX, mouseY + 8, tipWidth, 18, 0xF51A1720);
            GuiDraw.border(g, tipX, mouseY + 8, tipWidth, 18, 0xFF5A4B68);
            g.text(font, tip, tipX + 5, mouseY + 13, 0xFFE8E2EC, false);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x(), my = event.y();
        if (event.button() == 0 && mx >= panelX && mx <= panelX + PANEL_WIDTH && my >= panelY && my <= panelY + HEADER_HEIGHT) {
            draggingPanel = true; panelGrabX = mx - panelX; panelGrabY = my - panelY; return true;
        }
        int searchX = panelX + SIDEBAR_WIDTH, searchY = panelY + HEADER_HEIGHT + 6;
        searchFocused = mx >= searchX && mx <= panelX + PANEL_WIDTH - 10 && my >= searchY && my <= searchY + SEARCH_HEIGHT;
        if (searchFocused) return true;
        int index = 0;
        for (String category : categories) {
            int tabY = panelY + HEADER_HEIGHT + 8 + index++ * 25;
            if (event.button() == 0 && mx >= panelX + 7 && mx <= panelX + SIDEBAR_WIDTH - 5 && my >= tabY && my <= tabY + 21) {
                ConfigManager.setSelectedCategory(category); scroll = 0; return true;
            }
        }
        for (ModuleButton button : visibleButtons()) if (button.mouseClicked(event)) return true;
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (draggingPanel) {
            panelX = (int) Math.round(event.x() - panelGrabX); panelY = (int) Math.round(event.y() - panelGrabY);
            ConfigManager.setGuiPosition(panelX, panelY); return true;
        }
        for (ModuleButton button : visibleButtons()) if (button.mouseDragged(event, dragX, dragY)) return true;
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        boolean handled = draggingPanel; draggingPanel = false;
        for (ModuleButton button : visibleButtons()) handled |= button.mouseReleased(event);
        return handled || super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        if (mouseX >= panelX + SIDEBAR_WIDTH && mouseX <= panelX + PANEL_WIDTH && mouseY >= panelY + HEADER_HEIGHT && mouseY <= panelY + PANEL_HEIGHT) {
            scroll = Math.max(0, Math.min(maxScroll, scroll - vertical * 18.0)); return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        for (ModuleButton button : visibleButtons()) if (button.keyPressed(event)) return true;
        if (searchFocused && event.key() == GLFW.GLFW_KEY_BACKSPACE && !search.isEmpty()) { search = search.substring(0, search.length() - 1); scroll = 0; return true; }
        if (searchFocused && event.key() == GLFW.GLFW_KEY_ESCAPE) { searchFocused = false; return true; }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        for (ModuleButton button : visibleButtons()) if (button.charTyped(event)) return true;
        if (searchFocused && event.isAllowedChatCharacter() && search.length() < 32) { search += event.codepointAsString(); scroll = 0; return true; }
        return super.charTyped(event);
    }

    @Override public void removed() { ConfigManager.flushAsync(); super.removed(); }
    @Override public boolean isPauseScreen() { return false; }

    private List<ModuleButton> visibleButtons() {
        String needle = search.toLowerCase(Locale.ROOT); String category = ConfigManager.selectedCategory();
        return buttons.stream().filter(b -> !needle.isEmpty() ? b.module().name().toLowerCase(Locale.ROOT).contains(needle) : b.module().category.equals(category)).toList();
    }
    private static int withAlpha(int color, float alpha) { return ((int) (((color >>> 24) & 255) * alpha) << 24) | (color & 0xFFFFFF); }
}
