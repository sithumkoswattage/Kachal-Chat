package com.kachalchat.mesh

import com.kachalchat.model.MeshPacket

/**
 * In-memory stand-in for the real Nearby Connections layer.
 * Each instance represents one phone. Phones are "in range" when linked
 * with connectTo(), and packets are delivered instantly to the other side.
 */
class FakeMeshNetwork(val id: String) : MeshNetwork {

    private val peers = linkedMapOf<String, FakeMeshNetwork>()
    private var listener: MeshNetwork.Listener? = null

    /** Every packet this phone has sent: (toEndpointId, packet). Used by tests. */
    val sentLog = mutableListOf<Pair<String, MeshPacket>>()

    override fun connectedPeers(): Set<String> = peers.keys.toSet()

    override fun send(endpointId: String, packet: MeshPacket) {
        val target = peers[endpointId] ?: return
        sentLog += endpointId to packet
        target.listener?.onPacketReceived(id, packet)
    }

    override fun setListener(listener: MeshNetwork.Listener) {
        this.listener = listener
    }

    /** Simulates two phones coming into range of each other. */
    fun connectTo(other: FakeMeshNetwork) {
        if (peers.containsKey(other.id)) return
        peers[other.id] = other
        other.peers[id] = this
        listener?.onPeerConnected(other.id)
        other.listener?.onPeerConnected(id)
    }

    /** Simulates two phones moving out of range. */
    fun disconnectFrom(other: FakeMeshNetwork) {
        if (peers.remove(other.id) == null) return
        other.peers.remove(id)
        listener?.onPeerDisconnected(other.id)
        other.listener?.onPeerDisconnected(id)
    }

    /** Simulates a packet arriving from a peer, without a real sender. */
    fun deliver(fromEndpointId: String, packet: MeshPacket) {
        listener?.onPacketReceived(fromEndpointId, packet)
    }
}
