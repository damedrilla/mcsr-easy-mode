//package me.dota1g.mcsreasymode.mixin;
//
//import net.minecraft.client.MinecraftClient;
//import net.minecraft.client.gui.screen.recipebook.R
//import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
//import net.minecraft.client.gui.widget.TextFieldWidget;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Shadow;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
//@Mixin(RecipeBookWidget.class)
//public abstract class RecipeBookWidgetMixin {
//
//    @Shadow private TextFieldWidget searchField;
//
//    @Inject(method = "initialize", at = @At("TAIL"))
//    private void mcsreasymode$autoFocusSearch(int width, int height, MinecraftClient client,
//                                              boolean narrow, RecipeBookCategory category, CallbackInfo ci) {
//        if (searchField != null) {
//            searchField.setTextFieldFocused(true);
//        }
//    }
//}