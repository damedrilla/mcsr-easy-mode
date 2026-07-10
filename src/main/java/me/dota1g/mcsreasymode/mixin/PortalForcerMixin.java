package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import me.dota1g.mcsreasymode.RankedRngState;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.PortalForcer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PortalForcer.class)
public abstract class PortalForcerMixin {
    @Shadow
    @Final
    private ServerWorld world;

    @Inject(method = "createPortal", at = @At("HEAD"), cancellable = true)
    private void mcsreasymode$createBlindPortalOnSurface(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (this.world.getRegistryKey() == World.OVERWORLD) {
            Integer surfaceY = RankedRngState.consumeBlindPortalForcedSurfaceY();
            if (surfaceY == null) {
                return;
            }

            int x = MathHelper.floor(entity.getX());
            int y = MathHelper.clamp(surfaceY, 2, this.world.getDimensionHeight() - 5) + 1;
            int z = MathHelper.floor(entity.getZ());
            boolean floatingPortal = this.mcsreasymode$isLiquidPortalFloor(x, y, z);
            if (floatingPortal) {
                y = MathHelper.clamp(Math.max(y, 70), 2, this.world.getDimensionHeight() - 5);
            }

            this.mcsreasymode$buildSurfacePortal(x, y, z, floatingPortal);
            Mcsreasymode.debug("Blind portal surfacing built Overworld portal at X " + x + ", Y " + y + ", Z " + z + ".");
            cir.setReturnValue(true);
        }
    }

    private boolean mcsreasymode$isLiquidPortalFloor(int x, int y, int z) {
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int width = -1; width <= 2; width++) {
            for (int depth = -1; depth <= 1; depth++) {
                mutable.set(x + width, y - 1, z + depth);
                if (this.world.getBlockState(mutable).getMaterial().isLiquid()) {
                    return true;
                }
            }
        }
        return false;
    }

    private void mcsreasymode$buildSurfacePortal(int x, int y, int z, boolean addFooting) {
        BlockState obsidian = Blocks.OBSIDIAN.getDefaultState();
        BlockState portal = Blocks.NETHER_PORTAL.getDefaultState().with(NetherPortalBlock.AXIS, Direction.Axis.X);
        BlockState air = Blocks.AIR.getDefaultState();
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        for (int width = -1; width <= 2; width++) {
            for (int depth = -1; depth <= 1; depth++) {
                for (int height = 0; height <= 3; height++) {
                    mutable.set(x + width, y + height, z + depth);
                    this.world.setBlockState(mutable, air, 3);
                }
            }
        }

        if (addFooting) {
            for (int width = 0; width <= 1; width++) {
                mutable.set(x + width, y - 1, z - 1);
                this.world.setBlockState(mutable, obsidian, 3);
                mutable.set(x + width, y - 1, z + 1);
                this.world.setBlockState(mutable, obsidian, 3);
            }
        }

        for (int width = -1; width <= 2; width++) {
            for (int height = -1; height <= 3; height++) {
                mutable.set(x + width, y + height, z);
                if (width == -1 || width == 2 || height == -1 || height == 3) {
                    this.world.setBlockState(mutable, obsidian, 3);
                } else {
                    this.world.setBlockState(mutable, air, 3);
                }
            }
        }

        for (int width = 0; width <= 1; width++) {
            for (int height = 0; height <= 2; height++) {
                mutable.set(x + width, y + height, z);
                this.world.setBlockState(mutable, portal, 18);
            }
        }
    }
}
