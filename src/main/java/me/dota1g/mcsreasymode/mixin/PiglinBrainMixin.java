package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import me.dota1g.mcsreasymode.RankedRngState;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(PiglinBrain.class)
public abstract class PiglinBrainMixin {
    @Inject(method = "getBarteredItem", at = @At("RETURN"), cancellable = true)
    private static void mcsreasymode$applyBarterPity(PiglinEntity piglin, CallbackInfoReturnable<List<ItemStack>> cir) {
        if (Mcsreasymode.isRankedRngEnabled()) {
            cir.setReturnValue(RankedRngState.applyPiglinBarterPity(cir.getReturnValue(), piglin.getRandom()));
        }
    }
}
