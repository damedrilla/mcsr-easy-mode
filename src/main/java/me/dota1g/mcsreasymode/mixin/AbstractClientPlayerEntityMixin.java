package me.dota1g.mcsreasymode.mixin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import me.dota1g.mcsreasymode.client.skin.WorldSkinManager;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class AbstractClientPlayerEntityMixin {

    @Inject(method = "getSkinTexture", at = @At("RETURN"), cancellable = true)
    private void mcsreasymode$overrideWorldSkin(CallbackInfoReturnable<Identifier> cir) {
        AbstractClientPlayerEntity self = (AbstractClientPlayerEntity)(Object)this;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || !self.getUuid().equals(client.player.getUuid())) {
            return; // only reskin the local player
        }
        Identifier override = WorldSkinManager.getCurrentWorldSkin();
        if (override != null) {
            cir.setReturnValue(override);
        }
    }
}