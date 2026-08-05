package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import me.dota1g.mcsreasymode.client.BlazeSpawnerOverlayState;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.MobSpawnerBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;

@Mixin(MobSpawnerBlockEntityRenderer.class)
public abstract class MobSpawnerBlockEntityRendererMixin {
    @Inject(
            method = "render(Lnet/minecraft/block/entity/MobSpawnerBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V",
            at = @At("TAIL")
    )
    private void mcsreasymode$renderBlazeSpawnerOverlay(
            MobSpawnerBlockEntity spawner,
            float tickDelta,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            int overlay,
            CallbackInfo ci
    ) {
        if (!Mcsreasymode.shouldShowBlazeSpawnerOverlay()) {
            return;
        }

        BlazeSpawnerOverlayState.Snapshot snapshot = BlazeSpawnerOverlayState.observe(spawner);
        if (snapshot == null || !Mcsreasymode.shouldShowBlazeSpawnerTimer()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;
        String text = String.format(Locale.ROOT, "%.1fs", snapshot.spawnDelay() / 20.0F);
        matrices.push();
        matrices.translate(0.5D, 1.45D, 0.5D);
        matrices.multiply(client.getEntityRenderManager().getRotation());
        matrices.scale(-0.025F, -0.025F, 0.025F);
        Matrix4f matrix = matrices.peek().getModel();
        float x = -textRenderer.getWidth(text) / 2.0F;
        textRenderer.draw(text, x, 0.0F, 0xFF55FFFF, false, matrix, vertexConsumers, true, 0, 0xF000F0);
        matrices.pop();
    }
}
