package me.dota1g.mcsreasymode.mixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.Mutable;
import java.util.List;
import net.minecraft.world.biome.Biome;

@Mixin(targets = "net.minecraft.world.gen.feature.NetherFortressFeature")
public interface NetherFortressFeatureAccessor {
    @Accessor("MONSTER_SPAWNS")
    @Mutable
    static void setMonsterSpawns(List<Biome.SpawnEntry> spawns) {
        throw new AssertionError();
    }
}
