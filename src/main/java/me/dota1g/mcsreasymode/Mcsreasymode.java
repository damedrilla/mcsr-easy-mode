package me.dota1g.mcsreasymode;

import net.fabricmc.api.ModInitializer;

public class Mcsreasymode implements ModInitializer {
    public static final String MOD_ID = "mcsreasymode";
    public static McsreasymodeConfig CONFIG;

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
}
