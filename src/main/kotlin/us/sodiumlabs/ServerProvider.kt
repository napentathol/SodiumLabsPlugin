package us.sodiumlabs

import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.Minecraft
import net.minecraft.server.MinecraftServer
import java.util.Optional

object ServerProvider {
    fun getServer(): Optional<MinecraftServer> {
        return if (FabricLoader.getInstance().environmentType == EnvType.CLIENT) {
            Optional.ofNullable(Minecraft.getInstance().singleplayerServer)
        } else {
            Optional.of(FabricLoader.getInstance().gameInstance as MinecraftServer)
        }
    }
}
