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
package de.bixilon.minosoft.data.entities.entities.projectile

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.container.ItemStackUtil
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.data.EntityDataField
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.SynchronizedEntityData
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil

class ThrownEyeOfEnder(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : Entity(connection, entityType, data, position, rotation) {

    @get:SynchronizedEntityData
    val item: ItemStack
        get() = data.get(ITEM_DATA, defaultItem)

    val defaultItem: ItemStack
        get() = ItemStackUtil.of(connection.registries.item[DEFAULT_ITEM]!!, connection = connection)

    override fun onAttack(attacker: Entity): Boolean = false

    companion object : EntityFactory<ThrownEyeOfEnder> {
        private val DEFAULT_ITEM = KUtil.minecraft("ender_eye")
        override val identifier: ResourceLocation = KUtil.minecraft("eye_of_ender")
        private val ITEM_DATA = EntityDataField("THROWN_EYE_OF_ENDER_ITEM")


        override fun build(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation): ThrownEyeOfEnder {
            return ThrownEyeOfEnder(connection, entityType, data, position, rotation)
        }
    }
}
