package me.dota1g.mcsreasymode.client;

import me.dota1g.mcsreasymode.Mcsreasymode;
import me.dota1g.mcsreasymode.PieChartPath;
import me.dota1g.mcsreasymode.PieChartCycle;
import me.dota1g.mcsreasymode.PieChartKeyStore;
import me.dota1g.mcsreasymode.mixin.MinecraftClientPieAccessor;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.options.ControlsOptionsScreen;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.LiteralText;
import org.lwjgl.glfw.GLFW;
import java.io.IOException;

public final class PieChartShortcuts {
    public static KeyBinding cycleKey;
    private static final PieChartCycle CYCLE = new PieChartCycle();
    private static final PieChartKeyStore KEYS = new PieChartKeyStore(FabricLoader.getInstance().getConfigDir().resolve("mcsreasymode-pie-key.txt"));
    private static String savedKey;

    private PieChartShortcuts() { }

    public static void register() {
        cycleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.mcsreasymode.cyclePiePath",
                InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.categories.mcsreasymode"));
    }

    private static void loadKey() {
        if (savedKey != null) return;
        savedKey = cycleKey.getBoundKeyTranslationKey();
        try {
            String loaded = KEYS.load(savedKey);
            InputUtil.fromTranslationKey(loaded); // Validate with Minecraft before installing it.
            savedKey = loaded;
        } catch (IOException | IllegalArgumentException exception) {
            Mcsreasymode.LOGGER.warn("Unable to load pie shortcut key; using current binding", exception);
        }
    }

    private static void restoreKey() {
        loadKey();
        if (!savedKey.equals(cycleKey.getBoundKeyTranslationKey())) {
            cycleKey.setBoundKey(InputUtil.fromTranslationKey(savedKey));
            KeyBinding.updateKeysByCode();
        }
    }

    public static void tick(MinecraftClient client) {
        if (cycleKey == null || client.options == null) return;
        // Controls may be editing this binding; capture its final value when leaving that screen.
        if (!(client.currentScreen instanceof ControlsOptionsScreen)) restoreKey();
        while (cycleKey.wasPressed()) { /* Discard vanilla dispatch; direct input handles the shortcut. */ }
    }

    public static void beforeScreenChange(MinecraftClient client) {
        if (cycleKey == null || client.options == null) return;
        if (client.currentScreen instanceof ControlsOptionsScreen) {
            try {
                String current = cycleKey.getBoundKeyTranslationKey();
                KEYS.save(current);
                savedKey = current;
            } catch (IOException exception) {
                Mcsreasymode.LOGGER.warn("Unable to save pie shortcut key", exception);
            }
        } else restoreKey();
    }

    public static void bind(MinecraftClient client, InputUtil.Key key) throws IOException {
        String translation = key.getTranslationKey();
        KEYS.save(translation);
        savedKey = translation;
        cycleKey.setBoundKey(key);
        KeyBinding.updateKeysByCode();
        client.options.write();
        restoreKey();
    }

    private static boolean canUse(MinecraftClient client) {
        return cycleKey != null && client.options != null && client.world != null && client.player != null
                && client.currentScreen == null && Mcsreasymode.CONFIG != null;
    }

    public static boolean onKey(MinecraftClient client, int key, int scanCode, int action) {
        if (!canUse(client)) return false;
        restoreKey();
        if (cycleKey.isUnbound() || !cycleKey.matchesKey(key, scanCode)) return false;
        if (action == GLFW.GLFW_PRESS) cycle(client);
        return true; // Prevent number-key pie navigation or another binding overriding this selection.
    }

    public static boolean onMouse(MinecraftClient client, int button, int action) {
        if (!canUse(client)) return false;
        restoreKey();
        if (cycleKey.isUnbound() || !cycleKey.matchesMouse(button)) return false;
        if (action == GLFW.GLFW_PRESS) cycle(client);
        return true;
    }

    private static void cycle(MinecraftClient client) {
        MinecraftClientPieAccessor accessor = (MinecraftClientPieAccessor) client;
        PieChartPath target = CYCLE.next(Mcsreasymode.CONFIG.pieChartPaths, accessor.mcsreasymode$getPiePath());
        if (target == null) {
            client.player.sendMessage(new LiteralText("No valid pie paths. Add one in QoL > Pie Chart Paths."), true);
            return;
        }
        accessor.mcsreasymode$setPiePath(target.internalPath());
        client.options.debugEnabled = true;
        client.options.debugProfilerEnabled = true;
        client.player.sendMessage(new LiteralText("Pie: " + target.label()), true);
    }
}
