package us.sodiumlabs.plugins.villajerks;

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.entity.living.Villager;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import us.sodiumlabs.plugins.CauseCreator;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import static java.util.Objects.requireNonNull;

public class DeathService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss O");

    private final Logger logger;

    private final Game game;

    public DeathService(Logger logger, Game game) {
        this.logger = requireNonNull(logger);
        this.game = requireNonNull(game);
    }

    void onVillagerDeath(final Villager villager, final Cause cause, final CauseCreator creator) {
        villager.getValue(Keys.DISPLAY_NAME).ifPresent(textValue -> {
            // Log name and Death cause.
            logger.info("#### " + textValue.get().toPlain());
            logger.info(cause.toString());

            // Message channel.
            final Text deathMessage = Text.builder()
                .append(Text.builder("[").color(TextColors.BLACK).build())
                .append(Text.builder("REAPER").color(TextColors.RED).build())
                .append(Text.builder("]").color(TextColors.BLACK).build())
                .append(Text.of(" "))
                .append(textValue.get())
                .append(Text.of(" has died! May their eternal soul rest in peace!"))
                .build();
            game.getServer().getBroadcastChannel().send(deathMessage);

            // Spawn a sign.
            final String fullName = textValue.get().toPlain();
            final int firstSpace = fullName.indexOf(' ');
            final String firstName = fullName.substring(0, firstSpace);
            final String lastName = fullName.substring(firstSpace+1, fullName.length());

            final Location<World> deathBlock = findDeathBlock(villager.getLocation());

            final Cause signCause = creator.createNamedCause("SPAWN SIGN");
            deathBlock.setBlockType(BlockTypes.STANDING_SIGN, signCause);
            deathBlock.getTileEntity().ifPresent(t -> {
                t.get(SignData.class).ifPresent(s -> {
                    final OffsetDateTime offsetDateTime = OffsetDateTime.now();

                    final ImmutableList<Text> signText = ImmutableList.of(
                        Text.of(firstName),
                        Text.of(lastName),
                        Text.of(offsetDateTime.toLocalDate().toString()),
                        Text.of(FORMATTER.format(offsetDateTime)));

                    final SignData signData = s.setElements(signText);
                    t.offer(signData);
                    logger.info("#### LEFT CLUES SUCCESSFULLY");
                });
            });
        });
    }

    private Location<World> findDeathBlock(Location<World> location) {
        if(location.getBlockType() != BlockTypes.AIR) return findDeathBlock(location.add(0,1,0));
        final Location<World> below = location.add(0,-1,0);
        if(below.getBlockType() == BlockTypes.AIR) return findDeathBlock(below);
        return location;
    }
}
