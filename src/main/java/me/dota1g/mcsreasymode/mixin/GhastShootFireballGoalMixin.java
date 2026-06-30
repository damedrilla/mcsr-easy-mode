package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.entity.mob.GhastEntity$ShootFireballGoal")
public abstract class GhastShootFireballGoalMixin {
    @Inject(method = "canStart", at = @At("HEAD"), cancellable = true)
    private void mcsreasymode$disableFireballs(CallbackInfoReturnable<Boolean> cir) {
        if (Mcsreasymode.isGhastAggressionDisabled()) {
            cir.setReturnValue(false);
        }
    }
}
