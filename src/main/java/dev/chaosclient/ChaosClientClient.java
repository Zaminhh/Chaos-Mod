package dev.chaosclient;

import com.mojang.blaze3d.platform.InputConstants;
import dev.chaosclient.gui.ClickGuiScreen;
import dev.chaosclient.render.ChaosRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import java.util.EnumMap;
import java.util.Map;

public final class ChaosClientClient implements ClientModInitializer {
    private static KeyMapping menuKey;
    private static final Map<Module, Boolean> keyStates = new EnumMap<>(Module.class);
    private static boolean hadWorld;

    @Override
    public void onInitializeClient() {
        menuKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.chaosclient.menu", GLFW.GLFW_KEY_RIGHT_SHIFT, KeyMapping.Category.MISC));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (menuKey.consumeClick()) client.gui.setScreen(new ClickGuiScreen());
            handleModuleKeys(client);
            FeatureController.tick(client);
            ChaosRenderer.render(client);
            ConfigManager.tick();
            if (hadWorld && client.level == null) ConfigManager.flushAsync();
            hadWorld = client.level != null;
        });
    }

    private static void handleModuleKeys(Minecraft client) {
        if (client.gui.screen() != null) return;
        for (Module module : Module.values()) {
            int key = ConfigManager.keybind(module);
            boolean down = key >= 0 && InputConstants.isKeyDown(client.getWindow(), key);
            if (down && !keyStates.getOrDefault(module, false)) ConfigManager.toggle(module);
            keyStates.put(module, down);
        }
    }

    public static boolean active(Module module) {
        return Config.enabled(module);
    }

    public static Minecraft minecraft() {
        return Minecraft.getInstance();
    }
}
