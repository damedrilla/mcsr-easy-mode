package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.NetherRouteEntry;
import me.dota1g.mcsreasymode.NetherRouteState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public abstract class ServerWorldNetherRouteMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void mcsreasymode$restoreNetherRouteEntry(CallbackInfo ci) {
        ServerWorld world = (ServerWorld) (Object) this;
        if (world.getRegistryKey() != World.NETHER) return;
        ChunkGenerator generator = world.getChunkManager().getChunkGenerator();
        if (!(generator instanceof NetherRouteEntry)) return;
        NetherRouteState state = world.getPersistentStateManager().getOrCreate(NetherRouteState::new, NetherRouteState.ID);
        if (state.getEntry() != null) {
            ((NetherRouteEntry) generator).mcsreasymode$setNetherRouteEntry(state.getEntry());
        }
    }
}
