package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.client.PieChartShortcuts;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public abstract class KeyboardPieShortcutMixin {
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    private void mcsreasymode$handlePieShortcut(long window, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
        if (window == this.client.getWindow().getHandle() && PieChartShortcuts.onKey(this.client, key, scanCode, action)) ci.cancel();
    }
}
