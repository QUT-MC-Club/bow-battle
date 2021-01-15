package io.github.gibatron.bowbattle.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import xyz.nucleoid.plasmid.game.config.PlayerConfig;
import io.github.gibatron.bowbattle.game.map.BowBattleMapConfig;

public class BowBattleConfig {
    public static final Codec<BowBattleConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PlayerConfig.CODEC.fieldOf("players").forGetter(config -> config.playerConfig),
            BowBattleMapConfig.CODEC.fieldOf("map").forGetter(config -> config.mapConfig),
            Codec.INT.fieldOf("time_limit_secs").forGetter(config -> config.timeLimitSecs)
    ).apply(instance, BowBattleConfig::new));

    public final PlayerConfig playerConfig;
    public final BowBattleMapConfig mapConfig;
    public final int timeLimitSecs;

    public BowBattleConfig(PlayerConfig players, BowBattleMapConfig mapConfig, int timeLimitSecs) {
        this.playerConfig = players;
        this.mapConfig = mapConfig;
        this.timeLimitSecs = timeLimitSecs;
    }
}
