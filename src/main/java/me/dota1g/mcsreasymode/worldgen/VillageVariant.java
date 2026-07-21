package me.dota1g.mcsreasymode.worldgen;

public enum VillageVariant {
    DESERT("desert", "village/desert/houses/desert_weaponsmith_1"),
    PLAINS("plains", "village/plains/houses/plains_weaponsmith_1"),
    SAVANNA("savanna", "village/savanna/houses/savanna_weaponsmith_1"),
    SNOWY("snowy", "village/snowy/houses/snowy_weapon_smith_1"),
    TAIGA("taiga", "village/taiga/houses/taiga_weaponsmith_1");

    public final String logName;
    public final String template;

    VillageVariant(String logName, String template) {
        this.logName = logName;
        this.template = template;
    }

    public static VillageVariant fromPath(String path) {
        if (path.contains("desert")) {
            return DESERT;
        }
        if (path.contains("savanna")) {
            return SAVANNA;
        }
        if (path.contains("snowy")) {
            return SNOWY;
        }
        if (path.contains("taiga")) {
            return TAIGA;
        }
        return PLAINS;
    }
}
