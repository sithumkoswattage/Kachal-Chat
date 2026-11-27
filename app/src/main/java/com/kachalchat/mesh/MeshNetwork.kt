package com.kachalchat.mesh

import com.kachalchat.model.MeshPacket

/**
 * The link layer: moves packets between directly connected devices.
 * Implemented by the networking member (Nearby Connections).
 * The mesh engine only talks to this interface, so it can be tested with a fake.
 */
interface MeshNetwork {

    /** Ids of the endpoints this device is directly connected to right now. */
    fun connectedPeers(): Set<String>

    /** Sends a packet to one directly connected endpoint. */
    fun send(endpointId: String, packet: MeshPacket)

    /** Registers the object that should be told about network events. */
    fun setListener(listener: Listener)

    interface Listener {
        fun onPacketReceived(fromEndpointId: String, packet: MeshPacket)
        fun onPeerConnected(endpointId: String)
        fun onPeerDisconnected(endpointId: String)
    }
}
