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

package de.bixilon.minosoft.gui.rendering.models.block.state.baked

import de.bixilon.kutil.array.ArrayUtil.cast
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.cull.side.FaceProperties
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.cull.side.SideProperties
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureTransparencies

object BakingUtil {

    inline fun <reified T> Array<MutableList<T>>.compact(): Array<Array<T>> {
        val array: Array<Array<T>?> = arrayOfNulls(size)

        for ((index, entries) in this.withIndex()) {
            array[index] = entries.toTypedArray()
        }

        return array.cast()
    }

    fun Array<MutableList<FaceProperties>>.compactProperties(): Array<SideProperties?> {
        val array: Array<SideProperties?> = arrayOfNulls(size)

        for ((index, entries) in this.withIndex()) {
            if (entries.isEmpty()) continue
            val size = entries.toTypedArray()

            // TODO: minify sizes (e.g. grass: there is one with OPAQUE and TRANSPARENT. OPAQUE should be used om that case (same sized))

            var transparency: TextureTransparencies? = null
            var set = false

            for (entry in size) {
                if (!set) {
                    transparency = entry.transparency
                    set = true
                    continue
                }
                if (transparency != entry.transparency) {
                    transparency = null
                    break
                }
            }


            array[index] = SideProperties(size, transparency)
        }

        return array
    }


    fun FloatArray.pushRight(components: Int, steps: Int): FloatArray {
        if (steps == 0 || components == 0) return this
        if (this.size % components != 0) throw IllegalArgumentException("Size mismatch!")
        var steps = steps % (size / components)
        if (steps < 0) steps += size * components

        val target = FloatArray(size)

        // push every value $components*steps right

        val count = components * steps

        for ((index, value) in this.withIndex()) {
            val destination = (index + count) % this.size // TODO: allow negative steps
            target[destination] = value
        }

        return target
    }
}
