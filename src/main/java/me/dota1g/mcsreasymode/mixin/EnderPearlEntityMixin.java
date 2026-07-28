package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnderPearlEntity.class)
public abstract class EnderPearlEntityMixin {
    private static final double mcsreasymode$HUNGER_RESET_DISTANCE_SQUARED = 4.0D;

    @Unique
    private Vec3d mcsreasymode$startPos;

    @Inject(method = "tick", at = @At("HEAD"))
    private void mcsreasymode$capturePearlStart(CallbackInfo ci) {
        EnderPearlEntity pearl = (EnderPearlEntity) (Object) this;
        if (!pearl.world.isClient && this.mcsreasymode$startPos == null) {
            this.mcsreasymode$startPos = pearl.getPos();
        }
    }

    @Redirect(method = "onCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
    private boolean mcsreasymode$redirectPearlDamage(Entity owner, DamageSource source, float amount) {
        EnderPearlEntity pearl = (EnderPearlEntity) (Object) this;
        if (!Mcsreasymode.isFunReducedPearlDamageEnabled() || pearl.world.isClient || this.mcsreasymode$startPos == null) {
            return owner.damage(source, amount);
        }

        double distanceSquared = this.mcsreasymode$startPos.squaredDistanceTo(pearl.getPos());
        if (distanceSquared < mcsreasymode$HUNGER_RESET_DISTANCE_SQUARED) {
            Mcsreasymode.debugRateLimited(
                    "fun.pearl_damage.kept." + owner.getEntityId(),
                    "Fun mode pearl damage preserved for short hunger-reset pearl.",
                    1000L
            );
            return owner.damage(source, amount);
        }

        Mcsreasymode.debugRateLimited(
                "fun.pearl_damage.removed." + owner.getEntityId(),
                "Fun mode pearl damage removed after " + String.format("%.1f", Math.sqrt(distanceSquared)) + " block throw.",
                1000L
        );
        return false;
    }
}
