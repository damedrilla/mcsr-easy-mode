package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import net.minecraft.world.gen.ProbabilityConfig;
import net.minecraft.world.gen.carver.Carver;
import net.minecraft.world.gen.carver.RavineCarver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RavineCarver.class)
public abstract class UnderwaterRavineShapeMixin {
    private static final float STABLE_RARITY = 0.04F;
    private static final float STABLE_WIDTH = 5.0F;

    @Redirect(method = "shouldCarve(Ljava/util/Random;IILnet/minecraft/world/gen/ProbabilityConfig;)Z", at = @At(value = "FIELD", target = "Lnet/minecraft/world/gen/ProbabilityConfig;probability:F"))
    private float mcsreasymode$stableUnderwaterRavineProbability(ProbabilityConfig config) {
        if (this.mcsreasymode$isStableUnderwaterRavine()) {
            return STABLE_RARITY;
        }
        return config.probability;
    }

    @ModifyVariable(method = "carveRavine", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float mcsreasymode$stableUnderwaterRavineWidth(float vanillaWidth) {
        if (this.mcsreasymode$isStableUnderwaterRavine()) {
            return STABLE_WIDTH - 0.5F + Math.abs(vanillaWidth % 1.0F);
        }
        return vanillaWidth;
    }

    private boolean mcsreasymode$isStableUnderwaterRavine() {
        Carver<?> carver = (Carver<?>) (Object) this;
        return Mcsreasymode.isUnderwaterMagmaRavinesEnabled()
                && (Object) carver.getCodec() == Carver.UNDERWATER_CANYON.getCodec();
    }
}
