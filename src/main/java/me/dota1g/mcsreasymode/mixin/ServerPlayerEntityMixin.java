package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import me.dota1g.mcsreasymode.EndEntryHotbarSorter;
import me.dota1g.mcsreasymode.RankedRngState;
import me.dota1g.mcsreasymode.NetherRouteEntry;
import me.dota1g.mcsreasymode.NetherRouteState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {
    @Unique
    private boolean mcsreasymode$sortAfterEndEntry;

    @Inject(method = "changeDimension", at = @At("HEAD"))
    private void mcsreasymode$rememberEndEntry(ServerWorld destination, CallbackInfoReturnable<Entity> cir) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        this.mcsreasymode$sortAfterEndEntry = player.getServerWorld().getRegistryKey() == World.OVERWORLD
                && destination.getRegistryKey() == World.END;
    }

    @Inject(method = "changeDimension", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/PortalForcer;usePortal(Lnet/minecraft/entity/Entity;F)Z",
            ordinal = 0))
    private void mcsreasymode$captureNetherRouteEntry(ServerWorld destination, CallbackInfoReturnable<Entity> cir) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        if (player.getServerWorld().getRegistryKey() != World.OVERWORLD
                || destination.getRegistryKey() != World.NETHER) return;
        ChunkGenerator generator = destination.getChunkManager().getChunkGenerator();
        if (!(generator instanceof NetherRouteEntry)) return;
        // Vanilla has scaled/clamped X/Z here, but portal search has not loaded chunks yet.
        // Capturing even with routes disabled preserves the first entry if enabled later.
        NetherRouteState state = destination.getPersistentStateManager().getOrCreate(NetherRouteState::new, NetherRouteState.ID);
        BlockPos entry = state.capture(MathHelper.floor(player.getX()), MathHelper.floor(player.getZ()));
        ((NetherRouteEntry) generator).mcsreasymode$setNetherRouteEntry(entry);
    }

    @Inject(method = "changeDimension", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/PortalForcer;createPortal(Lnet/minecraft/entity/Entity;)Z",
            shift = At.Shift.BEFORE))
    private void mcsreasymode$armBlindPortalSurface(ServerWorld destination, CallbackInfoReturnable<Entity> cir) {
        if (!Mcsreasymode.isRankedBlindPortalEnabled()) {
            return;
        }

        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        ServerWorld source = player.getServerWorld();

        if (source.getRegistryKey() == World.NETHER
                && destination.getRegistryKey() == World.OVERWORLD
                && player.getY() >= 48.0D
                && RankedRngState.canUseBlindPortalSurface()) {
            // At this point vanilla and compatibility mixins have already moved the
            // player to the final destination X/Z. Sampling here keeps the forced
            // surface Y aligned with coordinate-remapping mods such as Artificial ZSG.
            int overworldX = (int) Math.floor(player.getX());
            int overworldZ = (int) Math.floor(player.getZ());
            int surfaceY = this.mcsreasymode$getGeneratedSurfaceY(destination, overworldX, overworldZ);

            RankedRngState.setBlindPortalForcedSurfaceY(surfaceY);
            Mcsreasymode.debug("Blind portal surfacing armed portal creation at surface Y " + surfaceY + " for Overworld X " + overworldX + ", Z " + overworldZ + ".");

            RankedRngState.consumeBlindPortalSurface();
        }
    }

    @Inject(method = "changeDimension", at = @At("RETURN"))
    private void mcsreasymode$clearUnusedBlindPortalSurface(ServerWorld destination, CallbackInfoReturnable<Entity> cir) {
        RankedRngState.clearPendingBlindPortalSurface();
    }

    @Inject(method = "changeDimension", at = @At("RETURN"))
    private void mcsreasymode$sortEndEntryHotbar(ServerWorld destination, CallbackInfoReturnable<Entity> cir) {
        boolean shouldSort = this.mcsreasymode$sortAfterEndEntry;
        this.mcsreasymode$sortAfterEndEntry = false;
        if (!shouldSort || !Mcsreasymode.shouldAutoSortEndEntryHotbar() || cir.getReturnValue() == null) {
            return;
        }

        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        if (destination.getRegistryKey() != World.END || player.isCreative() || player.isSpectator()) {
            return;
        }

        EndEntryHotbarSorter.sort(player);
    }

    private int mcsreasymode$getGeneratedSurfaceY(ServerWorld destination, int x, int z) {
        int chunkX = ChunkSectionPos.getSectionCoord(x);
        int chunkZ = ChunkSectionPos.getSectionCoord(z);
        WorldChunk chunk = destination.getChunk(chunkX, chunkZ);
        int localX = ChunkSectionPos.getLocalCoord(x);
        int localZ = ChunkSectionPos.getLocalCoord(z);
        int surfaceY = chunk.sampleHeightmap(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, localX, localZ);

        if (surfaceY <= 0) {
            surfaceY = destination.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
        }

        if (surfaceY <= 0) {
            surfaceY = 70;
            Mcsreasymode.debug("Blind portal surfacing warning: heightmap was still 0 after forcing chunk load; falling back to Y 70.");
        }

        return surfaceY;
    }
}
