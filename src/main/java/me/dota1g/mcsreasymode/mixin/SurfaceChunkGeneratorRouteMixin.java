package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import me.dota1g.mcsreasymode.McsreasymodeConfig;
import me.dota1g.mcsreasymode.NetherRoutePlanner;
import me.dota1g.mcsreasymode.NetherRouteEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorType;
import net.minecraft.world.gen.chunk.SurfaceChunkGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SurfaceChunkGenerator.class)
public abstract class SurfaceChunkGeneratorRouteMixin implements NetherRouteEntry {
    @Unique private volatile BlockPos mcsreasymode$routeEntry;
    @Shadow @Final private long field_24778;
    @Shadow @Final protected ChunkGeneratorType field_24774;
    @Shadow @Final private int horizontalNoiseResolution;
    @Shadow @Final private int verticalNoiseResolution;
    @Unique private volatile NetherRoutePlanner mcsreasymode$routePlan;
    @Unique private final ThreadLocal<Boolean> mcsreasymode$planningRoute = new ThreadLocal<>();
    @Unique private volatile boolean mcsreasymode$routeStructuresEnabled = true;

    @Inject(method = "populateNoise", at = @At("HEAD"))
    private void mcsreasymode$routeStructureSetting(WorldAccess world, StructureAccessor structures, Chunk chunk, CallbackInfo ci) {
        this.mcsreasymode$routeStructuresEnabled = structures.shouldGenerateStructures();
    }

    @Inject(method = "sampleNoiseColumn([DII)V", at = @At("RETURN"))
    private void mcsreasymode$openLocalRoutes(double[] buffer, int noiseX, int noiseZ, CallbackInfo ci) {
        McsreasymodeConfig config = Mcsreasymode.CONFIG;
        if (config == null || (!config.openNetherEntryToBastion && !config.openNetherBastionToFortress)
                || this.mcsreasymode$routeEntry == null
                || !this.mcsreasymode$routeStructuresEnabled
                || !this.field_24774.method_28555(ChunkGeneratorType.Preset.NETHER)
                || Boolean.TRUE.equals(this.mcsreasymode$planningRoute.get())) return;
        NetherRoutePlanner plan = this.mcsreasymode$getRoutePlan();
        int x = noiseX * this.horizontalNoiseResolution, z = noiseZ * this.horizontalNoiseResolution;
        boolean entry = config.openNetherEntryToBastion && plan.entryToBastion != null && plan.entryToBastion.containsColumn(x, z);
        boolean fortress = config.openNetherBastionToFortress && plan.bastionToFortress != null && plan.bastionToFortress.containsColumn(x, z);
        if (!entry && !fortress) return;
        for (int i = 0; i < buffer.length; i++) {
            int y = i * this.verticalNoiseResolution;
            if (entry) buffer[i] = plan.entryToBastion.openDensity(buffer[i], x, y, z);
            if (fortress) buffer[i] = plan.bastionToFortress.openDensity(buffer[i], x, y, z);
        }
    }

    @Override
    public synchronized void mcsreasymode$setNetherRouteEntry(BlockPos entry) {
        // Never move a route after any of its chunks may have been generated.
        if (this.mcsreasymode$routeEntry == null) {
            this.mcsreasymode$routeEntry = new BlockPos(entry.getX(), 64, entry.getZ());
        }
    }

    @Unique
    private NetherRoutePlanner mcsreasymode$getRoutePlan() {
        NetherRoutePlanner plan = this.mcsreasymode$routePlan;
        if (plan == null) {
            synchronized (this) {
                plan = this.mcsreasymode$routePlan;
                if (plan == null) {
                    // Terrain scoring must sample original noise, without recursively planning.
                    this.mcsreasymode$planningRoute.set(true);
                    try {
                        plan = new NetherRoutePlanner((ChunkGenerator) (Object) this, this.field_24778, this.mcsreasymode$routeEntry);
                        this.mcsreasymode$routePlan = plan;
                    } finally {
                        this.mcsreasymode$planningRoute.remove();
                    }
                }
            }
        }
        return plan;
    }
}
