data class Node(val id: String) {
    val edges: MutableSet<Node> = mutableSetOf()

    fun addEdgeTo(to: Node) {
        if (to.id != this.id) this.edges.add(to)
    }

    fun isBigCave() = id.uppercase() == id
    fun canVisitMoreThanOnce() = id !in setOf("start", "end")

    override fun toString() = "Node(id=$id, edges=${edges.map { it.id }.toList()})"
}

data class Route(val nodes: List<Node>) {
    fun withNodeAppended(node: Node) = Route(nodes + node)
    fun withNodePrepended(node: Node) = Route(listOf(node) + nodes)

    fun visitsSmallCaveTwice() = nodes.filter { !it.isBigCave() }.groupBy { it }.any { it.value.size > 1 }

    fun canIncludeNodeP1(node: Node) = node.isBigCave() || !nodes.contains(node)


    fun canIncludeNodeP2(node: Node): Boolean {
        if (node.isBigCave()) {
            return true
        }
        if (node.canVisitMoreThanOnce()) {
            return !this.visitsSmallCaveTwice()
        }
        return !nodes.contains(node)
    }
}

fun generateRoutesFrom(node: Node, bannedNodes: Set<Node> = setOf()): Sequence<Route> = sequence {
    yield(Route(listOf(node)))
    for (n in node.edges - bannedNodes) {
        yieldAll(generateRoutesFrom(n, if (node.isBigCave()) bannedNodes else bannedNodes + node)
            .filter { it.canIncludeNodeP1(node) }
            .map { it.withNodePrepended(node) })
    }
}

fun generateRoutesWithOneRevisit(currentRoute: Route): Sequence<Route> = sequence {
    for (n in currentRoute.nodes.last().edges) {
        if (n.isBigCave() ||
            (n.canVisitMoreThanOnce() && !currentRoute.visitsSmallCaveTwice()) ||
            !currentRoute.nodes.contains(n)
        ) {
            val nextRoute = currentRoute.withNodeAppended(n)
            yield(nextRoute)
            yieldAll(generateRoutesWithOneRevisit(nextRoute))
        }
    }
}

fun main() {
    val rawNodeMap = getPuzzleInput(12, 1)
        .split("[\n\\-]".toRegex())
        .distinct()
        .map { it to Node(it) }
        .toMap().toMutableMap()
    getPuzzleInput(12, 1)
        .split("\n")
        .map { it.split("-") }
        .forEach {
            rawNodeMap[it[0]]!!.addEdgeTo(rawNodeMap[it[1]]!!)
            rawNodeMap[it[1]]!!.addEdgeTo(rawNodeMap[it[0]]!!)
        }
    println(rawNodeMap)

    val numRoutesP1 = generateRoutesFrom(rawNodeMap["start"]!!)
        .filter { it.nodes.last() == rawNodeMap["end"] }
        .count()
    println("Part 1 has $numRoutesP1 routes")

    val numRoutesP2 = generateRoutesWithOneRevisit(Route(listOf(rawNodeMap["start"]!!)))
        .filter { it.nodes.last() == rawNodeMap["end"] }
        .count()
    println("Part 2 has $numRoutesP2 routes")
}