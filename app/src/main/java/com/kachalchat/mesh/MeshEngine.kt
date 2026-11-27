package com.kachalchat.mesh

import com.kachalchat.data.MessageRepository
import com.kachalchat.model.MeshPacket
import com.kachalchat.model.MessageStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

/**
 * The mesh relay engine. Sits between the link layer (MeshNetwork) and storage
 * (MessageRepository) and applies the routing rules:
 *
 *  1. Deduplication  - a message id that was already processed is dropped.
 *  2. Delivery       - public messages, and private messages addressed to this
 *                      device, are stored as RECEIVED.
 *  3. Forwarding     - if hopsRemaining > 0, the packet is relayed with one hop
 *                      less to every connected peer except the one it came from.
 *                      Private messages addressed to this device are not relayed.
 *  4. Store & retry  - own messages are stored as QUEUED and sent to every peer
 *                      that connects later, then marked SENT.
 */
class MeshEngine(
    private val myId: String,
    private val myName: String,
    private val myAvatarId: String,
    private val network: MeshNetwork,
    private val repository: MessageRepository,
    private val scope: CoroutineScope,
    private val clock: () -> Long = System::currentTimeMillis
) : MeshNetwork.Listener {

    /** Makes the check-then-mark of seen ids atomic when packets arrive together. */
    private val mutex = Mutex()

    /** Starts listening to network events. Call once after creating the engine. */
    fun start() {
        network.setListener(this)
    }

    /**
     * Creates and sends a new message from this device.
     * @param recipientId target user id, or null for the public room.
     */
    suspend fun sendMessage(content: String, recipientId: String? = null): MeshPacket {
        val packet = MeshPacket(
            messageId = UUID.randomUUID().toString(),
            senderId = myId,
            senderName = myName,
            avatarId = myAvatarId,
            recipientId = recipientId,
            content = content,
            timestamp = clock()
        )

        mutex.withLock {
            repository.markSeen(packet.messageId) // ignore it if it echoes back
            repository.save(packet, MessageStatus.QUEUED, isOutgoing = true)
        }

        val peers = network.connectedPeers()
        if (peers.isNotEmpty()) {
            peers.forEach { network.send(it, packet) }
            repository.updateStatus(packet.messageId, MessageStatus.SENT)
        }
        return packet
    }

    /** Applies the routing rules to a packet received from a direct peer. */
    suspend fun handleIncoming(fromEndpointId: String, packet: MeshPacket) {
        val addressedToMe = packet.recipientId == myId

        val isNew = mutex.withLock {
            if (repository.hasSeen(packet.messageId)) {
                false
            } else {
                repository.markSeen(packet.messageId)
                if (packet.isPublic || addressedToMe) {
                    repository.save(packet, MessageStatus.RECEIVED, isOutgoing = false)
                }
                true
            }
        }
        if (!isNew) return

        if (packet.hopsRemaining > 0 && !addressedToMe) {
            val relayed = packet.copy(hopsRemaining = packet.hopsRemaining - 1)
            network.connectedPeers()
                .filter { it != fromEndpointId }
                .forEach { network.send(it, relayed) }
        }
    }

    /** Sends every queued own message to a newly connected peer. */
    suspend fun flushQueue(endpointId: String) {
        repository.getQueued().forEach { packet ->
            network.send(endpointId, packet)
            repository.updateStatus(packet.messageId, MessageStatus.SENT)
        }
    }

    // --- MeshNetwork.Listener: network callbacks are not suspend, so launch ---

    override fun onPacketReceived(fromEndpointId: String, packet: MeshPacket) {
        scope.launch { handleIncoming(fromEndpointId, packet) }
    }

    override fun onPeerConnected(endpointId: String) {
        scope.launch { flushQueue(endpointId) }
    }

    override fun onPeerDisconnected(endpointId: String) {
        // Nothing to do: queued messages stay QUEUED until a peer connects.
    }
}
