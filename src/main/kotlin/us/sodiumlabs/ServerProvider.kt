package us.sodiumlabs

import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.server.MinecraftServer
import java.util.Optional

object ServerProvider {
    fun getServer(): Optional<MinecraftServer> {
        return if (FabricLoader.getInstance().environmentType == EnvType.CLIENT) {
            Optional.ofNullable(MinecraftClient.getInstance().server)
        } else {
            Optional.of(FabricLoader.getInstance().gameInstance as MinecraftServer)
        }
    }
}