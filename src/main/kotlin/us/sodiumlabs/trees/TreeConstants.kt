package us.sodiumlabs.trees

import net.minecraft.core.Vec3i
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import kotlin.math.abs

interface TreeType {
    val logTypes: Set<Block>
    val roofMedia: Set<Block>
    val growthMedia: Set<Block>
    val roofMediaSearchVecs: Set<Vec3i>
}

internal fun Vec3i.isOpposedAlongADimension(otherVec: Vec3i): Boolean {
    return abs(x + otherVec.x) != abs(x) + abs(otherVec.x)
            || abs(y + otherVec.y) != abs(y) + abs(otherVec.y)
            || abs(z + otherVec.z) != abs(z) + abs(otherVec.z)
}

internal fun Block.isLog() = TreeConstants.LOGS_TO_TREE_TYPES.containsKey(this)
internal fun Block.getLogTypes(): Set<Block> = TreeConstants.LOGS_TO_TREE_TYPES[this]?.logTypes ?: setOf()
internal fun Block.getRoofMedia(): Set<Block> = TreeConstants.LOGS_TO_TREE_TYPES[this]?.roofMedia ?: setOf()
internal fun Block.getGrowthMedia(): Set<Block> = TreeConstants.LOGS_TO_TREE_TYPES[this]?.growthMedia ?: setOf()
internal fun Block.roofMediaSearchVecs(): Set<Vec3i> =
    TreeConstants.LOGS_TO_TREE_TYPES[this]?.roofMediaSearchVecs ?: setOf()

private object TreeConstants {
    private val standardGrowthMedia = setOf(
        Blocks.GRASS_BLOCK,
        Blocks.DIRT,
        Blocks.COARSE_DIRT,
        Blocks.PODZOL,
        Blocks.ROOTED_DIRT,
        Blocks.MYCELIUM,
        Blocks.MOSS_BLOCK,
    )

    private object Acacia : TreeType {
        override val logTypes = setOf(
            Blocks.ACACIA_LOG,
            Blocks.STRIPPED_ACACIA_LOG
        )
        override val roofMedia = setOf(
            Blocks.ACACIA_LEAVES
        )
        override val growthMedia = standardGrowthMedia
        override val roofMediaSearchVecs = LEAF_NEXT_VECS
    }

    private object Birch : TreeType {
        override val logTypes = setOf(
            Blocks.BIRCH_LOG,
            Blocks.STRIPPED_BIRCH_LOG
        )
        override val roofMedia = setOf(
            Blocks.BIRCH_LEAVES
        )
        override val growthMedia = standardGrowthMedia
        override val roofMediaSearchVecs = LEAF_NEXT_VECS
    }

    private object DarkOak : TreeType {
        override val logTypes = setOf(
            Blocks.DARK_OAK_LOG,
            Blocks.STRIPPED_DARK_OAK_LOG
        )
        override val roofMedia = setOf(
            Blocks.DARK_OAK_LEAVES
        )
        override val growthMedia = standardGrowthMedia
        override val roofMediaSearchVecs = LEAF_NEXT_VECS
    }

    private object Jungle : TreeType {
        override val logTypes = setOf(
            Blocks.JUNGLE_LOG,
            Blocks.STRIPPED_JUNGLE_LOG
        )
        override val roofMedia = setOf(
            Blocks.JUNGLE_LEAVES
        )
        override val growthMedia = standardGrowthMedia
        override val roofMediaSearchVecs = LEAF_NEXT_VECS
    }

    private object Oak : TreeType {
        override val logTypes = setOf(
            Blocks.OAK_LOG,
            Blocks.STRIPPED_OAK_LOG
        )
        override val roofMedia = setOf(
            Blocks.OAK_LEAVES
        )
        override val growthMedia = standardGrowthMedia
        override val roofMediaSearchVecs = LEAF_NEXT_VECS
    }

    private object Spruce : TreeType {
        override val logTypes = setOf(
            Blocks.SPRUCE_LOG,
            Blocks.STRIPPED_SPRUCE_LOG
        )
        override val roofMedia = setOf(
            Blocks.SPRUCE_LEAVES
        )
        override val growthMedia = standardGrowthMedia
        override val roofMediaSearchVecs = LEAF_NEXT_VECS
    }

    private object Mushroom : TreeType {
        override val logTypes = setOf(
            Blocks.MUSHROOM_STEM
        )
        override val roofMedia = setOf(
            Blocks.BROWN_MUSHROOM_BLOCK,
            Blocks.RED_MUSHROOM_BLOCK
        )
        override val growthMedia = setOf(Blocks.DIRT, Blocks.MYCELIUM)
        override val roofMediaSearchVecs = MUSHROOM_NEXT_VECS

    }

    private object Crimson : TreeType {
        override val logTypes = setOf(
            Blocks.CRIMSON_STEM,
            Blocks.STRIPPED_CRIMSON_STEM
        )
        override val roofMedia = setOf(
            Blocks.NETHER_WART_BLOCK,
            Blocks.SHROOMLIGHT
        )
        override val growthMedia = setOf(
            Blocks.CRIMSON_NYLIUM,
            Blocks.NETHERRACK
        )
        override val roofMediaSearchVecs = WART_NEXT_VECS
    }

    private object Warped : TreeType {
        override val logTypes = setOf(
            Blocks.WARPED_STEM,
            Blocks.STRIPPED_WARPED_STEM
        )
        override val roofMedia = setOf(
            Blocks.WARPED_WART_BLOCK,
            Blocks.SHROOMLIGHT
        )
        override val growthMedia = setOf(
            Blocks.WARPED_NYLIUM,
            Blocks.NETHERRACK
        )
        override val roofMediaSearchVecs = WART_NEXT_VECS
    }

    internal val LOGS_TO_TREE_TYPES: Map<Block, TreeType> =
        setOf(Acacia, Birch, DarkOak, Jungle, Oak, Spruce, Mushroom, Crimson, Warped)
            .flatMap { it.logTypes.map { t -> t to it } }
            .associate { it }
}

internal val SIBLING_SUPPORT_VECTORS = setOf(
    Vec3i(1, 0, 0),
    Vec3i(-1, 0, 0),
    Vec3i(0, 0, 1),
    Vec3i(0, 0, -1)
)

internal val LOWER_DETECTION_VECTORS = setOf(
    Vec3i(0, -1, 0),
    Vec3i(1, -1, 0),
    Vec3i(-1, -1, 0),
    Vec3i(0, -1, 1),
    Vec3i(0, -1, -1)
)

internal val UPPER_DETECTION_VECTORS = setOf(
    Vec3i(0, 1, 0),
    Vec3i(1, 1, 0),
    Vec3i(-1, 1, 0),
    Vec3i(0, 1, 1),
    Vec3i(0, 1, -1)
)

internal val TREE_CAPITATOR_INIT_VECS = setOf(
    Vec3i(0, 1, 0),
    Vec3i(1, 1, 0),
    Vec3i(-1, 1, 0),
    Vec3i(0, 1, 1),
    Vec3i(0, 1, -1),
    Vec3i(1, 1, 1),
    Vec3i(1, 1, -1),
    Vec3i(-1, 1, 1),
    Vec3i(-1, 1, -1)
)

internal val TREE_CAPITATOR_NEXT_VECS = setOf(
    // This level
    Vec3i(1, 0, 0),
    Vec3i(-1, 0, 0),
    Vec3i(0, 0, 1),
    Vec3i(0, 0, -1),
    Vec3i(1, 0, 1),
    Vec3i(1, 0, -1),
    Vec3i(-1, 0, 1),
    Vec3i(-1, 0, -1),
    // Upper level
    Vec3i(0, 1, 0),
    Vec3i(1, 1, 0),
    Vec3i(-1, 1, 0),
    Vec3i(0, 1, 1),
    Vec3i(0, 1, -1),
    Vec3i(1, 1, 1),
    Vec3i(1, 1, -1),
    Vec3i(-1, 1, 1),
    Vec3i(-1, 1, -1)
)

private val LEAF_NEXT_VECS = setOf(
    Vec3i(1, 0, 0),
    Vec3i(-1, 0, 0),
    Vec3i(0, 0, 1),
    Vec3i(0, 0, -1),
    Vec3i(0, 1, 0),
    Vec3i(0, -1, 0),
)

private val WART_NEXT_VECS = setOf(
    // This level
    Vec3i(1, 0, 0),
    Vec3i(-1, 0, 0),
    Vec3i(0, 0, 1),
    Vec3i(0, 0, -1),
    Vec3i(1, 0, 1),
    Vec3i(1, 0, -1),
    Vec3i(-1, 0, 1),
    Vec3i(-1, 0, -1),
    // Upper level
    Vec3i(0, 1, 0),
    Vec3i(1, 1, 0),
    Vec3i(-1, 1, 0),
    Vec3i(0, 1, 1),
    Vec3i(0, 1, -1),
    Vec3i(1, 1, 1),
    Vec3i(1, 1, -1),
    Vec3i(-1, 1, 1),
    Vec3i(-1, 1, -1),
    // Lower level
    Vec3i(0, -1, 0),
    Vec3i(1, -1, 0),
    Vec3i(-1, -1, 0),
    Vec3i(0, -1, 1),
    Vec3i(0, -1, -1),
    Vec3i(1, -1, 1),
    Vec3i(1, -1, -1),
    Vec3i(-1, -1, 1),
    Vec3i(-1, -1, -1)
)

private val MUSHROOM_NEXT_VECS = setOf(
    // This level
    Vec3i(1, 0, 0),
    Vec3i(-1, 0, 0),
    Vec3i(0, 0, 1),
    Vec3i(0, 0, -1),
    Vec3i(1, 0, 1),
    Vec3i(1, 0, -1),
    Vec3i(-1, 0, 1),
    Vec3i(-1, 0, -1),
    // Upper level
    Vec3i(0, 1, 0),
    Vec3i(1, 1, 0),
    Vec3i(-1, 1, 0),
    Vec3i(0, 1, 1),
    Vec3i(0, 1, -1),
    // Lower level
    Vec3i(0, -1, 0),
    Vec3i(1, -1, 0),
    Vec3i(-1, -1, 0),
    Vec3i(0, -1, 1),
    Vec3i(0, -1, -1),
)

internal const val MAX_DETECTION_DEPTH = 32
internal const val MAX_LEAF_SEARCH_DEPTH = 6
