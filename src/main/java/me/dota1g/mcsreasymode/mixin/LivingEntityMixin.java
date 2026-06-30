package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    private static int mcsreasymode$blazeRodlessKills;

    @Inject(method = "dropLoot", at = @At("HEAD"), cancellable = true)
    private void mcsreasymode$rankedLoot(DamageSource source, boolean causedByPlayer, CallbackInfo ci) {
        Object self = this;
        if (!Mcsreasymode.isRankedRngEnabled()) {
            return;
        }

        if (self instanceof IronGolemEntity) {
            ((LivingEntity) self).dropStack(new ItemStack(Items.IRON_INGOT, 4));
            ci.cancel();
            return;
        }

        if (causedByPlayer && self instanceof BlazeEntity) {
            LivingEntity entity = (LivingEntity) self;
            boolean shouldDropRod = entity.getRandom().nextFloat() < 0.5F || mcsreasymode$blazeRodlessKills >= 2;
            if (shouldDropRod) {
                entity.dropStack(new ItemStack(Items.BLAZE_ROD));
                mcsreasymode$blazeRodlessKills = 0;
            } else {
                mcsreasymode$blazeRodlessKills++;
            }
            ci.cancel();
        }
    }
}
