package us.sodiumlabs.villager

import org.junit.jupiter.api.Test

internal class NameServiceTest {

    @Test
    fun getName() {
        val nameService = NameService(1, 2)
        (1..100).map { nameService.generateUniqueName() }
                .forEach(::println)
    }

    @Test
    fun getGolemName() {
        val nameService = NameService(1, 2)
        (1..100).map { nameService.getGolemName() }
                .forEach(::println)
    }
}