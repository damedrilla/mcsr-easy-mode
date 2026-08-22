package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import net.minecraft.client.Mouse;
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Mouse.class)
public abstract class MouseMixin {
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
