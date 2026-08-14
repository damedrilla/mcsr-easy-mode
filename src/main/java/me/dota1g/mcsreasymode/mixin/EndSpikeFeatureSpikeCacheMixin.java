package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collections;
import java.util.List;
import java.util.Random;

@Mixin(targets = "net.minecraft.world.gen.feature.EndSpikeFeature$SpikeCache")
public abstract class EndSpikeFeatureSpikeCacheMixin {

    private static final int MCSREASYMODE$FRONT_TOWER_INDEX = 9;
    private static final int MCSREASYMODE$FORCED_HEIGHT_L = 3; // l=3 -> height 76+3*3=85 (M85)

    @Redirect(method = "load", at = @At(value = "INVOKE",
            target = "Ljava/util/Collections;shuffle(Ljava/util/List;Ljava/util/Random;)V"))
    private void mcsreasymode$forceFrontTowerHeight(List<?> list, Random random) {
        Collections.shuffle(list, random);
//        if (!Mcsreasymode.CONFIG.forceFrontTowerM85) {
//            return;
//        }
        @SuppressWarnings("unchecked")
        List<Integer> intList = (List<Integer>) list;
        int currentPos = intList.indexOf(MCSREASYMODE$FORCED_HEIGHT_L);
        if (currentPos != -1 && currentPos != MCSREASYMODE$FRONT_TOWER_INDEX) {
            Collections.swap(intList, currentPos, MCSREASYMODE$FRONT_TOWER_INDEX);
        }
    }
}