package me.dota1g.mcsreasymode.mixin;

import net.minecraft.entity.boss.dragon.phase.HoldingPatternPhase;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HoldingPatternPhase.class)
public interface HoldingPatternPhaseAccessor {
    @Accessor("target")
    void mcsreasymode$setTarget(Vec3d target);
}