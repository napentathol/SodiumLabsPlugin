package us.sodiumlabs.plugins.villajerks;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.asset.AssetManager;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.data.type.*;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.living.Villager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.merchant.TradeOffer;
import org.spongepowered.api.item.merchant.TradeOfferListMutator;
import org.spongepowered.api.item.merchant.VillagerRegistry;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import us.sodiumlabs.plugins.AbstractPlugin;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Plugin(id = VillaJerks.ID, name = "VillaJerks", version = "1.0", description = "makes villagers do stuff" )
public class VillaJerks extends AbstractPlugin {
    static final String ID = "villajerks";

    static final String NITWIT_ID = "minecraft:nitwit";

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss O");

    private final Logger logger;

    private final AssetManager assetManager;

    private final Game game;

    private final Random random;

    private Career nitwit;

    private List<String> firstNames;

    private List<String> lastNames;

    @Inject
    public VillaJerks(final Logger logger, final Game game, final Random random, final AssetManager assetManager) {

        this.logger = requireNonNull(logger);
        this.game = requireNonNull(game);
        this.random = requireNonNull(random);
        this.assetManager = requireNonNull(assetManager);
        this.firstNames = ImmutableList.of();
        this.lastNames = ImmutableList.of();
    }

    @Listener
    public void onGamePreInitializationEvent(final GamePreInitializationEvent preInitializationEvent)
    {
        registerNitwitTrades();
        try {
            this.firstNames =   getNamesFromPath("first.txt");
            this.lastNames =    getNamesFromPath("last.txt");

            logger.info("Initialized VillaJerks!");
        } catch (URISyntaxException | IOException e) {
            logger.error("Failed to initialize VillaJerks!", e);
            throw new RuntimeException(e);
        }
    }

    private void registerNitwitTrades() {
        nitwit = game.getRegistry().getType(Career.class, NITWIT_ID)
            .orElseThrow(() -> new RuntimeException("No nitwits."));
        logger.info(nitwit.toString());

        final VillagerRegistry registry = game.getRegistry().getVillagerRegistry();
        for (int i = 0; i < MUTATOR_MAP.size(); i++) {
            registry.setMutators(nitwit, i + 1, MUTATOR_MAP.get(i));
        }
    }

    @Listener
    public void onBirth(final SpawnEntityEvent event) {
        if(event instanceof SpawnEntityEvent.ChunkLoad) return;

        event.getEntities().stream()
            .filter(e -> e instanceof Villager)
            .map(e -> (Villager) e)
            .forEach(this::nameVillager);
    }

    @Listener
    public void onInteractEvent(final InteractEntityEvent event) {
        if(event.getTargetEntity() instanceof Villager) {
            final Villager villager = (Villager) event.getTargetEntity();

            nameVillager(villager);
            addTrades(villager);
            resetTrades(villager);
        }
    }

    @Listener
    public void onDeathEvent(final DestructEntityEvent.Death event) {
        if(event.getTargetEntity() instanceof Villager) {
            final Villager villager = (Villager) event.getTargetEntity();

            villager.getValue(Keys.DISPLAY_NAME).ifPresent(textValue -> {
                // Log name and Death cause.
                logger.info("#### " + textValue.get().toPlain());
                logger.info(event.getCause().toString());

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

                final Cause cause = createNamedCause("SPAWN SIGN");
                deathBlock.setBlockType(BlockTypes.STANDING_SIGN, cause);
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
    }

    private void nameVillager(final Villager villager) {
        final Optional<Value<Text>> name = villager.getValue(Keys.DISPLAY_NAME);
        if(!name.isPresent()) {
            final Text text = buildVillagerName(villager);
            logger.info(String.format(
                "Added name [%s] to villager with UUID [%s]", text.toPlain(), villager.getUniqueId()));
            villager.offer(Keys.DISPLAY_NAME, text);
        }
    }

    // Hack trades onto nitwits
    private void addTrades(final Villager villager) {
        final List<TradeOffer> tradeOffers = villager.getValue(Keys.TRADE_OFFERS).map(t -> new ArrayList<>(t.getAll())).orElse(new ArrayList<>());

        if(tradeOffers.isEmpty() && nitwit.equals(villager.career().get())) {
            MUTATOR_MAP.get(0).forEach(a -> a.accept(villager, tradeOffers, random));
            villager.offer(Keys.TRADE_OFFERS, tradeOffers);
        }
    }

    private void resetTrades(final Villager villager) {
        final List<TradeOffer> tradeOffers = villager.getValue(Keys.TRADE_OFFERS).map(t -> new ArrayList<>(t.getAll())).orElse(new ArrayList<>());

        final List<TradeOffer> refreshedTradeOffers = tradeOffers.stream()
            .map(t -> TradeOffer.builder().from(t).uses(0).build())
            .collect(Collectors.toList());

        villager.offer(Keys.TRADE_OFFERS, refreshedTradeOffers);
    }

    private Location<World> findDeathBlock(Location<World> location) {
        if(location.getBlockType() != BlockTypes.AIR) return findDeathBlock(location.add(0,1,0));
        final Location<World> below = location.add(0,-1,0);
        if(below.getBlockType() == BlockTypes.AIR) return findDeathBlock(below);
        return location;
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

    @Override
    protected String getID() {
        return ID;
    }

    private static final List<List<TradeOfferListMutator>> MUTATOR_MAP;

    private static final List<DyeColor> DYE_COLORS = ImmutableList.of(
        DyeColors.BLACK,
        DyeColors.BLUE,
        DyeColors.BROWN,
        DyeColors.CYAN,
        DyeColors.GRAY,
        DyeColors.GREEN,
        DyeColors.LIGHT_BLUE,
        DyeColors.LIME,
        DyeColors.MAGENTA,
        DyeColors.ORANGE,
        DyeColors.PINK,
        DyeColors.PURPLE,
        DyeColors.RED,
        DyeColors.WHITE,
        DyeColors.SILVER,
        DyeColors.YELLOW );

    private static final List<PlantType> PLANT_TYPES = ImmutableList.of(
        PlantTypes.ALLIUM,
        PlantTypes.BLUE_ORCHID,
        PlantTypes.DANDELION,
        PlantTypes.HOUSTONIA,
        PlantTypes.ORANGE_TULIP,
        PlantTypes.OXEYE_DAISY,
        PlantTypes.PINK_TULIP,
        PlantTypes.POPPY,
        PlantTypes.RED_TULIP,
        PlantTypes.WHITE_TULIP );

    private static final int USES = 0xFF;

    static {
        final List<TradeOfferListMutator> mutators1 = ImmutableList.of(
            (merchant, offers, random) -> {
                final DyeColor color = DYE_COLORS.get(random.nextInt(DYE_COLORS.size()));

                final int count = random.nextInt(3)+1;

                offers.add(TradeOffer.builder()
                    .canGrantExperience(false)
                    .firstBuyingItem(
                        ItemStack.builder()
                            .itemType(ItemTypes.DYE)
                            .quantity(count*8)
                            .keyValue(Keys.DYE_COLOR, color)
                            .build())
                    .sellingItem(ItemStack.builder().itemType(ItemTypes.EMERALD).quantity(1).build())
                    .uses(0)
                    .maxUses(USES)
                    .build());

                offers.add(TradeOffer.builder()
                    .canGrantExperience(false)
                    .firstBuyingItem(ItemStack.builder().itemType(ItemTypes.EMERALD).quantity(1).build())
                    .sellingItem(ItemStack.builder()
                        .itemType(ItemTypes.DYE)
                        .quantity(count)
                        .keyValue(Keys.DYE_COLOR, color)
                        .build())
                    .uses(0)
                    .maxUses(USES)
                    .build());
            });

        final List<TradeOfferListMutator> mutators2 = ImmutableList.of(
            (merchant, offers, random) -> {
                final PlantType type = PLANT_TYPES.get(random.nextInt(PLANT_TYPES.size()));

                final int count = random.nextInt(3)+1;

                offers.add(TradeOffer.builder()
                    .canGrantExperience(true)
                    .firstBuyingItem(
                        ItemStack.builder()
                            .itemType(ItemTypes.RED_FLOWER)
                            .quantity(count*8)
                            .keyValue(Keys.PLANT_TYPE, type)
                            .build())
                    .sellingItem(ItemStack.builder().itemType(ItemTypes.EMERALD).quantity(1).build())
                    .uses(0)
                    .maxUses(USES)
                    .build());

                offers.add(TradeOffer.builder()
                    .canGrantExperience(true)
                    .firstBuyingItem(ItemStack.builder().itemType(ItemTypes.EMERALD).quantity(1).build())
                    .sellingItem(
                        ItemStack.builder()
                            .itemType(ItemTypes.RED_FLOWER)
                            .quantity(count)
                            .keyValue(Keys.PLANT_TYPE, type)
                            .build())
                    .uses(0)
                    .maxUses(USES)
                    .build());
            });

        final List<TradeOfferListMutator> mutators3 = ImmutableList.of(
            (merchant, offers, random) -> {
                final int count = random.nextInt(3)+1;
                offers.add(TradeOffer.builder()
                    .canGrantExperience(true)
                    .firstBuyingItem(ItemStack.builder().itemType(ItemTypes.EMERALD).quantity(count).build())
                    .sellingItem(ItemStack.builder().itemType(ItemTypes.NAME_TAG).quantity(1).build())
                    .uses(0)
                    .maxUses(USES)
                    .build());
            });

        MUTATOR_MAP = ImmutableList.of(mutators1, mutators2, mutators3);
    }
}
