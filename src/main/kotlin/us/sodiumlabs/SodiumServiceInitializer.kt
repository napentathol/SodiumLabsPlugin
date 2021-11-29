package us.sodiumlabs

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import us.sodiumlabs.trees.TreeFeller
import us.sodiumlabs.utils.TickScheduler

@Suppress("unused")
class SodiumServiceInitializer: ModInitializer {
    override fun onInitialize() {
        val tickScheduler = TickScheduler()
        PlayerBlockBreakEvents.BEFORE.register(TreeFeller(tickScheduler))
        ServerTickEvents.END_SERVER_TICK.register(tickScheduler)
    }
}