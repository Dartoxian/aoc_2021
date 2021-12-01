fun getPuzzleInput(day: Int, part: Int): String {
    return object {}.javaClass.getResource("${day.toString().padStart(2, '0')}_${part.toString().padStart(2, '0')}.txt")
        ?.readText()
        ?: throw IllegalArgumentException("Cannot find the input for puzzle ${day} part $part")
}