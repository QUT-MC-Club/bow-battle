package io.github.gibatron.bowbattle.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BowItem.class)
public abstract class BowItemMixin {
    @Inject(method = "getPullProgress", at = @At("RETURN"), cancellable = true)
    private static void getPullProgress(int useTicks, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(1.0F);
    }
    @Inject(method = "use", at = @At("HEAD"))
    private void use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        user.setVelocity(0, 0, 0);
        user.velocityModified = true;
    }
}
