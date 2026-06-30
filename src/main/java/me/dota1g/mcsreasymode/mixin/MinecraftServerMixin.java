package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.RankedRngState;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Inject(method = "loadWorld", at = @At("HEAD"))
    private void mcsreasymode$resetRankedRngState(CallbackInfo ci) {
        RankedRngState.reset();
    }
}
