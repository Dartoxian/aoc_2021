data class DotGrid(val dots: List<Pair<Int, Int>>) {
    fun withDot(at: Pair<Int, Int>) = DotGrid(dots + at)

    fun withFold(fold: Fold) =
        if (fold.horizontal) this.withHorizontalFoldAt(fold.at) else this.withVerticalFoldAt(fold.at)

    fun withVerticalFoldAt(x: Int) = DotGrid(dots.map {
        if (it.first > x) {
            x - (it.first - x) to it.second
        } else {
            it
        }
    })

    fun withHorizontalFoldAt(y: Int) = DotGrid(dots.map {
        if (it.second > y) {
            it.first to y - (it.second - y)
        } else {
            it
        }
    })

    override fun toString(): String {
        val width = dots.maxOf { it.first }
        val height = dots.maxOf { it.second }
        return with(dots.toSet()) {
            (0..height).map { y ->
                (0..width).map { x ->
                    if (x to y in this) "#" else "."
                }.joinToString("")
            }.joinToString("\n")
        }
    }
}

data class Fold(val horizontal: Boolean, val at: Int)

fun String.toFold(): Fold {
    val pat = "fold along (x|y)=(\\d+)".toRegex()
    val match = pat.find(this) ?: throw IllegalArgumentException("Cannot parse fold $this")
    return Fold(match.groupValues[1] == "y", match.groupValues[2].toInt())
}

fun main() {
    val input = getPuzzleInput(13, 1).split("\n")
    var dotGrid = DotGrid(listOf())
    var i = 0
    while (input[i] != "") {
        val dot = with(input[i].split(",").map { it.toInt() }) { this[0] to this[1] }
        dotGrid = dotGrid.withDot(dot)
        i++
    }
    i++
    val folds = input.slice(i until input.size).map(String::toFold)

    val dotsAfterFold1 = dotGrid.withFold(folds[0]).dots.toSet().size
    println(dotsAfterFold1)

    println(folds.fold(dotGrid, DotGrid::withFold).toString())
}