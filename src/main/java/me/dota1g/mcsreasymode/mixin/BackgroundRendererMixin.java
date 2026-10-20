package me.dota1g.mcsreasymode.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.dota1g.mcsreasymode.Mcsreasymode;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BackgroundRenderer.class)
public abstract class BackgroundRendererMixin {
    private static final float MCSREASYMODE_FOG_DISTANCE = 1_000_000.0F;

    @Inject(method = "applyFog", at = @At("HEAD"), cancellable = true)
    private static void mcsreasymode$disableFog(Camera camera, BackgroundRenderer.FogType fogType,
                                                float viewDistance, boolean thickFog, CallbackInfo ci) {
        if (!Mcsreasymode.shouldDisableFog()) {
            return;
        }

        // Reset every fog mode so switching fluids or effects cannot leave stale parameters behind.
        RenderSystem.fogDensity(0.0F);
        RenderSystem.fogStart(MCSREASYMODE_FOG_DISTANCE);
        RenderSystem.fogEnd(MCSREASYMODE_FOG_DISTANCE + 1.0F);
        RenderSystem.fogMode(GlStateManager.FogMode.LINEAR);
        RenderSystem.disableFog();
        ci.cancel();
    }
}
