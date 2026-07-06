package me.dota1g.mcsreasymode.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.KeyBinding;

import java.util.Locale;

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
            labels[index] = labelFor(options.keysHotbar[index]);
        }
        labels[9] = labelFor(options.keySwapHands);
        return labels;
    }

    private static String[] fallback() {
        return new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "F"};
    }

    private static String labelFor(KeyBinding keyBinding) {
        String mouseLabel = mouseLabelFor(keyBinding.getBoundKeyTranslationKey());
        if (!mouseLabel.isEmpty()) {
            return mouseLabel;
        }

        return compact(keyBinding.getBoundKeyLocalizedText().getString());
    }

    private static String mouseLabelFor(String translationKey) {
        if (translationKey == null || !translationKey.startsWith("key.mouse.")) {
            return "";
        }

        String button = translationKey.substring("key.mouse.".length());
        if (button.equals("left")) {
            return "M1";
        }
        if (button.equals("right")) {
            return "M2";
        }
        if (button.equals("middle")) {
            return "M3";
        }

        try {
            return "M" + Integer.parseInt(button);
        } catch (NumberFormatException ignored) {
            return "";
        }
    }

    private static String compact(String label) {
        if (label == null || label.isEmpty()) {
            return "";
        }

        label = label.trim();
        String lower = label.toLowerCase(Locale.ROOT);
        if (lower.equals("left button")) {
            return "M1";
        }
        if (lower.equals("right button")) {
            return "M2";
        }
        if (lower.equals("middle button")) {
            return "M3";
        }
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
