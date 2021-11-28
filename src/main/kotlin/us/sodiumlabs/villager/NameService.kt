package us.sodiumlabs.villager

import us.sodiumlabs.ServerProvider
import us.sodiumlabs.utils.ZipfProvider
import java.security.SecureRandom

class NameService(dictionarySeed: Int, nameSeed: Int) {
    private val edgeConsonantGroups = listOf(
            "'", "n", "t", "d",
            "s", "l",  "f", "m",
            "g", "ch", "b", "p",
            "k", "v", "h", "th",
            "sh", "j", "zh", "z",
            "q", "w", "rh", "y"
    ).toZipf(seed = dictionarySeed)

    private val centerConsonantGroups = listOf(
            "n", "t", "d", "s",
            "l", "w", "f", "m",
            "g", "ch", "b", "y",
            "p", "k", "rh", "v",
            "h", "th", "sh", "j",
            "zh", "q", "z", "'"
    ).toZipf(seed = dictionarySeed)

    private val vowelGroups = listOf(
            "e", "a", "o", "i",
            "u", "ee", "ei", "oo",
            "er", "ar", "or", "ir",
            "ur", "eer", "oor"
    ).toZipf(seed = dictionarySeed)

    companion object {

        private lateinit var nameService: NameService

        fun getNameServiceInstance(): NameService {
            if (!::nameService.isInitialized) {
                val seed = ServerProvider.getServer().map { it.saveProperties.generatorOptions.seed }
                        .orElseThrow { RuntimeException("No server to get the seed from") }

                nameService = NameService(seed.toInt(), SecureRandom().nextInt())
            }
            return nameService
        }
    }

    private val villagerNames: ZipfProvider<String>
    init {
        villagerNames = (0 until 1000).map { generateName() }.toZipf(exponent = 0.5, seed = nameSeed)
    }

    fun getName(): String = villagerNames.getNextValue()

    private fun generateName(): String =
            (randomEdgeConsonant() + randomVowel() + randomCenterConsonant() + randomVowel() + randomEdgeConsonant())
                    .replace(Regex("^'|'\$"), "").replaceFirstChar { it.titlecase() }

    private fun randomEdgeConsonant(): String = edgeConsonantGroups.getNextValue()
    private fun randomCenterConsonant(): String = centerConsonantGroups.getNextValue()
    private fun randomVowel(): String = vowelGroups.getNextValue()
}

private fun List<String>.toZipf(exponent: Double = 1.0, seed: Int): ZipfProvider<String> {
    return ZipfProvider(this, exponent = exponent, seed = seed)
}