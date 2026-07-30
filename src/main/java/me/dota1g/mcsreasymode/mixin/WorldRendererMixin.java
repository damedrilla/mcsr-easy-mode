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
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Heightmap;
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
    private static final float MCSR_TREASURE_RED = 1.0F;
    private static final float MCSR_TREASURE_GREEN = 0.72F;
    private static final float MCSR_TREASURE_BLUE = 0.0F;
    private static final float MCSR_TREASURE_ALPHA = 0.9F;

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
        boolean showPlacementOutline = Mcsreasymode.shouldShowBlockPlacementOutline();
        boolean showTreasureAssist = Mcsreasymode.shouldShowBuriedTreasureChunkAssist();
        if (!showPlacementOutline && !showTreasureAssist) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || client.options.hudHidden) {
            return;
        }

        if (showTreasureAssist) {
            mcsreasymode$drawBuriedTreasureMarker(matrices, vertexConsumer, client, cameraX, cameraY, cameraZ);
        }

        if (!showPlacementOutline || client.crosshairTarget == null
                || client.crosshairTarget.getType() != HitResult.Type.BLOCK) {
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

    private static void mcsreasymode$drawBuriedTreasureMarker(
            MatrixStack matrices,
            VertexConsumer vertexConsumer,
            MinecraftClient client,
            double cameraX,
            double cameraY,
            double cameraZ
    ) {
        int playerBlockX = MathHelper.floor(client.player.getX());
        int playerBlockZ = MathHelper.floor(client.player.getZ());
        int markerX = (playerBlockX >> 4 << 4) + 9;
        int markerZ = (playerBlockZ >> 4 << 4) + 9;
        int markerY = client.world.getTopY(Heightmap.Type.WORLD_SURFACE, markerX, markerZ) - 1;
        if (markerY < 0) {
            return;
        }

        BlockPos markerPos = new BlockPos(markerX, markerY, markerZ);
        Box outline = new Box(markerPos)
                .expand(0.004D)
                .offset(-cameraX, -cameraY, -cameraZ);
        WorldRenderer.drawBox(
                matrices,
                vertexConsumer,
                outline,
                MCSR_TREASURE_RED,
                MCSR_TREASURE_GREEN,
                MCSR_TREASURE_BLUE,
                MCSR_TREASURE_ALPHA
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
