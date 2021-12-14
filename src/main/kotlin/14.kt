fun main() {
    val input = getPuzzleInput(14, 1).split("\n")
    val initialPolymer = input[0]
    val polymerLookup = input
        .slice(2 until input.size)
        .map { it.split(" -> ") }
        .map { it[0] to it[1] }
        .toMap()

    val outComes: MutableList<Map<String, Map<Char, Long>>> = mutableListOf(
        polymerLookup
            .map {
                it.key to (it.key + it.value).fold(mutableMapOf<Char, Long>()) { acc, c ->
                    acc[c] = acc.getOrDefault(c, 0) + 1L
                    acc
                }
            }.toMap()
    )

    for (i in 1..40) {
        outComes.add(
            polymerLookup
                .map {
                    val l = outComes[i - 1]["${it.key[0]}${it.value}"]!!
                    val r = outComes[i - 1]["${it.value}${it.key[1]}"]!!
                    val midChar = it.value[0]
                    val res = (l.keys + r.keys)
                        .map { it to l.getOrDefault(it, 0) + r.getOrDefault(it, 0) + (if (it == midChar) - 1L else 0)  }
                        .toMap()
                    it.key to res
                }.toMap()
        )
    }
    val diffAfter10 = initialPolymer
        .zipWithNext()
        .map { outComes[9]["${it.first}${it.second}"]!! }
        .fold(mutableMapOf<Char, Long>()) { acc, m ->
            m.forEach { acc[it.key] = acc.getOrDefault(it.key, 0) + it.value }
            acc
        }.let { m ->
            // Deal with double counted letters from the initialPolymer split.
            initialPolymer.slice(1 until initialPolymer.length - 1)
                .forEach { m[it] = m.getOrDefault(it, 0) - 1 }
            m
        }.let { it.values.maxOf { it } - it.values.minOf { it } }
    println(diffAfter10)

    val diffAfter40 = initialPolymer
        .zipWithNext()
        .map { outComes[39]["${it.first}${it.second}"]!! }
        .fold(mutableMapOf<Char, Long>()) { acc, m ->
            m.forEach { acc[it.key] = acc.getOrDefault(it.key, 0) + it.value }
            acc
        }
        .let { m ->
            initialPolymer.slice(1 until initialPolymer.length - 1)
                .forEach { m[it] = m.getOrDefault(it, 0) - 1 }
            m
        }
        .let { it.values.maxOf { it } - it.values.minOf { it } }
    println(diffAfter40)
}