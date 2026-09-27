package dev.chaosclient.mixin;

import dev.chaosclient.Config;
import dev.chaosclient.Module;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow protected int missTime;

    @Inject(method = "tick", at = @At("HEAD"))
    private void chaosclient$fastPlace(CallbackInfo ci) {
        if (Config.enabled(Module.FAST_PLACE)) missTime = 0;
    }
}
