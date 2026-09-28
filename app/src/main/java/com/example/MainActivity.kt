package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Payment
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.InvoiceEntity
import com.example.ui.components.AuthDialog
import com.example.ui.components.NotificationsDialog
import com.example.ui.components.UserRoleSelectorDialog
import com.example.ui.screens.CreateInvoiceScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.InvoiceDetailScreen
import com.example.ui.screens.InvoicesScreen
import com.example.ui.screens.RemindersScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SyncAndApiScreen
import com.example.ui.screens.TaxReportsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.GstViewModel

enum class NavScreen(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    DASHBOARD("Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    INVOICES("Ledgers & Bills", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong),
    TAX_REPORTS("Daybook & Tax", Icons.Filled.Assessment, Icons.Outlined.Assessment),
    REMINDERS("Reminders", Icons.Filled.Payment, Icons.Outlined.Payment),
    INVENTORY("Catalog", Icons.Filled.Inventory2, Icons.Outlined.Inventory2),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val gstViewModel: GstViewModel = viewModel()
            val darkModeOverride by gstViewModel.darkModeOverride.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val isDarkTheme = darkModeOverride ?: systemDark

            MyApplicationTheme(darkTheme = isDarkTheme) {
                MainAppContent(viewModel = gstViewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: GstViewModel) {
    var currentScreen by remember { mutableStateOf(NavScreen.DASHBOARD) }
    var viewingInvoiceDetail by remember { mutableStateOf(false) }
    var creatingInvoice by remember { mutableStateOf(false) }
    var viewingSyncAndApi by remember { mutableStateOf(false) }

    var showRoleDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showAuthDialog by remember { mutableStateOf(false) }

    val selectedInvoice by viewModel.selectedInvoice.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    // Handle back button for sub-screens
    BackHandler(enabled = creatingInvoice || viewingInvoiceDetail || viewingSyncAndApi) {
        if (creatingInvoice) {
            creatingInvoice = false
        } else if (viewingInvoiceDetail) {
            viewingInvoiceDetail = false
            viewModel.selectInvoice(null)
        } else if (viewingSyncAndApi) {
            viewingSyncAndApi = false
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (!creatingInvoice && !viewingInvoiceDetail) {
                NavigationBar(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("bottom_nav_bar")
                ) {
                    NavScreen.values().forEach { screen ->
                        val isSelected = currentScreen == screen && !viewingSyncAndApi
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                viewingSyncAndApi = false
                                currentScreen = screen
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                    contentDescription = screen.title
                                )
                            },
                            label = { Text(screen.title) },
                            modifier = Modifier.testTag("nav_item_${screen.name.lowercase()}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                creatingInvoice -> {
                    CreateInvoiceScreen(
                        viewModel = viewModel,
                        onBack = { creatingInvoice = false }
                    )
                }
                viewingInvoiceDetail && selectedInvoice != null -> {
                    InvoiceDetailScreen(
                        invoice = selectedInvoice!!,
                        viewModel = viewModel,
                        onBack = {
                            viewingInvoiceDetail = false
                            viewModel.selectInvoice(null)
                        }
                    )
                }
                viewingSyncAndApi -> {
                    SyncAndApiScreen(
                        viewModel = viewModel,
                        onBack = { viewingSyncAndApi = false }
                    )
                }
                else -> {
                    when (currentScreen) {
                        NavScreen.DASHBOARD -> {
                            DashboardScreen(
                                viewModel = viewModel,
                                onNavigateToCreateInvoice = { creatingInvoice = true },
                                onNavigateToInvoices = { currentScreen = NavScreen.INVOICES },
                                onNavigateToReports = { currentScreen = NavScreen.TAX_REPORTS },
                                onNavigateToInventory = { currentScreen = NavScreen.INVENTORY },
                                onNavigateToReminders = { currentScreen = NavScreen.REMINDERS },
                                onNavigateToSync = { viewingSyncAndApi = true },
                                onSelectInvoice = { inv ->
                                    viewModel.selectInvoice(inv)
                                    viewingInvoiceDetail = true
                                },
                                onOpenRoleDialog = { showRoleDialog = true },
                                onOpenNotifications = { showNotificationsDialog = true },
                                onOpenAuthDialog = { showAuthDialog = true }
                            )
                        }
                        NavScreen.INVOICES -> {
                            InvoicesScreen(
                                viewModel = viewModel,
                                onNavigateToCreateInvoice = { creatingInvoice = true },
                                onSelectInvoice = { inv ->
                                    viewModel.selectInvoice(inv)
                                    viewingInvoiceDetail = true
                                }
                            )
                        }
                        NavScreen.TAX_REPORTS -> {
                            TaxReportsScreen(
                                viewModel = viewModel,
                                onBack = { currentScreen = NavScreen.DASHBOARD }
                            )
                        }
                        NavScreen.REMINDERS -> {
                            RemindersScreen(
                                viewModel = viewModel,
                                onBack = { currentScreen = NavScreen.DASHBOARD },
                                onSelectInvoice = { inv ->
                                    viewModel.selectInvoice(inv)
                                    viewingInvoiceDetail = true
                                }
                            )
                        }
                        NavScreen.INVENTORY -> {
                            InventoryScreen(
                                viewModel = viewModel,
                                onBack = { currentScreen = NavScreen.DASHBOARD }
                            )
                        }
                        NavScreen.SETTINGS -> {
                            SettingsScreen(
                                viewModel = viewModel,
                                onOpenRoleDialog = { showRoleDialog = true },
                                onOpenAuthDialog = { showAuthDialog = true }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showRoleDialog) {
        UserRoleSelectorDialog(
            availableUsers = viewModel.availableUsers,
            currentUser = currentUser,
            onSelectUser = { user -> viewModel.switchUser(user) },
            onDismiss = { showRoleDialog = false }
        )
    }

    if (showNotificationsDialog) {
        NotificationsDialog(
            notifications = notifications,
            onMarkAllRead = { viewModel.markAllNotificationsRead() },
            onDismiss = { showNotificationsDialog = false }
        )
    }

    if (showAuthDialog) {
        AuthDialog(
            authManager = viewModel.authManager,
            onAuthSuccess = { /* handled reactively by viewModel listener */ },
            onDismiss = { showAuthDialog = false }
        )
    }
}
