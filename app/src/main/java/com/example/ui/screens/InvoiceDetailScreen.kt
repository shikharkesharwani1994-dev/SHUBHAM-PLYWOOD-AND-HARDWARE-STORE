package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import android.widget.Toast
import com.example.util.ExportUtils
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InvoiceEntity
import com.example.data.model.UserRole
import com.example.ui.components.StatusBadge
import com.example.ui.components.SyncStatusBadge
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.PrimaryBlue
import com.example.ui.viewmodel.GstViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailScreen(
    invoice: InvoiceEntity,
    viewModel: GstViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val company by viewModel.companyProfile.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var showPaymentDialog by remember { mutableStateOf(false) }
    var paymentAmountInput by remember { mutableStateOf("") }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val items = remember(invoice.itemsJson) {
        viewModel.parseItems(invoice.itemsJson)
    }

    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val invoiceDateStr = sdf.format(Date(invoice.invoiceDate))
    val dueDateStr = sdf.format(Date(invoice.dueDate))
    val pendingAmount = (invoice.totalAmount - invoice.amountPaid).coerceAtLeast(0.0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(invoice.invoiceNumber, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val pdfFile = ExportUtils.generateInvoicePdf(context, invoice, items, company)
                            if (pdfFile != null) {
                                ExportUtils.shareExportedFile(context, pdfFile, "application/pdf", "Share PDF Invoice")
                            } else {
                                Toast.makeText(context, "Failed to generate PDF", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "Export PDF", tint = PrimaryBlue)
                    }
                    IconButton(
                        onClick = {
                            val excelFile = ExportUtils.generateInvoiceExcel(context, invoice, items, company)
                            if (excelFile != null) {
                                ExportUtils.shareExportedFile(context, excelFile, "text/csv", "Share Excel Invoice")
                            } else {
                                Toast.makeText(context, "Failed to generate Excel", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(Icons.Default.TableChart, contentDescription = "Export Excel", tint = EmeraldGreen)
                    }
                    IconButton(
                        onClick = {
                            val sendIntent: Intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, viewModel.getPaymentReminderText(invoice))
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Share Invoice via")
                            context.startActivity(shareIntent)
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                    if (currentUser.role == UserRole.ADMIN) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            // WhatsApp Reminder Share Intent
                            val message = viewModel.getPaymentReminderText(invoice)
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                putExtra(Intent.EXTRA_TEXT, message)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Send WhatsApp Reminder"))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_whatsapp_reminder"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reminder")
                    }

                    if (pendingAmount > 0) {
                        Button(
                            onClick = {
                                paymentAmountInput = pendingAmount.toString()
                                showPaymentDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_record_payment"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Record Pay")
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Quick Export Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val pdfFile = ExportUtils.generateInvoicePdf(context, invoice, items, company)
                        if (pdfFile != null) {
                            ExportUtils.shareExportedFile(context, pdfFile, "application/pdf", "Share PDF Tax Invoice")
                        } else {
                            Toast.makeText(context, "Error generating PDF", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f).testTag("btn_export_pdf"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("PDF Invoice", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        val excelFile = ExportUtils.generateInvoiceExcel(context, invoice, items, company)
                        if (excelFile != null) {
                            ExportUtils.shareExportedFile(context, excelFile, "text/csv", "Share Excel Tax Invoice")
                        } else {
                            Toast.makeText(context, "Error generating Excel", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f).testTag("btn_export_excel"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.TableChart, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Excel Format", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Printable Tax Invoice Document Container
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "TAX INVOICE",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = PrimaryBlue
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = company.tradeName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "GSTIN: ${company.gstin}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = company.address,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "State: ${company.stateName} (Code: ${company.stateCode})",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            StatusBadge(status = invoice.status)
                            Spacer(modifier = Modifier.height(4.dp))
                            SyncStatusBadge(syncStatus = invoice.syncStatus)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Invoice Metadata & Bill To
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "BILL TO:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = invoice.customerName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (invoice.customerGstin.isNotBlank()) {
                                Text(
                                    text = "GSTIN: ${invoice.customerGstin}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Text(
                                    text = "Party Type: Unregistered (B2C)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (invoice.billingAddress.isNotBlank()) {
                                Text(
                                    text = invoice.billingAddress,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "Place of Supply: ${invoice.placeOfSupply}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryBlue
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Invoice No: ${invoice.invoiceNumber}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Date: $invoiceDateStr",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Due Date: $dueDateStr",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (invoice.isInterState) "Type: Inter-State (IGST)" else "Type: Intra-State (CGST+SGST)",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Line Items
                    Text(
                        text = "ITEMIZED PARTICULARS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    items.forEachIndexed { index, item ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${index + 1}. ${item.itemName}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "₹${String.format(Locale.US, "%,.2f", item.total)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "HSN: ${item.hsnCode} • Qty: ${item.quantity} ${item.unit} @ ₹${item.rate}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "GST ${item.gstRate.toInt()}% (Tax: ₹${String.format(Locale.US, "%.1f", item.cgst + item.sgst + item.igst)})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Financial Tax Calculation Summary
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        TaxSummaryRow("Taxable Subtotal", "₹${String.format(Locale.US, "%,.2f", invoice.taxableAmount)}")
                        if (invoice.isInterState) {
                            TaxSummaryRow("Integrated Tax (IGST)", "₹${String.format(Locale.US, "%,.2f", invoice.igstAmount)}")
                        } else {
                            TaxSummaryRow("Central Tax (CGST)", "₹${String.format(Locale.US, "%,.2f", invoice.cgstAmount)}")
                            TaxSummaryRow("State Tax (SGST)", "₹${String.format(Locale.US, "%,.2f", invoice.sgstAmount)}")
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Grand Total (INR)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "₹${String.format(Locale.US, "%,.2f", invoice.totalAmount)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Amount Received",
                                style = MaterialTheme.typography.bodySmall,
                                color = EmeraldGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "₹${String.format(Locale.US, "%,.2f", invoice.amountPaid)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = EmeraldGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Balance Due",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (pendingAmount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "₹${String.format(Locale.US, "%,.2f", pendingAmount)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (pendingAmount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Bank & UPI Settlement Box
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = "UPI Payment QR",
                                tint = PrimaryBlue,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Bank Settlement Details",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${company.bankName} • A/C: ${company.bankAccount}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "IFSC: ${company.ifscCode} • UPI ID: ${company.upiId}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PrimaryBlue,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    if (invoice.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Notes / Terms: ${invoice.notes}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Prepared by: ${invoice.createdBy} (${invoice.createdByRole})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }

    // Record Payment Dialog
    if (showPaymentDialog) {
        AlertDialog(
            onDismissRequest = { showPaymentDialog = false },
            title = { Text("Record Payment") },
            text = {
                Column {
                    Text(
                        text = "Outstanding balance: ₹${String.format(Locale.US, "%,.2f", pendingAmount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = paymentAmountInput,
                        onValueChange = { paymentAmountInput = it },
                        label = { Text("Payment Amount (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_record_payment")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = paymentAmountInput.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            viewModel.recordPayment(invoice.invoiceNumber, amount)
                            showPaymentDialog = false
                        }
                    }
                ) {
                    Text("Confirm Payment")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Invoice?") },
            text = {
                Text("Are you sure you want to delete invoice ${invoice.invoiceNumber}? This action will update the cloud ledger and cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteInvoice(invoice)
                        showDeleteConfirm = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TaxSummaryRow(label: String, value: String) {
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
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
