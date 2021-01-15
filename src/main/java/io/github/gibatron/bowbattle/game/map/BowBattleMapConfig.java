package io.github.gibatron.bowbattle.game.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

public class BowBattleMapConfig {
    public static final Codec<BowBattleMapConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(config -> config.id)
    ).apply(instance, BowBattleMapConfig::new));

    public final Identifier id;

    public BowBattleMapConfig(Identifier id) {
        this.id = id;
    }
}
