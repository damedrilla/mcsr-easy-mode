package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import me.dota1g.mcsreasymode.RankedRngState;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.MemoryTransferTask;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.IntRange;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

@Mixin(PiglinBrain.class)
public abstract class PiglinBrainMixin {
    @Shadow
    private static IntRange field_25384;

    @Inject(method = "wearsGoldArmor", at = @At("HEAD"), cancellable = true)
    private static void mcsreasymode$spoofGoldArmor(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (Mcsreasymode.isPiglinAggressionDisabled() && entity instanceof PlayerEntity) {
            Mcsreasymode.debugRateLimited("piglin.gold_armor_spoof", "Piglin aggression softened: player treated as wearing gold armor.", 5000L);
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "makeGoToZombifiedPiglinTask", at = @At("HEAD"), cancellable = true)
    private static void mcsreasymode$ignoreZombifiedPiglinFearTask(CallbackInfoReturnable<MemoryTransferTask<PiglinEntity, LivingEntity>> cir) {
        cir.setReturnValue(new MemoryTransferTask<>(
                piglin -> !Mcsreasymode.isPiglinAggressionDisabled(),
                MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED,
                MemoryModuleType.AVOID_TARGET,
                field_25384
        ));
    }

    @Inject(method = "tickActivities", at = @At("HEAD"))
    private static void mcsreasymode$forgetZombifiedAvoidTarget(PiglinEntity piglin, CallbackInfo ci) {
        if (!Mcsreasymode.isPiglinAggressionDisabled()) {
            return;
        }

        Brain<PiglinEntity> brain = piglin.getBrain();
        Optional<LivingEntity> avoidTarget = brain.getOptionalMemory(MemoryModuleType.AVOID_TARGET);
        if (avoidTarget.isPresent() && PiglinBrain.isZombified(avoidTarget.get().getType())) {
            brain.forget(MemoryModuleType.AVOID_TARGET);
            Mcsreasymode.debugRateLimited("piglin.zombified_fear", "Piglin aggression softened: zombified avoid target forgotten.", 5000L);
        }
    }

    @Inject(method = "getNearestZombifiedPiglin", at = @At("HEAD"), cancellable = true)
    private static void mcsreasymode$ignoreNearestZombifiedPiglin(PiglinEntity piglin, CallbackInfoReturnable<Boolean> cir) {
        if (Mcsreasymode.isPiglinAggressionDisabled()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "getBarteredItem", at = @At("RETURN"), cancellable = true)
    private static void mcsreasymode$applyBarterPity(PiglinEntity piglin, CallbackInfoReturnable<List<ItemStack>> cir) {
        if (Mcsreasymode.isRankedPiglinBartersEnabled()) {
            cir.setReturnValue(RankedRngState.applyPiglinBarterPity(cir.getReturnValue(), piglin.getRandom()));
        }
    }
}
