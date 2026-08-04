package me.dota1g.mcsreasymode.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public final class GodsensBoatStatusRenderer {
    private GodsensBoatStatusRenderer() {
    }

    public static void render(MatrixStack matrices, MinecraftClient client) {
        if (client == null || client.player == null || client.textRenderer == null) {
            return;
        }

        GodsensBoatStatusState.Status status = GodsensBoatStatusState.getStatus();
        float alpha = GodsensBoatStatusState.getIconAlpha();
        if (alpha <= 0.0F) {
            return;
        }

        int x = 5;
        int y = 5;
        int right = x + 20;
        int bottom = y + 20;
        int frameAlpha = Math.max(0, Math.min(255, Math.round(255.0F * alpha)));
        int backgroundAlpha = Math.max(0, Math.min(0x88, Math.round(0x88 * alpha)));
        int frameColor = frameAlpha << 24 | status.color();
        int backgroundColor = backgroundAlpha << 24;

        DrawableHelper.fill(matrices, x, y, right, bottom, backgroundColor);
        DrawableHelper.fill(matrices, x, y, right, y + 1, frameColor);
        DrawableHelper.fill(matrices, x, bottom - 1, right, bottom, frameColor);
        DrawableHelper.fill(matrices, x, y, x + 1, bottom, frameColor);
        DrawableHelper.fill(matrices, right - 1, y, right, bottom, frameColor);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
        client.getItemRenderer().renderInGui(new ItemStack(Items.ENDER_EYE), x + 2, y + 2);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
