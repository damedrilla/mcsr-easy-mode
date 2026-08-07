package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import me.dota1g.mcsreasymode.client.HotbarHotkeyLabels;
import me.dota1g.mcsreasymode.client.HotbarHotkeyOverlayRenderer;
import me.dota1g.mcsreasymode.client.ItemHighlightHelper;
import me.dota1g.mcsreasymode.client.ItemHighlightState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {
    @Shadow
    protected int x;

    @Shadow
    protected int y;

    @Shadow
    @Final
    protected ScreenHandler handler;

    @Shadow
    @Final
    protected PlayerInventory playerInventory;

    @Inject(method = "render", at = @At("TAIL"))
    private void mcsreasymode$drawHotbarHotkeyLabels(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!Mcsreasymode.shouldShowHotbarHotkeys() || Mcsreasymode.CONFIG == null) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        String[] labels = HotbarHotkeyLabels.get(client);
        for (Slot slot : this.handler.slots) {
            if (slot.inventory != this.playerInventory) {
                continue;
            }

            this.mcsreasymode$drawHotbarHotkeyLabel(matrices, client, slot, labels);
        }
    }

    private void mcsreasymode$drawHotbarHotkeyLabel(MatrixStack matrices, MinecraftClient client, Slot slot, String[] labels) {
        int inventoryIndex = ((SlotAccessor) slot).mcsreasymode$getIndex();
        if (inventoryIndex >= 0 && inventoryIndex < 9) {
            HotbarHotkeyOverlayRenderer.renderScreenSlotLabel(matrices, client, Mcsreasymode.CONFIG, labels[inventoryIndex], this.x + slot.x, this.y + slot.y);
        } else if (inventoryIndex == 40 && labels.length > 9) {
            HotbarHotkeyOverlayRenderer.renderScreenSlotLabel(matrices, client, Mcsreasymode.CONFIG, labels[9], this.x + slot.x, this.y + slot.y);
        }
    }

    @Redirect(
            method = "drawSlot",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/item/ItemRenderer;renderGuiItemOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V"
            )
    )
    private void mcsreasymode$renderOverlayWithHighlight(
            ItemRenderer itemRenderer, TextRenderer textRenderer, ItemStack stack, int x, int y, String countOverride) {
        ItemHighlightState.highlight = ItemHighlightHelper.shouldHighlight(this.handler.slots, stack);
        itemRenderer.renderGuiItemOverlay(textRenderer, stack, x, y, countOverride);
        ItemHighlightState.highlight = false;
    }


}
