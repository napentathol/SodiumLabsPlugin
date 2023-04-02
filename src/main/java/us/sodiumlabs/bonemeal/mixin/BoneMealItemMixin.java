package us.sodiumlabs.bonemeal.mixin;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.TallFlowerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(BoneMealItem.class)
public class BoneMealItemMixin {
    private static final int MAX_SEARCH_RADIUS = 5;
    private static final double SUCCESS_CHANCE = 0.1;

    private static final ImmutableSet<Block> BANNED_FLOWER_BLOCKS = ImmutableSet.of(
        Blocks.WITHER_ROSE
    );

    @Inject(at = @At("HEAD"), method = "useOn", cancellable = true)
    private void inject_useOn(UseOnContext itemUsageContext, CallbackInfoReturnable<InteractionResult> infoReturnable) {
        final Level world = itemUsageContext.getLevel();
        final BlockPos usageLocation = itemUsageContext.getClickedPos();
        final BlockState blockState = world.getBlockState(usageLocation);
        final Block block = blockState.getBlock();

        if (
            (block instanceof FlowerBlock || block instanceof TallFlowerBlock)
                && !BANNED_FLOWER_BLOCKS.contains(block)
        ) {
            for(int i = -MAX_SEARCH_RADIUS; i <= MAX_SEARCH_RADIUS; i++) {
                int maxj = (MAX_SEARCH_RADIUS - Math.abs(i));
                for(int j = -maxj; j <= maxj; j++) {
                    int maxk = (MAX_SEARCH_RADIUS - Math.abs(i) - Math.abs(j));
                    for(int k = -maxk; k <= maxk; k++) {
                        randomlyPlaceFlower(world, blockState, usageLocation.offset(i,j,k));
                    }
                }
            }
            BoneMealItem.addGrowthParticles(world, usageLocation.below(), 0);
            itemUsageContext.getItemInHand().shrink(1);
            infoReturnable.setReturnValue(InteractionResult.sidedSuccess(world.isClientSide));
        }
    }

    private void randomlyPlaceFlower(Level world, BlockState blockState, BlockPos position) {
        final BlockState positionState = world.getBlockState(position);
        final BlockState groundState = world.getBlockState(position.below());
        final Random random = world.getRandom();
        if (Blocks.AIR.equals(positionState.getBlock())
            && Blocks.GRASS_BLOCK.equals(groundState.getBlock())
            && random.nextDouble() < SUCCESS_CHANCE
        ) {
            if (blockState.getBlock() instanceof TallFlowerBlock) {
                if (Blocks.AIR.equals(world.getBlockState(position.above()).getBlock())) {
                    world.setBlockAndUpdate(position, blockState.setValue(TallFlowerBlock.HALF, DoubleBlockHalf.LOWER));
                    world.setBlockAndUpdate(
                        position.above(), blockState.setValue(TallFlowerBlock.HALF, DoubleBlockHalf.UPPER));
                    if (!world.isClientSide) {
                        world.levelEvent(1505, position.below(), 0);
                    }
                }
            } else {
                world.setBlockAndUpdate(position, blockState);
                if (!world.isClientSide) {
                    world.levelEvent(1505, position.below(), 0);
                }
            }
        }
    }
}
