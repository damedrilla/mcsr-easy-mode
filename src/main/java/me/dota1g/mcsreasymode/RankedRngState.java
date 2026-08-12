package me.dota1g.mcsreasymode;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.Heightmap;
import net.minecraft.block.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Iterator;

public final class RankedRngState {
    private static final int PEARL_PITY_BARTERS = 24;
    private static final int OBSIDIAN_PITY_BARTERS = 24;
    private static final int STRING_PITY_BARTERS = 24;
    private static final int BASTION_IRON_MINIMUM = 3;
    private static final int BASTION_OBSIDIAN_MINIMUM = 5;
    private static final int FLINT_PITY_BREAKS = 10;
    private static final int ONE_SHOT_IRON_MINIMUM = 26;
    private static final int ONE_SHOT_IRON_MAXIMUM = 36;

    private static int pearlDryBarters;
    private static int obsidianDryBarters;
    private static int stringDryBarters;
    private static int oneShotIronBudget;
    private static int oneShotIronGiven;
    private static int flintDryBreaks;
    private static int thrownEyeCount;
    private static boolean bastionChestAdjusted;
    private static boolean blindPortalSurfacePending;
    private static boolean blindPortalSurfaceUsed;
    private static Integer blindPortalForcedSurfaceY;

    private RankedRngState() {
    }

    public static void reset() {
        resetBastionAttemptFields();
        flintDryBreaks = 0;
        thrownEyeCount = 0;
        blindPortalSurfacePending = false;
        blindPortalSurfaceUsed = false;
        blindPortalForcedSurfaceY = null;
        pendingRuinedPortalAnimalSpawns.clear();
        Mcsreasymode.debug("Ranked RNG state reset for new world.");
    }

    public static void resetBastionPracticeAttempt() {
        resetBastionAttemptFields();
        Mcsreasymode.debug("Bastion practice RNG state reset for new attempt.");
    }

    private static void resetBastionAttemptFields() {
        pearlDryBarters = 0;
        obsidianDryBarters = 0;
        stringDryBarters = 0;
        oneShotIronBudget = 0;
        oneShotIronGiven = 0;
        bastionChestAdjusted = false;
    }

    public static void resetOneShotIronForPracticeRun() {
        stringDryBarters = 0;
        oneShotIronBudget = 0;
        oneShotIronGiven = 0;
        Mcsreasymode.debug("One Shot for RSG iron budget reset for new bastion practice run.");
    }

    public static List<ItemStack> applyPiglinBarterPity(List<ItemStack> original, Random random, boolean rankedBarterPity, boolean rankedStringPity, boolean oneShotForRsg) {
        List<ItemStack> result = new ArrayList<>(original);

        boolean hasPearls = result.stream().anyMatch(stack -> stack.getItem() == Items.ENDER_PEARL);
        boolean hasObsidian = result.stream().anyMatch(stack -> stack.getItem() == Items.OBSIDIAN);
        boolean hasString = result.stream().anyMatch(stack -> stack.getItem() == Items.STRING);
        if (rankedStringPity && oneShotForRsg && hasString) {
            int ironCount = 0;
            int stringLeftover = 0;
            for (int i = 0; i < result.size(); i++) {
                ItemStack stack = result.get(i);
                if (stack.getItem() == Items.STRING) {
                    int convertedCount = consumeOneShotIron(stack.getCount(), random);
                    ironCount += convertedCount;
                    if (convertedCount > 0) {
                        result.set(i, new ItemStack(Items.IRON_INGOT, convertedCount));
                        stringLeftover += stack.getCount() - convertedCount;
                    }
                }
            }
            if (stringLeftover > 0) {
                result.add(new ItemStack(Items.STRING, stringLeftover));
            }
            if (ironCount > 0) {
                Mcsreasymode.debug("Piglin barter one shot for RSG: converted natural string trade into " + ironCount
                        + " iron ingots (" + oneShotIronGiven + "/" + oneShotIronBudget + " budget used).");
            }
        }

        if (rankedBarterPity && hasPearls) {
            pearlDryBarters = 0;
        } else if (rankedBarterPity) {
            pearlDryBarters++;
        }

        if (rankedBarterPity && hasObsidian) {
            obsidianDryBarters = 0;
        } else if (rankedBarterPity) {
            obsidianDryBarters++;
        }

        if (rankedStringPity && hasString) {
            stringDryBarters = 0;
        } else if (rankedStringPity) {
            stringDryBarters++;
        }

        if (rankedBarterPity && !hasPearls && pearlDryBarters >= PEARL_PITY_BARTERS) {
            int pearlCount = 4 + random.nextInt(5);
            result.add(new ItemStack(Items.ENDER_PEARL, pearlCount));
            Mcsreasymode.debug("Piglin barter pearl pity proc: added " + pearlCount + " ender pearls after " + PEARL_PITY_BARTERS + " dry barters.");
            pearlDryBarters = 0;
            return result;
        }

        if (rankedBarterPity && !hasObsidian && obsidianDryBarters >= OBSIDIAN_PITY_BARTERS) {
            result.add(new ItemStack(Items.OBSIDIAN));
            Mcsreasymode.debug("Piglin barter obsidian pity proc: added 1 obsidian after " + OBSIDIAN_PITY_BARTERS + " dry barters.");
            obsidianDryBarters = 0;
        }
        
        if (rankedStringPity && !hasString && stringDryBarters >= STRING_PITY_BARTERS) {
            int count = 8 + random.nextInt(15);
            if (oneShotForRsg) {
                int ironCount = consumeOneShotIron(count, random);
                if (ironCount > 0) {
                    result.add(new ItemStack(Items.IRON_INGOT, ironCount));
                    Mcsreasymode.debug("Piglin barter one shot for RSG proc: added " + ironCount
                            + " iron ingots instead of string after " + STRING_PITY_BARTERS + " dry barters ("
                            + oneShotIronGiven + "/" + oneShotIronBudget + " budget used).");
                }
            } else {
                result.add(new ItemStack(Items.STRING, count));
                Mcsreasymode.debug("Piglin barter string pity proc: added " + count + " strings after " + STRING_PITY_BARTERS + " dry barters.");
            }
            stringDryBarters = 0;
        }
        return result;
    }

    private static int consumeOneShotIron(int requested, Random random) {
        if (requested <= 0) {
            return 0;
        }
        if (oneShotIronBudget <= 0) {
            oneShotIronBudget = ONE_SHOT_IRON_MINIMUM + random.nextInt(ONE_SHOT_IRON_MAXIMUM - ONE_SHOT_IRON_MINIMUM + 1);
        }

        int remaining = oneShotIronBudget - oneShotIronGiven;
        if (remaining <= 0) {
            return 0;
        }

        int granted = Math.min(requested, remaining);
        oneShotIronGiven += granted;
        return granted;
    }

    public static boolean shouldAdjustBastionChest() {
        if (bastionChestAdjusted) {
            return false;
        }
        bastionChestAdjusted = true;
        return true;
    }

    public static int getBastionIronMinimum() {
        return BASTION_IRON_MINIMUM;
    }

    public static int getBastionObsidianMinimum() {
        return BASTION_OBSIDIAN_MINIMUM;
    }

    public static List<ItemStack> applyFlintPity(List<ItemStack> original) {
        List<ItemStack> result = new ArrayList<>(original);
        boolean hasFlint = result.stream().anyMatch(stack -> stack.getItem() == Items.FLINT);

        if (hasFlint) {
            flintDryBreaks = 0;
            return result;
        }

        flintDryBreaks++;
        if (flintDryBreaks >= FLINT_PITY_BREAKS) {
            result.clear();
            result.add(new ItemStack(Items.FLINT));
            Mcsreasymode.debug("Flint pity proc: forced 1 flint after " + FLINT_PITY_BREAKS + " gravel drops without flint.");
            flintDryBreaks = 0;
        }

        return result;
    }

    public static boolean shouldProtectThrownEye() {
        thrownEyeCount++;
        boolean protect = thrownEyeCount == 2;
        if (protect) {
            Mcsreasymode.debug("Eye of ender standardized: protected throw #2 from breaking.");
        } else {
            Mcsreasymode.debug("Eye of ender throw #" + thrownEyeCount + " uses vanilla break rate.");
        }
        return protect;
    }

    public static void armBlindPortalSurface() {
        if (!blindPortalSurfaceUsed) {
            blindPortalSurfacePending = true;
            Mcsreasymode.debug("Blind portal surfacing armed for next Nether-to-Overworld portal creation.");
        }
    }

    public static boolean shouldSurfaceBlindPortal() {
        return blindPortalSurfacePending && !blindPortalSurfaceUsed;
    }

    public static boolean canUseBlindPortalSurface() {
        return !blindPortalSurfaceUsed;
    }

    public static void consumeBlindPortalSurface() {
        blindPortalSurfacePending = false;
        blindPortalSurfaceUsed = true;
        Mcsreasymode.debug("Blind portal surfacing consumed.");
    }

    public static void clearPendingBlindPortalSurface() {
        blindPortalSurfacePending = false;
        blindPortalForcedSurfaceY = null;
    }

    public static void setBlindPortalForcedSurfaceY(int surfaceY) {
        blindPortalForcedSurfaceY = surfaceY;
    }

    public static Integer getBlindPortalForcedSurfaceY() {
        return blindPortalForcedSurfaceY;
    }

    public static Integer consumeBlindPortalForcedSurfaceY() {
        Integer surfaceY = blindPortalForcedSurfaceY;
        blindPortalForcedSurfaceY = null;
        return surfaceY;
    }

    private static final List<BlockPos> pendingRuinedPortalAnimalSpawns = new ArrayList<>();
    private static final EntityType<?>[] RUINED_PORTAL_ANIMAL_TYPES = new EntityType<?>[] {
            EntityType.COW, EntityType.PIG, EntityType.SHEEP, EntityType.CHICKEN
    };
    private static final int RUINED_PORTAL_ANIMAL_HERD_SIZE = 4;
    private static final int RUINED_PORTAL_ANIMAL_RADIUS_CHUNKS = 3;

    public static void registerPendingRuinedPortalAnimalSpawn(BlockPos pos) {
        pendingRuinedPortalAnimalSpawns.add(pos.toImmutable());
        Mcsreasymode.debug("Ruined portal animal guarantee armed near " + pos.toShortString() + ".");
    }

    public static void processPendingRuinedPortalAnimalSpawns(ServerWorld world) {
        if (pendingRuinedPortalAnimalSpawns.isEmpty()) {
            return;
        }

        List<BlockPos> readyPositions = new ArrayList<>();
        Iterator<BlockPos> iterator = pendingRuinedPortalAnimalSpawns.iterator();
        while (iterator.hasNext()) {
            BlockPos pos = iterator.next();
            if (world.isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4)) {
                readyPositions.add(pos);
                iterator.remove();
            }
        }

        for (BlockPos pos : readyPositions) {
            spawnRuinedPortalAnimalHerd(world, pos);
        }
    }

    private static void spawnRuinedPortalAnimalHerd(ServerWorld world, BlockPos center) {
        Random random = world.random;
        EntityType<?> species = RUINED_PORTAL_ANIMAL_TYPES[random.nextInt(RUINED_PORTAL_ANIMAL_TYPES.length)];
        int radiusBlocks = RUINED_PORTAL_ANIMAL_RADIUS_CHUNKS * 16;
        int spawned = 0;
        int attempts = 0;

        while (spawned < RUINED_PORTAL_ANIMAL_HERD_SIZE && attempts < RUINED_PORTAL_ANIMAL_HERD_SIZE * 20) {
            attempts++;
            int x = center.getX() + random.nextInt(radiusBlocks * 2 + 1) - radiusBlocks;
            int z = center.getZ() + random.nextInt(radiusBlocks * 2 + 1) - radiusBlocks;
            if (!world.isChunkLoaded(x >> 4, z >> 4)) {
                continue;
            }

            int y = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
            if (trySpawnAnimalAt(world, species, x, y, z, random)) {
                spawned++;
            }
        }

        if (spawned == 0) {
            spawned = forceSpawnAtPortalColumn(world, center, species, random);
            if (spawned > 0) {
                Mcsreasymode.debug("Ruined portal animal guarantee fallback: forced spawn at portal column near "
                        + center.toShortString() + " after radius search found no valid ground.");
            }
        }

        Mcsreasymode.debug("Ruined portal animal guarantee spawned " + spawned + " " + species
                + "(s) within " + RUINED_PORTAL_ANIMAL_RADIUS_CHUNKS + " chunks of " + center.toShortString() + ".");
    }

    private static boolean trySpawnAnimalAt(ServerWorld world, EntityType<?> species, int x, int y, int z, Random random) {
        BlockPos ground = new BlockPos(x, y - 1, z);
        BlockPos feet = new BlockPos(x, y, z);
        BlockState groundState = world.getBlockState(ground);
        if (!groundState.getMaterial().isSolid() || groundState.getMaterial().isLiquid()) {
            return false;
        }
        if (!world.getFluidState(feet).isEmpty()) {
            return false;
        }

        Entity entity = species.create(world);
        if (!(entity instanceof MobEntity)) {
            return false;
        }

        MobEntity mob = (MobEntity) entity;
        mob.refreshPositionAndAngles(x + 0.5, y, z + 0.5, random.nextFloat() * 360.0F, 0.0F);
        mob.initialize(world, world.getLocalDifficulty(feet), SpawnReason.STRUCTURE, null, null);
        world.spawnEntity(mob);
        return true;
    }

    private static int forceSpawnAtPortalColumn(ServerWorld world, BlockPos center, EntityType<?> species, Random random) {
        int forcedSpawned = 0;
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        for (int attempt = 0; attempt < RUINED_PORTAL_ANIMAL_HERD_SIZE; attempt++) {
            int offsetX = attempt % 2 == 0 ? attempt : -attempt;
            int offsetZ = attempt / 2;
            int x = center.getX() + offsetX;
            int z = center.getZ() + offsetZ;

            int chunkX = x >> 4;
            int chunkZ = z >> 4;
            world.getChunk(chunkX, chunkZ);

            int y = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
            if (y <= 0) {
                continue;
            }

            if (trySpawnAnimalAt(world, species, x, y, z, random)) {
                forcedSpawned++;
            }
        }

        return forcedSpawned;
    }
}
