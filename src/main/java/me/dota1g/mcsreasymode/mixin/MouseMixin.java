package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import net.minecraft.client.Mouse;
import net.minecraft.client.MinecraftClient;
import me.dota1g.mcsreasymode.client.PieChartShortcuts;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Mouse.class)
public abstract class MouseMixin {
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = true)
    private void mcsreasymode$handlePieShortcut(long window, int button, int action, int modifiers, CallbackInfo ci) {
        if (window == this.client.getWindow().getHandle() && PieChartShortcuts.onMouse(this.client, button, action)) ci.cancel();
    }

    @Redirect(
            method = "onMouseScroll",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerInventory;scrollInHotbar(D)V"
            )
    )
    private void mcsreasymode$disableHotbarScrolling(PlayerInventory inventory, double scrollAmount) {
        if (!Mcsreasymode.isStrictHotbarHotkeyModeEnabled()) {
            inventory.scrollInHotbar(scrollAmount);
        }
    }
}
