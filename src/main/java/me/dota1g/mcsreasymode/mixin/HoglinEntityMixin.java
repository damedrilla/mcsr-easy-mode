package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.mob.HoglinEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(HoglinEntity.class)
public abstract class HoglinEntityMixin {
    @Inject(method = "tryAttack", at = @At("HEAD"), cancellable = true)
    private void mcsreasymode$cancelHoglinAttack(Entity target, CallbackInfoReturnable<Boolean> cir) {
        if (Mcsreasymode.isHoglinAggressionDisabled()) {
            Mcsreasymode.debugRateLimited("hoglin.direct_attack", "Hoglin aggression cancelled: direct attack blocked.", 2000L);
            cir.setReturnValue(false);
        }
    }

    @Inject(
            method = "mobTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/mob/HoglinBrain;refreshActivities(Lnet/minecraft/entity/mob/HoglinEntity;)V"
            )
    )
    private void mcsreasymode$forgetHoglinAttackTarget(CallbackInfo ci) {
        HoglinEntity hoglin = (HoglinEntity) (Object) this;
        if (Mcsreasymode.isHoglinAggressionDisabled() && hoglin.getBrain().hasMemoryModule(MemoryModuleType.ATTACK_TARGET)) {
            hoglin.getBrain().forget(MemoryModuleType.ATTACK_TARGET);
            Mcsreasymode.debugRateLimited("hoglin.brain_target", "Hoglin aggression cancelled: brain attack target forgotten.", 2000L);
        }
    }

    @Inject(method = "getAmbientSound", at = @At("HEAD"), cancellable = true)
    private void mcsreasymode$replaceAngryAmbientSound(CallbackInfoReturnable<SoundEvent> cir) {
        if (!Mcsreasymode.isHoglinAggressionDisabled()) {
            return;
        }

        HoglinEntity hoglin = (HoglinEntity) (Object) this;
        Optional<Activity> activity = hoglin.getBrain().getFirstPossibleNonCoreActivity();
        if (activity.isPresent() && activity.get() == Activity.FIGHT) {
            Mcsreasymode.debugRateLimited("hoglin.angry_sound", "Hoglin aggression cancelled: angry ambient sound replaced.", 2000L);
            cir.setReturnValue(SoundEvents.ENTITY_HOGLIN_AMBIENT);
        }
    }
}
