package us.sodiumlabs.trees

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i
import net.minecraft.world.World
import us.sodiumlabs.utils.TickScheduler

class TreeFeller(private val tickScheduler: TickScheduler): PlayerBlockBreakEvents.Before {
    override fun beforeBlockBreak(
            world: World?,
            player: PlayerEntity?,
            blockPosition: BlockPos?,
            blockState: BlockState?,
            blockEntity: BlockEntity?
    ): Boolean {
        if (blockState != null && blockPosition != null && world != null) {
            val block = blockState.block ?: return true

            if (block.isLog() && detectIfTree(world, blockPosition, block)) {
                val roofMedia = LOGS_TO_ROOF_MEDIA[block]
                val logVariants = LOGS_TO_LOGS_VARIANTS[block]
                scheduleChopTasks(TREE_CAPITATOR_INIT_VECS, world, roofMedia, logVariants, blockPosition)
            }
        }
        return true
    }

    private fun scheduleChopTasks(
            vectors: Set<Vec3i>,
            world: World,
            roofMedia: Set<Block>,
            branchMediaSet: Set<Block>,
            blockPosition: BlockPos,
            depth: Int = 0
    ) {
        if (depth > MAX_DETECTION_DEPTH) return

        val positions = vectors.map { blockPosition.add(it) }
        positions
                .filter { branchMediaSet.contains(world.getBlockState(it).block) }
                .forEach { scheduleChopTask(world, roofMedia, branchMediaSet, it, depth) }

        positions
                .filter { roofMedia.contains(world.getBlockState(it).block) }
                .forEach {
                    val block = world.getBlockState(it).block
                    scheduleRoofBreak(ROOF_MEDIA_TO_SEARCH_VECS[block], world, roofMedia, branchMediaSet, it)
                }
    }

    private fun scheduleRoofBreaks(
            searchVectors: Set<Vec3i>,
            world: World,
            roofMediaSet: Set<Block>,
            branchMediaSet: Set<Block>,
            blockPosition: BlockPos,
            depth: Int
    ) {
        if (depth > MAX_LEAF_SEARCH_DEPTH) return

        searchVectors.map { blockPosition.add(it) }
                .filter { roofMediaSet.contains(world.getBlockState(it).block) }
                .forEach { scheduleRoofBreak(searchVectors, world, roofMediaSet, branchMediaSet, it) }
    }

    private fun scheduleRoofBreak(
            searchVectors: Set<Vec3i>,
            world: World,
            roofMediaSet: Set<Block>,
            branchMediaSet: Set<Block>,
            blockPosition: BlockPos,
            depth: Int = 0
    ) {
        tickScheduler.addScheduledTask(Runnable{
            if (roofMediaSet.contains(world.getBlockState(blockPosition).block)
                    && !roofBranchSearch(searchVectors, world, roofMediaSet, branchMediaSet, blockPosition)
            ) {
                world.breakBlock(blockPosition, true)
                scheduleRoofBreaks(searchVectors, world, roofMediaSet, branchMediaSet, blockPosition, depth + 1)
            }
        })
    }

    private fun roofBranchSearch(
            nextVectors: Set<Vec3i>,
            world: World,
            roofMediaSet: Set<Block>,
            branchMediaSet: Set<Block>,
            blockPosition: BlockPos,
            depth: Int = 0,
            blockPositionCache: MutableMap<BlockPos, Boolean> = mutableMapOf()
    ): Boolean {
        return blockPositionCache.getOrPut(blockPosition) {
            val block = world.getBlockState(blockPosition).block

            if (branchMediaSet.contains(block)) {
                true
            } else if (depth >= MAX_LEAF_SEARCH_DEPTH || !roofMediaSet.contains(block)) {
                false
            } else {
                val out = nextVectors
                        .map { blockPosition.add(it) to it }
                        .any {
                            roofBranchSearch(nextVectors.filterNot { n -> it.second.isOpposedAlongADimension(n) }.toSet(),
                                    world, roofMediaSet, branchMediaSet, it.first, depth + 1, blockPositionCache)
                        }
                out
            }
        }
    }

    private fun scheduleChopTask(
            world: World,
            leafMediaSet: Set<Block>,
            branchMediaSet: Set<Block>,
            blockPosition: BlockPos,
            depth: Int
    ) {
        tickScheduler.addScheduledTask(Runnable{
            if (branchMediaSet.contains(world.getBlockState(blockPosition).block)) {
                world.breakBlock(blockPosition, true)
                scheduleChopTasks(TREE_CAPITATOR_NEXT_VECS, world, leafMediaSet, branchMediaSet, blockPosition, depth + 1)
            }
        })
    }

    private fun detectIfTree(world: World, blockPosition: BlockPos, block: Block): Boolean =
            noSiblingSupports(world, blockPosition, block) &&
            aboveGrowthMedia(world, blockPosition, block) &&
            belowRoofMedia(world, blockPosition, block)

    private fun belowRoofMedia(world: World, blockPosition: BlockPos, block: Block): Boolean {
        val roofMedia = LOGS_TO_ROOF_MEDIA[block]
        val logVariants = LOGS_TO_LOGS_VARIANTS[block]
        return UPPER_DETECTION_VECTORS.map { blockPosition.add(it) }
                .any { treeDetectionCombinator(roofMedia, logVariants, UPPER_DETECTION_VECTORS, world, it, 0) }
    }

    private fun aboveGrowthMedia(world: World, blockPosition: BlockPos, block: Block): Boolean {
        val growthMedia = LOGS_TO_GROWTH_MEDIA[block]
        val logVariants = LOGS_TO_LOGS_VARIANTS[block]
        return LOWER_DETECTION_VECTORS.map { blockPosition.add(it) }
                .any { treeDetectionCombinator(growthMedia, logVariants, LOWER_DETECTION_VECTORS, world, it, 0) }
    }

    private fun treeDetectionCombinator(
            endMediaSet: Set<Block>,
            branchMediaSet: Set<Block>,
            detectionVectors: Set<Vec3i>,
            world: World,
            position: BlockPos,
            depth: Int
    ): Boolean {
        val blockHere = world.getBlockState(position).block

        return endMediaSet.contains(blockHere) ||
            (
                depth < MAX_DETECTION_DEPTH
                    && branchMediaSet.contains(blockHere)
                    && detectionVectors.map { position.add(it) }
                        .any {
                            treeDetectionCombinator(endMediaSet, branchMediaSet, detectionVectors, world, it, depth + 1)
                        }
            )
    }

    private fun noSiblingSupports(world: World, blockPosition: BlockPos, block: Block): Boolean =
            SIBLING_SUPPORT_VECTORS.stream()
                    .map { world.getBlockState(blockPosition.add(it)).block }
                    .allMatch { block != it }
}
