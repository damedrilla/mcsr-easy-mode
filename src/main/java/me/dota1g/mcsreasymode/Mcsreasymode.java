package me.dota1g.mcsreasymode;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class Mcsreasymode implements ModInitializer {
    public static final String MOD_ID = "mcsreasymode";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static McsreasymodeConfig CONFIG;
    private static final Map<String, Long> LAST_DEBUG_LOG = new HashMap<>();

    @Override
    public void onInitialize() {
    }

    public static boolean isRankedRngEnabled() {
        return CONFIG != null && CONFIG.hasAnyRankedRngFeature();
    }

    public static String rngModeDisplayName() {
        return CONFIG == null ? "Vanilla" : CONFIG.rngModeDisplayName();
    }

    public static boolean isRankedIronGolemDropsEnabled() {
        return CONFIG != null && CONFIG.rankedIronGolemDrops;
    }

    public static boolean isRankedEyeBreaksEnabled() {
        return CONFIG != null && CONFIG.rankedEyeBreaks;
    }

    public static boolean isRankedBlazeRodsEnabled() {
        return CONFIG != null && CONFIG.rankedBlazeRods;
    }

    public static boolean isRankedPiglinBartersEnabled() {
        return CONFIG != null && CONFIG.rankedPiglinBarters;
    }

    public static boolean isRankedPiglinStringEnabled() {
        return CONFIG != null && CONFIG.rankedPiglinString;
    }

    public static boolean isRankedFlintEnabled() {
        return CONFIG != null && CONFIG.rankedFlint;
    }

    public static boolean isRankedBlindPortalEnabled() {
        return CONFIG != null && CONFIG.rankedBlindPortal;
    }

    public static boolean isRankedBastionChestLootEnabled() {
        return CONFIG != null && CONFIG.rankedBastionChestLoot;
    }

    public static boolean isRankedChestLootTablesEnabled() {
        return CONFIG != null && CONFIG.rankedChestLootTables;
    }

    public static boolean isRankedHoglinStableRampartsEnabled() {
        return CONFIG != null && CONFIG.rankedHoglinStableRamparts;
    }

    public static boolean isPiglinAggressionDisabled() {
        return CONFIG == null || CONFIG.disablePiglinAggression;
    }

    public static boolean isGhastAggressionDisabled() {
        return CONFIG == null || CONFIG.disableGhastAggression;
    }

    public static boolean isHoglinAggressionDisabled() {
        return CONFIG == null || CONFIG.disableHoglinAggression;
    }

    public static boolean shouldMoveSaveAndQuitButton() {
        return CONFIG == null || CONFIG.moveSaveAndQuitButton;
    }

    public static boolean shouldShowHotbarHotkeys() {
        return CONFIG != null && CONFIG.showHotbarHotkeys;
    }

    public static boolean isOpenNetherTerrainEnabled() {
        return CONFIG != null && CONFIG.openNetherTerrain;
    }

    public static double netherTerrainXzScale() {
        return CONFIG == null ? 1.0D : Math.max(0.52D, Math.min(1.0D, CONFIG.netherTerrainXzScale));
    }

    public static double netherTerrainDensityFactor() {
        return CONFIG == null ? 0.0D : Math.max(0.0D, Math.min(0.08215D, CONFIG.netherTerrainDensityFactor));
    }

    public static double netherTerrainDensityOffset() {
        return CONFIG == null ? 0.019921875D : Math.max(0.01174D, Math.min(0.019921875D, CONFIG.netherTerrainDensityOffset));
    }

    public static void debug(String message) {
        LOGGER.info("[MCSR Easy Mode] {}", message);
    }

    public static void debugRateLimited(String key, String message, long intervalMillis) {
        long now = System.currentTimeMillis();
        Long last = LAST_DEBUG_LOG.get(key);
        if (last == null || now - last >= intervalMillis) {
            LAST_DEBUG_LOG.put(key, now);
            debug(message);
        }
    }
}
