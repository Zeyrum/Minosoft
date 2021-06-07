/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.network.connection

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.modding.event.EventInvoker
import de.bixilon.minosoft.modding.event.events.CancelableEvent
import de.bixilon.minosoft.modding.event.events.ConnectionEvent
import de.bixilon.minosoft.modding.event.events.PacketSendEvent
import de.bixilon.minosoft.protocol.network.Network
import de.bixilon.minosoft.protocol.packets.c2s.C2SPacket
import de.bixilon.minosoft.protocol.packets.s2c.S2CPacket
import de.bixilon.minosoft.protocol.protocol.ConnectionStates
import de.bixilon.minosoft.protocol.protocol.PacketTypes.C2S
import de.bixilon.minosoft.protocol.protocol.PacketTypes.S2C
import de.bixilon.minosoft.util.KUtil.synchronizedListOf
import de.bixilon.minosoft.util.KUtil.toSynchronizedList

abstract class Connection {
    val network = Network.getNetworkInstance(this)
    protected val eventListeners: MutableList<EventInvoker> = synchronizedListOf()
    val connectionId = lastConnectionId++
    abstract var connectionState: ConnectionStates
    var error: Throwable? = null
    var wasConnected = false

    abstract fun getPacketId(packetType: C2S): Int
    abstract fun getPacketById(packetId: Int): S2C?

    open fun sendPacket(packet: C2SPacket) {
        val event = PacketSendEvent(this, packet)
        if (fireEvent(event)) {
            return
        }
        network.sendPacket(packet)
    }

    /**
     * @param connectionEvent The event to fire
     * @return if the event has been cancelled or not
     */
    fun fireEvent(connectionEvent: ConnectionEvent): Boolean {
        for (eventManager in Minosoft.EVENT_MANAGERS) {
            for (eventListener in eventManager.globalEventListeners) {
                eventListener(connectionEvent)
            }
        }

        for (eventInvoker in eventListeners.toSynchronizedList()) {
            if (!eventInvoker.eventType.isAssignableFrom(connectionEvent::class.java)) {
                continue
            }
            eventInvoker(connectionEvent)
        }
        if (connectionEvent is CancelableEvent) {
            return connectionEvent.isCancelled
        }
        return false
    }

    open fun handle(packetType: S2C, packet: S2CPacket) {
        if (!packetType.isThreadSafe) {
            handlePacket(packet)
            return
        }
        Minosoft.THREAD_POOL.execute { handlePacket(packet) }
    }


    abstract fun handlePacket(packet: S2CPacket)

    open fun unregisterEvent(method: EventInvoker?) {
        eventListeners.remove(method)
    }

    open fun registerEvent(method: EventInvoker) {
        eventListeners.add(method)
    }

    open fun registerEvents(vararg method: EventInvoker) {
        eventListeners.addAll(method)
    }


    open fun disconnect() {
        network.disconnect()
    }

    companion object {
        var lastConnectionId: Int = 0
    }

}
