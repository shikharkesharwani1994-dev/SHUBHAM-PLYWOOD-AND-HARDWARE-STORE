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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InvoiceItem
import com.example.ui.components.CameraScannerDialog
import com.example.ui.components.ScannerMode
import com.example.ui.components.ScannedBarcodeItem
import com.example.ui.components.ScannedInvoiceData
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.PrimaryBlue
import com.example.ui.viewmodel.GstViewModel
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInvoiceScreen(
    viewModel: GstViewModel,
    onBack: () -> Unit
) {
    val customers by viewModel.customers.collectAsState()
    val inventory by viewModel.inventory.collectAsState()
    val company by viewModel.companyProfile.collectAsState()

    var invoiceNumber by remember {
        mutableStateOf("INV-2024-00" + (13..99).random())
    }
    var customerName by remember { mutableStateOf("") }
    var customerGstin by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var customerEmail by remember { mutableStateOf("") }
    var billingAddress by remember { mutableStateOf("") }
    var placeOfSupply by remember { mutableStateOf("09-Uttar Pradesh") }
    var paymentTermsDays by remember { mutableStateOf("15") }
    var notes by remember { mutableStateOf("Thank you for your business. Please quote invoice # in payment transfer.") }
    var showScannerDialog by remember { mutableStateOf(false) }
    var scannerMode by remember { mutableStateOf(ScannerMode.BARCODE) }

    val items = remember {
        mutableStateListOf(
            InvoiceItem(
                id = UUID.randomUUID().toString(),
                itemName = "Fortune Premium Chakki Fresh Atta (50kg Bag)",
                hsnCode = "11010000",
                quantity = 25.0,
                unit = "BAG",
                rate = 1680.0,
                discountPercent = 0.0,
                gstRate = 5.0
            )
        )
    }

    val indianStates = listOf(
        "09-Uttar Pradesh",
        "10-Bihar",
        "27-Maharashtra",
        "07-Delhi",
        "29-Karnataka",
        "24-Gujarat",
        "33-Tamil Nadu",
        "19-West Bengal",
        "06-Haryana",
        "36-Telangana",
        "08-Rajasthan"
    )

    var stateExpanded by remember { mutableStateOf(false) }
    var customerDropdownExpanded by remember { mutableStateOf(false) }

    val isInterState by remember {
        derivedStateOf {
            !placeOfSupply.startsWith(company.stateCode)
        }
    }

    // Dynamic calculations
    val (subtotal, totalTax, grandTotal) = remember(items.toList(), isInterState) {
        var sub = 0.0
        var tax = 0.0
        for (it in items) {
            val gross = it.quantity * it.rate
            val disc = gross * (it.discountPercent / 100.0)
            val taxable = (gross - disc).coerceAtLeast(0.0)
            val itTax = taxable * (it.gstRate / 100.0)
            sub += taxable
            tax += itTax
        }
        Triple(sub, tax, sub + tax)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Generate GST Invoice", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Grand Total (incl. GST)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "₹${String.format(Locale.US, "%,.2f", grandTotal)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    }

                    Button(
                        onClick = {
                            if (customerName.isNotBlank() && items.isNotEmpty()) {
                                val terms = paymentTermsDays.toLongOrNull() ?: 15L
                                val dueDate = System.currentTimeMillis() + (terms * 86400000L)
                                viewModel.saveInvoice(
                                    invoiceNumber = invoiceNumber,
                                    customerName = customerName,
                                    customerGstin = customerGstin,
                                    customerPhone = customerPhone,
                                    customerEmail = customerEmail,
                                    billingAddress = billingAddress,
                                    placeOfSupply = placeOfSupply,
                                    dueDate = dueDate,
                                    items = items.toList(),
                                    notes = notes
                                )
                                onBack()
                            }
                        },
                        enabled = customerName.isNotBlank() && items.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("btn_save_invoice")
                    ) {
                        Text("Create & Sync")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Invoice Metadata Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Invoice Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = invoiceNumber,
                            onValueChange = { invoiceNumber = it },
                            label = { Text("Invoice #") },
                            modifier = Modifier.weight(1f).testTag("input_invoice_number"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = paymentTermsDays,
                            onValueChange = { paymentTermsDays = it },
                            label = { Text("Credit Days") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("input_credit_days"),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Place of Supply / State Selector
                    ExposedDropdownMenuBox(
                        expanded = stateExpanded,
                        onExpandedChange = { stateExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = placeOfSupply,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Place of Supply (State)") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = stateExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .testTag("dropdown_place_of_supply")
                        )
                        ExposedDropdownMenu(
                            expanded = stateExpanded,
                            onDismissRequest = { stateExpanded = false }
                        ) {
                            indianStates.forEach { state ->
                                DropdownMenuItem(
                                    text = { Text(state) },
                                    onClick = {
                                        placeOfSupply = state
                                        stateExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = if (isInterState) Color(0xFFEFF6FF) else Color(0xFFECFDF5),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isInterState) {
                                "⚡ Inter-State Supply: 100% IGST applied"
                            } else {
                                "⚡ Intra-State Supply: 50% CGST + 50% SGST applied"
                            },
                            color = if (isInterState) PrimaryBlue else EmeraldGreen,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Customer Selector / Input Card
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
                        Text(
                            text = "Customer / Party Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (customers.isNotEmpty()) {
                            Text(
                                text = "Select Saved",
                                color = PrimaryBlue,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { customerDropdownExpanded = true }
                            )
                        }
                    }

                    if (customerDropdownExpanded) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                customers.forEach { cust ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                customerName = cust.name
                                                customerGstin = cust.gstin
                                                customerPhone = cust.phone
                                                customerEmail = cust.email
                                                billingAddress = cust.address
                                                // auto-detect place of supply
                                                val state = indianStates.find { it.startsWith(cust.stateCode) }
                                                if (state != null) placeOfSupply = state
                                                customerDropdownExpanded = false
                                            }
                                            .padding(vertical = 6.dp, horizontal = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(cust.name, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                                        Text(cust.gstin.ifBlank { "B2C" }, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Customer / Firm Name *") },
                        modifier = Modifier.fillMaxWidth().testTag("input_customer_name"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customerGstin,
                        onValueChange = {
                            customerGstin = it
                            if (it.length >= 2) {
                                val statePrefix = it.take(2)
                                val matched = indianStates.find { s -> s.startsWith(statePrefix) }
                                if (matched != null) placeOfSupply = matched
                            }
                        },
                        label = { Text("GSTIN (Leave blank for unregistered B2C)") },
                        modifier = Modifier.fillMaxWidth().testTag("input_customer_gstin"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = customerPhone,
                            onValueChange = { customerPhone = it },
                            label = { Text("Phone Number") },
                            modifier = Modifier.weight(1f).testTag("input_customer_phone"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = customerEmail,
                            onValueChange = { customerEmail = it },
                            label = { Text("Email Address") },
                            modifier = Modifier.weight(1f).testTag("input_customer_email"),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = billingAddress,
                        onValueChange = { billingAddress = it },
                        label = { Text("Billing Address") },
                        modifier = Modifier.fillMaxWidth().testTag("input_customer_address"),
                        maxLines = 2
                    )
                }
            }

            // Line Items Section
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
                        Text(
                            text = "Line Items (${items.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        OutlinedButton(
                            onClick = {
                                items.add(
                                    InvoiceItem(
                                        id = UUID.randomUUID().toString(),
                                        itemName = "Custom Service / Goods",
                                        hsnCode = "998314",
                                        quantity = 1.0,
                                        unit = "NOS",
                                        rate = 1000.0,
                                        discountPercent = 0.0,
                                        gstRate = 18.0
                                    )
                                )
                            },
                            modifier = Modifier.testTag("btn_add_item")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Item")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    items.forEachIndexed { index, item ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Item #${index + 1}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryBlue
                                    )
                                    if (items.size > 1) {
                                        IconButton(onClick = { items.removeAt(index) }) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Remove Item",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = item.itemName,
                                    onValueChange = { updated ->
                                        items[index] = item.copy(itemName = updated)
                                    },
                                    label = { Text("Item Name") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = item.hsnCode,
                                        onValueChange = { updated ->
                                            items[index] = item.copy(hsnCode = updated)
                                        },
                                        label = { Text("HSN/SAC") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = item.quantity.toString(),
                                        onValueChange = { updated ->
                                            items[index] = item.copy(quantity = updated.toDoubleOrNull() ?: 1.0)
                                        },
                                        label = { Text("Qty") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.weight(0.8f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = item.rate.toString(),
                                        onValueChange = { updated ->
                                            items[index] = item.copy(rate = updated.toDoubleOrNull() ?: 0.0)
                                        },
                                        label = { Text("Price (₹)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.weight(1.2f),
                                        singleLine = true
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = item.discountPercent.toString(),
                                        onValueChange = { updated ->
                                            items[index] = item.copy(discountPercent = updated.toDoubleOrNull() ?: 0.0)
                                        },
                                        label = { Text("Disc %") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = item.gstRate.toInt().toString(),
                                        onValueChange = { updated ->
                                            items[index] = item.copy(gstRate = updated.toDoubleOrNull() ?: 18.0)
                                        },
                                        label = { Text("GST % (0,5,12,18,28)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1.5f),
                                        singleLine = true
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Calculation Summary Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Tax Computation Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TaxSummaryRow("Taxable Amount:", "₹${String.format(Locale.US, "%,.2f", subtotal)}")
                    if (isInterState) {
                        TaxSummaryRow("Integrated Tax (IGST):", "₹${String.format(Locale.US, "%,.2f", totalTax)}")
                    } else {
                        TaxSummaryRow("Central Tax (CGST 50%):", "₹${String.format(Locale.US, "%,.2f", totalTax / 2.0)}")
                        TaxSummaryRow("State Tax (SGST 50%):", "₹${String.format(Locale.US, "%,.2f", totalTax / 2.0)}")
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                    TaxSummaryRow("Grand Total (INR):", "₹${String.format(Locale.US, "%,.2f", grandTotal)}")
                }
            }

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Terms & Conditions / Bank Narration") },
                modifier = Modifier.fillMaxWidth().testTag("input_invoice_notes"),
                maxLines = 3
            )
        }
    }
}
