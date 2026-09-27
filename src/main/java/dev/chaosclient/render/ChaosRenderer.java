package dev.chaosclient.render;

import dev.chaosclient.Config;
import dev.chaosclient.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.SimpleGizmoCollector;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class ChaosRenderer {
    private static final Set<Block> XRAY_BLOCKS = Set.of(
            Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE, Blocks.DIAMOND_BLOCK,
            Blocks.ANCIENT_DEBRIS, Blocks.NETHERITE_BLOCK, Blocks.CHEST,
            Blocks.TRAPPED_CHEST, Blocks.ENDER_CHEST, Blocks.SPAWNER, Blocks.TRIAL_SPAWNER);
    private static final List<BlockPos> xrayPositions = new ArrayList<>();
    private static final List<BlockPos> pendingScan = new ArrayList<>();
    private static final SimpleGizmoCollector BATCH = new SimpleGizmoCollector();
    private static final GizmoStyle PLAYER_STYLE = GizmoStyle.stroke(0xFF4DA6FF, 2.0f);
    private static final GizmoStyle MOB_STYLE = GizmoStyle.stroke(0xFFFF5555, 2.0f);
    private static final GizmoStyle ITEM_STYLE = GizmoStyle.stroke(0xFFFFFF55, 2.0f);
    private static BlockPos scanCenter;
    private static int scanIndex;
    private static final int RADIUS = 24;
    private static final int DIAMETER = RADIUS * 2 + 1;
    private static final int SCAN_VOLUME = DIAMETER * DIAMETER * DIAMETER;
    private static final int SCAN_BUDGET = 2048;

    private ChaosRenderer() {}

    public static void render(Minecraft mc) {
        if (mc.level == null || mc.player == null) return;
        scanBlocks(mc);
        try (Gizmos.TemporaryCollection ignored = Gizmos.withCollector(BATCH)) {
            if (Config.enabled(Module.ESP)) renderEntities(mc);
            if (Config.enabled(Module.XRAY) || Config.enabled(Module.ESP)) renderBlocks(mc);
        }
        mc.levelRenderer.addMainThreadGizmos(BATCH.drainGizmos());
    }

    private static void renderEntities(Minecraft mc) {
        Vec3 eye = mc.player.getEyePosition();
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity == mc.player || !wanted(entity)) continue;
            int color = entity instanceof Player ? 0xFF4DA6FF : entity instanceof ItemEntity ? 0xFFFFFF55 : 0xFFFF5555;
            GizmoStyle style = entity instanceof Player ? PLAYER_STYLE : entity instanceof ItemEntity ? ITEM_STYLE : MOB_STYLE;
            AABB box = entity.getBoundingBox().inflate(0.04);
            Gizmos.cuboid(box, style).setAlwaysOnTop().persistForMillis(70);
            Gizmos.line(eye, box.getCenter(), color, 1.25f).setAlwaysOnTop().persistForMillis(70);
            String label = entity.getName().getString() + " " + Math.round(mc.player.distanceTo(entity)) + "m";
            if (entity instanceof LivingEntity living) {
                label += " " + Math.round(living.getHealth()) + "/" + Math.round(living.getMaxHealth()) + "hp";
                double ratio = Math.max(0.0, living.getHealth() / living.getMaxHealth());
                Vec3 base = new Vec3(box.minX - 0.08, box.minY, box.minZ);
                Gizmos.line(base, base.add(0, box.getYsize() * ratio, 0), 0xFF40FF40, 3.0f)
                        .setAlwaysOnTop().persistForMillis(70);
            }
            Gizmos.billboardTextOverMob(entity, 12, label, 0xFFFFFFFF, 0.018f)
                    .setAlwaysOnTop().persistForMillis(70);
        }
    }

    private static boolean wanted(Entity entity) {
        if (entity instanceof Player) return true;
        if (entity instanceof ItemEntity) return true;
        return entity instanceof LivingEntity;
    }

    private static void scanBlocks(Minecraft mc) {
        if (!Config.enabled(Module.XRAY) && !Config.enabled(Module.ESP)) return;
        BlockPos playerPos = mc.player.blockPosition();
        if (scanCenter == null || scanCenter.distSqr(playerPos) > 64.0) {
            scanCenter = playerPos.immutable(); scanIndex = 0; pendingScan.clear();
        }
        int end = Math.min(SCAN_VOLUME, scanIndex + SCAN_BUDGET);
        for (; scanIndex < end; scanIndex++) {
            int x = scanIndex % DIAMETER;
            int y = (scanIndex / DIAMETER) % DIAMETER;
            int z = scanIndex / (DIAMETER * DIAMETER);
            BlockPos pos = scanCenter.offset(x - RADIUS, y - RADIUS, z - RADIUS);
            if (XRAY_BLOCKS.contains(mc.level.getBlockState(pos).getBlock())) pendingScan.add(pos);
        }
        if (scanIndex >= SCAN_VOLUME) {
            xrayPositions.clear(); xrayPositions.addAll(pendingScan);
            pendingScan.clear(); scanIndex = 0; scanCenter = playerPos.immutable();
        }
    }

    private static void renderBlocks(Minecraft mc) {
        for (BlockPos pos : xrayPositions) {
            Block block = mc.level.getBlockState(pos).getBlock();
            boolean storage = block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST || block == Blocks.ENDER_CHEST;
            boolean spawner = block == Blocks.SPAWNER || block == Blocks.TRIAL_SPAWNER;
            if (!Config.enabled(Module.XRAY) && !storage && !spawner) continue;
            int color = spawner ? 0xFFFF44FF : storage ? 0xFFFFAA22 : 0xFF00FFFF;
            Gizmos.cuboid(pos, GizmoStyle.strokeAndFill(color, 1.5f, color & 0x22FFFFFF))
                    .setAlwaysOnTop().persistForMillis(70);
        }
    }
}
