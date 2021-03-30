/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.entities.block;

import de.bixilon.minosoft.data.inventory.ItemStack;
import de.bixilon.minosoft.data.mappings.ResourceLocation;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.util.nbt.tag.CompoundTag;
import de.bixilon.minosoft.util.nbt.tag.ListTag;

public class CampfireBlockEntityMetaData extends BlockEntityMetaData {
    private final ItemStack[] items;

    public CampfireBlockEntityMetaData(ItemStack[] items) {
        this.items = items;
    }

    public CampfireBlockEntityMetaData(Connection connection, ListTag nbt) {
        this.items = new ItemStack[4];
        for (CompoundTag tag : nbt.<CompoundTag>getValue()) {
            this.items[tag.getByteTag("Slot").getValue()] = new ItemStack(null, connection.getMapping().getItemRegistry().get(new ResourceLocation(tag.getStringTag("id").getValue())), tag.getByteTag("Count").getValue()); // ToDo: version should not be null
        }
    }

    public ItemStack[] getItems() {
        return this.items;
    }
}
