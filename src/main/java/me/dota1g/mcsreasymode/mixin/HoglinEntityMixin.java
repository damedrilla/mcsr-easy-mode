package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.mob.HoglinEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HoglinEntity.class)
public abstract class HoglinEntityMixin {
    @Inject(method = "tryAttack", at = @At("HEAD"), cancellable = true)
    private void mcsreasymode$cancelHoglinAttack(Entity target, CallbackInfoReturnable<Boolean> cir) {
        if (Mcsreasymode.isHoglinAggressionDisabled()) {
            Mcsreasymode.debugRateLimited("hoglin.direct_attack", "Hoglin aggression cancelled: direct attack blocked.", 2000L);
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "mobTick", at = @At("HEAD"))
    private void mcsreasymode$forgetHoglinAttackTarget(CallbackInfo ci) {
        HoglinEntity hoglin = (HoglinEntity) (Object) this;
        if (Mcsreasymode.isHoglinAggressionDisabled() && hoglin.getBrain().hasMemoryModule(MemoryModuleType.ATTACK_TARGET)) {
            hoglin.getBrain().forget(MemoryModuleType.ATTACK_TARGET);
            Mcsreasymode.debugRateLimited("hoglin.brain_target", "Hoglin aggression cancelled: brain attack target forgotten.", 2000L);
        }
    }
}
