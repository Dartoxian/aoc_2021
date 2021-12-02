fun main() {
    val input = getPuzzleInput(2, 1)
    val instructionPattern = "(?<dir>forward|up|down) (?<q>\\d+)".toRegex()
    var horizontal = 0;
    var depth = 0
    for (l in input.split("\n")) {
        val matches = instructionPattern.find(l) ?: throw IllegalArgumentException("Cannot match on '$l'")
        val q = matches.groups["q"]!!.value.toInt()
        when(matches.groups["dir"]!!.value) {
            "forward" -> horizontal += q
            "up" -> depth -= q
            "down" -> depth += q
        }
    }
    println("Part 1 soln ${horizontal * depth}")

    horizontal = 0;
    depth = 0
    var aim = 0
    for (l in input.split("\n")) {
        val matches = instructionPattern.find(l) ?: throw IllegalArgumentException("Cannot match on '$l'")
        val q = matches.groups["q"]!!.value.toInt()
        when(matches.groups["dir"]!!.value) {
            "forward" -> {
                horizontal += q
                depth += aim * q
            }
            "up" -> aim -= q
            "down" -> aim += q
        }
    }
    println("Part 2 soln ${horizontal * depth}")
}