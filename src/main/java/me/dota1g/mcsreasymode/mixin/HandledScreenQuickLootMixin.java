package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public abstract class HandledScreenQuickLootMixin {

    @Shadow protected Slot focusedSlot;
    @Shadow protected ScreenHandler handler;

    @Shadow
    protected abstract void onMouseClick(Slot slot, int invSlot, int clickData, SlotActionType actionType);

    private Slot mcsreasymode$lastQuickLootSlot;
    private long mcsreasymode$lastQuickLootTime;

    private static final long MCSREASYMODE$WINDOW_MS = 500L;

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void mcsreasymode$fastQuickLoot(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (!Mcsreasymode.CONFIG.fastQuickLoot) {
            return;
        }
        if (button != 0 || !Screen.hasShiftDown() || this.focusedSlot == null || !this.focusedSlot.hasStack()) {
            return;
        }

        Slot clicked = this.focusedSlot;
        ItemStack clickedStack = clicked.getStack();
        long now = System.currentTimeMillis();

        boolean matches = mcsreasymode$lastQuickLootSlot != null
                && mcsreasymode$lastQuickLootSlot.inventory == clicked.inventory
                && ItemStack.areItemsEqual(mcsreasymode$lastQuickLootSlot.getStack(), clickedStack)
                && ItemStack.areTagsEqual(mcsreasymode$lastQuickLootSlot.getStack(), clickedStack)
                && (now - mcsreasymode$lastQuickLootTime) < MCSREASYMODE$WINDOW_MS;

        if (matches) {
            for (Slot slot : this.handler.slots) {
                if (slot != null && slot.hasStack() && slot.inventory == clicked.inventory
                        && ItemStack.areItemsEqual(slot.getStack(), clickedStack)
                        && ItemStack.areTagsEqual(slot.getStack(), clickedStack)) {
                    this.onMouseClick(slot, slot.id, button, SlotActionType.QUICK_MOVE);
                }
            }
            mcsreasymode$lastQuickLootSlot = null;
            cir.setReturnValue(true);
        } else {
            mcsreasymode$lastQuickLootSlot = clicked;
            mcsreasymode$lastQuickLootTime = now;
        }
    }
}