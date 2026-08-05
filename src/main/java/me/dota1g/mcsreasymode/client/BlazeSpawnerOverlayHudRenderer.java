package me.dota1g.mcsreasymode.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;

import java.util.Locale;

public final class BlazeSpawnerOverlayHudRenderer {
    private static final int GAP = 14;

    private BlazeSpawnerOverlayHudRenderer() {
    }

    public static void render(MatrixStack matrices, MinecraftClient client, int screenWidth, int screenHeight) {
        if (client == null || client.player == null || client.textRenderer == null) {
            return;
        }

        BlazeSpawnerOverlayState.Snapshot snapshot = BlazeSpawnerOverlayState.nearest(client.player);
        if (snapshot == null || snapshot.total() <= 0) {
            return;
        }

        String blocked = percentage("Blocked", snapshot.blocked(), snapshot.total());
        String bright = percentage("Too Bright", snapshot.tooBright(), snapshot.total());
        String available = percentage("Available", snapshot.available(), snapshot.total());
        int blockedWidth = client.textRenderer.getWidth(blocked);
        int brightWidth = client.textRenderer.getWidth(bright);
        int availableWidth = client.textRenderer.getWidth(available);
        int totalWidth = blockedWidth + brightWidth + availableWidth + GAP * 2;
        int x = screenWidth / 2 - totalWidth / 2;
        int y = screenHeight - 62;

        DrawableHelper.fill(matrices, x - 5, y - 4, x + totalWidth + 5, y + 12, 0x77000000);
        client.textRenderer.drawWithShadow(matrices, blocked, x, y, 0xFF5555);
        x += blockedWidth + GAP;
        client.textRenderer.drawWithShadow(matrices, bright, x, y, 0xFFFF55);
        x += brightWidth + GAP;
        client.textRenderer.drawWithShadow(matrices, available, x, y, 0x55FF55);
    }

    private static String percentage(String label, int count, int total) {
        return String.format(Locale.ROOT, "%s: %.1f%%", label, count * 100.0D / total);
    }
}
