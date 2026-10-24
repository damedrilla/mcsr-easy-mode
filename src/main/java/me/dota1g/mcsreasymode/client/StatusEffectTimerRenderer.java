package me.dota1g.mcsreasymode.client;

import com.google.common.collect.Ordering;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;

import java.util.ArrayList;
import java.util.List;

/** Transparent effect icons with centered duration labels at the top right of the HUD. */
public final class StatusEffectTimerRenderer {
    private static final int ICON_SIZE = 18;
    private static final int ROW_HEIGHT = 32;

    private StatusEffectTimerRenderer() {
    }

    public static void render(MatrixStack matrices, MinecraftClient client) {
        if (client.player == null || client.options.hudHidden) return;
        List<StatusEffectInstance> beneficial = new ArrayList<>();
        List<StatusEffectInstance> harmful = new ArrayList<>();
        for (StatusEffectInstance effect : Ordering.natural().reverse().sortedCopy(client.player.getStatusEffects())) {
            if (!effect.shouldShowIcon()) continue;
            (effect.getEffectType().isBeneficial() ? beneficial : harmful).add(effect);
        }
        int width = client.getWindow().getScaledWidth();
        int top = 6 + (client.isDemo() ? 15 : 0);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        int rows = renderGroup(matrices, client, beneficial, width, top);
        renderGroup(matrices, client, harmful, width, top + Math.max(1, rows) * ROW_HEIGHT);
        RenderSystem.color4f(1, 1, 1, 1);
    }

    private static int renderGroup(MatrixStack matrices, MinecraftClient client,
            List<StatusEffectInstance> effects, int width, int top) {
        if (effects.isEmpty()) return 0;
        int right = width - 6;
        int row = 0;
        for (StatusEffectInstance effect : effects) {
            // Match the inventory's timer, including permanent effects and long durations.
            String duration = StatusEffectUtil.durationToString(effect, 1.0F);
            int textWidth = client.textRenderer.getWidth(duration);
            int cellWidth = Math.max(28, textWidth + 6);
            if (right - cellWidth < 6 && right != width - 6) {
                row++;
                right = width - 6;
            }
            int center = right - cellWidth / 2;
            int y = top + row * ROW_HEIGHT;
            Sprite sprite = client.getStatusEffectSpriteManager().getSprite(effect.getEffectType());
            client.getTextureManager().bindTexture(sprite.getAtlas().getId());
            RenderSystem.color4f(1, 1, 1, 1);
            DrawableHelper.drawSprite(matrices, center - ICON_SIZE / 2, y, 0, ICON_SIZE, ICON_SIZE, sprite);
            client.textRenderer.drawWithShadow(matrices, duration, center - textWidth / 2.0F, y + 20, 0xFFFFFF);
            right -= cellWidth;
        }
        return row + 1;
    }
}
