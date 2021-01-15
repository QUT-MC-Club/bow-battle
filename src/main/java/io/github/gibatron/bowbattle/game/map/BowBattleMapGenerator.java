package io.github.gibatron.bowbattle.game.map;

import io.github.gibatron.bowbattle.BowBattle;
import net.minecraft.text.LiteralText;
import xyz.nucleoid.plasmid.game.GameOpenException;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.map.template.MapTemplateSerializer;
import xyz.nucleoid.plasmid.util.BlockBounds;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

public class BowBattleMapGenerator {

    private final BowBattleMapConfig config;

    public BowBattleMapGenerator(BowBattleMapConfig config) {
        this.config = config;
    }

    public BowBattleMap build() throws GameOpenException {
        try {
            MapTemplate template = MapTemplateSerializer.INSTANCE.loadFromResource(this.config.id);
            BowBattleMap map = new BowBattleMap(template, this.config);

            template.getMetadata().getRegions("spawn").forEach(x -> map.spawns.add(x.getBounds()));

            return map;
        } catch (IOException e) {
            throw new GameOpenException(new LiteralText("Failed to load map"));
        }
    }
}
