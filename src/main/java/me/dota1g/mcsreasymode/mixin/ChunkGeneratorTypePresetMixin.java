package me.dota1g.mcsreasymode.mixin;

import com.google.common.collect.Maps;
import me.dota1g.mcsreasymode.Mcsreasymode;
import net.minecraft.block.BlockState;
import net.minecraft.world.gen.chunk.ChunkGeneratorType;
import net.minecraft.world.gen.chunk.NoiseConfig;
import net.minecraft.world.gen.chunk.NoiseSamplingConfig;
import net.minecraft.world.gen.chunk.SlideConfig;
import net.minecraft.world.gen.chunk.StructureConfig;
import net.minecraft.world.gen.chunk.StructuresConfig;
import net.minecraft.world.gen.feature.StructureFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Mixin(ChunkGeneratorType.Preset.class)
public abstract class ChunkGeneratorTypePresetMixin {
    @Unique
    private Function<ChunkGeneratorType.Preset, ChunkGeneratorType> mcsreasymode$generatorTypeGetter;

    @Inject(method = "createCavesType", at = @At("HEAD"), cancellable = true)
    private static void mcsreasymode$createTunedNetherType(StructuresConfig config, BlockState defaultBlock, BlockState defaultFluid, ChunkGeneratorType.Preset preset, CallbackInfoReturnable<ChunkGeneratorType> cir) {
        if (!Mcsreasymode.isOpenNetherTerrainEnabled()) {
            return;
        }

        Map<StructureFeature<?>, StructureConfig> structures = Maps.newHashMap(StructuresConfig.DEFAULT_STRUCTURES);
        structures.put(StructureFeature.RUINED_PORTAL, new StructureConfig(25, 10, 34222645));

        cir.setReturnValue(new ChunkGeneratorType(
                new StructuresConfig(Optional.ofNullable(config.getStronghold()), structures),
                new NoiseConfig(
                        128,
                        new NoiseSamplingConfig(Mcsreasymode.netherTerrainXzScale(), 3.0D, 80.0D, 60.0D),
                        new SlideConfig(120, 3, 0),
                        new SlideConfig(320, 4, -1),
                        1,
                        2,
                        Mcsreasymode.netherTerrainDensityFactor(),
                        Mcsreasymode.netherTerrainDensityOffset(),
                        false,
                        false,
                        false,
                        false
                ),
                defaultBlock,
                defaultFluid,
                0,
                0,
                32,
                false,
                Optional.of(preset)
        ));
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void mcsreasymode$captureGeneratorTypeGetter(String id, Function<ChunkGeneratorType.Preset, ChunkGeneratorType> generatorTypeGetter, CallbackInfo ci) {
        this.mcsreasymode$generatorTypeGetter = generatorTypeGetter;
    }

    @Inject(method = "getChunkGeneratorType", at = @At("RETURN"), cancellable = true)
    private void mcsreasymode$alwaysCreateTunedType(CallbackInfoReturnable<ChunkGeneratorType> cir) {
        if (this.mcsreasymode$generatorTypeGetter != null && Mcsreasymode.isOpenNetherTerrainEnabled()) {
            cir.setReturnValue(this.mcsreasymode$generatorTypeGetter.apply((ChunkGeneratorType.Preset) (Object) this));
        }
    }
}
