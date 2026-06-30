package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import me.dota1g.mcsreasymode.RankedRngState;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {
    @Inject(method = "changeDimension", at = @At("HEAD"))
    private void mcsreasymode$armBlindPortalSurface(ServerWorld destination, CallbackInfoReturnable<Entity> cir) {
        if (!Mcsreasymode.isRankedRngEnabled()) {
            return;
        }

        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        ServerWorld source = player.getServerWorld();

        if (source.getRegistryKey() == World.OVERWORLD && destination.getRegistryKey() == World.NETHER) {
            RankedRngState.resetBlindPortalSurface();
        }

        if (source.getRegistryKey() == World.NETHER
                && destination.getRegistryKey() == World.OVERWORLD
                && player.getY() >= 48.0D) {
            int overworldX = (int) Math.floor(player.getX() * 8.0D);
            int overworldZ = (int) Math.floor(player.getZ() * 8.0D);
            int surfaceY = this.mcsreasymode$getGeneratedSurfaceY(destination, overworldX, overworldZ);

            player.refreshPositionAndAngles(player.getX(), surfaceY, player.getZ(), player.getYaw(1.0F), player.getPitch(1.0F));
            Mcsreasymode.debug("Blind portal surfacing applied pre-search: moved player to surface Y " + surfaceY + " at Overworld X " + overworldX + ", Z " + overworldZ + ".");

            RankedRngState.consumeBlindPortalSurface();
        }
    }

    @Inject(method = "changeDimension", at = @At("RETURN"))
    private void mcsreasymode$clearUnusedBlindPortalSurface(ServerWorld destination, CallbackInfoReturnable<Entity> cir) {
        RankedRngState.clearPendingBlindPortalSurface();
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
