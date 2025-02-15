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

package de.bixilon.minosoft.gui.rendering.system.opengl.buffer.vertex

import de.bixilon.minosoft.config.DebugOptions.EMPTY_BUFFERS
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.system.base.buffer.RenderableBufferStates
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.FloatVertexBuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.PrimitiveTypes
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGLRenderSystem
import de.bixilon.minosoft.gui.rendering.system.opengl.buffer.FloatOpenGLBuffer
import de.bixilon.minosoft.gui.rendering.util.mesh.MeshStruct
import org.lwjgl.opengl.GL20.glEnableVertexAttribArray
import org.lwjgl.opengl.GL20.glVertexAttribPointer
import org.lwjgl.opengl.GL30.*
import java.nio.FloatBuffer

class FloatOpenGLVertexBuffer(
    renderSystem: OpenGLRenderSystem,
    override val structure: MeshStruct,
    data: FloatBuffer,
    override val primitiveType: PrimitiveTypes,
) : FloatOpenGLBuffer(renderSystem, data), FloatVertexBuffer {
    override var vertices = -1
        private set
    private var vao = -1

    override fun init() {
        val floatsPerVertex = structure.BYTES_PER_VERTEX / Float.SIZE_BYTES

        vertices = buffer.position() / floatsPerVertex
        vao = glGenVertexArrays()
        super.init()
        glBindVertexArray(vao)

        super.initialUpload()
        bind()

        _data = null


        for (attribute in structure.attributes) {
            glVertexAttribPointer(attribute.index, attribute.size, GL_FLOAT, false, structure.BYTES_PER_VERTEX, attribute.stride)
            glEnableVertexAttribArray(attribute.index)
        }

        unbind()
    }

    override fun unbind() {
        if (RenderConstants.DIRTY_BUFFER_UNBIND) {
            return
        }
        super.unbind()
        glBindVertexArray(0)
    }

    fun bindVao() {
        super.bind()
        if (renderSystem.boundVao == vao) {
            return
        }
        glBindVertexArray(vao)
        renderSystem.boundVao = vao
    }

    override fun draw() {
        check(state == RenderableBufferStates.UPLOADED) { "Vertex buffer is not uploaded: $state" }
        bindVao()
        glDrawArrays(primitiveType.gl, 0, if (EMPTY_BUFFERS) 0 else vertices)
    }

    override fun unload() {
        if (state == RenderableBufferStates.UPLOADED) {
            glDeleteVertexArrays(vao)
            if (renderSystem.boundVao == vao) {
                renderSystem.boundVao = -1
            }
            vao = -1
        }
        super.unload()
    }

    private companion object {
        val PrimitiveTypes.gl: Int
            get() {
                return when (this) {
                    PrimitiveTypes.POINT -> GL_POINTS
                    PrimitiveTypes.LINE -> GL_LINES
                    PrimitiveTypes.TRIANGLE -> GL_TRIANGLES
                    PrimitiveTypes.QUAD -> GL_QUADS
                }
            }
    }

}
