package us.sodiumlabs.creeper.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PowerableMob;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Creeper.class)
public abstract class CreeperMixin extends LivingEntity implements PowerableMob {
    @Shadow
    private int explosionRadius;

    protected CreeperMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(at = @At("HEAD"), method = "explodeCreeper", cancellable = true)
    private void explodeCreeper(final CallbackInfo info) {
        if (!this.level.isClientSide) {
            Explosion.BlockInteraction blockInteraction = Explosion.BlockInteraction.NONE;
            float f = this.isPowered() ? 2.0F : 1.0F;
            this.dead = true;
            this.level.explode(
                this,
                this.getX(), this.getY(), this.getZ(),
                (float)this.explosionRadius * f, blockInteraction
            );
            this.discard();
            this.spawnLingeringCloud();
        }
        info.cancel();
    }

    @Invoker("spawnLingeringCloud")
    protected abstract void spawnLingeringCloud();
}
