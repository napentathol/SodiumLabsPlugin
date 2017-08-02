package us.sodiumlabs.plugins;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import static java.util.Objects.requireNonNull;

public abstract class AbstractPlugin {
    private PluginContainer pluginContainer;

    protected PluginContainer getOrInitializePluginContainer() {
        if(null == pluginContainer) {
            pluginContainer = requireNonNull(
                Sponge.getPluginManager().getPlugin(getID()).orElse(null), "Requires plugin container!" );
        }
        return pluginContainer;
    }

    protected void spawnItems(final Location<World> spawnLocation, final ItemStack itemStack, final Cause cause) {
        final World world = spawnLocation.getExtent();
        final Entity itemEntity = world.createEntity(EntityTypes.ITEM, spawnLocation.getPosition().add(0.5,0.5,0.5));
        itemEntity.offer(Keys.REPRESENTED_ITEM, itemStack.createSnapshot());
        world.spawnEntity(itemEntity, cause);
    }

    protected Cause createNamedCause(final String namedCause) {
        return Cause.of(NamedCause.of(namedCause, getOrInitializePluginContainer()));
    }

    protected Cause createCauseFromCause(final Cause cause) {
        return Cause.builder()
            .from(cause)
            .owner(getOrInitializePluginContainer())
            .build();
    }

    protected abstract String getID();
}
