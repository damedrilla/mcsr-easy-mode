package me.contaria.speedrunapi.mixin.gui;

import me.contaria.speedrunapi.config.screen.SpeedrunModConfigsScreen;
import me.contaria.speedrunapi.config.screen.widgets.IconButtonWidget;
import me.contaria.speedrunapi.util.IdentifierUtil;
import me.contaria.speedrunapi.util.TextUtil;
import net.minecraft.class_2561;
import net.minecraft.class_429;
import net.minecraft.class_437;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_429.class)
public abstract class OptionsScreenMixin extends class_437 {

    protected OptionsScreenMixin(class_2561 title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void addSpeedrunConfigButton(CallbackInfo ci) {
        this.method_25411(new IconButtonWidget(IdentifierUtil.ofVanilla("textures/item/writable_book.png"), this.field_22789 / 2 + 160, this.field_22790 / 6 - 12, TextUtil.translatable("speedrunapi.gui.config.button"), button -> {
            assert this.field_22787 != null;
            this.field_22787.method_1507(new SpeedrunModConfigsScreen(this));
        }));
    }
}
