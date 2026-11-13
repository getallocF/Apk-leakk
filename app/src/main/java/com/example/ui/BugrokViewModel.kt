package com.example.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.BugrokDatabase
import com.example.data.BugrokRepository
import com.example.data.DnsLookupSummary
import com.example.data.DownloadedPackEntity
import com.example.data.LogLevel
import com.example.data.PortScanItem
import com.example.data.ProtocolLogEntry
import com.example.data.RemotePackItem
import com.example.data.ServerProfileEntity
import com.example.data.ServerRakNetStatus
import com.example.data.XboxAuthSession
import com.example.data.XboxAuthStatus
import com.example.network.BedrockProtocolService
import com.example.network.NetworkDiagnosticsService
import com.example.network.XboxLiveAuthManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream

enum class BugrokTab {
    DASHBOARD,
    RESOURCE_PACKS,
    DIAGNOSTICS,
    PROFILES
}

class BugrokViewModel(
    private val appContext: Context,
    private val repository: BugrokRepository,
    private val xboxAuthManager: XboxLiveAuthManager,
    private val bedrockService: BedrockProtocolService,
    private val diagnosticsService: NetworkDiagnosticsService
) : ViewModel() {

    private val _currentTab = MutableStateFlow(BugrokTab.DASHBOARD)
    val currentTab: StateFlow<BugrokTab> = _currentTab.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    // Active Connection Inputs
    private val _serverHostInput = MutableStateFlow("geo.hivebedrock.network")
    val serverHostInput: StateFlow<String> = _serverHostInput.asStateFlow()

    private val _serverPortInput = MutableStateFlow("19132")
    val serverPortInput: StateFlow<String> = _serverPortInput.asStateFlow()

    private val _customEndpointInput = MutableStateFlow("raknet://geo.hivebedrock.network:19132")
    val customEndpointInput: StateFlow<String> = _customEndpointInput.asStateFlow()

    private val _autoDecryptEnabled = MutableStateFlow(true)
    val autoDecryptEnabled: StateFlow<Boolean> = _autoDecryptEnabled.asStateFlow()

    // Xbox Live Authentication State
    private val _xboxSession = MutableStateFlow(XboxAuthSession())
    val xboxSession: StateFlow<XboxAuthSession> = _xboxSession.asStateFlow()

    private val _showXboxAuthSheet = MutableStateFlow(false)
    val showXboxAuthSheet: StateFlow<Boolean> = _showXboxAuthSheet.asStateFlow()

    // Active Server & Resource Pack State
    private val _isScanningServer = MutableStateFlow(false)
    val isScanningServer: StateFlow<Boolean> = _isScanningServer.asStateFlow()

    private val _activeServerStatus = MutableStateFlow<ServerRakNetStatus?>(null)
    val activeServerStatus: StateFlow<ServerRakNetStatus?> = _activeServerStatus.asStateFlow()

    private val _remotePacks = MutableStateFlow<List<RemotePackItem>>(emptyList())
    val remotePacks: StateFlow<List<RemotePackItem>> = _remotePacks.asStateFlow()

    // Live Protocol Console Logs
    private val _protocolLogs = MutableStateFlow<List<ProtocolLogEntry>>(emptyList())
    val protocolLogs: StateFlow<List<ProtocolLogEntry>> = _protocolLogs.asStateFlow()

    private val _selectedLogFilter = MutableStateFlow<LogLevel?>(null)
    val selectedLogFilter: StateFlow<LogLevel?> = _selectedLogFilter.asStateFlow()

    // Network Diagnostics States
    private val _diagTargetHost = MutableStateFlow("geo.hivebedrock.network")
    val diagTargetHost: StateFlow<String> = _diagTargetHost.asStateFlow()

    private val _diagTargetPort = MutableStateFlow("19132")
    val diagTargetPort: StateFlow<String> = _diagTargetPort.asStateFlow()

    private val _diagCustomPorts = MutableStateFlow("19132, 19133, 25565, 80, 443")
    val diagCustomPorts: StateFlow<String> = _diagCustomPorts.asStateFlow()

    private val _isRunningDiag = MutableStateFlow(false)
    val isRunningDiag: StateFlow<Boolean> = _isRunningDiag.asStateFlow()

    private val _diagRakNetResult = MutableStateFlow<ServerRakNetStatus?>(null)
    val diagRakNetResult: StateFlow<ServerRakNetStatus?> = _diagRakNetResult.asStateFlow()

    private val _dnsLookupSummary = MutableStateFlow<DnsLookupSummary?>(null)
    val dnsLookupSummary: StateFlow<DnsLookupSummary?> = _dnsLookupSummary.asStateFlow()

    private val _portScanResults = MutableStateFlow<List<PortScanItem>>(emptyList())
    val portScanResults: StateFlow<List<PortScanItem>> = _portScanResults.asStateFlow()

    // Toast / Banner Feedback Message
    private val _statusBannerMessage = MutableStateFlow<String?>(null)
    val statusBannerMessage: StateFlow<String?> = _statusBannerMessage.asStateFlow()

    // Pending file for SAF Export (.mcpack)
    private val _pendingExportPackFile = MutableStateFlow<File?>(null)

    // Database Flows
    val savedProfiles: StateFlow<List<ServerProfileEntity>> = repository.allProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloadedPacks: StateFlow<List<DownloadedPackEntity>> = repository.allDownloadedPacks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        appendLog(LogLevel.INFO, "SYSTEM", "BugrokTool Engine v1.21.60 siap dijalankan (RakNet v11 + AES-256-CFB8 Decryptor).")
        viewModelScope.launch {
            repository.ensureDefaultProfilesSeeded()
        }
        viewModelScope.launch {
            repository.savedXboxSessionFlow.collect { saved ->
                if (saved != null && _xboxSession.value.status == XboxAuthStatus.DISCONNECTED) {
                    _xboxSession.value = saved
                    appendLog(
                        LogLevel.XBOX,
                        "XBOX-AUTH",
                        "Sesi Xbox Live dipulihkan untuk Gamertag '${saved.gamertag}' (XUID: ${saved.xuid})"
                    )
                }
            }
        }
    }

    fun selectTab(tab: BugrokTab) {
        _currentTab.value = tab
    }

    fun toggleDarkTheme() {
        _isDarkTheme.update { !it }
    }

    fun dismissBannerMessage() {
        _statusBannerMessage.value = null
    }

    fun showBanner(message: String) {
        _statusBannerMessage.value = message
    }

    fun updateServerHost(host: String) {
        _serverHostInput.value = host
        val port = _serverPortInput.value.ifBlank { "19132" }
        _customEndpointInput.value = "raknet://${host.trim()}:$port"
    }

    fun updateServerPort(port: String) {
        val filtered = port.filter { it.isDigit() }.take(5)
        _serverPortInput.value = filtered
        val host = _serverHostInput.value.trim()
        _customEndpointInput.value = "raknet://$host:${filtered.ifBlank { "19132" }}"
    }

    fun updateCustomEndpoint(endpoint: String) {
        _customEndpointInput.value = endpoint
    }

    fun setAutoDecrypt(enabled: Boolean) {
        _autoDecryptEnabled.value = enabled
    }

    fun setLogFilter(level: LogLevel?) {
        _selectedLogFilter.value = level
    }

    fun clearLogs() {
        _protocolLogs.value = emptyList()
        appendLog(LogLevel.INFO, "CONSOLE", "Riwayat log protokol telah dibersihkan.")
    }

    private fun appendLog(level: LogLevel, tag: String, message: String) {
        _protocolLogs.update { current ->
            (listOf(ProtocolLogEntry(level = level, tag = tag, message = message)) + current).take(120)
        }
    }

    // =========================================================================
    // Xbox Live Account Linking (Microsoft OAuth2 Device Code + Bedrock Chain)
    // =========================================================================

    fun setShowXboxAuthSheet(show: Boolean) {
        _showXboxAuthSheet.value = show
    }

    fun startXboxDeviceCodeFlow() {
        viewModelScope.launch {
            _xboxSession.update { it.copy(status = XboxAuthStatus.REQUESTING_DEVICE_CODE, errorMessage = null) }
            appendLog(LogLevel.XBOX, "OAUTH2", "Meminta Device Code dari login.live.com/oauth20_connect.srf (Title ID: 00000000441cc96b)...")
            val session = xboxAuthManager.requestDeviceCode()
            _xboxSession.value = session
            appendLog(
                LogLevel.XBOX,
                "DEVICE-CODE",
                "Kode Perangkat diterima: [${session.userCode}] — Buka ${session.verificationUri} untuk menautkan akun Xbox Live."
            )
        }
    }

    fun completeXboxDeviceCodeVerification(gamertagInput: String) {
        viewModelScope.launch {
            val current = _xboxSession.value
            _xboxSession.update { it.copy(status = XboxAuthStatus.EXCHANGING_XSTS_CHAIN, errorMessage = null) }
            appendLog(LogLevel.XBOX, "XSTS-CHAIN", "Memverifikasi persetujuan Device Code & menukar token XBL -> XSTS -> Minecraft Bedrock Chain...")

            val result = xboxAuthManager.pollAndExchangeDeviceCode(
                currentSession = current,
                fallbackGamertag = gamertagInput.trim().ifBlank { "BugrokPlayer" }
            )
            result.onSuccess { authenticatedSession ->
                _xboxSession.value = authenticatedSession
                repository.saveXboxSession(authenticatedSession)
                appendLog(
                    LogLevel.XBOX,
                    "AUTH-OK",
                    "Akun Xbox Live '${authenticatedSession.gamertag}' berhasil ditautkan! XUID: ${authenticatedSession.xuid}"
                )
                showBanner("Xbox Live berhasil ditautkan sebagai ${authenticatedSession.gamertag}")
            }.onFailure { err ->
                _xboxSession.update {
                    it.copy(
                        status = XboxAuthStatus.AWAITING_USER_APPROVAL,
                        errorMessage = err.message
                    )
                }
                appendLog(LogLevel.WARN, "XBOX-WAIT", err.message ?: "Verifikasi tertunda")
            }
        }
    }

    fun linkXboxDirectSession(gamertag: String, customToken: String) {
        viewModelScope.launch {
            _xboxSession.update { it.copy(status = XboxAuthStatus.EXCHANGING_XSTS_CHAIN, errorMessage = null) }
            appendLog(LogLevel.XBOX, "CHAIN-GEN", "Membuat kunci ECDSA secp384r1 & menandatangani rantai token Xbox Live untuk '${gamertag.ifBlank { "BugrokPlayer" }}'...")
            val session = xboxAuthManager.buildSignedBedrockChainSession(
                gamertag = gamertag,
                customXstsToken = customToken
            )
            _xboxSession.value = session
            repository.saveXboxSession(session)
            appendLog(
                LogLevel.XBOX,
                "AUTH-OK",
                "Sesi Xbox Live aktif: ${session.gamertag} (XUID: ${session.xuid}, PlayFab: ${session.playFabSessionId})"
            )
            showBanner("Akun Xbox Live '${session.gamertag}' siap digunakan untuk handshake server!")
            _showXboxAuthSheet.value = false
        }
    }

    fun disconnectXboxAccount() {
        viewModelScope.launch {
            val oldTag = _xboxSession.value.gamertag
            repository.clearXboxSession()
            _xboxSession.value = XboxAuthSession(status = XboxAuthStatus.DISCONNECTED)
            appendLog(LogLevel.XBOX, "LOGOUT", "Tautan akun Xbox Live '$oldTag' telah diputuskan.")
            showBanner("Akun Xbox Live telah diputuskan")
        }
    }

    // =========================================================================
    // Server Connection, RakNet Handshake & Resource Pack Downloader
    // =========================================================================

    fun connectAndScanServerPacks() {
        val host = _serverHostInput.value.trim()
        val port = _serverPortInput.value.toIntOrNull()?.coerceIn(1, 65535) ?: 19132

        if (host.isEmpty()) {
            showBanner("Masukkan alamat IP atau domain server terlebih dahulu.")
            return
        }

        if (_xboxSession.value.status != XboxAuthStatus.AUTHENTICATED) {
            appendLog(
                LogLevel.WARN,
                "AUTH-REQ",
                "Server Bedrock memerlukan rantai autentikasi Xbox Live untuk mengirim ResourcePacksInfoPacket. Membuka panel tautan Xbox..."
            )
            showBanner("Tautkan akun Xbox Live terlebih dahulu untuk mengunduh Resource Pack server.")
            _showXboxAuthSheet.value = true
            if (_xboxSession.value.userCode.isEmpty()) {
                startXboxDeviceCodeFlow()
            }
            return
        }

        viewModelScope.launch {
            _isScanningServer.value = true
            appendLog(LogLevel.RAKNET, "CONNECT", "Memulai koneksi ke $host:$port (${_customEndpointInput.value})...")

            val status = bedrockService.pingBedrockServer(
                host = host,
                port = port,
                onLog = { msg -> appendLog(LogLevel.RAKNET, "RAKNET", msg) }
            )
            _activeServerStatus.value = status

            // Update matching profile if present
            val matchingProfile = savedProfiles.value.firstOrNull {
                it.host.equals(host, ignoreCase = true) && it.port == port
            }
            if (matchingProfile != null) {
                repository.saveProfile(
                    matchingProfile.copy(
                        lastMotd = status.motdLine1,
                        lastOnlinePlayers = status.onlinePlayers,
                        lastMaxPlayers = status.maxPlayers,
                        lastLatencyMs = status.latencyMs
                    )
                )
            }

            val packs = bedrockService.discoverServerResourcePacks(
                serverStatus = status,
                xboxSession = _xboxSession.value,
                onLog = { msg -> appendLog(LogLevel.PACK, "BEDROCK", msg) }
            )
            _remotePacks.value = packs
            _isScanningServer.value = false
            showBanner("Terhubung ke $host:$port — ${packs.size} Resource Pack terdeteksi!")
        }
    }

    fun downloadSingleResourcePack(packUuid: String) {
        val pack = _remotePacks.value.firstOrNull { it.uuid == packUuid } ?: return
        val server = _activeServerStatus.value
        val host = server?.host ?: _serverHostInput.value.trim()
        val port = server?.port ?: (_serverPortInput.value.toIntOrNull() ?: 19132)
        val autoDecrypt = _autoDecryptEnabled.value

        viewModelScope.launch {
            appendLog(
                LogLevel.PACK,
                "DOWNLOAD",
                "Mengunduh '${pack.name}' v${pack.version} (${formatBytes(pack.sizeBytes)}) dari $host:$port..."
            )

            val result = bedrockService.downloadAndBuildMcpack(
                pack = pack,
                serverHost = host,
                serverPort = port,
                autoDecrypt = autoDecrypt,
                onProgress = { updatedPack ->
                    _remotePacks.update { list ->
                        list.map { if (it.uuid == packUuid) updatedPack else it }
                    }
                },
                onLog = { msg -> appendLog(LogLevel.PACK, "CHUNK", msg) }
            )

            result.onSuccess { mcpackFile ->
                val finalPack = _remotePacks.value.firstOrNull { it.uuid == packUuid } ?: pack
                val entity = DownloadedPackEntity(
                    packUuid = finalPack.uuid,
                    packName = finalPack.name,
                    packVersion = finalPack.version,
                    description = finalPack.description,
                    serverHost = host,
                    serverPort = port,
                    sizeBytes = mcpackFile.length().coerceAtLeast(finalPack.sizeBytes),
                    contentKey = finalPack.contentKey,
                    isEncrypted = finalPack.isEncrypted,
                    isDecrypted = finalPack.isEncrypted && autoDecrypt,
                    subpackName = finalPack.subpackName,
                    sha256Hash = finalPack.sha256Hash,
                    localFilePath = mcpackFile.absolutePath,
                    manifestJson = finalPack.manifestPreview
                )
                repository.saveDownloadedPack(entity)
                showBanner("Resource Pack '${finalPack.name}' berhasil diunduh & didekripsi (.mcpack)!")
            }.onFailure { err ->
                showBanner("Gagal mengunduh pack: ${err.message}")
            }
        }
    }

    fun downloadAllDetectedPacks() {
        val currentPacks = _remotePacks.value
        if (currentPacks.isEmpty()) {
            showBanner("Pindai server terlebih dahulu untuk mendeteksi Resource Pack.")
            return
        }
        currentPacks.forEach { pack ->
            downloadSingleResourcePack(pack.uuid)
        }
    }

    fun prepareExportMcpackFile(localFilePath: String): String {
        val file = File(localFilePath)
        _pendingExportPackFile.value = file
        return file.name.ifBlank { "resource_pack.mcpack" }
    }

    fun writePendingMcpackToUri(targetUri: Uri) {
        val srcFile = _pendingExportPackFile.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                appContext.contentResolver.openOutputStream(targetUri)?.use { out ->
                    FileInputStream(srcFile).use { input ->
                        input.copyTo(out)
                    }
                }
                withContext(Dispatchers.Main) {
                    appendLog(LogLevel.PACK, "EXPORT", "File ${srcFile.name} berhasil diekspor ke penyimpanan perangkat.")
                    showBanner("Berhasil menyimpan ${srcFile.name} ke perangkat!")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showBanner("Gagal menyimpan file: ${e.message}")
                }
            }
        }
    }

    fun deleteDownloadedPack(packId: Int) {
        viewModelScope.launch {
            repository.deleteDownloadedPack(packId)
            showBanner("Riwayat Resource Pack dihapus dari pustaka.")
        }
    }

    // =========================================================================
    // Network Diagnostics (RakNet Ping, DNS/SRV Resolver, Port Scanner)
    // =========================================================================

    fun updateDiagTargetHost(host: String) {
        _diagTargetHost.value = host
    }

    fun updateDiagTargetPort(port: String) {
        _diagTargetPort.value = port.filter { it.isDigit() }.take(5)
    }

    fun updateDiagCustomPorts(ports: String) {
        _diagCustomPorts.value = ports
    }

    fun runFullNetworkDiagnostics() {
        val host = _diagTargetHost.value.trim().ifBlank { _serverHostInput.value.trim() }
        val port = _diagTargetPort.value.toIntOrNull()?.coerceIn(1, 65535) ?: 19132
        if (host.isEmpty()) {
            showBanner("Masukkan domain atau IP target diagnostik.")
            return
        }

        viewModelScope.launch {
            _isRunningDiag.value = true
            appendLog(LogLevel.RAKNET, "DIAG", "Menjalankan diagnostik lengkap (RakNet Ping, DNS/SRV, Port Scan) untuk $host:$port...")

            // 1. RakNet Ping
            val pingRes = bedrockService.pingBedrockServer(
                host = host,
                port = port,
                onLog = { msg -> appendLog(LogLevel.RAKNET, "DIAG-PING", msg) }
            )
            _diagRakNetResult.value = pingRes

            // 2. DNS & SRV Record Lookup
            val dnsRes = diagnosticsService.performDnsAndSrvLookup(host)
            _dnsLookupSummary.value = dnsRes
            appendLog(
                LogLevel.INFO,
                "DIAG-DNS",
                "DNS Lookup $host selesai dalam ${dnsRes.lookupTimeMs}ms (${dnsRes.records.size} record ditemukan)"
            )

            // 3. Port Scanner
            val parsedPorts = _diagCustomPorts.value.split(",")
                .mapNotNull { it.trim().toIntOrNull() }
                .filter { it in 1..65535 }
                .ifEmpty { listOf(19132, 19133, 25565, 80, 443) }

            val scanRes = diagnosticsService.scanPorts(host, parsedPorts)
            _portScanResults.value = scanRes
            val openCount = scanRes.count { it.isOpen }
            appendLog(
                LogLevel.INFO,
                "DIAG-PORT",
                "Port Scanner selesai: $openCount/${scanRes.size} port terbuka pada $host"
            )

            _isRunningDiag.value = false
            showBanner("Diagnostik jaringan selesai untuk $host ($openCount port aktif)")
        }
    }

    // =========================================================================
    // Server Profiles & JSON Configuration Export/Import
    // =========================================================================

    fun applyProfileToConnection(profile: ServerProfileEntity) {
        _serverHostInput.value = profile.host
        _serverPortInput.value = profile.port.toString()
        _customEndpointInput.value = profile.customEndpoint.ifBlank { "raknet://${profile.host}:${profile.port}" }
        _autoDecryptEnabled.value = profile.autoDecrypt
        _diagTargetHost.value = profile.host
        _diagTargetPort.value = profile.port.toString()
        appendLog(LogLevel.INFO, "PROFILE", "Profil '${profile.name}' (${profile.host}:${profile.port}) dimuat ke sesi aktif.")
        showBanner("Profil '${profile.name}' dipilih")
    }

    fun saveServerProfile(
        id: Int = 0,
        name: String,
        host: String,
        port: Int,
        customEndpoint: String,
        protocolVersion: Int,
        gameVersion: String,
        autoDecrypt: Boolean,
        requireXboxAuth: Boolean,
        isFavorite: Boolean
    ) {
        viewModelScope.launch {
            val cleanHost = host.trim()
            if (cleanHost.isEmpty()) {
                showBanner("Host/IP server tidak boleh kosong.")
                return@launch
            }
            val entity = ServerProfileEntity(
                id = id,
                name = name.trim().ifBlank { cleanHost },
                host = cleanHost,
                port = port.coerceIn(1, 65535),
                customEndpoint = customEndpoint.trim().ifBlank { "raknet://$cleanHost:$port" },
                protocolVersion = protocolVersion,
                gameVersion = gameVersion.ifBlank { "1.21.60" },
                autoDecrypt = autoDecrypt,
                requireXboxAuth = requireXboxAuth,
                isFavorite = isFavorite
            )
            repository.saveProfile(entity)
            appendLog(LogLevel.INFO, "PROFILE", "Profil server '${entity.name}' (${entity.host}:${entity.port}) disimpan.")
            showBanner("Profil '${entity.name}' berhasil disimpan")
        }
    }

    fun toggleFavoriteProfile(profile: ServerProfileEntity) {
        viewModelScope.launch {
            repository.saveProfile(profile.copy(isFavorite = !profile.isFavorite))
        }
    }

    fun deleteServerProfile(profileId: Int) {
        viewModelScope.launch {
            repository.deleteProfile(profileId)
            showBanner("Profil server telah dihapus")
        }
    }

    fun exportConfigurationToUri(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val jsonString = repository.exportConfigurationJson()
                appContext.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(jsonString.toByteArray(Charsets.UTF_8))
                }
                withContext(Dispatchers.Main) {
                    appendLog(LogLevel.INFO, "CONFIG-EXPORT", "Konfigurasi profil & katalog pack berhasil diekspor ke JSON.")
                    showBanner("Konfigurasi JSON berhasil diekspor!")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showBanner("Gagal mengekspor konfigurasi: ${e.message}")
                }
            }
        }
    }

    fun exportInstalledApkToUri(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val sourceApkPath = appContext.applicationInfo.sourceDir
                val sourceApkFile = File(sourceApkPath)
                appContext.contentResolver.openOutputStream(uri)?.use { out ->
                    FileInputStream(sourceApkFile).use { input ->
                        input.copyTo(out)
                    }
                }
                withContext(Dispatchers.Main) {
                    appendLog(
                        LogLevel.INFO,
                        "APK-EXPORT",
                        "File installer BugrokTool-v1.0.apk (${formatBytes(sourceApkFile.length())}) berhasil disimpan."
                    )
                    showBanner("File BugrokTool-v1.0.apk berhasil disimpan!")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showBanner("Gagal mengekspor APK: ${e.message}")
                }
            }
        }
    }

    fun importConfigurationFromUri(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val rawJson = appContext.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }.orEmpty()
                if (rawJson.isBlank()) {
                    withContext(Dispatchers.Main) {
                        showBanner("File JSON konfigurasi kosong.")
                    }
                    return@launch
                }
                val count = repository.importConfigurationJson(rawJson)
                withContext(Dispatchers.Main) {
                    appendLog(LogLevel.INFO, "CONFIG-IMPORT", "Berhasil mengimpor $count profil server dari file JSON.")
                    showBanner("Berhasil mengimpor $count profil server!")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showBanner("Format file JSON tidak valid: ${e.message}")
                }
            }
        }
    }

    companion object {
        fun formatBytes(bytes: Long): String {
            if (bytes < 1024) return "$bytes B"
            val kb = bytes / 1024.0
            if (kb < 1024) return "%.1f KB".format(kb)
            val mb = kb / 1024.0
            return "%.2f MB".format(mb)
        }

        fun provideFactory(context: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val appContext = context.applicationContext
                    val db = BugrokDatabase.getInstance(appContext)
                    val repo = BugrokRepository(
                        context = appContext,
                        profileDao = db.serverProfileDao(),
                        packDao = db.downloadedPackDao()
                    )
                    return BugrokViewModel(
                        appContext = appContext,
                        repository = repo,
                        xboxAuthManager = XboxLiveAuthManager(),
                        bedrockService = BedrockProtocolService(appContext),
                        diagnosticsService = NetworkDiagnosticsService()
                    ) as T
                }
            }
        }
    }
}
