package us.sodiumlabs.villager;

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
import us.sodiumlabs.NameService;

@Mixin(VillagerEntity.class)
public abstract class VillagerMixin extends MerchantEntity {

    public VillagerMixin(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At("HEAD"), method = "initialize")
    private void initialize(ServerWorldAccess serverWorldAccess, LocalDifficulty localDifficulty, SpawnReason spawnReason, EntityData entityData, NbtCompound nbtCompound, CallbackInfoReturnable<EntityData> returnable) {
        String name = NameService.Companion.getNameServiceInstance().getName();
        this.setCustomName(Text.of(name));
        this.setCustomNameVisible(true);
    }

    /* code for if I want to add parentage
    Optional<String> motherUuid = Optional.empty();
    Optional<String> fatherUuid = Optional.empty();
    @Inject(at = @At("RETURN"), method = "writeCustomDataToNbt")
    private void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo info) {
        motherUuid.ifPresent(s -> nbt.putString("mother", s));
        fatherUuid.ifPresent(s -> nbt.putString("father", s));
    }

    @Inject(at = @At("RETURN"), method = "readCustomDataFromNbt")
    private void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo info) {
        if(nbt.contains("mother")) motherUuid = Optional.of(nbt.getString("mother"));
        if(nbt.contains("father")) fatherUuid = Optional.of(nbt.getString("father"));
    }*/
}
