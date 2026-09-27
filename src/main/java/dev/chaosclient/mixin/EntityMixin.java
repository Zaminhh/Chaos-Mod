package dev.chaosclient.mixin;

import dev.chaosclient.Config;
import dev.chaosclient.Module;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "maxUpStep", at = @At("HEAD"), cancellable = true)
    private void chaosclient$step(CallbackInfoReturnable<Float> cir) {
        if ((Object) this instanceof LocalPlayer && Config.enabled(Module.STEP)) cir.setReturnValue(1.25f);
    }
}
