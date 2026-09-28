package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InventoryItemEntity
import com.example.ui.theme.CoralRed
import com.example.ui.theme.CoralRedContainer
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.PrimaryBlue
import com.example.ui.viewmodel.GstViewModel
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: GstViewModel,
    onBack: () -> Unit
) {
    val inventory by viewModel.inventory.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    // Dialog state
    var itemName by remember { mutableStateOf("") }
    var itemSku by remember { mutableStateOf("") }
    var itemHsn by remember { mutableStateOf("") }
    var itemCategory by remember { mutableStateOf("Hardware") }
    var itemUnit by remember { mutableStateOf("PCS") }
    var itemPrice by remember { mutableStateOf("") }
    var itemPurchasePrice by remember { mutableStateOf("") }
    var itemGstRate by remember { mutableStateOf("18") }
    var itemStock by remember { mutableStateOf("") }
    var itemReorder by remember { mutableStateOf("5") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventory & Catalog", fontWeight = FontWeight.Bold) },
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_inventory")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(inventory, key = { it.id }) { item ->
                    val isLowStock = item.stockQty <= item.reorderLevel

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("inventory_item_${item.id}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "SKU: ${item.sku} • HSN: ${item.hsnCode} • ${item.category}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (isLowStock) {
                                    Surface(
                                        color = CoralRedContainer,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Warning,
                                                contentDescription = "Low Stock",
                                                tint = CoralRed,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Low Stock",
                                                color = CoralRed,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Selling Price: ₹${String.format(Locale.US, "%,.2f", item.sellingPrice)} (+${item.gstRate.toInt()}% GST)",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = PrimaryBlue
                                    )
                                    Text(
                                        text = "Cost: ₹${String.format(Locale.US, "%,.2f", item.purchasePrice)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Quick stock decrement
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.clickable {
                                            if (item.stockQty > 0) {
                                                viewModel.saveInventoryItem(item.copy(stockQty = item.stockQty - 1))
                                            }
                                        }
                                    ) {
                                        Text("-", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), fontWeight = FontWeight.Bold)
                                    }

                                    Text(
                                        text = "${item.stockQty.toInt()} ${item.unit}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isLowStock) CoralRed else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 10.dp)
                                    )

                                    // Quick stock increment
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.clickable {
                                            viewModel.saveInventoryItem(item.copy(stockQty = item.stockQty + 1))
                                        }
                                    ) {
                                        Text("+", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Catalog Product / Service") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = itemName,
                        onValueChange = { itemName = it },
                        label = { Text("Product / Service Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_new_prod_name")
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = itemSku,
                            onValueChange = { itemSku = it },
                            label = { Text("SKU") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = itemHsn,
                            onValueChange = { itemHsn = it },
                            label = { Text("HSN/SAC *") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = itemPrice,
                            onValueChange = { itemPrice = it },
                            label = { Text("Selling (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = itemGstRate,
                            onValueChange = { itemGstRate = it },
                            label = { Text("GST %") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = itemStock,
                            onValueChange = { itemStock = it },
                            label = { Text("Initial Stock") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = itemUnit,
                            onValueChange = { itemUnit = it },
                            label = { Text("Unit (PCS/BOX)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (itemName.isNotBlank() && itemPrice.isNotBlank()) {
                            viewModel.saveInventoryItem(
                                InventoryItemEntity(
                                    id = "PROD-" + UUID.randomUUID().toString().take(6).uppercase(),
                                    name = itemName,
                                    sku = itemSku.ifBlank { "SKU-" + (100..999).random() },
                                    hsnCode = itemHsn.ifBlank { "84713010" },
                                    category = itemCategory,
                                    unit = itemUnit.ifBlank { "PCS" },
                                    purchasePrice = itemPurchasePrice.toDoubleOrNull() ?: (itemPrice.toDoubleOrNull() ?: 0.0) * 0.75,
                                    sellingPrice = itemPrice.toDoubleOrNull() ?: 0.0,
                                    gstRate = itemGstRate.toDoubleOrNull() ?: 18.0,
                                    stockQty = itemStock.toDoubleOrNull() ?: 10.0,
                                    reorderLevel = itemReorder.toDoubleOrNull() ?: 5.0
                                )
                            )
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Save Item")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
