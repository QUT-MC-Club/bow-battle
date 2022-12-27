package io.github.gibatron.bowbattle.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gibatron.bowbattle.game.map.BowBattleMapConfig;
import xyz.nucleoid.plasmid.game.common.config.PlayerConfig;

public record BowBattleConfig(PlayerConfig players, BowBattleMapConfig map, int timeLimitSecs) {
    public static final Codec<BowBattleConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PlayerConfig.CODEC.fieldOf("players").forGetter(config -> config.players),
            BowBattleMapConfig.CODEC.fieldOf("map").forGetter(config -> config.map),
            Codec.INT.fieldOf("time_limit_secs").forGetter(config -> config.timeLimitSecs)
    ).apply(instance, BowBattleConfig::new));
}
