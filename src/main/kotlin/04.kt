data class BingoBoard(val grid: List<List<Int?>>) {
    fun without(number: Int) = BingoBoard(grid.map { it.map { if (it == number) null else it } })

    fun hasCompletedRow() = grid.any { it.all { it == null } }
    fun hasCompletedColumn() = (0 until 5).any { i -> grid.all { it[i] == null } }
    fun hasCompletedDiaganol() =
        (0 until 5).all { i -> grid[i][i] == null } || (0 until 5).all { i -> grid[4 - i][i] == null }

    fun isComplete() = this.hasCompletedRow() || this.hasCompletedColumn() || hasCompletedDiaganol()

    fun score(latestNumber: Int) = latestNumber * grid.sumOf { it.sumOf { it ?: 0 } }
}

fun main() {
    val inputs = getPuzzleInput(4, 1).split("\n")

    val numbers = inputs[0].split(",").map { it.toInt() }
    var initBoards = with(inputs.slice(2 until inputs.size)) {
        val rawBoards = this
        sequence<BingoBoard> {
            var i = 0
            while (i < rawBoards.size) {
                val grid = mutableListOf<List<Int>>()
                while (i < rawBoards.size && rawBoards[i] != "") {
                    grid.add(rawBoards[i].trim().split(" +".toRegex()).map { it.toInt() })
                    i++
                }
                yield(BingoBoard(grid))
                i++
            }
        }.toList()
    }

    var i = 0;
    var boards = initBoards.map { it.without(numbers[i]) }
    do {
        i++
        boards = boards.map { it.without(numbers[i]) }
    } while(boards.all { !it.isComplete() })

    val winningScore = boards.find { it.isComplete() }!!.score(numbers[i])
    println("The winning score is $winningScore")

    i = 0;
    boards = initBoards.map { it.without(numbers[i]) }.filter { !it.isComplete() }
    do {
        i++
        boards = boards.map { it.without(numbers[i]) }.filter { !it.isComplete() }
    } while(boards.size != 1)

    do {
        i++
        boards = boards.map { it.without(numbers[i]) }
    } while(boards.all { !it.isComplete() })

    val finalWinningScore = boards[0].score(numbers[i])
    println("The final winning score is $finalWinningScore")
}