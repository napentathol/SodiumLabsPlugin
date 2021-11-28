package us.sodiumlabs.utils

import org.apache.commons.math3.distribution.ZipfDistribution
import org.apache.commons.math3.random.JDKRandomGenerator

class ZipfProvider<T> (private val list: List<T>, exponent: Double = 1.0, seed: Int) {
    val zipfDistribution: ZipfDistribution
    init {
        zipfDistribution = ZipfDistribution(JDKRandomGenerator(seed), list.size, exponent)
    }

    fun getNextValue(): T = list[zipfDistribution.sample() - 1]
}