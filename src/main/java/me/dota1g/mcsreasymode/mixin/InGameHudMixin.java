package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import me.dota1g.mcsreasymode.client.HotbarHotkeyLabels;
import me.dota1g.mcsreasymode.client.HotbarHotkeyOverlayRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "render", at = @At("TAIL"))
    private void mcsreasymode$renderHotbarHotkeys(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (!Mcsreasymode.shouldShowHotbarHotkeys() || Mcsreasymode.CONFIG == null || this.client.options.hudHidden) {
            return;
        }

        HotbarHotkeyOverlayRenderer.render(matrices, this.client, Mcsreasymode.CONFIG, this.client.getWindow().getScaledWidth(), this.client.getWindow().getScaledHeight(), HotbarHotkeyLabels.get(this.client));
    }
}
