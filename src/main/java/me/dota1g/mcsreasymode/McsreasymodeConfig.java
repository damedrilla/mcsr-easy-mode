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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Config(init = Config.InitPoint.PRELAUNCH)
public class McsreasymodeConfig implements SpeedrunConfig {
    @Config.Category("rng")
    @Config.Name("speedrunapi.config.mcsreasymode.option.rngMode")
    @Config.Description("speedrunapi.config.mcsreasymode.option.rngMode.description")
    @Config.Text(getter = "getRngModeText")

    public RngMode rngMode = RngMode.VANILLA;

    public enum HoglinStableRampartMode {
        DOUBLE_TRIPLE("Double Triple"),
        TRIPLE_TO_GAP("Triple to Gap"),
        EITHER("Either");

        private final String displayName;

        HoglinStableRampartMode(String displayName) {
            this.displayName = displayName;
        }

        public String displayName() {
            return this.displayName;
        }

        public HoglinStableRampartMode next() {
            HoglinStableRampartMode[] values = values();
            return values[(this.ordinal() + 1) % values.length];
        }
    }
    @Config.Hide
    public boolean autoFocusSearchField = true;

    @Config.Hide
    public boolean searchPresetsEnabled = true;

    @Config.Ignored
    public java.util.LinkedHashMap<Integer, String> searchPresets = defaultSearchPresets();

    private static java.util.LinkedHashMap<Integer, String> defaultSearchPresets() {
        java.util.LinkedHashMap<Integer, String> map = new java.util.LinkedHashMap<>();
        map.put(org.lwjgl.glfw.GLFW.GLFW_KEY_B, "boat");
        map.put(org.lwjgl.glfw.GLFW.GLFW_KEY_K, "bucket");
        map.put(org.lwjgl.glfw.GLFW.GLFW_KEY_F, "flint_and_steel");
        map.put(org.lwjgl.glfw.GLFW.GLFW_KEY_P, "ender_pearl");
        return map;
    }

    private static
    final Gson SEARCH_PRESETS_GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path SEARCH_PRESETS_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("mcsreasymode-search-presets.json");

    public void loadSearchPresets() {
        try {
            if (Files.exists(SEARCH_PRESETS_PATH)) {
                String json = new String(Files.readAllBytes(SEARCH_PRESETS_PATH), StandardCharsets.UTF_8);
                Type type = new TypeToken<java.util.LinkedHashMap<Integer, String>>() {}.getType();
                java.util.LinkedHashMap<Integer, String> loaded = SEARCH_PRESETS_GSON.fromJson(json, type);
                if (loaded != null && !loaded.isEmpty()) {
                    this.searchPresets = loaded;
                }
            }
        } catch (IOException ignored) {
        }
    }
    public void saveSearchPresets() {
        try {
            Files.createDirectories(SEARCH_PRESETS_PATH.getParent());
            String json = SEARCH_PRESETS_GSON.toJson(this.searchPresets);
            Files.write(SEARCH_PRESETS_PATH, json.getBytes(StandardCharsets.UTF_8));
        } catch (IOException ignored) {
        }
    }

    @Config.Hide
    public boolean worldSkinEnabled = false;

    @Config.Ignored
    public java.util.List<String> worldSkinPaths = new java.util.ArrayList<>();

    private static final Gson WORLD_SKIN_GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path WORLD_SKIN_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("mcsreasymode-world-skins.json");

    public void loadWorldSkins() {
        try {
            if (Files.exists(WORLD_SKIN_PATH)) {
                String json = new String(Files.readAllBytes(WORLD_SKIN_PATH), StandardCharsets.UTF_8);
                Type type = new TypeToken<java.util.List<String>>() {}.getType();
                java.util.List<String> loaded = WORLD_SKIN_GSON.fromJson(json, type);
                if (loaded != null) {
                    this.worldSkinPaths = loaded;
                }
            }
        } catch (IOException ignored) {
        }
    }

    public void saveWorldSkins() {
        try {
            Files.createDirectories(WORLD_SKIN_PATH.getParent());
            String json = WORLD_SKIN_GSON.toJson(this.worldSkinPaths);
            Files.write(WORLD_SKIN_PATH, json.getBytes(StandardCharsets.UTF_8));
        } catch (IOException ignored) {
        }
    }

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
    public boolean rankedBuriedTreasureChance = false;

    @Config.Hide
    public boolean rankedBastionSpawnProtection = false;

    @Config.Hide
    public boolean rankedHoglinStableRamparts = false;

    @Config.Hide
    public HoglinStableRampartMode hoglinStableRampartMode = HoglinStableRampartMode.EITHER;

    @Config.Hide
    public boolean rankedHoglinBridgeRamparts = false;

    @Config.Hide
    public boolean rankedRuinedPortalAnimals = false;

    @Config.Hide
    public boolean fastQuickLoot = false;

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

    @Config.Category("ui")
    @Config.Name("speedrunapi.config.mcsreasymode.option.filterExplosionClutterDrops")
    @Config.Description("speedrunapi.config.mcsreasymode.option.filterExplosionClutterDrops.description")
    public boolean filterExplosionClutterDrops = false;

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
    @Config.Name("speedrunapi.config.mcsreasymode.option.oceanRavines")
    @Config.Description("speedrunapi.config.mcsreasymode.option.oceanRavines.description")
    public boolean oceanRavines = false;

    @Config.Category("worldgen")
    @Config.Name("speedrunapi.config.mcsreasymode.option.oceanRavineRarity")
    @Config.Description("speedrunapi.config.mcsreasymode.option.oceanRavineRarity.description")
    @Config.Numbers.Fractional.Bounds(min = 0.0F, max = 0.1F)
    public double oceanRavineRarity = 0.04D;

    @Config.Category("worldgen")
    @Config.Name("speedrunapi.config.mcsreasymode.option.oceanRavineWidth")
    @Config.Description("speedrunapi.config.mcsreasymode.option.oceanRavineWidth.description")
    @Config.Numbers.Fractional.Bounds(min = 1.0F, max = 8.0F)
    public double oceanRavineWidth = 5.0D;

    @Config.Category("worldgen")
    @Config.Name("speedrunapi.config.mcsreasymode.option.standardizeVillages")
    @Config.Description("speedrunapi.config.mcsreasymode.option.standardizeVillages.description")
    public boolean standardizeVillages = false;

    @Config.Category("worldgen")
    @Config.Name("speedrunapi.config.mcsreasymode.option.strongholdAntiCorruption")
    @Config.Description("speedrunapi.config.mcsreasymode.option.strongholdAntiCorruption.description")
    public boolean strongholdAntiCorruption = false;

    @Config.Category("worldgen")
    @Config.Name("speedrunapi.config.mcsreasymode.option.underwaterMagmaRavines")
    @Config.Description("speedrunapi.config.mcsreasymode.option.underwaterMagmaRavines.description")
    public boolean underwaterMagmaRavines = false;

    @Config.Category("fun")
    @Config.Name("speedrunapi.config.mcsreasymode.option.funStackedMinecartVelocityGlitch")
    @Config.Description("speedrunapi.config.mcsreasymode.option.funStackedMinecartVelocityGlitch.description")
    public boolean funStackedMinecartVelocityGlitch = false;

    @Config.Category("fun")
    @Config.Name("speedrunapi.config.mcsreasymode.option.showMinecartVelocityDebugger")
    @Config.Description("speedrunapi.config.mcsreasymode.option.showMinecartVelocityDebugger.description")
    public boolean showMinecartVelocityDebugger = false;

    @Config.Category("fun")
    @Config.Name("speedrunapi.config.mcsreasymode.option.ssgMode")
    @Config.Description("speedrunapi.config.mcsreasymode.option.ssgMode.description")
    public boolean ssgMode = false;

    @Config.Category("fun")
    @Config.Name("speedrunapi.config.mcsreasymode.option.funOneShotForRsg")
    @Config.Description("speedrunapi.config.mcsreasymode.option.funOneShotForRsg.description")
    public boolean funOneShotForRsg = false;

    @Config.Category("fun")
    @Config.Name("speedrunapi.config.mcsreasymode.option.funReducedPearlDamage")
    @Config.Description("speedrunapi.config.mcsreasymode.option.funReducedPearlDamage.description")
    public boolean funReducedPearlDamage = false;

    @Config.Category("fun")
    @Config.Name("speedrunapi.config.mcsreasymode.option.forceDragonFirstTargetFront")
    @Config.Description("speedrunapi.config.mcsreasymode.option.forceDragonFirstTargetFront.description")
    public boolean forceDragonFirstTargetFront = false;

    @Config.Category("fun")
    @Config.Name("speedrunapi.config.mcsreasymode.option.forceFrontTowerM85")
    @Config.Description("speedrunapi.config.mcsreasymode.option.forceFrontTowerM85.description")
    public boolean forceFrontTowerM85 = false;

    @Config.Category("fun")
    @Config.Name("speedrunapi.config.mcsreasymode.option.preventBuriedEndSpawn")
    @Config.Description("speedrunapi.config.mcsreasymode.option.preventBuriedEndSpawn.description")
    public boolean preventBuriedEndSpawn = false;

    @Config.Category("fun")
    @Config.Name("speedrunapi.config.mcsreasymode.option.heldPlaceDelayTicks")
    @Config.Description("speedrunapi.config.mcsreasymode.option.heldPlaceDelayTicks.description")
    @Config.Hide
    @Config.Numbers.Whole.Bounds(min = 1, max = 4)
    public int heldPlaceDelayTicks = 4;

    @Config.Category("fun")
    @Config.Hide
    @Config.Numbers.Whole.Bounds(min = 0, max = 20)
    public int heldPlaceRatePerSecond = 0;

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
    @Config.Name("speedrunapi.config.mcsreasymode.option.debugForcePieChart")
    @Config.Description("speedrunapi.config.mcsreasymode.option.debugForcePieChart.description")
    public boolean debugForcePieChart = true;

    @Config.Category("ui")
    @Config.Name("speedrunapi.config.mcsreasymode.option.debugShowEasyModeMarker")
    @Config.Description("speedrunapi.config.mcsreasymode.option.debugShowEasyModeMarker.description")
    public boolean debugShowEasyModeMarker = true;

    @Config.Category("ui")
    @Config.Name("speedrunapi.config.mcsreasymode.option.debugHighlightImportantNumbers")
    @Config.Description("speedrunapi.config.mcsreasymode.option.debugHighlightImportantNumbers.description")
    public boolean debugHighlightImportantNumbers = true;

    @Config.Category("ui")
    @Config.Name("speedrunapi.config.mcsreasymode.option.hideAdvancementToasts")
    @Config.Description("speedrunapi.config.mcsreasymode.option.hideAdvancementToasts.description")
    public boolean hideAdvancementToasts = false;

    @Config.Category("ui")
    @Config.Name("speedrunapi.config.mcsreasymode.option.showBlockPlacementOutline")
    @Config.Description("speedrunapi.config.mcsreasymode.option.showBlockPlacementOutline.description")
    public boolean showBlockPlacementOutline = false;

    @Config.Category("ui")
    @Config.Name("speedrunapi.config.mcsreasymode.option.showBuriedTreasureChunkAssist")
    @Config.Description("speedrunapi.config.mcsreasymode.option.showBuriedTreasureChunkAssist.description")
    public boolean showBuriedTreasureChunkAssist = false;

    @Config.Category("ui")
    @Config.Name("speedrunapi.config.mcsreasymode.option.asyncWorldListLoading")
    @Config.Description("speedrunapi.config.mcsreasymode.option.asyncWorldListLoading.description")
    public boolean asyncWorldListLoading = true;

    @Config.Category("ui")
    @Config.Name("speedrunapi.config.mcsreasymode.option.bucketDesyncGuard")
    @Config.Description("speedrunapi.config.mcsreasymode.option.bucketDesyncGuard.description")
    public boolean bucketDesyncGuard = false;

    @Config.Category("ui")
    @Config.Name("speedrunapi.config.mcsreasymode.option.showGodsensBoatStatus")
    @Config.Description("speedrunapi.config.mcsreasymode.option.showGodsensBoatStatus.description")
    public boolean showGodsensBoatStatus = false;

    @Config.Category("ui")
    @Config.Name("speedrunapi.config.mcsreasymode.option.showBlazeSpawnerOverlay")
    @Config.Description("speedrunapi.config.mcsreasymode.option.showBlazeSpawnerOverlay.description")
    public boolean showBlazeSpawnerOverlay = false;

    @Config.Hide
    public boolean blazeSpawnerOverlayTimer = true;

    @Config.Hide
    public boolean blazeSpawnerOverlayProblemMarkers = true;

    @Config.Hide
    public boolean blazeSpawnerOverlayDetailedText = true;

    @Config.Category("ui")
    @Config.Name("speedrunapi.config.mcsreasymode.option.showPersistentCoordinates")
    @Config.Description("speedrunapi.config.mcsreasymode.option.showPersistentCoordinates.description")
    public boolean showPersistentCoordinates = false;

    @Config.Hide
    public boolean persistentCoordinatesPrecise = true;

    @Config.Hide
    public boolean persistentCoordinatesBlock = false;

    @Config.Hide
    public boolean persistentCoordinatesLocalChunk = false;

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
        this.loadSearchPresets();
        this.loadWorldSkins();
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
        this.rankedBuriedTreasureChance = ranked;
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
                && this.rankedBuriedTreasureChance
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
                && !this.rankedBuriedTreasureChance
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
            this.migrateRngFeature(jsonObject, "rankedBuriedTreasureChance", oldRankedMode);
            this.migrateRngFeature(jsonObject, "rankedHoglinStableRamparts", oldRankedMode);

        }
        if (!jsonObject.has("hoglinStableRampartMode")) {
            jsonObject.addProperty("hoglinStableRampartMode", HoglinStableRampartMode.EITHER.name());
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
