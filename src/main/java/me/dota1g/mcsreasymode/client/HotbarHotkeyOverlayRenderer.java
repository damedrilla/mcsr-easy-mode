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
    private static final int SLOT_INSET = 3;
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
            drawSlotLabel(matrices, client, config, labels[index], hotbarX + 1 + index * SLOT_SIZE, hotbarY + 1);
        }

        ItemStack offHandStack = client.player.getOffHandStack();
        if (!offHandStack.isEmpty()) {
            int offhandX = client.player.getMainArm() == Arm.RIGHT ? hotbarX - 29 : hotbarX + HOTBAR_WIDTH + 10;
            drawSlotLabel(matrices, client, config, labels[9], offhandX + 1, hotbarY + 1);
        }
    }

    public static void renderPreview(MatrixStack matrices, MinecraftClient client, McsreasymodeConfig config, int screenWidth, int y, String[] labels) {
        if (client == null || client.textRenderer == null || labels.length < 10) {
            return;
        }

        int hotbarX = screenWidth / 2 - HOTBAR_WIDTH / 2;
        DrawableHelper.fill(matrices, hotbarX, y, hotbarX + HOTBAR_WIDTH, y + 22, 0xAA202020);
        for (int index = 0; index < 9; index++) {
            int slotX = hotbarX + 1 + index * SLOT_SIZE;
            DrawableHelper.fill(matrices, slotX, y + 1, slotX + 18, y + 19, 0xAA555555);
            DrawableHelper.fill(matrices, slotX + 1, y + 2, slotX + 17, y + 18, 0xAA111111);
            drawSlotLabel(matrices, client, config, labels[index], slotX, y + 1);
        }

        int offhandX = hotbarX - 29;
        DrawableHelper.fill(matrices, offhandX, y, offhandX + 22, y + 22, 0xAA202020);
        DrawableHelper.fill(matrices, offhandX + 1, y + 1, offhandX + 19, y + 19, 0xAA555555);
        DrawableHelper.fill(matrices, offhandX + 2, y + 2, offhandX + 18, y + 18, 0xAA111111);
        drawSlotLabel(matrices, client, config, labels[9], offhandX + 1, y + 1);
    }

    private static void drawSlotLabel(MatrixStack matrices, MinecraftClient client, McsreasymodeConfig config, String label, int slotX, int slotY) {
        if (label == null || label.isEmpty()) {
            return;
        }

        float scale = getTextScale(config);
        int textWidth = Math.round(client.textRenderer.getWidth(label) * scale);
        int textHeight = Math.round(9 * scale);
        int x = getLabelX(config.hotbarHotkeyPosition, slotX, textWidth);
        int y = getLabelY(config.hotbarHotkeyPosition, slotY, textHeight);
        int background = config.hotbarHotkeyBackground.color();

        RenderSystem.disableDepthTest();
        if ((background >>> 24) != 0) {
            DrawableHelper.fill(matrices, x - 1, y - 1, x + textWidth + 1, y + textHeight, background);
        }

        matrices.push();
        matrices.scale(scale, scale, 1.0F);
        client.textRenderer.drawWithShadow(matrices, label, x / scale, y / scale, TEXT_COLOR);
        matrices.pop();
        RenderSystem.enableDepthTest();
    }

    private static float getTextScale(McsreasymodeConfig config) {
        switch (Math.max(1, Math.min(4, config.hotbarHotkeyTextSize))) {
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

    private static int getLabelX(McsreasymodeConfig.HotbarHotkeyPosition position, int slotX, int textWidth) {
        if (position == McsreasymodeConfig.HotbarHotkeyPosition.TOP_RIGHT || position == McsreasymodeConfig.HotbarHotkeyPosition.BOTTOM_RIGHT) {
            return slotX + 18 - textWidth - SLOT_INSET;
        }
        return slotX + SLOT_INSET;
    }

    private static int getLabelY(McsreasymodeConfig.HotbarHotkeyPosition position, int slotY, int textHeight) {
        if (position == McsreasymodeConfig.HotbarHotkeyPosition.BOTTOM_LEFT || position == McsreasymodeConfig.HotbarHotkeyPosition.BOTTOM_RIGHT) {
            return slotY + 18 - textHeight - 1;
        }
        return slotY + 1;
    }
}
