package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.ExplosionDropState;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Explosion.class)
public abstract class ExplosionMixin {

    @Inject(method = "affectWorld", at = @At("HEAD"))
    private void mcsreasymode$beginExplosion(boolean particles, CallbackInfo ci) {
        ExplosionDropState.enter();
    }

    @Inject(method = "affectWorld", at = @At("RETURN"))
    private void mcsreasymode$endExplosion(boolean particles, CallbackInfo ci) {
        ExplosionDropState.exit();
    }
}