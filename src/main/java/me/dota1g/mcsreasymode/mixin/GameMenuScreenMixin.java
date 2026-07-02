package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {
    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "initWidgets", at = @At("TAIL"))
    private void mcsreasymode$moveSaveAndQuitButton(CallbackInfo ci) {
        if (!Mcsreasymode.shouldMoveSaveAndQuitButton()) {
            return;
        }

        for (AbstractButtonWidget button : this.buttons) {
            if (this.mcsreasymode$isSaveAndQuitButton(button)) {
                button.x = 8;
                button.y = this.height - button.getHeight() - 8;
                button.setWidth(120);
                Mcsreasymode.debugRateLimited("ui.move_save_quit", "Pause menu Save and Quit button moved to bottom-left.", 5000L);
                return;
            }
        }
    }

    private boolean mcsreasymode$isSaveAndQuitButton(AbstractButtonWidget button) {
        Text message = button.getMessage();
        if (message instanceof TranslatableText) {
            String key = ((TranslatableText) message).getKey();
            return "menu.returnToMenu".equals(key) || "menu.disconnect".equals(key);
        }
        return false;
    }
}
