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

package de.bixilon.minosoft.commands.parser.minecraft.target

import de.bixilon.minosoft.commands.parser.minecraft.target.targets.selector.properties.sort.Sorting

@Deprecated("")
data class TargetPropertiesLEGACY(
    var x: Double?,
    var y: Double?,
    var z: Double?,
    var volumeX: Double?,
    var volumeY: Double?,
    var volumeZ: Double?,
    var scores: Any?, // ToDo
    var tag: Any?, // ToDo
    var team: Any?, // ToDo,
    var sort: Sorting?,
    var limit: Int? = null,
    var level: IntRange? = null,
    var nbt: Any? = null, // ToDo
    var advancements: Any? = null, // ToDo
    var predicate: Any? = null, // ToDo
)
