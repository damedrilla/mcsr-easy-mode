package me.dota1g.mcsreasymode;

import me.contaria.speedrunapi.config.SpeedrunConfigContainer;
import me.contaria.speedrunapi.config.api.SpeedrunConfig;
import me.contaria.speedrunapi.config.api.SpeedrunConfigParsedMetadata;
import me.contaria.speedrunapi.config.api.annotations.Config;
import com.google.gson.JsonObject;
import me.dota1g.mcsreasymode.client.McsreasymodeConfigScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

@Config(init = Config.InitPoint.PRELAUNCH)
public class McsreasymodeConfig implements SpeedrunConfig {
    @Config.Category("rng")
    @Config.Name("speedrunapi.config.mcsreasymode.option.rngMode")
    @Config.Description("speedrunapi.config.mcsreasymode.option.rngMode.description")
    @Config.Text(getter = "getRngModeText")
    public RngMode rngMode = RngMode.VANILLA;

    @Config.Category("mobs")
    @Config.Name("speedrunapi.config.mcsreasymode.option.disablePiglinAggression")
    @Config.Description("speedrunapi.config.mcsreasymode.option.disablePiglinAggression.description")
    public boolean disablePiglinAggression = true;

    @Config.Category("mobs")
    @Config.Name("speedrunapi.config.mcsreasymode.option.disableGhastAggression")
    @Config.Description("speedrunapi.config.mcsreasymode.option.disableGhastAggression.description")
    public boolean disableGhastAggression = true;

    @Config.Category("mobs")
    @Config.Name("speedrunapi.config.mcsreasymode.option.disableHoglinAggression")
    @Config.Description("speedrunapi.config.mcsreasymode.option.disableHoglinAggression.description")
    public boolean disableHoglinAggression = true;

    @Config.Hide
    public boolean disableAggression = true;

    @Config.Category("ui")
    @Config.Name("speedrunapi.config.mcsreasymode.option.moveSaveAndQuitButton")
    @Config.Description("speedrunapi.config.mcsreasymode.option.moveSaveAndQuitButton.description")
    public boolean moveSaveAndQuitButton = true;

    @Config.Category("ui")
    @Config.Name("speedrunapi.config.mcsreasymode.option.showHotbarHotkeys")
    @Config.Description("speedrunapi.config.mcsreasymode.option.showHotbarHotkeys.description")
    public boolean showHotbarHotkeys = true;

    @Config.Category("ui")
    @Config.Name("speedrunapi.config.mcsreasymode.option.hotbarHotkeyPosition")
    @Config.Description("speedrunapi.config.mcsreasymode.option.hotbarHotkeyPosition.description")
    @Config.Text(getter = "getHotbarHotkeyPositionText")
    public HotbarHotkeyPosition hotbarHotkeyPosition = HotbarHotkeyPosition.BOTTOM_RIGHT;

    @Config.Category("ui")
    @Config.Name("speedrunapi.config.mcsreasymode.option.hotbarHotkeyTextSize")
    @Config.Description("speedrunapi.config.mcsreasymode.option.hotbarHotkeyTextSize.description")
    @Config.Numbers.Whole.Bounds(min = 1, max = 4)
    public int hotbarHotkeyTextSize = 2;

    @Config.Category("ui")
    @Config.Name("speedrunapi.config.mcsreasymode.option.hotbarHotkeyBackground")
    @Config.Description("speedrunapi.config.mcsreasymode.option.hotbarHotkeyBackground.description")
    @Config.Text(getter = "getHotbarHotkeyBackgroundText")
    public HotbarHotkeyBackground hotbarHotkeyBackground = HotbarHotkeyBackground.DARK;

    @Config.Ignored
    public SpeedrunConfigContainer<?> container;

    {
        Mcsreasymode.CONFIG = this;
    }

    @Override
    public boolean isAvailable() {
        return MinecraftClient.getInstance().world == null;
    }

    @Override
    public void finishInitialization(SpeedrunConfigContainer<?> container) {
        this.container = container;
    }

    @Override
    public @NotNull Screen createConfigScreen(Screen parent) {
        return new McsreasymodeConfigScreen(this, parent);
    }

    public Text getRngModeText(RngMode rngMode) {
        return new LiteralText(rngMode.displayName);
    }

    public Text getHotbarHotkeyPositionText(HotbarHotkeyPosition position) {
        return new LiteralText(position.displayName);
    }

    public Text getHotbarHotkeyBackgroundText(HotbarHotkeyBackground background) {
        return new LiteralText(background.displayName);
    }

    @Override
    public void onLoad(JsonObject jsonObject, SpeedrunConfigParsedMetadata metadata) {
        if (jsonObject.has("disableAggression")) {
            boolean oldValue = jsonObject.get("disableAggression").getAsBoolean();
            if (!jsonObject.has("disablePiglinAggression")) {
                jsonObject.addProperty("disablePiglinAggression", oldValue);
            }
            if (!jsonObject.has("disableGhastAggression")) {
                jsonObject.addProperty("disableGhastAggression", oldValue);
            }
            if (!jsonObject.has("disableHoglinAggression")) {
                jsonObject.addProperty("disableHoglinAggression", oldValue);
            }
        }
    }

    @Override
    public String modID() {
        return Mcsreasymode.MOD_ID;
    }

    public enum RngMode {
        VANILLA("Vanilla"),
        RANKED("Ranked");

        private final String displayName;

        RngMode(String displayName) {
            this.displayName = displayName;
        }

        public String displayName() {
            return this.displayName;
        }
    }

    public enum HotbarHotkeyPosition {
        TOP_LEFT("Top Left"),
        TOP_RIGHT("Top Right"),
        BOTTOM_LEFT("Bottom Left"),
        BOTTOM_RIGHT("Bottom Right");

        private final String displayName;

        HotbarHotkeyPosition(String displayName) {
            this.displayName = displayName;
        }

        public String displayName() {
            return this.displayName;
        }

        public HotbarHotkeyPosition next() {
            HotbarHotkeyPosition[] values = values();
            return values[(this.ordinal() + 1) % values.length];
        }
    }

    public enum HotbarHotkeyBackground {
        DARK("Dark", 0x88000000),
        LIGHT("Light", 0x88FFFFFF),
        BLUE("Blue", 0x883050B8),
        NONE("None", 0x00000000);

        private final String displayName;
        private final int color;

        HotbarHotkeyBackground(String displayName, int color) {
            this.displayName = displayName;
            this.color = color;
        }

        public String displayName() {
            return this.displayName;
        }

        public int color() {
            return this.color;
        }

        public HotbarHotkeyBackground next() {
            HotbarHotkeyBackground[] values = values();
            return values[(this.ordinal() + 1) % values.length];
        }
    }
}
