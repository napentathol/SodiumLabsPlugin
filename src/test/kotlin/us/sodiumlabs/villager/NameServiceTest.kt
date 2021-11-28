package us.sodiumlabs.villager

import org.junit.jupiter.api.Test

internal class NameServiceTest {

    @Test
    fun getName() {
        val nameService = NameService(1, 2)
        (1..100).map { nameService.getName() }
                .sorted()
                .forEach(::println)
    }
}