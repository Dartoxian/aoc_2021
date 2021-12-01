fun main() {
    val input = getPuzzleInput(1, 1)
    val ints = input.split("\n").map{it.toInt()}
    var i = 1
    var sinking = 0
    while (i < ints.size) {
        if (ints[i] > ints[i - 1]) {
            sinking++
        }
        i++
    }
    println("Sinks $sinking times")

    i = 3
    sinking = 0
    while (i < ints.size) {
        if ((ints[i] + ints[i - 1] + ints[i - 2]) > (ints[i - 3] + ints[i - 1] + ints[i - 2])) {
            sinking++
        }
        i++
    }
    println("Sinks $sinking times")
}