class IllegalCharacterException(val character: Char, message: String? = "Illegal character, got $character") :
    Exception(message) {
    fun score() = when (this.character) {
        ')' -> 3
        ']' -> 57
        '}' -> 1197
        '>' -> 25137
        else -> throw IllegalArgumentException("${this.character} is not a valid illegal character!!")
    }
}

class IncompleteLineException(val line: String, message: String? = "Line '$line' is incomplete") :
    Exception(message)

data class Chunk(val children: List<Chunk>)

data class AutocompleteSuggestion(val suffix: String) {
    fun score() = suffix.fold(0L) { acc, character -> (5 * acc) + when(character) {
        ')' -> 1
        ']' -> 2
        '}' -> 3
        '>' -> 4
        else -> throw IllegalArgumentException("${character} cannot be scored")
    } }
}

val expectedClose = mapOf('(' to ')', '[' to ']', '{' to '}', '<' to '>')

fun String.toChunks(): Sequence<Chunk> = sequence {
    val input = this@toChunks
    var i = 0
    fun parseChunk(): Sequence<Chunk> = sequence {
        var children = mutableListOf<Chunk>()
        val openingChar = input[i]
        i++
        if (i >= input.length) throw IncompleteLineException(input)

        while (i < input.length && input[i] in expectedClose.keys) {
            children.addAll(parseChunk())
        }
        if (i >= input.length) throw IncompleteLineException(input)
        if (input[i] == expectedClose[openingChar]) {
            i++
            yield(Chunk(children))
        } else {
            throw IllegalCharacterException(input[i])
        }
    }
    while (i < input.length) {
        yieldAll(parseChunk())
    }
}

fun String.toAutocompleteSuggestion(): AutocompleteSuggestion {
    val input = this
    var i = 0
    fun getAutocompleteSuggestion(): String {
        var suggestion = ""
        val openingChar = input[i]
        i++
        if (i >= input.length) return suggestion + expectedClose[openingChar]

        while (i < input.length && input[i] in expectedClose.keys) {
            suggestion += getAutocompleteSuggestion()
        }
        if (i >= input.length) return suggestion + expectedClose[openingChar]
        if (input[i] == expectedClose[openingChar]) {
            i++
        }
        return suggestion
    }
    var suggestion = ""
    while (suggestion == "") {
        suggestion = getAutocompleteSuggestion()
    }
    return AutocompleteSuggestion(suggestion)
}

fun main() {
    val input = getPuzzleInput(10, 1)
    var score = 0
    val incompleteLines = mutableListOf<String>()
    for (l in input.split("\n")) {
        try {
            l.toChunks().toList()
            println("$l")
        } catch (e: IllegalCharacterException) {
            score += e.score()
        } catch (e: IncompleteLineException) {
            incompleteLines.add(l)
        }

    }
    println("Illegal character score $score")
    val suggestionScores = incompleteLines.map { it.toAutocompleteSuggestion().score() }.sorted()
    println("The middle autocomplete score is ${suggestionScores[(suggestionScores.size) / 2]}")
}