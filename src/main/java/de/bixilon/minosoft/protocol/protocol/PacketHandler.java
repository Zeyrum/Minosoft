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

package de.bixilon.minosoft.protocol.protocol;

import de.bixilon.minosoft.game.datatypes.GameMode;
import de.bixilon.minosoft.game.datatypes.blocks.Blocks;
import de.bixilon.minosoft.game.datatypes.entities.meta.HumanMetaData;
import de.bixilon.minosoft.game.datatypes.world.BlockPosition;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.clientbound.login.PacketEncryptionKeyRequest;
import de.bixilon.minosoft.protocol.packets.clientbound.login.PacketLoginDisconnect;
import de.bixilon.minosoft.protocol.packets.clientbound.login.PacketLoginSuccess;
import de.bixilon.minosoft.protocol.packets.clientbound.play.*;
import de.bixilon.minosoft.protocol.packets.clientbound.status.PacketStatusPong;
import de.bixilon.minosoft.protocol.packets.clientbound.status.PacketStatusResponse;
import de.bixilon.minosoft.protocol.packets.serverbound.login.PacketEncryptionResponse;
import de.bixilon.minosoft.protocol.packets.serverbound.play.PacketKeepAliveResponse;
import de.bixilon.minosoft.protocol.packets.serverbound.play.PacketPlayerPositionAndRotationSending;

import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.security.PublicKey;

public class PacketHandler {
    final Connection connection;

    public PacketHandler(Connection connection) {
        this.connection = connection;
    }

    public void handle(PacketStatusResponse pkg) {
        Log.info(String.format("Status response received: %s/%s online. MotD: '%s'", pkg.getResponse().getPlayerOnline(), pkg.getResponse().getMaxPlayers(), pkg.getResponse().getMotd()));
    }

    public void handle(PacketStatusPong pkg) {
        Log.debug("Pong: " + pkg.getID());
        if (connection.isOnlyPing()) {
            // pong arrived, closing connection
            connection.disconnect();
        }
    }

    public void handle(PacketEncryptionKeyRequest pkg) {
        SecretKey secretKey = CryptManager.createNewSharedKey();
        PublicKey publicKey = CryptManager.decodePublicKey(pkg.getPublicKey());
        String serverHash = new BigInteger(CryptManager.getServerHash(pkg.getServerId(), publicKey, secretKey)).toString(16);
        connection.getPlayer().getAccount().join(serverHash);
        connection.sendPacket(new PacketEncryptionResponse(secretKey, pkg.getVerifyToken(), publicKey));

    }

    public void handle(PacketLoginSuccess pkg) {
        // now we are playing
        // already done in packet thread
        // connection.setConnectionState(ConnectionState.PLAY);
    }

    public void handle(PacketJoinGame pkg) {
        connection.getPlayer().setGameMode(pkg.getGameMode());
        connection.getPlayer().setEntityId(pkg.getEntityId());
        connection.getPlayer().getWorld().setHardcore(pkg.isHardcore());
        connection.getPlayer().getWorld().setDimension(pkg.getDimension());
    }

    public void handle(PacketLoginDisconnect pkg) {
        Log.info(String.format("Disconnecting from server(%s)", pkg.getReason().getColoredMessage()));
        connection.setConnectionState(ConnectionState.DISCONNECTING);
    }

    public void handle(PacketPlayerInfo pkg) {
    }

    public void handle(PacketTimeUpdate pkg) {
    }

    public void handle(PacketKeepAlive pkg) {
        connection.sendPacket(new PacketKeepAliveResponse(pkg.getId()));
    }

    public void handle(PacketChunkBulk pkg) {
        connection.getPlayer().getWorld().setChunks(pkg.getChunkMap());
    }

    public void handle(PacketUpdateHealth pkg) {
        connection.getPlayer().setFood(pkg.getFood());
        connection.getPlayer().setHealth(pkg.getHealth());
        connection.getPlayer().setSaturation(pkg.getSaturation());
    }

    public void handle(PacketPluginMessageReceiving pkg) {
        connection.getPluginChannelHandler().handle(pkg.getChannel(), pkg.getData());
    }

    public void handle(PacketSpawnLocation pkg) {
        connection.getPlayer().setSpawnLocation(pkg.getSpawnLocation());
    }

    public void handle(PacketChatMessage pkg) {
    }

    public void handle(PacketDisconnect pkg) {
        // got kicked
        connection.setConnectionState(ConnectionState.DISCONNECTING);
    }

    public void handle(PacketHeldItemChangeReceiving pkg) {
        connection.getPlayer().setSelectedSlot(pkg.getSlot());
    }

    public void handle(PacketSetExperience pkg) {
        connection.getPlayer().setLevel(pkg.getLevel());
        connection.getPlayer().setTotalExperience(pkg.getTotal());
    }

    public void handle(PacketChangeGameState pkg) {
        switch (pkg.getReason()) {
            case START_RAIN:
                connection.getPlayer().getWorld().setRaining(true);
                break;
            case END_RAIN:
                connection.getPlayer().getWorld().setRaining(false);
                break;
            case CHANGE_GAMEMODE:
                connection.getPlayer().setGameMode(GameMode.byId(pkg.getValue().intValue()));
                break;
            //ToDo: handle all updates
        }
    }

    public void handle(PacketSpawnMob pkg) {
        connection.getPlayer().getWorld().addEntity(pkg.getMob());
    }

    public void handle(PacketEntityPositionAndRotation pkg) {
        connection.getPlayer().getWorld().getEntity(pkg.getEntityId()).setLocation(pkg.getRelativeLocation());
        connection.getPlayer().getWorld().getEntity(pkg.getEntityId()).setYaw(pkg.getYaw());
        connection.getPlayer().getWorld().getEntity(pkg.getEntityId()).setPitch(pkg.getPitch());
    }

    public void handle(PacketEntityPosition pkg) {
        connection.getPlayer().getWorld().getEntity(pkg.getEntityId()).setLocation(pkg.getRelativeLocation());
    }

    public void handle(PacketEntityRotation pkg) {
        connection.getPlayer().getWorld().getEntity(pkg.getEntityId()).setYaw(pkg.getYaw());
        connection.getPlayer().getWorld().getEntity(pkg.getEntityId()).setPitch(pkg.getPitch());
    }

    public void handle(PacketDestroyEntity pkg) {
        for (int entityId : pkg.getEntityIds()) {
            connection.getPlayer().getWorld().removeEntity(entityId);
        }
    }

    public void handle(PacketEntityVelocity pkg) {
        if (pkg.getEntityId() == connection.getPlayer().getEntityId()) {
            // this is us
            //ToDo
            return;
        }
        connection.getPlayer().getWorld().getEntity(pkg.getEntityId()).setVelocity(pkg.getVelocity());
    }

    public void handle(PacketSpawnPlayer pkg) {
        connection.getPlayer().getWorld().addEntity(pkg.getPlayer());
    }

    public void handle(PacketEntityTeleport pkg) {
        connection.getPlayer().getWorld().getEntity(pkg.getEntityId()).setLocation(pkg.getRelativeLocation());
        connection.getPlayer().getWorld().getEntity(pkg.getEntityId()).setYaw(pkg.getYaw());
        connection.getPlayer().getWorld().getEntity(pkg.getEntityId()).setPitch(pkg.getPitch());
    }

    public void handle(PacketEntityHeadRotation pkg) {
        connection.getPlayer().getWorld().getEntity(pkg.getEntityId()).setHeadYaw(pkg.getHeadYaw());
    }

    public void handle(PacketWindowItems pkg) {
        switch (pkg.getWindowId()) {
            case 0: //Inventory
                connection.getPlayer().setInventory(pkg.getData());
                break;
        }
    }

    public void handle(PacketEntityMetadata pkg) {
        if (pkg.getEntityId() == connection.getPlayer().getEntityId()) {
            // our own meta data...set it
            connection.getPlayer().setMetaData((HumanMetaData) pkg.getEntityData(HumanMetaData.class));
        } else {
            connection.getPlayer().getWorld().getEntity(pkg.getEntityId()).setMetaData(pkg.getEntityData(connection.getPlayer().getWorld().getEntity(pkg.getEntityId()).getMetaDataClass()));
        }
    }

    public void handle(PacketEntityEquipment pkg) {
        connection.getPlayer().getWorld().getEntity(pkg.getEntityId()).setEquipment(pkg.getSlot(), pkg.getData());
    }

    public void handle(PacketBlockChange pkg) {
        connection.getPlayer().getWorld().setBlock(pkg.getPosition(), pkg.getBlock());
    }

    public void handle(PacketMultiBlockChange pkg) {
        connection.getPlayer().getWorld().getChunk(pkg.getLocation()).setBlocks(pkg.getBlocks());
    }

    public void handle(PacketRespawn pkg) {
        connection.getPlayer().getWorld().setDimension(pkg.getDimension());
        connection.getPlayer().setSpawnConfirmed(false);
        connection.getPlayer().setGameMode(pkg.getGameMode());
    }

    public void handle(PacketOpenSignEditor pkg) {
        //ToDo
    }

    public void handle(PacketSpawnObject pkg) {
        connection.getPlayer().getWorld().addEntity(pkg.getObject());
    }

    public void handle(PacketSpawnExperienceOrb pkg) {
        connection.getPlayer().getWorld().addEntity(pkg.getOrb());
    }

    public void handle(PacketSpawnWeatherEntity pkg) {
        //ToDo
    }

    public void handle(PacketChunkData pkg) {
        connection.getPlayer().getWorld().setChunk(pkg.getLocation(), pkg.getChunk());
    }

    public void handle(PacketEntityEffect pkg) {
        connection.getPlayer().getWorld().getEntity(pkg.getEntityId()).addEffect(pkg.getEffect());
    }

    public void handle(PacketRemoveEntityEffect pkg) {
        connection.getPlayer().getWorld().getEntity(pkg.getEntityId()).removeEffect(pkg.getEffect());
    }

    public void handle(PacketUpdateSignReceiving pkg) {
        connection.getPlayer().getWorld().updateSign(pkg.getPosition(), pkg.getLines());
    }

    public void handle(PacketEntityAnimation pkg) {
        //ToDo
    }

    public void handle(PacketEntityStatus pkg) {
        //ToDo
    }

    public void handle(PacketSoundEffect pkg) {
        //ToDo
    }

    public void handle(PacketPlayerAbilitiesReceiving pkg) {
        //ToDo: used to set fly abilities
    }

    public void handle(PacketPlayerPositionAndRotation pkg) {
        //ToDo handle with gui
        if (!connection.getPlayer().isSpawnConfirmed()) {
            // oops, not spawned yet, confirming position
            //ToDo feet position
            connection.sendPacket(new PacketPlayerPositionAndRotationSending(pkg.getLocation().getX(), pkg.getLocation().getY() - 1.65F, pkg.getLocation().getY(), pkg.getLocation().getZ(), pkg.getYaw(), pkg.getPitch(), pkg.isOnGround()));
            connection.getPlayer().setSpawnConfirmed(true);
        }
    }

    public void handle(PacketAttachEntity pkg) {
        connection.getPlayer().getWorld().getEntity(pkg.getEntityId()).attachTo(pkg.getVehicleId());
        //ToDo leash support
    }

    public void handle(PacketUseBed pkg) {
        //ToDo
    }

    public void handle(PacketBlockEntityMetadata pkg) {
        connection.getPlayer().getWorld().setBlockEntityData(pkg.getPosition(), pkg.getNbt());
    }

    public void handle(PacketBlockBreakAnimation pkg) {
        // ToDo
    }

    public void handle(PacketBlockAction pkg) {
        // ToDo
    }

    public void handle(PacketExplosion pkg) {
        // remove all blocks set by explosion
        for (byte[] record : pkg.getRecords()) {
            int x = ((int) pkg.getLocation().getX()) + record[0];
            int y = ((int) pkg.getLocation().getY()) + record[1];
            int z = ((int) pkg.getLocation().getY()) + record[2];
            BlockPosition blockPosition = new BlockPosition(x, (short) y, z);
            connection.getPlayer().getWorld().setBlock(blockPosition, Blocks.AIR);
        }
        //ToDo: motion support
    }

    public void handle(PacketCollectItem pkg) {
        //ToDo
    }

    public void handle(PacketOpenWindow pkg) {
        //ToDo
    }

    public void handle(PacketCloseWindowReceiving pkg) {
        // ToDo
    }

    public void handle(PacketSetSlot pkg) {
        //ToDo
    }

    public void handle(PacketWindowProperty pkg) {
        //ToDo
    }

    public void handle(PacketConfirmTransactionReceiving pkg) {
        //ToDo
    }

    public void handle(PacketStatistics pkg) {
        //ToDo
    }

    public void handle(PacketTabCompleteReceiving pkg) {
        //ToDo
    }

    public void handle(PacketSpawnPainting pkg) {
    }

    public void handle(PacketEntity pkg) {
    }

    public void handle(PacketParticle pkg) {
        //ToDo
    }
}
