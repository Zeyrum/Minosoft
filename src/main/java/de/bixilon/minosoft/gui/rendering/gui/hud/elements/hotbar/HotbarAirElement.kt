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

package de.bixilon.minosoft.gui.rendering.gui.hud.elements.hotbar

import de.bixilon.minosoft.data.registries.fluid.DefaultFluids
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.ImageElement
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.util.vec.Vec2Util.EMPTY
import glm_.vec2.Vec2i

class HotbarAirElement(hudRenderer: HUDRenderer) : Element(hudRenderer) {
    private val water = hudRenderer.connection.registries.fluidRegistry[DefaultFluids.WATER]!!
    private val airBubble = hudRenderer.atlasManager["minecraft:air_bubble"]!!
    private val poppingAirBubble = hudRenderer.atlasManager["minecraft:popping_air_bubble"]!!

    init {
        forceSilentApply()
    }

    private var bubbles = 0
    private var lastBubblePopping = false

    override fun forceRender(offset: Vec2i, z: Int, consumer: GUIVertexConsumer): Int {
        if (bubbles <= 0) {
            return 0
        }

        for (i in bubbles - 1 downTo 0) {
            var atlasElement = airBubble
            if (i == 0 && lastBubblePopping) {
                atlasElement = poppingAirBubble
            }

            val image = ImageElement(hudRenderer, atlasElement)

            image.render(offset + Vec2i(i * BUBBLE_SIZE.x, 0), z, consumer)
        }

        return 1
    }

    override fun silentApply(): Boolean {
        val player = hudRenderer.connection.player

        val submergedFluid = player.submergedFluid

        var bubbles = 0

        if (submergedFluid == water) {
            bubbles = 10 // ToDo: Get air and check time, etc
        }


        if (this.bubbles == bubbles) {
            return false
        }

        this.bubbles = bubbles

        forceSilentApply()

        return true
    }

    override fun forceSilentApply() {
        size = if (bubbles <= 0.0f) { // ToDo: This notifies the parent, should we really notify it in silentApply?
            Vec2i.EMPTY
        } else {
            Vec2i(BUBBLE_SIZE.x * bubbles, BUBBLE_SIZE.y)
        }
        cacheUpToDate = false
    }

    override fun tick() {
        apply()
    }

    companion object {
        private val BUBBLE_SIZE = Vec2i(8, 9)
    }
}
