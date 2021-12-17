import kotlin.math.sign
import kotlin.math.sqrt

data class TargetArea(val x: IntRange, val y: IntRange)

data class Probe(val x: Int, val y: Int, val vx: Int, val vy: Int) {
    fun step() = Probe(x + vx, y + vy, vx - vx.sign, vy - 1)
    fun crosses(targetArea: TargetArea): Boolean {
        var probe = this
        while (probe.y >= targetArea.y.first && probe.x <= targetArea.x.last ) {
            if (probe.x in targetArea.x && probe.y in targetArea.y) {
                return true
            }
            probe = probe.step()
        }
        return false
    }
}


fun String.toTargetArea(): TargetArea {
    val match = "target area: x=(-?\\d+)..(-?\\d+), y=(-?\\d+)..(-?\\d+)".toRegex().find(this)
        ?: throw IllegalArgumentException("Cannot parse $this")
    return TargetArea(
        match.groupValues[1].toInt()..match.groupValues[2].toInt(),
        match.groupValues[3].toInt()..match.groupValues[4].toInt()
    )
}

fun main() {
    val targetArea = getPuzzleInput(17, 1).toTargetArea()
    // 0 = n(n+1)/2  - targetArea.x.last
    var maxVx = (1/2.0) + sqrt((1/4.0) + targetArea.x.last * 2)
    var minVx = (1/2.0) + sqrt((1/4.0) + targetArea.x.start * 2)
    var vx = minVx.toInt()
    var vy = 200
    val solutions = sequence {
        while (vx <= targetArea.x.last) {
            while (vy >= targetArea.y.first) {
                vy--
                val probe = Probe(0, 0, vx, vy)
                if (probe.crosses(targetArea)) {
                    yield(probe)
                }
            }
            vy = 200
            vx++
        }
    }.toList()
    val maxY = solutions.map { solution ->
        (0..targetArea.x.last)
            .scan(solution) { acc, i -> acc.step() }.maxOf { it.y }
    }.maxOf{it}
    println(maxY)
    println(solutions.size)
}