/*
 * Codename Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.game.datatypes.inventory;

import de.bixilon.minosoft.game.datatypes.TextComponent;
import de.bixilon.minosoft.game.datatypes.entities.items.Item;
import de.bixilon.minosoft.game.datatypes.entities.items.Items;
import de.bixilon.minosoft.nbt.tag.CompoundTag;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class Slot {
    Item item;
    int itemCount;
    short itemMetadata;
    CompoundTag nbt;

    public Slot(Item item, int itemCount, CompoundTag nbt) {
        this.item = item;
        this.itemCount = itemCount;
        this.nbt = nbt;
    }

    public Slot(Item item, byte itemCount, short itemMetadata, CompoundTag nbt) {
        this.item = item;
        this.itemMetadata = itemMetadata;
        this.itemCount = itemCount;
        this.nbt = nbt;
    }

    public Slot(Item item) {
        this.item = item;
    }

    public Slot(Item item, byte itemCount) {
        this.item = item;
        this.itemCount = itemCount;
    }

    public Item getItem() {
        return item;
    }

    public int getItemId(ProtocolVersion version) {
        return Items.getItemId(item, version);
    }

    public int getItemCount() {
        return itemCount;
    }

    public short getItemMetadata() {
        return itemMetadata;
    }

    public CompoundTag getNbt() {
        return nbt;
    }

    public String getDisplayName() {
        if (nbt != null && nbt.containsKey("display") && nbt.getCompoundTag("display").containsKey("Name")) { // check if object has nbt data, and a custom display name
            return String.format("%s (%s)", new TextComponent(nbt.getCompoundTag("display").getStringTag("Name").getValue()).getColoredMessage(), item);
        }
        return item.toString(); // ToDo display name per Item (from language file)
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }
        Slot their = (Slot) obj;

        // ToDo: check nbt

        return their.getItem().equals(getItem()) && their.getItemCount() == getItemCount() && their.getItemMetadata() == getItemMetadata();
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
