package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.LogLevel
import com.example.data.PackTransferState
import com.example.data.ProtocolLogEntry
import com.example.data.RemotePackItem
import com.example.data.ServerProfileEntity
import com.example.data.ServerRakNetStatus
import com.example.data.XboxAuthSession
import com.example.data.XboxAuthStatus
import com.example.ui.BugrokTab
import com.example.ui.BugrokViewModel
import com.example.ui.theme.JetBrainsMonoFontFamily
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    serverHost: String,
    serverPort: String,
    customEndpoint: String,
    autoDecrypt: Boolean,
    xboxSession: XboxAuthSession,
    isScanning: Boolean,
    activeServerStatus: ServerRakNetStatus?,
    remotePacks: List<RemotePackItem>,
    savedProfiles: List<ServerProfileEntity>,
    protocolLogs: List<ProtocolLogEntry>,
    selectedLogFilter: LogLevel?,
    onHostChange: (String) -> Unit,
    onPortChange: (String) -> Unit,
    onEndpointChange: (String) -> Unit,
    onAutoDecryptChange: (Boolean) -> Unit,
    onOpenXboxSheet: () -> Unit,
    onConnectAndScan: () -> Unit,
    onSelectProfile: (ServerProfileEntity) -> Unit,
    onDownloadPack: (String) -> Unit,
    onNavigateToTab: (BugrokTab) -> Unit,
    onLogFilterChange: (LogLevel?) -> Unit,
    onClearLogs: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val isXboxConnected = xboxSession.status == XboxAuthStatus.AUTHENTICATED

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_lazy_column"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hero Visual & Active Service Overview Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_bedrock_hub_1794569318138),
                        contentDescription = "BugrokTool Bedrock Resource Pack Hub",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(176.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(176.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x880B1120),
                                        Color(0xEB0B1120)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = Color(0xFF2563EB).copy(alpha = 0.9f),
                                shape = RoundedCornerShape(50)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "RAKNET v11 • BEDROCK 1.21.60",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White
                                    )
                                }
                            }

                            Surface(
                                color = if (isXboxConnected) Color(0xFF15803D) else Color(0xFFD97706),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.clickable { onOpenXboxSheet() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isXboxConnected) Icons.Default.VerifiedUser else Icons.Default.Security,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = if (isXboxConnected) "XBOX: ${xboxSession.gamertag}" else "XBOX BELUM TERTAUT",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "BugrokTool Pack Extractor",
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White
                        )
                        Text(
                            text = "Inspeksi server Bedrock via IP & Port, tautkan rantai token Xbox Live, dan unduh Resource Pack (.mcpack) dengan dekripsi AES-256-CFB8 otomatis.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCBD5E1),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // 2. Xbox Live Account Status & Service Control Card
        item {
            ElevatedCard(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenXboxSheet() }
                        .padding(16.dp)
                        .testTag("xbox_status_card"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (isXboxConnected) {
                                    MaterialTheme.colorScheme.secondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.tertiaryContainer
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isXboxConnected) Icons.Default.VerifiedUser else Icons.Default.Link,
                            contentDescription = "Status Akun Xbox Live",
                            tint = if (isXboxConnected) {
                                MaterialTheme.colorScheme.secondary
                            } else {
                                MaterialTheme.colorScheme.tertiary
                            },
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isXboxConnected) {
                                "Xbox Live Terhubung • ${xboxSession.gamertag}"
                            } else {
                                "Tautkan Akun Xbox Live (Wajib untuk Handshake)"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isXboxConnected) {
                                "XUID: ${xboxSession.xuid} • ECDSA secp384r1 Siap"
                            } else {
                                "Ketuk untuk login via Microsoft Device Code atau Token XSTS"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = onOpenXboxSheet,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isXboxConnected) {
                                MaterialTheme.colorScheme.secondaryContainer
                            } else {
                                MaterialTheme.colorScheme.secondary
                            },
                            contentColor = if (isXboxConnected) {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSecondary
                            }
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("open_xbox_auth_button")
                    ) {
                        Text(
                            text = if (isXboxConnected) "Detail" else "Login Xbox",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }

        // 3. Server IP & Port Configuration + Resource Pack Scanner Card
        item {
            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Dns,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Target Server Bedrock",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }

                        AssistChip(
                            onClick = { onNavigateToTab(BugrokTab.PROFILES) },
                            label = { Text("${savedProfiles.size} Profil") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Storage,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            modifier = Modifier.testTag("manage_profiles_chip")
                        )
                    }

                    // Quick Server Profile Selector Chips
                    if (savedProfiles.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(savedProfiles, key = { it.id }) { profile ->
                                val isSelected = profile.host.equals(serverHost.trim(), ignoreCase = true) &&
                                    profile.port.toString() == serverPort.trim()
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onSelectProfile(profile) },
                                    label = { Text(profile.name) },
                                    leadingIcon = if (isSelected) {
                                        {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    } else null,
                                    modifier = Modifier.testTag("profile_chip_${profile.id}")
                                )
                            }
                        }
                    }

                    // Server IP / Domain & Port Inputs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = serverHost,
                            onValueChange = onHostChange,
                            label = { Text("IP / Domain Server") },
                            placeholder = { Text("geo.hivebedrock.network") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(0.68f)
                                .testTag("server_host_input")
                        )

                        OutlinedTextField(
                            value = serverPort,
                            onValueChange = onPortChange,
                            label = { Text("Port") },
                            placeholder = { Text("19132") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(0.32f)
                                .testTag("server_port_input")
                        )
                    }

                    // Custom Endpoint & Auto-Decrypt Toggle
                    OutlinedTextField(
                        value = customEndpoint,
                        onValueChange = onEndpointChange,
                        label = { Text("Endpoint RakNet / Proxy Kustom") },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.labelMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_endpoint_input")
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "Auto-Decrypt AES-256-CFB8",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "Dekripsi otomatis paket menggunakan Content Key server",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = autoDecrypt,
                            onCheckedChange = onAutoDecryptChange,
                            modifier = Modifier.testTag("auto_decrypt_switch")
                        )
                    }

                    Button(
                        onClick = onConnectAndScan,
                        enabled = !isScanning,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("connect_scan_button")
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.5.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(Modifier.width(10.dp))
                            Text("Handshake RakNet & Memindai Pack...")
                        } else {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Hubungkan & Pindai Resource Pack",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 4. Live Server Status & Discovered Resource Packs Preview
        item {
            AnimatedVisibility(visible = activeServerStatus != null) {
                val status = activeServerStatus ?: return@AnimatedVisibility
                ElevatedCard(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.secondary)
                                )
                                Text(
                                    text = status.motdLine1,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "${status.latencyMs} ms",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MetricPill(
                                label = "IP Target",
                                value = "${status.resolvedIp}:${status.port}",
                                modifier = Modifier.weight(1f)
                            )
                            MetricPill(
                                label = "Versi / Protokol",
                                value = "${status.versionName} (${status.protocolVersion})",
                                modifier = Modifier.weight(1f)
                            )
                            MetricPill(
                                label = "Pemain Online",
                                value = "${status.onlinePlayers}/${status.maxPlayers}",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (remotePacks.isNotEmpty()) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${remotePacks.size} Resource Pack Terdeteksi",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                OutlinedButton(
                                    onClick = { onNavigateToTab(BugrokTab.RESOURCE_PACKS) },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("view_all_packs_button")
                                ) {
                                    Text("Buka Manajer Pack", style = MaterialTheme.typography.labelLarge)
                                    Spacer(Modifier.width(4.dp))
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            remotePacks.forEach { pack ->
                                DashboardPackQuickRow(
                                    pack = pack,
                                    onDownload = { onDownloadPack(pack.uuid) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Quick Access Network Diagnostics Strip
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { onNavigateToTab(BugrokTab.DIAGNOSTICS) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("quick_diag_button")
                ) {
                    Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Ping, DNS & Port")
                }

                OutlinedButton(
                    onClick = { onNavigateToTab(BugrokTab.RESOURCE_PACKS) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("quick_packs_button")
                ) {
                    Icon(Icons.Default.FolderZip, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Pustaka .mcpack")
                }
            }
        }

        // 6. Real-Time Protocol & RakNet Console Card
        item {
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
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Log Protokol RakNet & Bedrock",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        Row {
                            IconButton(
                                onClick = {
                                    val fullLog = protocolLogs.joinToString("\n") { "[${it.tag}] ${it.message}" }
                                    clipboardManager.setText(AnnotatedString(fullLog))
                                    onShowMessage("Log protokol disalin ke clipboard")
                                },
                                modifier = Modifier.testTag("copy_logs_button")
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "Salin Log",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = onClearLogs,
                                modifier = Modifier.testTag("clear_logs_button")
                            ) {
                                Icon(
                                    Icons.Default.DeleteSweep,
                                    contentDescription = "Bersihkan Log",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Filter Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedLogFilter == null,
                            onClick = { onLogFilterChange(null) },
                            label = { Text("Semua (${protocolLogs.size})") }
                        )
                        listOf(LogLevel.RAKNET, LogLevel.XBOX, LogLevel.PACK, LogLevel.WARN).forEach { lvl ->
                            FilterChip(
                                selected = selectedLogFilter == lvl,
                                onClick = {
                                    onLogFilterChange(if (selectedLogFilter == lvl) null else lvl)
                                },
                                label = { Text(lvl.name) }
                            )
                        }
                    }

                    val filteredLogs = if (selectedLogFilter == null) {
                        protocolLogs.take(12)
                    } else {
                        protocolLogs.filter { it.level == selectedLogFilter }.take(12)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF090D16))
                            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        if (filteredLogs.isEmpty()) {
                            Text(
                                text = "Belum ada entri log pada kategori ini.",
                                fontFamily = JetBrainsMonoFontFamily,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF64748B)
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                                filteredLogs.forEach { entry ->
                                    val badgeColor = when (entry.level) {
                                        LogLevel.RAKNET -> Color(0xFF60A5FA)
                                        LogLevel.XBOX -> Color(0xFF4ADE80)
                                        LogLevel.PACK -> Color(0xFFFBBF24)
                                        LogLevel.WARN -> Color(0xFFFB923C)
                                        LogLevel.ERROR -> Color(0xFFF87171)
                                        LogLevel.INFO -> Color(0xFF94A3B8)
                                    }
                                    Text(
                                        text = "${timeFormat.format(Date(entry.timestamp))} [${entry.tag}] ${entry.message}",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = badgeColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DashboardPackQuickRow(
    pack: RemotePackItem,
    onDownload: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pack.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "v${pack.version} • ${BugrokViewModel.formatBytes(pack.sizeBytes)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (pack.isEncrypted) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                Icons.Default.Key,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "AES-256 Key",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
            }

            when (pack.state) {
                PackTransferState.COMPLETED -> {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(50)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Siap (.mcpack)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
                PackTransferState.REQUESTING_CHUNKS,
                PackTransferState.DECRYPTING_AES_CFB8,
                PackTransferState.PACKAGING_MCPACK -> {
                    CircularProgressIndicator(
                        progress = { pack.progress },
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 3.dp
                    )
                }
                else -> {
                    Button(
                        onClick = onDownload,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("quick_download_${pack.uuid}")
                    ) {
                        Icon(
                            Icons.Default.CloudDownload,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Unduh", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        if (pack.state in listOf(
                PackTransferState.REQUESTING_CHUNKS,
                PackTransferState.DECRYPTING_AES_CFB8,
                PackTransferState.PACKAGING_MCPACK
            )
        ) {
            LinearProgressIndicator(
                progress = { pack.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
            )
        }
    }
}
