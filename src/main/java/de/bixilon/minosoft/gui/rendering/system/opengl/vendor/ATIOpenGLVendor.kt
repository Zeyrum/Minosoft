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

package de.bixilon.minosoft.gui.rendering.system.opengl.vendor

import org.lwjgl.opengl.ATIMeminfo.GL_VBO_FREE_MEMORY_ATI
import org.lwjgl.opengl.GL11.glGetInteger

object ATIOpenGLVendor : OpenGLVendor {
    override val strict: Boolean = false
    override val shaderDefine: String = "__ATI"

    override val availableVRAM: Long
        get() = glGetInteger(GL_VBO_FREE_MEMORY_ATI).toLong() * 1024
}
