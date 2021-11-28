package us.sodiumlabs.villager

import com.google.common.collect.ImmutableMultiset
import us.sodiumlabs.ServerProvider
import java.security.SecureRandom
import java.util.Optional
import java.util.Random
import kotlin.math.roundToInt

class NameService(private val dictionaryRandom: Random, private val nameRandom: Random) {

    companion object {
        val EDGE_CONSONANT_GROUPS = listOf(
                "'", "n", "t", "d",
                "s", "l",  "f", "m",
                "g", "ch", "b", "p",
                "k", "v", "h", "th",
                "sh", "j", "zh", "w",
                "q", "z", "rh", "y"
        ).toZipf()

        val CENTER_CONSONANT_GROUPS = listOf(
                "n", "t", "d", "s",
                "l", "w", "f", "m",
                "g", "ch", "b", "y",
                "p", "k", "rh", "v",
                "h", "th", "sh", "j",
                "zh", "q", "z", "'"
        ).toZipf()

        val VOWEL_GROUPS = listOf(
                "e", "a", "o", "i",
                "u", "ee", "ei", "oo",
                "er", "ar", "or", "ir",
                "ur", "eer", "oor"
        ).toZipf()

        private lateinit var nameService: NameService

        fun getNameServiceInstance(): Optional<NameService> {
            if (!::nameService.isInitialized) {
                val seed = ServerProvider.getServer().map { it.saveProperties.generatorOptions.seed }
                if(seed.isEmpty) return Optional.empty()

                nameService = NameService(Random(seed.get()), SecureRandom())
            }
            return Optional.of(nameService)
        }
    }

    private val villagerNames: List<String>
    init {
        villagerNames = (0 until 1000).map { generateName() }.toZipf()
    }

    fun getName(): String {
        val motherName = randomNameFromList(villagerNames)
        val fatherName = randomNameFromList(villagerNames)
        val name = randomNameFromList(villagerNames)

        return if (name == motherName || name == fatherName) {
            getName()
        } else if(motherName == fatherName) {
            "$name $motherName"
        } else {
            "$name $motherName-$fatherName"
        }
    }

    private fun randomNameFromList(list: List<String>) = list[nameRandom.nextInt(0, list.size)]

    private fun generateName(): String =
            (randomEdgeConsonant() + randomVowel() + randomCenterConsonant() + randomVowel() + randomEdgeConsonant())
                    .replace(Regex("^'|'\$"), "").replaceFirstChar { it.titlecase() }

    private fun randomEdgeConsonant(): String = EDGE_CONSONANT_GROUPS[dictionaryRandom.nextInt(0, EDGE_CONSONANT_GROUPS.size)]
    private fun randomCenterConsonant(): String = CENTER_CONSONANT_GROUPS[dictionaryRandom.nextInt(0, CENTER_CONSONANT_GROUPS.size)]
    private fun randomVowel(): String = VOWEL_GROUPS[dictionaryRandom.nextInt(0, VOWEL_GROUPS.size)]
}

private const val ZIPF_MAX = 420.0

private fun List<String>.toZipf(): List<String> {
    val multisetBuilder = ImmutableMultiset.builder<String>()
    for (i in 0 until this.size) {
        multisetBuilder.addCopies(this[i], (ZIPF_MAX / (i + 1)).roundToInt())
    }
    return multisetBuilder.build().toList()
}