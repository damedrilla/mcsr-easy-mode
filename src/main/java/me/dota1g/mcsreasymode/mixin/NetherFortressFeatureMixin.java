package me.dota1g.mcsreasymode.mixin;
import net.minecraft.entity.EntityType;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.List;

@Mixin(targets = "net.minecraft.world.gen.feature.NetherFortressFeature")
public class NetherFortressFeatureMixin {
    @Shadow @Final
    private static List<Biome.SpawnEntry> MONSTER_SPAWNS;

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void onClassInit(CallbackInfo ci) {
//        MONSTER_SPAWNS.removeIf(entry -> entry.type == EntityType.WITHER_SKELETON);

        MONSTER_SPAWNS.set(2, new Biome.SpawnEntry(EntityType.WITHER_SKELETON, 1, 1, 2));
    }
}
