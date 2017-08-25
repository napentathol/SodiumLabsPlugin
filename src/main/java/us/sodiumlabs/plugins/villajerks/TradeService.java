package us.sodiumlabs.plugins.villajerks;

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.*;
import org.spongepowered.api.entity.living.Villager;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.merchant.TradeOffer;
import org.spongepowered.api.item.merchant.TradeOfferListMutator;
import org.spongepowered.api.item.merchant.VillagerRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

class TradeService {

    static final String NITWIT_ID = "minecraft:nitwit";

    private final Logger logger;

    private final Game game;

    private final Random random;

    private Career nitwit;

    TradeService(Logger logger, Game game, Random random) {
        this.logger = requireNonNull(logger);
        this.game = requireNonNull(game);
        this.random = requireNonNull(random);
    }

    void registerNitwitTrades() {
        nitwit = game.getRegistry().getType(Career.class, NITWIT_ID)
            .orElseThrow(() -> new RuntimeException("No nitwits."));
        logger.info(nitwit.toString());

        final VillagerRegistry registry = game.getRegistry().getVillagerRegistry();
        for (int i = 0; i < MUTATOR_MAP.size(); i++) {
            registry.setMutators(nitwit, i + 1, MUTATOR_MAP.get(i));
        }
    }

    // Hack trades onto nitwits
    void addTrades(final Villager villager) {
        final List<TradeOffer> tradeOffers = villager.getValue(Keys.TRADE_OFFERS).map(t -> new ArrayList<>(t.getAll())).orElse(new ArrayList<>());

        if(tradeOffers.isEmpty() && nitwit.equals(villager.career().get())) {
            MUTATOR_MAP.get(0).forEach(a -> a.accept(villager, tradeOffers, random));
            villager.offer(Keys.TRADE_OFFERS, tradeOffers);
        }
    }

    void resetTrades(final Villager villager) {
        final List<TradeOffer> tradeOffers = villager.getValue(Keys.TRADE_OFFERS).map(t -> new ArrayList<>(t.getAll())).orElse(new ArrayList<>());

        final List<TradeOffer> refreshedTradeOffers = tradeOffers.stream()
            .map(t -> TradeOffer.builder().from(t).uses(0).build())
            .collect(Collectors.toList());

        villager.offer(Keys.TRADE_OFFERS, refreshedTradeOffers);
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
