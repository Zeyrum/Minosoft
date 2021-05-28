/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.util

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.Directions
import de.bixilon.minosoft.data.mappings.blocks.RandomOffsetTypes
import de.bixilon.minosoft.data.mappings.blocks.types.Block
import de.bixilon.minosoft.gui.rendering.chunk.models.AABB
import de.bixilon.minosoft.gui.rendering.chunk.models.loading.BlockModelElement
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.MMath.ceilInt
import de.bixilon.minosoft.util.MMath.floorInt
import glm_.func.common.clamp
import glm_.func.common.floor
import glm_.func.cos
import glm_.func.sin
import glm_.glm
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec3.Vec3i

object VecUtil {
    val Vec3.Companion.EMPTY: Vec3
        get() = Vec3(0, 0, 0)

    val Vec3i.Companion.EMPTY: Vec3i
        get() = Vec3i(0, 0, 0)

    val Vec3.Companion.ONE: Vec3
        get() = Vec3(1, 1, 1)

    fun JsonElement.toVec3(): Vec3 {
        return when (this) {
            is JsonArray -> Vec3(this[0].asFloat, this[1].asFloat, this[2].asFloat)
            is JsonObject -> Vec3(this["x"]?.asFloat ?: 0, this["y"]?.asFloat ?: 0, this["z"]?.asFloat ?: 0)
            else -> throw IllegalArgumentException("Not a Vec3!")
        }
    }

    fun Vec3.clear() {
        x = 0.0f
        y = 0.0f
        z = 0.0f
    }

    infix fun Vec3.assign(vec3: Vec3) {
        x = vec3.x
        y = vec3.y
        z = vec3.z
    }

    @JvmName(name = "times2")
    infix operator fun Vec3.times(lambda: () -> Float): Vec3 {
        return Vec3(
            x = x * lambda(),
            y = y * lambda(),
            z = z * lambda(),
        )
    }

    infix operator fun Vec3.plus(lambda: () -> Float): Vec3 {
        return Vec3(
            x = x + lambda(),
            y = y + lambda(),
            z = z + lambda(),
        )
    }

    infix operator fun Vec3i.plus(lambda: () -> Int): Vec3i {
        return Vec3i(
            x = x + lambda(),
            y = y + lambda(),
            z = z + lambda(),
        )
    }

    infix operator fun Vec3i.minus(lambda: () -> Int): Vec3i {
        return Vec3i(
            x = x - lambda(),
            y = y - lambda(),
            z = z - lambda(),
        )
    }

    infix operator fun Vec3.plusAssign(lambda: () -> Float) {
        this assign this + lambda
    }

    infix operator fun Vec3.timesAssign(lambda: () -> Float) {
        this assign this * lambda
    }

    val Float.sqr: Float
        get() = this * this

    val Vec3.ticks: Vec3
        get() = this / ProtocolDefinition.TICKS_PER_SECOND

    val Vec3.millis: Vec3
        get() = this * ProtocolDefinition.TICKS_PER_SECOND

    fun getRotatedValues(x: Float, y: Float, sin: Float, cos: Float, rescale: Boolean): Vec2 {
        val result = Vec2(x * cos - y * sin, x * sin + y * cos)
        if (rescale) {
            return result / cos
        }
        return result
    }

    fun Vec3.rotate(angle: Float, axis: Axes): Vec3 {
        return this.rotate(angle, axis, false)
    }

    fun Vec3.rotate(angle: Float, axis: Axes, rescale: Boolean): Vec3 {
        if (angle == 0.0f) {
            return this
        }
        return when (axis) {
            Axes.X -> {
                val rotatedValues = getRotatedValues(this.y, this.z, angle.sin, angle.cos, rescale)
                Vec3(this.x, rotatedValues)
            }
            Axes.Y -> {
                val rotatedValues = getRotatedValues(this.x, this.z, angle.sin, angle.cos, rescale)
                Vec3(rotatedValues.x, this.y, rotatedValues.y)
            }
            Axes.Z -> {
                val rotatedValues = getRotatedValues(this.x, this.y, angle.sin, angle.cos, rescale)
                Vec3(rotatedValues.x, rotatedValues.y, this.z)
            }
        }
    }

    fun Vec3.rotate(axis: Vec3, sin: Float, cos: Float): Vec3 {
        return this * cos + (axis cross this) * sin + axis * (axis dot this) * (1 - cos)
    }

    fun JsonArray.readUV(): Pair<Vec2, Vec2> {
        return Pair(Vec2(this[0].asFloat, BlockModelElement.BLOCK_RESOLUTION - this[1].asFloat), Vec2(this[2].asFloat, BlockModelElement.BLOCK_RESOLUTION - this[3].asFloat))
    }

    fun Int.chunkPosition(multiplier: Int): Int {
        return if (this >= 0) {
            this / multiplier
        } else {
            ((this + 1) / multiplier) - 1
        }
    }

    val Vec3i.chunkPosition: Vec2i
        get() = Vec2i(this.x.chunkPosition(ProtocolDefinition.SECTION_WIDTH_X), this.z.chunkPosition(ProtocolDefinition.SECTION_WIDTH_Z))

    fun Int.inChunkPosition(multiplier: Int): Int {
        var coordinate: Int = this % multiplier
        if (coordinate < 0) {
            coordinate += multiplier
        }
        return coordinate
    }

    val Vec3i.inChunkPosition: Vec3i
        get() = Vec3i(this.x.inChunkPosition(ProtocolDefinition.SECTION_WIDTH_X), y, this.z.inChunkPosition(ProtocolDefinition.SECTION_WIDTH_Z))

    val Vec3i.inChunkSectionPosition: Vec3i
        get() {
            val inVec2i = inChunkPosition
            val y = if (y < 0) {
                ((ProtocolDefinition.SECTION_HEIGHT_Y + (y % ProtocolDefinition.SECTION_HEIGHT_Y))) % ProtocolDefinition.SECTION_HEIGHT_Y
            } else {
                y % ProtocolDefinition.SECTION_HEIGHT_Y
            }
            return Vec3i(inVec2i.x, y, inVec2i.z)
        }

    val Vec3i.sectionHeight: Int
        get() {
            return if (y < 0) {
                (y + 1) / ProtocolDefinition.SECTION_HEIGHT_Y - 1
            } else {
                y / ProtocolDefinition.SECTION_HEIGHT_Y
            }
        }

    val Vec3i.entityPosition: Vec3
        get() = Vec3(x + 0.5f, y, z + 0.5f) // ToDo: Confirm

    val Vec3.blockPosition: Vec3i
        get() = Vec3i((x - 0.5f).toInt(), y.toInt(), (z - 0.5f).toInt()) // ToDo: Confirm

    val Vec3i.center: Vec3
        get() = Vec3(x + 0.5f, y + 0.5f, z + 0.5f) // ToDo: Confirm

    fun Vec3i.Companion.of(chunkPosition: Vec2i, sectionHeight: Int, inChunkSectionPosition: Vec3i): Vec3i {
        return Vec3i(
            chunkPosition.x * ProtocolDefinition.SECTION_WIDTH_X + inChunkSectionPosition.x,
            sectionHeight * ProtocolDefinition.SECTION_HEIGHT_Y + inChunkSectionPosition.y,
            chunkPosition.y * ProtocolDefinition.SECTION_WIDTH_Z + inChunkSectionPosition.z
        ) // ToDo: Confirm
    }

    infix operator fun Vec3i.plus(vec3: Vec3i?): Vec3i {
        if (vec3 == null) {
            return this
        }
        return Vec3i((x + vec3.x), (y + vec3.y), (z + vec3.z))
    }

    infix operator fun Vec3i.plus(vec2: Vec2i?): Vec3i {
        if (vec2 == null) {
            return this
        }
        return Vec3i((x + vec2.x), y, (z + vec2.y))
    }

    infix operator fun Vec3i.plus(direction: Directions?): Vec3i {
        return this + direction?.directionVector
    }

    infix operator fun Vec3i.plus(input: Vec3): Vec3 {
        return Vec3(input.x + x, input.y + y, input.z + z)
    }

    infix operator fun Vec2i.plus(vec3: Vec3i): Vec2i {
        return Vec2i(x + vec3.x, y + vec3.z)
    }

    infix operator fun Vec2i.plus(direction: Directions): Vec2i {
        return this + direction.directionVector
    }

    fun Vec3i.getWorldOffset(block: Block): Vec3 {
        if (block.randomOffsetType == null || !Minosoft.config.config.game.other.flowerRandomOffset) {
            return Vec3.EMPTY
        }

        val positionHash = generatePositionHash(x, 0, z)
        val maxModelOffset = 0.25f // ToDo: use block.model.max_model_offset

        fun horizontal(axisHash: Long): Float {
            return (((axisHash and 0xF) / 15.0f) - 0.5f) / 2.0f
        }

        return Vec3(
            x = horizontal(positionHash),
            y = if (block.randomOffsetType === RandomOffsetTypes.XYZ) {
                (((positionHash shr 4 and 0xF) / 15.0f) - 1.0f) / 5.0f
            } else {
                0.0f
            },
            z = horizontal(positionHash shr 8)).clamp(-maxModelOffset, maxModelOffset)
    }

    private fun Vec3.clamp(min: Float, max: Float): Vec3 {
        return Vec3(
            x = x.clamp(min, max),
            y = y.clamp(min, max),
            z = z.clamp(min, max),
        )
    }

    private fun generatePositionHash(x: Int, y: Int, z: Int): Long {
        var hash = (x * 3129871L) xor z.toLong() * 116129781L xor y.toLong()
        hash = hash * hash * 42317861L + hash * 11L
        return hash shr 16
    }

    fun getDistanceToNextIntegerAxisInDirection(position: Vec3, direction: Vec3): Float {
        fun getTarget(direction: Vec3, position: Vec3, axis: Axes): Int {
            return if (direction[axis] > 0) {
                position[axis].floorInt + 1
            } else {
                position[axis].ceilInt - 1
            }
        }

        fun getLengthMultiplier(direction: Vec3, position: Vec3, axis: Axes): Float {
            return (getTarget(direction, position, axis) - position[axis]) / direction[axis]
        }

        val directionXDistance = getLengthMultiplier(direction, position, Axes.X)
        val directionYDistance = getLengthMultiplier(direction, position, Axes.Y)
        val directionZDistance = getLengthMultiplier(direction, position, Axes.Z)
        return glm.min(directionXDistance, directionYDistance, directionZDistance)
    }

    val Vec3.min: Float
        get() = glm.min(this.x, this.y, this.z)

    val Vec3.max: Float
        get() = glm.max(this.x, this.y, this.z)

    val Vec3.signs: Vec3
        get() = Vec3(glm.sign(this.x), glm.sign(this.y), glm.sign(this.z))

    val Vec3.floor: Vec3i
        get() = Vec3i(this.x.floor, this.y.floor, this.z.floor)

    fun Vec3.getMinDistanceDirection(aabb: AABB): Directions {
        var minDistance = Float.MAX_VALUE
        var minDistanceDirection = Directions.UP
        fun getDistance(position: Vec3, direction: Directions): Float {
            val axis = direction.axis
            return (position[axis] - this[axis]) * -direction[axis]
        }
        for (direction in Directions.VALUES) {
            val distance = if (direction[direction.axis] > 0f) {
                getDistance(aabb.max, direction)
            } else {
                getDistance(aabb.min, direction)
            }
            if (distance < minDistance) {
                minDistance = distance
                minDistanceDirection = direction
            }
        }
        return minDistanceDirection
    }

    val Vec3i.toVec3: Vec3
        get() = Vec3(this)

    operator fun Vec3.get(axis: Axes): Float {
        return when (axis) {
            Axes.X -> this.x
            Axes.Y -> this.y
            Axes.Z -> this.z
        }
    }

    operator fun Vec3i.get(axis: Axes): Int {
        return when (axis) {
            Axes.X -> this.x
            Axes.Y -> this.y
            Axes.Z -> this.z
        }
    }
}
