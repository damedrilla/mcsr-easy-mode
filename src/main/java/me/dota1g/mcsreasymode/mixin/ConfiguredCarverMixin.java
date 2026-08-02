package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import net.minecraft.world.gen.ProbabilityConfig;
import net.minecraft.world.gen.carver.Carver;
import net.minecraft.world.gen.carver.CarverConfig;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ConfiguredCarver.class)
public abstract class ConfiguredCarverMixin {
    private static final ProbabilityConfig MCSREASYMODE_AREESSGEE_OCEAN_RAVINE_RARITY = new ProbabilityConfig(0.04F);

    @Shadow
    @Final
    public Carver<?> carver;

    @ModifyArg(
            method = "shouldCarve",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/gen/carver/Carver;shouldCarve(Ljava/util/Random;IILnet/minecraft/world/gen/carver/CarverConfig;)Z"
            ),
            index = 3
    )
    private CarverConfig mcsreasymode$increaseUnderwaterRavineRarity(CarverConfig config) {
        if (Mcsreasymode.isUnderwaterMagmaRavinesEnabled() && this.carver == Carver.UNDERWATER_CANYON) {
            return MCSREASYMODE_AREESSGEE_OCEAN_RAVINE_RARITY;
        }
        return config;
    }
}
