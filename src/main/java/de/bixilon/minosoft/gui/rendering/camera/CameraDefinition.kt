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

package de.bixilon.minosoft.gui.rendering.camera

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition

object CameraDefinition {
    const val NEAR_PLANE = 0.01f
    const val FAR_PLANE = World.MAX_RENDER_DISTANCE * ProtocolDefinition.SECTION_LENGTH.toFloat()
    val CAMERA_UP_VEC3 = Vec3(0.0f, 1.0f, 0.0f)

}
