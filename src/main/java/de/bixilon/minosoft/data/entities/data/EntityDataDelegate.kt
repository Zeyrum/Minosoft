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

package de.bixilon.minosoft.data.entities.data

import de.bixilon.kutil.observer.DataObserver
import kotlin.reflect.KProperty

class EntityDataDelegate<V>(
    default: V,
    val field: EntityDataField,
    val data: EntityData,
    val converter: ((Any) -> V)? = null,
) : DataObserver<V>(default) {

    init {
        data.observe<V>(field) {
            if (it == null) {
                set(default)
                return@observe
            }
            val value = if (converter == null) it else converter.invoke(it)
            set(value)
        }
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: V) {
        this.value = value
        data[field] = value
        lock.lock()
        unsafeSet(value)
        lock.unlock()
    }
}
