import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

//#############
//#1234567781011# H1 - 11
//###D#C#A#B### A,B,C,D 1
//  #C#D#A#B#   A,B,C,D 2
//  #########

fun energyCost(type: String) = when (type) {
    "A" -> 1L
    "B" -> 10L
    "C" -> 100L
    "D" -> 1000L
    else -> throw IllegalArgumentException("$type unknown")
}

fun hallwayPositionFromRoom(roomLetter: String) = when (roomLetter) {
    "A" -> 3
    "B" -> 5
    "C" -> 7
    "D" -> 9
    else -> throw IllegalArgumentException("Room $roomLetter unknown")
}

val allowedHallwayPositions = setOf(1, 2, 4, 6, 8, 10, 11)

fun costToMove(hallwayPosition: Int, roomLetter: String, roomPosition: Int, type: String): Long {
    val energyCost = energyCost(type)
    return (roomPosition * energyCost) + (abs(hallwayPositionFromRoom(roomLetter) - hallwayPosition) * energyCost)
}

fun costToMove(p1: String, p2: String, type: String): Long {
    val hp = if (p1.startsWith("H")) p1 else p2
    val rp = if (p1.startsWith("H")) p2 else p1
    return costToMove(hp.slice(1 until hp.length).toInt(), rp.slice(0..0), rp[1].digitToInt(), type)
}

data class Burrow(val roomDepth: Int, val occupiedLocations: Map<String, String>, val energyUsed: Long) {
    init {
        if (occupiedLocations.size != roomDepth * 4) {
            throw IllegalArgumentException("Invalid burrow $this")
        }
    }

    fun isComplete() = listOf("A", "B", "C", "D")
        .all { room -> (1..roomDepth).all { rd -> occupiedLocations["$room$rd"] == room } }

    fun allowedLocationsFrom(location: String): Sequence<String> {
        if (location !in occupiedLocations) throw IllegalArgumentException("Nothing can move from $location")
        if (location.startsWith("H")) {
            return sequence {
                val type = occupiedLocations[location]!!
                val hallwayPositionForRoom = hallwayPositionFromRoom(type)
                val currentHallwayPosition = location.slice(1 until location.length).toInt()
                if ((min(currentHallwayPosition, hallwayPositionForRoom) + 1..max(
                        currentHallwayPosition,
                        hallwayPositionForRoom
                    ) - 1)
                        .any { "H$it" in occupiedLocations }
                ) {
                    return@sequence
                }
                for (i in roomDepth downTo 1) {
                    if ((1..i).all { "$type$it" !in occupiedLocations } &&
                        (i + 1..roomDepth).all { occupiedLocations["$type$it"] == type }) {
                        yield("$type$i")
                        break
                    }
                }
            }
        } else {
            return sequence {
                val room = location.slice(0..0)
                val locationDepth = location[1].digitToInt()
                if ((locationDepth..roomDepth).all { occupiedLocations["$room$it"] == room }) return@sequence
                if ((1..locationDepth - 1).any { "$room$it" in occupiedLocations }) return@sequence
                val p = hallwayPositionFromRoom(room)
                var i = p
                while (i > 0) {
                    if ("H$i" !in occupiedLocations) {
                        if (i in allowedHallwayPositions) {
                            yield("H$i")
                        }
                    } else {
                        break
                    }
                    i--
                }
                i = p
                while (i < 12) {
                    if ("H$i" !in occupiedLocations) {
                        if (i in allowedHallwayPositions) {
                            yield("H$i")
                        }
                    } else {
                        break
                    }
                    i++
                }
            }
        }
    }

    fun nextBurrows() = occupiedLocations.flatMap { (p1, type) ->
        allowedLocationsFrom(p1).map { p2 ->
            val oL = occupiedLocations.toMutableMap()
            val cost = costToMove(p1, p2, type)
            oL.put(p2, type)
            oL.remove(p1)
            Burrow(roomDepth, oL, energyUsed + cost)
        }
    }
}

fun main() {
    val initBurrow = Burrow(
        2,
        mapOf(
            "A1" to "D",
            "A2" to "C",
            "B1" to "C",
            "B2" to "D",
            "C1" to "A",
            "C2" to "A",
            "D1" to "B",
            "D2" to "B"
        ), 0
    )

    var q = PriorityQueue<Burrow> { a, b -> a.energyUsed.compareTo(b.energyUsed) }
    var seenConfigurations = mutableSetOf<Map<String, String>>()
    q.add(initBurrow)
    while (q.isNotEmpty()) {
        val nextBurrow = q.remove()
        if (nextBurrow.occupiedLocations in seenConfigurations) {
            continue
        }
        seenConfigurations.add(nextBurrow.occupiedLocations)
        if (nextBurrow.isComplete()) {
            println("Done ${nextBurrow.energyUsed}")
            break
        }
        q.addAll(nextBurrow.nextBurrows())
    }

    val secondBurrow = Burrow(
        4,
        mapOf(
            "A1" to "D",
            "A2" to "D",
            "A3" to "D",
            "A4" to "C",
            "B1" to "C",
            "B2" to "C",
            "B3" to "B",
            "B4" to "D",
            "C1" to "A",
            "C2" to "B",
            "C3" to "A",
            "C4" to "A",
            "D1" to "B",
            "D2" to "A",
            "D3" to "C",
            "D4" to "B"
        ), 0
    )
    q = PriorityQueue<Burrow> { a, b -> a.energyUsed.compareTo(b.energyUsed) }
    seenConfigurations = mutableSetOf<Map<String, String>>()
    q.add(secondBurrow)
    while (q.isNotEmpty()) {
        val nextBurrow = q.remove()
        if (nextBurrow.occupiedLocations in seenConfigurations) {
            continue
        }
        seenConfigurations.add(nextBurrow.occupiedLocations)
        if (nextBurrow.isComplete()) {
            println("Done ${nextBurrow.energyUsed}")
            break
        }
        q.addAll(nextBurrow.nextBurrows())
    }
}