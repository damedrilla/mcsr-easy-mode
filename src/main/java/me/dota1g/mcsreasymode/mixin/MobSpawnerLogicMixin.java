package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.BlazeSpawnerServerState;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobSpawnerLogic.class)
public abstract class MobSpawnerLogicMixin {
    @Inject(method = "update", at = @At("RETURN"))
    private void mcsreasymode$captureAuthoritativeSpawnerDelay(CallbackInfo ci) {
        MobSpawnerLogic logic = (MobSpawnerLogic) (Object) this;
        World world = logic.getWorld();
        if (world != null && !world.isClient) {
            int delay = ((MobSpawnerLogicAccessor) this).mcsreasymode$getSpawnDelay();
            BlazeSpawnerServerState.update(world.getRegistryKey(), logic.getPos(), Math.max(0, delay));
        }
    }
}
