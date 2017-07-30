package us.sodiumlabs.plugins.villajerks;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.asset.AssetManager;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.living.Villager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Plugin(id = VillaJerks.ID, name = "VillaJerks", version = "1.0", description = "makes villagers do stuff" )
public class VillaJerks {
    static final String ID = "villajerks";

    private final Logger logger;

    private final AssetManager assetManager;

    private List<String> firstNames;

    private List<String> lastNames;

    @Inject
    public VillaJerks(final Logger logger, final AssetManager assetManager) {

        this.logger = requireNonNull(logger);
        this.assetManager = requireNonNull(assetManager);
        this.firstNames = ImmutableList.of();
        this.lastNames = ImmutableList.of();
    }

    @Listener
    public void onGamePreInitializationEvent(final GamePreInitializationEvent preInitializationEvent)
    {
        try {
            this.firstNames =   getNamesFromPath("first.txt");
            this.lastNames =    getNamesFromPath("last.txt");

            logger.info("Initialized VillaJerks!");
        } catch (URISyntaxException | IOException e) {
            logger.error("Failed to initialize VillaJerks!", e);
            throw new RuntimeException(e);
        }
    }

    @Listener
    public void onInteractEvent(final InteractEntityEvent event) {
        if(event.getTargetEntity() instanceof Villager) {
            Villager villager = (Villager) event.getTargetEntity();

            final Optional<Value<Text>> name = villager.getValue(Keys.DISPLAY_NAME);
            if(!name.isPresent()) {
                final Text text = buildVillagerName(villager);
                logger.info(String.format(
                    "Added name [%s] to villager with UUID [%s]", text.toPlain(), villager.getUniqueId()));
                villager.offer(Keys.DISPLAY_NAME, text);
            }
        }
    }

    @VisibleForTesting
    Text buildVillagerName(final Villager villager) {
        final UUID uuid = villager.getUniqueId();
        final long firstNameIdPart = uuid.getMostSignificantBits();
        final long lastNameIdPart = uuid.getLeastSignificantBits();
        final String firstName = getName(firstNameIdPart, firstNames);
        final String lastName = getName(lastNameIdPart, lastNames);

        return Text.builder(firstName + " " + lastName).build();
    }

    private String getName(final long part, final List<String> names) {
        final Long pos = Math.floorMod(part, names.size());
        return names.get(pos.intValue());
    }

    private List<String> getNamesFromPath(final String resourceName)
        throws URISyntaxException, IOException
    {
        logger.info("Looking at: " + resourceName);
        final List<String> lines = assetManager.getAsset(this, resourceName)
            .orElseThrow(() -> new RuntimeException("No asset found! Looking at: " + resourceName))
            .readLines();

        return ImmutableList.copyOf(lines.stream()
            .map(String::trim)
            .filter(s->!s.isEmpty())
            .collect(Collectors.toList()));
    }
}
