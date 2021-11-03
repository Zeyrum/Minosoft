/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.config.config.game.controls

import com.squareup.moshi.Json

data class ControlsGameConfig(
    @Json(name = "key_bindings") var keyBindings: KeyBindingsGameConfig = KeyBindingsGameConfig(),
    @Json(name = "enable_flattening") var enableFlattening: Boolean = true,
    @Json(name = "enable_stripping") var enableStripping: Boolean = true,
    @Json(name = "enable_tilling") var enableTilling: Boolean = true,

    @Json(name = "mouse_sensitivity") var moseSensitivity: Float = 0.1f,
    @Json(name = "hotbar_scroll_sensitivity") var hotbarScrollSensitivity: Double = 1.0,
)
