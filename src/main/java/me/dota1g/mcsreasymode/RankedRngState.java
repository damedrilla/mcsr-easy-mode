package me.dota1g.mcsreasymode;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class RankedRngState {
    private static final int PEARL_PITY_BARTERS = 12;
    private static final int OBSIDIAN_PITY_BARTERS = 16;

    private static int pearlDryBarters;
    private static int obsidianDryBarters;

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
            result.add(new ItemStack(Items.ENDER_PEARL, 4 + random.nextInt(5)));
            pearlDryBarters = 0;
            return result;
        }

        if (!hasObsidian && obsidianDryBarters >= OBSIDIAN_PITY_BARTERS) {
            result.add(new ItemStack(Items.OBSIDIAN));
            obsidianDryBarters = 0;
        }

        return result;
    }
}
