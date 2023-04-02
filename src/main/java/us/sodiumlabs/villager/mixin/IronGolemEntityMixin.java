package us.sodiumlabs.villager.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import us.sodiumlabs.villager.NameService;

@Mixin(IronGolem.class)
public abstract class IronGolemEntityMixin extends AbstractGolem {
    protected IronGolemEntityMixin(EntityType<? extends AbstractGolem> entityType, Level world) {
        super(entityType, world);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(
        ServerLevelAccessor serverWorldAccess,
        DifficultyInstance localDifficulty,
        MobSpawnType spawnReason,
        SpawnGroupData entityData,
        CompoundTag nbtCompound
    ) {
        this.setCustomName(new TextComponent(NameService.Companion.getNameServiceInstance().getGolemName()));
        this.setCustomNameVisible(true);

        return super.finalizeSpawn(serverWorldAccess, localDifficulty, spawnReason, entityData, nbtCompound);
    }
}
