package us.sodiumlabs.villager.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import us.sodiumlabs.villager.NameService;

@Mixin(Villager.class)
public abstract class VillagerMixin extends AbstractVillager {

    public VillagerMixin(EntityType<? extends Villager> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(at = @At("HEAD"), method = "finalizeSpawn")
    private void finalizeSpawn(
        ServerLevelAccessor serverWorldAccess,
        DifficultyInstance localDifficulty,
        MobSpawnType spawnReason,
        SpawnGroupData entityData,
        CompoundTag nbtCompound,
        CallbackInfoReturnable<SpawnGroupData> returnable
    ) {
        final NameService nameService = NameService.Companion.getNameServiceInstance();
        final String name = nameService.getName();
        this.setCustomName(new TextComponent(name));
        this.setCustomNameVisible(true);
    }

    @Inject(at = @At("HEAD"), method = "stopSleeping")
    private void inject_stopSleeping(CallbackInfo info) {
        this.heal(1.0f);
    }
}
