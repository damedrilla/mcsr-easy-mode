package me.dota1g.mcsreasymode;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public final class BlazeSpawnerServerState {
    private static final Map<RegistryKey<World>, Map<Long, Integer>> DELAYS = new HashMap<>();

    private BlazeSpawnerServerState() {
    }

    public static synchronized void update(RegistryKey<World> world, BlockPos pos, int delay) {
        DELAYS.computeIfAbsent(world, ignored -> new HashMap<>()).put(pos.asLong(), delay);
    }

    public static synchronized Integer get(RegistryKey<World> world, BlockPos pos) {
        Map<Long, Integer> worldDelays = DELAYS.get(world);
        return worldDelays == null ? null : worldDelays.get(pos.asLong());
    }
}
