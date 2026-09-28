package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LedgerEntryEntity
import com.example.ui.theme.AmberGold
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.PrimaryBlue
import com.example.ui.viewmodel.GstViewModel
import com.example.util.ExportUtils
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxReportsScreen(
    viewModel: GstViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Daybook", "Cash Ledger", "Bank Ledger", "GSTR-1 & 3B")

    val ledgerEntries by viewModel.ledgerEntries.collectAsState()
    val invoices by viewModel.rawInvoices.collectAsState()
    val inwardBills by viewModel.inwardSupplies.collectAsState()
    val company by viewModel.companyProfile.collectAsState()

    // Dialogs
    var showAddBankEntryDialog by remember { mutableStateOf(false) }
    var showAddCashEntryDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Accounting & Reports", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(company.tradeName, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = PrimaryBlue
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                fontSize = 11.5.sp
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> {
                    // TAB 0: DAYBOOK
                    DaybookReportView(
                        entries = ledgerEntries,
                        company = company,
                        context = context
                    )
                }
                1 -> {
                    // TAB 1: CASH LEDGER
                    CashLedgerView(
                        entries = ledgerEntries.filter { it.accountType == "CASH" },
                        company = company,
                        context = context,
                        onAddCashEntry = { showAddCashEntryDialog = true }
                    )
                }
                2 -> {
                    // TAB 2: BANK LEDGER (ENTRY & STATEMENTS)
                    BankLedgerView(
                        entries = ledgerEntries.filter { it.accountType == "BANK" },
                        company = company,
                        context = context,
                        onAddBankEntry = { showAddBankEntryDialog = true }
                    )
                }
                3 -> {
                    // TAB 3: GSTR-1 & GSTR-3B TAX REPORTS
                    GstTaxComplianceView(
                        invoices = invoices,
                        inwardBills = inwardBills,
                        company = company,
                        context = context
                    )
                }
            }
        }
    }

    // DIALOG: ADD BANK LEDGER ENTRY
    if (showAddBankEntryDialog) {
        AddBankLedgerEntryDialog(
            company = company,
            onDismiss = { showAddBankEntryDialog = false },
            onSave = { type, amount, particulars, refNo, mode ->
                viewModel.addBankLedgerEntry(type, amount, particulars, refNo, mode)
                showAddBankEntryDialog = false
            }
        )
    }

    // DIALOG: ADD CASH LEDGER ENTRY
    if (showAddCashEntryDialog) {
        AddCashLedgerEntryDialog(
            onDismiss = { showAddCashEntryDialog = false },
            onSave = { type, amount, particulars, voucherNo ->
                viewModel.addCashEntry(type, amount, particulars, voucherNo)
                showAddCashEntryDialog = false
            }
        )
    }
}

@Composable
fun DaybookReportView(
    entries: List<LedgerEntryEntity>,
    company: com.example.data.model.CompanyProfile,
    context: Context
) {
    var selectedDayOffset by remember { mutableIntStateOf(0) } // 0 = Today, 1 = Yesterday, 2 = 2 days ago
    val dayMs = 86400000L
    val targetTime = System.currentTimeMillis() - (selectedDayOffset * dayMs)
    val sdfDay = SimpleDateFormat("dd MMMM yyyy", Locale.US)
    val currentDayStr = sdfDay.format(Date(targetTime))

    // Filter daybook entries (within 24 hours of target day)
    val dayStart = (targetTime / dayMs) * dayMs
    val dayEnd = dayStart + dayMs
    val dayEntries = entries.filter { it.date in dayStart..dayEnd }

    val totalDebit = dayEntries.sumOf { it.debit }
    val totalCredit = dayEntries.sumOf { it.credit }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Date Selector & Export Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ElevatedFilterChip(
                    selected = selectedDayOffset == 0,
                    onClick = { selectedDayOffset = 0 },
                    label = { Text("Today", fontSize = 11.sp) }
                )
                ElevatedFilterChip(
                    selected = selectedDayOffset == 1,
                    onClick = { selectedDayOffset = 1 },
                    label = { Text("Yesterday", fontSize = 11.sp) }
                )
                ElevatedFilterChip(
                    selected = selectedDayOffset == 2,
                    onClick = { selectedDayOffset = 2 },
                    label = { Text("2 Days Ago", fontSize = 11.sp) }
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(onClick = {
                    val file = ExportUtils.generateLedgerPdf(context, "Daybook_$currentDayStr", "DAYBOOK", dayEntries.ifEmpty { entries.take(15) }, company)
                    if (file != null) {
                        ExportUtils.shareExportedFile(context, file, "application/pdf", "Daybook PDF")
                    }
                }) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF", tint = PrimaryBlue)
                }
                IconButton(onClick = {
                    val file = ExportUtils.generateLedgerExcel(context, "Daybook_$currentDayStr", "DAYBOOK", dayEntries.ifEmpty { entries.take(15) }, company)
                    if (file != null) {
                        ExportUtils.shareExportedFile(context, file, "text/csv", "Daybook Excel")
                    }
                }) {
                    Icon(Icons.Default.TableChart, contentDescription = "Excel", tint = EmeraldGreen)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Day Summary Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = PrimaryBlue),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Daybook Date", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                        Text(currentDayStr, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Transactions", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                        Text("${dayEntries.size} Vouchers", color = EmeraldGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Debits: ₹ %,.2f".format(Locale.US, totalDebit), color = Color(0xFFFECACA), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("Total Credits: ₹ %,.2f".format(Locale.US, totalCredit), color = Color(0xFFBBF7D0), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text("Daily Vouchers (Sales, Purchase, Bank & Cash)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(6.dp))

        val displayedEntries = if (dayEntries.isNotEmpty()) dayEntries else entries.take(15)

        if (displayedEntries.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No transactions recorded for this day.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(displayedEntries) { entry ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("${entry.voucherType} #${entry.voucherNumber}", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(entry.accountName, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.primary)
                                }
                                Text(entry.particulars, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                if (entry.debit > 0) {
                                    Text("Dr: ₹ %,.2f".format(Locale.US, entry.debit), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFE11D48))
                                }
                                if (entry.credit > 0) {
                                    Text("Cr: ₹ %,.2f".format(Locale.US, entry.credit), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = EmeraldGreen)
                                }
                                Text(entry.paymentMethod, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CashLedgerView(
    entries: List<LedgerEntryEntity>,
    company: com.example.data.model.CompanyProfile,
    context: Context,
    onAddCashEntry: () -> Unit
) {
    // Current Cash Balance: Dr (Inflow) - Cr (Outflow)
    val totalCashIn = entries.sumOf { it.debit }
    val totalCashOut = entries.sumOf { it.credit }
    val currentCashBalance = (50000.0 + totalCashIn - totalCashOut).coerceAtLeast(0.0) // initial cash float + flow

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Cash Balance Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Cash in Hand A/c", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("₹ %,.2f".format(Locale.US, currentCashBalance), fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = PrimaryBlue)
                    }
                    Button(
                        onClick = onAddCashEntry,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Cash Entry", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Cash Received: ₹ %,.2f".format(Locale.US, totalCashIn), fontSize = 12.sp, color = EmeraldGreen, fontWeight = FontWeight.Bold)
                    Text("Cash Outflow: ₹ %,.2f".format(Locale.US, totalCashOut), fontSize = 12.sp, color = Color(0xFFE11D48), fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Export Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Cash Book Transactions", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedButton(
                    onClick = {
                        val file = ExportUtils.generateLedgerPdf(context, "Cash_in_Hand_Account", "CASH", entries, company)
                        if (file != null) {
                            ExportUtils.shareExportedFile(context, file, "application/pdf", "Cash Ledger PDF")
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PDF", fontSize = 11.sp)
                }
                OutlinedButton(
                    onClick = {
                        val file = ExportUtils.generateLedgerExcel(context, "Cash_in_Hand_Account", "CASH", entries, company)
                        if (file != null) {
                            ExportUtils.shareExportedFile(context, file, "text/csv", "Cash Ledger Excel")
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.TableChart, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Excel", fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (entries.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No cash ledger entries. Tap + Cash Entry to add one.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(entries) { entry ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(entry.particulars, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Voucher: ${entry.voucherType} #${entry.voucherNumber} • ${sdf.format(Date(entry.date))}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                if (entry.debit > 0) {
                                    Text("+₹ %,.2f".format(Locale.US, entry.debit), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = EmeraldGreen)
                                }
                                if (entry.credit > 0) {
                                    Text("-₹ %,.2f".format(Locale.US, entry.credit), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFE11D48))
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
fun BankLedgerView(
    entries: List<LedgerEntryEntity>,
    company: com.example.data.model.CompanyProfile,
    context: Context,
    onAddBankEntry: () -> Unit
) {
    val totalInflow = entries.sumOf { it.debit }
    val totalOutflow = entries.sumOf { it.credit }
    val liveBankBalance = (350000.0 + totalInflow - totalOutflow).coerceAtLeast(0.0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Bank Details Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = PrimaryBlue),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(company.bankName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("A/C: ${company.bankAccount}", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                        Text("IFSC: ${company.ifscCode} • Mandi Branch", color = Color.White.copy(alpha = 0.8f), fontSize = 11.5.sp)
                        Text("UPI: ${company.upiId}", color = AmberGold, fontSize = 11.5.sp)
                    }
                    Button(
                        onClick = onAddBankEntry,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Bank Entry", color = PrimaryBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Bank Clear Balance:", color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
                    Text("₹ %,.2f".format(Locale.US, liveBankBalance), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Export Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Bank Book Statements", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedButton(
                    onClick = {
                        val file = ExportUtils.generateLedgerPdf(context, "SBI_Bank_Account", "BANK", entries, company)
                        if (file != null) {
                            ExportUtils.shareExportedFile(context, file, "application/pdf", "Bank Ledger PDF")
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PDF", fontSize = 11.sp)
                }
                OutlinedButton(
                    onClick = {
                        val file = ExportUtils.generateLedgerExcel(context, "SBI_Bank_Account", "BANK", entries, company)
                        if (file != null) {
                            ExportUtils.shareExportedFile(context, file, "text/csv", "Bank Ledger Excel")
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.TableChart, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Excel", fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (entries.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No bank entries recorded. Tap + Bank Entry to add one.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(entries) { entry ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(entry.particulars, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Ref: ${entry.voucherNumber} • ${entry.paymentMethod} • ${sdf.format(Date(entry.date))}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                if (entry.debit > 0) {
                                    Text("+₹ %,.2f".format(Locale.US, entry.debit), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = EmeraldGreen)
                                }
                                if (entry.credit > 0) {
                                    Text("-₹ %,.2f".format(Locale.US, entry.credit), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFE11D48))
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
fun GstTaxComplianceView(
    invoices: List<com.example.data.model.InvoiceEntity>,
    inwardBills: List<com.example.data.model.InwardSupplyEntity>,
    company: com.example.data.model.CompanyProfile,
    context: Context
) {
    val totalOutwardTaxable = invoices.sumOf { it.taxableAmount }
    val totalOutputTax = invoices.sumOf { it.cgstAmount + it.sgstAmount + it.igstAmount }

    val totalInwardTaxable = inwardBills.sumOf { it.taxableAmount }
    val totalInputTaxCredit = inwardBills.sumOf { it.cgstAmount + it.sgstAmount + it.igstAmount }

    val netPayableToGovt = (totalOutputTax - totalInputTaxCredit).coerceAtLeast(0.0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // GSTR-3B Tax Comparison Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("GSTR-3B Monthly Return Summary", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("Output Tax Liability vs Inward Input Tax Credit (ITC)", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Output Tax (Sales)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("₹ %,.2f".format(Locale.US, totalOutputTax), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFE11D48))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Input Credit (Purchases)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("₹ %,.2f".format(Locale.US, totalInputTaxCredit), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = EmeraldGreen)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Net GST Cash Payable:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("₹ %,.2f".format(Locale.US, netPayableToGovt), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = PrimaryBlue)
                }
            }
        }

        // GSTR-1 Sales Breakdown Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("GSTR-1 Outward Supply Breakdown", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(8.dp))

                val b2b = invoices.filter { it.customerGstin.isNotBlank() }
                val b2c = invoices.filter { it.customerGstin.isBlank() }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("B2B Registered Wholesale:", fontSize = 12.5.sp)
                    Text("${b2b.size} bills (₹ %,.2f)".format(Locale.US, b2b.sumOf { it.totalAmount }), fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("B2C Counter Retail:", fontSize = 12.5.sp)
                    Text("${b2c.size} bills (₹ %,.2f)".format(Locale.US, b2c.sumOf { it.totalAmount }), fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = {
                        val json = JSONObject().apply {
                            put("gstin", company.gstin)
                            put("fp", "092024")
                            put("gt", totalOutwardTaxable)
                            put("b2b", JSONArray(b2b.map { it.invoiceNumber }))
                        }.toString(2)

                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("GSTR1_JSON", json))
                        Toast.makeText(context, "GSTR-1 JSON copied to clipboard for GST Portal!", Toast.LENGTH_LONG).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy GSTR-1 JSON for GST Portal")
                }
            }
        }
    }
}

@Composable
fun AddBankLedgerEntryDialog(
    company: com.example.data.model.CompanyProfile,
    onDismiss: () -> Unit,
    onSave: (type: String, amount: Double, particulars: String, refNo: String, mode: String) -> Unit
) {
    var entryType by remember { mutableStateOf("DEPOSIT") } // DEPOSIT, WITHDRAWAL, INFLOW, OUTFLOW, CHARGES
    var amountInput by remember { mutableStateOf("25000") }
    var particulars by remember { mutableStateOf("Cash Deposited in SBI Bank (Contra)") }
    var refNo by remember { mutableStateOf("BNK-DEP-${System.currentTimeMillis() % 1000}") }
    var paymentMethod by remember { mutableStateOf("CASH") }

    val typeOptions = listOf(
        "DEPOSIT" to "Cash Deposit (Contra)",
        "WITHDRAWAL" to "Cash Withdrawal (Contra)",
        "INFLOW" to "Customer Transfer (UPI/NEFT)",
        "OUTFLOW" to "Supplier Payment",
        "CHARGES" to "Bank Charges"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Bank Ledger Entry") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Select Transaction Type:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(typeOptions) { (key, label) ->
                        ElevatedFilterChip(
                            selected = entryType == key,
                            onClick = {
                                entryType = key
                                when (key) {
                                    "DEPOSIT" -> {
                                        particulars = "Cash Deposited in SBI Bank (Contra)"
                                        paymentMethod = "CASH"
                                    }
                                    "WITHDRAWAL" -> {
                                        particulars = "Cash Withdrawn from Bank for Shop (Contra)"
                                        paymentMethod = "CASH"
                                    }
                                    "INFLOW" -> {
                                        particulars = "Customer UPI/NEFT Payment Received"
                                        paymentMethod = "BANK_UPI"
                                    }
                                    "OUTFLOW" -> {
                                        particulars = "Supplier RTGS Payout"
                                        paymentMethod = "BANK_NEFT"
                                    }
                                    "CHARGES" -> {
                                        particulars = "Bank SMS / Service Charges"
                                        paymentMethod = "BANK"
                                    }
                                }
                            },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = particulars,
                    onValueChange = { particulars = it },
                    label = { Text("Particulars / Narration") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = refNo,
                        onValueChange = { refNo = it },
                        label = { Text("Ref / UTR No") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = paymentMethod,
                        onValueChange = { paymentMethod = it },
                        label = { Text("Mode") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountInput.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        onSave(entryType, amt, particulars, refNo, paymentMethod)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
            ) {
                Text("Record Bank Entry")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddCashLedgerEntryDialog(
    onDismiss: () -> Unit,
    onSave: (type: String, amount: Double, particulars: String, voucherNo: String) -> Unit
) {
    var entryType by remember { mutableStateOf("RECEIPT") } // RECEIPT, EXPENSE
    var amountInput by remember { mutableStateOf("1500") }
    var particulars by remember { mutableStateOf("Counter Cash Sale") }
    var voucherNo by remember { mutableStateOf("CSH-${System.currentTimeMillis() % 1000}") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Cash Ledger Entry") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ElevatedFilterChip(
                        selected = entryType == "RECEIPT",
                        onClick = {
                            entryType = "RECEIPT"
                            particulars = "Counter Cash Sale / Collection"
                        },
                        label = { Text("Cash In (Receipt)") }
                    )
                    ElevatedFilterChip(
                        selected = entryType == "EXPENSE",
                        onClick = {
                            entryType = "EXPENSE"
                            particulars = "Godown Labor / Mandi Porter Wages"
                        },
                        label = { Text("Cash Out (Expense)") }
                    )
                }

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("Cash Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = particulars,
                    onValueChange = { particulars = it },
                    label = { Text("Particulars / Reason") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = voucherNo,
                    onValueChange = { voucherNo = it },
                    label = { Text("Cash Voucher #") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountInput.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        onSave(entryType, amt, particulars, voucherNo)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
            ) {
                Text("Save Cash Entry")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
