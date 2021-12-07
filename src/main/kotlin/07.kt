import kotlin.math.abs

fun p1(input: List<Int>) {
    val positionToCost = ((input.minOrNull()?:0)..(input.maxOrNull() ?: 0))
        .map {  p -> p to input.sumOf { abs(it - p) } }
        .sortedBy { it.second }

    println ("There is ${positionToCost[0].second} fuel needed")
}

fun p2(input: List<Int>) {
    val positionToCost = ((input.minOrNull()?:0)..(input.maxOrNull() ?: 0))
        .map {  p -> p to input.sumOf { abs(it - p) * (abs(it - p)+1) / 2 } }
        .sortedBy { it.second }

    println ("There is ${positionToCost[0].second} fuel needed for part 2")
}

fun main() {
    val input = getPuzzleInput(7, 1).split(",").map { it.toInt() }
    p1(input)
    p2(input)
}