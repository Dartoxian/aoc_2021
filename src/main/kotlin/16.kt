abstract class Packet(version: Int, typeId: Int) {
    abstract fun sumVersions(): Int
    abstract fun resolve(): Long
}

data class LiteralPacket(val version: Int, val typeId: Int, val value: Long) : Packet(version, typeId) {
    override fun sumVersions() = version
    override fun resolve() = value
}

data class OperatorPacket(
    val version: Int, val typeId: Int, val subpackets: List<Packet>
) : Packet(version, typeId) {
    override fun sumVersions() = version + subpackets.sumOf { it.sumVersions() }
    override fun resolve() = when(typeId) {
        0 -> subpackets.sumOf(Packet::resolve)
        1 -> subpackets.map(Packet::resolve).reduce(Long::times)
        2 -> subpackets.minOf(Packet::resolve)
        3 -> subpackets.maxOf(Packet::resolve)
        5 -> subpackets.map(Packet::resolve).let { if (it[0] > it[1]) 1 else 0 }
        6 -> subpackets.map(Packet::resolve).let { if (it[0] < it[1]) 1 else 0 }
        7 -> subpackets.map(Packet::resolve).let { if (it[0] == it[1]) 1 else 0 }
        else -> throw IllegalArgumentException("Cannot resolve $typeId")
    }
}

fun Char.toBitSequence() = this
    .digitToInt(16)
    .toString(2)
    .padStart(4, '0')
    .asSequence()

fun String.toBitSequence() = this.asSequence().flatMap(Char::toBitSequence)

fun <T> Iterator<T>.next(q: Int) = (0 until q).map { this.next() }

fun Iterator<Char>.parseVersion() = this.next(3).joinToString("").toInt(2)
fun Iterator<Char>.parseType() = this.next(3).joinToString("").toInt(2)
fun Iterator<Char>.parseLiteralValue() = sequence {
    while (true) {
        val chunk = this@parseLiteralValue.next(5)
        yieldAll(chunk.slice(1 until 5))
        if (chunk[0] == '0') {
            break
        }
    }
}.joinToString("").toLong(2)

fun Iterator<Char>.parseSubpacketsWithRawLength(rawLength: Int) = this.next(rawLength)
    .iterator()
    .let {
        sequence {
            while (it.hasNext()) {
                try {
                    yield(it.parsePacket())
                } catch (e: NoSuchElementException) {
                    // pass
                }
            }
        }
    }.toList()


fun Iterator<Char>.parseSubpackets(numPackets: Int) = (1..numPackets).map { this.parsePacket() }

fun Iterator<Char>.parseChildren() = when (this.next()) {
    '0' -> parseSubpacketsWithRawLength(this.next(15).joinToString("").toInt(2))
    '1' -> parseSubpackets(this.next(11).joinToString("").toInt(2))
    else -> throw IllegalArgumentException("Cannot interpret character")
}

fun Iterator<Char>.parsePacket(): Packet {
    val version = this.parseVersion()
    val type = this.parseType()
    return when (type) {
        4 -> LiteralPacket(version, type, this.parseLiteralValue())
        else -> OperatorPacket(version, type, this.parseChildren())
    }
}

fun String.toPacket() = this.toBitSequence().iterator().parsePacket()

fun main() {
    val packet = getPuzzleInput(16, 1).toPacket()
    val p1Result = packet.sumVersions()
    val p2Result = packet.resolve()
    println(p1Result)
    println(p2Result)
}