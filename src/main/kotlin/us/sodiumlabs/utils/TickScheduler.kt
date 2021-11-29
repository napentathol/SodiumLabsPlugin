package us.sodiumlabs.utils;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.EndTick;
import net.minecraft.server.MinecraftServer

private const val MAX_TO_RUN_PER_TICK = 32
private const val MAX_TO_SCHEDULE = MAX_TO_RUN_PER_TICK * 16

class TickScheduler: EndTick {
    private val scheduledTasks = mutableListOf<Runnable>()
    private val lock = object {}

    override fun onEndTick(server: MinecraftServer) {
        synchronized(lock) {
            val tasksToRun = scheduledTasks.subList(0, scheduledTasks.size.coerceAtMost(MAX_TO_RUN_PER_TICK)).toList()
            scheduledTasks.removeAll(tasksToRun)
            tasksToRun
        }.forEach(Runnable::run)
    }

    fun addScheduledTask(vararg tasks: Runnable) {
        synchronized(lock) {
            scheduledTasks.addAll(tasks.sliceArray(0 until tasks.size.coerceAtMost(MAX_TO_SCHEDULE - scheduledTasks.size)))
        }
    }
}
