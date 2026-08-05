package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import me.dota1g.mcsreasymode.client.HotbarHotkeyLabels;
import me.dota1g.mcsreasymode.client.HotbarHotkeyOverlayRenderer;
import me.dota1g.mcsreasymode.client.GodsensBoatStatusRenderer;
import me.dota1g.mcsreasymode.client.BlazeSpawnerOverlayHudRenderer;
import me.dota1g.mcsreasymode.client.MinecartVelocityDebugRenderer;
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
        if (!Mcsreasymode.shouldShowHotbarHotkeys() || Mcsreasymode.CONFIG == null || this.client.options.hudHidden || this.client.currentScreen != null) {
            return;
        }

        HotbarHotkeyOverlayRenderer.render(matrices, this.client, Mcsreasymode.CONFIG, this.client.getWindow().getScaledWidth(), this.client.getWindow().getScaledHeight(), HotbarHotkeyLabels.get(this.client));
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void mcsreasymode$renderMinecartVelocityDebugger(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (!Mcsreasymode.shouldShowMinecartVelocityDebugger() || this.client.options.hudHidden || this.client.currentScreen != null) {
            return;
        }

        MinecartVelocityDebugRenderer.render(matrices, this.client, this.client.getWindow().getScaledWidth(), this.client.getWindow().getScaledHeight());
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void mcsreasymode$renderGodsensBoatStatus(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (!Mcsreasymode.shouldShowGodsensBoatStatus() || this.client.options.hudHidden || this.client.currentScreen != null) {
            return;
        }

        GodsensBoatStatusRenderer.render(matrices, this.client);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void mcsreasymode$renderBlazeSpawnerDetails(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (!Mcsreasymode.shouldShowBlazeSpawnerDetailedText() || this.client.options.hudHidden || this.client.currentScreen != null) {
            return;
        }

        BlazeSpawnerOverlayHudRenderer.render(matrices, this.client, this.client.getWindow().getScaledWidth(), this.client.getWindow().getScaledHeight());
    }
}
