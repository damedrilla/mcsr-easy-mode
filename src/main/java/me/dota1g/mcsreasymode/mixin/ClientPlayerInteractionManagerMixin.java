package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BucketItem;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {
    @Shadow
    @Final
    private ClientPlayNetworkHandler networkHandler;

    @Inject(
            method = "interactItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V"
            )
    )
    private void mcsreasymode$syncBucketClickPose(PlayerEntity player, World world, Hand hand, CallbackInfoReturnable<?> cir) {
        if (!Mcsreasymode.shouldGuardBucketDesync()
                || !(player.getStackInHand(hand).getItem() instanceof BucketItem)) {
            return;
        }

        if (player.hasVehicle()) {
            this.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookOnly(player.yaw, player.pitch, player.isOnGround()));
            return;
        }

        this.networkHandler.sendPacket(new PlayerMoveC2SPacket.Both(
                player.getX(),
                player.getY(),
                player.getZ(),
                player.yaw,
                player.pitch,
                player.isOnGround()
        ));
    }
}
