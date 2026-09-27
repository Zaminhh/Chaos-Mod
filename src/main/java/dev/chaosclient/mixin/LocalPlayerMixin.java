package dev.chaosclient.mixin;

import dev.chaosclient.Config;
import dev.chaosclient.FeatureController;
import dev.chaosclient.Module;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
    @ModifyExpressionValue(method = "raycastHitResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;entityInteractionRange()D"))
    private double chaosclient$entityReach(double original) {
        return Config.enabled(Module.REACH) ? 4.5 : original;
    }

    @ModifyExpressionValue(method = "raycastHitResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;blockInteractionRange()D"))
    private double chaosclient$blockReach(double original) {
        return Config.enabled(Module.REACH) ? 4.5 : original;
    }

    @ModifyVariable(method = "move", at = @At("HEAD"), argsOnly = true)
    private Vec3 chaosclient$safeWalk(Vec3 movement) {
        LocalPlayer self = (LocalPlayer) (Object) this;
        if (!Config.enabled(Module.SAFE_WALK) || !self.onGround()) return movement;
        if (self.level().getBlockState(self.blockPosition().offset((int) Math.signum(movement.x), -1,
                (int) Math.signum(movement.z))).isAir()) return new Vec3(0.0, movement.y, 0.0);
        return movement;
    }

    @Inject(method = "sendPosition", at = @At("HEAD"), cancellable = true)
    private void chaosclient$freezeServerPosition(CallbackInfo ci) {
        if (FeatureController.freecamActive()) ci.cancel();
    }

    @Inject(method = "isSlowDueToUsingItem", at = @At("HEAD"), cancellable = true)
    private void chaosclient$noSlow(CallbackInfoReturnable<Boolean> cir) {
        if (Config.enabled(Module.NO_SLOW)) cir.setReturnValue(false);
    }

    @Inject(method = "itemUseSpeedMultiplier", at = @At("HEAD"), cancellable = true)
    private void chaosclient$noSlowMultiplier(CallbackInfoReturnable<Float> cir) {
        if (Config.enabled(Module.NO_SLOW)) cir.setReturnValue(1.0f);
    }
}
