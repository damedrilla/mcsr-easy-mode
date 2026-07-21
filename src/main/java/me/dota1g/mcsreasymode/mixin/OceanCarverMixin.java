package me.dota1g.mcsreasymode.mixin;

import com.mojang.serialization.Codec;
import me.dota1g.mcsreasymode.Mcsreasymode;
import net.minecraft.world.gen.ProbabilityConfig;
import net.minecraft.world.gen.carver.Carver;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Carver.class)
public abstract class OceanCarverMixin {
    @Shadow
    @Final
    public static Carver<ProbabilityConfig> UNDERWATER_CANYON;

    @Shadow
    public abstract Codec<ConfiguredCarver<ProbabilityConfig>> getCodec();

    @Redirect(method = "carveRegion", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(II)I", ordinal = 1))
    private int mcsreasymode$deepenOceanRavines(int first, int second) {
        if (Mcsreasymode.isUnderwaterMagmaRavinesEnabled() && this.getCodec() == UNDERWATER_CANYON.getCodec()) {
            return 1;
        }
        return Math.max(first, second);
    }
}
