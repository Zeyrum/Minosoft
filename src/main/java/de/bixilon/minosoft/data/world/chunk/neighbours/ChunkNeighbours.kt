/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.world.chunk.neighbours

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.world.biome.accessor.NoiseBiomeAccessor
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.positions.ChunkPositionUtil.chunkPosition
import de.bixilon.minosoft.data.world.positions.SectionHeight
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.inChunkPosition
import de.bixilon.minosoft.protocol.packets.s2c.play.block.chunk.ChunkUtil

class ChunkNeighbours(val chunk: Chunk) : Iterable<Chunk?> {
    val neighbours: Array<Chunk?> = arrayOfNulls(COUNT)
    private var count = 0

    val complete: Boolean get() = count == COUNT

    fun get(): Array<Chunk>? {
        if (count == COUNT) {
            return neighbours.unsafeCast()
        }
        return null
    }

    operator fun set(index: Int, chunk: Chunk) {
        this.chunk.lock.lock()
        val current = neighbours[index]
        neighbours[index] = chunk
        if (current == null) {
            count++
            if (count == COUNT) {
                complete(get()!!)
            }
        }
        this.chunk.lock.unlock()
    }

    operator fun set(offset: Vec2i, chunk: Chunk) {
        set(getIndex(offset), chunk)
    }

    fun remove(index: Int) {
        chunk.lock.lock()
        val current = neighbours[index]
        neighbours[index] = null
        if (current != null) {
            count--
        }
        chunk.lock.unlock()
    }

    fun remove(offset: Vec2i) {
        remove(getIndex(offset))
    }

    fun completeSection(neighbours: Array<Chunk>, section: ChunkSection, sectionHeight: SectionHeight, biomeCacheAccessor: NoiseBiomeAccessor?) {
        section.neighbours = ChunkUtil.getDirectNeighbours(neighbours, chunk, sectionHeight)
        if (biomeCacheAccessor != null) {
            section.buildBiomeCache(neighbours, biomeCacheAccessor)
        }
    }

    private fun complete(neighbours: Array<Chunk>) {
        val biomeCacheAccessor = chunk.world.cacheBiomeAccessor
        for ((index, section) in chunk.sections.withIndex()) {
            if (section == null) continue
            val sectionHeight = index + chunk.minSection
            completeSection(neighbours, section, sectionHeight, biomeCacheAccessor)
        }
        chunk.light.recalculate(false)
        chunk.light.propagateFromNeighbours(fireEvent = false, fireSameChunkEvent = false)
    }


    operator fun get(index: Int): Chunk? {
        return neighbours[index]
    }

    operator fun get(offset: Vec2i): Chunk? {
        if (offset.x == 0 && offset.y == 0) {
            return chunk
        }
        return this[getIndex(offset)]
    }

    override fun iterator(): Iterator<Chunk?> {
        return neighbours.iterator()
    }


    fun update(neighbours: Array<Chunk>, sectionHeight: Int) {
        for (nextSectionHeight in sectionHeight - 1..sectionHeight + 1) {
            if (nextSectionHeight < chunk.minSection || nextSectionHeight > chunk.maxSection) {
                continue
            }

            val section = chunk[nextSectionHeight] ?: continue
            val sectionNeighbours = ChunkUtil.getDirectNeighbours(neighbours, chunk, nextSectionHeight)
            section.neighbours = sectionNeighbours
        }
    }

    fun trace(offset: Vec2i): Chunk? {
        if (offset.x == 0 && offset.y == 0) {
            return chunk
        }

        val chunk = when {
            offset.x > 0 -> {
                offset.x--
                this[6]
            }

            offset.x < 0 -> {
                offset.x++
                this[1]
            }

            offset.y > 0 -> {
                offset.y--
                this[4]
            }

            offset.y < 0 -> {
                offset.y++
                this[3]
            }

            else -> Broken("Can not get chunk from offset: $offset")
        }
        return chunk?.neighbours?.trace(offset)
    }

    fun traceBlock(offset: Vec3i, origin: Vec3i, blockPosition: Vec3i = origin + offset): BlockState? {
        val chunkDelta = (origin - blockPosition).chunkPosition

        return traceBlock(blockPosition.x and 0x0F, blockPosition.y, blockPosition.z and 0x0F, chunkDelta)
    }

    fun traceBlock(offset: Vec3i): BlockState? {
        return traceBlock(offset.inChunkPosition, offset.chunkPosition)
    }

    private fun traceBlock(inChunkPosition: Vec3i, chunkOffset: Vec2i): BlockState? {
        return trace(chunkOffset)?.get(inChunkPosition)
    }

    fun traceBlock(x: Int, y: Int, z: Int, chunkOffset: Vec2i): BlockState? {
        return trace(chunkOffset)?.get(x, y, z)
    }

    companion object {
        const val COUNT = 8
        const val NORTH = 3
        const val SOUTH = 4
        const val WEST = 1
        const val EAST = 6


        /**
         * 0 | 3 | 5
         * 1 | - | 6
         * 2 | 4 | 7
         */

        val OFFSETS = arrayOf(
            Vec2i(-1, -1), // 0
            Vec2i(-1, +0), // 1
            Vec2i(-1, +1), // 2
            Vec2i(+0, -1), // 3
            Vec2i(+0, +1), // 4
            Vec2i(+1, -1), // 5
            Vec2i(+1, +0), // 6
            Vec2i(+1, +1), // 7
        )

        fun getIndex(offset: Vec2i): Int {
            return when {
                offset.x == -1 && offset.y == -1 -> 0
                offset.x == -1 && offset.y == 0 -> 1
                offset.x == -1 && offset.y == 1 -> 2
                offset.x == 0 && offset.y == -1 -> 3
                offset.x == 0 && offset.y == 1 -> 4
                offset.x == 1 && offset.y == -1 -> 5
                offset.x == 1 && offset.y == 0 -> 6
                offset.x == 1 && offset.y == 1 -> 7
                else -> Broken("Can not get neighbour chunk from offset $offset")
            }
        }
    }
}
