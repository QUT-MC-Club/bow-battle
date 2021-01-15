package io.github.gibatron.bowbattle.game;

import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import xyz.nucleoid.plasmid.game.*;
import xyz.nucleoid.plasmid.game.event.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import io.github.gibatron.bowbattle.game.map.BowBattleMap;
import io.github.gibatron.bowbattle.game.map.BowBattleMapGenerator;
import xyz.nucleoid.fantasy.BubbleWorldConfig;

public class BowBattleWaiting {
    private final GameSpace gameSpace;
    private final BowBattleMap map;
    private final BowBattleConfig config;
    private final BowBattleSpawnLogic spawnLogic;

    private BowBattleWaiting(GameSpace gameSpace, BowBattleMap map, BowBattleConfig config) {
        this.gameSpace = gameSpace;
        this.map = map;
        this.config = config;
        this.spawnLogic = new BowBattleSpawnLogic(gameSpace, map);
    }

    public static GameOpenProcedure open(GameOpenContext<BowBattleConfig> context) {
        BowBattleConfig config = context.getConfig();
        BowBattleMapGenerator generator = new BowBattleMapGenerator(config.mapConfig);
        BowBattleMap map = generator.build();

        BubbleWorldConfig worldConfig = new BubbleWorldConfig()
                .setGenerator(map.asGenerator(context.getServer()))
                .setDefaultGameMode(GameMode.SPECTATOR);

        return context.createOpenProcedure(worldConfig, game -> {
            BowBattleWaiting waiting = new BowBattleWaiting(game.getSpace(), map, context.getConfig());

            GameWaitingLobby.applyTo(game, config.playerConfig);

            game.on(RequestStartListener.EVENT, waiting::requestStart);
            game.on(PlayerAddListener.EVENT, waiting::addPlayer);
        });
    }

    private StartResult requestStart() {
        BowBattleActive.open(this.gameSpace, this.map, this.config);
        return StartResult.OK;
    }

    private void addPlayer(ServerPlayerEntity player) {
        this.spawnPlayer(player);
    }

    private void spawnPlayer(ServerPlayerEntity player) {
        this.spawnLogic.resetPlayer(player, GameMode.ADVENTURE);
        this.spawnLogic.spawnPlayer(player);
    }
}
