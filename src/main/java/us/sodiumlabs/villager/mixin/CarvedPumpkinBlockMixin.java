package us.sodiumlabs.villager.mixin;

import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CarvedPumpkinBlock.class)
public abstract class CarvedPumpkinBlockMixin {

    @Shadow
    protected abstract BlockPattern getOrCreateSnowGolemFull();

    @Shadow
    protected abstract BlockPattern getOrCreateIronGolemFull();

    /*
    might need this later

    @Inject(at = @At("HEAD"), method = "trySpawnEntity", cancellable = true)
    private void trySpawnEntity(World world, BlockPos blockPos, CallbackInfo info) {
    */
}
