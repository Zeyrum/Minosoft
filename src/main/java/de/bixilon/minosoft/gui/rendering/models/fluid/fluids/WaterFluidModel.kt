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

package de.bixilon.minosoft.gui.rendering.models.fluid.fluids

import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.models.fluid.FluidModel
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureTransparencies
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import de.bixilon.minosoft.gui.rendering.tint.TintProvider
import de.bixilon.minosoft.gui.rendering.tint.tints.fluid.WaterTintProvider

class WaterFluidModel : FluidModel {
    override val tint: TintProvider = WaterTintProvider

    override var still: Texture = unsafeNull()
    override var flowing: Texture = unsafeNull()
    override val transparency = TextureTransparencies.TRANSLUCENT// TODO: from texture

    override fun load(context: RenderContext) {
        still = context.textures.static.create(context.models.block.fixTexturePath(STILL).texture())
        flowing = context.textures.static.create(context.models.block.fixTexturePath(FLOWING).texture())
    }

    companion object {
        private val STILL = minecraft("block/water_still")
        private val FLOWING = minecraft("block/water_flow")
    }
}
