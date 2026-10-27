package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public abstract class HandledScreenSearchFocusMixin {

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void mcsreasymode$focusRecipeSearchOnChatKey(int keyCode, int scanCode, int modifiers,
                                                           CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!Mcsreasymode.CONFIG.focusSearchOnChatKey || client.player == null || client.player.isSpectator()
                || !client.options.keyChat.matchesKey(keyCode, scanCode)
                || !((Object) this instanceof RecipeBookProvider)) {
            return;
        }

        RecipeBookWidget book = ((RecipeBookProvider) (Object) this).getRecipeBookWidget();
        TextFieldWidget searchField = ((RecipeBookWidgetAccessor) book).mcsreasymode$getSearchField();
        if (!book.isOpen() || searchField == null) {
            return;
        }

        searchField.setSelected(true);
        ((RecipeBookWidgetAccessor) book).mcsreasymode$setSearching(true);
        ((AbstractParentElementInvoker) (Screen) (Object) this).mcsreasymode$setFocused(book);
        cir.setReturnValue(true);
    }
}
