package us.sodiumlabs.enderman.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.entity.monster.EnderMan$EndermanTakeBlockGoal")
public class EndermanPickUpMixin {
    @Inject(at = @At("HEAD"), method = "canUse", cancellable = true)
    private void canStart(CallbackInfoReturnable<Boolean> infoReturnable) {
        infoReturnable.setReturnValue(false);
    }
}
