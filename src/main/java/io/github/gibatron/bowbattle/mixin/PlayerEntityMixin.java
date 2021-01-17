package io.github.gibatron.bowbattle.mixin;

import io.github.gibatron.bowbattle.BowBattle;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.plasmid.game.ManagedGameSpace;
import xyz.nucleoid.plasmid.game.rule.RuleResult;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends Entity {

    @Shadow @Final public PlayerInventory inventory;

    public PlayerEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "addExperienceLevels", at = @At("HEAD"))
    private void addExperienceLevels(int levels, CallbackInfo ci) {
        ManagedGameSpace gameSpace = ManagedGameSpace.forWorld(world);
        if (gameSpace != null && gameSpace.testRule(BowBattle.XP_RESTOCKS_ARROWS) == RuleResult.ALLOW)
            this.inventory.insertStack(17, new ItemStack(Items.ARROW, 1));
    }
}
