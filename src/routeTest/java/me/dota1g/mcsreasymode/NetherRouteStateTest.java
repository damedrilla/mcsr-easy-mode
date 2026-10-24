package me.dota1g.mcsreasymode;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.math.BlockPos;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public final class NetherRouteStateTest {
    public static void main(String[] args) throws Exception {
        NetherRouteState first = new NetherRouteState();
        require(first.getEntry() == null && !first.isDirty(), "New world already anchored");
        require(first.capture(-401, 987).equals(new BlockPos(-401, 64, 987)), "Entry X/Z changed");
        require(first.isDirty(), "First entry was not marked for saving");
        first.setDirty(false);
        require(first.capture(1234, -5678).equals(new BlockPos(-401, 64, 987)), "Later entry moved anchor");
        require(!first.isDirty(), "Later entry dirtied unchanged state");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        NbtIo.writeCompressed(first.toTag(new CompoundTag()), bytes);
        NetherRouteState reloaded = new NetherRouteState();
        reloaded.fromTag(NbtIo.readCompressed(new ByteArrayInputStream(bytes.toByteArray())));
        require(reloaded.capture(0, 0).equals(first.getEntry()), "Reload lost fixed entry");
        NetherRouteState origin = new NetherRouteState();
        origin.capture(0, 0);
        reloaded.fromTag(origin.toTag(new CompoundTag()));
        require(reloaded.getEntry().equals(new BlockPos(0, 64, 0)), "Origin confused with missing entry");
        reloaded.fromTag(new CompoundTag());
        require(reloaded.getEntry() == null, "Missing state fell back to origin");
        require(first.getEntry().equals(new BlockPos(-401, 64, 987)), "Worlds shared anchor state");
        System.out.println("Nether route entry state checks passed (first entry, reload, negative coordinates, world isolation).");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
