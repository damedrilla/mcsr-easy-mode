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

/** Transparent effect icons with duration labels in either HUD position. */
public final class StatusEffectTimerRenderer {
    private static final int ICON_SIZE = 18;
    private static final int ROW_HEIGHT = 32;
    private static final int COMPACT_ICON_SIZE = 12;
    private static final int COMPACT_ROW_HEIGHT = 22;
    private static final float COMPACT_TEXT_SCALE = 0.7F;

    private StatusEffectTimerRenderer() {
    }

    public static void render(MatrixStack matrices, MinecraftClient client, boolean aboveHunger) {
        if (client.player == null || client.options.hudHidden) return;
        List<StatusEffectInstance> beneficial = new ArrayList<>();
        List<StatusEffectInstance> harmful = new ArrayList<>();
        for (StatusEffectInstance effect : Ordering.natural().reverse().sortedCopy(client.player.getStatusEffects())) {
            if (!effect.shouldShowIcon()) continue;
            (effect.getEffectType().isBeneficial() ? beneficial : harmful).add(effect);
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        if (aboveHunger) {
            renderCompact(matrices, client, beneficial, harmful);
        } else {
            int width = client.getWindow().getScaledWidth();
            int top = 6 + (client.isDemo() ? 15 : 0);
            int rows = renderGroup(matrices, client, beneficial, width, top);
            renderGroup(matrices, client, harmful, width, top + Math.max(1, rows) * ROW_HEIGHT);
        }
        RenderSystem.color4f(1, 1, 1, 1);
    }

    private static void renderCompact(MatrixStack matrices, MinecraftClient client,
            List<StatusEffectInstance> beneficial, List<StatusEffectInstance> harmful) {
        // Hunger occupies the right half of the hotbar. Air bubbles can appear
        // immediately above it, so the lowest timer row ends above both bars.
        int left = client.getWindow().getScaledWidth() / 2 + 10;
        int rightEdge = client.getWindow().getScaledWidth() / 2 + 95;
        int right = rightEdge;
        int bottomRowY = client.getWindow().getScaledHeight() - 73;
        int row = 0;
        List<StatusEffectInstance> effects = new ArrayList<>(beneficial);
        effects.addAll(harmful);
        for (StatusEffectInstance effect : effects) {
            String duration = StatusEffectUtil.durationToString(effect, 1.0F);
            int textWidth = client.textRenderer.getWidth(duration);
            int cellWidth = Math.max(20, (int) Math.ceil(textWidth * COMPACT_TEXT_SCALE) + 4);
            if (right - cellWidth < left && right != rightEdge) {
                row++;
                right = rightEdge;
            }
            int center = right - cellWidth / 2;
            int y = bottomRowY - row * COMPACT_ROW_HEIGHT;
            Sprite sprite = client.getStatusEffectSpriteManager().getSprite(effect.getEffectType());
            client.getTextureManager().bindTexture(sprite.getAtlas().getId());
            RenderSystem.color4f(1, 1, 1, 1);
            DrawableHelper.drawSprite(matrices, center - COMPACT_ICON_SIZE / 2, y, 0,
                    COMPACT_ICON_SIZE, COMPACT_ICON_SIZE, sprite);
            matrices.push();
            matrices.translate(center, y + 13, 0);
            matrices.scale(COMPACT_TEXT_SCALE, COMPACT_TEXT_SCALE, 1.0F);
            client.textRenderer.drawWithShadow(matrices, duration, -textWidth / 2.0F, 0, 0xFFFFFF);
            matrices.pop();
            right -= cellWidth;
        }
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
