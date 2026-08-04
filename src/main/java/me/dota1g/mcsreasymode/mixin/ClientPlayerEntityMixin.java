package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.client.GodsensBoatStatusState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void mcsreasymode$trackGodsensBoatRide(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        if (MinecraftClient.getInstance().player == player) {
            GodsensBoatStatusState.tick(player);
        }
    }
}
