package dev.chaosclient;

import net.fabricmc.api.ModInitializer;

public final class ChaosClient implements ModInitializer {
    public static final String MOD_ID = "chaosclient";

    @Override
    public void onInitialize() {
        Config.load();
    }
}
