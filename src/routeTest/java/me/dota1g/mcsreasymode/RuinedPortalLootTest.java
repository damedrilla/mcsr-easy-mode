package me.dota1g.mcsreasymode;

import net.minecraft.Bootstrap;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.structure.RuinedPortalStructurePiece;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class RuinedPortalLootTest {
    public static void main(String[] args) throws Exception {
        Bootstrap.initialize();
        List<BlockPos> frame = new ArrayList<>();
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 5; y++) {
                if (((x == 0 || x == 3) && y >= 1 && y <= 3)
                        || ((y == 0 || y == 4) && (x == 1 || x == 2))) frame.add(new BlockPos(x, y, 0));
            }
        }
        require(frame.size() == 10 && RuinedPortalFramePlanner.missingForMinimumFrame(frame, Collections.emptyList()) == 0,
                "Complete minimum frame was not recognized");
        BlockPos removed = frame.remove(0);
        require(RuinedPortalFramePlanner.missingForMinimumFrame(frame, Collections.singletonList(removed)) > 0,
                "Crying obsidian incorrectly counted as usable frame obsidian");

        for (int index = 1; index <= 13; index++) {
            String name = index <= 10 ? "portal_" + index : "giant_portal_" + (index - 10);
            String path = "data/minecraft/structures/ruined_portal/" + name + ".nbt";
            try (InputStream input = RuinedPortalLootTest.class.getClassLoader().getResourceAsStream(path)) {
                require(input != null, "Missing vanilla template: " + path);
                Structure template = new Structure();
                template.fromTag(NbtIo.readCompressed(input));
                for (BlockRotation rotation : BlockRotation.values()) {
                    StructurePlacementData placement = new StructurePlacementData().setRotation(rotation)
                            .setMirror(BlockMirror.FRONT_BACK).setPosition(new BlockPos(template.getSize().getX() / 2, 0,
                                    template.getSize().getZ() / 2));
                    RuinedPortalFramePlanner.Result result = RuinedPortalFramePlanner.analyze(template,
                            new BlockPos(32, 70, -16), placement,
                            RuinedPortalStructurePiece.VerticalPlacement.ON_LAND_SURFACE, false);
                    require(result.missingObsidian >= 0 && result.missingObsidian <= 10,
                            "Invalid frame deficit for " + name + " " + rotation);
                    require(!template.getInfosForBlock(new BlockPos(32, 70, -16), placement, Blocks.CHEST).isEmpty(),
                            "No template chest found in " + name);
                }
            }
        }

        SimpleInventory obsidian = new SimpleInventory(27);
        obsidian.setStack(0, new ItemStack(Items.OBSIDIAN, 2));
        RuinedPortalResourceRules.apply(obsidian, 5, false, false);
        require(count(obsidian, Items.OBSIDIAN) == 5 && count(obsidian, Items.WATER_BUCKET) == 0,
                "Obsidian route did not fill exactly the frame deficit");
        require(count(obsidian, Items.GOLDEN_CARROT) == 6 && count(obsidian, Items.IRON_NUGGET) == 18
                        && count(obsidian, Items.FIRE_CHARGE) == 1,
                "Food, spare iron nuggets, or ignition missing");

        SimpleInventory bucket = new SimpleInventory(27);
        bucket.setStack(0, new ItemStack(Items.OBSIDIAN, 2));
        RuinedPortalResourceRules.apply(bucket, 5, true, false);
        require(count(bucket, Items.OBSIDIAN) == 4 && count(bucket, Items.WATER_BUCKET) == 1,
                "Bucket route did not reserve exactly one frame block for lava casting");

        SimpleInventory sufficient = new SimpleInventory(27);
        sufficient.setStack(0, new ItemStack(Items.OBSIDIAN, 7));
        sufficient.setStack(1, new ItemStack(Items.GOLDEN_APPLE));
        sufficient.setStack(2, new ItemStack(Items.FLINT_AND_STEEL));
        sufficient.setStack(3, new ItemStack(Items.IRON_NUGGET, 20));
        RuinedPortalResourceRules.apply(sufficient, 5, false, false);
        require(count(sufficient, Items.GOLDEN_CARROT) == 0 && count(sufficient, Items.FIRE_CHARGE) == 0
                        && count(sufficient, Items.IRON_NUGGET) == 20,
                "Already sufficient vanilla loot was modified");

        int animalRoutes = 0;
        for (long seed = 0; seed < 1000; seed++) {
            require(McsreasymodeRuinedPortalLootPiece.animalFoodRoute(seed)
                            == McsreasymodeRuinedPortalLootPiece.animalFoodRoute(seed),
                    "Food route changed for the same seed");
            if (McsreasymodeRuinedPortalLootPiece.animalFoodRoute(seed)) animalRoutes++;
        }
        require(animalRoutes > 400 && animalRoutes < 600, "Food routes are not balanced");

        SimpleInventory animal = new SimpleInventory(27);
        animal.setStack(0, new ItemStack(Items.GOLDEN_APPLE));
        animal.setStack(1, new ItemStack(Items.GOLDEN_CARROT, 3));
        animal.setStack(2, new ItemStack(Items.GOLDEN_SWORD));
        RuinedPortalResourceRules.apply(animal, 5, false, true);
        require(count(animal, Items.GOLDEN_APPLE) == 0 && count(animal, Items.GOLDEN_CARROT) == 0,
                "Animal route retained chest food");
        require(count(animal, Items.GOLDEN_SWORD) == 1
                        && EnchantmentHelper.getLevel(Enchantments.FIRE_ASPECT, animal.getStack(2)) > 0,
                "Animal route did not enchant the golden sword");
        SimpleInventory noSword = new SimpleInventory(27);
        RuinedPortalResourceRules.apply(noSword, 5, false, true);
        require(findFireAspectSword(noSword), "Animal route did not add a Fire Aspect golden sword");

        CompoundTag saved = new CompoundTag();
        saved.putLong("ChestPos", new BlockPos(4, 80, -5).asLong());
        saved.putLong("PlanSeed", 123456L);
        saved.putInt("MissingFrameObsidian", 6);
        saved.putBoolean("BucketRoute", true);
        saved.putBoolean("AnimalFoodRoute", true);
        ListTag plan = new ListTag();
        CompoundTag stack = new ItemStack(Items.WATER_BUCKET).toTag(new CompoundTag());
        stack.putInt("Slot", 7);
        plan.add(stack);
        saved.put("PlannedInventory", plan);
        McsreasymodeRuinedPortalLootPiece restored = new McsreasymodeRuinedPortalLootPiece(saved);
        CompoundTag roundTrip = new CompoundTag();
        restored.toNbt(roundTrip);
        require(saved.equals(roundTrip), "Saved portal route or inventory changed after reload");
        require(!restored.intersectsChunk(new ChunkPos(0, -1), 12), "Loot metadata reshaped portal terrain");
        System.out.println("Ruined portal loot checks passed (13 templates, frame, routes, resources, NBT).");
    }

    private static int count(SimpleInventory inventory, Item item) {
        int count = 0;
        for (int slot = 0; slot < inventory.size(); slot++) {
            ItemStack stack = inventory.getStack(slot);
            if (stack.getItem() == item) count += stack.getCount();
        }
        return count;
    }

    private static boolean findFireAspectSword(SimpleInventory inventory) {
        for (int slot = 0; slot < inventory.size(); slot++) {
            ItemStack stack = inventory.getStack(slot);
            if (stack.getItem() == Items.GOLDEN_SWORD
                    && EnchantmentHelper.getLevel(Enchantments.FIRE_ASPECT, stack) > 0) return true;
        }
        return false;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
