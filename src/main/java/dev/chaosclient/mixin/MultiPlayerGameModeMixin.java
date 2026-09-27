package dev.chaosclient.mixin;

import dev.chaosclient.Config;
import dev.chaosclient.Module;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {
    @Shadow private float destroyProgress;
    @Shadow private int destroyDelay;

    @Inject(method = "tick", at = @At("HEAD"))
    private void chaosclient$fastBreak(CallbackInfo ci) {
        if (Config.enabled(Module.FAST_BREAK)) {
            destroyDelay = 0;
            if (destroyProgress > 0.15f) destroyProgress = 1.0f;
        }
    }
}
