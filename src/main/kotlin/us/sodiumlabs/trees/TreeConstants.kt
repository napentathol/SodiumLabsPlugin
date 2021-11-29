package us.sodiumlabs.trees

import com.google.common.collect.ImmutableSetMultimap
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.util.math.Vec3i
import kotlin.math.abs

internal val LOG_TYPES = setOf(Blocks.ACACIA_LOG, Blocks.BIRCH_LOG, Blocks.CRIMSON_STEM, Blocks.DARK_OAK_LOG,
        Blocks.JUNGLE_LOG, Blocks.MUSHROOM_STEM, Blocks.OAK_LOG, Blocks.SPRUCE_LOG, Blocks.WARPED_STEM)

internal val LOGS_TO_ROOF_MEDIA = ImmutableSetMultimap.builder<Block, Block>()
        .put(Blocks.ACACIA_LOG, Blocks.ACACIA_LEAVES)
        .put(Blocks.BIRCH_LOG, Blocks.BIRCH_LEAVES)
        .putAll(Blocks.CRIMSON_STEM, Blocks.NETHER_WART_BLOCK, Blocks.SHROOMLIGHT)
        .put(Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_LEAVES)
        .put(Blocks.JUNGLE_LOG, Blocks.JUNGLE_LEAVES)
        .putAll(Blocks.OAK_LOG, Blocks.OAK_LEAVES, Blocks.AZALEA_LEAVES, Blocks.FLOWERING_AZALEA_LEAVES)
        .put(Blocks.SPRUCE_LOG, Blocks.SPRUCE_LEAVES)
        .putAll(Blocks.WARPED_STEM, Blocks.WARPED_WART_BLOCK, Blocks.SHROOMLIGHT)

        .put(Blocks.STRIPPED_ACACIA_LOG, Blocks.ACACIA_LEAVES)
        .put(Blocks.STRIPPED_BIRCH_LOG, Blocks.BIRCH_LEAVES)
        .putAll(Blocks.STRIPPED_CRIMSON_STEM, Blocks.NETHER_WART_BLOCK, Blocks.SHROOMLIGHT)
        .put(Blocks.STRIPPED_DARK_OAK_LOG, Blocks.DARK_OAK_LEAVES)
        .put(Blocks.STRIPPED_JUNGLE_LOG, Blocks.JUNGLE_LEAVES)
        .putAll(Blocks.STRIPPED_OAK_LOG, Blocks.OAK_LEAVES, Blocks.AZALEA_LEAVES, Blocks.FLOWERING_AZALEA_LEAVES)
        .put(Blocks.STRIPPED_SPRUCE_LOG, Blocks.SPRUCE_LEAVES)
        .putAll(Blocks.STRIPPED_WARPED_STEM, Blocks.WARPED_WART_BLOCK, Blocks.SHROOMLIGHT)

        .putAll(Blocks.MUSHROOM_STEM, Blocks.BROWN_MUSHROOM_BLOCK, Blocks.RED_MUSHROOM_BLOCK)
        .build()

internal val BASIC_GROWTH_MEDIA = setOf<Block>(Blocks.GRASS_BLOCK, Blocks.COARSE_DIRT, Blocks.PODZOL, Blocks.MYCELIUM,
        Blocks.ROOTED_DIRT, Blocks.DIRT, Blocks.MOSS_BLOCK)

internal val LOGS_TO_GROWTH_MEDIA = ImmutableSetMultimap.builder<Block, Block>()
        .putAll(Blocks.ACACIA_LOG, BASIC_GROWTH_MEDIA)
        .putAll(Blocks.BIRCH_LOG, BASIC_GROWTH_MEDIA)
        .putAll(Blocks.DARK_OAK_LOG, BASIC_GROWTH_MEDIA)
        .putAll(Blocks.JUNGLE_LOG, BASIC_GROWTH_MEDIA)
        .putAll(Blocks.OAK_LOG, BASIC_GROWTH_MEDIA)
        .putAll(Blocks.SPRUCE_LOG, BASIC_GROWTH_MEDIA)

        .putAll(Blocks.STRIPPED_ACACIA_LOG, BASIC_GROWTH_MEDIA)
        .putAll(Blocks.STRIPPED_BIRCH_LOG, BASIC_GROWTH_MEDIA)
        .putAll(Blocks.STRIPPED_DARK_OAK_LOG, BASIC_GROWTH_MEDIA)
        .putAll(Blocks.STRIPPED_JUNGLE_LOG, BASIC_GROWTH_MEDIA)
        .putAll(Blocks.STRIPPED_OAK_LOG, BASIC_GROWTH_MEDIA)
        .putAll(Blocks.STRIPPED_SPRUCE_LOG, BASIC_GROWTH_MEDIA)

        .putAll(Blocks.CRIMSON_STEM, Blocks.CRIMSON_NYLIUM, Blocks.NETHERRACK)
        .putAll(Blocks.WARPED_STEM, Blocks.WARPED_NYLIUM, Blocks.NETHERRACK)
        .putAll(Blocks.STRIPPED_CRIMSON_STEM, Blocks.CRIMSON_NYLIUM, Blocks.NETHERRACK)
        .putAll(Blocks.STRIPPED_WARPED_STEM, Blocks.WARPED_NYLIUM, Blocks.NETHERRACK)

        .putAll(Blocks.MUSHROOM_STEM, Blocks.MYCELIUM, Blocks.DIRT)
        .build()

internal val LOGS_TO_LOGS_VARIANTS = ImmutableSetMultimap.builder<Block, Block>()
        .putAll(Blocks.ACACIA_LOG, Blocks.ACACIA_LOG, Blocks.STRIPPED_ACACIA_LOG)
        .putAll(Blocks.BIRCH_LOG, Blocks.BIRCH_LOG, Blocks.STRIPPED_BIRCH_LOG)
        .putAll(Blocks.CRIMSON_STEM, Blocks.CRIMSON_STEM, Blocks.STRIPPED_CRIMSON_STEM)
        .putAll(Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_LOG, Blocks.STRIPPED_DARK_OAK_LOG)
        .putAll(Blocks.JUNGLE_LOG, Blocks.JUNGLE_LOG, Blocks.STRIPPED_JUNGLE_LOG)
        .putAll(Blocks.OAK_LOG, Blocks.OAK_LOG, Blocks.STRIPPED_OAK_LOG)
        .putAll(Blocks.SPRUCE_LOG, Blocks.SPRUCE_LOG, Blocks.STRIPPED_SPRUCE_LOG)
        .putAll(Blocks.WARPED_STEM, Blocks.WARPED_STEM, Blocks.STRIPPED_WARPED_STEM)

        .putAll(Blocks.STRIPPED_ACACIA_LOG, Blocks.ACACIA_LOG, Blocks.STRIPPED_ACACIA_LOG)
        .putAll(Blocks.STRIPPED_BIRCH_LOG, Blocks.BIRCH_LOG, Blocks.STRIPPED_BIRCH_LOG)
        .putAll(Blocks.STRIPPED_CRIMSON_STEM, Blocks.CRIMSON_STEM, Blocks.STRIPPED_CRIMSON_STEM)
        .putAll(Blocks.STRIPPED_DARK_OAK_LOG, Blocks.DARK_OAK_LOG, Blocks.STRIPPED_DARK_OAK_LOG)
        .putAll(Blocks.STRIPPED_JUNGLE_LOG, Blocks.JUNGLE_LOG, Blocks.STRIPPED_JUNGLE_LOG)
        .putAll(Blocks.STRIPPED_OAK_LOG, Blocks.OAK_LOG, Blocks.STRIPPED_OAK_LOG)
        .putAll(Blocks.STRIPPED_SPRUCE_LOG, Blocks.SPRUCE_LOG, Blocks.STRIPPED_SPRUCE_LOG)
        .putAll(Blocks.STRIPPED_WARPED_STEM, Blocks.WARPED_STEM, Blocks.STRIPPED_WARPED_STEM)

        .putAll(Blocks.MUSHROOM_STEM, Blocks.MUSHROOM_STEM)
        .build()

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

internal val LEAF_NEXT_VECS = setOf(
        Vec3i(1, 0, 0),
        Vec3i(-1, 0, 0),
        Vec3i(0, 0, 1),
        Vec3i(0, 0, -1),
        Vec3i(0, 1, 0),
        Vec3i(0, -1, 0),
)

internal val WART_NEXT_VECS = setOf(
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

internal val MUSHROOM_NEXT_VECS = setOf(
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

internal val ROOF_MEDIA_TO_SEARCH_VECS = ImmutableSetMultimap.builder<Block, Vec3i>()
        .putAll(Blocks.ACACIA_LEAVES, LEAF_NEXT_VECS)
        .putAll(Blocks.AZALEA_LEAVES, LEAF_NEXT_VECS)
        .putAll(Blocks.BIRCH_LEAVES, LEAF_NEXT_VECS)
        .putAll(Blocks.BROWN_MUSHROOM_BLOCK, MUSHROOM_NEXT_VECS)
        .putAll(Blocks.NETHER_WART_BLOCK, WART_NEXT_VECS)
        .putAll(Blocks.DARK_OAK_LEAVES, LEAF_NEXT_VECS)
        .putAll(Blocks.JUNGLE_LEAVES, LEAF_NEXT_VECS)
        .putAll(Blocks.OAK_LEAVES, LEAF_NEXT_VECS)
        .putAll(Blocks.RED_MUSHROOM_BLOCK, MUSHROOM_NEXT_VECS)
        .putAll(Blocks.SPRUCE_LEAVES, LEAF_NEXT_VECS)
        .putAll(Blocks.WARPED_WART_BLOCK, WART_NEXT_VECS)
        .build()

internal const val MAX_DETECTION_DEPTH = 32
internal const val MAX_LEAF_SEARCH_DEPTH = 6

internal fun Vec3i.isOpposedAlongADimension(otherVec: Vec3i): Boolean {
    return abs(x + otherVec.x) != abs(x) + abs(otherVec.x)
            || abs(y + otherVec.y) != abs(y) + abs(otherVec.y)
            || abs(z + otherVec.z) != abs(z) + abs(otherVec.z)
}

internal fun Block.isLog() = LOG_TYPES.contains(this)
