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
}
