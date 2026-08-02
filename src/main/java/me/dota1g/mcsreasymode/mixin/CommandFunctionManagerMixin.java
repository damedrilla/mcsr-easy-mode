package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.RankedRngState;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.CommandFunctionManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(CommandFunctionManager.class)
public abstract class CommandFunctionManagerMixin {
    private static final Identifier MCSREASYMODE_BASTION_PRACTICE_START = new Identifier("practice", "start");
    private static final Identifier MCSREASYMODE_BASTION_BUILDER_GENERATE = new Identifier("bastionbuilder", "generate");

    @Shadow
    public abstract Optional<CommandFunction> getFunction(Identifier id);

    @Inject(method = "execute", at = @At("HEAD"))
    private void mcsreasymode$resetBastionAttemptState(
            CommandFunction function,
            ServerCommandSource source,
            CallbackInfoReturnable<Integer> cir
    ) {
        if (MCSREASYMODE_BASTION_PRACTICE_START.equals(function.getId())
                && this.getFunction(MCSREASYMODE_BASTION_BUILDER_GENERATE).isPresent()) {
            RankedRngState.resetBastionPracticeAttempt();
        }
    }
}
