package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(ServerWorld.class)
public abstract class ServerWorldEndPlatformMixin {

    // Flat clearing radius - everything inside this is fully opened up so the
    // platform reads as floating, not tucked into a carved pocket.
    private static final int MCSREASYMODE$CLEAR_RADIUS = 14;
    // Width of the jittered boundary band, in blocks, where the edge breaks up
    // into a ragged natural-looking line instead of a hard circular cutoff.
    private static final int MCSREASYMODE$EDGE_JITTER = 3;
    private static final int MCSREASYMODE$CLEAR_ABOVE = 40; // headroom above platform
    private static final int MCSREASYMODE$CLEAR_BELOW = 6;  // small clearance below, so it isn't embedded in a floor either
    private static final int MCSREASYMODE$MAX_CLEAR_BELOW = 30; // safety cap for downward carve
    private static final int MCSREASYMODE$AIR_STREAK_TO_STOP = 4;
    @Inject(method = "createEndSpawnPlatform", at = @At("TAIL"))
    private static void mcsreasymode$clearBuriedEndSpawn(ServerWorld world, CallbackInfo ci) {
        if (!Mcsreasymode.CONFIG.preventBuriedEndSpawn) {
            return;
        }

        BlockPos spawn = ServerWorld.END_SPAWN_POS;
        int baseX = spawn.getX();
        int baseY = spawn.getY() - 2;
        int baseZ = spawn.getZ();
        Random random = world.random;

        int scanRadius = MCSREASYMODE$CLEAR_RADIUS + MCSREASYMODE$EDGE_JITTER;

        for (int dx = -scanRadius; dx <= scanRadius; dx++) {
            for (int dz = -scanRadius; dz <= scanRadius; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > MCSREASYMODE$CLEAR_RADIUS - MCSREASYMODE$EDGE_JITTER) {
                    if (dist > MCSREASYMODE$CLEAR_RADIUS + MCSREASYMODE$EDGE_JITTER) {
                        continue;
                    }
                    double edgeChance = 1.0 - ((dist - (MCSREASYMODE$CLEAR_RADIUS - MCSREASYMODE$EDGE_JITTER)) / (MCSREASYMODE$EDGE_JITTER * 2.0));
                    if (random.nextDouble() > edgeChance) {
                        continue;
                    }
                }

                int x = baseX + dx;
                int z = baseZ + dz;
                boolean insidePlatform = dx >= -2 && dx <= 2 && dz >= -2 && dz <= 2;

                // Clear above (unchanged): dynamic scan up to full headroom.
                for (int y = baseY + 1; y <= baseY + MCSREASYMODE$CLEAR_ABOVE; y++) {
                    if (!world.isAir(new BlockPos(x, y, z))) {
                        world.setBlockState(new BlockPos(x, y, z), Blocks.AIR.getDefaultState());
                    }
                }

                // Clear below: dynamic downward scan so a floating look holds even
                // under deep terrain, stopping once real open space is reached.
                int airStreak = 0;
                for (int y = baseY - 1; y >= baseY - MCSREASYMODE$MAX_CLEAR_BELOW; y--) {
                    if (insidePlatform && y == baseY) {
                        continue; // never touch the obsidian itself (unreachable here, y < baseY already, kept for clarity)
                    }
                    BlockPos pos = new BlockPos(x, y, z);
                    if (world.isAir(pos)) {
                        airStreak++;
                        if (airStreak >= MCSREASYMODE$AIR_STREAK_TO_STOP) {
                            break;
                        }
                    } else {
                        world.setBlockState(pos, Blocks.AIR.getDefaultState());
                        airStreak = 0;
                    }
                }
            }
        }
    }
}