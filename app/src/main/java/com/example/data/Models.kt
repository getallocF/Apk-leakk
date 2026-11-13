package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "server_profiles")
data class ServerProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val host: String,
    val port: Int = 19132,
    val customEndpoint: String = "",
    val protocolVersion: Int = 776, // Minecraft Bedrock 1.21.60+
    val gameVersion: String = "1.21.60",
    val autoDecrypt: Boolean = true,
    val requireXboxAuth: Boolean = true,
    val timeoutMs: Int = 6000,
    val isFavorite: Boolean = false,
    val lastMotd: String = "",
    val lastOnlinePlayers: Int = 0,
    val lastMaxPlayers: Int = 0,
    val lastLatencyMs: Long = -1L,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "downloaded_packs")
data class DownloadedPackEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val packUuid: String,
    val packName: String,
    val packVersion: String,
    val description: String,
    val serverHost: String,
    val serverPort: Int,
    val sizeBytes: Long,
    val contentKey: String,
    val isEncrypted: Boolean,
    val isDecrypted: Boolean,
    val subpackName: String = "",
    val sha256Hash: String,
    val localFilePath: String,
    val manifestJson: String,
    val fileCount: Int = 12,
    val downloadedAt: Long = System.currentTimeMillis()
)

enum class XboxAuthStatus {
    DISCONNECTED,
    REQUESTING_DEVICE_CODE,
    AWAITING_USER_APPROVAL,
    EXCHANGING_XSTS_CHAIN,
    AUTHENTICATED,
    ERROR
}

data class XboxAuthSession(
    val status: XboxAuthStatus = XboxAuthStatus.DISCONNECTED,
    val gamertag: String = "",
    val xuid: String = "",
    val userHash: String = "",
    val deviceCode: String = "",
    val userCode: String = "",
    val verificationUri: String = "https://www.microsoft.com/link",
    val expiresInSeconds: Int = 900,
    val xstsTokenPreview: String = "",
    val ecdsaPublicKeyBase64: String = "",
    val playFabSessionId: String = "",
    val connectedAt: Long = 0L,
    val errorMessage: String? = null
)

data class ServerRakNetStatus(
    val isOnline: Boolean = false,
    val host: String = "",
    val resolvedIp: String = "",
    val port: Int = 19132,
    val latencyMs: Long = 0L,
    val edition: String = "MCPE",
    val motdLine1: String = "",
    val motdLine2: String = "",
    val protocolVersion: Int = 776,
    val versionName: String = "1.21.60",
    val onlinePlayers: Int = 0,
    val maxPlayers: Int = 0,
    val serverUniqueId: String = "",
    val gamemode: String = "Survival",
    val portIpv4: Int = 19132,
    val portIpv6: Int = 19133,
    val queriedAt: Long = System.currentTimeMillis(),
    val rawPongPayload: String = ""
)

enum class PackTransferState {
    DETECTED,
    REQUESTING_CHUNKS,
    DECRYPTING_AES_CFB8,
    PACKAGING_MCPACK,
    COMPLETED,
    FAILED
}

data class RemotePackItem(
    val uuid: String,
    val name: String,
    val version: String,
    val description: String,
    val sizeBytes: Long,
    val contentKey: String,
    val isEncrypted: Boolean,
    val isAddon: Boolean = false,
    val isRaytracingCapable: Boolean = false,
    val subpackName: String = "default",
    val sha256Hash: String,
    val chunkSize: Int = 65536,
    val downloadedChunks: Int = 0,
    val totalChunks: Int = 1,
    val progress: Float = 0f,
    val speedKbps: Int = 0,
    val state: PackTransferState = PackTransferState.DETECTED,
    val localPath: String = "",
    val manifestPreview: String = ""
)

enum class LogLevel {
    INFO,
    RAKNET,
    XBOX,
    PACK,
    WARN,
    ERROR
}

data class ProtocolLogEntry(
    val id: Long = System.nanoTime(),
    val timestamp: Long = System.currentTimeMillis(),
    val level: LogLevel,
    val tag: String,
    val message: String
)

data class DnsRecordItem(
    val type: String, // A, AAAA, SRV, TXT, CNAME
    val name: String,
    val value: String,
    val ttlSeconds: Int,
    val priority: Int? = null,
    val port: Int? = null
)

data class DnsLookupSummary(
    val queryHost: String = "",
    val resolvedPrimaryIp: String = "",
    val srvTargetHost: String? = null,
    val srvTargetPort: Int? = null,
    val lookupTimeMs: Long = 0L,
    val records: List<DnsRecordItem> = emptyList(),
    val error: String? = null
)

data class PortScanItem(
    val port: Int,
    val protocol: String, // TCP / UDP-RakNet
    val serviceName: String,
    val isOpen: Boolean,
    val latencyMs: Long,
    val bannerOrInfo: String = ""
)
