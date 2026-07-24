package me.dota1g.mcsreasymode;

public final class SsgModeState {
    public static final int BASTION_CHEST_LIMIT = 3;
    public static final int BASTION_IRON_TOTAL = 32;
    public static final int BASTION_OBSIDIAN_TOTAL = 16;
    public static final int BASTION_GOLD_BLOCK_TOTAL = 3;

    private static int boostedBastionChests;

    private SsgModeState() {
    }

    public static void reset() {
        boostedBastionChests = 0;
    }

    public static int consumeBastionChestBoost() {
        if (!Mcsreasymode.isSsgModeEnabled() || boostedBastionChests >= BASTION_CHEST_LIMIT) {
            return 0;
        }

        boostedBastionChests++;
        return boostedBastionChests;
    }

    public static int getIronMinimum(int boostIndex) {
        return getDistributedCount(BASTION_IRON_TOTAL, boostIndex);
    }

    public static int getObsidianMinimum(int boostIndex) {
        return getDistributedCount(BASTION_OBSIDIAN_TOTAL, boostIndex);
    }

    public static int getGoldBlockMinimum(int boostIndex) {
        return getDistributedCount(BASTION_GOLD_BLOCK_TOTAL, boostIndex);
    }

    public static int getBoostedBastionChests() {
        return boostedBastionChests;
    }

    private static int getDistributedCount(int total, int boostIndex) {
        if (boostIndex < 1 || boostIndex > BASTION_CHEST_LIMIT) {
            return 0;
        }

        int base = total / BASTION_CHEST_LIMIT;
        int remainder = total % BASTION_CHEST_LIMIT;
        return base + (boostIndex <= remainder ? 1 : 0);
    }
}
