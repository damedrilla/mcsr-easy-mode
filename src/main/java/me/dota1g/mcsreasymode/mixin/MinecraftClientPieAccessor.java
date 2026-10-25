package me.dota1g.mcsreasymode.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftClient.class)
public interface MinecraftClientPieAccessor {
    @Accessor("openProfilerSection")
    String mcsreasymode$getPiePath();
    @Accessor("openProfilerSection")
    void mcsreasymode$setPiePath(String path);
}
