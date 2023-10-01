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
package de.bixilon.minosoft.protocol.packets.c2s.common

import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.protocol.packets.c2s.PlayC2SPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.buffers.OutByteBuffer
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayOutByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class ChannelC2SP(
    val channel: ResourceLocation,
    val data: ByteArray,
) : PlayC2SPacket {

    constructor(channel: ResourceLocation, buffer: OutByteBuffer) : this(channel, buffer.toArray())

    override fun write(buffer: PlayOutByteBuffer) {
        buffer.writeLegacyResourceLocation(channel)
        if (buffer.versionId < ProtocolVersions.V_14W29A) {
            buffer.writeShort(data.size)
        } else if (buffer.versionId < ProtocolVersions.V_14W31A) {
            buffer.writeVarInt(data.size)
        }
        if (buffer.versionId < ProtocolVersions.V_1_8_9) { // TODO: guessed
            buffer.writeBareByteArray(data)
        } else {
            buffer.writeByteArray(data)
        }
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_OUT, LogLevels.VERBOSE) { "Channel (channel=$channel, data=${data.contentToString()})" }
    }
}
