package io.github.gibatron.bowbattle.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {

    @Shadow @Final public PlayerInventory inventory;

    @Inject(method = "addExperienceLevels", at = @At("HEAD"))
    private void addExperienceLevels(int levels, CallbackInfo ci) {
        this.inventory.insertStack(17, new ItemStack(Items.ARROW, 1));
    }
}
