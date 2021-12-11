data class Step(val flashes: Int, val nextGrid: Grid)

data class Grid(val octopus: List<List<Int>>) {
    fun step(): Step {
        var nextOctopuses = octopus.map { it.map { it + 1 }.toMutableList() }
        var flashes = 0
        var flashLocations = nextOctopuses.flatMapIndexed { i, row ->
            row.mapIndexedNotNull { j, octopusVal ->
                if (octopusVal > 9) i to j else null
            }
        }
        while (flashLocations.isNotEmpty()) {
            flashLocations.forEach { p ->
                flashes++
                nextOctopuses[p.first][p.second] = 0
                this.neighbours(p.first, p.second)
                    .filter { nextOctopuses[it.first][it.second] > 0 }
                    .forEach { nextOctopuses[it.first][it.second] = nextOctopuses[it.first][it.second] + 1 }
            }
            flashLocations = nextOctopuses.flatMapIndexed { i, row ->
                row.mapIndexedNotNull { j, octopusVal ->
                    if (octopusVal > 9) i to j else null
                }
            }
        }
        return Step(flashes, Grid(nextOctopuses))
    }

    fun isMegaFlash() = octopus.all { it.all { it == 0 } }

    fun neighbours(i: Int, j: Int) = sequence {
        if (i > 0) {
            if (j > 0) yield(i - 1 to j - 1)
            yield(i - 1 to j)
            if (j < octopus[0].size - 1) yield(i - 1 to j + 1)
        }
        if (j > 0) yield(i to j - 1)
        if (j < octopus[0].size - 1) yield(i to j + 1)
        if (i < octopus.size - 1) {
            if (j > 0) yield(i + 1 to j - 1)
            yield(i + 1 to j)
            if (j < octopus[0].size - 1) yield(i + 1 to j + 1)
        }
    }
}

fun String.toGrid() = Grid(this.split("\n").map { it.map { it.digitToInt() } })

fun main() {
    val grid = getPuzzleInput(11, 1).toGrid()
    val totalFlashes = (1..100)
        .scan(Step(0, grid)) { prevStep, i -> prevStep.nextGrid.step() }
        .map { it.flashes }
        .sum()
    println("After 100 steps there are, $totalFlashes total flashes")

    val firstAllFlashStep = generateSequence(1) { it + 1 }
        .scan(Step(0, grid) to 0) { prevStep, i -> prevStep.first.nextGrid.step() to i }
        .find { it.first.nextGrid.isMegaFlash() }!!.second
    println("The first time all octopuses flash was on step $firstAllFlashStep")
}