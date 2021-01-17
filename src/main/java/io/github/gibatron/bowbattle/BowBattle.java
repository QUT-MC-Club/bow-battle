package io.github.gibatron.bowbattle;

import net.fabricmc.api.ModInitializer;
import xyz.nucleoid.plasmid.game.GameType;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.github.gibatron.bowbattle.game.BowBattleConfig;
import io.github.gibatron.bowbattle.game.BowBattleWaiting;
import xyz.nucleoid.plasmid.game.rule.GameRule;

public class BowBattle implements ModInitializer {

    public static final String ID = "bowbattle";
    public static final Logger LOGGER = LogManager.getLogger(ID);

    public static final GameRule BOW_GRAPPLES_SOUL_LANTERNS = new GameRule();
    public static final GameRule BOW_SLOW_MO = new GameRule();
    public static final GameRule XP_RESTOCKS_ARROWS = new GameRule();

    public static final GameType<BowBattleConfig> TYPE = GameType.register(
            new Identifier(ID, "bowbattle"),
            BowBattleWaiting::open,
            BowBattleConfig.CODEC
    );

    @Override
    public void onInitialize() {}
}
