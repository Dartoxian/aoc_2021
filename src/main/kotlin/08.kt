data class DigitDisplay(val wireMap: Map<Char, Set<Char>>) {
    companion object {
        val INIT = DigitDisplay("abcdefg".map { it to "abcdefg".toSet() }.toMap())
    }

    fun configurationKnown() = wireMap.values.all { it.size == 1 }
    fun withWire(mapping: Pair<Char, Set<Char>>) =
        DigitDisplay(wireMap.mapValues { if (it.key == mapping.first) it.value.intersect(mapping.second) else it.value })

    fun withObservedWire(activeWires: Set<Char>): DigitDisplay {
        when(activeWires.size) {
            2 -> return DigitDisplay(wireMap.mapValues { if (it.key in "cf") activeWires.intersect(it.value) else it.value })
            3 -> return DigitDisplay(wireMap.mapValues { if (it.key in "acf") activeWires.intersect(it.value) else it.value })
            4 -> return DigitDisplay(wireMap.mapValues { if (it.key in "bcdf") activeWires.intersect(it.value) else it.value })
            else -> null
        }
        return this
    }

    fun withWiresSelfConstrained(): DigitDisplay {
        val newWireMap = wireMap.toMutableMap()
        var improvable = true
        while (improvable) {
            improvable = false
            val requiredMappings = newWireMap
                .map { it.value to it.key }
                .groupBy { it.first }
                .mapValues { it.value.map { it.second }.toSet() }
                .filter { it.key.size == it.value.size }

            newWireMap.forEach { newWire ->
                requiredMappings.forEach { requiredWire ->
                    if (requiredWire.key != newWire.value && requiredWire.key.intersect(newWire.value).isNotEmpty()) {
                        improvable = true
                        newWireMap[newWire.key] = newWire.value - requiredWire.key
                    }
                }
            }
        }
        return DigitDisplay(newWireMap)
    }

    fun getDigit(activeWires: Set<Char>): Int {
        if (!this.configurationKnown()) {
            throw IllegalArgumentException("The configuration for $this is not known, cannot get digit")
        }
        val wireLookup = wireMap.entries.map { it.value.first() to it.key }.toMap()
        return when (activeWires.map { wireLookup[it] }.toSet()) {
            setOf('a', 'b', 'c', 'e', 'f', 'g') -> 0
            setOf('c', 'f') -> 1
            setOf('a', 'c', 'd', 'e', 'g') -> 2
            setOf('a', 'c', 'd', 'f', 'g') -> 3
            setOf('b', 'c', 'd', 'f') -> 4
            setOf('a', 'b', 'd', 'f', 'g') -> 5
            setOf('a', 'b', 'd', 'e', 'f', 'g') -> 6
            setOf('a', 'c', 'f',) -> 7
            setOf('a', 'b', 'c', 'd', 'e', 'f', 'g') -> 8
            setOf('a', 'b', 'c', 'd', 'f', 'g') -> 9
            else -> throw RuntimeException("Received active wires that do not represent a digit\n$this\n$activeWires")
        }
    }
}

fun main() {
    val input = getPuzzleInput(8, 1)
    var numEasyDigits = 0
    var total = 0
    for (l in input.split("\n")) {
        val s = l.split(" | ")
        val examples = s[0].split((" ")).map { it.toCharArray().toSet() }
        val fourDigits = s[1].split(" ").map { it.toCharArray().toSet() }
        numEasyDigits += fourDigits.filter { it.size in setOf(2, 3, 4, 7) }.count()
        var display = DigitDisplay.INIT

        // Input easy examples
        examples.forEach { display = display.withObservedWire(it) }
        // Constrain
        display = display.withWiresSelfConstrained()

        // Common value in 6 digits must be wire b
        val bWire = examples.filter { it.size == 6 }.reduce{a, b -> a.intersect(b)}
        display = display.withWire('b' to bWire)
        // Constrain
        display = display.withWiresSelfConstrained()

        // Common value in 5 digits must be wire a, d or g
        val adgWire = examples.filter { it.size == 5 }.reduce{a, b -> a.intersect(b)}
        display = display.withWire('a' to adgWire)
        display = display.withWire('d' to adgWire)
        display = display.withWire('g' to adgWire)
        // Constrain
        display = display.withWiresSelfConstrained()

        // Value for 6 has abdefg. At this point we know the mapping for all except f.
        val wiresIn6 = display.wireMap.filter { it.key in "abdeg" }.values.flatMap { it }.toSet()
        val allWiresIn6 = examples.find { it.size == 6 && wiresIn6.intersect(it).size == 5 }!!
        display = display.withWire('f' to (allWiresIn6 - wiresIn6))
        // Constrain
        display = display.withWiresSelfConstrained()

        total += fourDigits.map { display.getDigit(it) }.fold(0) {acc, i -> (10 * acc) + i}
    }
    println(numEasyDigits)
    println(total)
}