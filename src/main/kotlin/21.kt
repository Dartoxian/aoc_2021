import kotlin.math.min

class DeterministicDie {
    private var nextRollResult = 1
    public var numRolls = 0
    fun roll(): Int {
        numRolls++
        val r = nextRollResult
        nextRollResult++
        if (nextRollResult == 101) nextRollResult = 1
        return r
    }
}

data class Player(val position: Int, val score: Int) {
    fun takeTurn(die: DeterministicDie) =
        with(((die.roll() + die.roll() + die.roll() + position - 1) % 10) + 1) { Player(this, score + this) }
}

class Game(var p1: Player, var p2: Player, val die: DeterministicDie) {
    fun score() = min(p1.score, p2.score) * die.numRolls
    fun playGame() {
        while (true) {
            p1 = p1.takeTurn(die)
            if (p1.score >= 1000) break
            p2 = p2.takeTurn(die)
            if (p2.score >= 1000) break
        }
    }
}

data class MultiversePlayer(val playerId: Int, val position: Int, val score: Int) {
    fun takeTurn() = (1..3).flatMap { r1 ->
        (1..3).flatMap { r2 ->
            (1..3).map { r3 -> r1 + r2 + r3 }
        }
    }
        .groupBy { ((it + position - 1) % 10) + 1 }
        .mapValues { it.value.size }
        .map { MultiversePlayer(playerId, it.key, score + it.key) to it.value }
}

data class MultiverseGame(var p1: MultiversePlayer, var p2: MultiversePlayer, val numerOfUniverses: Long) {
    fun gameOver() = p2.score >= 21
    fun playTurn() = if (gameOver()) listOf(this) else p1.takeTurn().map { MultiverseGame(p2, it.first, numerOfUniverses * it.second) }
}

fun main() {
    val game = Game(Player(4, 0), Player(9, 0), DeterministicDie())
    game.playGame()
    println(game.score())

    var multiverseGames = listOf(
        MultiverseGame(MultiversePlayer(1, 4, 0), MultiversePlayer(2, 9, 0), 1)
    )
    while (multiverseGames.any{!it.gameOver()}) {
        multiverseGames = multiverseGames
            .flatMap { it.playTurn() }
            .groupBy { it.p1 to it.p2 }
            .mapValues { it.value.reduce{g1, g2 ->
                MultiverseGame(g1.p1, g1.p2, g1.numerOfUniverses + g2.numerOfUniverses)
            } }.values.toList()
    }
    println(multiverseGames.groupBy { it.p2.playerId }.mapValues { it.value.sumOf { it.numerOfUniverses } }.values.maxOf { it })
}