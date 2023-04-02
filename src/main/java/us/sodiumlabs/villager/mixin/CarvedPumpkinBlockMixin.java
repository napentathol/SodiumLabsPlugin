package us.sodiumlabs.villager.mixin;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CarvedPumpkinBlock.class)
public abstract class CarvedPumpkinBlockMixin {

    @Shadow
    protected abstract BlockPattern getOrCreateSnowGolemFull();

    @Shadow
    protected abstract BlockPattern getOrCreateIronGolemFull();

    private void doSetPosition(Level world, AbstractGolem golem, BlockPos cachedBlockPosition) {
        golem.moveTo(
            (double) cachedBlockPosition.getX() + 0.5D,
            (double) cachedBlockPosition.getY() + 0.05D,
            (double) cachedBlockPosition.getZ() + 0.5D,
            0.0F, 0.0F
        );
        world.addFreshEntity(golem);

        for (ServerPlayer playerEntity : world.getEntitiesOfClass(ServerPlayer.class, golem.getBoundingBox().inflate(5.0D))) {
            CriteriaTriggers.SUMMONED_ENTITY.trigger(playerEntity, golem);
        }
    }

    @Inject(at = @At("HEAD"), method = "trySpawnGolem", cancellable = true)
    private void trySpawnGolem(Level world, BlockPos blockPos, CallbackInfo info) {
        // only work on server levels!
        if (world instanceof ServerLevel) {
            BlockPattern.BlockPatternMatch result = this.getOrCreateSnowGolemFull().find(world, blockPos);
            if (result != null) {
                for (int i = 0; i < this.getOrCreateSnowGolemFull().getHeight(); ++i) {
                    BlockInWorld cachedBlockPosition = result.getBlock(0, i, 0);
                    world.setBlock(cachedBlockPosition.getPos(), Blocks.AIR.defaultBlockState(), 2);
                    world.levelEvent(2001, cachedBlockPosition.getPos(), Block.getId(cachedBlockPosition.getState()));
                }

                SnowGolem snowGolemEntity = EntityType.SNOW_GOLEM.create(world);
                BlockPos cachedBlockPosition = result.getBlock(0, 2, 0).getPos();
                //noinspection ConstantConditions
                doSetPosition(world, snowGolemEntity, cachedBlockPosition);

                for (int j = 0; j < this.getOrCreateSnowGolemFull().getHeight(); ++j) {
                    BlockInWorld serverPlayerEntity = result.getBlock(0, j, 0);
                    world.blockUpdated(serverPlayerEntity.getPos(), Blocks.AIR);
                }
            } else {
                result = this.getOrCreateIronGolemFull().find(world, blockPos);
                if (result != null) {
                    for (int i = 0; i < this.getOrCreateIronGolemFull().getWidth(); ++i) {
                        for (
                            int cachedBlockPosition = 0;
                            cachedBlockPosition < this.getOrCreateIronGolemFull().getHeight();
                            ++cachedBlockPosition
                        ) {
                            BlockInWorld blockInWorld = result.getBlock(i, cachedBlockPosition, 0);
                            world.setBlock(blockInWorld.getPos(), Blocks.AIR.defaultBlockState(), 2);
                            world.levelEvent(2001, blockInWorld.getPos(), Block.getId(blockInWorld.getState()));
                        }
                    }

                    BlockPos pos = result.getBlock(1, 2, 0).getPos();
                    // ++++ change the IRON_GOLEM create method so that it calls trySpawnGolem ++++
                    IronGolem ironGolemEntity = EntityType.IRON_GOLEM.create(
                        (ServerLevel) world, null, null, null, pos, MobSpawnType.EVENT, false, false);
                    // ---- change the IRON_GOLEM create method so that it calls trySpawnGolem ----
                    //noinspection ConstantConditions
                    ironGolemEntity.setPlayerCreated(true);
                    doSetPosition(world, ironGolemEntity, pos);

                    for (int j = 0; j < this.getOrCreateIronGolemFull().getWidth(); ++j) {
                        for (int height = 0; height < this.getOrCreateIronGolemFull().getHeight(); ++height) {
                            BlockInWorld cachedBlockPosition2 = result.getBlock(j, height, 0);
                            world.blockUpdated(cachedBlockPosition2.getPos(), Blocks.AIR);
                        }
                    }
                }
            }
            info.cancel();
        }
    }
}
