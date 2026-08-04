package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RayTraceContext;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public abstract class ServerPlayerInteractionManagerMixin {
    @Unique
    private boolean mcsreasymode$guardedBucketUse;

    @Unique
    private BlockHitResult mcsreasymode$bucketTarget;

    @Inject(method = "interactItem", at = @At("HEAD"))
    private void mcsreasymode$captureBucketTarget(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        this.mcsreasymode$guardedBucketUse = Mcsreasymode.shouldGuardBucketDesync()
                && stack.getItem() instanceof BucketItem;
        this.mcsreasymode$bucketTarget = null;
        if (!this.mcsreasymode$guardedBucketUse) {
            return;
        }

        RayTraceContext.FluidHandling fluidHandling = stack.getItem() == Items.BUCKET
                ? RayTraceContext.FluidHandling.SOURCE_ONLY
                : RayTraceContext.FluidHandling.NONE;
        this.mcsreasymode$bucketTarget = ItemInvoker.mcsreasymode$rayTrace(world, player, fluidHandling);
    }

    @Inject(method = "interactItem", at = @At("RETURN"))
    private void mcsreasymode$correctBucketState(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (!this.mcsreasymode$guardedBucketUse) {
            return;
        }
        this.mcsreasymode$guardedBucketUse = false;

        int inventorySlot = hand == Hand.MAIN_HAND
                ? player.inventory.selectedSlot
                : player.inventory.main.size() + player.inventory.armor.size();
        player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(
                -2,
                inventorySlot,
                player.getStackInHand(hand).copy()
        ));

        if (this.mcsreasymode$bucketTarget != null
                && this.mcsreasymode$bucketTarget.getType() == HitResult.Type.BLOCK) {
            BlockPos targetPos = this.mcsreasymode$bucketTarget.getBlockPos();
            BlockPos adjacentPos = targetPos.offset(this.mcsreasymode$bucketTarget.getSide());
            player.networkHandler.sendPacket(new BlockUpdateS2CPacket(world, targetPos));
            player.networkHandler.sendPacket(new BlockUpdateS2CPacket(world, adjacentPos));
        }
        this.mcsreasymode$bucketTarget = null;

        ActionResult result = cir.getReturnValue();
        boolean accepted = result != null && result.isAccepted();
        Mcsreasymode.debugRateLimited(
                "ui.bucket_desync_guard." + player.getEntityId() + "." + accepted,
                accepted
                        ? "Bucket Desync Guard synchronized an accepted fluid interaction."
                        : "Bucket Desync Guard corrected a rejected fluid interaction.",
                1000L
        );
    }
}
