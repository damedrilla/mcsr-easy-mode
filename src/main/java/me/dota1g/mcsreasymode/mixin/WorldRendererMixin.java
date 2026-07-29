package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
    private static final float MCSR_PLACEMENT_RED = 0.0F;
    private static final float MCSR_PLACEMENT_GREEN = 0.9F;
    private static final float MCSR_PLACEMENT_BLUE = 1.0F;
    private static final float MCSR_PLACEMENT_ALPHA = 0.8F;

    @Inject(method = "drawBlockOutline", at = @At("TAIL"))
    private static void mcsreasymode$drawPlacementOutline(
            MatrixStack matrices,
            VertexConsumer vertexConsumer,
            Entity entity,
            double cameraX,
            double cameraY,
            double cameraZ,
            BlockPos vanillaTarget,
            BlockState vanillaTargetState,
            CallbackInfo ci
    ) {
        if (!Mcsreasymode.shouldShowBlockPlacementOutline()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || client.options.hudHidden
                || client.crosshairTarget == null || client.crosshairTarget.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockHitResult hitResult = (BlockHitResult) client.crosshairTarget;
        ItemPlacementContext placementContext = mcsreasymode$getPlacementContext(client, hitResult);
        if (placementContext == null || !placementContext.canPlace()) {
            return;
        }

        BlockPos placementPos = placementContext.getBlockPos();
        boolean replacesVanillaTarget = vanillaTargetState.canReplace(placementContext);
        if (placementPos.equals(vanillaTarget) && !replacesVanillaTarget) {
            return;
        }

        Box outline = new Box(placementPos)
                .expand(0.002D)
                .offset(-cameraX, -cameraY, -cameraZ);
        WorldRenderer.drawBox(
                matrices,
                vertexConsumer,
                outline,
                MCSR_PLACEMENT_RED,
                MCSR_PLACEMENT_GREEN,
                MCSR_PLACEMENT_BLUE,
                MCSR_PLACEMENT_ALPHA
        );
    }

    private static ItemPlacementContext mcsreasymode$getPlacementContext(MinecraftClient client, BlockHitResult hitResult) {
        ItemStack mainHand = client.player.getMainHandStack();
        if (mainHand.getItem() instanceof BlockItem) {
            ItemPlacementContext context = new ItemPlacementContext(new ItemUsageContext(client.player, Hand.MAIN_HAND, hitResult));
            if (context.canPlace()) {
                return context;
            }
        }

        ItemStack offHand = client.player.getOffHandStack();
        if (offHand.getItem() instanceof BlockItem) {
            return new ItemPlacementContext(new ItemUsageContext(client.player, Hand.OFF_HAND, hitResult));
        }

        return null;
    }
}
