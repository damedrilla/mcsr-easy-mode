package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import me.dota1g.mcsreasymode.RankedRngState;
import me.dota1g.mcsreasymode.SsgModeState;
import me.dota1g.mcsreasymode.StrongholdProtection;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Unique
    private boolean mcsreasymode$bastionPracticeRunning;

    @Inject(method = "loadWorld", at = @At("HEAD"))
    private void mcsreasymode$resetRankedRngState(CallbackInfo ci) {
        RankedRngState.reset();
        SsgModeState.reset();
        StrongholdProtection.reset();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void mcsreasymode$resetOneShotIronForBastionPractice(CallbackInfo ci) {
        MinecraftServer server = (MinecraftServer) (Object) this;
        Scoreboard scoreboard = server.getScoreboard();
        ScoreboardObjective objective = scoreboard.getNullableObjective("bastion.temp");
        boolean running = objective != null && scoreboard.getPlayerScore("running", objective).getScore() > 0;

        if (running && !this.mcsreasymode$bastionPracticeRunning
                && Mcsreasymode.isFunOneShotForRsgEnabled()) {
            RankedRngState.resetOneShotIronForPracticeRun();
        }
        this.mcsreasymode$bastionPracticeRunning = running;
    }
}
