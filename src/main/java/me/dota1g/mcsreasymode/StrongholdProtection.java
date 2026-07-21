package me.dota1g.mcsreasymode;

import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public final class StrongholdProtection {
    private static final int SPAWNER_CLEANUP_CHUNK_RADIUS = 16;
    private static final String SILVERFISH_ID = "minecraft:silverfish";
    private static final ThreadLocal<Map<Long, Snapshot>> ACTIVE_SNAPSHOTS = new ThreadLocal<>();
    private static final ThreadLocal<ChunkPos> ACTIVE_STARTER_CHUNK = new ThreadLocal<>();

    private StrongholdProtection() {
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
        ACTIVE_STARTER_CHUNK.set(new ChunkPos(new BlockPos(centerX, pieceBox.minY, centerZ)));
    }

    public static void restoreAndEnd(ChunkRegion region) {
        Map<Long, Snapshot> snapshots = ACTIVE_SNAPSHOTS.get();
        if (snapshots == null) {
            return;
        }

        try {
            int changedBlocks = 0;
            for (Map.Entry<Long, Snapshot> entry : snapshots.entrySet()) {
                BlockPos pos = BlockPos.fromLong(entry.getKey());
                Snapshot snapshot = entry.getValue();
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
            int removedSpawners = removeNonSilverfishSpawners(region, ACTIVE_STARTER_CHUNK.get());
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

    private static boolean isSilverfishSpawner(ChunkRegion region, Chunk chunk, BlockPos pos) {
        BlockEntity blockEntity = region.getBlockEntity(pos);
        CompoundTag tag = blockEntity == null ? chunk.getBlockEntityTag(pos) : blockEntity.toTag(new CompoundTag());
        if (tag == null || !tag.contains("SpawnData", 10)) {
            return false;
        }

        CompoundTag spawnData = tag.getCompound("SpawnData");
        return SILVERFISH_ID.equals(spawnData.getString("id"));
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
