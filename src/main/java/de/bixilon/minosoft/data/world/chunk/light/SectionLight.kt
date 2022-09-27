/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.world.chunk.light

import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.blocks.light.TransparentProperty
import de.bixilon.minosoft.data.world.chunk.Chunk
import de.bixilon.minosoft.data.world.chunk.ChunkNeighbours
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.chunk.ChunkSection.Companion.getIndex
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition

class SectionLight(
    val section: ChunkSection,
    var light: ByteArray = ByteArray(ProtocolDefinition.BLOCKS_PER_SECTION), // packed (skyLight: 0xF0, blockLight: 0x0F)
) : AbstractSectionLight() {

    fun onBlockChange(x: Int, y: Int, z: Int, previous: BlockState?, now: BlockState?) {
        val previousLuminance = previous?.luminance ?: 0
        val luminance = now?.luminance ?: 0

        if (previousLuminance == luminance) {
            if (previous?.lightProperties?.propagatesLight == now?.lightProperties?.propagatesLight) {
                // no change for light data
                return
            }
            if (now == null || now.lightProperties.propagatesLight) {
                // block got destroyed/is propagating light now
                propagateFromNeighbours(x, y, z)
                return
            }
            // ToDo: else decrease light around placed block
        }

        if (luminance > previousLuminance) {
            traceIncrease(x, y, z, luminance, null)
        } else {
            startDecreaseTrace(x, y, z)
        }
    }

    private fun startDecreaseTrace(x: Int, y: Int, z: Int) {
        // that is kind of hacky, but far easier and kind of faster
        val light = this.light[getIndex(x, y, z)].toInt() and BLOCK_LIGHT_MASK

        decreaseLight(x, y, z, light, true) // just clear the light
        decreaseLight(x, y, z, light, false) // increase the light in all sections
    }

    private fun decreaseLight(x: Int, y: Int, z: Int, light: Int, reset: Boolean) {
        decreaseCheckLevel(x, z, light, reset)

        val neighbours = section.neighbours ?: return
        if (y - light < 0) {
            neighbours[Directions.O_DOWN]?.light?.decreaseCheckLevel(x, z, light - y, reset)
        }
        if (y + light > ProtocolDefinition.SECTION_MAX_Y) {
            neighbours[Directions.O_UP]?.light?.decreaseCheckLevel(x, z, light - (ProtocolDefinition.SECTION_MAX_Y - y), reset)
        }
    }

    private fun decreaseCheckLevel(x: Int, z: Int, light: Int, reset: Boolean) {
        decreaseCheckX(z, light, reset)
        val neighbours = section.neighbours ?: return

        if (x - light < 0) {
            neighbours[Directions.O_WEST]?.light?.decreaseCheckX(z, light - x, reset)
        }
        if (x + light > ProtocolDefinition.SECTION_MAX_X) {
            neighbours[Directions.O_EAST]?.light?.decreaseCheckX(z, light - (ProtocolDefinition.SECTION_MAX_X - x), reset)
        }
    }

    private fun decreaseCheckX(z: Int, light: Int, reset: Boolean) {
        val neighbours = section.neighbours ?: return
        if (reset) resetLight() else calculate()

        if (z - light < 0) {
            val neighbour = neighbours[Directions.O_NORTH]?.light
            if (reset) neighbour?.resetLight() else neighbour?.calculate()
        }
        if (z + light > ProtocolDefinition.SECTION_MAX_Z) {
            val neighbour = neighbours[Directions.O_SOUTH]?.light
            if (reset) neighbour?.resetLight() else neighbour?.calculate()
        }
    }

    fun traceIncrease(x: Int, y: Int, z: Int, nextLuminance: Int, source: Directions?) {
        val index = getIndex(x, y, z)
        val block = section.blocks.unsafeGet(index)
        val lightProperties = block?.lightProperties ?: TransparentProperty
        val blockLuminance = block?.luminance ?: 0
        if (block != null && !lightProperties.propagatesLight && blockLuminance == 0) {
            // light can not pass through the block
            return
        }

        // get block or next luminance level
        val currentLight = light[index].toInt() and BLOCK_LIGHT_MASK // we just care about block light
        if (currentLight >= nextLuminance) {
            // light is already higher, no need to trace
            return
        }
        this.light[index] = ((this.light[index].toInt() and SKY_LIGHT_MASK) or nextLuminance).toByte() // keep the sky light set
        if (!update) {
            update = true
        }
        val chunk = section.chunk ?: return
        val chunkNeighbours = chunk.neighbours ?: return
        val neighbours = section.neighbours ?: return

        if (nextLuminance == 1) {
            // we can not further increase the light
            // set neighbour update, cullface might change lighting properties
            if (y == 0) neighbours[Directions.O_DOWN]?.light?.update = true
            if (y == ProtocolDefinition.SECTION_MAX_Y) neighbours[Directions.O_UP]?.light?.update = true
            if (z == 0) neighbours[Directions.O_NORTH]?.light?.update = true
            if (z == ProtocolDefinition.SECTION_MAX_Z) neighbours[Directions.O_SOUTH]?.light?.update = true
            if (x == 0) neighbours[Directions.O_WEST]?.light?.update = true
            if (x == ProtocolDefinition.SECTION_MAX_X) neighbours[Directions.O_EAST]?.light?.update = true
            return
        }


        if (blockLuminance > nextLuminance) {
            // we only want to set our own light sources
            return
        }

        val neighbourLuminance = nextLuminance - 1

        if (source == null || (source != Directions.DOWN && lightProperties.propagatesLight(source, Directions.DOWN))) {
            if (y > 0) {
                traceIncrease(x, y - 1, z, neighbourLuminance, Directions.UP)
            } else if (section.sectionHeight == chunk.lowestSection) {
                chunk.bottomLight.traceIncrease(x, z, neighbourLuminance)
            } else {
                (neighbours[Directions.O_DOWN] ?: chunk.getOrPut(section.sectionHeight - 1, false))?.light?.traceIncrease(x, ProtocolDefinition.SECTION_MAX_Y, z, neighbourLuminance, Directions.UP)
            }
        }
        if (source == null || (source != Directions.UP && lightProperties.propagatesLight(source, Directions.UP))) {
            if (y < ProtocolDefinition.SECTION_MAX_Y) {
                traceIncrease(x, y + 1, z, neighbourLuminance, Directions.DOWN)
            } else if (section.sectionHeight == chunk.highestSection) {
                chunk.topLight.traceIncrease(x, z, neighbourLuminance)
            } else {
                (neighbours[Directions.O_UP] ?: chunk.getOrPut(section.sectionHeight + 1, false))?.light?.traceIncrease(x, 0, z, neighbourLuminance, Directions.DOWN)
            }
        }

        if (source == null || (source != Directions.NORTH && lightProperties.propagatesLight(source, Directions.NORTH))) {
            if (z > 0) {
                traceIncrease(x, y, z - 1, neighbourLuminance, Directions.SOUTH)
            } else {
                neighbours[Directions.O_NORTH, ChunkNeighbours.NORTH, chunkNeighbours]?.light?.traceIncrease(x, y, ProtocolDefinition.SECTION_MAX_Z, neighbourLuminance, Directions.SOUTH)
            }
        }
        if (source == null || (source != Directions.SOUTH && lightProperties.propagatesLight(source, Directions.SOUTH))) {
            if (z < ProtocolDefinition.SECTION_MAX_Y) {
                traceIncrease(x, y, z + 1, neighbourLuminance, Directions.NORTH)
            } else {
                neighbours[Directions.O_SOUTH, ChunkNeighbours.SOUTH, chunkNeighbours]?.light?.traceIncrease(x, y, 0, neighbourLuminance, Directions.NORTH)
            }
        }
        if (source == null || (source != Directions.WEST && lightProperties.propagatesLight(source, Directions.WEST))) {
            if (x > 0) {
                traceIncrease(x - 1, y, z, neighbourLuminance, Directions.EAST)
            } else {
                neighbours[Directions.O_WEST, ChunkNeighbours.WEST, chunkNeighbours]?.light?.traceIncrease(ProtocolDefinition.SECTION_MAX_X, y, z, neighbourLuminance, Directions.EAST)
            }
        }
        if (source == null || (source != Directions.EAST && lightProperties.propagatesLight(source, Directions.EAST))) {
            if (x < ProtocolDefinition.SECTION_MAX_X) {
                traceIncrease(x + 1, y, z, neighbourLuminance, Directions.WEST)
            } else {
                neighbours[Directions.O_EAST, ChunkNeighbours.EAST, chunkNeighbours]?.light?.traceIncrease(0, y, z, neighbourLuminance, Directions.WEST)
            }
        }
    }

    fun resetLight() {
        update = true
        for (index in light.indices) {
            light[index] = 0x00.toByte()
        }
    }


    fun recalculate() {
        update = true
        resetLight()
        calculate()
    }

    fun calculate() {
        update = true
        val blocks = section.blocks

        blocks.acquire()
        for (y in 0 until ProtocolDefinition.SECTION_HEIGHT_Y) {
            for (z in 0 until ProtocolDefinition.SECTION_WIDTH_Z) {
                for (x in 0 until ProtocolDefinition.SECTION_WIDTH_X) {
                    val index = getIndex(x, y, z)
                    val luminance = blocks.unsafeGet(index)?.luminance ?: continue
                    if (luminance == 0) {
                        // block is not emitting light, ignore it
                        continue
                    }
                    traceIncrease(x, y, z, luminance, null)
                }
            }
        }
        blocks.release()
    }


    override inline operator fun get(index: Int): Byte {
        return light[index]
    }

    fun propagateFromNeighbours() {
        val neighbours = section.neighbours ?: return
        // ToDo(p): this::traceIncrease checks als the block light level, not needed

        // ToDo: Check if current block can propagate into that direction
        val baseY = section.sectionHeight * ProtocolDefinition.SECTION_HEIGHT_Y
        for (x in 0 until ProtocolDefinition.SECTION_WIDTH_X) {
            if (neighbours[Directions.O_DOWN] != null || neighbours[Directions.O_UP] != null) {
                propagateY(neighbours, x, baseY)
            }
            if (neighbours[Directions.O_NORTH] != null || neighbours[Directions.O_SOUTH] != null) {
                propagateZ(baseY, neighbours, x)
            }
        }
        if (neighbours[Directions.O_WEST] != null || neighbours[Directions.O_EAST] != null) {
            propagateX(baseY, neighbours)
        }
    }

    private fun propagateX(baseY: Int, neighbours: Array<ChunkSection?>) {
        for (z in 0 until ProtocolDefinition.SECTION_WIDTH_Z) {
            for (y in 0 until ProtocolDefinition.SECTION_HEIGHT_Y) {
                val totalY = baseY + y
                neighbours[Directions.O_WEST]?.light?.get(ProtocolDefinition.SECTION_MAX_Z, y, z)?.toInt()?.let {
                    if (it == 0) return@let
                    traceIncrease(0, y, z, (it and BLOCK_LIGHT_MASK).dec(), Directions.EAST)
                    traceSkylight(0, y, z, (it and SKY_LIGHT_MASK shr 4).dec(), Directions.EAST, totalY)
                }
                neighbours[Directions.O_EAST]?.light?.get(0, y, z)?.toInt()?.let {
                    if (it == 0) return@let
                    traceIncrease(ProtocolDefinition.SECTION_MAX_X, y, z, (it and BLOCK_LIGHT_MASK).dec(), Directions.WEST)
                    traceSkylight(ProtocolDefinition.SECTION_MAX_X, y, z, (it and SKY_LIGHT_MASK shr 4).dec(), Directions.WEST, totalY)
                }
            }
        }
    }

    private fun propagateZ(baseY: Int, neighbours: Array<ChunkSection?>, x: Int) {
        for (y in 0 until ProtocolDefinition.SECTION_HEIGHT_Y) {
            val totalY = baseY + y
            neighbours[Directions.O_NORTH]?.light?.get(x, y, ProtocolDefinition.SECTION_MAX_Z)?.toInt()?.let {
                if (it == 0) return@let
                traceIncrease(x, y, 0, (it and BLOCK_LIGHT_MASK).dec(), Directions.SOUTH)
                traceSkylight(x, y, 0, (it and SKY_LIGHT_MASK shr 4).dec(), Directions.SOUTH, totalY)
            }
            neighbours[Directions.O_SOUTH]?.light?.get(x, y, 0)?.toInt()?.let {
                if (it == 0) return@let
                traceIncrease(x, y, ProtocolDefinition.SECTION_MAX_Z, (it and BLOCK_LIGHT_MASK).dec(), Directions.NORTH)
                traceSkylight(x, y, ProtocolDefinition.SECTION_MAX_Z, (it and SKY_LIGHT_MASK shr 4).dec(), Directions.NORTH, totalY)
            }
        }
    }

    private fun propagateY(neighbours: Array<ChunkSection?>, x: Int, baseY: Int) {
        for (z in 0 until ProtocolDefinition.SECTION_WIDTH_Z) {
            neighbours[Directions.O_DOWN]?.light?.get(x, ProtocolDefinition.SECTION_MAX_Y, z)?.toInt()?.let {
                if (it == 0) return@let
                traceIncrease(x, 0, z, (it and BLOCK_LIGHT_MASK).dec(), Directions.UP)
                traceSkylight(x, 0, z, (it and SKY_LIGHT_MASK shr 4).dec(), Directions.UP, baseY + 0) // ToDo: Is that possible?
            }
            neighbours[Directions.O_UP]?.light?.get(x, 0, z)?.toInt()?.let {
                if (it == 0) return@let
                traceIncrease(x, ProtocolDefinition.SECTION_MAX_Y, z, (it and BLOCK_LIGHT_MASK).dec(), Directions.DOWN)
                traceSkylight(x, ProtocolDefinition.SECTION_MAX_Y, z, (it and SKY_LIGHT_MASK shr 4).dec(), Directions.DOWN, baseY + ProtocolDefinition.SECTION_MAX_Y)
            }
        }
    }

    private inline fun traceSkylight(x: Int, y: Int, z: Int, nextLevel: Int, direction: Directions?, totalY: Int) {
        return traceSkylight(x, y, z, nextLevel, direction, totalY, true)
    }

    fun traceSkylight(x: Int, y: Int, z: Int, nextLevel: Int, direction: Directions?, totalY: Int, noForce: Boolean) {
        val chunk = section.chunk ?: Broken("chunk == null")
        if (noForce && totalY >= chunk.getMaxHeight(x, z)) {
            // this light level will be 15, don't care
            return
        }
        val chunkNeighbours = chunk.neighbours ?: return
        val index = getIndex(x, y, z)
        val currentLight = this[index].toInt()
        if (noForce && ((currentLight and SKY_LIGHT_MASK) shr 4) >= nextLevel) {
            return
        }

        var lightProperties = section.blocks.unsafeGet(index)?.lightProperties

        if (lightProperties == null) {
            lightProperties = TransparentProperty
        } else if (!lightProperties.propagatesLight) {
            return
        }

        this.light[index] = ((currentLight and BLOCK_LIGHT_MASK) or (nextLevel shl 4)).toByte()

        if (!update) {
            update = true
        }

        if (nextLevel <= 1) {
            return
        }

        val neighbours = this.section.neighbours ?: return
        val nextNeighbourLevel = nextLevel - 1

        if (direction != Directions.UP && (direction == null || lightProperties.propagatesLight(direction, Directions.UP))) {
            if (y > 0) {
                traceSkylight(x, y - 1, z, nextNeighbourLevel, Directions.DOWN, totalY - 1)
            } else if (section.sectionHeight != chunk.highestSection) {
                (neighbours[Directions.O_UP] ?: chunk.getOrPut(section.sectionHeight + 1, false))?.light?.traceSkylight(x, 0, z, nextNeighbourLevel, direction, totalY)
            }
        }
        if (direction != Directions.DOWN && direction != null && (lightProperties.propagatesLight(direction, Directions.DOWN))) {
            if (y < ProtocolDefinition.SECTION_MAX_Y) {
                traceSkylight(x, y + 1, z, nextNeighbourLevel, Directions.UP, totalY + 1)
            } else if (section.sectionHeight == chunk.lowestSection) {
                // ToDo: Trace through bottom light
            } else {
                (neighbours[Directions.O_DOWN] ?: chunk.getOrPut(section.sectionHeight - 1, false))?.light?.traceSkylight(x, ProtocolDefinition.SECTION_MAX_Y, z, nextNeighbourLevel, direction, totalY)
            }
        }
        if (direction != Directions.NORTH && (direction == null || lightProperties.propagatesLight(direction, Directions.NORTH))) {
            if (z > 0) {
                traceSkylight(x, y, z - 1, nextNeighbourLevel, Directions.SOUTH, totalY)
            } else {
                neighbours[Directions.O_NORTH, ChunkNeighbours.NORTH, chunkNeighbours]?.light?.traceSkylight(x, y, ProtocolDefinition.SECTION_MAX_Z, nextNeighbourLevel, Directions.SOUTH, totalY)
            }
        }
        if (direction != Directions.SOUTH && (direction == null || lightProperties.propagatesLight(direction, Directions.SOUTH))) {
            if (z < ProtocolDefinition.SECTION_MAX_Z) {
                traceSkylight(x, y, z + 1, nextNeighbourLevel, Directions.NORTH, totalY)
            } else {
                neighbours[Directions.O_SOUTH, ChunkNeighbours.SOUTH, chunkNeighbours]?.light?.traceSkylight(x, y, 0, nextNeighbourLevel, Directions.NORTH, totalY)
            }
        }
        if (direction != Directions.WEST && (direction == null || lightProperties.propagatesLight(direction, Directions.WEST))) {
            if (x > 0) {
                traceSkylight(x - 1, y, z, nextNeighbourLevel, Directions.EAST, totalY)
            } else {
                neighbours[Directions.O_WEST, ChunkNeighbours.WEST, chunkNeighbours]?.light?.traceSkylight(ProtocolDefinition.SECTION_MAX_X, y, z, nextNeighbourLevel, direction, totalY)
            }
        }
        if (direction != Directions.EAST && (direction == null || lightProperties.propagatesLight(direction, Directions.EAST))) {
            if (x < ProtocolDefinition.SECTION_MAX_X) {
                traceSkylight(x + 1, y, z, nextNeighbourLevel, Directions.WEST, totalY)
            } else {
                neighbours[Directions.O_EAST, ChunkNeighbours.EAST, chunkNeighbours]?.light?.traceSkylight(0, y, z, nextNeighbourLevel, direction, totalY)
            }
        }
    }

    private inline operator fun Array<ChunkSection?>.get(direction: Int, neighbour: Int, neighbours: Array<Chunk>): ChunkSection? {
        return this[direction] ?: neighbours[neighbour].getOrPut(section.sectionHeight, false)
    }

    fun propagateFromNeighbours(x: Int, y: Int, z: Int) {
        val neighbours = section.neighbours ?: return

        var skylight = 0
        var blockLight = 0


        fun pushLight(light: Byte) {
            val nextSkylight = light.toInt() and SKY_LIGHT_MASK shr 4
            if (skylight > nextSkylight) {
                skylight = nextSkylight
            }
            val nextBlockLight = light.toInt() and BLOCK_LIGHT_MASK
            if (blockLight > nextBlockLight) {
                blockLight = nextBlockLight
            }
        }

        // ToDo: check if light can exit block at side or can enter block at neighbpu

        if (x > 0) {
            pushLight(this[x - 1, y, z])
        } else {
            neighbours[Directions.O_WEST]?.light?.get(ProtocolDefinition.SECTION_MAX_X, y, z)?.let { pushLight(it) }
        }
        if (x < ProtocolDefinition.SECTION_MAX_X) {
            pushLight(this[x + 1, y, z])
        } else {
            neighbours[Directions.O_EAST]?.light?.get(0, y, z)?.let { pushLight(it) }
        }

        if (y > 0) {
            pushLight(this[x, y - 1, z])
        } else {
            neighbours[Directions.O_DOWN]?.light?.get(x, ProtocolDefinition.SECTION_MAX_Y, z)?.let { pushLight(it) }
        }

        if (y < ProtocolDefinition.SECTION_MAX_Y) {
            pushLight(this[x, y + 1, z])
        } else {
            neighbours[Directions.O_UP]?.light?.get(x, 0, z)?.let { pushLight(it) }
        }

        if (z > 0) {
            pushLight(this[x, y, z - 1])
        } else {
            neighbours[Directions.O_NORTH]?.light?.get(x, y, ProtocolDefinition.SECTION_MAX_Z)?.let { pushLight(it) }
        }
        if (z < ProtocolDefinition.SECTION_MAX_Z) {
            pushLight(this[x, y, z + 1])
        } else {
            neighbours[Directions.O_SOUTH]?.light?.get(x, y, 0)?.let { pushLight(it) }
        }

        traceIncrease(x, y, z, blockLight - 1, null)
        traceSkylight(x, y, z, skylight - 1, null, section.sectionHeight * ProtocolDefinition.SECTION_MAX_Y + y)
    }

    companion object {
        const val BLOCK_LIGHT_MASK = 0x0F
        const val SKY_LIGHT_MASK = 0xF0
    }
}
