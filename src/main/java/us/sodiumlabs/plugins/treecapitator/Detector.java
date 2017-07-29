package us.sodiumlabs.plugins.treecapitator;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.trait.EnumTrait;
import org.spongepowered.api.block.trait.EnumTraits;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.type.TreeType;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

class Detector {
    private static final long MAX_DETECTION_TIME = 500;

    private static final int MAX_DETECTION_DEPTH = 32;

    private static final Predicate<BlockSnapshot> DIRT_PREDICATE =
        (BlockSnapshot snapshot) -> BlockTypes.DIRT.equals(snapshot.getState().getType());

    private static final ImmutableList<Vector3i> LOWER_DETECTION_VECTORS =
        ImmutableList.of(
            new Vector3i( 0, -1, 0 ),
            new Vector3i( 1, -1, 0 ),
            new Vector3i( -1, -1, 0 ),
            new Vector3i( 0, -1, 1 ),
            new Vector3i( 0, -1, -1 ) );

    private static final ImmutableList<Vector3i> UPPER_DETECTION_VECTORS =
        ImmutableList.of(
            new Vector3i( 0, 1, 0 ),
            new Vector3i( 1, 1, 0 ),
            new Vector3i( -1, 1, 0 ),
            new Vector3i( 0, 1, 1 ),
            new Vector3i( 0, 1, -1 ) );

    private final Logger logger;

    Detector(final Logger logger) {
        this.logger = requireNonNull(logger);
    }

    boolean operatingOnTree(Transaction<BlockSnapshot> transaction) {
        final BlockSnapshot snapshot = transaction.getOriginal();
        final BlockState state = snapshot.getState();
        final Optional<World> world = Sponge.getServer().getWorld(snapshot.getWorldUniqueId());

        return world.isPresent() && getCorrectTrait(state.getType())
            .map(t -> (TreeType) state.getTraitMap().get(t))
            .map(t -> isAboveDirt(snapshot, t, world.get()) && isBelowLeaves(snapshot, t, world.get()))
            .orElse( false );
    }

    boolean operatingOnWood(final Transaction<BlockSnapshot> blockSnapshot) {
        return isLog(blockSnapshot.getOriginal().getState().getType());
    }

    boolean isLog(final BlockType type) {
        return BlockTypes.LOG.equals(type) || BlockTypes.LOG2.equals(type);
    }

    private boolean isLeaves(final BlockType type) {
        return BlockTypes.LEAVES.equals( type ) || BlockTypes.LEAVES2.equals( type );
    }

    boolean sameLogType(BlockState a, BlockState b) {
        return getTreeTypeOfBlock(a).map(aType -> getTreeTypeOfBlock(b).map(bType -> bType.equals(aType)).orElse(false)).orElse(false);
    }

    private boolean isBelowLeaves(final BlockSnapshot snapshot, final TreeType treeType, final World world) {
        return treeDetectionCombinator(
            leavesPredicate(treeType), logPredicate(treeType), UPPER_DETECTION_VECTORS,
            world, snapshot, System.currentTimeMillis(), 0);
    }

    private boolean isAboveDirt(final BlockSnapshot snapshot, final TreeType treeType, final World world) {
        return treeDetectionCombinator(
            DIRT_PREDICATE, logPredicate(treeType), LOWER_DETECTION_VECTORS,
            world, snapshot, System.currentTimeMillis(), 0);
    }

    private Optional<TreeType> getTreeTypeOfBlock(final BlockState block)
    {
        return getCorrectTrait(block.getType()).map(t -> (TreeType) block.getTraitMap().get(t));
    }

    private Optional<EnumTrait<?>> getCorrectTrait(final BlockType type ) {
        if(BlockTypes.SAPLING.equals(type)) return Optional.of(EnumTraits.SAPLING_TYPE);
        if(BlockTypes.LOG.equals(type))     return Optional.of(EnumTraits.LOG_VARIANT);
        if(BlockTypes.LOG2.equals(type))    return Optional.of(EnumTraits.LOG2_VARIANT);
        if(BlockTypes.LEAVES.equals(type))  return Optional.of(EnumTraits.LEAVES_VARIANT);
        if(BlockTypes.LEAVES2.equals(type)) return Optional.of(EnumTraits.LEAVES2_VARIANT);

        return Optional.empty();
    }

    private Predicate<BlockSnapshot> logPredicate(final TreeType treeType) {
        return (BlockSnapshot snapshot) -> isLog(snapshot.getState().getType())
            && treeType.equals(getTreeTypeOfBlock(snapshot.getState()).orElse(null));
    }

    private Predicate<BlockSnapshot> leavesPredicate(final TreeType treeType) {
        return (BlockSnapshot snapshot) -> isLeaves(snapshot.getState().getType())
            && treeType.equals(getTreeTypeOfBlock(snapshot.getState()).orElse(null));
    }

    private boolean treeDetectionCombinator(
        final Predicate<BlockSnapshot> leafPredicate,
        final Predicate<BlockSnapshot> branchPredicate,
        final List<Vector3i> detectionVectors,
        final World world,
        final BlockSnapshot snapshot,
        final long startTimeMillis,
        final int d)
    {
        if(System.currentTimeMillis() - startTimeMillis > MAX_DETECTION_TIME)
        {
            logger.warn("Tree detection timed out!");

            return false;
        }

        if(d >= MAX_DETECTION_DEPTH)
        {
            return false;
        }

        final BlockSnapshot[] snapshots = new BlockSnapshot[detectionVectors.size()];
        for(int i = 0; i < detectionVectors.size(); i++) {
            snapshots[i] = world.createSnapshot(snapshot.getPosition().add(detectionVectors.get(i)));
        }

        for(BlockSnapshot testSnapshot : snapshots)
        {
            if(leafPredicate.test(testSnapshot))
            {
                return true;
            }
        }

        final int dd = d + 1;
        for(BlockSnapshot testSnapshot : snapshots)
        {
            if(branchPredicate.test(testSnapshot)
                && treeDetectionCombinator(leafPredicate, branchPredicate, detectionVectors, world, testSnapshot, startTimeMillis, dd))
            {
                return true;
            }
        }

        return false;
    }
}
