package me.dota1g.mcsreasymode;

import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.EndPortalFrameBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class StrongholdProtection {
    private static final int SPAWNER_CLEANUP_CHUNK_RADIUS = 16;
    private static final String SILVERFISH_ID = "minecraft:silverfish";
    private static final ThreadLocal<Map<Long, Snapshot>> ACTIVE_SNAPSHOTS = new ThreadLocal<>();
    private static final ThreadLocal<ChunkPos> ACTIVE_STARTER_CHUNK = new ThreadLocal<>();
    private static final Set<ChunkPos> KNOWN_STARTER_CHUNKS = Collections.synchronizedSet(new HashSet<>());
    private static final Set<Long> KNOWN_SILVERFISH_SPAWNERS = Collections.synchronizedSet(new HashSet<>());

    private StrongholdProtection() {
    }

    public static void reset() {
        ACTIVE_SNAPSHOTS.remove();
        ACTIVE_STARTER_CHUNK.remove();
        KNOWN_STARTER_CHUNKS.clear();
        KNOWN_SILVERFISH_SPAWNERS.clear();
    }

    public static void beginFeatureGeneration() {
        if (Mcsreasymode.isStrongholdAntiCorruptionEnabled()) {
            ACTIVE_SNAPSHOTS.set(new LinkedHashMap<>());
        }
    }

    public static void capturePiece(ServerWorldAccess world, BlockBox pieceBox, BlockBox generationBox) {
        Map<Long, Snapshot> snapshots = ACTIVE_SNAPSHOTS.get();
        if (snapshots == null) {
            return;
        }

        // Iron-door entrance buttons sit one block outside the owning piece box.
        int minX = Math.max(pieceBox.minX - 1, generationBox.minX);
        int minY = Math.max(pieceBox.minY, generationBox.minY);
        int minZ = Math.max(pieceBox.minZ - 1, generationBox.minZ);
        int maxX = Math.min(pieceBox.maxX + 1, generationBox.maxX);
        int maxY = Math.min(pieceBox.maxY, generationBox.maxY);
        int maxZ = Math.min(pieceBox.maxZ + 1, generationBox.maxZ);
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    mutable.set(x, y, z);
                    BlockState state = world.getBlockState(mutable);
                    BlockEntity blockEntity = world.getBlockEntity(mutable);
                    CompoundTag blockEntityTag = blockEntity == null ? null : blockEntity.toTag(new CompoundTag());
                    snapshots.put(mutable.asLong(), new Snapshot(state, blockEntityTag));
                }
            }
        }
    }

    public static void captureStarterStaircase(BlockBox pieceBox) {
        if (ACTIVE_SNAPSHOTS.get() == null) {
            return;
        }

        int centerX = (pieceBox.minX + pieceBox.maxX) / 2;
        int centerZ = (pieceBox.minZ + pieceBox.maxZ) / 2;
        ChunkPos starterChunk = new ChunkPos(new BlockPos(centerX, pieceBox.minY, centerZ));
        ACTIVE_STARTER_CHUNK.set(starterChunk);
        KNOWN_STARTER_CHUNKS.add(starterChunk);
        Mcsreasymode.debugRateLimited(
                "stronghold-starter-tracked",
                "Stronghold starter stairs tracked at chunk " + starterChunk.x + ", " + starterChunk.z + ".",
                5000L
        );
    }

    public static void capturePortalRoomSpawners(ServerWorldAccess world, BlockBox pieceBox, BlockBox generationBox) {
        if (ACTIVE_SNAPSHOTS.get() == null) {
            return;
        }

        int minX = Math.max(pieceBox.minX, generationBox.minX);
        int minY = Math.max(pieceBox.minY, generationBox.minY);
        int minZ = Math.max(pieceBox.minZ, generationBox.minZ);
        int maxX = Math.min(pieceBox.maxX, generationBox.maxX);
        int maxY = Math.min(pieceBox.maxY, generationBox.maxY);
        int maxZ = Math.min(pieceBox.maxZ, generationBox.maxZ);
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    mutable.set(x, y, z);
                    if (world.getBlockState(mutable).isOf(Blocks.SPAWNER)) {
                        BlockPos spawnerPos = new BlockPos(mutable);
                        KNOWN_SILVERFISH_SPAWNERS.add(spawnerPos.asLong());
                        repairSilverfishSpawner(world, spawnerPos);
                        Mcsreasymode.debugRateLimited(
                                "stronghold-silverfish-spawner-tracked",
                                "Stronghold silverfish spawner tracked and repaired at "
                                        + spawnerPos.getX() + ", " + spawnerPos.getY() + ", " + spawnerPos.getZ() + ".",
                                5000L
                        );
                    }
                }
            }
        }
    }

    public static void completeSsgPortalRoom(ServerWorldAccess world, BlockBox pieceBox, BlockBox generationBox) {
        if (!Mcsreasymode.isSsgModeEnabled()) {
            return;
        }

        int minX = Math.max(pieceBox.minX, generationBox.minX);
        int minY = Math.max(pieceBox.minY, generationBox.minY);
        int minZ = Math.max(pieceBox.minZ, generationBox.minZ);
        int maxX = Math.min(pieceBox.maxX, generationBox.maxX);
        int maxY = Math.min(pieceBox.maxY, generationBox.maxY);
        int maxZ = Math.min(pieceBox.maxZ, generationBox.maxZ);
        List<BlockPos> frames = new ArrayList<>();
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    mutable.set(x, y, z);
                    BlockState state = world.getBlockState(mutable);
                    if (state.isOf(Blocks.END_PORTAL_FRAME)) {
                        frames.add(new BlockPos(mutable));
                        if (!state.get(EndPortalFrameBlock.EYE)) {
                            world.setBlockState(mutable, state.with(EndPortalFrameBlock.EYE, true), 2);
                        }
                    }
                }
            }
        }

        if (frames.size() < 12) {
            return;
        }

        int frameMinX = frames.stream().mapToInt(BlockPos::getX).min().orElse(minX);
        int frameMaxX = frames.stream().mapToInt(BlockPos::getX).max().orElse(maxX);
        int frameMinZ = frames.stream().mapToInt(BlockPos::getZ).min().orElse(minZ);
        int frameMaxZ = frames.stream().mapToInt(BlockPos::getZ).max().orElse(maxZ);
        int frameY = frames.get(0).getY();

        for (int x = frameMinX + 1; x <= frameMaxX - 1; x++) {
            for (int z = frameMinZ + 1; z <= frameMaxZ - 1; z++) {
                mutable.set(x, frameY, z);
                if (!world.getBlockState(mutable).isOf(Blocks.END_PORTAL_FRAME)) {
                    world.setBlockState(mutable, Blocks.END_PORTAL.getDefaultState(), 2);
                }
            }
        }

        Mcsreasymode.debugRateLimited(
                "ssg-portal-room",
                "SSG Mode completed stronghold portal room with 12 eyes.",
                5000L
        );
    }

    public static void restoreAndEnd(ChunkRegion region) {
        Map<Long, Snapshot> snapshots = ACTIVE_SNAPSHOTS.get();
        if (snapshots == null) {
            cleanupKnownStrongholdSpawners(region);
            repairKnownSilverfishSpawners(region);
            return;
        }

        try {
            int changedBlocks = 0;
            for (Map.Entry<Long, Snapshot> entry : snapshots.entrySet()) {
                BlockPos pos = BlockPos.fromLong(entry.getKey());
                Snapshot snapshot = entry.getValue();
                if (isKnownSilverfishSpawner(pos) && region.getBlockState(pos).isOf(Blocks.SPAWNER)) {
                    continue;
                }
                if (!region.getBlockState(pos).equals(snapshot.state)) {
                    changedBlocks++;
                }
                region.setBlockState(pos, snapshot.state, 2);
            }
            for (Map.Entry<Long, Snapshot> entry : snapshots.entrySet()) {
                Snapshot snapshot = entry.getValue();
                if (snapshot.blockEntityTag == null) {
                    continue;
                }
                BlockPos pos = BlockPos.fromLong(entry.getKey());
                if (isKnownSilverfishSpawner(pos)) {
                    repairSilverfishSpawner(region, pos);
                    continue;
                }
                BlockEntity blockEntity = region.getBlockEntity(pos);
                if (blockEntity != null) {
                    blockEntity.fromTag(snapshot.state, snapshot.blockEntityTag.copy());
                    blockEntity.markDirty();
                }
            }
            if (!snapshots.isEmpty()) {
                Mcsreasymode.debugRateLimited(
                        "stronghold-anti-corruption",
                        "Stronghold anti-corruption protected " + snapshots.size()
                                + " blocks and restored " + changedBlocks + " later worldgen changes.",
                        5000L
                );
            }
            ChunkPos starterChunk = ACTIVE_STARTER_CHUNK.get();
            if (starterChunk != null) {
                KNOWN_STARTER_CHUNKS.add(starterChunk);
            }
            repairKnownSilverfishSpawners(region);
            int removedSpawners = removeNonSilverfishSpawnersNearKnownStrongholds(region);
            repairKnownSilverfishSpawners(region);
            if (removedSpawners > 0) {
                Mcsreasymode.debugRateLimited(
                        "stronghold-spawner-cleanup",
                        "Stronghold spawner cleanup removed " + removedSpawners + " non-silverfish spawner(s) near starter stairs.",
                        5000L
                );
            }
        } finally {
            ACTIVE_SNAPSHOTS.remove();
            ACTIVE_STARTER_CHUNK.remove();
        }
    }

    public static void cleanupKnownStrongholdSpawners(ChunkRegion region) {
        if (!Mcsreasymode.isStrongholdAntiCorruptionEnabled()) {
            return;
        }

        int removedSpawners = removeNonSilverfishSpawnersNearKnownStrongholds(region);
        repairKnownSilverfishSpawners(region);
        if (removedSpawners > 0) {
            Mcsreasymode.debugRateLimited(
                    "stronghold-spawner-cleanup",
                    "Stronghold spawner cleanup removed " + removedSpawners + " non-silverfish spawner(s) near starter stairs.",
                    5000L
            );
        }
    }

    private static int removeNonSilverfishSpawnersNearKnownStrongholds(ChunkRegion region) {
        Set<ChunkPos> starterChunks;
        synchronized (KNOWN_STARTER_CHUNKS) {
            starterChunks = new HashSet<>(KNOWN_STARTER_CHUNKS);
        }

        int removed = 0;
        for (ChunkPos starterChunk : starterChunks) {
            removed += removeNonSilverfishSpawners(region, starterChunk);
        }
        return removed;
    }

    private static int removeNonSilverfishSpawners(ChunkRegion region, ChunkPos starterChunk) {
        if (starterChunk == null) {
            return 0;
        }

        int removed = 0;
        int minChunkX = starterChunk.x - SPAWNER_CLEANUP_CHUNK_RADIUS;
        int maxChunkX = starterChunk.x + SPAWNER_CLEANUP_CHUNK_RADIUS;
        int minChunkZ = starterChunk.z - SPAWNER_CLEANUP_CHUNK_RADIUS;
        int maxChunkZ = starterChunk.z + SPAWNER_CLEANUP_CHUNK_RADIUS;

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                if (!region.isChunkLoaded(chunkX, chunkZ)) {
                    continue;
                }
                Chunk chunk = region.getChunk(chunkX, chunkZ);
                for (BlockPos pos : new ArrayList<>(chunk.getBlockEntityPositions())) {
                    if (!region.getBlockState(pos).isOf(Blocks.SPAWNER)) {
                        continue;
                    }
                    if (isKnownSilverfishSpawner(pos)) {
                        repairSilverfishSpawner(region, pos);
                        continue;
                    }
                    if (isSilverfishSpawner(region, chunk, pos)) {
                        continue;
                    }
                    region.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
                    chunk.removeBlockEntity(pos);
                    removed++;
                }
            }
        }
        return removed;
    }

    private static void repairKnownSilverfishSpawners(ServerWorldAccess world) {
        Set<Long> spawnerPositions;
        synchronized (KNOWN_SILVERFISH_SPAWNERS) {
            spawnerPositions = new HashSet<>(KNOWN_SILVERFISH_SPAWNERS);
        }

        for (long packedPos : spawnerPositions) {
            BlockPos pos = BlockPos.fromLong(packedPos);
            if (world instanceof ChunkRegion) {
                ChunkRegion region = (ChunkRegion) world;
                if (!region.isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4)) {
                    continue;
                }
            }
            repairSilverfishSpawner(world, pos);
        }
    }

    private static void repairSilverfishSpawner(ServerWorldAccess world, BlockPos pos) {
        if (!world.getBlockState(pos).isOf(Blocks.SPAWNER)) {
            world.setBlockState(pos, Blocks.SPAWNER.getDefaultState(), 2);
        }

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof MobSpawnerBlockEntity) {
            ((MobSpawnerBlockEntity) blockEntity).getLogic().setEntityId(EntityType.SILVERFISH);
            blockEntity.markDirty();
            CompoundTag tag = blockEntity.toTag(new CompoundTag());
            Mcsreasymode.debugRateLimited(
                    "stronghold-silverfish-spawner-nbt",
                    "Stronghold silverfish spawner NBT after repair: " + tag.toString(),
                    5000L
            );
        }
    }

    private static boolean isKnownSilverfishSpawner(BlockPos pos) {
        return KNOWN_SILVERFISH_SPAWNERS.contains(pos.asLong());
    }

    private static boolean isSilverfishSpawner(ChunkRegion region, Chunk chunk, BlockPos pos) {
        BlockEntity blockEntity = region.getBlockEntity(pos);
        CompoundTag tag = blockEntity == null ? chunk.getBlockEntityTag(pos) : blockEntity.toTag(new CompoundTag());
        if (tag == null) {
            return false;
        }

        if (tag.contains("EntityId", 8) && SILVERFISH_ID.equals(tag.getString("EntityId"))) {
            return true;
        }

        if (!tag.contains("SpawnData", 10)) {
            return false;
        }

        CompoundTag spawnData = tag.getCompound("SpawnData");
        if (SILVERFISH_ID.equals(spawnData.getString("id"))) {
            return true;
        }
        if (spawnData.contains("entity", 10) && SILVERFISH_ID.equals(spawnData.getCompound("entity").getString("id"))) {
            return true;
        }

        if (tag.contains("SpawnPotentials", 9)) {
            List<?> spawnPotentials = tag.getList("SpawnPotentials", 10);
            for (Object entry : spawnPotentials) {
                if (!(entry instanceof CompoundTag)) {
                    continue;
                }
                CompoundTag potential = (CompoundTag) entry;
                if (potential.contains("Entity", 10) && SILVERFISH_ID.equals(potential.getCompound("Entity").getString("id"))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static final class Snapshot {
        private final BlockState state;
        private final CompoundTag blockEntityTag;

        private Snapshot(BlockState state, CompoundTag blockEntityTag) {
            this.state = state;
            this.blockEntityTag = blockEntityTag;
        }
    }
}
