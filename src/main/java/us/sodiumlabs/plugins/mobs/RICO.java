package us.sodiumlabs.plugins.mobs;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.property.item.UseLimitProperty;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.ArmorEquipable;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Hostile;
import org.spongepowered.api.entity.living.monster.Creeper;
import org.spongepowered.api.entity.living.monster.Enderman;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.explosion.Explosion;
import us.sodiumlabs.plugins.AbstractPlugin;

import java.security.SecureRandom;
import java.util.Optional;
import java.util.Random;


@Plugin(id=RICO.ID, name="RICO", version = "1.0", description = "makes fighting mobs cooler")
public class RICO extends AbstractPlugin {
    static final String ID = "rico";

    private final Logger logger;

    private final Random random;

    @VisibleForTesting
    RICO(final Logger logger, final Random random) {
        this.logger = logger;
        this.random = random;
    }

    @Inject
    public RICO(final Logger logger) {
        this(logger, new SecureRandom());
    }

    @Listener
    public void onEnderEvent(final ChangeBlockEvent event, @Root final Enderman enderman) {
        event.setCancelled(true);
        event.getTransactions().forEach(t->t.setValid(false));
        logger.debug(":::: prevented an enderthief!");
    }

    @Listener
    public void onExplode(final ExplosionEvent.Pre event, @Root final Creeper creeper) {
        // prevent creeper explosions
        if(event.getExplosion().shouldBreakBlocks()) {

            event.setCancelled(true);

            final Explosion explosion = Explosion.builder()
                .from(event.getExplosion())
                .shouldBreakBlocks(false)
                .build();

            final Cause cause = createNamedCause("HARMLESS EXPLOSION");

            explosion.getWorld().triggerExplosion(explosion, cause);

            logger.debug(":::: prevented some creefing");
        }
    }

    @Listener
    public void onBirth(final SpawnEntityEvent event) {
        if(event instanceof SpawnEntityEvent.ChunkLoad) return;

        event.getEntities().stream()
            .filter( entity -> entity instanceof ArmorEquipable && entity instanceof Hostile )
            .map( e -> (ArmorEquipable) e )
            .forEach( this::damageItems );
    }

    @Listener
    public void onDeath(final DestructEntityEvent.Death event) {
        final Entity entity = event.getTargetEntity();

        // Make mobs drop their stuff on death.
        if(entity instanceof ArmorEquipable && entity instanceof Hostile) {
            final ArmorEquipable armorEntity = (ArmorEquipable) entity;

            final Cause cause = createNamedCause("DROP ITEMS");
            final Location<World> location = entity.getLocation();

            spawnArmor(location, armorEntity.getBoots(), cause);
            spawnArmor(location, armorEntity.getLeggings(), cause);
            spawnArmor(location, armorEntity.getChestplate(), cause);
            spawnArmor(location, armorEntity.getHelmet(), cause);
            spawnArmor(location, armorEntity.getItemInHand(HandTypes.MAIN_HAND), cause);
            spawnArmor(location, armorEntity.getItemInHand(HandTypes.OFF_HAND), cause);

            armorEntity.setBoots(null);
            armorEntity.setLeggings(null);
            armorEntity.setChestplate(null);
            armorEntity.setHelmet(null);
            armorEntity.setItemInHand(HandTypes.MAIN_HAND,null);
            armorEntity.setItemInHand(HandTypes.OFF_HAND,null);
        }
    }

    private void damageItems(final ArmorEquipable armorEntity) {
        damageItems(armorEntity.getBoots()).ifPresent(armorEntity::setBoots);
        damageItems(armorEntity.getLeggings()).ifPresent(armorEntity::setLeggings);
        damageItems(armorEntity.getChestplate()).ifPresent(armorEntity::setChestplate);
        damageItems(armorEntity.getHelmet()).ifPresent(armorEntity::setHelmet);
        damageItems(armorEntity.getItemInHand(HandTypes.MAIN_HAND))
            .ifPresent( i -> armorEntity.setItemInHand(HandTypes.MAIN_HAND, i));
        damageItems(armorEntity.getItemInHand(HandTypes.OFF_HAND))
            .ifPresent( i -> armorEntity.setItemInHand(HandTypes.OFF_HAND, i));
    }

    private Optional<ItemStack> damageItems(final Optional<ItemStack> optionalItemStack) {
        optionalItemStack.ifPresent( itemStack -> {
            itemStack.getValue(Keys.ITEM_DURABILITY).ifPresent(durability -> {
                final int max = itemStack.getProperty(UseLimitProperty.class).map(UseLimitProperty::getValue).orElse(1);
                itemStack.offer(Keys.ITEM_DURABILITY, calculateDamage(max));
            });
        });

        return optionalItemStack;
    }

    private void spawnArmor(final Location<World> location, final Optional<ItemStack> optionalArmor, final Cause cause) {
        optionalArmor.ifPresent(armor -> {
            spawnItems(location, armor, cause);
        });
    }

    @VisibleForTesting
    int calculateDamage(final int max) {
        final double r = random.nextDouble() * random.nextDouble();
        final int damage = (int) Math.floor(r * max);

        logger.info(String.format(":::: Offering item with durability max [%d] min [%d] r [%s], new durability [%d]",
            max, 0, r, damage));

        return damage;
    }

    @Override
    protected String getID() {
        return ID;
    }
}
