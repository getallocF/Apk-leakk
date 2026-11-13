package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.FolderZip
import androidx.compose.material.icons.outlined.NetworkCheck
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.XboxAuthStatus
import com.example.ui.BugrokTab
import com.example.ui.BugrokViewModel
import com.example.ui.components.XboxAuthBottomSheet
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DiagnosticsScreen
import com.example.ui.screens.ProfilesScreen
import com.example.ui.screens.ResourcePacksScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val viewModel: BugrokViewModel = viewModel(
                factory = BugrokViewModel.provideFactory(context)
            )
            val isDark by viewModel.isDarkTheme.collectAsStateWithLifecycle()

            MyApplicationTheme(darkTheme = isDark) {
                BugrokMainApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BugrokMainApp(viewModel: BugrokViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()

    val serverHost by viewModel.serverHostInput.collectAsStateWithLifecycle()
    val serverPort by viewModel.serverPortInput.collectAsStateWithLifecycle()
    val customEndpoint by viewModel.customEndpointInput.collectAsStateWithLifecycle()
    val autoDecrypt by viewModel.autoDecryptEnabled.collectAsStateWithLifecycle()

    val xboxSession by viewModel.xboxSession.collectAsStateWithLifecycle()
    val showXboxSheet by viewModel.showXboxAuthSheet.collectAsStateWithLifecycle()

    val isScanning by viewModel.isScanningServer.collectAsStateWithLifecycle()
    val activeServerStatus by viewModel.activeServerStatus.collectAsStateWithLifecycle()
    val remotePacks by viewModel.remotePacks.collectAsStateWithLifecycle()
    val downloadedPacks by viewModel.downloadedPacks.collectAsStateWithLifecycle()
    val savedProfiles by viewModel.savedProfiles.collectAsStateWithLifecycle()

    val protocolLogs by viewModel.protocolLogs.collectAsStateWithLifecycle()
    val selectedLogFilter by viewModel.selectedLogFilter.collectAsStateWithLifecycle()

    val diagHost by viewModel.diagTargetHost.collectAsStateWithLifecycle()
    val diagPort by viewModel.diagTargetPort.collectAsStateWithLifecycle()
    val diagCustomPorts by viewModel.diagCustomPorts.collectAsStateWithLifecycle()
    val isRunningDiag by viewModel.isRunningDiag.collectAsStateWithLifecycle()
    val diagRakNetResult by viewModel.diagRakNetResult.collectAsStateWithLifecycle()
    val dnsSummary by viewModel.dnsLookupSummary.collectAsStateWithLifecycle()
    val portScanResults by viewModel.portScanResults.collectAsStateWithLifecycle()

    val bannerMessage by viewModel.statusBannerMessage.collectAsStateWithLifecycle()

    // SAF Launchers for Exporting .mcpack, Exporting JSON Config, and Importing JSON Config
    val exportMcpackLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null) {
            viewModel.writePendingMcpackToUri(uri)
        }
    }

    val exportConfigLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.exportConfigurationToUri(uri)
        }
    }

    val importConfigLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importConfigurationFromUri(uri)
        }
    }

    val exportApkLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.android.package-archive")
    ) { uri ->
        if (uri != null) {
            viewModel.exportInstalledApkToUri(uri)
        }
    }

    LaunchedEffect(bannerMessage) {
        if (bannerMessage != null) {
            delay(3800)
            viewModel.dismissBannerMessage()
        }
    }

    val isXboxConnected = xboxSession.status == XboxAuthStatus.AUTHENTICATED

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderZip,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "BugrokTool",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "Bedrock Pack Downloader & Network Suite",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Xbox Auth Status Pill
                    Surface(
                        color = if (isXboxConnected) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable {
                                viewModel.setShowXboxAuthSheet(true)
                                if (xboxSession.userCode.isEmpty() && !isXboxConnected) {
                                    viewModel.startXboxDeviceCodeFlow()
                                }
                            }
                            .testTag("topbar_xbox_pill")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isXboxConnected) {
                                            MaterialTheme.colorScheme.secondary
                                        } else {
                                            MaterialTheme.colorScheme.tertiary
                                        }
                                    )
                            )
                            Icon(
                                imageVector = if (isXboxConnected) Icons.Default.VerifiedUser else Icons.Default.Security,
                                contentDescription = "Xbox Live Status",
                                tint = if (isXboxConnected) {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = if (isXboxConnected) xboxSession.gamertag.take(10) else "Login Xbox",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isXboxConnected) {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.toggleDarkTheme() },
                        modifier = Modifier.testTag("toggle_theme_button")
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Ubah Tema Terang/Gelap"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                windowInsets = WindowInsets.navigationBars,
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == BugrokTab.DASHBOARD,
                    onClick = { viewModel.selectTab(BugrokTab.DASHBOARD) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == BugrokTab.DASHBOARD) {
                                Icons.Filled.Dashboard
                            } else {
                                Icons.Outlined.Dashboard
                            },
                            contentDescription = "Dashboard"
                        )
                    },
                    label = { Text("Dashboard") },
                    modifier = Modifier.testTag("nav_tab_dashboard")
                )

                NavigationBarItem(
                    selected = currentTab == BugrokTab.RESOURCE_PACKS,
                    onClick = { viewModel.selectTab(BugrokTab.RESOURCE_PACKS) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (remotePacks.isNotEmpty()) {
                                    Badge { Text("${remotePacks.size}") }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (currentTab == BugrokTab.RESOURCE_PACKS) {
                                    Icons.Filled.FolderZip
                                } else {
                                    Icons.Outlined.FolderZip
                                },
                                contentDescription = "Resource Pack"
                            )
                        }
                    },
                    label = { Text("Resource Pack") },
                    modifier = Modifier.testTag("nav_tab_packs")
                )

                NavigationBarItem(
                    selected = currentTab == BugrokTab.DIAGNOSTICS,
                    onClick = { viewModel.selectTab(BugrokTab.DIAGNOSTICS) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == BugrokTab.DIAGNOSTICS) {
                                Icons.Filled.NetworkCheck
                            } else {
                                Icons.Outlined.NetworkCheck
                            },
                            contentDescription = "Diagnostik"
                        )
                    },
                    label = { Text("Diagnostik") },
                    modifier = Modifier.testTag("nav_tab_diagnostics")
                )

                NavigationBarItem(
                    selected = currentTab == BugrokTab.PROFILES,
                    onClick = { viewModel.selectTab(BugrokTab.PROFILES) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == BugrokTab.PROFILES) {
                                Icons.Filled.Storage
                            } else {
                                Icons.Outlined.Storage
                            },
                            contentDescription = "Profil & Konfig"
                        )
                    },
                    label = { Text("Profil") },
                    modifier = Modifier.testTag("nav_tab_profiles")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 700.dp)
            ) {
                when (currentTab) {
                    BugrokTab.DASHBOARD -> {
                        DashboardScreen(
                            serverHost = serverHost,
                            serverPort = serverPort,
                            customEndpoint = customEndpoint,
                            autoDecrypt = autoDecrypt,
                            xboxSession = xboxSession,
                            isScanning = isScanning,
                            activeServerStatus = activeServerStatus,
                            remotePacks = remotePacks,
                            savedProfiles = savedProfiles,
                            protocolLogs = protocolLogs,
                            selectedLogFilter = selectedLogFilter,
                            onHostChange = viewModel::updateServerHost,
                            onPortChange = viewModel::updateServerPort,
                            onEndpointChange = viewModel::updateCustomEndpoint,
                            onAutoDecryptChange = viewModel::setAutoDecrypt,
                            onOpenXboxSheet = {
                                viewModel.setShowXboxAuthSheet(true)
                                if (xboxSession.userCode.isEmpty() && !isXboxConnected) {
                                    viewModel.startXboxDeviceCodeFlow()
                                }
                            },
                            onConnectAndScan = viewModel::connectAndScanServerPacks,
                            onSelectProfile = viewModel::applyProfileToConnection,
                            onDownloadPack = viewModel::downloadSingleResourcePack,
                            onNavigateToTab = viewModel::selectTab,
                            onLogFilterChange = viewModel::setLogFilter,
                            onClearLogs = viewModel::clearLogs,
                            onShowMessage = viewModel::showBanner
                        )
                    }

                    BugrokTab.RESOURCE_PACKS -> {
                        ResourcePacksScreen(
                            remotePacks = remotePacks,
                            downloadedPacks = downloadedPacks,
                            activeServerHost = serverHost,
                            activeServerPort = serverPort,
                            onDownloadSinglePack = viewModel::downloadSingleResourcePack,
                            onDownloadAllPacks = viewModel::downloadAllDetectedPacks,
                            onSaveMcpackToDevice = { localPath ->
                                val fileName = viewModel.prepareExportMcpackFile(localPath)
                                exportMcpackLauncher.launch(fileName)
                            },
                            onDeleteDownloadedPack = viewModel::deleteDownloadedPack,
                            onNavigateBackToDashboard = { viewModel.selectTab(BugrokTab.DASHBOARD) },
                            onCopiedNotice = viewModel::showBanner
                        )
                    }

                    BugrokTab.DIAGNOSTICS -> {
                        DiagnosticsScreen(
                            targetHost = diagHost,
                            targetPort = diagPort,
                            customPortsList = diagCustomPorts,
                            isRunning = isRunningDiag,
                            rakNetResult = diagRakNetResult,
                            dnsSummary = dnsSummary,
                            portScanResults = portScanResults,
                            onHostChange = viewModel::updateDiagTargetHost,
                            onPortChange = viewModel::updateDiagTargetPort,
                            onCustomPortsChange = viewModel::updateDiagCustomPorts,
                            onRunDiagnostics = viewModel::runFullNetworkDiagnostics,
                            onNavigateBack = { viewModel.selectTab(BugrokTab.DASHBOARD) }
                        )
                    }

                    BugrokTab.PROFILES -> {
                        ProfilesScreen(
                            profiles = savedProfiles,
                            onUseAndConnectProfile = { profile ->
                                viewModel.applyProfileToConnection(profile)
                                viewModel.selectTab(BugrokTab.DASHBOARD)
                            },
                            onSaveProfile = viewModel::saveServerProfile,
                            onToggleFavorite = viewModel::toggleFavoriteProfile,
                            onDeleteProfile = viewModel::deleteServerProfile,
                            onExportConfigClick = {
                                exportConfigLauncher.launch("bugroktool_config.json")
                            },
                            onImportConfigClick = {
                                importConfigLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                            },
                            onExportApkClick = {
                                exportApkLauncher.launch("BugrokTool-v1.0.apk")
                            },
                            onNavigateBack = { viewModel.selectTab(BugrokTab.DASHBOARD) }
                        )
                    }
                }

                // Floating Status Toast Banner
                AnimatedVisibility(
                    visible = bannerMessage != null,
                    enter = slideInVertically { -it } + fadeIn(),
                    exit = slideOutVertically { -it } + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.inverseSurface,
                        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                        shape = RoundedCornerShape(14.dp),
                        shadowElevation = 6.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
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
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = bannerMessage.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            IconButton(
                                onClick = { viewModel.dismissBannerMessage() },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Tutup Notifikasi",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showXboxSheet) {
        XboxAuthBottomSheet(
            session = xboxSession,
            onDismiss = { viewModel.setShowXboxAuthSheet(false) },
            onRequestNewDeviceCode = { viewModel.startXboxDeviceCodeFlow() },
            onVerifyDeviceCode = { gamertag ->
                viewModel.completeXboxDeviceCodeVerification(gamertag)
            },
            onDirectLinkSession = { gamertag, token ->
                viewModel.linkXboxDirectSession(gamertag, token)
            },
            onDisconnect = { viewModel.disconnectXboxAccount() },
            onCopiedNotice = viewModel::showBanner
        )
    }
}
