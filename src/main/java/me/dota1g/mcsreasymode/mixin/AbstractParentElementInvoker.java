package me.dota1g.mcsreasymode.mixin;

import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.gui.Element;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractParentElement.class)
public interface AbstractParentElementInvoker {
    @Invoker("setFocused")
    void mcsreasymode$setFocused(Element element);
}