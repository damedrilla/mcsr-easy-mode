package me.dota1g.mcsreasymode.client;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.StringRenderable;
import net.minecraft.text.Text;

import java.util.List;

public final class McsreasymodeTooltip {
    private static final int MAX_WIDTH = 200;

    private McsreasymodeTooltip() {
    }

    public static boolean isHovered(TextRenderer textRenderer, String label, int x, int y, int mouseX, int mouseY) {
        return mouseX > x
                && mouseX < x + textRenderer.getWidth(label)
                && mouseY > y
                && mouseY < y + textRenderer.fontHeight;
    }

    public static List<StringRenderable> wrap(TextRenderer textRenderer, Text tooltip) {
        return textRenderer.wrapLines(tooltip, MAX_WIDTH);
    }

    public static int clampedY(List<StringRenderable> lines, int mouseY, int minY, int maxY) {
        int height = lines.size() * 10;
        int y = Math.min(mouseY, maxY - height);
        return Math.max(y, minY - height);
    }
}
