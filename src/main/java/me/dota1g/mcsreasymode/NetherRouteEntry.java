package me.dota1g.mcsreasymode;

import net.minecraft.util.math.BlockPos;

/** Per-generator anchor, installed before portal search can generate Nether chunks. */
public interface NetherRouteEntry {
    void mcsreasymode$setNetherRouteEntry(BlockPos entry);
}
