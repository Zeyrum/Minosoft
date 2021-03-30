/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation,
 either version 3 of the License,
 or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not,
 see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB,
 the original developer of Minecraft.
 */
package de.bixilon.minosoft.protocol.protocol

import de.bixilon.minosoft.protocol.ErrorHandler
import de.bixilon.minosoft.protocol.packets.ClientboundPacket
import de.bixilon.minosoft.protocol.packets.clientbound.login.*
import de.bixilon.minosoft.protocol.packets.clientbound.play.*
import de.bixilon.minosoft.protocol.packets.clientbound.play.title.TitlePacketFactory
import de.bixilon.minosoft.protocol.packets.clientbound.status.PacketStatusPong
import de.bixilon.minosoft.protocol.packets.clientbound.status.PacketStatusResponse

class PacketTypes {
    enum class Serverbound {
        HANDSHAKING_HANDSHAKE,
        STATUS_PING,
        STATUS_REQUEST,
        LOGIN_LOGIN_START,
        LOGIN_ENCRYPTION_RESPONSE,
        LOGIN_PLUGIN_RESPONSE,
        PLAY_TELEPORT_CONFIRM,
        PLAY_QUERY_BLOCK_NBT,
        PLAY_SET_DIFFICULTY,
        PLAY_CHAT_MESSAGE,
        PLAY_CLIENT_STATUS,
        PLAY_CLIENT_SETTINGS,
        PLAY_TAB_COMPLETE,
        PLAY_WINDOW_CONFIRMATION,
        PLAY_CLICK_WINDOW_BUTTON,
        PLAY_CLICK_WINDOW,
        PLAY_CLOSE_WINDOW,
        PLAY_PLUGIN_MESSAGE,
        PLAY_EDIT_BOOK,
        PLAY_ENTITY_NBT_REQUEST,
        PLAY_INTERACT_ENTITY,
        PLAY_KEEP_ALIVE,
        PLAY_LOCK_DIFFICULTY,
        PLAY_PLAYER_POSITION,
        PLAY_PLAYER_POSITION_AND_ROTATION,
        PLAY_PLAYER_ROTATION,
        PLAY_VEHICLE_MOVE,
        PLAY_STEER_BOAT,
        PLAY_PICK_ITEM,
        PLAY_CRAFT_RECIPE_REQUEST,
        PLAY_PLAYER_ABILITIES,
        PLAY_PLAYER_DIGGING,
        PLAY_ENTITY_ACTION,
        PLAY_STEER_VEHICLE,
        PLAY_RECIPE_BOOK_DATA,
        PLAY_NAME_ITEM,
        PLAY_RESOURCE_PACK_STATUS,
        PLAY_ADVANCEMENT_TAB,
        PLAY_SELECT_TRADE,
        PLAY_SET_BEACON_EFFECT,
        PLAY_HELD_ITEM_CHANGE,
        PLAY_UPDATE_COMMAND_BLOCK,
        PLAY_CREATIVE_INVENTORY_ACTION,
        PLAY_UPDATE_JIGSAW_BLOCK,
        PLAY_UPDATE_STRUCTURE_BLOCK,
        PLAY_UPDATE_SIGN,
        PLAY_ANIMATION,
        PLAY_SPECTATE,
        PLAY_PLAYER_BLOCK_PLACEMENT,
        PLAY_USE_ITEM,
        PLAY_UPDATE_COMMAND_BLOCK_MINECART,
        PLAY_GENERATE_STRUCTURE,
        PLAY_SET_DISPLAYED_RECIPE,
        PLAY_SET_RECIPE_BOOK_STATE,
        PLAY_PLAYER_GROUND_CHANGE,
        PLAY_PREPARE_CRAFTING_GRID,
        PLAY_VEHICLE_MOVEMENT,
        PLAY_QUERY_ENTITY_NBT,
        ;

        val state: ConnectionStates = ConnectionStates.valueOf(name.split("_".toRegex()).toTypedArray()[0])
    }


    enum class Clientbound(
        val factory: ((buffer: InByteBuffer) -> ClientboundPacket)? = null,
        val isThreadSafe: Boolean = true,
        val errorHandler: ErrorHandler? = null,
    ) {
        STATUS_RESPONSE({ PacketStatusResponse(it) }, false),
        STATUS_PONG({ PacketStatusPong(it) }, false),
        LOGIN_DISCONNECT({ PacketLoginDisconnect(it) }, false),
        LOGIN_ENCRYPTION_REQUEST({ PacketEncryptionRequest(it) }, false),
        LOGIN_LOGIN_SUCCESS({ PacketLoginSuccess(it) }, false),
        LOGIN_SET_COMPRESSION({ PacketLoginSetCompression(it) }, false),
        LOGIN_PLUGIN_REQUEST({ PacketLoginPluginRequest(it) }),
        PLAY_SPAWN_MOB({ PacketSpawnMob(it) }, false),
        PLAY_SPAWN_EXPERIENCE_ORB({ PacketSpawnExperienceOrb(it) }, false),
        PLAY_SPAWN_WEATHER_ENTITY({ PacketSpawnWeatherEntity(it) }, false),
        PLAY_SPAWN_PAINTING({ PacketSpawnPainting(it) }, false),
        PLAY_SPAWN_PLAYER({ PacketSpawnPlayer(it) }, false),
        PLAY_ENTITY_ANIMATION({ PacketEntityAnimation(it) }),
        PLAY_STATS_RESPONSE({ PacketStatistics(it) }),
        PLAY_ACKNOWLEDGE_PLAYER_DIGGING({ PacketAcknowledgePlayerDigging(it) }),
        PLAY_BLOCK_BREAK_ANIMATION({ PacketBlockBreakAnimation(it) }),
        PLAY_BLOCK_ENTITY_DATA({ PacketBlockEntityMetadata(it) }),
        PLAY_BLOCK_ACTION({ PacketBlockAction(it) }),
        PLAY_BLOCK_CHANGE({ PacketBlockChange(it) }),
        PLAY_BOSS_BAR({ PacketBossBar(it) }),
        PLAY_SERVER_DIFFICULTY({ PacketServerDifficulty(it) }),
        PLAY_CHAT_MESSAGE({ PacketChatMessageReceiving(it) }),
        PLAY_MULTIBLOCK_CHANGE({ PacketMultiBlockChange(it) }),
        PLAY_TAB_COMPLETE({ PacketTabCompleteReceiving(it) }),
        PLAY_DECLARE_COMMANDS({ PacketDeclareCommands(it) }),
        PLAY_WINDOW_CONFIRMATION({ PacketConfirmTransactionReceiving(it) }),
        PLAY_CLOSE_WINDOW({ PacketCloseWindowReceiving(it) }),
        PLAY_WINDOW_ITEMS({ PacketWindowItems(it) }),
        PLAY_WINDOW_PROPERTY({ PacketWindowProperty(it) }),
        PLAY_SET_SLOT({ PacketSetSlot(it) }),
        PLAY_SET_COOLDOWN({ PacketSetCooldown(it) }),
        PLAY_PLUGIN_MESSAGE({ PacketPluginMessageReceiving(it) }),
        PLAY_NAMED_SOUND_EFFECT({ PacketNamedSoundEffect(it) }),
        PLAY_DISCONNECT({ PacketDisconnect(it) }, false),
        PLAY_ENTITY_EVENT({ PacketEntityEvent(it) }),
        PLAY_EXPLOSION({ PacketExplosion(it) }),
        PLAY_UNLOAD_CHUNK({ PacketUnloadChunk(it) }),
        PLAY_CHANGE_GAME_STATE({ PacketChangeGameState(it) }),
        PLAY_OPEN_HORSE_WINDOW({ PacketOpenHorseWindow(it) }),
        PLAY_KEEP_ALIVE({ PacketKeepAlive(it) }),
        PLAY_CHUNK_DATA({ PacketChunkData(it) }),
        PLAY_EFFECT({ PacketEffect(it) }),
        PLAY_PARTICLE({ PacketParticle(it) }),
        PLAY_UPDATE_LIGHT({ PacketUpdateLight(it) }),
        PLAY_JOIN_GAME({ PacketJoinGame(it) }, false, PacketJoinGame),
        PLAY_MAP_DATA({ PacketMapData(it) }),
        PLAY_TRADE_LIST({ PacketTradeList(it) }),
        PLAY_ENTITY_MOVEMENT_AND_ROTATION({ PacketEntityMovementAndRotation(it) }),
        PLAY_ENTITY_ROTATION({ PacketEntityRotation(it) }),
        PLAY_ENTITY_MOVEMENT({ PacketEntityMovement(it) }),
        PLAY_VEHICLE_MOVEMENT({ PacketVehicleMovement(it) }),
        PLAY_OPEN_BOOK({ PacketOpenBook(it) }),
        PLAY_OPEN_WINDOW({ PacketOpenWindow(it) }),
        PLAY_OPEN_SIGN_EDITOR({ PacketOpenSignEditor(it) }),
        PLAY_CRAFT_RECIPE_RESPONSE({ PacketCraftRecipeResponse(it) }),
        PLAY_PLAYER_ABILITIES({ PacketPlayerAbilitiesReceiving(it) }),
        PLAY_COMBAT_EVENT({ PacketCombatEvent(it) }),
        PLAY_PLAYER_LIST_ITEM({ PacketPlayerListItem(it) }),
        PLAY_FACE_PLAYER({ PacketFacePlayer(it) }),
        PLAY_PLAYER_POSITION_AND_ROTATION({ PacketPlayerPositionAndRotation(it) }),
        PLAY_UNLOCK_RECIPES({ PacketUnlockRecipes(it) }),
        PLAY_DESTROY_ENTITIES({ PacketDestroyEntity(it) }),
        PLAY_REMOVE_ENTITY_EFFECT({ PacketRemoveEntityStatusEffect(it) }),
        PLAY_RESOURCE_PACK_SEND({ PacketResourcePackSend(it) }),
        PLAY_RESPAWN({ PacketRespawn(it) }, false),
        PLAY_ENTITY_HEAD_ROTATION({ PacketEntityHeadRotation(it) }),
        PLAY_SELECT_ADVANCEMENT_TAB({ PacketSelectAdvancementTab(it) }),
        PLAY_WORLD_BORDER({ PacketWorldBorder(it) }),
        PLAY_CAMERA({ PacketCamera(it) }),
        PLAY_HELD_ITEM_CHANGE({ PacketHeldItemChangeReceiving(it) }),
        PLAY_UPDATE_VIEW_POSITION({ PacketUpdateViewPosition(it) }),
        PLAY_DISPLAY_SCOREBOARD({ PacketScoreboardDisplayScoreboard(it) }),
        PLAY_ENTITY_METADATA({ PacketEntityMetadata(it) }),
        PLAY_ATTACH_ENTITY({ PacketAttachEntity(it) }),
        PLAY_ENTITY_VELOCITY({ PacketEntityVelocity(it) }),
        PLAY_ENTITY_EQUIPMENT({ PacketEntityEquipment(it) }),
        PLAY_SET_EXPERIENCE({ PacketSetExperience(it) }),
        PLAY_UPDATE_HEALTH({ PacketUpdateHealth(it) }),
        PLAY_SCOREBOARD_OBJECTIVE({ PacketScoreboardObjective(it) }),
        PLAY_SET_PASSENGERS({ PacketSetPassenger(it) }),
        PLAY_TEAMS({ PacketTeams(it) }),
        PLAY_UPDATE_SCORE({ PacketScoreboardUpdateScore(it) }),
        PLAY_SPAWN_POSITION({ PacketSpawnPosition(it) }),
        PLAY_TIME_UPDATE({ PacketTimeUpdate(it) }),
        PLAY_ENTITY_SOUND_EFFECT({ PacketEntitySoundEffect(it) }),
        PLAY_SOUND_EFFECT({ PacketSoundEffect(it) }),
        PLAY_STOP_SOUND({ PacketStopSound(it) }),
        PLAY_PLAYER_LIST_HEADER_AND_FOOTER({ PacketTabHeaderAndFooter(it) }),
        PLAY_NBT_QUERY_RESPONSE({ PacketNBTQueryResponse(it) }),
        PLAY_COLLECT_ITEM({ PacketCollectItem(it) }),
        PLAY_ENTITY_TELEPORT({ PacketEntityTeleport(it) }, false),
        PLAY_ADVANCEMENTS({ PacketAdvancements(it) }),
        PLAY_ENTITY_PROPERTIES({ PacketEntityProperties(it) }),
        PLAY_ENTITY_EFFECT({ PacketEntityEffect(it) }),
        PLAY_DECLARE_RECIPES({ PacketDeclareRecipes(it) }),
        PLAY_TAGS({ PacketTags(it) }),
        PLAY_USE_BED({ PacketUseBed(it) }),
        PLAY_UPDATE_VIEW_DISTANCE({ PacketUpdateViewDistance(it) }),
        PLAY_CHUNK_BULK({ PacketChunkBulk(it) }),
        PLAY_UPDATE_SIGN({ PacketUpdateSignReceiving(it) }),
        PLAY_STATISTICS({ PacketStatistics(it) }),
        PLAY_SPAWN_ENTITY({ PacketSpawnObject(it) }, false),
        PLAY_TITLE({ TitlePacketFactory.createPacket(it) }),
        PLAY_ENTITY_INITIALISATION({ PacketEntityInitialisation(it) }, false),
        PLAY_SET_COMPRESSION({ PacketSetCompression(it) }, false),
        PLAY_ADVANCEMENT_PROGRESS({ TODO() }),
        PLAY_SCULK_VIBRATION_SIGNAL({ PacketSculkVibrationSignal(it) }),
        ;


        val state: ConnectionStates = ConnectionStates.valueOf(name.split("_".toRegex()).toTypedArray()[0])
    }
}
