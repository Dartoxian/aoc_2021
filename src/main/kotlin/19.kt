import java.util.*
import kotlin.math.abs

data class Position(val x: Int, val y: Int, val z: Int) {
    fun rotateClockwiseX() = Position(x, -z, y)
    fun rotateClockwiseY() = Position(z, y, -x)
    fun rotateClockwiseZ() = Position(y, -x, z)
    fun rotate(xTimes: Int, yTimes: Int, zTimes: Int): Position {
        var r = this
        (0..xTimes).forEach { r = r.rotateClockwiseX() }
        (0..yTimes).forEach { r = r.rotateClockwiseY() }
        (0..zTimes).forEach { r = r.rotateClockwiseZ() }
        return r
    }
    fun transpose(dx: Int, dy: Int, dz: Int) = Position(x + dx, y + dy, z + dz)
    fun manhattanDistance(other: Position) = abs(x - other.x) + abs(y - other.y) + abs(z - other.z)
}

data class Scanner(val id: Int, val probes: List<Position>, val position: Position = Position(0, 0, 0)) {
    fun orientations() = sequence {
        (0..3).map { xRot ->
            (0..3).map { yRot ->
                (0..3).map { zRot ->
                    yield(Scanner(id, probes.map { it.rotate(xRot, yRot, zRot) }))
                }
            }
        }
    }.distinct()

    fun transpose(dx: Int, dy: Int, dz: Int): Scanner = Scanner(id, probes.map { it.transpose(dx, dy, dz) }, position.transpose(dx, dy, dz))

    fun alignedWith(other: Scanner): Scanner? {
        for (orientation in orientations()) {
            for (probe in orientation.probes) {
                for (otherProbe in other.probes) {
                    val transposedOrientation =
                        orientation.transpose(otherProbe.x - probe.x, otherProbe.y - probe.y, otherProbe.z - probe.z)
                    if (other.probes.toSet().intersect(transposedOrientation.probes.toSet()).size >= 12) {
                        return transposedOrientation
                    }
                }
            }
        }
        return null
    }
}

fun String.toScanners(): Sequence<Scanner> = sequence {
    val lines = this@toScanners.split("\n")
    var i = 0
    while (i < lines.size) {
        val matches = "--- scanner (\\d+) ---".toRegex().find(lines[i])
            ?: throw IllegalArgumentException("'${lines[i]}' is not scanner start")
        i++
        val probes = sequence {
            while (i < lines.size && lines[i] != "") {
                yield(lines[i].split(",").map(String::toInt).let { Position(it[0], it[1], it[2]) })
                i++
            }
        }.toList()
        yield(Scanner(matches.groupValues[1].toInt(), probes))
        i++
    }
}

fun main() {
    val scanners = getPuzzleInput(19, 1).toScanners().toList()
    var knownCorrectScanners = mutableListOf(scanners[0])
    val q: Queue<Pair<Scanner, Int>> = LinkedList(scanners.slice(1 until scanners.size).map { it to 0 })
    while (q.isNotEmpty()) {
        val (s, attemptedScanners) = q.remove()
        val correctScanner =
            knownCorrectScanners.slice(attemptedScanners until knownCorrectScanners.size)
                .asSequence().map { s.alignedWith(it) }.filterNotNull().firstOrNull()
        if (correctScanner == null) {
            q.add(s to knownCorrectScanners.size - 1)
        } else {
            knownCorrectScanners.add(correctScanner)
        }
    }
    println(knownCorrectScanners.flatMap { it.probes }.distinct().size)
    println(knownCorrectScanners.maxOf { s1 ->
        knownCorrectScanners.filter { it != s1 }
            .maxOf { s1.position.manhattanDistance(it.position) } })
}