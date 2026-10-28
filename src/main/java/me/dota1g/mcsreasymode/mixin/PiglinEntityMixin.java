package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PiglinEntity.class)
public abstract class PiglinEntityMixin {
    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void mcsreasymode$keepAngerWithoutCrossbowShot(LivingEntity target, float pullProgress, CallbackInfo ci) {
        if (target instanceof PlayerEntity && Mcsreasymode.shouldSuppressProvokedPiglinAttacks()) {
            Mcsreasymode.debugRateLimited("piglin.crossbow", "Piglin followed angry target without firing a crossbow.", 2000L);
            ci.cancel();
        }
    }
}
