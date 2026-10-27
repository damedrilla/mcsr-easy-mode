package me.dota1g.mcsreasymode;

import me.dota1g.mcsreasymode.worldgen.VillageVariant;
import net.minecraft.Bootstrap;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.io.InputStream;
import java.util.Collections;

public final class VillageResourceTest {
    public static void main(String[] args) throws Exception {
        Bootstrap.initialize();
        for (VillageVariant variant : VillageVariant.values()) {
            String path = "data/minecraft/structures/" + variant.template + ".nbt";
            try (InputStream input = VillageResourceTest.class.getClassLoader().getResourceAsStream(path)) {
                require(input != null, "Missing vanilla smith template: " + path);
                Structure structure = new Structure();
                structure.fromTag(NbtIo.readCompressed(input));
                long chests = structure.getInfosForBlock(BlockPos.ORIGIN,
                                new StructurePlacementData().setRotation(BlockRotation.NONE), Blocks.CHEST).stream()
                        .filter(info -> info.tag != null && info.tag.contains("LootTable")).count();
                require(chests > 0, "Smith template has no discoverable loot chest: " + variant);
            }
        }

        SimpleInventory plain = new SimpleInventory(27);
        plain.setStack(0, new ItemStack(Items.IRON_INGOT));
        VillageResourceRules.apply(VillageVariant.PLAINS, false, 4, Collections.singletonList(plain), 0);
        require(count(plain, Items.IRON_INGOT) == 3, "Seven-iron route was not filled exactly");
        require(count(plain, Items.BREAD) == 1, "Food missing without haybales");

        SimpleInventory taiga = new SimpleInventory(27);
        taiga.setStack(0, new ItemStack(Items.OBSIDIAN, 5));
        VillageResourceRules.apply(VillageVariant.TAIGA, true, 4, Collections.singletonList(taiga), 0);
        require(count(taiga, Items.OBSIDIAN) == 10, "Taiga obsidian guarantee failed");
        require(count(taiga, Items.IRON_INGOT) == 4 && count(taiga, Items.BREAD) == 0,
                "Taiga chest iron or haybale rule failed");

        SimpleInventory taigaPickaxe = new SimpleInventory(27);
        taigaPickaxe.setStack(0, new ItemStack(Items.IRON_PICKAXE));
        VillageResourceRules.apply(VillageVariant.TAIGA, true, 3, Collections.singletonList(taigaPickaxe), 0);
        require(count(taigaPickaxe, Items.OBSIDIAN) == 10 && count(taigaPickaxe, Items.IRON_INGOT) == 0,
                "Existing taiga iron pickaxe should satisfy the mining route");

        SimpleInventory taigaPartialIron = new SimpleInventory(27);
        taigaPartialIron.setStack(0, new ItemStack(Items.IRON_INGOT, 3));
        VillageResourceRules.apply(VillageVariant.TAIGA, true, 3, Collections.singletonList(taigaPartialIron), 0);
        require(count(taigaPartialIron, Items.IRON_INGOT) == 4,
                "Taiga partial chest iron was not topped up exactly");

        SimpleInventory diamonds = new SimpleInventory(27);
        diamonds.setStack(0, new ItemStack(Items.DIAMOND, 3));
        VillageResourceRules.apply(VillageVariant.DESERT, true, 3, Collections.singletonList(diamonds), 0);
        require(count(diamonds, Items.IRON_INGOT) == 1, "Four-iron and three-diamond route failed with vanilla golem drops");

        SimpleInventory highObsidian = new SimpleInventory(27);
        highObsidian.setStack(0, new ItemStack(Items.OBSIDIAN, 10));
        VillageResourceRules.apply(VillageVariant.SAVANNA, true, 4, Collections.singletonList(highObsidian), 0);
        require(count(highObsidian, Items.IRON_INGOT) == 0, "High obsidian incorrectly guaranteed chest iron");

        for (VillageVariant variant : VillageVariant.values()) {
            SimpleInventory smith = new SimpleInventory(27);
            SimpleInventory house = new SimpleInventory(27);
            house.setStack(0, new ItemStack(Items.BREAD));
            VillageResourceRules.apply(variant, false, 3, java.util.Arrays.asList(smith, house), 0);
            require(count(smith, Items.IRON_INGOT) == 4,
                    "Wrong smith iron plan for " + variant);
            require(count(smith, Items.OBSIDIAN) == (variant == VillageVariant.TAIGA ? 10 : 0),
                    "Wrong smith obsidian plan for " + variant);
            require(count(smith, Items.BREAD) == 0 && count(house, Items.BREAD) == 1,
                    "Existing house food was not counted for " + variant);
        }

        CompoundTag saved = new CompoundTag();
        saved.putString("VillageVariant", VillageVariant.TAIGA.name());
        saved.putLong("PlanSeed", 123456789L);
        saved.putBoolean("HasHay", true);
        saved.putInt("GolemIron", 3);
        ListTag entries = new ListTag();
        CompoundTag entry = new CompoundTag();
        entry.putLong("Pos", new BlockPos(32, 70, -16).asLong());
        entry.putString("Table", "minecraft:chests/village/village_toolsmith");
        entries.add(entry);
        saved.put("Chests", entries);
        McsreasymodeVillageLootPiece restored = new McsreasymodeVillageLootPiece(saved);
        CompoundTag roundTrip = new CompoundTag();
        restored.toNbt(roundTrip);
        require(saved.equals(roundTrip), "Village loot manifest changed after reload");
        require(restored.hasSmithChest(), "Toolsmith chest was not recognized as adjustment destination");
        require(!restored.intersectsChunk(new ChunkPos(2, -1), 12), "Loot manifest reshaped terrain");
        ListTag inventoryPlan = new ListTag();
        ListTag plannedChest = new ListTag();
        CompoundTag plannedStack = new ItemStack(Items.OBSIDIAN, 10).toTag(new CompoundTag());
        plannedStack.putInt("Slot", 5);
        plannedChest.add(plannedStack);
        inventoryPlan.add(plannedChest);
        saved.put("PlannedInventories", inventoryPlan);
        CompoundTag plannedRoundTrip = new CompoundTag();
        new McsreasymodeVillageLootPiece(saved).toNbt(plannedRoundTrip);
        require(saved.equals(plannedRoundTrip), "Rolled chest inventory changed after reload");
        System.out.println("Village resource checks passed (five templates, taiga mining route, iron routes, food, high obsidian).");
    }

    private static int count(SimpleInventory inventory, Item item) {
        int total = 0;
        for (int slot = 0; slot < inventory.size(); slot++) {
            ItemStack stack = inventory.getStack(slot);
            if (stack.getItem() == item) total += stack.getCount();
        }
        return total;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
