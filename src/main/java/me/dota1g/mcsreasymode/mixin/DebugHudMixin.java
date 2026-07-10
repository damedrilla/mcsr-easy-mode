package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(DebugHud.class)
public abstract class DebugHudMixin {
    @Inject(method = "getRightText", at = @At("RETURN"), cancellable = true)
    private void mcsreasymode$addEasyModeMarker(CallbackInfoReturnable<List<String>> cir) {
        List<String> lines = new ArrayList<>(cir.getReturnValue());
        String rngMode = Mcsreasymode.rngModeDisplayName() + " RNG";
        lines.add(Math.min(1, lines.size()), "MCSR Easy Mode: " + rngMode);
        cir.setReturnValue(lines);
    }
}
