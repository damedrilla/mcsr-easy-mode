package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import net.minecraft.client.toast.AdvancementToast;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ToastManager.class)
public abstract class ToastManagerMixin {
    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    private void mcsreasymode$hideAdvancementToasts(Toast toast, CallbackInfo ci) {
        if (Mcsreasymode.shouldHideAdvancementToasts() && toast instanceof AdvancementToast) {
            ci.cancel();
        }
    }
}
