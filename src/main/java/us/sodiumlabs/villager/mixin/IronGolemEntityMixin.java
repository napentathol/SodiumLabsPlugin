package us.sodiumlabs.villager.mixin;

import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import us.sodiumlabs.villager.NameService;

@Mixin(IronGolemEntity.class)
public abstract class IronGolemEntityMixin extends GolemEntity {
    protected IronGolemEntityMixin(EntityType<? extends GolemEntity> entityType, World world) {
        super(entityType, world);
    }

    @Nullable
    @Override
    public EntityData initialize(ServerWorldAccess serverWorldAccess, LocalDifficulty localDifficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound nbtCompound) {
        this.setCustomName(Text.of(NameService.Companion.getNameServiceInstance().getGolemName()));
        this.setCustomNameVisible(true);

        return super.initialize(serverWorldAccess, localDifficulty, spawnReason, entityData, nbtCompound);
    }
}
