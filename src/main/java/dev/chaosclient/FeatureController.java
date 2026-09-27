package dev.chaosclient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class FeatureController {
    private static long lastAttack;
    private static Vec3 freecamOrigin;

    private FeatureController() {}

    public static void tick(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || mc.gameMode == null) return;
        for (Module module : Config.enabledModules()) {
            switch (module) {
                case FULLBRIGHT -> mc.options.gamma().set(16.0);
                case FREECAM -> freecam(player);
                case SPRINT -> { if (player.input.hasForwardImpulse()) player.setSprinting(true); }
                case FLY -> fly(mc, player);
                case SPEED -> speed(player);
                case NO_FALL -> noFall(player);
                case KILLAURA -> aura(mc, player);
                case AUTO_TOTEM -> autoTotem(mc, player);
                case AUTO_ARMOR -> autoArmor(mc, player);
                case SCAFFOLD -> scaffold(mc, player);
                case AUTO_TOOL -> autoTool(mc, player);
                default -> { }
            }
        }
    }

    private static void freecam(LocalPlayer player) {
        if (freecamOrigin == null) freecamOrigin = player.position();
        player.noPhysics = true;
    }

    private static void fly(Minecraft mc, LocalPlayer player) {
            Vec3 velocity = player.getDeltaMovement();
            double flySpeed = ConfigManager.number("flySpeed");
            double vertical = mc.options.keyJump.isDown() ? flySpeed : mc.options.keyShift.isDown() ? -flySpeed : 0.0;
            player.setDeltaMovement(velocity.x, vertical, velocity.z);
            player.fallDistance = 0.0f;
    }

    private static void speed(LocalPlayer player) {
        if (player.input.hasForwardImpulse()) {
            Vec3 velocity = player.getDeltaMovement();
            double factor = 1.0 + Math.max(0.0, ConfigManager.number("speedMultiplier") - 1.0) * 0.1;
            player.setDeltaMovement(velocity.x * factor, velocity.y, velocity.z * factor);
        }
    }

    private static void noFall(LocalPlayer player) {
        if (player.fallDistance > 2.5f && player.connection != null) {
            player.connection.send(new ServerboundMovePlayerPacket.StatusOnly(true, player.horizontalCollision));
        }
    }

    private static void aura(Minecraft mc, LocalPlayer player) {
        long delay = 1000L / Math.max(1, (int) ConfigManager.number("auraCps"));
        if (System.currentTimeMillis() - lastAttack < delay) return;
        List<LivingEntity> targets = new ArrayList<>();
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof LivingEntity living && entity != player && living.isAlive()
                    && !living.isAlliedTo(player) && player.distanceTo(living) <= ConfigManager.number("auraReach")) {
                targets.add(living);
            }
        }
        targets.sort(Comparator.comparingDouble(player::distanceToSqr));
        int count = Math.min((int) ConfigManager.number("auraTargets"), targets.size());
        for (int i = 0; i < count; i++) {
            LivingEntity target = targets.get(i);
            float[] rotation = rotations(player.getEyePosition(), target.getEyePosition());
            if (player.connection != null) {
                player.connection.send(new ServerboundMovePlayerPacket.Rot(rotation[0], rotation[1], player.onGround(), player.horizontalCollision));
            }
            mc.gameMode.attack(player, target);
            player.swing(InteractionHand.MAIN_HAND);
        }
        if (count > 0) lastAttack = System.currentTimeMillis();
    }

    private static float[] rotations(Vec3 from, Vec3 to) {
        Vec3 delta = to.subtract(from);
        double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        return new float[]{(float) Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90.0f,
                (float) -Math.toDegrees(Math.atan2(delta.y, horizontal))};
    }

    private static void autoTotem(Minecraft mc, LocalPlayer player) {
        Inventory inventory = player.getInventory();
        if (player.getOffhandItem().getItem() != Items.TOTEM_OF_UNDYING) {
            for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
                if (inventory.getItem(slot).is(Items.TOTEM_OF_UNDYING)) {
                    int menuSlot = slot < 9 ? slot + 36 : slot;
                    mc.gameMode.handleContainerInput(player.inventoryMenu.containerId, menuSlot, 40, ContainerInput.SWAP, player);
                    break;
                }
            }
        }
    }

    private static void autoArmor(Minecraft mc, LocalPlayer player) {
        Inventory inventory = player.getInventory();
            for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
                ItemStack stack = inventory.getItem(slot);
                Equippable armor = stack.get(DataComponents.EQUIPPABLE);
                if (armor != null && armor.slot().isArmor()) {
                    EquipmentSlot target = armor.slot();
                    if (player.getItemBySlot(target).isEmpty()) {
                        int menuSlot = slot < 9 ? slot + 36 : slot;
                        mc.gameMode.handleContainerInput(player.inventoryMenu.containerId, menuSlot, 0, ContainerInput.QUICK_MOVE, player);
                    }
                }
            }
    }

    private static void scaffold(Minecraft mc, LocalPlayer player) {
        if (!player.getMainHandItem().getItem().equals(Items.AIR)) {
            BlockPos below = player.blockPosition().below();
            if (mc.level.getBlockState(below).isAir()) {
                int blockSlot = findBlock(player.getInventory());
                if (blockSlot >= 0) {
                    player.getInventory().setSelectedSlot(blockSlot);
                    BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(below), Direction.UP, below.below(), false);
                    mc.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hit);
                }
            }
        }
    }

    private static void autoTool(Minecraft mc, LocalPlayer player) {
        if (mc.hitResult instanceof BlockHitResult hit) {
            float best = 1.0f;
            int bestSlot = player.getInventory().getSelectedSlot();
            for (int slot = 0; slot < 9; slot++) {
                ItemStack stack = player.getInventory().getItem(slot);
                float speed = stack.getDestroySpeed(mc.level.getBlockState(hit.getBlockPos()));
                if (speed > best) { best = speed; bestSlot = slot; }
            }
            player.getInventory().setSelectedSlot(bestSlot);
        }
    }

    private static int findBlock(Inventory inventory) {
        for (int i = 0; i < 9; i++) {
            if (inventory.getItem(i).getItem() instanceof BlockItem) return i;
        }
        return -1;
    }

    public static boolean freecamActive() { return Config.enabled(Module.FREECAM); }

    public static void onDisabled(Module module) {
        if (module == Module.FREECAM && freecamOrigin != null) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) { mc.player.setPos(freecamOrigin); mc.player.noPhysics = false; }
            freecamOrigin = null;
        }
    }
}
