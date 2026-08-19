package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.util.Formatting;
import java.util.ArrayList;
import java.util.List;

@Mixin(DebugHud.class)
public abstract class DebugHudMixin {
    //no shift pie chart because why not
    //march 7th > evernight
    private static final Pattern MCSREASYMODE$FACING_ANGLES =
            Pattern.compile("\\(([-\\d.]+) / ([-\\d.]+)\\)$");
    @Inject(method = "render", at = @At("HEAD"))
    private void mcsreasymode$forcePieChart(net.minecraft.client.util.math.MatrixStack matrixStack, CallbackInfo ci) {
        // Respect config toggle
        if (Mcsreasymode.CONFIG != null && Mcsreasymode.CONFIG.debugForcePieChart) {
            MinecraftClient.getInstance().options.debugProfilerEnabled = true;
        }
    }

    @Inject(method = "getRightText", at = @At("RETURN"), cancellable = true)
    private void mcsreasymode$addEasyModeMarker(CallbackInfoReturnable<List<String>> cir) {
        List<String> lines = new ArrayList<>(cir.getReturnValue());
        String rngMode = Mcsreasymode.rngModeDisplayName() + " RNG";
        lines.add(Math.min(1, lines.size()), "MCSR Easy Mode: " + rngMode);
        cir.setReturnValue(lines);
    }

    @Inject(method = "getLeftText", at = @At("RETURN"), cancellable = true)
    private void mcsreasymode$highlightImportantNumbers(CallbackInfoReturnable<List<String>> cir) {
        if (Mcsreasymode.CONFIG == null || !Mcsreasymode.CONFIG.debugHighlightImportantNumbers) {
            return;
        }

        List<String> lines = new ArrayList<>(cir.getReturnValue());

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            if (line.startsWith("Facing: ")) {
                Matcher matcher = MCSREASYMODE$FACING_ANGLES.matcher(line);
                if (matcher.find()) {
                    double yaw = Double.parseDouble(matcher.group(1));
                    double pitch = Double.parseDouble(matcher.group(2));

                    String yawColored = (yaw < 0 ? Formatting.RED : Formatting.GREEN) + matcher.group(1) + Formatting.RESET;
                    String pitchColored = (pitch < 0 ? Formatting.RED : Formatting.GREEN) + matcher.group(2) + Formatting.RESET;

                    lines.set(i, line.substring(0, matcher.start()) + "(" + yawColored + " / " + pitchColored + ")");
                }
                continue;
            }

            String replaced = line.replaceAll("\\bO: (\\d+|\\?\\?)", Formatting.RED + "O: $1" + Formatting.RESET);
            if (!replaced.equals(line)) {
                lines.set(i, replaced);
            }
        }

        cir.setReturnValue(lines);
    }
}