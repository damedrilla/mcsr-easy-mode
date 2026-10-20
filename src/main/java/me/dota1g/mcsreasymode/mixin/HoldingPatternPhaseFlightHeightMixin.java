package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import net.minecraft.entity.boss.dragon.phase.HoldingPatternPhase;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(HoldingPatternPhase.class)
public abstract class HoldingPatternPhaseFlightHeightMixin {

    @Shadow private Vec3d target;

    // Clamp both existing targets before distance checks and newly selected targets
    // before the dragon consumes them for movement in the same tick.
    @Inject(method = "serverTick", at = @At("HEAD"))
    private void mcsreasymode$floorExistingTarget(CallbackInfo ci) {
        this.mcsreasymode$applyHeightFloor();
    }

    @Inject(method = "method_6842", at = @At("TAIL"))
    private void mcsreasymode$floorNewTarget(CallbackInfo ci) {
        this.mcsreasymode$applyHeightFloor();
    }

    @org.spongepowered.asm.mixin.Unique
    private void mcsreasymode$applyHeightFloor() {
        int minimumY = Math.max(0, Math.min(255, Mcsreasymode.CONFIG.dragonMinCirclingY));
        if (minimumY > 0 && this.target != null && this.target.y < minimumY) {
            this.target = new Vec3d(this.target.x, minimumY, this.target.z);
        }
    }

    @Redirect(method = "method_6842", at = @At(value = "INVOKE", target = "Ljava/util/Random;nextFloat()F"))
    private float mcsreasymode$capFlightHeightOffset(Random random) {
        float value = random.nextFloat();
        //if (Mcsreasymode.CONFIG.rankedZeroCycleHeightCap) {
            return value * 0.75F; // scales vanilla's 0-20 range down to Ranked's 0-15
        //}
        //return value;
    }
}
