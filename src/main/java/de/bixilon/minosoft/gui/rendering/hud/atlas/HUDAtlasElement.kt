/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.hud.atlas

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.gui.rendering.textures.Texture
import glm_.vec2.Vec2

data class HUDAtlasElement(
    override val texture: Texture,
    val binding: Vec2Binding,
    val slots: Map<Int, Vec2Binding> = mapOf(),
) : TextureLike {
    override val uvStart: Vec2
        get() = Vec2((binding.start.x * texture.widthFactor) / texture.width.toFloat(), (binding.start.y * texture.heightFactor) / texture.height.toFloat())
    override val uvEnd: Vec2
        get() = Vec2(((binding.end.x + 1) * texture.widthFactor) / texture.width, ((binding.end.y + 1) * texture.heightFactor) / (texture.height + 1.0f))
    override val size: Vec2
        get() = binding.size


    companion object {
        fun deserialize(json: Map<ResourceLocation, JsonObject>, versionId: Int): Pair<Collection<Texture>, Map<ResourceLocation, HUDAtlasElement>> {
            val textures: MutableMap<ResourceLocation, Texture> = mutableMapOf()
            val ret: MutableMap<ResourceLocation, HUDAtlasElement> = mutableMapOf()
            for ((resourceLocation, data) in json) {
                ret[resourceLocation] = deserialize(data, textures, versionId)
            }
            return Pair(textures.values, ret)
        }

        fun deserialize(json: JsonObject, textures: MutableMap<ResourceLocation, Texture>, versionId: Int): HUDAtlasElement {
            val keys: MutableSet<Int> = mutableSetOf()
            var textureResourceLocation: ResourceLocation? = json["texture"]?.asString?.let { ResourceLocation(it) }
            for (key in json["versions"].asJsonObject.keySet()) {
                keys.add(key.toInt())
            }
            // ToDo: Sort and get correct version
            val imageJson = json["versions"].asJsonObject[keys.iterator().next().toString()].asJsonObject

            imageJson["texture"]?.asString?.let { textureResourceLocation = ResourceLocation(it) }


            val texture = textures[textureResourceLocation] ?: let {
                val createdTexture = Texture(textureResourceLocation!!)
                textures[textureResourceLocation!!] = createdTexture
                createdTexture
            }

            val slots: MutableMap<Int, Vec2Binding> = mutableMapOf()

            imageJson["slots"]?.asJsonObject?.let {
                for ((id, data) in it.entrySet()) {
                    slots[id.toInt()] = Vec2Binding.deserialize(data)
                }
            }


            return HUDAtlasElement(texture, Vec2Binding.deserialize(imageJson), slots)
        }
    }

}
