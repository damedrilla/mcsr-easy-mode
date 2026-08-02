package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.McsreasymodeMinecartVelocityState;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractMinecartEntity.class)
public abstract class AbstractMinecartEntityMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void mcsreasymode$tickFunMinecartVelocity(CallbackInfo ci) {
        McsreasymodeMinecartVelocityState.tickStackedMinecart((AbstractMinecartEntity) (Object) this);
    }

    @Inject(method = "dropItems", at = @At("HEAD"))
    private void mcsreasymode$transferFunMinecartVelocity(DamageSource damageSource, CallbackInfo ci) {
        AbstractMinecartEntity cart = (AbstractMinecartEntity) (Object) this;
        McsreasymodeMinecartVelocityState.transferToPassengers(cart);
        McsreasymodeMinecartVelocityState.forget(cart);
    }
}
