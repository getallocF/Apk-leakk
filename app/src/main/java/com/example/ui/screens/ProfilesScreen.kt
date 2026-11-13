package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.ServerProfileEntity

@Composable
fun ProfilesScreen(
    profiles: List<ServerProfileEntity>,
    onUseAndConnectProfile: (ServerProfileEntity) -> Unit,
    onSaveProfile: (
        id: Int,
        name: String,
        host: String,
        port: Int,
        customEndpoint: String,
        protocolVersion: Int,
        gameVersion: String,
        autoDecrypt: Boolean,
        requireXboxAuth: Boolean,
        isFavorite: Boolean
    ) -> Unit,
    onToggleFavorite: (ServerProfileEntity) -> Unit,
    onDeleteProfile: (Int) -> Unit,
    onExportConfigClick: () -> Unit,
    onImportConfigClick: () -> Unit,
    onExportApkClick: () -> Unit,
    onNavigateBack: () -> Unit
) {
    BackHandler {
        onNavigateBack()
    }

    var showProfileDialog by remember { mutableStateOf(false) }
    var editingProfile by remember { mutableStateOf<ServerProfileEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header & Add Profile Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Profil Server & Konfigurasi",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "Kelola endpoint kustom, versi protokol, dan cadangan JSON",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = {
                    editingProfile = null
                    showProfileDialog = true
                },
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier.testTag("add_profile_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Profil Baru")
            }
        }

        // Export / Import JSON Configuration & APK Installer Card
        ElevatedCard(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Cadangan Konfigurasi (.json) & Ekspor APK",
                    style = MaterialTheme.typography.titleSmall
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onExportConfigClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("export_config_button")
                    ) {
                        Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Ekspor JSON")
                    }

                    OutlinedButton(
                        onClick = onImportConfigClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("import_config_button")
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Impor JSON")
                    }
                }

                Button(
                    onClick = onExportApkClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("export_apk_button")
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Simpan File Installer (BugrokTool-v1.0.apk)", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Profiles List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("profiles_lazy_column"),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(profiles, key = { it.id }) { profile ->
                ServerProfileCard(
                    profile = profile,
                    onUseAndConnect = { onUseAndConnectProfile(profile) },
                    onEdit = {
                        editingProfile = profile
                        showProfileDialog = true
                    },
                    onToggleFavorite = { onToggleFavorite(profile) },
                    onDelete = { onDeleteProfile(profile.id) }
                )
            }
        }
    }

    if (showProfileDialog) {
        ProfileEditorDialog(
            initial = editingProfile,
            onDismiss = { showProfileDialog = false },
            onConfirm = { id, name, host, port, endpoint, proto, ver, decrypt, xbox, fav ->
                onSaveProfile(id, name, host, port, endpoint, proto, ver, decrypt, xbox, fav)
                showProfileDialog = false
            }
        )
    }
}

@Composable
private fun ServerProfileCard(
    profile: ServerProfileEntity,
    onUseAndConnect: () -> Unit,
    onEdit: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = profile.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${profile.host}:${profile.port}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row {
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.testTag("fav_profile_${profile.id}")
                    ) {
                        Icon(
                            imageVector = if (profile.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorit",
                            tint = if (profile.isFavorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.testTag("edit_profile_${profile.id}")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Profil")
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_profile_${profile.id}")
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Hapus Profil",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            if (profile.lastMotd.isNotBlank()) {
                Text(
                    text = profile.lastMotd,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "v${profile.gameVersion} (Proto ${profile.protocolVersion})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                if (profile.autoDecrypt) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "AES-CFB8 AutoDecrypt",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                if (profile.lastLatencyMs > 0) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "${profile.lastLatencyMs} ms",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            Button(
                onClick = onUseAndConnect,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("use_profile_${profile.id}")
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Gunakan Profil & Pindai Server", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ProfileEditorDialog(
    initial: ServerProfileEntity?,
    onDismiss: () -> Unit,
    onConfirm: (
        id: Int,
        name: String,
        host: String,
        port: Int,
        customEndpoint: String,
        protocolVersion: Int,
        gameVersion: String,
        autoDecrypt: Boolean,
        requireXboxAuth: Boolean,
        isFavorite: Boolean
    ) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var host by remember { mutableStateOf(initial?.host ?: "") }
    var portStr by remember { mutableStateOf((initial?.port ?: 19132).toString()) }
    var endpoint by remember { mutableStateOf(initial?.customEndpoint ?: "") }
    var protocolStr by remember { mutableStateOf((initial?.protocolVersion ?: 776).toString()) }
    var gameVersion by remember { mutableStateOf(initial?.gameVersion ?: "1.21.60") }
    var autoDecrypt by remember { mutableStateOf(initial?.autoDecrypt ?: true) }
    var isFavorite by remember { mutableStateOf(initial?.isFavorite ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initial == null) "Tambah Profil Server Baru" else "Edit Profil Server",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Profil") },
                    placeholder = { Text("Contoh: Server SMP Komunitas") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_profile_name_input")
                )

                OutlinedTextField(
                    value = host,
                    onValueChange = {
                        host = it
                        val p = portStr.ifBlank { "19132" }
                        endpoint = "raknet://${it.trim()}:$p"
                    },
                    label = { Text("Alamat IP / Domain Server") },
                    placeholder = { Text("play.example.net") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_profile_host_input")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = portStr,
                        onValueChange = {
                            portStr = it.filter { c -> c.isDigit() }.take(5)
                            endpoint = "raknet://${host.trim()}:${portStr.ifBlank { "19132" }}"
                        },
                        label = { Text("Port") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = protocolStr,
                        onValueChange = { protocolStr = it.filter { c -> c.isDigit() }.take(4) },
                        label = { Text("Protokol") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = gameVersion,
                    onValueChange = { gameVersion = it },
                    label = { Text("Versi Bedrock Target") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = endpoint,
                    onValueChange = { endpoint = it },
                    label = { Text("Endpoint RakNet Kustom") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Auto-Decrypt AES-256-CFB8", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = autoDecrypt, onCheckedChange = { autoDecrypt = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        initial?.id ?: 0,
                        name,
                        host,
                        portStr.toIntOrNull() ?: 19132,
                        endpoint,
                        protocolStr.toIntOrNull() ?: 776,
                        gameVersion,
                        autoDecrypt,
                        true,
                        isFavorite
                    )
                },
                modifier = Modifier.testTag("dialog_save_profile_button")
            ) {
                Text("Simpan Profil")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
