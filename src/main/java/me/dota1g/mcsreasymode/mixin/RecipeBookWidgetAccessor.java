package me.dota1g.mcsreasymode.mixin;

import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RecipeBookWidget.class)
public interface RecipeBookWidgetAccessor {
    @Accessor("searchField")
    TextFieldWidget mcsreasymode$getSearchField();

    @Invoker("refreshResults")
    void mcsreasymode$refreshResults(boolean resetCurrentPage);
}