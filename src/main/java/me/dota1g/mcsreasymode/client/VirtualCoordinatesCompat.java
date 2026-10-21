package me.dota1g.mcsreasymode.client;

import me.dota1g.mcsreasymode.Mcsreasymode;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import java.lang.reflect.Method;

/** Optional bridge: Artificial ZSG is not required to load this mod. */
final class VirtualCoordinatesCompat {
    private static boolean initialized;
    private static Method appliesTo;
    private static Method virtualX;
    private static Method virtualZ;

    private VirtualCoordinatesCompat() {
    }

    /** Returns null when vanilla coordinates should be displayed. */
    static Vec3d position(double x, double y, double z, RegistryKey<World> dimension) {
        if (!initialized) {
            initialized = true;
            if (FabricLoader.getInstance().isModLoaded("artificialzsg")) {
                try {
                    Class<?> state = Class.forName("me.dota1g.artificialzsg.client.VirtualCoordinateState");
                    virtualX = state.getMethod("x", double.class, RegistryKey.class);
                    virtualZ = state.getMethod("z", double.class, RegistryKey.class);
                    appliesTo = state.getMethod("appliesTo", RegistryKey.class);
                } catch (ReflectiveOperationException | LinkageError e) {
                    disable(e);
                }
            }
        }
        if (appliesTo == null) {
            return null;
        }
        try {
            if (!(boolean) appliesTo.invoke(null, dimension)) {
                return null;
            }
            return new Vec3d((double) virtualX.invoke(null, x, dimension), y,
                    (double) virtualZ.invoke(null, z, dimension));
        } catch (ReflectiveOperationException | LinkageError e) {
            disable(e);
            return null;
        }
    }

    private static void disable(Throwable error) {
        appliesTo = null;
        Mcsreasymode.LOGGER.warn("Artificial ZSG virtual coordinates unavailable; using actual coordinates.", error);
    }
}
