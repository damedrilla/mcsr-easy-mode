package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.BedItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BoatItem;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow private int itemUseCooldown;
    @Shadow public ClientPlayerEntity player;
    @Shadow public HitResult crosshairTarget;

    @Inject(
            method = "doItemUse",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/MinecraftClient;itemUseCooldown:I",
                    opcode = Opcodes.PUTFIELD,
                    shift = At.Shift.AFTER
            )
    )
    private void mcsreasymode$applyHeldPlaceDelay(CallbackInfo ci) {
        if (this.player == null || this.isTargetingEntity()) {
            return;
        }

        boolean hasFastRepeatItem = false;
        for (Hand hand : Hand.values()) {
            Item item = this.player.getStackInHand(hand).getItem();
            if (item instanceof BucketItem || item instanceof BedItem) {
                return;
            }
            if (item instanceof BlockItem || item instanceof BoatItem) {
                hasFastRepeatItem = true;
            }
        }

        if (hasFastRepeatItem) {
            this.itemUseCooldown = Mcsreasymode.heldPlaceDelayTicks();
        }
    }

    private boolean isTargetingEntity() {
        return this.crosshairTarget != null && this.crosshairTarget.getType() == HitResult.Type.ENTITY;
    }
}
