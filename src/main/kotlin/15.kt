import java.util.*
import kotlin.math.cos
import kotlin.math.max

data class Coordinate(val x: Int, val y: Int)

data class PathCost(val destination: Coordinate, val cost: Int): Comparable<PathCost> {
    override fun compareTo(other: PathCost): Int = cost.compareTo(other.cost)
}

open class CaveGrid(open val grid: List<List<Int>>) {
    open val width = lazy {  grid[0].size}
    open val height = lazy { grid.size}

    fun findCheapestPath(from: Coordinate, to: Coordinate): Int {
        val q = PriorityQueue<PathCost>()
        q.add(PathCost(from, 0))
        val cheapestRoute = mutableMapOf<Coordinate, Int>(from to 0)
        while (q.isNotEmpty()) {
            val e = q.remove()
            getNeighbours(e.destination).forEach { n ->
                if (visitCost(n) + cheapestRoute[e.destination]!! < cheapestRoute.getOrDefault(n, Int.MAX_VALUE)) {
                    cheapestRoute[n] = visitCost(n) + cheapestRoute[e.destination]!!
                    q.add(PathCost(n, cheapestRoute[n]!!))
                }
            }
        }
        return cheapestRoute[to]!!
    }

    open fun visitCost(to: Coordinate): Int = grid[to.y][to.x]

    fun getNeighbours(from: Coordinate) = sequence {
        if (from.x > 0) yield(Coordinate(from.x - 1, from.y))
        if (from.x < width.value - 1) yield(Coordinate(from.x + 1, from.y))
        if (from.y > 0) yield(Coordinate(from.x, from.y - 1))
        if (from.y < height.value - 1) yield(Coordinate(from.x, from.y + 1))
    }
}

data class MegaCaveGrid(override val grid: List<List<Int>>, val dimensionMultiplier: Int): CaveGrid(grid) {
    override val width = lazy { grid[0].size * dimensionMultiplier}
    override val height = lazy {grid.size * dimensionMultiplier}

    override fun visitCost(to: Coordinate): Int {
        val gridsAway = (to.x / grid[0].size) + (to.y / grid.size)
        var cost = gridsAway + grid[to.x % grid[0].size][to.y % grid.size]
        if (cost > 9) {
            cost = (cost % 10) + (cost/10)
        }
        return cost
    }
}

fun main() {
    val input = getPuzzleInput(15, 1).split("\n").map { it.toCharArray().map { it.digitToInt() } }
    val grid = CaveGrid(input)
    val cheapestPathCost = grid.findCheapestPath(Coordinate(0, 0), Coordinate(99, 99))
    println(cheapestPathCost)

    val megaGrid = MegaCaveGrid(input, 5)
    val cheapestPathCostInMegaCaveGrid = megaGrid.findCheapestPath(Coordinate(0, 0), Coordinate(499, 499))
    println(cheapestPathCostInMegaCaveGrid)
}