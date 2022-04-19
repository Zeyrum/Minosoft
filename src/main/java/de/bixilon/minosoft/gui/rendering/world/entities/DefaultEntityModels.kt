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

package de.bixilon.minosoft.gui.rendering.world.entities

import de.bixilon.minosoft.gui.rendering.world.entities.renderer.sign.standing.StandingSignModels
import de.bixilon.minosoft.gui.rendering.world.entities.renderer.sign.wall.WallSignModels
import de.bixilon.minosoft.gui.rendering.world.entities.renderer.storage.DoubleChestRenderer
import de.bixilon.minosoft.gui.rendering.world.entities.renderer.storage.SingleChestRenderer

object DefaultEntityModels {
    val MODELS = listOf(
        SingleChestRenderer.NormalChest,
        SingleChestRenderer.TrappedChest,
        SingleChestRenderer.EnderChest,

        DoubleChestRenderer.NormalChest,
        DoubleChestRenderer.TrappedChest,


        StandingSignModels.Acacia,
        StandingSignModels.Birch,
        StandingSignModels.Crimson,
        StandingSignModels.DarkOak,
        StandingSignModels.Jungle,
        StandingSignModels.Oak,
        StandingSignModels.Spruce,
        StandingSignModels.WarpedSign,

        WallSignModels.Acacia,
        WallSignModels.Birch,
        WallSignModels.Crimson,
        WallSignModels.DarkOak,
        WallSignModels.Jungle,
        WallSignModels.Oak,
        WallSignModels.Spruce,
        WallSignModels.WarpedSign,
    )
}
