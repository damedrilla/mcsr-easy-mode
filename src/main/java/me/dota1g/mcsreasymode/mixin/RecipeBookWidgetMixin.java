package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RecipeBookWidget.class)
public abstract class RecipeBookWidgetMixin {

    @Shadow private TextFieldWidget searchField;

    @Inject(method = "reset", at = @At("TAIL"))
    private void mcsreasymode$autoFocusSearch(boolean isNarrow, CallbackInfo ci) {
        if (searchField == null || !Mcsreasymode.CONFIG.autoFocusSearchField) {
            return;
        }

        searchField.setSelected(true);

        Screen currentScreen = MinecraftClient.getInstance().currentScreen;
        if (currentScreen != null) {
            ((AbstractParentElementInvoker) currentScreen).mcsreasymode$setFocused((RecipeBookWidget) (Object) this);
        }
    }
}