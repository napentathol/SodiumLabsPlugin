package us.sodiumlabs.villager

import us.sodiumlabs.ServerProvider
import us.sodiumlabs.utils.ZipfProvider
import java.security.SecureRandom
import kotlin.random.Random

class NameService(dictionarySeed: Long, nameSeed: Long) {
    private val dictRandom = Random(dictionarySeed)
    private val firstConsonantGroups = listOf(
            "'", "j", "m", "d", "k", "l", "c", "r", "t", "br", "s", "n", "g", "h", "sh", "b", "ch", "p", "w", "kr", "v",
            "tr", "st", "cl", "f", "gr", "fr", "chr", "z", "cr", "q", "pr"
    ).toZipf(seed = dictRandom.nextInt())

    private val centerConsonantGroups = listOf(
            "r", "l", "n", "m", "s", "c", "d", "st", "v", "t", "nn", "nd", "ll", "b", "sh", "ss", "nt", "k", "g", "th",
            "ndr", "ch", "q", "dr", "nc", "x", "ck", "lv", "mm", "ph", "ls", "ng", "br", "f", "lb", "z", "tr"
    ).toZipf(seed = dictRandom.nextInt(), exponent = .7)

    private val finalConsonantGroups = listOf(
            "'", "n", "l", "s", "ne", "d", "ll", "ce", "h", "ck", "m", "tte", "lle", "t", "nne", "th", "tt", "nce",
            "nn", "se", "nt", "le", "k", "c", "ld", "z", "ke", "nd", "de", "gh"
    ).toZipf(seed = dictRandom.nextInt(), exponent = 1.5)

    private val vowelGroups = listOf(
            "a", "e", "i", "o", "y", "ar", "er", "ia", "ie", "or", "u", "ey", "ea", "ay", "ee", "ai", "io", "ir", "ue",
            "ae", "au", "ei", "ui", "eo", "ier", "ua", "aur", "yr", "oe", "oy"
    ).toZipf(seed = dictRandom.nextInt(), exponent = 1.2)

    private val golemFirstConsonantGroups = listOf(
            "'", "m", "h", "sh", "zh", "j", "z", "n", "t", "g", "s", "b", "r", "d", "k", "p", "l", "c", "ch", "th"
    ).toZipf(seed = dictRandom.nextInt(), exponent = 0.5)

    private val golemSecondConsonantGroups = listOf(
            "b", "l", "m", "n", "sh", "d", "z", "ph", "h", "th", "s", "zh", "ch", "k", "mm", "bb", "g", "mr", "v", "lm",
            "br", "zr", "bd", "j", "t", "phr", "shb", "gd", "gg", "gl"
    ).toZipf(seed = dictRandom.nextInt(), exponent = 0.5)

    private val golemThirdConsonantGroups = listOf(
            "d", "m", "n", "g", "l", "sh", "'", "b", "s", "th", "z", "k", "r", "h", "st", "ch", "ph", "zh", "q", "t",
            "kh", "gl", "j", "v"
    ).toZipf(seed = dictRandom.nextInt(), exponent = 0.4)

    private val golemFourthConsonantGroups = listOf(
            "'", "m", "n", "h", "l", "sh", "s", "z", "d", "th", "zh", "ph", "b", "k", "g", "y"
    ).toZipf(seed = dictRandom.nextInt(), exponent = 0.5)

    private val golemFirstVowelGroups = listOf(
            "a", "e", "i", "o", "u", "er", "ar", "ai", "aa", "ur", "or", "ir", "aar", "ia", "y", "ear", "yr"
    ).toZipf(seed = dictRandom.nextInt())

    private val golemLastVowelGroups = listOf(
            "a", "e", "o", "u", "i", "ia", "ur", "ai", "ar", "ie", "ee", "er", "ea", "or", "ir"
    ).toZipf(seed = dictRandom.nextInt())

    private val syllableCounts = listOf(2, 3, 1, 4).toZipf(seed = dictRandom.nextInt(), exponent = 1.5)

    companion object {

        private lateinit var nameService: NameService

        fun getNameServiceInstance(): NameService {
            if (!::nameService.isInitialized) {
                val seed = ServerProvider.getServer()
                    .map { it.worldData.worldGenSettings().seed() }
                    .orElseThrow { RuntimeException("No server to get the seed from") }

                nameService = NameService(seed, SecureRandom().nextLong())
            }
            return nameService
        }
    }

    private val nameSet: MutableSet<String> = mutableSetOf()
    private val tokenSet: MutableSet<String> = mutableSetOf()
    private val villagerNames: ZipfProvider<String>
    private val golemFirstTokens: ZipfProvider<String>
    private val golemLastTokens: ZipfProvider<String>
    init {
        val r = Random(nameSeed)
        villagerNames = (0 until 1000).map { generateUniqueName() }.toZipf(exponent = 0.5, seed = r.nextInt())
        golemFirstTokens = (0 until 48).map { generateUniqueGolemFirstToken() }.toZipf(exponent = 0.5, seed = r.nextInt())
        golemLastTokens = (0 until 48).map { generateUniqueGolemLastToken() }.toZipf(exponent = 0.5, seed = r.nextInt())
    }

    fun getName(): String = villagerNames.getNextValue()
    fun getGolemName(): String = (golemFirstTokens.getNextValue() + golemLastTokens.getNextValue())
            .replace(Regex("^'|'\$"), "").replaceFirstChar { it.titlecase() }

    internal fun generateUniqueName(): String {
        val test = generateName()
        return if(nameSet.contains(test) || test.length < 4 || test.length > 10) {
            generateUniqueName()
        } else {
            nameSet.add(test)
            test
        }
    }

    private fun generateName(): String = when (syllableCounts.getNextValue()) {
        1 -> (randomFirstConsonant() + randomVowel() + randomFinalConsonant())
        2 -> (randomFirstConsonant() + randomVowel() + randomCenterConsonant() + randomVowel() + randomFinalConsonant())
        3 -> (randomFirstConsonant() + randomVowel() + randomCenterConsonant() + randomVowel() + randomCenterConsonant()
                + randomVowel() + randomFinalConsonant())
        else -> (randomFirstConsonant() + randomVowel() + randomCenterConsonant()
                + randomVowel() + randomCenterConsonant()
                + randomVowel() + randomCenterConsonant()
                + randomVowel() + randomFinalConsonant())
    }.replace(Regex("^'|'\$"), "").replaceFirstChar { it.titlecase() }

    private fun randomFirstConsonant(): String = firstConsonantGroups.getNextValue()
    private fun randomFinalConsonant(): String = finalConsonantGroups.getNextValue()
    private fun randomCenterConsonant(): String = centerConsonantGroups.getNextValue()
    private fun randomVowel(): String = vowelGroups.getNextValue()

    internal fun generateUniqueGolemFirstToken(): String {
        val test = generateGolemFirstToken()
        return if(tokenSet.contains(test) || test.length > 7) {
            generateUniqueGolemFirstToken()
        } else {
            tokenSet.add(test)
            test
        }
    }
    internal fun generateUniqueGolemLastToken(): String {
        val test = generateGolemLastToken()
        return if(tokenSet.contains(test) || test.length > 5) {
            generateUniqueGolemLastToken()
        } else {
            tokenSet.add(test)
            test
        }
    }

    private fun generateGolemFirstToken(): String = randomGolemFirstConsonant() + randomGolemFirstVowel() +
            randomGolemSecondConsonant() + randomGolemFirstVowel()
    private fun generateGolemLastToken(): String = randomGolemThirdConsonant() + randomGolemLastVowel() +
            randomGolemFourthConsonant()

    private fun randomGolemFirstConsonant(): String = golemFirstConsonantGroups.getNextValue()
    private fun randomGolemSecondConsonant(): String = golemSecondConsonantGroups.getNextValue()
    private fun randomGolemThirdConsonant(): String = golemThirdConsonantGroups.getNextValue()
    private fun randomGolemFourthConsonant(): String = golemFourthConsonantGroups.getNextValue()
    private fun randomGolemFirstVowel(): String = golemFirstVowelGroups.getNextValue()
    private fun randomGolemLastVowel(): String = golemLastVowelGroups.getNextValue()
}

private fun <E> List<E>.toZipf(exponent: Double = 1.0, seed: Int): ZipfProvider<E> {
    return ZipfProvider(this, exponent = exponent, seed = seed)
}
