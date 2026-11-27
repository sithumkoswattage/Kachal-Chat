package com.kachalchat.model

/** Delivery state of a stored message. */
enum class MessageStatus {
    QUEUED,   // Created locally, waiting for a connected peer.
    SENT,     // Handed to at least one connected peer.
    RECEIVED, // Arrived on this device from another device.
    FAILED    // Could not be sent.
}
