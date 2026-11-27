package com.kachalchat.mesh

import com.kachalchat.data.MessageRepository
import com.kachalchat.model.MeshPacket
import com.kachalchat.model.MessageStatus

/**
 * In-memory stand-in for the real Room repository.
 * Stores everything in plain collections so tests can inspect them.
 */
class FakeMessageRepository : MessageRepository {

    data class Stored(val packet: MeshPacket, var status: MessageStatus, val isOutgoing: Boolean)

    val messages = linkedMapOf<String, Stored>()
    private val seenIds = mutableSetOf<String>()

    override suspend fun save(packet: MeshPacket, status: MessageStatus, isOutgoing: Boolean) {
        messages[packet.messageId] = Stored(packet, status, isOutgoing)
    }

    override suspend fun updateStatus(messageId: String, status: MessageStatus) {
        messages[messageId]?.status = status
    }

    override suspend fun getQueued(): List<MeshPacket> =
        messages.values
            .filter { it.isOutgoing && it.status == MessageStatus.QUEUED }
            .map { it.packet }

    override suspend fun hasSeen(messageId: String): Boolean = messageId in seenIds

    override suspend fun markSeen(messageId: String) {
        seenIds += messageId
    }

    fun statusOf(messageId: String): MessageStatus? = messages[messageId]?.status
}
