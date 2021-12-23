import kotlin.math.max
import kotlin.math.min

data class CubeRule(val on: Boolean, val x: LongRange, val y: LongRange, val z: LongRange) {
    init {
        if (x.isEmpty() || y.isEmpty() || z.isEmpty()) {
            throw IllegalArgumentException("$this is invalid as a cube rule")
        }
    }

    fun volume() =
        if (x.isEmpty() || y.isEmpty() || z.isEmpty()) 0L
        else (x.last - x.first + 1) * (y.last - y.first + 1) * (z.last - z.first + 1)
    fun contains(p: Triple<Long, Long, Long>) = p.first in x && p.second in y && p.third in z

    fun from(corner1: Triple<Long, Long, Long>, corner2: Triple<Long, Long, Long>) = CubeRule(on, 
        corner1.first..corner2.first,
        corner1.second..corner2.second,
        corner1.third..corner2.third
        )
    
    fun subtract(other: CubeRule) = sequence {
        if (x.first > other.x.last || x.last < other.x.first ||
            y.first > other.y.last || y.last < other.y.first ||
            z.first > other.z.last || z.last < other.z.first) {
            yield(this@CubeRule)
        } else {
            if (other.z.first > z.first) yield(from(Triple(x.first, y.first, z.first), Triple(x.last, y.last, other.z.first - 1)))
            if (other.z.last < z.last) yield(from(Triple(x.first, y.first, other.z.last + 1), Triple(x.last, y.last, z.last)))

            if (other.x.first > x.first) yield(from(Triple(x.first, y.first, max(other.z.first, z.first)), Triple(other.x.first - 1, y.last, min(z.last, other.z.last))))
            if (other.x.last < x.last) yield(from(Triple(other.x.last + 1, y.first, max(other.z.first, z.first)), Triple(x.last, y.last, min(z.last, other.z.last))))

            if (other.y.first > y.first) yield(
                from(
                    Triple(max(x.first, other.x.first), y.first, max(z.first, other.z.first)),
                    Triple(min(other.x.last, x.last), other.y.first - 1, min(z.last, other.z.last))
                )
            )
            if (other.y.last < y.last) yield(from(Triple(max(x.first, other.x.first), other.y.last + 1, max(z.first, other.z.first)), Triple(min(other.x.last, x.last), y.last, min(z.last, other.z.last))))
        }
    }
}

fun String.toCubeRule(): CubeRule {
    val pat = "(on|off) x=(-?\\d+)\\.\\.(-?\\d+),y=(-?\\d+)\\.\\.(-?\\d+),z=(-?\\d+)\\.\\.(-?\\d+)".toRegex()
    val m = pat.find(this) ?: throw IllegalArgumentException("Cannot parse '$this'")
    return CubeRule(
        m.groupValues[1] == "on",
        m.groupValues[2].toLong()..m.groupValues[3].toLong(),
        m.groupValues[4].toLong()..m.groupValues[5].toLong(),
        m.groupValues[6].toLong()..m.groupValues[7].toLong(),
    )
}

fun main() {
    val rules = getPuzzleInput(22, 1).split("\n").map(String::toCubeRule)
    println(rules.size)
    val onAfterInitialisation = (-50..50).flatMap { x -> (-50..50).flatMap { y -> (-50..50).map { z ->
        rules.findLast { it.contains(Triple(x.toLong(), y.toLong(), z.toLong())) }?.on ?: false
    } } }.count { it }
    println(onAfterInitialisation)
    var i = 0
    val activeCubes = rules.fold(listOf<CubeRule>()) {acc, r ->
        i++
        println("Rule $i")
        println("Cubes ${acc.size}")
        acc.flatMap { it.subtract(r) } + (if (r.on) listOf(r) else listOf())
    }.sumOf { it.volume() }
    println(activeCubes)
}