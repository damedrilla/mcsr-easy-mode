package me.dota1g.mcsreasymode;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class RankedRngState {
    private static final int PEARL_PITY_BARTERS = 24;
    private static final int OBSIDIAN_PITY_BARTERS = 24;
    private static final int BASTION_IRON_MINIMUM = 3;
    private static final int BASTION_OBSIDIAN_MINIMUM = 5;
    private static final int FLINT_PITY_BREAKS = 10;

    private static int pearlDryBarters;
    private static int obsidianDryBarters;
    private static int flintDryBreaks;
    private static boolean bastionChestAdjusted;
    private static boolean blindPortalSurfacePending;
    private static boolean blindPortalSurfaceUsed;

    private RankedRngState() {
    }

    public static List<ItemStack> applyPiglinBarterPity(List<ItemStack> original, Random random) {
        List<ItemStack> result = new ArrayList<>(original);

        boolean hasPearls = result.stream().anyMatch(stack -> stack.getItem() == Items.ENDER_PEARL);
        boolean hasObsidian = result.stream().anyMatch(stack -> stack.getItem() == Items.OBSIDIAN);

        if (hasPearls) {
            pearlDryBarters = 0;
        } else {
            pearlDryBarters++;
        }

        if (hasObsidian) {
            obsidianDryBarters = 0;
        } else {
            obsidianDryBarters++;
        }

        if (!hasPearls && pearlDryBarters >= PEARL_PITY_BARTERS) {
            int pearlCount = 4 + random.nextInt(5);
            result.add(new ItemStack(Items.ENDER_PEARL, pearlCount));
            Mcsreasymode.debug("Piglin barter pearl pity proc: added " + pearlCount + " ender pearls after " + PEARL_PITY_BARTERS + " dry barters.");
            pearlDryBarters = 0;
            return result;
        }

        if (!hasObsidian && obsidianDryBarters >= OBSIDIAN_PITY_BARTERS) {
            result.add(new ItemStack(Items.OBSIDIAN));
            Mcsreasymode.debug("Piglin barter obsidian pity proc: added 1 obsidian after " + OBSIDIAN_PITY_BARTERS + " dry barters.");
            obsidianDryBarters = 0;
        }

        return result;
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

    public static void armBlindPortalSurface() {
        if (!blindPortalSurfaceUsed) {
            blindPortalSurfacePending = true;
            Mcsreasymode.debug("Blind portal surfacing armed for next Nether-to-Overworld portal creation.");
        }
    }

    public static void resetBlindPortalSurface() {
        blindPortalSurfacePending = false;
        blindPortalSurfaceUsed = false;
        Mcsreasymode.debug("Blind portal surfacing state reset after entering the Nether.");
    }

    public static boolean shouldSurfaceBlindPortal() {
        return blindPortalSurfacePending && !blindPortalSurfaceUsed;
    }

    public static void consumeBlindPortalSurface() {
        blindPortalSurfacePending = false;
        blindPortalSurfaceUsed = true;
        Mcsreasymode.debug("Blind portal surfacing consumed.");
    }

    public static void clearPendingBlindPortalSurface() {
        blindPortalSurfacePending = false;
    }
}
