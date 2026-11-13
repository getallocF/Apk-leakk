package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

private val Context.authDataStore by preferencesDataStore(name = "bugrok_xbox_auth_prefs")

class BugrokRepository(
    private val context: Context,
    private val profileDao: ServerProfileDao,
    private val packDao: DownloadedPackDao
) {
    val allProfiles: Flow<List<ServerProfileEntity>> = profileDao.getAllProfiles()
    val allDownloadedPacks: Flow<List<DownloadedPackEntity>> = packDao.getAllDownloadedPacks()

    private object PrefKeys {
        val GAMERTAG = stringPreferencesKey("xbox_gamertag")
        val XUID = stringPreferencesKey("xbox_xuid")
        val USER_HASH = stringPreferencesKey("xbox_uhs")
        val XSTS_TOKEN = stringPreferencesKey("xbox_xsts_token")
        val ECDSA_PUB_KEY = stringPreferencesKey("xbox_ecdsa_pub")
        val PLAYFAB_ID = stringPreferencesKey("xbox_playfab_id")
        val CONNECTED_AT = longPreferencesKey("xbox_connected_at")
    }

    val savedXboxSessionFlow: Flow<XboxAuthSession?> = context.authDataStore.data.map { prefs ->
        val gamertag = prefs[PrefKeys.GAMERTAG] ?: return@map null
        if (gamertag.isBlank()) return@map null
        XboxAuthSession(
            status = XboxAuthStatus.AUTHENTICATED,
            gamertag = gamertag,
            xuid = prefs[PrefKeys.XUID] ?: "",
            userHash = prefs[PrefKeys.USER_HASH] ?: "",
            xstsTokenPreview = prefs[PrefKeys.XSTS_TOKEN] ?: "",
            ecdsaPublicKeyBase64 = prefs[PrefKeys.ECDSA_PUB_KEY] ?: "",
            playFabSessionId = prefs[PrefKeys.PLAYFAB_ID] ?: "",
            connectedAt = prefs[PrefKeys.CONNECTED_AT] ?: System.currentTimeMillis()
        )
    }

    suspend fun saveXboxSession(session: XboxAuthSession) {
        context.authDataStore.edit { prefs ->
            prefs[PrefKeys.GAMERTAG] = session.gamertag
            prefs[PrefKeys.XUID] = session.xuid
            prefs[PrefKeys.USER_HASH] = session.userHash
            prefs[PrefKeys.XSTS_TOKEN] = session.xstsTokenPreview
            prefs[PrefKeys.ECDSA_PUB_KEY] = session.ecdsaPublicKeyBase64
            prefs[PrefKeys.PLAYFAB_ID] = session.playFabSessionId
            prefs[PrefKeys.CONNECTED_AT] = session.connectedAt
        }
    }

    suspend fun clearXboxSession() {
        context.authDataStore.edit { prefs ->
            prefs.clear()
        }
    }

    suspend fun ensureDefaultProfilesSeeded() = withContext(Dispatchers.IO) {
        val current = profileDao.getAllProfilesSnapshot()
        if (current.isEmpty()) {
            val defaults = listOf(
                ServerProfileEntity(
                    name = "The Hive Bedrock",
                    host = "geo.hivebedrock.network",
                    port = 19132,
                    customEndpoint = "raknet://geo.hivebedrock.network:19132",
                    protocolVersion = 776,
                    gameVersion = "1.21.60",
                    autoDecrypt = true,
                    requireXboxAuth = true,
                    isFavorite = true,
                    lastMotd = "The Hive - Minigames & Custom Cosmetics"
                ),
                ServerProfileEntity(
                    name = "CubeCraft Games",
                    host = "play.cubecraft.net",
                    port = 19132,
                    customEndpoint = "raknet://play.cubecraft.net:19132",
                    protocolVersion = 776,
                    gameVersion = "1.21.60",
                    autoDecrypt = true,
                    requireXboxAuth = true,
                    isFavorite = true,
                    lastMotd = "CubeCraft Bedrock Network - SkyBlock & EggWars"
                ),
                ServerProfileEntity(
                    name = "NetherGames Network",
                    host = "play.nethergames.org",
                    port = 19132,
                    customEndpoint = "raknet://play.nethergames.org:19132",
                    protocolVersion = 776,
                    gameVersion = "1.21.60",
                    autoDecrypt = true,
                    requireXboxAuth = true,
                    isFavorite = false,
                    lastMotd = "NetherGames - BedWars, Factions & Custom Packs"
                ),
                ServerProfileEntity(
                    name = "Zeqa Practice Network",
                    host = "zeqa.net",
                    port = 19132,
                    customEndpoint = "raknet://zeqa.net:19132",
                    protocolVersion = 776,
                    gameVersion = "1.21.60",
                    autoDecrypt = true,
                    requireXboxAuth = true,
                    isFavorite = false,
                    lastMotd = "Zeqa Competitive PvP & Custom UI Pack"
                )
            )
            profileDao.insertProfiles(defaults)
        }
    }

    suspend fun saveProfile(profile: ServerProfileEntity): Long = withContext(Dispatchers.IO) {
        profileDao.insertProfile(profile.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteProfile(id: Int) = withContext(Dispatchers.IO) {
        profileDao.deleteProfileById(id)
    }

    suspend fun saveDownloadedPack(pack: DownloadedPackEntity): Long = withContext(Dispatchers.IO) {
        packDao.insertPack(pack)
    }

    suspend fun deleteDownloadedPack(id: Int) = withContext(Dispatchers.IO) {
        packDao.deletePackById(id)
    }

    suspend fun exportConfigurationJson(): String = withContext(Dispatchers.IO) {
        val profiles = profileDao.getAllProfilesSnapshot()
        val packs = packDao.getAllDownloadedPacksSnapshot()
        val root = JSONObject()
        root.put("tool", "BugrokTool")
        root.put("schemaVersion", 1)
        root.put("exportedAt", System.currentTimeMillis())

        val profilesArr = JSONArray()
        for (p in profiles) {
            val obj = JSONObject()
            obj.put("name", p.name)
            obj.put("host", p.host)
            obj.put("port", p.port)
            obj.put("customEndpoint", p.customEndpoint)
            obj.put("protocolVersion", p.protocolVersion)
            obj.put("gameVersion", p.gameVersion)
            obj.put("autoDecrypt", p.autoDecrypt)
            obj.put("requireXboxAuth", p.requireXboxAuth)
            obj.put("timeoutMs", p.timeoutMs)
            obj.put("isFavorite", p.isFavorite)
            obj.put("lastMotd", p.lastMotd)
            profilesArr.put(obj)
        }
        root.put("profiles", profilesArr)

        val packsArr = JSONArray()
        for (pk in packs) {
            val obj = JSONObject()
            obj.put("packUuid", pk.packUuid)
            obj.put("packName", pk.packName)
            obj.put("packVersion", pk.packVersion)
            obj.put("description", pk.description)
            obj.put("serverHost", pk.serverHost)
            obj.put("serverPort", pk.serverPort)
            obj.put("sizeBytes", pk.sizeBytes)
            obj.put("contentKey", pk.contentKey)
            obj.put("isEncrypted", pk.isEncrypted)
            obj.put("sha256Hash", pk.sha256Hash)
            packsArr.put(obj)
        }
        root.put("packCatalogHistory", packsArr)
        root.toString(2)
    }

    suspend fun importConfigurationJson(rawJson: String): Int = withContext(Dispatchers.IO) {
        val root = JSONObject(rawJson)
        val profilesArr = root.optJSONArray("profiles") ?: return@withContext 0
        var importedCount = 0
        for (i in 0 until profilesArr.length()) {
            val obj = profilesArr.optJSONObject(i) ?: continue
            val host = obj.optString("host", "").trim()
            if (host.isEmpty()) continue
            val profile = ServerProfileEntity(
                name = obj.optString("name", host),
                host = host,
                port = obj.optInt("port", 19132).coerceIn(1, 65535),
                customEndpoint = obj.optString("customEndpoint", "raknet://$host:${obj.optInt("port", 19132)}"),
                protocolVersion = obj.optInt("protocolVersion", 776),
                gameVersion = obj.optString("gameVersion", "1.21.60"),
                autoDecrypt = obj.optBoolean("autoDecrypt", true),
                requireXboxAuth = obj.optBoolean("requireXboxAuth", true),
                timeoutMs = obj.optInt("timeoutMs", 6000),
                isFavorite = obj.optBoolean("isFavorite", false),
                lastMotd = obj.optString("lastMotd", "")
            )
            profileDao.insertProfile(profile)
            importedCount++
        }
        importedCount
    }
}
