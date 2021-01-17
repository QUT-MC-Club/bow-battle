package io.github.gibatron.bowbattle.game;

import io.github.gibatron.bowbattle.BowBattle;
import io.github.gibatron.bowbattle.game.map.BowBattleMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import org.apache.commons.lang3.RandomStringUtils;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.event.*;
import xyz.nucleoid.plasmid.game.player.JoinResult;
import xyz.nucleoid.plasmid.game.player.PlayerSet;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;
import xyz.nucleoid.plasmid.util.PlayerRef;
import xyz.nucleoid.plasmid.widget.GlobalWidgets;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class BowBattleActive {
    private final BowBattleConfig config;

    public final GameSpace gameSpace;
    private final BowBattleMap gameMap;

    private final Object2ObjectMap<PlayerRef, BowBattlePlayer> participants;
    private final BowBattleSpawnLogic spawnLogic;
    private final BowBattleStageManager stageManager;
    private final boolean ignoreWinState;
    private final BowBattleTimerBar timerBar;
    private final Team scoreboardTeam;

    private BowBattleActive(GameSpace gameSpace, BowBattleMap map, GlobalWidgets widgets, BowBattleConfig config, Set<PlayerRef> participants) {
        this.gameSpace = gameSpace;
        this.config = config;
        this.gameMap = map;
        this.spawnLogic = new BowBattleSpawnLogic(gameSpace, map);
        this.participants = new Object2ObjectOpenHashMap<>();

        Scoreboard scoreboard = gameSpace.getServer().getScoreboard();
        scoreboardTeam = scoreboard.addTeam(RandomStringUtils.randomAlphanumeric(16));
        scoreboardTeam.setNameTagVisibilityRule(AbstractTeam.VisibilityRule.NEVER);
        scoreboardTeam.setCollisionRule(AbstractTeam.CollisionRule.NEVER);

        for (PlayerRef player : participants) {
            scoreboard.addPlayerToTeam(player.getEntity(gameSpace.getWorld()).getName().asString(), scoreboardTeam);
            this.participants.put(player, new BowBattlePlayer());
        }

        this.stageManager = new BowBattleStageManager();
        this.ignoreWinState = this.participants.size() <= 1;
        this.timerBar = new BowBattleTimerBar(widgets);
    }

    public static void open(GameSpace gameSpace, BowBattleMap map, BowBattleConfig config) {
        gameSpace.openGame(game -> {
            Set<PlayerRef> participants = gameSpace.getPlayers().stream()
                    .map(PlayerRef::of)
                    .collect(Collectors.toSet());
            GlobalWidgets widgets = new GlobalWidgets(game);
            BowBattleActive active = new BowBattleActive(gameSpace, map, widgets, config, participants);

            game.setRule(GameRule.CRAFTING, RuleResult.DENY);
            game.setRule(GameRule.PORTALS, RuleResult.DENY);
            game.setRule(GameRule.PVP, RuleResult.ALLOW);
            game.setRule(GameRule.HUNGER, RuleResult.DENY);
            game.setRule(GameRule.FALL_DAMAGE, RuleResult.DENY);
            game.setRule(GameRule.INTERACTION, RuleResult.DENY);
            game.setRule(GameRule.BLOCK_DROPS, RuleResult.DENY);
            game.setRule(GameRule.THROW_ITEMS, RuleResult.DENY);
            game.setRule(GameRule.UNSTABLE_TNT, RuleResult.DENY);
            game.setRule(BowBattle.BOW_SLOW_MO, RuleResult.ALLOW);
            game.setRule(BowBattle.BOW_GRAPPLES_SOUL_LANTERNS, RuleResult.ALLOW);
            game.setRule(BowBattle.XP_RESTOCKS_ARROWS, RuleResult.ALLOW);

            game.on(GameOpenListener.EVENT, active::onOpen);
            game.on(GameCloseListener.EVENT, active::onClose);

            game.on(OfferPlayerListener.EVENT, player -> JoinResult.ok());
            game.on(PlayerAddListener.EVENT, active::addPlayer);
            game.on(PlayerRemoveListener.EVENT, active::removePlayer);

            game.on(GameTickListener.EVENT, active::tick);

            game.on(PlayerDamageListener.EVENT, active::onPlayerDamage);
            game.on(PlayerDeathListener.EVENT, active::onPlayerDeath);
            game.on(PlayerFireArrowListener.EVENT, active::onPlayerFire);
        });
    }

    private void onOpen() {
        ServerWorld world = this.gameSpace.getWorld();
        for (PlayerRef ref : this.participants.keySet()) {
            ref.ifOnline(world, this::spawnParticipant);
        }
        this.stageManager.onOpen(world.getTime(), this.config);
    }

    private void onClose() {
        gameSpace.getServer().getScoreboard().removeTeam(scoreboardTeam);
    }

    private void addPlayer(ServerPlayerEntity player) {
        if (!this.participants.containsKey(PlayerRef.of(player))) {
            this.spawnSpectator(player);
        }
    }

    private void removePlayer(ServerPlayerEntity player) {
        this.participants.remove(PlayerRef.of(player));
    }

    private ActionResult onPlayerDamage(ServerPlayerEntity player, DamageSource source, float amount) {
        if (source.isProjectile() && source.getAttacker() != player) {
            if (source.getAttacker() != null) {
                ((ServerPlayerEntity)source.getAttacker()).playSound(SoundEvents.ENTITY_ARROW_HIT_PLAYER, SoundCategory.PLAYERS, 1f, 1f);
                gameSpace.getPlayers().sendMessage(new LiteralText(String.format("☠ - %s was shot by %s", player.getDisplayName().getString(), source.getAttacker().getDisplayName().getString())).formatted(Formatting.GRAY));
            }
            participants.get(PlayerRef.of(player)).timesHit += 1;
            //Thanks Potatoboy9999 ;)
            for (int i = 0; i < 75; i++) {
                gameSpace.getWorld().spawnParticles(
                        ParticleTypes.FIREWORK,
                        player.getPos().getX(),
                        player.getPos().getY() + 1.0f,
                        player.getPos().getZ(),
                        1,
                        ((player.getRandom().nextFloat() * 2.0f) - 1.0f) * 0.35f,
                        ((player.getRandom().nextFloat() * 2.0f) - 1.0f) * 0.35f,
                        ((player.getRandom().nextFloat() * 2.0f) - 1.0f) * 0.35f,
                        0.1);
            }
            this.spawnParticipant(player);
        }
        return ActionResult.FAIL;
    }

    private ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        this.spawnParticipant(player);
        return ActionResult.FAIL;
    }

    private ActionResult onPlayerFire(ServerPlayerEntity player, ItemStack bowStack, ArrowItem arrowItem, int remaining, PersistentProjectileEntity projectile) {
        projectile.pickupType = PersistentProjectileEntity.PickupPermission.DISALLOWED;
        Vec3d velocity = projectile.getVelocity();
        projectile.setVelocity(velocity.x, velocity.y, velocity.z, 3F, 1.0F);
        player.experienceLevel -= 1;
        return ActionResult.CONSUME;
    }

    private void spawnParticipant(ServerPlayerEntity player) {
        this.spawnLogic.resetPlayer(player, GameMode.ADVENTURE);
        this.spawnLogic.spawnPlayer(player);
    }

    private void spawnSpectator(ServerPlayerEntity player) {
        this.spawnLogic.resetPlayer(player, GameMode.SPECTATOR);
        this.spawnLogic.spawnPlayer(player);
    }

    private void tick() {
        ServerWorld world = this.gameSpace.getWorld();
        long time = world.getTime();

        BowBattleStageManager.IdleTickResult result = this.stageManager.tick(time, gameSpace);

        switch (result) {
            case CONTINUE_TICK:
                break;
            case TICK_FINISHED:
                return;
            case GAME_FINISHED:
                this.broadcastWin(this.checkWinResult());
                return;
            case GAME_CLOSED:
                this.gameSpace.close();
                return;
        }

        this.timerBar.update(this.stageManager.finishTime - time, this.config.timeLimitSecs * 20);

        PlayerSet players = this.gameSpace.getPlayers();
        for (ServerPlayerEntity player : players) {
            if (!player.isSpectator()) {
                boolean usingBow = player.getActiveItem().getItem() == Items.BOW;
                player.sendMessage(new LiteralText(String.format("Times hit: %s", participants.get(PlayerRef.of(player)).timesHit)).formatted(Formatting.WHITE, Formatting.BOLD), true);
                if (player.experienceLevel < 5 && !usingBow) {
                    if (player.age % 4 == 0)
                        player.addExperience(1);
                }
                //player.setNoGravity(usingBow);
                if (usingBow) {
                    if (!player.hasStatusEffect(StatusEffects.LEVITATION))
                        player.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 10000, -1, true, false));
                    player.setVelocity(0, 0, 0);
                    player.velocityModified = true;
                } else {
                    if (player.hasStatusEffect(StatusEffects.LEVITATION))
                        player.removeStatusEffect(StatusEffects.LEVITATION);
                }
            }
        }
    }

    private void broadcastWin(WinResult result) {
        ServerPlayerEntity winningPlayer = result.getWinningPlayer();

        Text message;
        if (winningPlayer != null) {
            message = new LiteralText("★ - ").append(winningPlayer.getDisplayName().shallowCopy().append(" has won the game by only being hit " + participants.get(PlayerRef.of(winningPlayer)).timesHit + " times!").formatted(Formatting.GOLD));
        } else {
            message = new LiteralText("The game ended, but nobody won!").formatted(Formatting.GOLD);
        }

        PlayerSet players = this.gameSpace.getPlayers();
        players.sendMessage(message);
        players.sendSound(SoundEvents.ENTITY_VILLAGER_YES);
    }

    private WinResult checkWinResult() {
        // for testing purposes: don't end the game if we only ever had one participant
        if (this.ignoreWinState) {
            return WinResult.no();
        }

        ServerWorld world = this.gameSpace.getWorld();
        ServerPlayerEntity winningPlayer = null;

        PlayerRef best = null;
        for (Map.Entry<PlayerRef, BowBattlePlayer> entry : participants.entrySet())
        {
            if (best == null)
                best = entry.getKey();
            else if (participants.get(best).timesHit > entry.getValue().timesHit)
                best = entry.getKey();
        }
        if (best != null)
            return WinResult.win(best.getEntity(gameSpace.getWorld()));
        return WinResult.no();
    }

    static class WinResult {
        final ServerPlayerEntity winningPlayer;
        final boolean win;

        private WinResult(ServerPlayerEntity winningPlayer, boolean win) {
            this.winningPlayer = winningPlayer;
            this.win = win;
        }

        static WinResult no() {
            return new WinResult(null, false);
        }

        static WinResult win(ServerPlayerEntity player) {
            return new WinResult(player, true);
        }

        public boolean isWin() {
            return this.win;
        }

        public ServerPlayerEntity getWinningPlayer() {
            return this.winningPlayer;
        }
    }
}
