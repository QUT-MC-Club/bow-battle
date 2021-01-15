package io.github.gibatron.bowbattle.game;

import io.github.gibatron.bowbattle.BowBattle;
import io.github.gibatron.bowbattle.game.map.BowBattleMap;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import xyz.nucleoid.plasmid.game.GameSpace;

public class BowBattleSpawnLogic {
    private final GameSpace gameSpace;
    private final BowBattleMap map;

    public BowBattleSpawnLogic(GameSpace gameSpace, BowBattleMap map) {
        this.gameSpace = gameSpace;
        this.map = map;
    }

    public void resetPlayer(ServerPlayerEntity player, GameMode gameMode) {
        player.setGameMode(gameMode);
        player.setVelocity(Vec3d.ZERO);

        player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.NIGHT_VISION,
                20 * 60 * 60,
                1,
                true,
                false
        ));

        player.inventory.clear();
        ItemStack bowStack = new ItemStack(Items.BOW);
        bowStack.getOrCreateTag().putBoolean("Unbreakable", true);
        bowStack.addHideFlag(ItemStack.TooltipSection.UNBREAKABLE);
        player.inventory.setStack(0, bowStack);
        player.setExperiencePoints(0);
        player.setExperienceLevel(0);
    }

    public void spawnPlayer(ServerPlayerEntity player) {
        ServerWorld world = this.gameSpace.getWorld();

        Vec3d pos = this.map.spawns.get(player.getRandom().nextInt(this.map.spawns.size())).getCenter();
        player.teleport(world, pos.getX(), pos.getY(), pos.getZ(), 0.0F, 0.0F);
    }
}
