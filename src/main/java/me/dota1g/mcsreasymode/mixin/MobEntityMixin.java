package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.mob.HoglinEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PiglinEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin {
    @ModifyVariable(method = "setTarget", at = @At("HEAD"), argsOnly = true)
    private LivingEntity mcsreasymode$removeTarget(LivingEntity target) {
        if (this.mcsreasymode$shouldDisableAggression()) {
            return null;
        }
        return target;
    }

    @Inject(method = "tryAttack", at = @At("HEAD"), cancellable = true)
    private void mcsreasymode$cancelAttack(Entity target, CallbackInfoReturnable<Boolean> cir) {
        if (this.mcsreasymode$shouldDisableAggression()) {
            cir.setReturnValue(false);
        }
    }

    private boolean mcsreasymode$shouldDisableAggression() {
        Object self = this;
        if (self instanceof PiglinEntity) {
            return Mcsreasymode.isPiglinAggressionDisabled();
        }
        if (self instanceof GhastEntity) {
            return Mcsreasymode.isGhastAggressionDisabled();
        }
        if (self instanceof HoglinEntity) {
            return Mcsreasymode.isHoglinAggressionDisabled();
        }
        return false;
    }
}
