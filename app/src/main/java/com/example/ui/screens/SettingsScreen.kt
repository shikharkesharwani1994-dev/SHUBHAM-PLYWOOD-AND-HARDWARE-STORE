package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CompanyProfile
import com.example.data.model.UserRole
import com.example.ui.components.RoleBadge
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.PrimaryBlue
import com.example.ui.viewmodel.GstViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: GstViewModel,
    onOpenRoleDialog: () -> Unit,
    onOpenAuthDialog: () -> Unit
) {
    val company by viewModel.companyProfile.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val darkModeOverride by viewModel.darkModeOverride.collectAsState()

    val notifyOverdue by viewModel.notifyOverdue.collectAsState()
    val notifyLowStock by viewModel.notifyLowStock.collectAsState()
    val notifyTaxDeadlines by viewModel.notifyTaxDeadlines.collectAsState()
    val notifyCloudSync by viewModel.notifyCloudSync.collectAsState()

    var tradeName by remember(company) { mutableStateOf(company.tradeName) }
    var gstin by remember(company) { mutableStateOf(company.gstin) }
    var address by remember(company) { mutableStateOf(company.address) }
    var bankAccount by remember(company) { mutableStateOf(company.bankAccount) }
    var ifscCode by remember(company) { mutableStateOf(company.ifscCode) }
    var upiId by remember(company) { mutableStateOf(company.upiId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings & Access Control", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Role & Administrative Oversight
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryBlue.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentUser.avatarInitials,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue,
                                    fontSize = 16.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = currentUser.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = currentUser.email,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        RoleBadge(role = currentUser.role)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Current Role Permissions: ${currentUser.role.description}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onOpenRoleDialog,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("btn_switch_role")
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Switch Role")
                        }

                        OutlinedButton(
                            onClick = onOpenAuthDialog,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("btn_open_auth")
                        ) {
                            Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (currentUser.firebaseUid != null) "Firebase User" else "Sign In")
                        }
                    }
                }
            }

            // Firebase Authentication & Cloud Identity Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = PrimaryBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Firebase Cloud Auth",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        val isFirebaseUser = currentUser.firebaseUid != null
                        Surface(
                            color = if (isFirebaseUser) EmeraldGreen.copy(alpha = 0.15f) else Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (isFirebaseUser) "Connected" else "Configured",
                                color = if (isFirebaseUser) EmeraldGreen else Color(0xFFB45309),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (currentUser.firebaseUid != null) {
                            "Authenticated as ${currentUser.email} (UID: ${currentUser.firebaseUid?.take(10)}...)"
                        } else {
                            "Google Sign-In & Firebase Auth are enabled with Android Credential Manager."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onOpenAuthDialog,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("btn_manage_firebase_auth")
                    ) {
                        Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (currentUser.firebaseUid != null) "Manage Firebase Account" else "Sign In with Google / Firebase")
                    }
                }
            }

            // Dark Mode and Visual Appearance
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DarkMode, contentDescription = null, tint = PrimaryBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Visual Appearance & Theme",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = darkModeOverride == null,
                            onClick = { viewModel.setDarkModeOverride(null) }
                        )
                        Text(
                            text = "System Default",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.clickable { viewModel.setDarkModeOverride(null) }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = darkModeOverride == true,
                            onClick = { viewModel.setDarkModeOverride(true) }
                        )
                        Text(
                            text = "Dark Mode",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.clickable { viewModel.setDarkModeOverride(true) }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = darkModeOverride == false,
                            onClick = { viewModel.setDarkModeOverride(false) }
                        )
                        Text(
                            text = "Light Mode",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.clickable { viewModel.setDarkModeOverride(false) }
                        )
                    }
                }
            }

            // Custom Notification Preferences
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = PrimaryBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Push Notification Channels",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    NotificationSettingRow(
                        title = "Overdue Invoice Alerts",
                        description = "Immediate notification when payments pass due date",
                        checked = notifyOverdue,
                        onCheckedChange = { viewModel.notifyOverdue.value = it }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    NotificationSettingRow(
                        title = "Low Stock Warnings",
                        description = "Trigger alerts when catalog stock hits reorder threshold",
                        checked = notifyLowStock,
                        onCheckedChange = { viewModel.notifyLowStock.value = it }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    NotificationSettingRow(
                        title = "GST Filing Deadlines",
                        description = "Reminders for GSTR-1 (11th) and GSTR-3B (20th)",
                        checked = notifyTaxDeadlines,
                        onCheckedChange = { viewModel.notifyTaxDeadlines.value = it }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    NotificationSettingRow(
                        title = "Multi-Device Sync Feed",
                        description = "Alerts when co-workers create/edit invoices from other devices",
                        checked = notifyCloudSync,
                        onCheckedChange = { viewModel.notifyCloudSync.value = it }
                    )
                }
            }

            // Company GST Master Details
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Business, contentDescription = null, tint = PrimaryBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Company Legal & GST Master",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (currentUser.role != UserRole.ADMIN) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Note: Viewing mode. Only Admin role can alter company legal tax profile.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = tradeName,
                        onValueChange = { tradeName = it },
                        label = { Text("Trade Name") },
                        enabled = currentUser.role == UserRole.ADMIN,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = gstin,
                        onValueChange = { gstin = it },
                        label = { Text("GSTIN") },
                        enabled = currentUser.role == UserRole.ADMIN,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Registered Address") },
                        enabled = currentUser.role == UserRole.ADMIN,
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = bankAccount,
                            onValueChange = { bankAccount = it },
                            label = { Text("Bank A/C No") },
                            enabled = currentUser.role == UserRole.ADMIN,
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = ifscCode,
                            onValueChange = { ifscCode = it },
                            label = { Text("IFSC Code") },
                            enabled = currentUser.role == UserRole.ADMIN,
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = upiId,
                        onValueChange = { upiId = it },
                        label = { Text("UPI ID (for Reminders & QR)") },
                        enabled = currentUser.role == UserRole.ADMIN,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    if (currentUser.role == UserRole.ADMIN) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                viewModel.updateCompanyProfile(
                                    company.copy(
                                        tradeName = tradeName,
                                        gstin = gstin,
                                        address = address,
                                        bankAccount = bankAccount,
                                        ifscCode = ifscCode,
                                        upiId = upiId
                                    )
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("btn_save_company_profile")
                        ) {
                            Text("Save Company Settings")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationSettingRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
