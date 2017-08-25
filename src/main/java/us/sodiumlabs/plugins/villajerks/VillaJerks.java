package us.sodiumlabs.plugins.villajerks;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.asset.AssetManager;
import org.spongepowered.api.entity.living.Villager;
import org.spongepowered.api.entity.living.golem.Golem;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import us.sodiumlabs.plugins.AbstractPlugin;

import java.util.Random;

import static java.util.Objects.requireNonNull;

@Plugin(id = VillaJerks.ID, name = "VillaJerks", version = "1.0", description = "makes villagers do stuff" )
public class VillaJerks extends AbstractPlugin {
    static final String ID = "villajerks";

    private final Logger logger;

    private final TradeService tradeService;

    private final DeathService deathService;

    private final NameService nameService;

    @Inject
    public VillaJerks(final Logger logger, final Game game, final Random random, final AssetManager assetManager) {

        this.logger = requireNonNull(logger);

        this.tradeService = new TradeService(logger, game, random);
        this.deathService = new DeathService(logger, game);
        this.nameService = new NameService(this, logger, assetManager);
    }

    @Listener
    public void onGamePreInitializationEvent(final GamePreInitializationEvent preInitializationEvent)
    {
        try {
            tradeService.registerNitwitTrades();
            nameService.initialize();
            logger.info("Initialized VillaJerks!");
        } catch (Exception e) {
            logger.error("Failed to initialize VillaJerks!", e);
        }
    }

    @Listener
    public void onBirth(final SpawnEntityEvent event) {
        if(event instanceof SpawnEntityEvent.ChunkLoad) return;

        event.getEntities().stream()
            .filter(e -> e instanceof Villager)
            .map(e -> (Villager) e)
            .forEach(nameService::nameVillager);

        event.getEntities().stream()
            .filter(e -> e instanceof Golem)
            .map(e -> (Golem) e)
            .forEach(nameService::nameGolem);
    }

    @Listener
    public void onInteractEvent(final InteractEntityEvent event) {
        if(event.getTargetEntity() instanceof Villager) {
            final Villager villager = (Villager) event.getTargetEntity();

            nameService.nameVillager(villager);
            tradeService.addTrades(villager);
            tradeService.resetTrades(villager);
        } else if (event.getTargetEntity() instanceof Golem) {
            nameService.nameGolem((Golem) event.getTargetEntity());
        }
    }

    @Listener
    public void onDeathEvent(final DestructEntityEvent.Death event) {
        if(event.getTargetEntity() instanceof Villager) {
            final Villager villager = (Villager) event.getTargetEntity();
            deathService.onVillagerDeath(villager, event.getCause(), this);
        }
    }

    @Override
    protected String getID() {
        return ID;
    }
}
