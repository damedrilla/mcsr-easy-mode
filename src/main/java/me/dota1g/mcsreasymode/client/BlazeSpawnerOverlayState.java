package me.dota1g.mcsreasymode.client;

import me.dota1g.mcsreasymode.Mcsreasymode;
import me.dota1g.mcsreasymode.BlazeSpawnerServerState;
import me.dota1g.mcsreasymode.mixin.MobSpawnerLogicAccessor;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class BlazeSpawnerOverlayState {
    private static final DustParticleEffect BLOCKED_DUST = new DustParticleEffect(1.0F, 0.05F, 0.05F, 0.75F);
    private static final DustParticleEffect BRIGHT_DUST = new DustParticleEffect(1.0F, 0.8F, 0.0F, 0.75F);
    private static final long SNAPSHOT_LIFETIME_MILLIS = 1500L;
    private static final int ANALYSIS_INTERVAL_TICKS = 10;
    private static final int PARTICLE_INTERVAL_TICKS = 3;
    private static final int PARTICLES_PER_BATCH = 36;

    private static final Map<Long, Snapshot> SNAPSHOTS = new HashMap<>();
    private static ClientWorld trackedWorld;

    private BlazeSpawnerOverlayState() {
    }

    public static Snapshot observe(MobSpawnerBlockEntity spawner) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld world = client.world;
        if (world == null || !isBlazeSpawner(spawner)) {
            return null;
        }
        if (world != trackedWorld) {
            trackedWorld = world;
            SNAPSHOTS.clear();
        }

        BlockPos pos = spawner.getPos();
        Snapshot snapshot = SNAPSHOTS.computeIfAbsent(pos.asLong(), ignored -> new Snapshot(pos.toImmutable()));
        snapshot.lastSeenMillis = System.currentTimeMillis();
        Integer serverDelay = BlazeSpawnerServerState.get(world.getRegistryKey(), pos);
        int clientDelay = ((MobSpawnerLogicAccessor) spawner.getLogic()).mcsreasymode$getSpawnDelay();
        snapshot.spawnDelay = Math.max(0, serverDelay == null ? clientDelay : serverDelay);
        long tick = world.getTime();
        if (snapshot.lastAnalysisTick == Long.MIN_VALUE || tick - snapshot.lastAnalysisTick >= ANALYSIS_INTERVAL_TICKS) {
            analyze(world, snapshot);
            snapshot.lastAnalysisTick = tick;
            Mcsreasymode.debugRateLimited(
                    "blaze_spawner_overlay." + pos.asLong(),
                    "Blaze spawner overlay at " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()
                            + ": delay " + snapshot.spawnDelay + " ticks, blocked " + snapshot.blocked
                            + ", too bright " + snapshot.tooBright + ", available " + snapshot.available + ".",
                    5000L
            );
        }
        if (Mcsreasymode.shouldShowBlazeSpawnerProblemMarkers()
                && (snapshot.lastParticleTick == Long.MIN_VALUE || tick - snapshot.lastParticleTick >= PARTICLE_INTERVAL_TICKS)) {
            emitProblemParticles(world, snapshot);
            snapshot.lastParticleTick = tick;
        }
        return snapshot;
    }

    public static Snapshot nearest(ClientPlayerEntity player) {
        if (player == null || player.world != trackedWorld) {
            return null;
        }

        long now = System.currentTimeMillis();
        Snapshot nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        Iterator<Snapshot> iterator = SNAPSHOTS.values().iterator();
        while (iterator.hasNext()) {
            Snapshot snapshot = iterator.next();
            if (now - snapshot.lastSeenMillis > SNAPSHOT_LIFETIME_MILLIS) {
                iterator.remove();
                continue;
            }
            double distance = snapshot.pos.getSquaredDistance(player.getX(), player.getY(), player.getZ(), true);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = snapshot;
            }
        }
        return nearest;
    }

    private static boolean isBlazeSpawner(MobSpawnerBlockEntity spawner) {
        Entity rendered = spawner.getLogic().getRenderedEntity();
        return rendered instanceof BlazeEntity;
    }

    private static void analyze(ClientWorld world, Snapshot snapshot) {
        snapshot.markers.clear();
        snapshot.blocked = 0;
        snapshot.tooBright = 0;
        snapshot.available = 0;

        EntityDimensions dimensions = EntityType.BLAZE.getDimensions();
        double halfWidth = dimensions.width / 2.0D;
        BlockPos origin = snapshot.pos;
        for (int yOffset = -1; yOffset <= 1; yOffset++) {
            for (int xOffset = -4; xOffset <= 4; xOffset++) {
                for (int zOffset = -4; zOffset <= 4; zOffset++) {
                    int x = origin.getX() + xOffset;
                    int y = origin.getY() + yOffset;
                    int z = origin.getZ() + zOffset;
                    double centerX = x + 0.5D;
                    double centerZ = z + 0.5D;
                    Box blazeBox = new Box(
                            centerX - halfWidth,
                            y,
                            centerZ - halfWidth,
                            centerX + halfWidth,
                            y + dimensions.height,
                            centerZ + halfWidth
                    );
                    BlockPos candidate = new BlockPos(x, y, z);
                    if (world.getBlockCollisions(null, blazeBox).findAny().isPresent()) {
                        snapshot.blocked++;
                        snapshot.markers.add(new Marker(centerX, blockedMarkerY(world, candidate, dimensions.height), centerZ, MarkerType.BLOCKED));
                    } else if (world.getLightLevel(candidate) > 11) {
                        snapshot.tooBright++;
                        snapshot.markers.add(new Marker(centerX, y + 0.12D, centerZ, MarkerType.TOO_BRIGHT));
                    } else {
                        snapshot.available++;
                    }
                }
            }
        }
    }

    private static void emitProblemParticles(ClientWorld world, Snapshot snapshot) {
        int markerCount = snapshot.markers.size();
        if (markerCount == 0) {
            return;
        }

        int count = Math.min(PARTICLES_PER_BATCH, markerCount);
        for (int i = 0; i < count; i++) {
            Marker marker = snapshot.markers.get(snapshot.particleCursor++ % markerCount);
            DustParticleEffect particle = marker.type == MarkerType.BLOCKED ? BLOCKED_DUST : BRIGHT_DUST;
            world.addParticle(particle, true, marker.x, marker.y, marker.z, 0.0D, 0.0D, 0.0D);
        }
    }

    private static double blockedMarkerY(ClientWorld world, BlockPos candidate, float blazeHeight) {
        int topY = candidate.getY() + Math.max(0, (int) Math.ceil(blazeHeight) - 1);
        BlockPos.Mutable cursor = new BlockPos.Mutable(candidate.getX(), candidate.getY(), candidate.getZ());
        for (int y = candidate.getY(); y <= topY; y++) {
            cursor.setY(y);
            VoxelShape shape = world.getBlockState(cursor).getCollisionShape(world, cursor);
            if (!shape.isEmpty()) {
                return y + shape.getMax(net.minecraft.util.math.Direction.Axis.Y) + 0.04D;
            }
        }
        return candidate.getY() + blazeHeight + 0.04D;
    }

    public static final class Snapshot {
        private final BlockPos pos;
        private final List<Marker> markers = new ArrayList<>();
        private long lastSeenMillis;
        private long lastAnalysisTick = Long.MIN_VALUE;
        private long lastParticleTick = Long.MIN_VALUE;
        private int particleCursor;
        private int spawnDelay;
        private int blocked;
        private int tooBright;
        private int available;

        private Snapshot(BlockPos pos) {
            this.pos = pos;
        }

        public int spawnDelay() {
            return this.spawnDelay;
        }

        public int blocked() {
            return this.blocked;
        }

        public int tooBright() {
            return this.tooBright;
        }

        public int available() {
            return this.available;
        }

        public int total() {
            return this.blocked + this.tooBright + this.available;
        }
    }

    private static final class Marker {
        private final double x;
        private final double y;
        private final double z;
        private final MarkerType type;

        private Marker(double x, double y, double z, MarkerType type) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.type = type;
        }
    }

    private enum MarkerType {
        BLOCKED,
        TOO_BRIGHT
    }
}
