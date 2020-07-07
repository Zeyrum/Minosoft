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

package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.game.datatypes.Identifier;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class PacketSelectAdvancementTab implements ClientboundPacket {
    AdvancementTabs tab;


    @Override
    public boolean read(InPacketBuffer buffer) {
        switch (buffer.getVersion()) {
            case VERSION_1_12_2:
                if (buffer.readBoolean()) {
                    tab = AdvancementTabs.byName(buffer.readString(), buffer.getVersion());
                }
                return true;
        }

        return false;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Received select advancement tab (tab=%s)", ((tab == null) ? null : tab.name())));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public AdvancementTabs getTab() {
        return tab;
    }

    public enum AdvancementTabs {
        STORY(new Identifier("story/root")),
        NETHER(new Identifier("nether/root")),
        END(new Identifier("end/root")),
        ADVENTURE(new Identifier("adventure/root")),
        HUSBANDRY(new Identifier("husbandry/root"));

        final Identifier identifier;

        AdvancementTabs(Identifier identifier) {
            this.identifier = identifier;
        }

        public static AdvancementTabs byName(String name, ProtocolVersion version) {
            for (AdvancementTabs advancementTab : values()) {
                if (advancementTab.getIdentifier().get(version).equals(name)) {
                    return advancementTab;
                }
            }
            return null;
        }

        public Identifier getIdentifier() {
            return identifier;
        }
    }
}
