import kotlin.math.ceil
import kotlin.math.floor

sealed interface AbstractSnailfishNumber {
    fun magnitude(): Int
    fun add(other: AbstractSnailfishNumber): AbstractSnailfishNumber
    fun applyExplode(): AbstractSnailfishNumber
    fun applySplit(): AbstractSnailfishNumber
    fun addToLeftmost(v: Int): AbstractSnailfishNumber
    fun addToRightmost(v: Int): AbstractSnailfishNumber
}

data class RegularNumber(val value: Int) : AbstractSnailfishNumber {
    override fun magnitude(): Int = value
    override fun add(other: AbstractSnailfishNumber) = throw NotImplementedError("Snailfish don't add regular numbers")
    override fun applyExplode(): AbstractSnailfishNumber = throw NotImplementedError("Regular numbers can't explode")
    override fun applySplit() = if (value >= 10)
        SnailfishNumber(RegularNumber(floor(value / 2.0).toInt()), RegularNumber(ceil(value / 2.0).toInt()))
    else this

    override fun addToLeftmost(v: Int): AbstractSnailfishNumber = RegularNumber(value + v)
    override fun addToRightmost(v: Int): AbstractSnailfishNumber = RegularNumber(value + v)
}

data class SnailfishNumber(val left: AbstractSnailfishNumber, val right: AbstractSnailfishNumber) :
    AbstractSnailfishNumber {
    override fun magnitude(): Int = (3 * left.magnitude()) + (2 * right.magnitude())

    override fun add(other: AbstractSnailfishNumber): AbstractSnailfishNumber {
        var additionResult: AbstractSnailfishNumber = SnailfishNumber(this, other)
        while (true) {
            // explode
            val e = additionResult.applyExplode()
            if (e != additionResult) {
                additionResult = e
                continue
            }

            // splits
            val s = additionResult.applySplit()
            if (s != additionResult) {
                additionResult = s
                continue
            }
            break
        }
        return additionResult
    }

    override fun applySplit(): AbstractSnailfishNumber {
        val l = left.applySplit()
        if (l != left) {
            return SnailfishNumber(l, right)
        }
        return SnailfishNumber(left, right.applySplit())
    }

    override fun applyExplode() = this.applyExplode(0).second

    private fun applyExplode(depth: Int): Triple<Int, AbstractSnailfishNumber, Int> {
        if (left is RegularNumber && right is RegularNumber) {
            if (depth > 3) {
                return Triple(left.value, RegularNumber(0), right.value)
            }
            return Triple(0, this, 0)
        } else {
            if (left is SnailfishNumber) {
                val (l, num, r) = left.applyExplode(depth + 1)
                if (num != left) {
                    return Triple(l, SnailfishNumber(num, right.addToLeftmost(r)), 0)
                }
            }
            if (right is SnailfishNumber) {
                val (l, num, r) = right.applyExplode(depth + 1)
                if (num != right) {
                    return Triple(0, SnailfishNumber(left.addToRightmost(l), num), r)
                }
            }
        }
        return Triple(0, this, 0)
    }

    override fun addToLeftmost(v: Int) = SnailfishNumber(left.addToLeftmost(v), right)

    override fun addToRightmost(v: Int) = SnailfishNumber(left, right.addToRightmost(v))
}

fun String.toSnailfishNumber(): SnailfishNumber {
    var i = 0
    fun parseSnailfishNumber(): SnailfishNumber {
        if (this[i] != '[') throw IllegalArgumentException("Expected to see [ at $i in '$this'")
        i++
        var left: AbstractSnailfishNumber? = null
        if (this[i] == '[') {
            left = parseSnailfishNumber()
        } else {
            left = RegularNumber(this[i].digitToInt())
            i++
        }
        if (this[i] != ',') throw IllegalArgumentException("Expected to see , at $i in '$this'")
        i++
        var right: AbstractSnailfishNumber? = null
        if (this[i] == '[') {
            right = parseSnailfishNumber()
        } else {
            right = RegularNumber(this[i].digitToInt())
            i++
        }
        if (this[i] != ']') throw IllegalArgumentException("Expected to see ] at $i in '$this'")
        i++
        return SnailfishNumber(left, right)

    }
    return parseSnailfishNumber()
}

fun main() {
    val numbers = getPuzzleInput(18, 1).split("\n").map(String::toSnailfishNumber)
    val sum = numbers.reduce(AbstractSnailfishNumber::add)
    println(sum.magnitude())

    println(numbers.flatMap { n1 -> numbers.filter {it != n1}.map { n2 -> n1.add(n2)} }.maxOf { it.magnitude() })
}