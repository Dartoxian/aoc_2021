import kotlin.math.max
import kotlin.math.min

fun String.chunkedWithOverlap(chunkSize: Int): Sequence<String> = sequence {
    if (chunkSize < 2) throw IllegalArgumentException("Cannot chunk to size less than two on '${this@chunkedWithOverlap}'")
    var i = 0
    while (i < this@chunkedWithOverlap.length) {
        yield(this@chunkedWithOverlap.slice(i..min(i + chunkSize - 1, this@chunkedWithOverlap.length - 1)))
        i += chunkSize - 1
    }
}

fun getNextPolymerMemoized(polymerLookup: Map<String, String>): (Sequence<String>) -> Sequence<String> {
    var cache = polymerLookup
        .mapValues { it.key[0].toString() + it.value + it.key[1] }
        .toMutableMap()

    val maxCachedChunkSize = 100000
    fun getNextPolymer(inputSequence: Sequence<String>): Sequence<String> = sequence {
        val seq = sequence {
            val iterator = inputSequence.iterator()
            var currentValue = iterator.next()
            var prevValue: String? = null
            while (iterator.hasNext()) {
                var i = 2
                while (i < currentValue.length) {
                    if (currentValue.slice(0 until i + 1) in cache) {
                        i++
                    } else {
                        break
                    }
                }
                if (i > 2) {
                    prevValue = if (prevValue != null) prevValue.last().toString() + currentValue.slice(0 until i) else currentValue.slice(0 until i)
                    yield(prevValue)
                    currentValue = currentValue.slice(i until currentValue.length)
                } else if (currentValue.length > maxCachedChunkSize) {
                    yield(if (prevValue != null) prevValue.last().toString() + currentValue else currentValue)
                    prevValue = currentValue
                    currentValue = iterator.next()
                } else {
                    currentValue += iterator.next()
                }
            }
            yield(if (prevValue != null) prevValue.last().toString() + currentValue else currentValue)
        }
        val iterator = seq.iterator()
        while (iterator.hasNext()) {
            val input = iterator.next()
            if (input in cache) {
                yield(cache[input]!!)
            } else if (input.length == 1) {
                yield(input)
            } else {
                val chunks = input.chunkedWithOverlap(min(max(2, input.length / 2), maxCachedChunkSize))
                for (chunk in chunks) {
                    if (chunk !in cache && chunk.length > 0) {
                        cache[chunk] = getNextPolymer(sequence { yield(chunk) }).joinToString("")
                    }
                }
                yieldAll(chunks
                    .map { cache[it]!! }
                    .let {
                        sequence {
                            val iterator2 = it.iterator()
                            var next = iterator2.next()
                            while (iterator2.hasNext()) {
                                yield(next.slice(0 until next.length - 1))
                                next = iterator2.next()
                            }
                            if (!iterator.hasNext()) {
                                yield(next)
                            } else {
                                yield(next.slice(0 until next.length - 1))
                            }
                        }
                    })
            }
        }
    }
    return { getNextPolymer(it) }
}

fun getCharacterCountsMemoized(): (String) -> Map<Char, Int> {
    val cache = mutableMapOf<String, Map<Char, Int>>()
    fun getCharacterCounts(input: String): Map<Char, Int> {
        val key = input.toCharArray().sorted().joinToString("")
        if (key !in cache) {
            cache[key] = input.toCharArray().fold(mutableMapOf()) { acc, c ->
                acc[c] = acc.getOrDefault(c, 0) + 1
                acc
            }
        }
        return cache[key]!!
    }
    return { getCharacterCounts(it) }
}

fun main() {
    val input = getPuzzleInput(14, 1).split("\n")
    val initialPolymer = input[0]
    val polymerLookup = input
        .slice(2 until input.size)
        .map { it.split(" -> ") }
        .map { it[0] to it[1] }
        .toMap()

    val memoizedNextPolymer = getNextPolymerMemoized(polymerLookup)
    val tenStepPolymer = (1..10).fold(sequence { yield(initialPolymer) }) { acc, i ->
        memoizedNextPolymer(acc)
    }
    val p1Result = tenStepPolymer.joinToString("").toCharArray().groupBy { it }.let {
        it.values.maxOf { it.count() } - it.values.minOf { it.count() }
    }
    println(p1Result)

    val fortyStepPolymer = (1..40).fold(sequence { yield(initialPolymer) }) { acc, i ->
        memoizedNextPolymer(acc)
    }
    val memoizedCharacterCounts = getCharacterCountsMemoized()
    val p2Result = fortyStepPolymer.map(memoizedCharacterCounts).fold(mutableMapOf<Char, Int>()) { acc, m ->
        m.forEach { c, t -> acc[c] = acc.getOrDefault(c, 0) + t }
        acc
    }.let {
        it.values.maxOf { it } - it.values.minOf { it }
    }
    println(p2Result)
}