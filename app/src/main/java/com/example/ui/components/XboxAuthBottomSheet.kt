package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.XboxAuthSession
import com.example.data.XboxAuthStatus
import com.example.ui.theme.JetBrainsMonoFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XboxAuthBottomSheet(
    session: XboxAuthSession,
    onDismiss: () -> Unit,
    onRequestNewDeviceCode: () -> Unit,
    onVerifyDeviceCode: (String) -> Unit,
    onDirectLinkSession: (String, String) -> Unit,
    onDisconnect: () -> Unit,
    onCopiedNotice: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    var selectedAuthMode by remember { mutableIntStateOf(0) } // 0 = Device Code OAuth2, 1 = Direct Chain Token
    var gamertagInput by remember(session.gamertag) {
        mutableStateOf(session.gamertag.ifBlank { "KraaBedrock" })
    }
    var customXstsTokenInput by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (session.status == XboxAuthStatus.AUTHENTICATED) {
                            Icons.Default.VerifiedUser
                        } else {
                            Icons.Default.Security
                        },
                        contentDescription = "Xbox Live Authentication",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Penautan Akun Xbox Live",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "Otentikasi XBL / XSTS + Kunci ECDSA P-384 untuk Handshake Server Bedrock",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (session.status == XboxAuthStatus.AUTHENTICATED) {
                // Connected Account Details Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                    ),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Sesi Xbox Live Aktif",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Surface(
                                color = MaterialTheme.colorScheme.secondary,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "XSTS VERIFIED",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                        AuthDetailRow(label = "Gamertag", value = session.gamertag)
                        AuthDetailRow(label = "XUID", value = session.xuid)
                        AuthDetailRow(label = "User Hash (uhs)", value = session.userHash)
                        AuthDetailRow(label = "PlayFab ID", value = session.playFabSessionId)
                        AuthDetailRow(
                            label = "ECDSA P-384 Key",
                            value = session.ecdsaPublicKeyBase64.take(28) + "..."
                        )
                        AuthDetailRow(label = "XSTS Chain", value = session.xstsTokenPreview)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDisconnect,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("xbox_disconnect_button")
                    ) {
                        Icon(Icons.Default.LinkOff, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Putuskan Akun")
                    }
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("xbox_done_button")
                    ) {
                        Text("Selesai")
                    }
                }
            } else {
                // Mode Selector: Device Code vs Direct Session
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = selectedAuthMode == 0,
                        onClick = { selectedAuthMode = 0 },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        modifier = Modifier.testTag("auth_mode_device_code")
                    ) {
                        Text("Microsoft Device Code")
                    }
                    SegmentedButton(
                        selected = selectedAuthMode == 1,
                        onClick = { selectedAuthMode = 1 },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        modifier = Modifier.testTag("auth_mode_direct_token")
                    ) {
                        Text("Tautan Cepat / Token")
                    }
                }

                if (selectedAuthMode == 0) {
                    // Device Code OAuth2 Card
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                        ),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "1. Salin kode perangkat 8-digit di bawah ini dan masukkan pada halaman resmi Microsoft Link:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            if (session.status == XboxAuthStatus.REQUESTING_DEVICE_CODE) {
                                CircularProgressIndicator(modifier = Modifier.size(36.dp))
                                Text(
                                    text = "Meminta Device Code dari login.live.com...",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            } else {
                                val displayCode = session.userCode.ifBlank { " Memuat... " }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(MaterialTheme.colorScheme.background)
                                        .border(
                                            width = 1.5.dp,
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .padding(vertical = 14.dp, horizontal = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = displayCode,
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 26.sp,
                                            letterSpacing = 4.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        IconButton(
                                            onClick = {
                                                if (session.userCode.isNotBlank()) {
                                                    clipboardManager.setText(AnnotatedString(session.userCode))
                                                    onCopiedNotice("Kode ${session.userCode} disalin ke clipboard!")
                                                }
                                            },
                                            modifier = Modifier.testTag("copy_device_code_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Salin Kode",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            if (session.userCode.isNotBlank()) {
                                                clipboardManager.setText(AnnotatedString(session.userCode))
                                            }
                                            val uri = Uri.parse(session.verificationUri.ifBlank { "https://www.microsoft.com/link" })
                                            val intent = Intent(Intent.ACTION_VIEW, uri)
                                            try {
                                                context.startActivity(intent)
                                            } catch (_: Exception) {
                                                onCopiedNotice("Buka ${session.verificationUri} di browser Anda")
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("open_microsoft_link_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text("Buka microsoft.com/link")
                                    }

                                    IconButton(
                                        onClick = onRequestNewDeviceCode,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .testTag("refresh_device_code_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Minta Kode Baru"
                                        )
                                    }
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = gamertagInput,
                        onValueChange = { gamertagInput = it },
                        label = { Text("Gamertag Xbox Anda (Identitas Sesi)") },
                        placeholder = { Text("Contoh: KraaBedrock") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("xbox_gamertag_input")
                    )

                    if (!session.errorMessage.isNullOrBlank()) {
                        Text(
                            text = session.errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }

                    Button(
                        onClick = { onVerifyDeviceCode(gamertagInput) },
                        enabled = session.status != XboxAuthStatus.EXCHANGING_XSTS_CHAIN,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("verify_xbox_code_button")
                    ) {
                        if (session.status == XboxAuthStatus.EXCHANGING_XSTS_CHAIN) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                            Spacer(Modifier.width(10.dp))
                            Text("Menukar Token XBL & XSTS...")
                        } else {
                            Icon(Icons.Default.Link, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Verifikasi & Tautkan Akun Xbox Live",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    // Direct Session / Custom XSTS Token Mode
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                        ),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Tautkan sesi Xbox Live secara instan menggunakan Gamertag dan kunci ECDSA P-384 lokal atau tempelkan token XSTS3.0 kustom Anda:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            OutlinedTextField(
                                value = gamertagInput,
                                onValueChange = { gamertagInput = it },
                                label = { Text("Gamertag Xbox Live") },
                                singleLine = true,
                                leadingIcon = { Icon(Icons.Default.VerifiedUser, contentDescription = null) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("direct_gamertag_input")
                            )

                            OutlinedTextField(
                                value = customXstsTokenInput,
                                onValueChange = { customXstsTokenInput = it },
                                label = { Text("Token XSTS / Bedrock Chain (Opsional)") },
                                placeholder = { Text("Kosongkan untuk generate otomatis ECDSA P-384") },
                                singleLine = true,
                                leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("direct_xsts_input")
                            )

                            Button(
                                onClick = { onDirectLinkSession(gamertagInput, customXstsTokenInput) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.onSecondary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("direct_link_submit_button")
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Aktifkan Rantai Token Xbox Live", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}
