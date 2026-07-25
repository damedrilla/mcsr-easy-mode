package me.dota1g.mcsreasymode;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class RankedRngState {
    private static final int PEARL_PITY_BARTERS = 24;
    private static final int OBSIDIAN_PITY_BARTERS = 24;
    private static final int STRING_PITY_BARTERS = 24;
    private static final int BASTION_IRON_MINIMUM = 3;
    private static final int BASTION_OBSIDIAN_MINIMUM = 5;
    private static final int FLINT_PITY_BREAKS = 10;
    private static final int ONE_SHOT_IRON_MINIMUM = 26;
    private static final int ONE_SHOT_IRON_MAXIMUM = 36;

    private static int pearlDryBarters;
    private static int obsidianDryBarters;
    private static int stringDryBarters;
    private static int oneShotIronBudget;
    private static int oneShotIronGiven;
    private static int flintDryBreaks;
    private static int thrownEyeCount;
    private static boolean bastionChestAdjusted;
    private static boolean blindPortalSurfacePending;
    private static boolean blindPortalSurfaceUsed;
    private static Integer blindPortalForcedSurfaceY;

    private RankedRngState() {
    }

    public static void reset() {
        pearlDryBarters = 0;
        obsidianDryBarters = 0;
        stringDryBarters = 0;
        oneShotIronBudget = 0;
        oneShotIronGiven = 0;
        flintDryBreaks = 0;
        thrownEyeCount = 0;
        bastionChestAdjusted = false;
        blindPortalSurfacePending = false;
        blindPortalSurfaceUsed = false;
        blindPortalForcedSurfaceY = null;
        Mcsreasymode.debug("Ranked RNG state reset for new world.");
    }

    public static List<ItemStack> applyPiglinBarterPity(List<ItemStack> original, Random random, boolean rankedBarterPity, boolean rankedStringPity, boolean oneShotForRsg) {
        List<ItemStack> result = new ArrayList<>(original);

        boolean hasPearls = result.stream().anyMatch(stack -> stack.getItem() == Items.ENDER_PEARL);
        boolean hasObsidian = result.stream().anyMatch(stack -> stack.getItem() == Items.OBSIDIAN);
        boolean hasString = result.stream().anyMatch(stack -> stack.getItem() == Items.STRING);
        if (rankedStringPity && oneShotForRsg && hasString) {
            int ironCount = 0;
            int stringLeftover = 0;
            for (int i = 0; i < result.size(); i++) {
                ItemStack stack = result.get(i);
                if (stack.getItem() == Items.STRING) {
                    int convertedCount = consumeOneShotIron(stack.getCount(), random);
                    ironCount += convertedCount;
                    if (convertedCount > 0) {
                        result.set(i, new ItemStack(Items.IRON_INGOT, convertedCount));
                        stringLeftover += stack.getCount() - convertedCount;
                    }
                }
            }
            if (stringLeftover > 0) {
                result.add(new ItemStack(Items.STRING, stringLeftover));
            }
            if (ironCount > 0) {
                Mcsreasymode.debug("Piglin barter one shot for RSG: converted natural string trade into " + ironCount
                        + " iron ingots (" + oneShotIronGiven + "/" + oneShotIronBudget + " budget used).");
            }
        }

        if (rankedBarterPity && hasPearls) {
            pearlDryBarters = 0;
        } else if (rankedBarterPity) {
            pearlDryBarters++;
        }

        if (rankedBarterPity && hasObsidian) {
            obsidianDryBarters = 0;
        } else if (rankedBarterPity) {
            obsidianDryBarters++;
        }

        if (rankedStringPity && hasString) {
            stringDryBarters = 0;
        } else if (rankedStringPity) {
            stringDryBarters++;
        }

        if (rankedBarterPity && !hasPearls && pearlDryBarters >= PEARL_PITY_BARTERS) {
            int pearlCount = 4 + random.nextInt(5);
            result.add(new ItemStack(Items.ENDER_PEARL, pearlCount));
            Mcsreasymode.debug("Piglin barter pearl pity proc: added " + pearlCount + " ender pearls after " + PEARL_PITY_BARTERS + " dry barters.");
            pearlDryBarters = 0;
            return result;
        }

        if (rankedBarterPity && !hasObsidian && obsidianDryBarters >= OBSIDIAN_PITY_BARTERS) {
            result.add(new ItemStack(Items.OBSIDIAN));
            Mcsreasymode.debug("Piglin barter obsidian pity proc: added 1 obsidian after " + OBSIDIAN_PITY_BARTERS + " dry barters.");
            obsidianDryBarters = 0;
        }
        
        if (rankedStringPity && !hasString && stringDryBarters >= STRING_PITY_BARTERS) {
            int count = 8 + random.nextInt(15);
            if (oneShotForRsg) {
                int ironCount = consumeOneShotIron(count, random);
                if (ironCount > 0) {
                    result.add(new ItemStack(Items.IRON_INGOT, ironCount));
                    Mcsreasymode.debug("Piglin barter one shot for RSG proc: added " + ironCount
                            + " iron ingots instead of string after " + STRING_PITY_BARTERS + " dry barters ("
                            + oneShotIronGiven + "/" + oneShotIronBudget + " budget used).");
                }
            } else {
                result.add(new ItemStack(Items.STRING, count));
                Mcsreasymode.debug("Piglin barter string pity proc: added " + count + " strings after " + STRING_PITY_BARTERS + " dry barters.");
            }
            stringDryBarters = 0;
        }
        return result;
    }

    private static int consumeOneShotIron(int requested, Random random) {
        if (requested <= 0) {
            return 0;
        }
        if (oneShotIronBudget <= 0) {
            oneShotIronBudget = ONE_SHOT_IRON_MINIMUM + random.nextInt(ONE_SHOT_IRON_MAXIMUM - ONE_SHOT_IRON_MINIMUM + 1);
        }

        int remaining = oneShotIronBudget - oneShotIronGiven;
        if (remaining <= 0) {
            return 0;
        }

        int granted = Math.min(requested, remaining);
        oneShotIronGiven += granted;
        return granted;
    }

    public static boolean shouldAdjustBastionChest() {
        if (bastionChestAdjusted) {
            return false;
        }
        bastionChestAdjusted = true;
        return true;
    }

    public static int getBastionIronMinimum() {
        return BASTION_IRON_MINIMUM;
    }

    public static int getBastionObsidianMinimum() {
        return BASTION_OBSIDIAN_MINIMUM;
    }

    public static List<ItemStack> applyFlintPity(List<ItemStack> original) {
        List<ItemStack> result = new ArrayList<>(original);
        boolean hasFlint = result.stream().anyMatch(stack -> stack.getItem() == Items.FLINT);

        if (hasFlint) {
            flintDryBreaks = 0;
            return result;
        }

        flintDryBreaks++;
        if (flintDryBreaks >= FLINT_PITY_BREAKS) {
            result.clear();
            result.add(new ItemStack(Items.FLINT));
            Mcsreasymode.debug("Flint pity proc: forced 1 flint after " + FLINT_PITY_BREAKS + " gravel drops without flint.");
            flintDryBreaks = 0;
        }

        return result;
    }

    public static boolean shouldProtectThrownEye() {
        thrownEyeCount++;
        boolean protect = thrownEyeCount == 2;
        if (protect) {
            Mcsreasymode.debug("Eye of ender standardized: protected throw #2 from breaking.");
        } else {
            Mcsreasymode.debug("Eye of ender throw #" + thrownEyeCount + " uses vanilla break rate.");
        }
        return protect;
    }

    public static void armBlindPortalSurface() {
        if (!blindPortalSurfaceUsed) {
            blindPortalSurfacePending = true;
            Mcsreasymode.debug("Blind portal surfacing armed for next Nether-to-Overworld portal creation.");
        }
    }

    public static boolean shouldSurfaceBlindPortal() {
        return blindPortalSurfacePending && !blindPortalSurfaceUsed;
    }

    public static boolean canUseBlindPortalSurface() {
        return !blindPortalSurfaceUsed;
    }

    public static void consumeBlindPortalSurface() {
        blindPortalSurfacePending = false;
        blindPortalSurfaceUsed = true;
        Mcsreasymode.debug("Blind portal surfacing consumed.");
    }

    public static void clearPendingBlindPortalSurface() {
        blindPortalSurfacePending = false;
        blindPortalForcedSurfaceY = null;
    }

    public static void setBlindPortalForcedSurfaceY(int surfaceY) {
        blindPortalForcedSurfaceY = surfaceY;
    }

    public static Integer getBlindPortalForcedSurfaceY() {
        return blindPortalForcedSurfaceY;
    }

    public static Integer consumeBlindPortalForcedSurfaceY() {
        Integer surfaceY = blindPortalForcedSurfaceY;
        blindPortalForcedSurfaceY = null;
        return surfaceY;
    }
}
