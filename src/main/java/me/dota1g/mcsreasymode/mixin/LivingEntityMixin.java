package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.RabbitEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Unique
    private static int mcsreasymode$blazeRodlessKills;
    @Unique
    private boolean mcsreasymode$trackingBlazeRodDrop;
    @Unique
    private boolean mcsreasymode$droppedBlazeRod;

    @Inject(method = "dropLoot", at = @At("HEAD"), cancellable = true)
    private void mcsreasymode$rankedLootHead(DamageSource source, boolean causedByPlayer, CallbackInfo ci) {
        Object self = this;
        if (Mcsreasymode.isRankedIronGolemDropsEnabled() && self instanceof IronGolemEntity) {
            ((LivingEntity) self).dropStack(new ItemStack(Items.IRON_INGOT, 4));
            Mcsreasymode.debug("Iron golem loot standardized: dropped exactly 4 iron ingots.");
            ci.cancel();
            return;
        }

        if (Mcsreasymode.isRankedBlazeRodsEnabled() && causedByPlayer && self instanceof BlazeEntity) {
            this.mcsreasymode$trackingBlazeRodDrop = true;
            this.mcsreasymode$droppedBlazeRod = false;
        }
    }

    @Redirect(method = "dropLoot", at = @At(value = "INVOKE", target = "Lnet/minecraft/loot/LootTable;generateLoot(Lnet/minecraft/loot/context/LootContext;Ljava/util/function/Consumer;)V"))
    private void mcsreasymode$trackGeneratedLoot(LootTable lootTable, LootContext context, Consumer<ItemStack> dropper) {
        // Capture generated drops so they can be reordered for animal food priority without affecting other mob logic
        java.util.List<ItemStack> generated = new java.util.ArrayList<>();
        lootTable.generateLoot(context, generated::add);

        // Preserve existing blaze rod tracking logic
        if (this.mcsreasymode$trackingBlazeRodDrop) {
            for (ItemStack stack : generated) {
                if (stack != null && !stack.isEmpty() && stack.getItem() == Items.BLAZE_ROD) {
                    this.mcsreasymode$droppedBlazeRod = true;
                    break;
                }
            }
        }

        // Only reorder for common passive animals so blaze/blaze-rod behavior is untouched
        Object self = this;
        boolean reordered = false;
        if (self instanceof CowEntity || self instanceof SheepEntity || self instanceof ChickenEntity || self instanceof PigEntity || self instanceof RabbitEntity) {
            java.util.List<ItemStack> food = new java.util.ArrayList<>();
            java.util.List<ItemStack> others = new java.util.ArrayList<>();
            for (ItemStack stack : generated) {
                if (stack != null && !stack.isEmpty() && mcsreasymode$isAnimalFood(stack.getItem())) {
                    food.add(stack);
                } else {
                    others.add(stack);
                }
            }
            if (!food.isEmpty()) {
                generated.clear();
                generated.addAll(food);
                generated.addAll(others);
                reordered = true;
            }
        }

        // Emit drops in (possibly reordered) sequence
        for (ItemStack stack : generated) {
            dropper.accept(stack);
        }

        if (reordered) {
            Mcsreasymode.debug("Reordered entity drops to prioritize food for looting multiplier on animal drop.");
        }
    }

    @Unique
    private static boolean mcsreasymode$isAnimalFood(Item item) {
        return item == Items.BEEF || item == Items.COOKED_BEEF
                || item == Items.CHICKEN || item == Items.COOKED_CHICKEN
                || item == Items.PORKCHOP || item == Items.COOKED_PORKCHOP
                || item == Items.MUTTON || item == Items.COOKED_MUTTON
                || item == Items.RABBIT || item == Items.COOKED_RABBIT;
    }

    @Inject(method = "dropLoot", at = @At("TAIL"))
    private void mcsreasymode$rankedLootTail(DamageSource source, boolean causedByPlayer, CallbackInfo ci) {
        Object self = this;
        if (!this.mcsreasymode$trackingBlazeRodDrop) {
            return;
        }

        this.mcsreasymode$trackingBlazeRodDrop = false;
        if (!Mcsreasymode.isRankedBlazeRodsEnabled() || !causedByPlayer || !(self instanceof BlazeEntity)) {
            return;
        }

        LivingEntity entity = (LivingEntity) self;
        if (this.mcsreasymode$droppedBlazeRod) {
            Mcsreasymode.debug("Blaze rod pity reset: vanilla dropped rod after " + mcsreasymode$blazeRodlessKills + " dry blaze kills.");
            mcsreasymode$blazeRodlessKills = 0;
        } else if (mcsreasymode$blazeRodlessKills >= 2) {
            entity.dropStack(new ItemStack(Items.BLAZE_ROD));
            Mcsreasymode.debug("Blaze rod pity proc: added 1 rod after " + mcsreasymode$blazeRodlessKills + " dry blaze kills.");
            mcsreasymode$blazeRodlessKills = 0;
        } else {
            mcsreasymode$blazeRodlessKills++;
            Mcsreasymode.debug("Blaze: vanilla dropped no rod, dry blaze kills now " + mcsreasymode$blazeRodlessKills + ".");
        }
    }
}
