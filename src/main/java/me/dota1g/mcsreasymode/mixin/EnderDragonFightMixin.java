package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.entity.boss.dragon.phase.HoldingPatternPhase;
import net.minecraft.entity.boss.dragon.phase.Phase;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(EnderDragonFight.class)
public abstract class EnderDragonFightMixin {

    // node 11 on EnderDragonEntity's 12-node circle (~330°, matching front tower)
    private static final double MCSREASYMODE$FRONT_TARGET_X = 51.0;
    private static final double MCSREASYMODE$FRONT_TARGET_Y = 128.0;
    private static final double MCSREASYMODE$FRONT_TARGET_Z = -30.0;

    @Inject(method = "createDragon", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/boss/dragon/phase/PhaseManager;setPhase(Lnet/minecraft/entity/boss/dragon/phase/PhaseType;)V",
            shift = At.Shift.AFTER),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void mcsreasymode$forceFirstTarget(CallbackInfoReturnable<EnderDragonEntity> cir, EnderDragonEntity dragon) {
        if (!Mcsreasymode.CONFIG.forceDragonFirstTargetFront) {
            return;
        }
        Phase phase = dragon.getPhaseManager().getCurrent();
        if (phase instanceof HoldingPatternPhase) {
            ((HoldingPatternPhaseAccessor) phase).mcsreasymode$setTarget(
                    new Vec3d(MCSREASYMODE$FRONT_TARGET_X, MCSREASYMODE$FRONT_TARGET_Y, MCSREASYMODE$FRONT_TARGET_Z));
        }
    }
}