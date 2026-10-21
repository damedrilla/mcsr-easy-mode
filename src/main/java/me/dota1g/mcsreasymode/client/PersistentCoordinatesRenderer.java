package me.dota1g.mcsreasymode.client;

import me.dota1g.mcsreasymode.McsreasymodeConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

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

        Vec3d position = client.player.getPos();
        Vec3d virtualPosition = client.world == null ? null : VirtualCoordinatesCompat.position(
                position.x, position.y, position.z, client.world.getRegistryKey());
        String marker = virtualPosition == null ? "" : " (V)";
        if (virtualPosition != null) {
            position = virtualPosition;
        }
        BlockPos blockPos = new BlockPos(position);
        List<String> lines = new ArrayList<>();
        if (config.persistentCoordinatesPrecise) {
            lines.add(String.format(
                    Locale.ROOT,
                    "XYZ%s: %.3f / %.3f / %.3f",
                    marker,
                    position.x,
                    position.y,
                    position.z
            ));
        }
        if (config.persistentCoordinatesBlock) {
            lines.add("B" + marker + ": " + blockPos.getX() + " " + blockPos.getY() + " " + blockPos.getZ());
        }
        if (config.persistentCoordinatesLocalChunk) {
            lines.add("LC" + marker + ": "
                    + Math.floorMod(blockPos.getX(), 16) + " "
                    + Math.floorMod(blockPos.getY(), 16) + " "
                    + Math.floorMod(blockPos.getZ(), 16));
        }
        if (config.persistentCoordinatesRotation) {
            Direction facing = client.player.getHorizontalFacing();
            String axis = facing.getAxis() == Direction.Axis.X ? "X" : "Z";
            String sign = facing.getDirection() == Direction.AxisDirection.POSITIVE ? "+" : "-";
            String compass = facing.getName();
            compass = compass.substring(0, 1).toUpperCase(Locale.ROOT) + compass.substring(1);
            lines.add(String.format(Locale.ROOT, "Y: %.1f P: %.1f FD: %s (%s%s)",
                    MathHelper.wrapDegrees(client.player.yaw),
                    MathHelper.wrapDegrees(client.player.pitch), compass, sign, axis));
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
