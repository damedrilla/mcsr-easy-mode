package me.dota1g.mcsreasymode.client;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dota1g.mcsreasymode.McsreasymodeConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;

public final class HotbarHotkeyOverlayRenderer {
    private static final int HOTBAR_WIDTH = 182;
    private static final int SLOT_SIZE = 20;
    private static final int HUD_SLOT_INSET = 1;
    private static final int SCREEN_SLOT_INSET = 1;
    private static final int LABEL_Z = 325;
    private static final int TEXT_COLOR = 0xFFFFFF;

    private HotbarHotkeyOverlayRenderer() {
    }

    public static void render(MatrixStack matrices, MinecraftClient client, McsreasymodeConfig config, int screenWidth, int screenHeight, String[] labels) {
        if (client == null || client.textRenderer == null || client.player == null || labels.length < 10) {
            return;
        }

        int hotbarX = screenWidth / 2 - HOTBAR_WIDTH / 2;
        int hotbarY = screenHeight - 22;
        for (int index = 0; index < 9; index++) {
            drawSlotLabel(matrices, client, config.hotbarHotkeyPosition, config.hotbarHotkeyTextSize, config.hotbarHotkeyBackground, labels[index], hotbarX + 1 + index * SLOT_SIZE, hotbarY + 1, HUD_SLOT_INSET);
        }

        ItemStack offHandStack = client.player.getOffHandStack();
        if (!offHandStack.isEmpty()) {
            int offhandX = client.player.getMainArm() == Arm.RIGHT ? hotbarX - 29 : hotbarX + HOTBAR_WIDTH + 10;
            drawSlotLabel(matrices, client, config.hotbarHotkeyPosition, config.hotbarHotkeyTextSize, config.hotbarHotkeyBackground, labels[9], offhandX + 1, hotbarY + 1, HUD_SLOT_INSET);
        }
    }

    public static void renderPreview(MatrixStack matrices, MinecraftClient client, McsreasymodeConfig config, int screenWidth, int y, String[] labels) {
        renderPreviewAt(matrices, client, config, screenWidth / 2 - HOTBAR_WIDTH / 2, y, labels);
    }

    public static void renderPreviewAt(MatrixStack matrices, MinecraftClient client, McsreasymodeConfig config, int hotbarX, int y, String[] labels) {
        if (client == null || client.textRenderer == null || labels.length < 10) {
            return;
        }

        DrawableHelper.fill(matrices, hotbarX, y, hotbarX + HOTBAR_WIDTH, y + 22, 0xAA202020);
        for (int index = 0; index < 9; index++) {
            int slotX = hotbarX + 1 + index * SLOT_SIZE;
            DrawableHelper.fill(matrices, slotX, y + 1, slotX + 18, y + 19, 0xAA555555);
            DrawableHelper.fill(matrices, slotX + 1, y + 2, slotX + 17, y + 18, 0xAA111111);
            drawSlotLabel(matrices, client, config.hotbarHotkeyPosition, config.hotbarHotkeyTextSize, config.hotbarHotkeyBackground, labels[index], slotX, y + 1, HUD_SLOT_INSET);
        }

        int offhandX = hotbarX - 29;
        DrawableHelper.fill(matrices, offhandX, y, offhandX + 22, y + 22, 0xAA202020);
        DrawableHelper.fill(matrices, offhandX + 1, y + 1, offhandX + 19, y + 19, 0xAA555555);
        DrawableHelper.fill(matrices, offhandX + 2, y + 2, offhandX + 18, y + 18, 0xAA111111);
        drawSlotLabel(matrices, client, config.hotbarHotkeyPosition, config.hotbarHotkeyTextSize, config.hotbarHotkeyBackground, labels[9], offhandX + 1, y + 1, HUD_SLOT_INSET);
    }

    public static void renderHandledPreview(MatrixStack matrices, MinecraftClient client, McsreasymodeConfig config, int screenWidth, int y, String[] labels) {
        renderHandledPreviewAt(matrices, client, config, screenWidth / 2 - HOTBAR_WIDTH / 2, y, labels);
    }

    public static void renderHandledPreviewAt(MatrixStack matrices, MinecraftClient client, McsreasymodeConfig config, int hotbarX, int y, String[] labels) {
        if (client == null || client.textRenderer == null || labels.length < 9) {
            return;
        }

        DrawableHelper.fill(matrices, hotbarX - 3, y - 3, hotbarX + HOTBAR_WIDTH + 3, y + 24, 0xAA303030);
        for (int index = 0; index < 9; index++) {
            int slotX = hotbarX + 1 + index * SLOT_SIZE;
            DrawableHelper.fill(matrices, slotX, y + 1, slotX + 18, y + 19, 0xAA8A8A8A);
            DrawableHelper.fill(matrices, slotX + 1, y + 2, slotX + 17, y + 18, 0xAA4D4D4D);
            drawSlotLabel(matrices, client, config.handledHotbarHotkeyPosition, config.handledHotbarHotkeyTextSize, config.handledHotbarHotkeyBackground, labels[index], slotX, y + 1, SCREEN_SLOT_INSET);
        }
    }

    public static void renderScreenSlotLabel(MatrixStack matrices, MinecraftClient client, McsreasymodeConfig config, String label, int slotX, int slotY) {
        if (client == null || client.textRenderer == null || config == null) {
            return;
        }

        drawSlotLabel(matrices, client, config.handledHotbarHotkeyPosition, config.handledHotbarHotkeyTextSize, config.handledHotbarHotkeyBackground, label, slotX, slotY, SCREEN_SLOT_INSET);
    }

    private static void drawSlotLabel(MatrixStack matrices, MinecraftClient client, McsreasymodeConfig.HotbarHotkeyPosition position, int textSize, McsreasymodeConfig.HotbarHotkeyBackground hotkeyBackground, String label, int slotX, int slotY, int slotInset) {
        if (label == null || label.isEmpty()) {
            return;
        }

        float scale = getTextScale(textSize);
        int textWidth = Math.round(client.textRenderer.getWidth(label) * scale);
        int textHeight = Math.round(9 * scale);
        int x = getLabelX(position, slotX, textWidth, slotInset);
        int y = getLabelY(position, slotY, textHeight, slotInset);
        int background = hotkeyBackground.color();

        RenderSystem.disableDepthTest();
        matrices.push();
        matrices.translate(0.0D, 0.0D, LABEL_Z);
        if ((background >>> 24) != 0) {
            DrawableHelper.fill(matrices, x - 1, y - 1, x + textWidth + 1, y + textHeight, background);
        }

        matrices.scale(scale, scale, 1.0F);
        client.textRenderer.drawWithShadow(matrices, label, x / scale, y / scale, TEXT_COLOR);
        matrices.pop();
        RenderSystem.enableDepthTest();
    }

    private static float getTextScale(int textSize) {
        switch (Math.max(1, Math.min(4, textSize))) {
            case 1:
                return 0.5F;
            case 3:
                return 1.0F;
            case 4:
                return 1.25F;
            case 2:
            default:
                return 0.75F;
        }
    }

    private static int getLabelX(McsreasymodeConfig.HotbarHotkeyPosition position, int slotX, int textWidth, int slotInset) {
        if (position == McsreasymodeConfig.HotbarHotkeyPosition.TOP_RIGHT || position == McsreasymodeConfig.HotbarHotkeyPosition.BOTTOM_RIGHT) {
            return slotX + 18 - textWidth - slotInset;
        }
        return slotX + slotInset;
    }

    private static int getLabelY(McsreasymodeConfig.HotbarHotkeyPosition position, int slotY, int textHeight, int slotInset) {
        if (position == McsreasymodeConfig.HotbarHotkeyPosition.BOTTOM_LEFT || position == McsreasymodeConfig.HotbarHotkeyPosition.BOTTOM_RIGHT) {
            return slotY + 18 - textHeight - slotInset;
        }
        return slotY + slotInset;
    }
}
