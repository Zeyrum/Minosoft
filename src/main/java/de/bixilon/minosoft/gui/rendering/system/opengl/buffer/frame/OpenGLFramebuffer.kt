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

package de.bixilon.minosoft.gui.rendering.system.opengl.buffer.frame

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.Framebuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.FramebufferState
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.texture.FramebufferTexture
import de.bixilon.minosoft.gui.rendering.system.base.buffer.render.Renderbuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.render.RenderbufferModes
import de.bixilon.minosoft.gui.rendering.system.opengl.MemoryLeakException
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGLRenderSystem
import de.bixilon.minosoft.gui.rendering.system.opengl.buffer.frame.texture.OpenGLFramebufferColorTexture
import de.bixilon.minosoft.gui.rendering.system.opengl.buffer.frame.texture.OpenGLFramebufferDepthTexture
import de.bixilon.minosoft.gui.rendering.system.opengl.buffer.render.OpenGLRenderbuffer
import org.lwjgl.opengl.GL30.*

class OpenGLFramebuffer(
    val renderSystem: OpenGLRenderSystem,
    var size: Vec2i,
    val color: Boolean,
    val depth: Boolean,
) : Framebuffer {
    override var state: FramebufferState = FramebufferState.PREPARING
        private set

    private var id = -1

    private var colorTexture: OpenGLFramebufferColorTexture? = null
    private var depthTexture: OpenGLFramebufferDepthTexture? = null
    private var depthBuffer: OpenGLRenderbuffer? = null

    override fun init() {
        check(state != FramebufferState.COMPLETE) { "Framebuffer is complete!" }
        id = glGenFramebuffers()
        unsafeBind()

        glViewport(0, 0, size.x, size.y)

        if (color) {
            val colorTexture = OpenGLFramebufferColorTexture(size)
            this.colorTexture = colorTexture
            colorTexture.init()
            attach(colorTexture)
        }

        if (depth) {
            val depth = OpenGLRenderbuffer(renderSystem, RenderbufferModes.DEPTH_COMPONENT24, size)
            this.depthBuffer = depth
            depth.init()
            attach(depth)
        }

        //depthTexture = OpenGLFramebufferDepthTexture(size)
        //depthTexture.init()
        //attach(depthTexture)

        val state = glCheckFramebufferStatus(GL_FRAMEBUFFER)
        check(state == GL_FRAMEBUFFER_COMPLETE) { "Framebuffer is incomplete: $state" }
        this.state = FramebufferState.COMPLETE
    }

    fun bind() {
        check(state == FramebufferState.COMPLETE) { "Framebuffer is incomplete: $state" }
        unsafeBind()
    }

    private fun unsafeBind() {
        glBindFramebuffer(GL_FRAMEBUFFER, id)
    }

    override fun attach(renderbuffer: Renderbuffer) {
        check(renderbuffer is OpenGLRenderbuffer) { "Can not attach non OpenGL renderbuffer!" }
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, renderbuffer.id)
    }

    override fun attach(texture: FramebufferTexture) {
        when (texture) {
            is OpenGLFramebufferDepthTexture -> glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, texture.id, 0)
            is OpenGLFramebufferColorTexture -> glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture.id, 0)
            else -> throw IllegalArgumentException("Can not attach non OpenGL texture!")
        }
    }

    override fun delete() {
        check(state == FramebufferState.COMPLETE) { "Framebuffer is incomplete: $state" }
        glDeleteFramebuffers(id)
        id = -1
        state = FramebufferState.DELETED
    }

    override fun bindTexture() {
        check(state == FramebufferState.COMPLETE) { "Framebuffer is incomplete: $state" }
        colorTexture?.bind(0)
        depthTexture?.bind(1)
    }

    override fun resize(size: Vec2i) {
        if (size == this.size) {
            return
        }
        colorTexture?.unload()
        depthBuffer?.unload()
        this.size = size
        delete()
        init()
    }

    protected fun finalize() {
        if (state == FramebufferState.COMPLETE && renderSystem.active) {
            throw MemoryLeakException("Buffer has not been unloaded!")
        }
    }
}
