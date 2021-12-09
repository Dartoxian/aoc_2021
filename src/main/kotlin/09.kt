import kotlin.math.max
import kotlin.math.min

fun main() {
    val map = getPuzzleInput(9, 1)
        .split("\n")
        .map { it.map { it.toString().toInt() } }

    val riskScore = map.flatMapIndexed { j, row ->
        row.filterIndexed { i, height ->
            (i == 0 || height < map[j][i - 1]) &&
                    (i == row.size - 1 || height < map[j][i + 1]) &&
                    (j == 0 || height < map[j - 1][i]) &&
                    (j == map.size - 1 || height < map[j + 1][i])
        }
    }.map { it + 1 }.sum()

    println(riskScore)

    var basinMask: List<MutableList<Int?>> =
        (0 until map.size).map { (0 until map[0].size).map { null }.toMutableList() }
    var nextBasinId = 0
    var basinsAdjacent = mutableMapOf<Int, Int>()
    map.forEachIndexed { i, row ->
        row.forEachIndexed { j, height ->
            if (height == 9) {
                null
            } else {
                val basinAbove = basinMask[j].getOrNull(i - 1)
                val basinLeft = if (j > 0) basinMask[j - 1][i] else null
                val basinId =
                    listOf(basinAbove, basinLeft, basinsAdjacent[basinAbove], basinsAdjacent[basinLeft], nextBasinId++)
                        .minOf { if (it == null) Int.MAX_VALUE else it }
                if (basinAbove != null) {
                    basinsAdjacent[basinAbove] = basinId
                }
                if (basinLeft != null) {
                    basinsAdjacent[basinLeft] = basinId
                }
                if (basinId !in basinsAdjacent) {
                    basinsAdjacent[basinId] = basinId
                }
                basinMask[j][i] = basinId
            }
        }
    }
    // Need to modify the basins adjacent lookup to lookup the lowest ID in a chain
    // of connected basins. The chain will always be in descreasing basin id order.
    basinsAdjacent = basinsAdjacent.mapValues {
        var minBasinId = it.value
        while (basinsAdjacent[minBasinId] != minBasinId) {
            minBasinId = basinsAdjacent[minBasinId]!!
        }
        minBasinId
    }.toMutableMap()
    basinMask = basinMask.map { it.map { basinsAdjacent[it] }.toMutableList() }

    val basinsToSizes = basinMask.flatMap { it }
        .filterNotNull()
        .groupBy { it }
        .mapValues { it.value.size }
    val sortedBasinIds = basinsToSizes
        .toList()
        .sortedBy { it.second }
        .reversed()
        .map { it.first }

    val p2Solution = sortedBasinIds.slice(0 until 3).map { basinsToSizes[it]!! }.reduce(Int::times)

    println(p2Solution)
}