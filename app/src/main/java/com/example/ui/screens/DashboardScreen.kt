package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.InvoiceEntity
import com.example.ui.components.MetricCard
import com.example.ui.components.OfflineModeBanner
import com.example.ui.components.RoleBadge
import com.example.ui.components.SalesBarChart
import com.example.ui.components.StatusBadge
import com.example.ui.components.SyncStatusBadge
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CoralRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryNavy
import com.example.ui.viewmodel.GstViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: GstViewModel,
    onNavigateToCreateInvoice: () -> Unit,
    onNavigateToInvoices: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onNavigateToSync: () -> Unit,
    onSelectInvoice: (InvoiceEntity) -> Unit,
    onOpenRoleDialog: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenAuthDialog: () -> Unit
) {
    val invoices by viewModel.rawInvoices.collectAsState()
    val inventory by viewModel.inventory.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val company by viewModel.companyProfile.collectAsState()

    val pendingSyncCount = invoices.count { it.syncStatus == "PENDING_SYNC" }
    val unreadNotifCount = notifications.count { !it.isRead }

    // Aggregate metrics
    val totalSales = invoices.sumOf { it.totalAmount }
    val totalGstCollected = invoices.sumOf { it.cgstAmount + it.sgstAmount + it.igstAmount }
    val outstanding = invoices.sumOf { (it.totalAmount - it.amountPaid).coerceAtLeast(0.0) }
    val lowStockCount = inventory.count { it.stockQty <= it.reorderLevel }

    val recentInvoices = invoices.take(4)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // App Header / Status
        item {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = company.tradeName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .clickable { onOpenRoleDialog() }
                            ) {
                                Text(
                                    text = currentUser.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                RoleBadge(role = currentUser.role)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Online/Offline switch
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                    contentDescription = null,
                                    tint = if (isOnline) EmeraldGreen else CoralRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Switch(
                                    checked = isOnline,
                                    onCheckedChange = { viewModel.toggleOnlineStatus() },
                                    modifier = Modifier.size(width = 36.dp, height = 24.dp),
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = EmeraldGreen,
                                        checkedTrackColor = EmeraldGreen.copy(alpha = 0.3f),
                                        uncheckedThumbColor = CoralRed,
                                        uncheckedTrackColor = CoralRed.copy(alpha = 0.3f)
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Notification Bell
                            IconButton(
                                onClick = onOpenNotifications,
                                modifier = Modifier.testTag("notification_button")
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (unreadNotifCount > 0) {
                                            Badge(
                                                containerColor = CoralRed,
                                                contentColor = Color.White
                                            ) {
                                                Text(unreadNotifCount.toString())
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Notifications",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Firebase Auth Button
                            IconButton(
                                onClick = onOpenAuthDialog,
                                modifier = Modifier.testTag("auth_account_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Firebase Account",
                                    tint = if (currentUser.firebaseUid != null) EmeraldGreen else PrimaryBlue
                                )
                            }
                        }
                    }
                }
            }
        }

        // Hero Banner Art & Offline Banner
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                OfflineModeBanner(
                    isOnline = isOnline,
                    pendingCount = pendingSyncCount,
                    onSyncClick = { viewModel.triggerSync() }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = R.drawable.img_hero_banner_1790589820696),
                            contentDescription = "GST Pro Hero Banner",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            PrimaryNavy.copy(alpha = 0.90f),
                                            PrimaryNavy.copy(alpha = 0.40f)
                                        )
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "GST Billing & Tax Suite",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "GSTIN: ${company.gstin} • ${company.stateName} (POS: ${company.stateCode})",
                                color = Color(0xFF93C5FD),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = EmeraldGreen.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "Real-time Sync Active",
                                        color = EmeraldGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Multi-device connected",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick KPI Metric Cards
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Total Sales",
                        value = "₹${String.format(Locale.US, "%,.0f", totalSales)}",
                        subtitle = "+14.2% this month",
                        icon = Icons.Default.ReceiptLong,
                        color = PrimaryBlue,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToInvoices
                    )
                    MetricCard(
                        title = "GST Collected",
                        value = "₹${String.format(Locale.US, "%,.0f", totalGstCollected)}",
                        subtitle = "GSTR-1 Ready",
                        icon = Icons.Default.Assessment,
                        color = EmeraldGreen,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToReports
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Outstanding",
                        value = "₹${String.format(Locale.US, "%,.0f", outstanding)}",
                        subtitle = "Overdue & Pending",
                        icon = Icons.Default.Payment,
                        color = AmberGold,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToReminders
                    )
                    MetricCard(
                        title = "Low Stock Alert",
                        value = "$lowStockCount Items",
                        subtitle = if (lowStockCount > 0) "Needs Reorder" else "All In Stock",
                        icon = if (lowStockCount > 0) Icons.Default.Warning else Icons.Default.Inventory2,
                        color = if (lowStockCount > 0) CoralRed else EmeraldGreen,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToInventory
                    )
                }
            }
        }

        // Sales Trends Chart
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                SalesBarChart(
                    labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
                    values = listOf(28000.0, 45000.0, 32000.0, 85000.0, 64000.0, 112000.0, 95000.0),
                    barColor = EmeraldGreen
                )
            }
        }

        // Fast Action Buttons Row
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Button(
                            onClick = onNavigateToCreateInvoice,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("action_new_invoice")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("New Sales Bill")
                        }
                    }
                    item {
                        OutlinedButton(
                            onClick = onNavigateToInvoices,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("action_inward_supply")
                        ) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Inward & Ledgers")
                        }
                    }
                    item {
                        OutlinedButton(
                            onClick = onNavigateToReports,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("action_reports")
                        ) {
                            Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Daybook & Bank")
                        }
                    }
                    item {
                        OutlinedButton(
                            onClick = onNavigateToReminders,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("action_reminders")
                        ) {
                            Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reminders")
                        }
                    }
                    item {
                        OutlinedButton(
                            onClick = onNavigateToSync,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("action_sync")
                        ) {
                            Icon(Icons.Default.Devices, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sync & APIs")
                        }
                    }
                }
            }
        }

        // Recent Invoices Section
        item {
            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Invoices",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onNavigateToInvoices() }
                    ) {
                        Text(
                            text = "View All",
                            style = MaterialTheme.typography.labelMedium,
                            color = PrimaryBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        items(recentInvoices) { invoice ->
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val dateStr = sdf.format(Date(invoice.invoiceDate))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 5.dp)
                    .clickable { onSelectInvoice(invoice) }
                    .testTag("invoice_card_${invoice.invoiceNumber}"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = invoice.invoiceNumber,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            SyncStatusBadge(syncStatus = invoice.syncStatus)
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = invoice.customerName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$dateStr • POS: ${invoice.placeOfSupply}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "₹${String.format(Locale.US, "%,.2f", invoice.totalAmount)}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        StatusBadge(status = invoice.status)
                    }
                }
            }
        }
    }
}
