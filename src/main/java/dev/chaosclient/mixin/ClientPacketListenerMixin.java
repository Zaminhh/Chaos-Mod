package dev.chaosclient.mixin;

import dev.chaosclient.Config;
import dev.chaosclient.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    @Inject(method = "handleSetEntityMotion", at = @At("HEAD"), cancellable = true)
    private void chaosclient$velocity(ClientboundSetEntityMotionPacket packet, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (Config.enabled(Module.VELOCITY) && mc.player != null && packet.id() == mc.player.getId()) ci.cancel();
    }
}
