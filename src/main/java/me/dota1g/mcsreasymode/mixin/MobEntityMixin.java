package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.entity.mob.HoglinEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void mcsreasymode$clearExistingDrownedTarget(CallbackInfo ci) {
        Object self = this;
        if (self instanceof DrownedEntity && Mcsreasymode.isDrownedAggressionDisabled()) {
            DrownedEntity drowned = (DrownedEntity) self;
            if (drowned.getTarget() != null) {
                drowned.setTarget(null);
                Mcsreasymode.debugRateLimited("drowned.target", "Drowned aggression cancelled: target cleared.", 2000L);
            }
        }
    }

    @ModifyVariable(method = "setTarget", at = @At("HEAD"), argsOnly = true)
    private LivingEntity mcsreasymode$removeTarget(LivingEntity target) {
        if (this.mcsreasymode$shouldDisableAggression()) {
            Mcsreasymode.debugRateLimited(this.mcsreasymode$getAggressionLogKey() + ".target", this.mcsreasymode$getAggressionLogName() + " aggression cancelled: target cleared.", 2000L);
            return null;
        }
        return target;
    }

    @Inject(method = "tryAttack", at = @At("HEAD"), cancellable = true)
    private void mcsreasymode$cancelAttack(Entity target, CallbackInfoReturnable<Boolean> cir) {
        Object self = this;
        if (self instanceof BlazeEntity && Mcsreasymode.isBlazeMeleeAttackDisabled()) {
            cir.setReturnValue(false);
            return;
        }
        if (self instanceof PiglinEntity && target instanceof PlayerEntity && Mcsreasymode.shouldSuppressProvokedPiglinAttacks()) {
            Mcsreasymode.debugRateLimited("piglin.melee", "Piglin followed angry target without melee damage.", 2000L);
            cir.setReturnValue(false);
            return;
        }
        if (this.mcsreasymode$shouldDisableAggression()) {
            Mcsreasymode.debugRateLimited(this.mcsreasymode$getAggressionLogKey() + ".attack", this.mcsreasymode$getAggressionLogName() + " aggression cancelled: attack blocked.", 2000L);
            cir.setReturnValue(false);
        }
    }

    private boolean mcsreasymode$shouldDisableAggression() {
        Object self = this;
        if (self instanceof GhastEntity) {
            return Mcsreasymode.isGhastAggressionDisabled();
        }
        if (self instanceof HoglinEntity) {
            return Mcsreasymode.isHoglinAggressionDisabled();
        }
        if (self instanceof DrownedEntity) {
            return Mcsreasymode.isDrownedAggressionDisabled();
        }
        return false;
    }

    private String mcsreasymode$getAggressionLogName() {
        Object self = this;
        if (self instanceof GhastEntity) {
            return "Ghast";
        }
        if (self instanceof HoglinEntity) {
            return "Hoglin";
        }
        if (self instanceof DrownedEntity) {
            return "Drowned";
        }
        return "Mob";
    }

    private String mcsreasymode$getAggressionLogKey() {
        return this.mcsreasymode$getAggressionLogName().toLowerCase();
    }
}
