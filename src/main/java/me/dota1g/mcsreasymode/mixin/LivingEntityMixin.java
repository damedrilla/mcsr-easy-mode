package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.entity.passive.IronGolemEntity;
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
        lootTable.generateLoot(context, stack -> {
            if (this.mcsreasymode$trackingBlazeRodDrop && stack.getItem() == Items.BLAZE_ROD && !stack.isEmpty()) {
                this.mcsreasymode$droppedBlazeRod = true;
            }
            dropper.accept(stack);
        });
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
            Mcsreasymode.debug("Blaze rod standardized: vanilla dropped rod after " + mcsreasymode$blazeRodlessKills + " dry blaze kills.");
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
