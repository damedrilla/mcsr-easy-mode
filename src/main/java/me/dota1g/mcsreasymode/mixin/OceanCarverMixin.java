package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import net.minecraft.world.gen.ProbabilityConfig;
import net.minecraft.world.gen.carver.Carver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Carver.class)
public abstract class OceanCarverMixin {
    @Redirect(method = "carveRegion", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(II)I", ordinal = 1))
    private int mcsreasymode$deepenOceanRavines(int first, int second) {
        if (Mcsreasymode.areOceanRavinesEnabled() && (Object) this == Carver.UNDERWATER_CANYON) {
            return 1;
        }
        return Math.max(first, second);
    }
}
