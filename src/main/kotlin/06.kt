import java.math.BigInteger

data class LanternFish(val daysUntilSpawn: Int) {
    fun step(): List<LanternFish> =
        if (daysUntilSpawn == 0) listOf(LanternFish(6), LanternFish(8))
        else listOf(LanternFish(daysUntilSpawn - 1))
}

fun main() {
    val input = getPuzzleInput(6, 1)
    var fish = input.split(",").map { LanternFish(it.toInt()) }
    for (i in 1..80) {
        fish = fish.flatMap { it.step() }
    }
    println("There are ${fish.size} fish after 80 days.")

    var daysUntilSpawnToNumFish = input.split(",")
        .groupBy { it.toInt() }
        .mapValues { it.value.count().toBigInteger() }
    for (i in 1..(256)) {
        val newDay8Fish = daysUntilSpawnToNumFish.getOrDefault(0, BigInteger.ZERO)
        daysUntilSpawnToNumFish = daysUntilSpawnToNumFish
            .map { (if (it.key == 0) 6 else it.key - 1) to it.value }
            .groupBy { it.first }
            .mapValues { it.value.sumOf { it.second } }
            .toMutableMap()
        daysUntilSpawnToNumFish[8] = newDay8Fish
    }
    println("There are ${daysUntilSpawnToNumFish.values.reduce(BigInteger::add)} fish after 256 days.")
}