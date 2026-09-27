package dev.chaosclient.mixin;

import dev.chaosclient.Config;
import dev.chaosclient.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {
    @Shadow protected AbstractContainerMenu menu;
    private int chaosclient$delay;

    @Inject(method = "containerTick", at = @At("TAIL"))
    private void chaosclient$steal(CallbackInfo ci) {
        if (!Config.enabled(Module.CHEST_STEALER) || chaosclient$delay++ % 2 != 0) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gameMode == null) return;
        int playerSlots = 36;
        int containerSlots = Math.max(0, menu.slots.size() - playerSlots);
        for (int i = 0; i < containerSlots; i++) {
            Slot slot = menu.getSlot(i);
            if (slot.hasItem()) {
                mc.gameMode.handleContainerInput(menu.containerId, i, 0, ContainerInput.QUICK_MOVE, mc.player);
                return;
            }
        }
    }
}
