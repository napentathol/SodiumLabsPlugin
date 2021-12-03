package us.sodiumlabs.villager.mixin;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.CarvedPumpkinBlock;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CarvedPumpkinBlock.class)
public abstract class CarvedPumpkinBlockMixin {

    @Shadow
    protected abstract BlockPattern getSnowGolemPattern();
    @Shadow
    protected abstract BlockPattern getIronGolemPattern();

    @Inject(at = @At("HEAD"), method = "trySpawnEntity", cancellable = true)
    private void trySpawnEntity(World world, BlockPos blockPos, CallbackInfo info) {
        if (world instanceof ServerWorld) {
            BlockPattern.Result result = this.getSnowGolemPattern().searchAround(world, blockPos);
            if (result != null) {
                for (int i = 0; i < this.getSnowGolemPattern().getHeight(); ++i) {
                    CachedBlockPosition cachedBlockPosition = result.translate(0, i, 0);
                    world.setBlockState(cachedBlockPosition.getBlockPos(), Blocks.AIR.getDefaultState(), 2);
                    world.syncWorldEvent(2001, cachedBlockPosition.getBlockPos(), Block.getRawIdFromState(cachedBlockPosition.getBlockState()));
                }

                SnowGolemEntity snowGolemEntity = EntityType.SNOW_GOLEM.create(world);
                BlockPos cachedBlockPosition = result.translate(0, 2, 0).getBlockPos();
                snowGolemEntity.refreshPositionAndAngles((double) cachedBlockPosition.getX() + 0.5D, (double) cachedBlockPosition.getY() + 0.05D, (double) cachedBlockPosition.getZ() + 0.5D, 0.0F, 0.0F);
                world.spawnEntity(snowGolemEntity);

                for (ServerPlayerEntity playerEntity : world.getNonSpectatingEntities(ServerPlayerEntity.class, snowGolemEntity.getBoundingBox().expand(5.0D))) {
                    Criteria.SUMMONED_ENTITY.trigger(playerEntity, snowGolemEntity);
                }

                for (int j = 0; j < this.getSnowGolemPattern().getHeight(); ++j) {
                    CachedBlockPosition serverPlayerEntity = result.translate(0, j, 0);
                    world.updateNeighbors(serverPlayerEntity.getBlockPos(), Blocks.AIR);
                }
            } else {
                result = this.getIronGolemPattern().searchAround(world, blockPos);
                if (result != null) {
                    for (int i = 0; i < this.getIronGolemPattern().getWidth(); ++i) {
                        for (int cachedBlockPosition = 0; cachedBlockPosition < this.getIronGolemPattern().getHeight(); ++cachedBlockPosition) {
                            CachedBlockPosition j = result.translate(i, cachedBlockPosition, 0);
                            world.setBlockState(j.getBlockPos(), Blocks.AIR.getDefaultState(), 2);
                            world.syncWorldEvent(2001, j.getBlockPos(), Block.getRawIdFromState(j.getBlockState()));
                        }
                    }

                    BlockPos pos = result.translate(1, 2, 0).getBlockPos();
                    IronGolemEntity ironGolemEntity = EntityType.IRON_GOLEM.create((ServerWorld) world, null, null, null, pos, SpawnReason.EVENT, false, false);
                    //noinspection ConstantConditions
                    ironGolemEntity.setPlayerCreated(true);
                    ironGolemEntity.refreshPositionAndAngles((double) pos.getX() + 0.5D, (double) pos.getY() + 0.05D, (double) pos.getZ() + 0.5D, 0.0F, 0.0F);
                    world.spawnEntity(ironGolemEntity);

                    for (ServerPlayerEntity playerEntity : world.getNonSpectatingEntities(ServerPlayerEntity.class, ironGolemEntity.getBoundingBox().expand(5.0D))) {
                        Criteria.SUMMONED_ENTITY.trigger(playerEntity, ironGolemEntity);
                    }

                    for (int j = 0; j < this.getIronGolemPattern().getWidth(); ++j) {
                        for (int height = 0; height < this.getIronGolemPattern().getHeight(); ++height) {
                            CachedBlockPosition cachedBlockPosition2 = result.translate(j, height, 0);
                            world.updateNeighbors(cachedBlockPosition2.getBlockPos(), Blocks.AIR);
                        }
                    }
                }
            }
            info.cancel();
        }
    }
}
