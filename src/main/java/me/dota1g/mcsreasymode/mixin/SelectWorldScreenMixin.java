package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.client.AsyncWorldListStatus;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SelectWorldScreen.class)
public abstract class SelectWorldScreenMixin extends Screen {
    @Shadow
    private WorldListWidget levelList;

    protected SelectWorldScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void mcsreasymode$renderAsyncLoading(
            MatrixStack matrices,
            int mouseX,
            int mouseY,
            float delta,
            CallbackInfo ci
    ) {
        if (this.levelList instanceof AsyncWorldListStatus
                && ((AsyncWorldListStatus) this.levelList).mcsreasymode$isLoadingWorlds()) {
            this.drawCenteredText(
                    matrices,
                    this.textRenderer,
                    new TranslatableText("mcsreasymode.world_list.loading"),
                    this.width / 2,
                    this.height / 2,
                    0xFFFFFF
            );
        }
    }
}
