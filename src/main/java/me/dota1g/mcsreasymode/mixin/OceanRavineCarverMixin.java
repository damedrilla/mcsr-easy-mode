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
public abstract class OceanRavineCarverMixin {
    @Redirect(method = "shouldCarve(Ljava/util/Random;IILnet/minecraft/world/gen/ProbabilityConfig;)Z", at = @At(value = "FIELD", target = "Lnet/minecraft/world/gen/ProbabilityConfig;probability:F"))
    private float mcsreasymode$oceanRavineProbability(ProbabilityConfig config) {
        if (Mcsreasymode.areOceanRavinesEnabled() && (Object) this == Carver.UNDERWATER_CANYON) {
            return Mcsreasymode.oceanRavineRarity();
        }
        return config.probability;
    }

    @ModifyVariable(method = "carveRavine", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float mcsreasymode$widenOceanRavine(float vanillaWidth) {
        if (Mcsreasymode.areOceanRavinesEnabled() && (Object) this == Carver.UNDERWATER_CANYON) {
            float configuredWidth = Mcsreasymode.oceanRavineWidth();
            return configuredWidth - 0.5F + Math.abs(vanillaWidth % 1.0F);
        }
        return vanillaWidth;
    }
}
