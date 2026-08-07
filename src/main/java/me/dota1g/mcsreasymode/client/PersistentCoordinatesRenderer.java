package me.dota1g.mcsreasymode.client;

import me.dota1g.mcsreasymode.McsreasymodeConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class PersistentCoordinatesRenderer {
    private PersistentCoordinatesRenderer() {
    }

    public static void render(MatrixStack matrices, MinecraftClient client, McsreasymodeConfig config) {
        if (client == null || client.player == null || client.textRenderer == null || config == null) {
            return;
        }

        BlockPos blockPos = client.player.getBlockPos();
        List<String> lines = new ArrayList<>();
        if (config.persistentCoordinatesPrecise) {
            lines.add(String.format(
                    Locale.ROOT,
                    "XYZ: %.3f / %.3f / %.3f",
                    client.player.getX(),
                    client.player.getY(),
                    client.player.getZ()
            ));
        }
        if (config.persistentCoordinatesBlock) {
            lines.add("Block: " + blockPos.getX() + " " + blockPos.getY() + " " + blockPos.getZ());
        }
        if (config.persistentCoordinatesLocalChunk) {
            lines.add("Chunk: "
                    + Math.floorMod(blockPos.getX(), 16) + " "
                    + Math.floorMod(blockPos.getY(), 16) + " "
                    + Math.floorMod(blockPos.getZ(), 16));
        }
        if (lines.isEmpty()) {
            return;
        }

        int x = 5;
        int y = 5;
        for (int index = 0; index < lines.size(); index++) {
            client.textRenderer.drawWithShadow(matrices, lines.get(index), x, y + index * 10, 0xFFFFFF);
        }
    }
}
