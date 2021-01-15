package io.github.gibatron.bowbattle.game.map;

import net.minecraft.server.MinecraftServer;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.map.template.TemplateChunkGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import xyz.nucleoid.plasmid.util.BlockBounds;

import java.util.ArrayList;
import java.util.List;

public class BowBattleMap {
    private final MapTemplate template;
    private final BowBattleMapConfig config;
    public List<BlockBounds> spawns = new ArrayList<>();

    public BowBattleMap(MapTemplate template, BowBattleMapConfig config) {
        this.template = template;
        this.config = config;
    }

    public ChunkGenerator asGenerator(MinecraftServer server) {
        return new TemplateChunkGenerator(server, this.template);
    }
}
