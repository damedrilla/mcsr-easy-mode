package me.dota1g.mcsreasymode;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

/** The first intended Nether destination stays fixed across entries and world reloads. */
public final class NetherRouteState extends PersistentState {
    public static final String ID = "mcsreasymode_nether_route";
    private BlockPos entry;

    public NetherRouteState() {
        super(ID);
    }

    public BlockPos getEntry() {
        return this.entry;
    }

    public BlockPos capture(int x, int z) {
        if (this.entry == null) {
            this.entry = new BlockPos(x, 64, z);
            this.markDirty();
        }
        return this.entry;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        this.entry = tag.getBoolean("HasEntry")
                ? new BlockPos(tag.getInt("EntryX"), 64, tag.getInt("EntryZ")) : null;
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.putBoolean("HasEntry", this.entry != null);
        if (this.entry != null) {
            tag.putInt("EntryX", this.entry.getX());
            tag.putInt("EntryZ", this.entry.getZ());
        }
        return tag;
    }
}
