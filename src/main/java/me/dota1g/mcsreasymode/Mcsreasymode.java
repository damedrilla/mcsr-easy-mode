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
        return CONFIG != null && CONFIG.rngMode == McsreasymodeConfig.RngMode.RANKED;
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
