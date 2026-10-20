package me.dota1g.mcsreasymode.mixin;

import net.minecraft.world.gen.feature.BastionRemnantFeatureConfig;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(BastionRemnantFeatureConfig.class)
public interface BastionRemnantFeatureConfigAccessor {
    @Accessor("possibleConfigs")
    List<StructurePoolFeatureConfig> mcsreasymode$getPossibleConfigs();
}
