package io.github.gibatron.bowbattle.mixin;

import io.github.gibatron.bowbattle.BowBattle;
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
import xyz.nucleoid.plasmid.game.ManagedGameSpace;
import xyz.nucleoid.plasmid.game.rule.RuleResult;

@Mixin(BowItem.class)
public abstract class BowItemMixin {
    @Inject(method = "use", at = @At("HEAD"))
    private void use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        ManagedGameSpace gameSpace = ManagedGameSpace.forWorld(world);
        if (gameSpace != null && gameSpace.testRule(BowBattle.BOW_SLOW_MO) == RuleResult.ALLOW) {
            user.setVelocity(0, 0, 0);
            user.velocityModified = true;
        }
    }
}
