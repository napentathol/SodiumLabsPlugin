package us.sodiumlabs.plugins.villajerks;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.spongepowered.api.asset.AssetManager;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.living.Villager;
import org.spongepowered.api.entity.living.golem.Golem;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class NameService {

    private final Logger logger;

    private final AssetManager assetManager;

    private final Object plugin;

    private List<String> firstNames;

    private List<String> lastNames;

    private List<String> golemFirstNames;

    private List<String> golemLastNames;

    NameService(Object plugin, Logger logger, AssetManager assetManager) {
        this.logger = requireNonNull(logger);
        this.assetManager = requireNonNull(assetManager);
        this.plugin = requireNonNull(plugin);
    }

    void initialize() {
        try {
            this.firstNames = getNamesFromPath("first.txt");
            this.lastNames = getNamesFromPath("last.txt");
            this.golemFirstNames = getNamesFromPath("golem.first.txt");
            this.golemLastNames = getNamesFromPath("golem.last.txt");
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    void nameVillager(final Villager villager) {
        final Optional<Value<Text>> name = villager.getValue(Keys.DISPLAY_NAME);
        if(!name.isPresent()) {
            final Text text = buildVillagerName(villager);
            logger.info(String.format(
                "Added name [%s] to villager with UUID [%s]", text.toPlain(), villager.getUniqueId()));
            villager.offer(Keys.DISPLAY_NAME, text);
        }
    }

    void nameGolem(final Golem golem) {
        final Optional<Value<Text>> name = golem.getValue(Keys.DISPLAY_NAME);
        if(!name.isPresent()) {
            final Text text = buildGolemName(golem);
            logger.info(String.format(
                "Added name [%s] to golem with UUID [%s]", text.toPlain(), golem.getUniqueId()));
            golem.offer(Keys.DISPLAY_NAME, text);
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

    @VisibleForTesting
    Text buildGolemName(final Golem golem) {
        final UUID uuid = golem.getUniqueId();
        final long firstNameIdPart = uuid.getMostSignificantBits();
        final long lastNameIdPart = uuid.getLeastSignificantBits();
        final String firstName = getName(firstNameIdPart, golemFirstNames);
        final String lastName = getName(lastNameIdPart, golemLastNames);

        return Text.builder(firstName + lastName).build();
    }

    private String getName(final long part, final List<String> names) {
        final Long pos = Math.floorMod(part, names.size());
        return names.get(pos.intValue());
    }

    private List<String> getNamesFromPath(final String resourceName)
        throws URISyntaxException, IOException
    {
        logger.info("Looking at: " + resourceName);
        final List<String> lines = assetManager.getAsset(plugin, resourceName)
            .orElseThrow(() -> new RuntimeException("No asset found! Looking at: " + resourceName))
            .readLines();

        return ImmutableList.copyOf(lines.stream()
            .map(String::trim)
            .filter(s->!s.isEmpty())
            .collect(Collectors.toList()));
    }
}
