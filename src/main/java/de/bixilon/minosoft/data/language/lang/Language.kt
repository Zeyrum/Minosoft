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
package de.bixilon.minosoft.data.language.lang

import de.bixilon.minosoft.data.language.placeholder.PlaceholderUtil
import de.bixilon.minosoft.data.language.translate.Translator
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.TextComponent

class Language(
    val name: String,
    private val data: LanguageData,
) : Translator {

    override fun translate(key: ResourceLocation?, parent: TextComponent?, restricted: Boolean, vararg data: Any?): ChatComponent? {
        val placeholder = this.data[key?.path] ?: return null
        return PlaceholderUtil.format(placeholder, parent, restricted, *data)
    }

    override fun toString(): String {
        return name
    }
}
