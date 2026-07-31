package me.dota1g.mcsreasymode.client;

import me.dota1g.mcsreasymode.McsreasymodeMinecartVelocityState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;

public final class MinecartVelocityDebugRenderer {
    private MinecartVelocityDebugRenderer() {
    }

    public static void render(MatrixStack matrices, MinecraftClient client, int screenWidth, int screenHeight) {
        if (client == null || client.player == null || client.textRenderer == null) {
            return;
        }

        double minecartSpeed = McsreasymodeMinecartVelocityState.minecartChargeSpeed(client.player);
        double bowSpeed = isHoldingBow(client) ? McsreasymodeMinecartVelocityState.projectileBoostSpeed(client.player) : 0.0D;
        int x = screenWidth / 2 + 96;
        int y = screenHeight - 58;
        String minecartText = "Minecart: " + Math.round(minecartSpeed) + " b/t";
        String bowText = "Bow: " + Math.round(bowSpeed) + " b/t";
        int width = Math.max(client.textRenderer.getWidth(minecartText), client.textRenderer.getWidth(bowText)) + 8;

        DrawableHelper.fill(matrices, x - 4, y - 4, x + width, y + 22, 0x88000000);
        client.textRenderer.drawWithShadow(matrices, minecartText, x, y, 0x55FFFF);
        client.textRenderer.drawWithShadow(matrices, bowText, x, y + 11, bowSpeed > 0.0D ? 0xFFFF55 : 0xFFFFFF);
    }

    private static boolean isHoldingBow(MinecraftClient client) {
        ItemStack mainHand = client.player.getMainHandStack();
        ItemStack offHand = client.player.getOffHandStack();
        return mainHand.getItem() instanceof BowItem || offHand.getItem() instanceof BowItem;
    }
}
