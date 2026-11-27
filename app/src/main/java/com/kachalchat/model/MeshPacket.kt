package com.kachalchat.model

/**
 * A single message travelling through the mesh network.
 * This is the shared contract between UI, data, networking and mesh engine.
 * Do not change fields without agreement from the whole team.
 */
data class MeshPacket(
    val messageId: String,      // Unique id (UUID). Used to detect duplicates.
    val senderId: String,       // Stable id of the original sender's device/user.
    val senderName: String,     // Display name of the sender.
    val avatarId: String,       // Built-in avatar id, e.g. "icon_3".
    val recipientId: String?,   // Target user id, or null for a public broadcast.
    val content: String,        // Message text.
    val timestamp: Long,        // Creation time in milliseconds.
    val hopsRemaining: Int = DEFAULT_TTL // How many more times it may be forwarded.
) {
    val isPublic: Boolean get() = recipientId == null

    companion object {
        const val DEFAULT_TTL = 3
    }
}
