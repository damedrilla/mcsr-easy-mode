package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import net.minecraft.entity.boss.dragon.phase.HoldingPatternPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

@Mixin(HoldingPatternPhase.class)
public abstract class HoldingPatternPhaseFlightHeightMixin {

    @Redirect(method = "method_6842", at = @At(value = "INVOKE", target = "Ljava/util/Random;nextFloat()F"))
    private float mcsreasymode$capFlightHeightOffset(Random random) {
        float value = random.nextFloat();
        //if (Mcsreasymode.CONFIG.rankedZeroCycleHeightCap) {
            return value * 0.75F; // scales vanilla's 0-20 range down to Ranked's 0-15
        //}
        //return value;
    }
}