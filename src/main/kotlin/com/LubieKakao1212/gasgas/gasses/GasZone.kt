package com.LubieKakao1212.gasgas.gasses

import com.LubieKakao1212.gasgas.util.chainToList
import com.LubieKakao1212.qulib.libs.joml.Vector3d
import com.LubieKakao1212.qulib.math.extensions.from
import com.LubieKakao1212.qulib.math.extensions.minus
import com.LubieKakao1212.qulib.math.extensions.toBlockPos
import com.LubieKakao1212.qulib.physics.raycast.raycastGridUntil
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import java.util.PriorityQueue


fun calculateZone(level : Level, source : BlockPos, volume : UInt) : List<BlockPos> {
    val expansionQueue = PriorityQueue(Node::compare)
    val visitedSet = mutableSetOf<BlockPos>()

    expansionQueue.add(Node(source, 0.0, null, null, 0))

    val visited = mutableListOf<BlockPos>()

    for(i in 0u..volume) {
        val currentNode = expansionQueue.poll()

        visited.add(currentNode.pos)
        visitedSet.add(currentNode.pos)

        val currentPos = Vector3d().from(currentNode.pos)

        val append : (BlockPos) -> Unit = append@ { targetPos ->
            if(!visitedSet.contains((targetPos))) {
                visitedSet.add(targetPos)
                if(!isPassable(level, targetPos)) {
                    return@append
                }
                val target = Vector3d().from(targetPos)

                var newBend = currentNode.lastBend

                val newNode = currentNode.lastBend?.let { bend ->
                    val bPos = Vector3d().from(bend.pos)
                    val delta = bPos - target
                    val dir = delta.normalize(Vector3d())

                    var notHit = true
                    var distanceToBend = delta.length()

                    raycastGridUntil(target, dir, distanceToBend, true) {
                        notHit = isPassable(level, it.target.toBlockPos())
                        notHit
                    }

                    //TODO raycast path from last bend
                    if(!notHit) {
                        newBend = currentNode
                        distanceToBend = 1.0 + (bPos - currentPos).length()
                    }

                    Node(targetPos, distanceToBend, currentNode, newBend, currentNode.order + 1)
                }

                expansionQueue.add(newNode)
            }
        }

        append(currentNode.pos.above())
        append(currentNode.pos.below())
        append(currentNode.pos.north())
        append(currentNode.pos.east())
        append(currentNode.pos.south())
        append(currentNode.pos.west())
    }

    return visited
}

private fun isPassable(level : Level, pos: BlockPos) : Boolean {
    return level.getBlockState(pos).isAir
}


private class Node(val pos: BlockPos, val distance: Double, val previous : Node?, val lastBend : Node?, val order : Int) {

    val chain : List<Node>
        get() {
            return chainToList(order) {
                it.previous
            }
        }

    val bendChain : List<Node>
        get() {
            return chainToList(1) {
                it.lastBend
            }
        }

    fun compare(other : Node) :  Int {
        return distance.compareTo(other.distance)
    }

    fun compareRev(other : Node) :  Int {
        return other.compare(this)
    }
}