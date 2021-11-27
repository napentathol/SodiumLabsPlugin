package us.sodiumlabs

import java.util.Random

class NameService(random: Random) {
    companion object {
        val CONSONANT_GROUPS = setOf<String>()
        private lateinit var nameService: NameService

        fun getNameServiceInstance(): NameService {
            if(!::nameService.isInitialized) {
                nameService = NameService(Random())
            }
            return nameService
        }
    }

    fun getName(): String = "GenByNameService"
}