package me.dota1g.mcsreasymode.mixin;

import net.minecraft.structure.RuinedPortalStructurePiece;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RuinedPortalStructurePiece.class)
public interface RuinedPortalStructurePieceAccessor {
    @Accessor("verticalPlacement")
    RuinedPortalStructurePiece.VerticalPlacement mcsreasymode$getVerticalPlacement();

    @Accessor("properties")
    RuinedPortalStructurePiece.Properties mcsreasymode$getProperties();
}
