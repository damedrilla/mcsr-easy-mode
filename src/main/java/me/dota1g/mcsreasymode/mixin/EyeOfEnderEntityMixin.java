package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import net.minecraft.entity.EyeOfEnderEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EyeOfEnderEntity.class)
public abstract class EyeOfEnderEntityMixin {
    @Shadow
    private boolean dropsItem;

    @Inject(method = "moveTowards", at = @At("TAIL"))
    private void mcsreasymode$makeEyeReturn(BlockPos pos, CallbackInfo ci) {
        if (Mcsreasymode.isRankedRngEnabled()) {
            this.dropsItem = true;
            Mcsreasymode.debug("Eye of ender standardized: forced thrown eye to drop back.");
        }
    }
}
