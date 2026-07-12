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

    @Config.Hide
    public boolean rankedIronGolemDrops = false;

    @Config.Hide
    public boolean rankedEyeBreaks = false;

    @Config.Hide
    public boolean rankedBlazeRods = false;

    @Config.Hide
    public boolean rankedPiglinBarters = false;

    @Config.Hide
    public boolean rankedPiglinString = false;

    @Config.Hide
    public boolean rankedFlint = false;

    @Config.Hide
    public boolean rankedBlindPortal = false;

    @Config.Hide
    public boolean rankedBastionChestLoot = false;

    @Config.Hide
    public boolean rankedChestLootTables = false;

    @Config.Hide
    public boolean rankedHoglinStableRamparts = false;

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

    @Config.Category("worldgen")
    @Config.Name("speedrunapi.config.mcsreasymode.option.openNetherTerrain")
    @Config.Description("speedrunapi.config.mcsreasymode.option.openNetherTerrain.description")
    public boolean openNetherTerrain = false;

    @Config.Category("worldgen")
    @Config.Name("speedrunapi.config.mcsreasymode.option.netherTerrainXzScale")
    @Config.Description("speedrunapi.config.mcsreasymode.option.netherTerrainXzScale.description")
    @Config.Numbers.Fractional.Bounds(min = 0.52F, max = 1.0F)
    public double netherTerrainXzScale = 1.0D;

    @Config.Category("worldgen")
    @Config.Name("speedrunapi.config.mcsreasymode.option.netherTerrainDensityFactor")
    @Config.Description("speedrunapi.config.mcsreasymode.option.netherTerrainDensityFactor.description")
    @Config.Numbers.Fractional.Bounds(min = 0.0F, max = 0.08215F)
    public double netherTerrainDensityFactor = 0.0D;

    @Config.Category("worldgen")
    @Config.Name("speedrunapi.config.mcsreasymode.option.netherTerrainDensityOffset")
    @Config.Description("speedrunapi.config.mcsreasymode.option.netherTerrainDensityOffset.description")
    @Config.Numbers.Fractional.Bounds(min = 0.01174F, max = 0.019921875F)
    public double netherTerrainDensityOffset = 0.019921875D;

    @Config.Category("worldgen")
    @Config.Name("speedrunapi.config.mcsreasymode.option.standardizeVillages")
    @Config.Description("speedrunapi.config.mcsreasymode.option.standardizeVillages.description")
    public boolean standardizeVillages = false;

    @Config.Category("worldgen")
    @Config.Name("speedrunapi.config.mcsreasymode.option.strongholdAntiCorruption")
    @Config.Description("speedrunapi.config.mcsreasymode.option.strongholdAntiCorruption.description")
    public boolean strongholdAntiCorruption = false;

    @Config.Category("ui")
    @Config.Name("speedrunapi.config.mcsreasymode.option.moveSaveAndQuitButton")
    @Config.Description("speedrunapi.config.mcsreasymode.option.moveSaveAndQuitButton.description")
    public boolean moveSaveAndQuitButton = true;

    @Config.Category("ui")
    @Config.Name("speedrunapi.config.mcsreasymode.option.showHotbarHotkeys")
    @Config.Description("speedrunapi.config.mcsreasymode.option.showHotbarHotkeys.description")
    public boolean showHotbarHotkeys = true;

    @Config.Category("ui")
    @Config.Name("speedrunapi.config.mcsreasymode.option.showDebugChatLogs")
    @Config.Description("speedrunapi.config.mcsreasymode.option.showDebugChatLogs.description")
    public boolean showDebugChatLogs = false;

    @Config.Category("ui")
    @Config.Name("speedrunapi.config.mcsreasymode.option.hideAdvancementToasts")
    @Config.Description("speedrunapi.config.mcsreasymode.option.hideAdvancementToasts.description")
    public boolean hideAdvancementToasts = false;

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

    @Config.Category("ui")
    @Config.Name("speedrunapi.config.mcsreasymode.option.handledHotbarHotkeyPosition")
    @Config.Description("speedrunapi.config.mcsreasymode.option.handledHotbarHotkeyPosition.description")
    @Config.Text(getter = "getHotbarHotkeyPositionText")
    public HotbarHotkeyPosition handledHotbarHotkeyPosition = HotbarHotkeyPosition.TOP_LEFT;

    @Config.Category("ui")
    @Config.Name("speedrunapi.config.mcsreasymode.option.handledHotbarHotkeyTextSize")
    @Config.Description("speedrunapi.config.mcsreasymode.option.handledHotbarHotkeyTextSize.description")
    @Config.Numbers.Whole.Bounds(min = 1, max = 4)
    public int handledHotbarHotkeyTextSize = 2;

    @Config.Category("ui")
    @Config.Name("speedrunapi.config.mcsreasymode.option.handledHotbarHotkeyBackground")
    @Config.Description("speedrunapi.config.mcsreasymode.option.handledHotbarHotkeyBackground.description")
    @Config.Text(getter = "getHotbarHotkeyBackgroundText")
    public HotbarHotkeyBackground handledHotbarHotkeyBackground = HotbarHotkeyBackground.DARK;

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

    public String rngModeDisplayName() {
        this.updateRngModeFromFeatures();
        return this.rngMode.displayName();
    }

    public void setAllRngFeatures(boolean ranked) {
        this.rankedIronGolemDrops = ranked;
        this.rankedEyeBreaks = ranked;
        this.rankedBlazeRods = ranked;
        this.rankedPiglinBarters = ranked;
        this.rankedPiglinString = ranked;
        this.rankedFlint = ranked;
        this.rankedBlindPortal = ranked;
        this.rankedBastionChestLoot = ranked;
        this.rankedChestLootTables = ranked;
        this.rankedHoglinStableRamparts = ranked;
        this.updateRngModeFromFeatures();
    }

    public boolean areAllRngFeaturesRanked() {
        return this.rankedIronGolemDrops
                && this.rankedEyeBreaks
                && this.rankedBlazeRods
                && this.rankedPiglinBarters
                && this.rankedPiglinString
                && this.rankedFlint
                && this.rankedBlindPortal
                && this.rankedBastionChestLoot
                && this.rankedChestLootTables
                && this.rankedHoglinStableRamparts;
    }

    public boolean areAllRngFeaturesVanilla() {
        return !this.rankedIronGolemDrops
                && !this.rankedEyeBreaks
                && !this.rankedBlazeRods
                && !this.rankedPiglinBarters
                && !this.rankedPiglinString
                && !this.rankedFlint
                && !this.rankedBlindPortal
                && !this.rankedBastionChestLoot
                && !this.rankedChestLootTables
                && !this.rankedHoglinStableRamparts;
    }

    public boolean hasAnyRankedRngFeature() {
        return !this.areAllRngFeaturesVanilla();
    }

    public void updateRngModeFromFeatures() {
        if (this.areAllRngFeaturesRanked()) {
            this.rngMode = RngMode.RANKED;
        } else if (this.areAllRngFeaturesVanilla()) {
            this.rngMode = RngMode.VANILLA;
        } else {
            this.rngMode = RngMode.CUSTOM;
        }
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
        if (jsonObject.has("rngMode")) {
            boolean oldRankedMode = "RANKED".equalsIgnoreCase(jsonObject.get("rngMode").getAsString());
            this.migrateRngFeature(jsonObject, "rankedIronGolemDrops", oldRankedMode);
            this.migrateRngFeature(jsonObject, "rankedEyeBreaks", oldRankedMode);
            this.migrateRngFeature(jsonObject, "rankedBlazeRods", oldRankedMode);
            this.migrateRngFeature(jsonObject, "rankedPiglinBarters", oldRankedMode);
            this.migrateRngFeature(jsonObject, "rankedPiglinString", oldRankedMode);
            this.migrateRngFeature(jsonObject, "rankedFlint", oldRankedMode);
            this.migrateRngFeature(jsonObject, "rankedBlindPortal", oldRankedMode);
            this.migrateRngFeature(jsonObject, "rankedBastionChestLoot", oldRankedMode);
            this.migrateRngFeature(jsonObject, "rankedChestLootTables", oldRankedMode);
            this.migrateRngFeature(jsonObject, "rankedHoglinStableRamparts", oldRankedMode);
        }
    }

    private void migrateRngFeature(JsonObject jsonObject, String key, boolean oldRankedMode) {
        if (!jsonObject.has(key)) {
            jsonObject.addProperty(key, oldRankedMode);
        }
    }

    @Override
    public String modID() {
        return Mcsreasymode.MOD_ID;
    }

    public enum RngMode {
        VANILLA("Vanilla"),
        RANKED("Ranked"),
        CUSTOM("Custom");

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
