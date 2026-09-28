package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CustomerEntity
import com.example.data.model.InvoiceEntity
import com.example.data.model.InwardSupplyEntity
import com.example.data.model.LedgerEntryEntity
import com.example.data.model.SupplierEntity
import com.example.ui.components.StatusBadge
import com.example.ui.components.SyncStatusBadge
import com.example.ui.theme.AmberGold
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.PrimaryBlue
import com.example.ui.viewmodel.GstViewModel
import com.example.util.ExportUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoicesScreen(
    viewModel: GstViewModel,
    onNavigateToCreateInvoice: () -> Unit,
    onSelectInvoice: (InvoiceEntity) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Outward (Sales)", "Inward (Purchases)", "Customer Ledger", "Supplier Ledger")

    val invoices by viewModel.filteredInvoices.collectAsState()
    val rawInvoices by viewModel.rawInvoices.collectAsState()
    val inwardBills by viewModel.inwardSupplies.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val suppliers by viewModel.suppliers.collectAsState()
    val ledgerEntries by viewModel.ledgerEntries.collectAsState()
    val company by viewModel.companyProfile.collectAsState()

    val searchQuery by viewModel.invoiceSearchQuery.collectAsState()
    val activeFilter by viewModel.invoiceStatusFilter.collectAsState()
    val filterOptions = listOf("ALL", "PAID", "PENDING", "OVERDUE", "PARTIAL")

    // Inward Supply Dialog state
    var showInwardDialog by remember { mutableStateOf(false) }

    // Supplier Payment Dialog state
    var selectedInwardBillForPayment by remember { mutableStateOf<InwardSupplyEntity?>(null) }
    var supplierPayAmount by remember { mutableStateOf("") }
    var supplierPayMode by remember { mutableStateOf("BANK") }

    // Selected customer / supplier for ledger view
    var selectedCustomerForLedger by remember { mutableStateOf<CustomerEntity?>(null) }
    var selectedSupplierForLedger by remember { mutableStateOf<SupplierEntity?>(null) }

    // Record receipt from customer dialog
    var showCustomerReceiptDialog by remember { mutableStateOf(false) }
    var customerReceiptAmount by remember { mutableStateOf("") }
    var customerReceiptMode by remember { mutableStateOf("BANK_UPI") }

    Scaffold(
        floatingActionButton = {
            when (selectedTab) {
                0 -> {
                    FloatingActionButton(
                        onClick = onNavigateToCreateInvoice,
                        containerColor = PrimaryBlue,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("fab_create_invoice")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Create Outward Invoice")
                    }
                }
                1 -> {
                    FloatingActionButton(
                        onClick = { showInwardDialog = true },
                        containerColor = EmeraldGreen,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("fab_add_inward_supply")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Inward Supply Bill")
                    }
                }
                else -> {}
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Top Primary Tabs
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
                    // TAB 0: OUTWARD SUPPLY (SALES INVOICES)
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Search Input
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.invoiceSearchQuery.value = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .testTag("invoice_search_field"),
                            placeholder = { Text("Search by invoice #, customer or GSTIN...") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.invoiceSearchQuery.value = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Status Filter Chips & Register Export
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                items(filterOptions) { filter ->
                                    val isSelected = activeFilter.equals(filter, ignoreCase = true)
                                    ElevatedFilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.invoiceStatusFilter.value = filter },
                                        label = { Text(filter, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                        modifier = Modifier.testTag("filter_$filter")
                                    )
                                }
                            }

                            // Quick Excel Register Export Button
                            IconButton(
                                onClick = {
                                    val excelFile = ExportUtils.generateLedgerExcel(
                                        context = context,
                                        accountName = "All_Outward_Supplies",
                                        accountType = "SALES_REGISTER",
                                        entries = rawInvoices.map { inv ->
                                            LedgerEntryEntity(
                                                date = inv.invoiceDate,
                                                accountType = "CUSTOMER",
                                                accountId = inv.customerGstin.ifBlank { "B2C" },
                                                accountName = inv.customerName,
                                                voucherType = "SALES",
                                                voucherNumber = inv.invoiceNumber,
                                                debit = inv.totalAmount,
                                                credit = 0.0,
                                                particulars = "Outward Supply (Taxable: ₹${inv.taxableAmount})",
                                                paymentMethod = "CREDIT",
                                                balanceAfter = inv.totalAmount - inv.amountPaid
                                            )
                                        },
                                        company = company
                                    )
                                    if (excelFile != null) {
                                        ExportUtils.shareExportedFile(context, excelFile, "text/csv", "Share Sales Register (Excel)")
                                    }
                                }
                            ) {
                                Icon(Icons.Default.TableChart, contentDescription = "Export All to Excel", tint = EmeraldGreen)
                            }
                        }

                        // Invoices List
                        if (invoices.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.ReceiptLong,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "No outward invoices found",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Tap + to issue a new GST sales bill.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("invoices_list"),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(invoices, key = { it.invoiceNumber }) { invoice ->
                                    OutwardInvoiceCard(
                                        invoice = invoice,
                                        onClick = { onSelectInvoice(invoice) },
                                        onExportPdf = {
                                            val items = viewModel.parseItems(invoice.itemsJson)
                                            val file = ExportUtils.generateInvoicePdf(context, invoice, items, company)
                                            if (file != null) {
                                                ExportUtils.shareExportedFile(context, file, "application/pdf", "Share PDF Invoice")
                                            } else {
                                                Toast.makeText(context, "Error generating PDF", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        onExportExcel = {
                                            val items = viewModel.parseItems(invoice.itemsJson)
                                            val file = ExportUtils.generateInvoiceExcel(context, invoice, items, company)
                                            if (file != null) {
                                                ExportUtils.shareExportedFile(context, file, "text/csv", "Share Excel Invoice")
                                            } else {
                                                Toast.makeText(context, "Error generating Excel", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: INWARD SUPPLY (PURCHASES / SUPPLIER BILLS)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Summary Card
                        val totalInward = inwardBills.sumOf { it.totalAmount }
                        val totalItc = inwardBills.sumOf { it.cgstAmount + it.sgstAmount + it.igstAmount }
                        val totalPaid = inwardBills.sumOf { it.amountPaid }
                        val totalPayable = totalInward - totalPaid

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
                                        Text("Total Inward Supplies", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                                        Text("₹ %,.2f".format(Locale.US, totalInward), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Eligible ITC Credit", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                                        Text("₹ %,.2f".format(Locale.US, totalItc), color = EmeraldGreen, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Outstanding Supplier Due: ₹ %,.2f".format(Locale.US, totalPayable), color = AmberGold, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Button(
                                        onClick = { showInwardDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add Inward Bill", color = PrimaryBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text("Inward Purchase Vouchers (Suppliers)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        if (inwardBills.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No inward supply bills found. Tap + to record purchases.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(inwardBills) { bill ->
                                    InwardBillCard(
                                        bill = bill,
                                        onRecordPay = {
                                            selectedInwardBillForPayment = bill
                                            supplierPayAmount = (bill.totalAmount - bill.amountPaid).coerceAtLeast(0.0).toString()
                                        },
                                        onExportBill = {
                                            // Export Bill as Excel
                                            val dummyInv = InvoiceEntity(
                                                invoiceNumber = bill.billNumber,
                                                customerName = bill.supplierName,
                                                customerGstin = bill.supplierGstin,
                                                customerPhone = "",
                                                customerEmail = "",
                                                billingAddress = "Supplier Godown / Mill",
                                                placeOfSupply = if (bill.isInterState) "Inter-State" else company.stateCode,
                                                invoiceDate = bill.billDate,
                                                dueDate = bill.dueDate,
                                                taxableAmount = bill.taxableAmount,
                                                cgstAmount = bill.cgstAmount,
                                                sgstAmount = bill.sgstAmount,
                                                igstAmount = bill.igstAmount,
                                                totalAmount = bill.totalAmount,
                                                amountPaid = bill.amountPaid,
                                                status = bill.status,
                                                isInterState = bill.isInterState,
                                                notes = bill.notes
                                            )
                                            val file = ExportUtils.generateInvoiceExcel(context, dummyInv, emptyList(), company)
                                            if (file != null) {
                                                ExportUtils.shareExportedFile(context, file, "text/csv", "Share Inward Bill (Excel)")
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 2: CUSTOMER LEDGER (STATEMENT OF ACCOUNT)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        if (selectedCustomerForLedger == null) {
                            Text("Select Customer to View Ledger", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(customers) { cust ->
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedCustomerForLedger = cust }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(cust.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                Text("GSTIN: ${cust.gstin.ifBlank { "Unregistered Retailer" }}", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text(cust.address.take(40), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text("Due Balance", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text("₹ %,.2f".format(Locale.US, cust.outstandingAmount), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (cust.outstandingAmount > 0) Color(0xFFE11D48) else EmeraldGreen)
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            val cust = selectedCustomerForLedger!!
                            val entries = ledgerEntries.filter { it.accountType == "CUSTOMER" && (it.accountId == cust.id || it.accountName == cust.name) }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = { selectedCustomerForLedger = null }) {
                                    Text("← All Customers")
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    IconButton(onClick = {
                                        val file = ExportUtils.generateLedgerPdf(context, cust.name, "CUSTOMER", entries, company)
                                        if (file != null) {
                                            ExportUtils.shareExportedFile(context, file, "application/pdf", "Customer Ledger PDF")
                                        }
                                    }) {
                                        Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF Ledger", tint = PrimaryBlue)
                                    }
                                    IconButton(onClick = {
                                        val file = ExportUtils.generateLedgerExcel(context, cust.name, "CUSTOMER", entries, company)
                                        if (file != null) {
                                            ExportUtils.shareExportedFile(context, file, "text/csv", "Customer Ledger Excel")
                                        }
                                    }) {
                                        Icon(Icons.Default.TableChart, contentDescription = "Excel Ledger", tint = EmeraldGreen)
                                    }
                                }
                            }

                            // Customer Summary Card
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(cust.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("Company: ${cust.companyName} | GSTIN: ${cust.gstin.ifBlank { "Unregistered" }}", fontSize = 11.5.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Current Due: ₹ %,.2f".format(Locale.US, cust.outstandingAmount), fontWeight = FontWeight.Bold, color = if (cust.outstandingAmount > 0) Color(0xFFE11D48) else EmeraldGreen)
                                        Text("Credit Limit: ₹ %,.2f".format(Locale.US, cust.creditLimit), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Transaction History (Dr / Cr)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(6.dp))

                            if (entries.isEmpty()) {
                                Text("No ledger entries found for this customer.", modifier = Modifier.padding(16.dp))
                            } else {
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(entries) { entry ->
                                        LedgerEntryRow(entry = entry)
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // TAB 3: SUPPLIER LEDGER (STATEMENT OF ACCOUNT)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        if (selectedSupplierForLedger == null) {
                            Text("Select FMCG Supplier to View Ledger", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(suppliers) { sup ->
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedSupplierForLedger = sup }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(sup.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                Text("GSTIN: ${sup.gstin}", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text(sup.address.take(40), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text("Payable Due", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text("₹ %,.2f".format(Locale.US, sup.payableBalance), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AmberGold)
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            val sup = selectedSupplierForLedger!!
                            val entries = ledgerEntries.filter { it.accountType == "SUPPLIER" && (it.accountId == sup.id || it.accountName.contains(sup.name.take(6))) }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = { selectedSupplierForLedger = null }) {
                                    Text("← All Suppliers")
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    IconButton(onClick = {
                                        val file = ExportUtils.generateLedgerPdf(context, sup.name, "SUPPLIER", entries, company)
                                        if (file != null) {
                                            ExportUtils.shareExportedFile(context, file, "application/pdf", "Supplier Ledger PDF")
                                        }
                                    }) {
                                        Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF Ledger", tint = PrimaryBlue)
                                    }
                                    IconButton(onClick = {
                                        val file = ExportUtils.generateLedgerExcel(context, sup.name, "SUPPLIER", entries, company)
                                        if (file != null) {
                                            ExportUtils.shareExportedFile(context, file, "text/csv", "Supplier Ledger Excel")
                                        }
                                    }) {
                                        Icon(Icons.Default.TableChart, contentDescription = "Excel Ledger", tint = EmeraldGreen)
                                    }
                                }
                            }

                            // Supplier Summary Card
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(sup.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("Company: ${sup.companyName} | GSTIN: ${sup.gstin}", fontSize = 11.5.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Outstanding Payable: ₹ %,.2f".format(Locale.US, sup.payableBalance), fontWeight = FontWeight.Bold, color = AmberGold)
                                        Text("Credit Period: ${sup.creditPeriodDays} Days", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Supplier Purchases & Payment Ledger (Dr / Cr)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(6.dp))

                            if (entries.isEmpty()) {
                                Text("No ledger entries found for this supplier.", modifier = Modifier.padding(16.dp))
                            } else {
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(entries) { entry ->
                                        LedgerEntryRow(entry = entry)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // DIALOG: Record New Inward Supply Bill
    if (showInwardDialog) {
        AddInwardSupplyDialog(
            suppliers = suppliers,
            onDismiss = { showInwardDialog = false },
            onSave = { billNo, sup, taxable, cgst, sgst, igst, total, paid, mode, interState, notes ->
                viewModel.saveInwardSupply(
                    billNumber = billNo,
                    supplier = sup,
                    taxableAmount = taxable,
                    cgst = cgst,
                    sgst = sgst,
                    igst = igst,
                    totalAmount = total,
                    amountPaid = paid,
                    paymentMode = mode,
                    isInterState = interState,
                    notes = notes
                )
                showInwardDialog = false
            }
        )
    }

    // DIALOG: Supplier Payment
    if (selectedInwardBillForPayment != null) {
        val bill = selectedInwardBillForPayment!!
        AlertDialog(
            onDismissRequest = { selectedInwardBillForPayment = null },
            title = { Text("Record Payment to Supplier") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Supplier: ${bill.supplierName}", fontWeight = FontWeight.Bold)
                    Text("Bill No: ${bill.billNumber} | Total: ₹${bill.totalAmount}")
                    OutlinedTextField(
                        value = supplierPayAmount,
                        onValueChange = { supplierPayAmount = it },
                        label = { Text("Payment Amount (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ElevatedFilterChip(
                            selected = supplierPayMode == "BANK",
                            onClick = { supplierPayMode = "BANK" },
                            label = { Text("SBI Bank (RTGS/UPI)") }
                        )
                        ElevatedFilterChip(
                            selected = supplierPayMode == "CASH",
                            onClick = { supplierPayMode = "CASH" },
                            label = { Text("Cash Counter") }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = supplierPayAmount.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            viewModel.recordSupplierPayment(bill.billNumber, amt, supplierPayMode)
                            selectedInwardBillForPayment = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                ) {
                    Text("Confirm Payout")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedInwardBillForPayment = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun OutwardInvoiceCard(
    invoice: InvoiceEntity,
    onClick: () -> Unit,
    onExportPdf: () -> Unit,
    onExportExcel: () -> Unit
) {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val dateStr = sdf.format(Date(invoice.invoiceDate))

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("invoice_card_${invoice.invoiceNumber}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(invoice.invoiceNumber, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    SyncStatusBadge(syncStatus = invoice.syncStatus)
                }
                StatusBadge(status = invoice.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = invoice.customerName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (invoice.customerGstin.isNotBlank()) {
                Text(
                    text = "GSTIN: ${invoice.customerGstin}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Date: $dateStr", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    val taxType = if (invoice.isInterState) "IGST: ₹${String.format(Locale.US, "%.1f", invoice.igstAmount)}" else "CGST+SGST: ₹${String.format(Locale.US, "%.1f", invoice.cgstAmount + invoice.sgstAmount)}"
                    Text(text = taxType, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "₹ %,.2f".format(Locale.US, invoice.totalAmount),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryBlue
                        )
                        if (invoice.status != "PAID") {
                            val balance = invoice.totalAmount - invoice.amountPaid
                            Text(
                                text = "Due: ₹ %,.2f".format(Locale.US, balance),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE11D48)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(onClick = onExportPdf) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF", tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onExportExcel) {
                        Icon(Icons.Default.TableChart, contentDescription = "Excel", tint = EmeraldGreen, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun InwardBillCard(
    bill: InwardSupplyEntity,
    onRecordPay: () -> Unit,
    onExportBill: () -> Unit
) {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val dateStr = sdf.format(Date(bill.billDate))
    val pending = (bill.totalAmount - bill.amountPaid).coerceAtLeast(0.0)

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(bill.billNumber, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ITC ELIGIBLE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGreen,
                        modifier = Modifier
                            .background(EmeraldGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                StatusBadge(status = bill.status)
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(bill.supplierName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text("GSTIN: ${bill.supplierGstin} | Mode: ${bill.paymentMode}", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Date: $dateStr", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    val itcText = if (bill.isInterState) "IGST ITC: ₹${String.format(Locale.US, "%.1f", bill.igstAmount)}" else "CGST+SGST ITC: ₹${String.format(Locale.US, "%.1f", bill.cgstAmount + bill.sgstAmount)}"
                    Text(itcText, fontSize = 11.sp, color = EmeraldGreen, fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("₹ %,.2f".format(Locale.US, bill.totalAmount), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = PrimaryBlue)
                    if (pending > 0) {
                        Text("Due: ₹ %,.2f".format(Locale.US, pending), fontSize = 11.sp, color = AmberGold, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onExportBill,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.TableChart, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Excel", fontSize = 11.sp)
                }
                if (pending > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onRecordPay,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pay Supplier", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun LedgerEntryRow(entry: LedgerEntryEntity) {
    val sdf = SimpleDateFormat("dd/MM/yy HH:mm", Locale.US)
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                    val isDr = entry.debit > 0
                    Icon(
                        imageVector = if (isDr) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = if (isDr) Color(0xFFE11D48) else EmeraldGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${entry.voucherType} #${entry.voucherNumber}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(sdf.format(Date(entry.date)), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                Text("Bal: ₹ %,.2f".format(Locale.US, entry.balanceAfter), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInwardSupplyDialog(
    suppliers: List<SupplierEntity>,
    onDismiss: () -> Unit,
    onSave: (
        billNo: String,
        supplier: SupplierEntity,
        taxable: Double,
        cgst: Double,
        sgst: Double,
        igst: Double,
        total: Double,
        paid: Double,
        mode: String,
        interState: Boolean,
        notes: String
    ) -> Unit
) {
    var billNumber by remember { mutableStateOf("PUR-${System.currentTimeMillis() % 10000}") }
    var selectedSupplierIndex by remember { mutableIntStateOf(0) }
    var taxableInput by remember { mutableStateOf("50000") }
    var gstRateInput by remember { mutableStateOf("5") } // 5%, 12%, 18%
    var paidInput by remember { mutableStateOf("0") }
    var paymentMode by remember { mutableStateOf("BANK") }
    var isInterState by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("FMCG goods delivery") }

    val currentSupplier = suppliers.getOrNull(selectedSupplierIndex)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Inward Supply (Purchase)") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = billNumber,
                    onValueChange = { billNumber = it },
                    label = { Text("Bill / Purchase Voucher #") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (suppliers.isNotEmpty()) {
                    Text("Select Supplier:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(suppliers.size) { idx ->
                            ElevatedFilterChip(
                                selected = selectedSupplierIndex == idx,
                                onClick = {
                                    selectedSupplierIndex = idx
                                    isInterState = suppliers[idx].stateCode != "09"
                                },
                                label = { Text(suppliers[idx].name.take(18)) }
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = taxableInput,
                        onValueChange = { taxableInput = it },
                        label = { Text("Taxable (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = gstRateInput,
                        onValueChange = { gstRateInput = it },
                        label = { Text("GST %") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = paidInput,
                        onValueChange = { paidInput = it },
                        label = { Text("Amount Paid Now") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = paymentMode,
                        onValueChange = { paymentMode = it },
                        label = { Text("Mode (BANK/CASH)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Goods / Mill notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (currentSupplier != null) {
                        val taxable = taxableInput.toDoubleOrNull() ?: 0.0
                        val rate = gstRateInput.toDoubleOrNull() ?: 5.0
                        val tax = taxable * (rate / 100.0)
                        val cgst = if (isInterState) 0.0 else tax / 2.0
                        val sgst = if (isInterState) 0.0 else tax / 2.0
                        val igst = if (isInterState) tax else 0.0
                        val total = taxable + tax
                        val paid = paidInput.toDoubleOrNull() ?: 0.0
                        onSave(billNumber, currentSupplier, taxable, cgst, sgst, igst, total, paid, paymentMode, isInterState, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
            ) {
                Text("Save Inward Bill")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
