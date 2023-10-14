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

package de.bixilon.minosoft.gui.rendering.chunk.entities.renderer.storage

import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.time.DateUtil
import de.bixilon.minosoft.data.entities.block.container.storage.StorageBlockEntity
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties.getFacing
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.chunk.entities.EntityRendererRegister
import de.bixilon.minosoft.gui.rendering.models.loader.ModelLoader
import de.bixilon.minosoft.gui.rendering.models.loader.ModelLoader.Companion.bbModel
import de.bixilon.minosoft.gui.rendering.skeletal.baked.BakedSkeletalModel
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import de.bixilon.minosoft.gui.rendering.util.VecUtil.toVec3

class SingleChestRenderer(
    val entity: StorageBlockEntity,
    context: RenderContext,
    blockState: BlockState,
    blockPosition: Vec3i,
    model: BakedSkeletalModel,
    light: Int,
) : StorageBlockEntityRenderer<StorageBlockEntity>(
    blockState,
    model.createInstance(context, (blockPosition - context.camera.offset.offset).toVec3, blockState.getFacing().rotatedMatrix),
    light,
) {

    companion object {
        val SINGLE_MODEL = minecraft("block/entities/single_chest").bbModel()
        private val named = minecraft("chest")

        fun register(loader: ModelLoader, name: ResourceLocation, texture: ResourceLocation) {
            val texture = loader.context.textures.staticTextures.createTexture(texture)
            loader.skeletal.register(name, SINGLE_MODEL, mapOf(named to texture))
        }
    }

    object NormalChest : EntityRendererRegister {
        val NAME = minecraft("block/entities/single_chest")
        val TEXTURE = minecraft("entity/chest/normal").texture()
        val TEXTURE_CHRISTMAS = minecraft("entity/chest/christmas").texture()

        override fun register(loader: ModelLoader) {
            register(loader, NAME, if (DateUtil.christmas) TEXTURE_CHRISTMAS else TEXTURE)
        }
    }

    object TrappedChest : EntityRendererRegister {
        val NAME = minecraft("block/entities/trapped_chest")
        val TEXTURE = minecraft("entity/chest/trapped").texture()

        override fun register(loader: ModelLoader) {
            register(loader, NAME, TEXTURE)
        }
    }

    object EnderChest : EntityRendererRegister {
        val NAME = minecraft("block/entities/ender_chest")
        val TEXTURE = minecraft("entity/chest/ender").texture()

        override fun register(loader: ModelLoader) {
            register(loader, NAME, TEXTURE)
        }
    }
}
