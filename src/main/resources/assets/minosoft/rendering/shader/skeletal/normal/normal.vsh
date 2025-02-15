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

#version 330 core

layout (location = 0) in vec3 vinPosition;
layout (location = 1) in vec2 vinUV;
layout (location = 2) in float vinTransformNormal; // transform (0x7F000), normal (0xFFF)
layout (location = 3) in float vinIndexLayerAnimation;// texture index (0xF0000000), texture layer (0x0FFFF000), animation index (0x00000FFF)

#include "minosoft:animation/header_vertex"
#include "minosoft:skeletal/vertex"


#include "minosoft:animation/buffer"
#include "minosoft:animation/main_vertex"


#include "minosoft:color"

uniform uint uTintColor;


void main() {
    run_skeletal(floatBitsToUint(vinTransformNormal), vinPosition);
    run_animation();

    finTintColor *= getRGBColor(uTintColor & 0xFFFFFFu);
}
