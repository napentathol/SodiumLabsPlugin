package us.sodiumlabs

import org.junit.jupiter.api.Test
import java.security.SecureRandom

internal class NameServiceTest {

    @Test
    fun getName() {
        val nameService = NameService(SecureRandom(), SecureRandom())
        (1..1000).map { nameService.getName() }
                .sorted()
                .forEach(::println)
    }
}