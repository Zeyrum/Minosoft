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

package de.bixilon.minosoft.gui.rendering.system.base.texture

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.gui.atlas.TextureLikeTexture
import de.bixilon.minosoft.gui.rendering.system.base.shader.NativeShader
import de.bixilon.minosoft.gui.rendering.system.base.shader.ShaderUniforms
import de.bixilon.minosoft.gui.rendering.system.base.texture.dynamic.DynamicTextureArray
import de.bixilon.minosoft.gui.rendering.system.base.texture.skin.SkinManager
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.minosoft

abstract class TextureManager {
    abstract val staticTextures: StaticTextureArray
    abstract val dynamicTextures: DynamicTextureArray

    lateinit var debugTexture: AbstractTexture
        private set
    lateinit var whiteTexture: TextureLikeTexture
        private set
    lateinit var skins: SkinManager
        private set

    fun loadDefaultTextures() {
        if (this::debugTexture.isInitialized) {
            throw IllegalStateException("Already initialized!")
        }
        debugTexture = staticTextures.createTexture(RenderConstants.DEBUG_TEXTURE_RESOURCE_LOCATION)
        whiteTexture = TextureLikeTexture(texture = staticTextures.createTexture(minosoft("white").texture()), uvStart = Vec2(0.0f, 0.0f), uvEnd = Vec2(0.001f, 0.001f), size = Vec2i(16, 16))
    }

    fun initializeSkins(connection: PlayConnection) {
        skins = SkinManager(this)
        skins.initialize(connection.account, connection.assetsManager)
    }

    fun use(shader: NativeShader, name: String = ShaderUniforms.TEXTURES) {
        staticTextures.use(shader, name)
        dynamicTextures.use(shader, name)
    }
}
