package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.client.GodsensBoatStatusState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
    @Inject(method = "onGameJoin", at = @At("HEAD"))
    private void mcsreasymode$resetGodsensStatusForNewWorld(GameJoinS2CPacket packet, CallbackInfo ci) {
        GodsensBoatStatusState.reset();
    }
}
