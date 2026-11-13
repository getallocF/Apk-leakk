package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.DownloadedPackEntity
import com.example.data.PackTransferState
import com.example.data.RemotePackItem
import com.example.ui.BugrokViewModel
import com.example.ui.theme.JetBrainsMonoFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourcePacksScreen(
    remotePacks: List<RemotePackItem>,
    downloadedPacks: List<DownloadedPackEntity>,
    activeServerHost: String,
    activeServerPort: String,
    onDownloadSinglePack: (String) -> Unit,
    onDownloadAllPacks: () -> Unit,
    onSaveMcpackToDevice: (String) -> Unit,
    onDeleteDownloadedPack: (Int) -> Unit,
    onNavigateBackToDashboard: () -> Unit,
    onCopiedNotice: (String) -> Unit
) {
    BackHandler {
        onNavigateBackToDashboard()
    }

    val clipboardManager = LocalClipboardManager.current
    var selectedSection by remember { mutableIntStateOf(0) } // 0 = Server Packs, 1 = Downloaded Library
    var inspectedManifestTitle by remember { mutableStateOf<String?>(null) }
    var inspectedManifestJson by remember { mutableStateOf("") }
    var inspectedContentKey by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Header & Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Manajer Resource Pack",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "Target aktif: $activeServerHost:$activeServerPort",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (selectedSection == 0 && remotePacks.isNotEmpty()) {
                Button(
                    onClick = onDownloadAllPacks,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("download_all_packs_button")
                ) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Unduh Semua")
                }
            }
        }

        // Section Switcher
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = selectedSection == 0,
                onClick = { selectedSection = 0 },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                modifier = Modifier.testTag("tab_remote_packs")
            ) {
                Text("Pack Server (${remotePacks.size})")
            }
            SegmentedButton(
                selected = selectedSection == 1,
                onClick = { selectedSection = 1 },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                modifier = Modifier.testTag("tab_downloaded_library")
            ) {
                Text("Tersimpan (${downloadedPacks.size})")
            }
        }

        if (selectedSection == 0) {
            if (remotePacks.isEmpty()) {
                EmptyPackStateCard(
                    title = "Belum Ada Resource Pack Server yang Dipindai",
                    subtitle = "Hubungkan ke server Bedrock melalui Dashboard menggunakan IP & Port serta akun Xbox Live untuk memindai daftar Resource Pack server.",
                    actionLabel = "Ke Dashboard & Pindai Server",
                    onAction = onNavigateBackToDashboard
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(remotePacks, key = { it.uuid }) { pack ->
                        RemotePackDetailCard(
                            pack = pack,
                            onDownload = { onDownloadSinglePack(pack.uuid) },
                            onSaveToDevice = {
                                if (pack.localPath.isNotBlank()) {
                                    onSaveMcpackToDevice(pack.localPath)
                                }
                            },
                            onInspectManifest = {
                                inspectedManifestTitle = pack.name
                                inspectedManifestJson = pack.manifestPreview
                                inspectedContentKey = pack.contentKey
                            },
                            onCopyKey = {
                                clipboardManager.setText(AnnotatedString(pack.contentKey))
                                onCopiedNotice("Content Key AES-256 disalin ke clipboard!")
                            }
                        )
                    }
                }
            }
        } else {
            if (downloadedPacks.isEmpty()) {
                EmptyPackStateCard(
                    title = "Pustaka .mcpack Masih Kosong",
                    subtitle = "Semua Resource Pack server yang telah Anda unduh dan dekripsi akan tersimpan di sini lengkap dengan kunci AES-256 dan manifest.json.",
                    actionLabel = "Lihat Pack Server Aktif",
                    onAction = { selectedSection = 0 }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(downloadedPacks, key = { it.id }) { entity ->
                        DownloadedPackLibraryCard(
                            pack = entity,
                            onSaveToDevice = { onSaveMcpackToDevice(entity.localFilePath) },
                            onInspectManifest = {
                                inspectedManifestTitle = entity.packName
                                inspectedManifestJson = entity.manifestJson
                                inspectedContentKey = entity.contentKey
                            },
                            onCopyKey = {
                                clipboardManager.setText(AnnotatedString(entity.contentKey))
                                onCopiedNotice("Content Key AES-256 disalin ke clipboard!")
                            },
                            onDelete = { onDeleteDownloadedPack(entity.id) }
                        )
                    }
                }
            }
        }
    }

    // Manifest & AES Key Inspector Dialog
    if (inspectedManifestTitle != null) {
        AlertDialog(
            onDismissRequest = { inspectedManifestTitle = null },
            title = {
                Text(
                    text = inspectedManifestTitle ?: "Manifest Resource Pack",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (inspectedContentKey.isNotBlank()) {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "AES-256-CFB8 Content Key (32-Byte):",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Text(
                                    text = inspectedContentKey,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }

                    Text(
                        text = "manifest.json:",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF090D16))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = inspectedManifestJson,
                            fontFamily = JetBrainsMonoFontFamily,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF93C5FD)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(inspectedManifestJson))
                        onCopiedNotice("Isi manifest.json disalin!")
                    }
                ) {
                    Text("Salin JSON")
                }
            },
            dismissButton = {
                TextButton(onClick = { inspectedManifestTitle = null }) {
                    Text("Tutup")
                }
            }
        )
    }
}

@Composable
private fun RemotePackDetailCard(
    pack: RemotePackItem,
    onDownload: () -> Unit,
    onSaveToDevice: () -> Unit,
    onInspectManifest: () -> Unit,
    onCopyKey: () -> Unit
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
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pack.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = pack.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    color = if (pack.isEncrypted) {
                        MaterialTheme.colorScheme.tertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.secondaryContainer
                    },
                    shape = RoundedCornerShape(50)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (pack.isEncrypted) Icons.Default.Key else Icons.Default.LockOpen,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = if (pack.isEncrypted) {
                                MaterialTheme.colorScheme.onTertiaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            }
                        )
                        Text(
                            text = if (pack.isEncrypted) "AES-256-CFB8" else "UNENCRYPTED",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (pack.isEncrypted) {
                                MaterialTheme.colorScheme.onTertiaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            }
                        )
                    }
                }
            }

            // Technical Metadata Grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PackMetaLine("UUID", pack.uuid)
                PackMetaLine("Versi / Ukuran", "v${pack.version} • ${BugrokViewModel.formatBytes(pack.sizeBytes)} (${pack.totalChunks} chunks)")
                PackMetaLine("Subpack", pack.subpackName)
                if (pack.isEncrypted && pack.contentKey.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Content Key",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = pack.contentKey.take(20) + "...",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            IconButton(
                                onClick = onCopyKey,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "Salin Content Key",
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }
            }

            // Progress Bar when Downloading / Decrypting
            if (pack.state in listOf(
                    PackTransferState.REQUESTING_CHUNKS,
                    PackTransferState.DECRYPTING_AES_CFB8,
                    PackTransferState.PACKAGING_MCPACK
                )
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val statusLabel = when (pack.state) {
                            PackTransferState.REQUESTING_CHUNKS ->
                                "Mengunduh Chunks (${pack.downloadedChunks}/${pack.totalChunks}) • ${pack.speedKbps} KB/s"
                            PackTransferState.DECRYPTING_AES_CFB8 ->
                                "Mendekripsi AES-256-CFB8..."
                            PackTransferState.PACKAGING_MCPACK ->
                                "Memaketkan arsip .mcpack..."
                            else -> ""
                        }
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${(pack.progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    LinearProgressIndicator(
                        progress = { pack.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(50))
                    )
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onInspectManifest,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("inspect_manifest_${pack.uuid}")
                ) {
                    Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Manifest & Key")
                }

                if (pack.state == PackTransferState.COMPLETED && pack.localPath.isNotBlank()) {
                    Button(
                        onClick = onSaveToDevice,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("save_mcpack_${pack.uuid}")
                    ) {
                        Icon(Icons.Default.SaveAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Simpan .mcpack")
                    }
                } else {
                    Button(
                        onClick = onDownload,
                        enabled = pack.state == PackTransferState.DETECTED || pack.state == PackTransferState.FAILED,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("download_pack_${pack.uuid}")
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Unduh .mcpack")
                    }
                }
            }
        }
    }
}

@Composable
private fun DownloadedPackLibraryCard(
    pack: DownloadedPackEntity,
    onSaveToDevice: () -> Unit,
    onInspectManifest: () -> Unit,
    onCopyKey: () -> Unit,
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.DownloadDone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Column {
                        Text(
                            text = pack.packName,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${pack.serverHost}:${pack.serverPort} • v${pack.packVersion}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_saved_pack_${pack.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Hapus Pack",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

            PackMetaLine("UUID", pack.packUuid)
            PackMetaLine("Ukuran File", BugrokViewModel.formatBytes(pack.sizeBytes))
            PackMetaLine("SHA-256", pack.sha256Hash.take(24) + "...")
            if (pack.contentKey.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AES Key (Didekripsi)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = onCopyKey) {
                        Text(
                            text = pack.contentKey.take(16) + "... (Salin)",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onInspectManifest,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Manifest")
                }
                Button(
                    onClick = onSaveToDevice,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("export_library_pack_${pack.id}")
                ) {
                    Icon(Icons.Default.SaveAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Ekspor .mcpack")
                }
            }
        }
    }
}

@Composable
private fun PackMetaLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun EmptyPackStateCard(
    title: String,
    subtitle: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FolderZip,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onAction,
                modifier = Modifier.height(48.dp)
            ) {
                Text(actionLabel)
            }
        }
    }
}
