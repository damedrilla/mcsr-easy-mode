package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.client.ItemHighlightState;
import net.minecraft.client.render.item.ItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    @ModifyArg(
            method = "renderGuiItemOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/font/TextRenderer;draw(Ljava/lang/String;FFIZLnet/minecraft/util/math/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;ZII)I"
            ),
            index = 3
    )
    private int mcsreasymode$colorizeCount(int originalColor) {
        if (ItemHighlightState.highlight) {
            return 0x55FF55;
        }
        return originalColor;
    }
}