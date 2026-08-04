package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.client.GodsensBoatStatusState;
import net.minecraft.entity.vehicle.BoatEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BoatEntity.class)
public abstract class BoatEntityClientMixin {
    @Inject(method = "setInputs", at = @At("HEAD"))
    private void mcsreasymode$trackGodsensSteering(boolean pressingLeft, boolean pressingRight, boolean pressingForward, boolean pressingBack, CallbackInfo ci) {
        GodsensBoatStatusState.onBoatInput((BoatEntity) (Object) this, pressingLeft, pressingRight);
    }
}
