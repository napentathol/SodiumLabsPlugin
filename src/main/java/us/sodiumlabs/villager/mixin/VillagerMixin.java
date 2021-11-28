package us.sodiumlabs.villager.mixin;

import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import us.sodiumlabs.villager.NameService;

@Mixin(VillagerEntity.class)
public abstract class VillagerMixin extends MerchantEntity {

    public VillagerMixin(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At("HEAD"), method = "initialize")
    private void initialize(ServerWorldAccess serverWorldAccess, LocalDifficulty localDifficulty, SpawnReason spawnReason, EntityData entityData, NbtCompound nbtCompound, CallbackInfoReturnable<EntityData> returnable) {
        final NameService nameService = NameService.Companion.getNameServiceInstance()
            .orElseThrow(() -> new RuntimeException("No server to get the seed from"));
        final String name = nameService.getName();
        this.setCustomName(Text.of(name));
        this.setCustomNameVisible(true);
    }
}
