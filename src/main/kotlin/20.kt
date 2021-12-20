fun invertPixel(pixel: Char) = if (pixel == '#') '.' else '#'
fun Char.pixelToDigit() = if (this == '#') 1 else 0
fun String.pixelsToInt() = this.map(Char::pixelToDigit).fold(0) { acc, b -> (2 * acc) + b }

data class Image(val defaultPixel: Char, val nonDefaultPixels: Set<Pair<Long, Long>>) {
    fun numberOfLitPixels() = when (defaultPixel) {
        '.' -> nonDefaultPixels.size
        else -> IllegalArgumentException("An infinite number of pixels are lit")
    }

    operator fun get(i: Long, j: Long) =
        if (i to j in nonDefaultPixels) invertPixel(defaultPixel) else defaultPixel

    fun enhancementSample(i: Long, j: Long) = (i-1..i+1).flatMap { lookI -> (j-1..j+1).map { lookJ ->
        this[lookI, lookJ]
    } }.joinToString("")

    fun enhance(enhancementAlgorithm: String): Image {
        val enhancedDefaultPixel = enhancementAlgorithm[(1..9).map { defaultPixel }.joinToString("").pixelsToInt()]
        val enhancedNonDefaultPixels = sequence {
            (nonDefaultPixels.minOf { it.first } - 1 .. nonDefaultPixels.maxOf { it.first } + 1).map { i ->
                (nonDefaultPixels.minOf { it.second } - 1 .. nonDefaultPixels.maxOf { it.second } + 1).map { j ->
                    val enhancedIJ = enhancementAlgorithm[enhancementSample(i, j).pixelsToInt()]
                    if (enhancedIJ != enhancedDefaultPixel) yield(i to j)
                }
            }
        }.toSet()
        return Image(enhancedDefaultPixel, enhancedNonDefaultPixels)
    }
}

fun List<String>.toImage(defaultPixel: Char) = Image(defaultPixel, this.flatMapIndexed {i, l ->
    l.mapIndexedNotNull{j, c ->
        if (c != defaultPixel) {
            i.toLong() to j.toLong()
        } else {
            null
        }
    }
}.toSet())

fun main() {
    val input = getPuzzleInput(20, 1).split("\n")
    val enhancementAlgorithm = input[0]
    val initialImage = input.slice(2 until input.size).toImage('.')
    println(initialImage.enhance(enhancementAlgorithm).enhance(enhancementAlgorithm).numberOfLitPixels())
    println((1..50).fold(initialImage) {acc, i -> acc.enhance(enhancementAlgorithm)}.numberOfLitPixels())
}