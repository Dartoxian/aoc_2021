import kotlin.math.max
import kotlin.math.min

data class Rule(val x1: Int, val y1: Int, val x2: Int, val y2: Int) {
    fun isHorizontalOrVertical(): Boolean = x1 == x2 || y1 == y2
    fun covers(): List<Pair<Int, Int>> =
        if (this.isHorizontalOrVertical())
            (min(x1, x2)..max(x1, x2)).flatMap { x -> ((min(y1, y2)..max(y1, y2))).map { y -> x to y } }
        else (if (x1 < x2) x1..x2 else x1 downTo x2).zip(if (y1 < y2) y1..y2 else y1 downTo y2)
}

val parsePattern = "(\\d+),(\\d+) -> (\\d+),(\\d+)".toRegex()
fun String.toRule(): Rule {
    val match = parsePattern.find(this) ?: throw IllegalArgumentException("Unable to parse line $this")
    return Rule(
        match.groupValues[1].toInt(),
        match.groupValues[2].toInt(),
        match.groupValues[3].toInt(),
        match.groupValues[4].toInt(),
    )
}

fun p1(rules: List<Rule>) {
    val numVentsAtPoint = rules
        .filter(Rule::isHorizontalOrVertical)
        .flatMap(Rule::covers)
        .groupBy { it }
    val numPointsWithOverlap = numVentsAtPoint.count { it.value.count() > 1}
    println("There are $numPointsWithOverlap points with at least 2 vents")
}

fun p2(rules: List<Rule>) {
    val numVentsAtPoint = rules
        .flatMap(Rule::covers)
        .groupBy { it }
    val numPointsWithOverlap = numVentsAtPoint.count { it.value.count() > 1}
    println("(including diagonal rules) There are $numPointsWithOverlap points with at least 2 vents")
}

fun main() {
    val input = getPuzzleInput(5, 1)
    val rules = input.split("\n").map(String::toRule)
    p1(rules)
    p2(rules)
}