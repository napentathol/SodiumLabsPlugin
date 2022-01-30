package us.sodiumlabs.bonemeal.mixin;

import com.google.common.collect.ImmutableSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TallPlantBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(BoneMealItem.class)
public class BoneMealItemMixin {
    private static final int MAX_SEARCH_RADIUS = 5;
    private static final double SUCCESS_CHANCE = 0.1;

    private static final ImmutableSet<Block> FLOWER_BLOCKS = ImmutableSet.of(
        Blocks.POPPY, Blocks.BLUE_ORCHID, Blocks.ALLIUM, Blocks.AZURE_BLUET, Blocks.RED_TULIP, Blocks.ORANGE_TULIP,
        Blocks.WHITE_TULIP, Blocks.PINK_TULIP, Blocks.OXEYE_DAISY, Blocks.CORNFLOWER, Blocks.LILY_OF_THE_VALLEY,
        Blocks.SUNFLOWER, Blocks.LILAC, Blocks.ROSE_BUSH, Blocks.PEONY, Blocks.LARGE_FERN, Blocks.FERN,
        Blocks.TALL_GRASS, Blocks.GRASS, Blocks.DANDELION
    );

    @Inject(at = @At("HEAD"), method = "useOnBlock", cancellable = true)
    private void inject_useOnBlock(ItemUsageContext itemUsageContext, CallbackInfoReturnable<ActionResult> infoReturnable) {
        final World world = itemUsageContext.getWorld();
        final BlockPos usageLocation = itemUsageContext.getBlockPos();
        final BlockState blockState = world.getBlockState(usageLocation);
        if (FLOWER_BLOCKS.contains(blockState.getBlock())) {
            for(int i = -MAX_SEARCH_RADIUS; i <= MAX_SEARCH_RADIUS; i++) {
                int maxj = (MAX_SEARCH_RADIUS - Math.abs(i));
                for(int j = -maxj; j <= maxj; j++) {
                    int maxk = (MAX_SEARCH_RADIUS - Math.abs(i) - Math.abs(j));
                    for(int k = -maxk; k <= maxk; k++) {
                        randomlyPlaceFlower(world, blockState, usageLocation.add(i,j,k));
                    }
                }
            }
            BoneMealItem.createParticles(world, usageLocation.down(), 0);
            itemUsageContext.getStack().decrement(1);
            infoReturnable.setReturnValue(ActionResult.success(world.isClient));
        }
    }

    private void randomlyPlaceFlower(World world, BlockState blockState, BlockPos position) {
        final BlockState positionState = world.getBlockState(position);
        final BlockState groundState = world.getBlockState(position.down());
        final Random random = world.getRandom();
        if (Blocks.AIR.equals(positionState.getBlock())
            && Blocks.GRASS_BLOCK.equals(groundState.getBlock())
            && random.nextDouble() < SUCCESS_CHANCE
        ) {
            if (blockState.getBlock() instanceof TallPlantBlock) {
                if (Blocks.AIR.equals(world.getBlockState(position.up()).getBlock())) {
                    world.setBlockState(position, blockState.with(Properties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER));
                    world.setBlockState(
                        position.up(), blockState.with(Properties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER));
                    if (!world.isClient) {
                        world.syncWorldEvent(1505, position.down(), 0);
                    }
                }
            } else {
                world.setBlockState(position, blockState);
                if (!world.isClient) {
                    world.syncWorldEvent(1505, position.down(), 0);
                }
            }
        }
    }
}
