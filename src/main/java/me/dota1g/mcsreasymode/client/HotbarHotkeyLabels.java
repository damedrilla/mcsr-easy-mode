package me.dota1g.mcsreasymode.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.GameOptions;

public final class HotbarHotkeyLabels {
    private HotbarHotkeyLabels() {
    }

    public static String[] get(MinecraftClient client) {
        String[] labels = new String[10];
        if (client == null) {
            return fallback();
        }

        GameOptions options = client.options;
        for (int index = 0; index < 9; index++) {
            labels[index] = compact(options.keysHotbar[index].getBoundKeyLocalizedText().asString());
        }
        labels[9] = compact(options.keySwapHands.getBoundKeyLocalizedText().asString());
        return labels;
    }

    private static String[] fallback() {
        return new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "F"};
    }

    private static String compact(String label) {
        if (label == null || label.isEmpty()) {
            return "";
        }

        String lower = label.toLowerCase();
        if (lower.startsWith("mouse button ")) {
            return "M" + label.substring("mouse button ".length()).trim();
        }
        if (lower.startsWith("button ")) {
            return "M" + label.substring("button ".length()).trim();
        }
        if (lower.startsWith("mouse ")) {
            return "M" + label.substring("mouse ".length()).trim();
        }

        return label;
    }
}
