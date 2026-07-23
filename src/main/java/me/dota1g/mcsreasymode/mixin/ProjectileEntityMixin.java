package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import me.dota1g.mcsreasymode.McsreasymodeMinecartVelocityState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ProjectileEntity.class)
public abstract class ProjectileEntityMixin {
    @Inject(method = "setProperties", at = @At("TAIL"))
    private void mcsreasymode$addFunMinecartVelocity(Entity shooter, float pitch, float yaw, float roll, float modifier, float divergence, CallbackInfo ci) {
        ProjectileEntity projectile = (ProjectileEntity) (Object) this;
        double boostSpeed = McsreasymodeMinecartVelocityState.projectileBoostSpeed(shooter);
        if (boostSpeed <= 0.0D) {
            return;
        }

        Vec3d currentVelocity = projectile.getVelocity();
        Vec3d aimDirection = currentVelocity.lengthSquared() > 0.0001D ? currentVelocity.normalize() : shooter.getRotationVec(1.0F);
        projectile.setVelocity(aimDirection.multiply(currentVelocity.length() + boostSpeed));
        McsreasymodeMinecartVelocityState.stopShooterRecoil(shooter);
        Mcsreasymode.debugRateLimited(
                "fun.minecart.projectile." + shooter.getEntityId(),
                "Fun mode minecart velocity added " + Math.round(boostSpeed) + " b/t to projectile.",
                1000L
        );
    }
}
