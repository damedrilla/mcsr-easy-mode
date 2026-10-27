package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import me.dota1g.mcsreasymode.McsreasymodeConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.ThreadLocalRandom;

@Mixin(RecipeBookWidget.class)
public abstract class RecipeBookWidgetKeyMixin {

    @Shadow private TextFieldWidget searchField;

    @Shadow private boolean searching;

    @Shadow
    private void refreshResults(boolean resetCurrentPage) {
        throw new AssertionError("mixin shadow not applied");
    }

    @Shadow
    private void refreshSearchResults() {
        throw new AssertionError("mixin shadow not applied");
    }

    private boolean mcsreasymode$suppressNextChar = false;

    // --- typing simulation state ---
    private String mcsreasymode$typingText = null;
    private int mcsreasymode$typingIndex = 0;
    private long mcsreasymode$nextCharAt = 0L;

    private static final double MCSREASYMODE$BASE_INTERVAL_MS = 1000.0 / 17.6; // ~79ms, matches AHK CPS
    private static final double MCSREASYMODE$JITTER_MS = MCSREASYMODE$BASE_INTERVAL_MS * 0.3;

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void mcsreasymode$backspaceMode(int keyCode, int scanCode, int modifiers,
                                            CallbackInfoReturnable<Boolean> cir) {
        McsreasymodeConfig.SearchBackspaceMode mode = Mcsreasymode.CONFIG.searchBackspaceMode;
        MinecraftClient client = MinecraftClient.getInstance();
        if (mode == McsreasymodeConfig.SearchBackspaceMode.NORMAL || keyCode != GLFW.GLFW_KEY_BACKSPACE
                || searchField == null || !searchField.isFocused()
                || !((RecipeBookWidget) (Object) this).isOpen()
                || client.player == null || client.player.isSpectator()) {
            return;
        }

        searching = false;
        if (mode == McsreasymodeConfig.SearchBackspaceMode.TO_WHITESPACE) {
            searchField.eraseWords(-1);
        } else {
            searchField.setText("");
        }
        this.refreshSearchResults();
        cir.setReturnValue(true);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void mcsreasymode$presetSearch(int keyCode, int scanCode, int modifiers,
                                           CallbackInfoReturnable<Boolean> cir) {
        if (!Mcsreasymode.CONFIG.searchPresetsEnabled) return;

        boolean altHeld = (modifiers & GLFW.GLFW_MOD_ALT) != 0;
        String preset = Mcsreasymode.CONFIG.searchPresets.get(keyCode);
        if (altHeld && preset != null) {
            searchField.setText("");
            refreshResults(true);

            mcsreasymode$typingText = preset;
            mcsreasymode$typingIndex = 0;
            mcsreasymode$nextCharAt = System.currentTimeMillis() + 35L;

            mcsreasymode$suppressNextChar = true;
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    private void mcsreasymode$suppressPresetChar(char chr, int keyCode, CallbackInfoReturnable<Boolean> cir) {
        if (mcsreasymode$suppressNextChar) {
            mcsreasymode$suppressNextChar = false;
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void mcsreasymode$advanceTyping(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (mcsreasymode$typingText == null || mcsreasymode$typingIndex >= mcsreasymode$typingText.length()) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now < mcsreasymode$nextCharAt) {
            return;
        }

        char next = mcsreasymode$typingText.charAt(mcsreasymode$typingIndex);
        searchField.write(String.valueOf(next));
        refreshResults(false);
        mcsreasymode$typingIndex++;

        double jitter = ThreadLocalRandom.current().nextDouble(-MCSREASYMODE$JITTER_MS, MCSREASYMODE$JITTER_MS);
        long delayMs = Math.max(15L, Math.round(MCSREASYMODE$BASE_INTERVAL_MS + jitter));
        mcsreasymode$nextCharAt = now + delayMs;

        if (mcsreasymode$typingIndex >= mcsreasymode$typingText.length()) {
            mcsreasymode$typingText = null;
        }
    }
}
