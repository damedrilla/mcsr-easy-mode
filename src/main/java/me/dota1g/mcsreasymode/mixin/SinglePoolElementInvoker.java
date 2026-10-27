package me.dota1g.mcsreasymode.mixin;

import net.minecraft.structure.Structure;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.pool.SinglePoolElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SinglePoolElement.class)
public interface SinglePoolElementInvoker {
    @Invoker("method_27233")
    Structure mcsreasymode$getStructure(StructureManager manager);
}
