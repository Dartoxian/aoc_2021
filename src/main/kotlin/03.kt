fun main() {
    val input = getPuzzleInput(3, 1)
    val gammaRaw = input.split("\n").fold(listOf(), { res: List<List<Char>>, next: String ->
        if (res.isEmpty()) {
            next.toCharArray().map { listOf(it) }
        } else {
            res.zip(next.toList(), { a, b -> a + listOf(b) })
        }
    }).map { if (it.count { it == '1' } > it.size / 2) '1' else '0' }.joinToString(separator = "")

    val gamma = gammaRaw.toInt(2)
    val epsilon = gammaRaw.map {
        if (it == '0') '1' else '0'
    }.joinToString(separator = "").toInt(2)
    println("Power rate is ${gamma * epsilon}")

    var oxygenGeneratorRatings = input.split("\n")
    var bitPosition = 0
    while (oxygenGeneratorRatings.size > 1) {
        val mostCommonValue =
            with(oxygenGeneratorRatings.groupBy { it[bitPosition] }.toMap().mapValues { it.value.size }) {
                if (this['0']!! > this['1']!!) '0' else '1'
            }
        oxygenGeneratorRatings = oxygenGeneratorRatings.filter { it[bitPosition] == mostCommonValue }
        bitPosition++
    }

    var co2ScrubberRating = input.split("\n")
    bitPosition = 0
    while (co2ScrubberRating.size > 1) {
        val leastCommonValue =
            with(co2ScrubberRating.groupBy { it[bitPosition] }.toMap().mapValues { it.value.size }) {
                if (this['0']!! > this['1']!!) '1' else '0'
            }
        co2ScrubberRating = co2ScrubberRating.filter { it[bitPosition] == leastCommonValue }
        bitPosition++
    }

    println("The life support rating is ${co2ScrubberRating[0].toInt(2) * oxygenGeneratorRatings[0].toInt(2)}")
}