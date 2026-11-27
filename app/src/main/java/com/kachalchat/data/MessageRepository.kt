package com.kachalchat.data

import com.kachalchat.model.MeshPacket
import com.kachalchat.model.MessageStatus

/**
 * Storage used by the mesh engine and the UI.
 * Implemented by the data member (Room). Functions are suspend because
 * database work must not run on the main thread.
 */
interface MessageRepository {

    /** Stores a message with its delivery status. */
    suspend fun save(packet: MeshPacket, status: MessageStatus, isOutgoing: Boolean)

    /** Changes the status of an already stored message. */
    suspend fun updateStatus(messageId: String, status: MessageStatus)

    /** Outgoing messages still waiting to be sent. */
    suspend fun getQueued(): List<MeshPacket>

    /** True if this message id has already been processed on this device. */
    suspend fun hasSeen(messageId: String): Boolean

    /** Remembers a message id so duplicates can be dropped later. */
    suspend fun markSeen(messageId: String)
}
