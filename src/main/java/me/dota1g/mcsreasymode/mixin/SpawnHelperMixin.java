package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpawnHelper.class)
public class SpawnHelperMixin {

    @Inject(method = "canSpawn", at = @At("HEAD"), cancellable = true)
    private static void mcsreasymode$blockNonBastionSpawnsInBastion(
            ServerWorld world,
            SpawnGroup group,
            StructureAccessor structureAccessor,
            ChunkGenerator chunkGenerator,
            Biome.SpawnEntry spawnEntry,
            BlockPos.Mutable pos,
            double squaredDistance,
            CallbackInfoReturnable<Boolean> cir) {

        if (!Mcsreasymode.isRankedBastionSpawnRestrictionEnabled()) {
            return;
        }

        EntityType<?> entityType = spawnEntry.type;
        if (entityType == EntityType.PIGLIN || entityType == EntityType.HOGLIN) {
            return;
        }

        StructureStart<?> structureStart = structureAccessor.method_28388(pos, false, StructureFeature.BASTION_REMNANT);
        if (structureStart.hasChildren() && structureStart.getBoundingBox().contains(pos)) {
            cir.setReturnValue(false);
        }
    }
}