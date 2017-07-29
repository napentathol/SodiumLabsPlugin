package us.sodiumlabs.plugins.treecapitator;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;

import static java.util.Objects.requireNonNull;


@Plugin(id = TreeCapitator.ID, name = "Treecapitator", version = "1.1", description = "treecapitates trees" )
public class TreeCapitator {
    static final String ID = "treecapitator";

    private static final int MAX_BREAK_DEPTH = 64;

    private static final ImmutableList<Vector3i> TREE_CAPITATOR_INIT_VECTORS =
        ImmutableList.of(
            new Vector3i( 0, 1, 0 ),
            new Vector3i( 1, 1, 0 ),
            new Vector3i( -1, 1, 0 ),
            new Vector3i( 0, 1, 1 ),
            new Vector3i( 0, 1, -1 ),
            new Vector3i( 1, 1, 1 ),
            new Vector3i( 1, 1, -1 ),
            new Vector3i( -1, 1, 1 ),
            new Vector3i( -1, 1, -1 ) );

    private static final ImmutableList<Vector3i> TREE_CAPITATOR_NEXT_VECTORS =
        ImmutableList.of(
            // This level
            new Vector3i( 1, 0, 0 ),
            new Vector3i( -1, 0, 0 ),
            new Vector3i( 0, 0, 1 ),
            new Vector3i( 0, 0, -1 ),
            new Vector3i( 1, 0, 1 ),
            new Vector3i( 1, 0, -1 ),
            new Vector3i( -1, 0, 1 ),
            new Vector3i( -1, 0, -1 ),
            // Upper level
            new Vector3i( 0, 1, 0 ),
            new Vector3i( 1, 1, 0 ),
            new Vector3i( -1, 1, 0 ),
            new Vector3i( 0, 1, 1 ),
            new Vector3i( 0, 1, -1 ),
            new Vector3i( 1, 1, 1 ),
            new Vector3i( 1, 1, -1 ),
            new Vector3i( -1, 1, 1 ),
            new Vector3i( -1, 1, -1 ) );

    private final Logger logger;

    private final Detector detector;

    private final boolean debug;

    private PluginContainer pluginContainer;

    @Inject
    public TreeCapitator(final Logger logger) {
        this.logger = requireNonNull(logger);
        this.detector = new Detector(logger);
        this.debug = false;

        logger.info( "Initialized Treecapitator!" );
    }

    @Listener
    public void onBlockBreak(final ChangeBlockEvent.Break event) {
        event.getTransactions().stream()
            .filter(detector::operatingOnWood)
            .filter(detector::operatingOnTree)
            .forEach(t -> t.getOriginal().getLocation().ifPresent(location -> {
                if(debug) {
                    final Vector3i v = location.getBlockPosition();
                    Sponge.getServer().getBroadcastChannel()
                        .send(Text.builder(String.format("Found tree at %d,%d,%d", v.getX(), v.getY(), v.getZ())).color(TextColors.GOLD).build());
                }
                scheduleChopTasks(location, t.getOriginal().getState(), TREE_CAPITATOR_INIT_VECTORS, 0);
            }));
    }

    private void scheduleChopTasks(final Location<World> location, final BlockState original, final List<Vector3i> initVectors, final int depth) {
        if(depth < MAX_BREAK_DEPTH) {
            for (final Vector3i initVector : initVectors) {
                scheduleChopTask(new Location<>(location.getExtent(), location.getBlockPosition().add(initVector)), original, depth);
            }
        } else {
            logger.warn("Reached max break depth!!!");
        }
    }

    private void scheduleChopTask(final Location<World> location, final BlockState original, final int depth) {
        final World world = location.getExtent();

        if(detector.isLog(world.getBlockType(location.getBlockPosition()))) {
            Task.builder().execute(chopTask(location, original, depth)).delayTicks(1).submit(this);
        }
    }

    private Runnable chopTask(final Location<World> location, final BlockState original, final int depth) {
        return () -> {
            final World world = location.getExtent();
            final BlockSnapshot snapshot = world.createSnapshot(location.getBlockPosition());

            if(detector.isLog(snapshot.getState().getType())
                && detector.sameLogType(original, snapshot.getState())) {
                breakBlock(location);
                scheduleChopTasks(location, original, TREE_CAPITATOR_NEXT_VECTORS, depth + 1);
            }
        };
    }

    private PluginContainer getOrInitializePluginContainer() {
        if(null == pluginContainer) {
            pluginContainer = requireNonNull(
                Sponge.getPluginManager().getPlugin(ID).orElse(null), "Requires plugin container!" );
        }
        return pluginContainer;
    }

    private void breakBlock(final Location<World> location) {
        final World world = location.getExtent();
        final BlockSnapshot blockSnapshot = world.createSnapshot(location.getBlockPosition());

        if(!BlockTypes.AIR.equals(blockSnapshot.getState().getType())) {
            final Cause cause = Cause.of(NamedCause.of(NamedCause.DECAY, getOrInitializePluginContainer()));
            world.setBlockType(location.getBlockPosition(), BlockTypes.AIR, BlockChangeFlag.ALL, cause);

            final ItemStack itemStack = ItemStack.builder().fromBlockSnapshot(blockSnapshot).quantity(1).build();
            final Entity itemEntity = world.createEntity(EntityTypes.ITEM, location.getPosition().add(0.5,0.5,0.5));
            itemEntity.offer(Keys.REPRESENTED_ITEM, itemStack.createSnapshot());
            world.spawnEntity(itemEntity, cause);
        }
    }
}
