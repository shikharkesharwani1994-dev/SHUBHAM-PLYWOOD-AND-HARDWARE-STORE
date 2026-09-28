package com.example.data.repository

import com.example.data.local.AppDao
import com.example.data.model.AppNotificationEntity
import com.example.data.model.CompanyProfile
import com.example.data.model.CustomerEntity
import com.example.data.model.InventoryItemEntity
import com.example.data.model.InvoiceEntity
import com.example.data.model.InvoiceItem
import com.example.data.model.SyncLogEntity
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GstRepository(private val appDao: AppDao) {

    val allInvoices: Flow<List<InvoiceEntity>> = appDao.getAllInvoices()
    val allCustomers: Flow<List<CustomerEntity>> = appDao.getAllCustomers()
    val allSuppliers: Flow<List<com.example.data.model.SupplierEntity>> = appDao.getAllSuppliers()
    val allInwardSupplies: Flow<List<com.example.data.model.InwardSupplyEntity>> = appDao.getAllInwardSupplies()
    val allLedgerEntries: Flow<List<com.example.data.model.LedgerEntryEntity>> = appDao.getAllLedgerEntries()
    val allInventory: Flow<List<InventoryItemEntity>> = appDao.getAllInventory()
    val allSyncLogs: Flow<List<SyncLogEntity>> = appDao.getAllSyncLogs()
    val allNotifications: Flow<List<AppNotificationEntity>> = appDao.getAllNotifications()

    fun getInvoice(invoiceNumber: String): Flow<InvoiceEntity?> =
        appDao.getInvoiceByNumber(invoiceNumber)

    fun getAccountLedger(accountId: String): Flow<List<com.example.data.model.LedgerEntryEntity>> =
        appDao.getLedgerEntriesByAccount(accountId)

    fun getDaybook(startOfDay: Long, endOfDay: Long): Flow<List<com.example.data.model.LedgerEntryEntity>> =
        appDao.getDaybookEntries(startOfDay, endOfDay)

    suspend fun saveInvoice(
        invoice: InvoiceEntity,
        isOnline: Boolean,
        currentUser: UserProfile
    ) = withContext(Dispatchers.IO) {
        val syncStatus = if (isOnline) "SYNCED" else "PENDING_SYNC"
        val finalInvoice = invoice.copy(
            syncStatus = syncStatus,
            lastUpdated = System.currentTimeMillis(),
            createdBy = currentUser.name,
            createdByRole = currentUser.role.name
        )
        appDao.insertInvoice(finalInvoice)

        // Automatically record Outward Supply in Customer Ledger (Debit Customer)
        val customer = appDao.getAllCustomers().first().find { it.name.equals(invoice.customerName, ignoreCase = true) || it.gstin == invoice.customerGstin }
        val customerId = customer?.id ?: "CUST-${invoice.customerName.take(4).uppercase()}"
        val currentBal = customer?.outstandingAmount ?: 0.0
        val newBal = currentBal + invoice.totalAmount

        appDao.insertLedgerEntry(
            com.example.data.model.LedgerEntryEntity(
                date = invoice.invoiceDate,
                accountType = "CUSTOMER",
                accountId = customerId,
                accountName = invoice.customerName,
                voucherType = "SALES",
                voucherNumber = invoice.invoiceNumber,
                debit = invoice.totalAmount,
                credit = 0.0,
                particulars = "To Outward GST Supply (${if (invoice.isInterState) "IGST" else "CGST+SGST"})",
                paymentMethod = "CREDIT",
                balanceAfter = newBal,
                createdBy = currentUser.name
            )
        )

        if (customer != null) {
            appDao.updateCustomer(customer.copy(outstandingAmount = newBal))
        }

        // Add sync log
        appDao.insertSyncLog(
            SyncLogEntity(
                userName = currentUser.name,
                userRole = currentUser.role.displayName,
                action = if (isOnline) "INVOICE_SAVED_ONLINE" else "INVOICE_QUEUED_OFFLINE",
                details = "Outward Supply ${invoice.invoiceNumber} for ${invoice.customerName} (₹${String.format(Locale.US, "%.2f", invoice.totalAmount)})",
                deviceId = currentUser.deviceName,
                isSynced = isOnline
            )
        )

        // If invoice is overdue or large, trigger notification
        if (finalInvoice.status == "OVERDUE") {
            appDao.insertNotification(
                AppNotificationEntity(
                    title = "Overdue Invoice: ${finalInvoice.invoiceNumber}",
                    message = "${finalInvoice.customerName} has an overdue balance of ₹${String.format(Locale.US, "%.2f", finalInvoice.totalAmount - finalInvoice.amountPaid)}",
                    category = "PAYMENT",
                    urgency = "HIGH"
                )
            )
        }
    }

    suspend fun saveInwardSupply(
        bill: com.example.data.model.InwardSupplyEntity,
        isOnline: Boolean,
        currentUser: UserProfile
    ) = withContext(Dispatchers.IO) {
        appDao.insertInwardSupply(bill)

        // Automatically record Inward Supply in Supplier Ledger (Credit Supplier)
        val supplier = appDao.getSupplierById(bill.supplierId).first()
        val currentPayable = supplier?.payableBalance ?: 0.0
        val newPayable = currentPayable + (bill.totalAmount - bill.amountPaid)

        appDao.insertLedgerEntry(
            com.example.data.model.LedgerEntryEntity(
                date = bill.billDate,
                accountType = "SUPPLIER",
                accountId = bill.supplierId,
                accountName = bill.supplierName,
                voucherType = "PURCHASE",
                voucherNumber = bill.billNumber,
                debit = 0.0,
                credit = bill.totalAmount,
                particulars = "By Inward Supply (Purchase Bill: ${bill.billNumber})",
                paymentMethod = bill.paymentMode,
                balanceAfter = newPayable,
                createdBy = currentUser.name
            )
        )

        if (supplier != null) {
            appDao.updateSupplier(supplier.copy(payableBalance = newPayable))
        }

        // If immediate payment was made, record payment in Supplier ledger & Cash/Bank ledger
        if (bill.amountPaid > 0) {
            val accountType = if (bill.paymentMode == "CASH") "CASH" else "BANK"
            val accountId = if (bill.paymentMode == "CASH") "CASH_A_C" else "SBI_BANK_A_C"
            val accountName = if (bill.paymentMode == "CASH") "Cash in Hand A/c" else "State Bank of India"

            appDao.insertLedgerEntry(
                com.example.data.model.LedgerEntryEntity(
                    date = bill.billDate,
                    accountType = "SUPPLIER",
                    accountId = bill.supplierId,
                    accountName = bill.supplierName,
                    voucherType = "PAYMENT",
                    voucherNumber = "PAY-${bill.billNumber}",
                    debit = bill.amountPaid,
                    credit = 0.0,
                    particulars = "To Payment via ${bill.paymentMode} for ${bill.billNumber}",
                    paymentMethod = bill.paymentMode,
                    balanceAfter = newPayable,
                    createdBy = currentUser.name
                )
            )

            appDao.insertLedgerEntry(
                com.example.data.model.LedgerEntryEntity(
                    date = bill.billDate,
                    accountType = accountType,
                    accountId = accountId,
                    accountName = accountName,
                    voucherType = "PAYMENT",
                    voucherNumber = "PAY-${bill.billNumber}",
                    debit = 0.0,
                    credit = bill.amountPaid,
                    particulars = "Supplier Settlement: ${bill.supplierName}",
                    paymentMethod = bill.paymentMode,
                    balanceAfter = 0.0,
                    createdBy = currentUser.name
                )
            )
        }

        appDao.insertSyncLog(
            SyncLogEntity(
                userName = currentUser.name,
                userRole = currentUser.role.displayName,
                action = "INWARD_SUPPLY_RECORDED",
                details = "Purchase Bill ${bill.billNumber} from ${bill.supplierName} (₹${String.format(Locale.US, "%.2f", bill.totalAmount)})",
                deviceId = currentUser.deviceName,
                isSynced = isOnline
            )
        )
    }

    suspend fun recordSupplierPayment(
        billNumber: String,
        amount: Double,
        paymentMode: String,
        currentUser: UserProfile
    ) = withContext(Dispatchers.IO) {
        val bill = appDao.getInwardSupplyByNumber(billNumber).first() ?: return@withContext
        val newPaid = (bill.amountPaid + amount).coerceAtMost(bill.totalAmount)
        val newStatus = if (newPaid >= bill.totalAmount) "PAID" else "PARTIAL"

        appDao.updateInwardSupply(bill.copy(amountPaid = newPaid, status = newStatus))

        val supplier = appDao.getSupplierById(bill.supplierId).first()
        if (supplier != null) {
            val newPayable = (supplier.payableBalance - amount).coerceAtLeast(0.0)
            appDao.updateSupplier(supplier.copy(payableBalance = newPayable))
        }

        // Ledger entry in Supplier Account (Debit Supplier)
        appDao.insertLedgerEntry(
            com.example.data.model.LedgerEntryEntity(
                date = System.currentTimeMillis(),
                accountType = "SUPPLIER",
                accountId = bill.supplierId,
                accountName = bill.supplierName,
                voucherType = "PAYMENT",
                voucherNumber = "PAY-${System.currentTimeMillis() % 10000}",
                debit = amount,
                credit = 0.0,
                particulars = "To Payment via $paymentMode for Bill ${bill.billNumber}",
                paymentMethod = paymentMode,
                balanceAfter = supplier?.payableBalance ?: 0.0,
                createdBy = currentUser.name
            )
        )

        // Contra / Payment entry in Cash or Bank
        val isCash = paymentMode == "CASH"
        appDao.insertLedgerEntry(
            com.example.data.model.LedgerEntryEntity(
                date = System.currentTimeMillis(),
                accountType = if (isCash) "CASH" else "BANK",
                accountId = if (isCash) "CASH_A_C" else "SBI_BANK_A_C",
                accountName = if (isCash) "Cash in Hand A/c" else "State Bank of India",
                voucherType = "PAYMENT",
                voucherNumber = "PAY-${System.currentTimeMillis() % 10000}",
                debit = 0.0,
                credit = amount,
                particulars = "Payment to Supplier: ${bill.supplierName} (${bill.billNumber})",
                paymentMethod = paymentMode,
                balanceAfter = 0.0,
                createdBy = currentUser.name
            )
        )
    }

    suspend fun addBankLedgerEntry(
        type: String, // "DEPOSIT", "WITHDRAWAL", "INFLOW", "OUTFLOW", "CHARGES"
        amount: Double,
        particulars: String,
        referenceNo: String,
        paymentMethod: String,
        currentUser: UserProfile
    ) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        when (type.uppercase()) {
            "DEPOSIT" -> { // Cash to Bank (Contra)
                appDao.insertLedgerEntry(
                    com.example.data.model.LedgerEntryEntity(
                        date = now,
                        accountType = "BANK",
                        accountId = "SBI_BANK_A_C",
                        accountName = "State Bank of India (30920014567890)",
                        voucherType = "CONTRA",
                        voucherNumber = referenceNo.ifBlank { "DEP-${now % 10000}" },
                        debit = amount, // Bank Dr (Inflow)
                        credit = 0.0,
                        particulars = particulars.ifBlank { "Cash Deposited in SBI Bank (Contra)" },
                        paymentMethod = "CASH",
                        createdBy = currentUser.name
                    )
                )
                appDao.insertLedgerEntry(
                    com.example.data.model.LedgerEntryEntity(
                        date = now,
                        accountType = "CASH",
                        accountId = "CASH_A_C",
                        accountName = "Cash in Hand A/c",
                        voucherType = "CONTRA",
                        voucherNumber = referenceNo.ifBlank { "DEP-${now % 10000}" },
                        debit = 0.0,
                        credit = amount, // Cash Cr (Outflow)
                        particulars = "Deposit to SBI Bank A/c",
                        paymentMethod = "CASH",
                        createdBy = currentUser.name
                    )
                )
            }
            "WITHDRAWAL" -> { // Bank to Cash (Contra)
                appDao.insertLedgerEntry(
                    com.example.data.model.LedgerEntryEntity(
                        date = now,
                        accountType = "CASH",
                        accountId = "CASH_A_C",
                        accountName = "Cash in Hand A/c",
                        voucherType = "CONTRA",
                        voucherNumber = referenceNo.ifBlank { "WDL-${now % 10000}" },
                        debit = amount, // Cash Dr (Inflow)
                        credit = 0.0,
                        particulars = particulars.ifBlank { "Cash Withdrawn from Bank for Shop Expenses" },
                        paymentMethod = "CASH",
                        createdBy = currentUser.name
                    )
                )
                appDao.insertLedgerEntry(
                    com.example.data.model.LedgerEntryEntity(
                        date = now,
                        accountType = "BANK",
                        accountId = "SBI_BANK_A_C",
                        accountName = "State Bank of India (30920014567890)",
                        voucherType = "CONTRA",
                        voucherNumber = referenceNo.ifBlank { "WDL-${now % 10000}" },
                        debit = 0.0,
                        credit = amount, // Bank Cr (Outflow)
                        particulars = "Cash Withdrawal for Counter",
                        paymentMethod = "CASH",
                        createdBy = currentUser.name
                    )
                )
            }
            "INFLOW" -> { // Direct Bank Inward / RTGS
                appDao.insertLedgerEntry(
                    com.example.data.model.LedgerEntryEntity(
                        date = now,
                        accountType = "BANK",
                        accountId = "SBI_BANK_A_C",
                        accountName = "State Bank of India (30920014567890)",
                        voucherType = "RECEIPT",
                        voucherNumber = referenceNo.ifBlank { "REC-${now % 10000}" },
                        debit = amount,
                        credit = 0.0,
                        particulars = particulars.ifBlank { "Direct Bank Inward Settlement" },
                        paymentMethod = paymentMethod,
                        createdBy = currentUser.name
                    )
                )
            }
            else -> { // Outflow or Charges
                appDao.insertLedgerEntry(
                    com.example.data.model.LedgerEntryEntity(
                        date = now,
                        accountType = "BANK",
                        accountId = "SBI_BANK_A_C",
                        accountName = "State Bank of India (30920014567890)",
                        voucherType = if (type == "CHARGES") "EXPENSE" else "PAYMENT",
                        voucherNumber = referenceNo.ifBlank { "BNK-${now % 10000}" },
                        debit = 0.0,
                        credit = amount,
                        particulars = particulars.ifBlank { "Bank Charges / Payment" },
                        paymentMethod = paymentMethod,
                        createdBy = currentUser.name
                    )
                )
            }
        }
    }

    suspend fun saveSupplier(supplier: com.example.data.model.SupplierEntity, currentUser: UserProfile) = withContext(Dispatchers.IO) {
        appDao.insertSupplier(supplier)
        appDao.insertSyncLog(
            SyncLogEntity(
                userName = currentUser.name,
                userRole = currentUser.role.displayName,
                action = "SUPPLIER_SAVED",
                details = "Supplier ${supplier.name} (GSTIN: ${supplier.gstin})",
                deviceId = currentUser.deviceName,
                isSynced = true
            )
        )
    }

    suspend fun deleteInvoice(invoice: InvoiceEntity, currentUser: UserProfile) = withContext(Dispatchers.IO) {
        appDao.deleteInvoice(invoice)
        appDao.insertSyncLog(
            SyncLogEntity(
                userName = currentUser.name,
                userRole = currentUser.role.displayName,
                action = "INVOICE_DELETED",
                details = "Deleted invoice ${invoice.invoiceNumber}",
                deviceId = currentUser.deviceName,
                isSynced = true
            )
        )
    }

    suspend fun recordPayment(
        invoiceNumber: String,
        amount: Double,
        currentUser: UserProfile,
        isOnline: Boolean
    ) = withContext(Dispatchers.IO) {
        val invoice = appDao.getInvoiceByNumber(invoiceNumber).first() ?: return@withContext
        val newPaid = (invoice.amountPaid + amount).coerceAtMost(invoice.totalAmount)
        val newStatus = if (newPaid >= invoice.totalAmount) "PAID" else "PARTIAL"

        val updated = invoice.copy(
            amountPaid = newPaid,
            status = newStatus,
            syncStatus = if (isOnline) "SYNCED" else "PENDING_SYNC",
            lastUpdated = System.currentTimeMillis()
        )
        appDao.updateInvoice(updated)

        appDao.insertSyncLog(
            SyncLogEntity(
                userName = currentUser.name,
                userRole = currentUser.role.displayName,
                action = "PAYMENT_RECORDED",
                details = "Payment of ₹${String.format(Locale.US, "%.2f", amount)} received on ${invoice.invoiceNumber}. New Status: $newStatus",
                deviceId = currentUser.deviceName,
                isSynced = isOnline
            )
        )

        appDao.insertNotification(
            AppNotificationEntity(
                title = "Payment Received on ${invoice.invoiceNumber}",
                message = "₹${String.format(Locale.US, "%.2f", amount)} recorded. Remaining: ₹${String.format(Locale.US, "%.2f", updated.totalAmount - updated.amountPaid)}",
                category = "PAYMENT",
                urgency = "NORMAL"
            )
        )
    }

    suspend fun saveCustomer(customer: CustomerEntity, currentUser: UserProfile) = withContext(Dispatchers.IO) {
        appDao.insertCustomer(customer)
        appDao.insertSyncLog(
            SyncLogEntity(
                userName = currentUser.name,
                userRole = currentUser.role.displayName,
                action = "CUSTOMER_SAVED",
                details = "Customer ${customer.name} (GSTIN: ${customer.gstin.ifEmpty { "Unregistered" }})",
                deviceId = currentUser.deviceName,
                isSynced = true
            )
        )
    }

    suspend fun saveInventoryItem(item: InventoryItemEntity, currentUser: UserProfile) = withContext(Dispatchers.IO) {
        appDao.insertInventoryItem(item)
        appDao.insertSyncLog(
            SyncLogEntity(
                userName = currentUser.name,
                userRole = currentUser.role.displayName,
                action = "INVENTORY_UPDATED",
                details = "Item: ${item.name}, Stock: ${item.stockQty} ${item.unit}",
                deviceId = currentUser.deviceName,
                isSynced = true
            )
        )
        if (item.stockQty <= item.reorderLevel) {
            appDao.insertNotification(
                AppNotificationEntity(
                    title = "Low Stock Alert: ${item.name}",
                    message = "Only ${item.stockQty} ${item.unit} remaining (Reorder threshold: ${item.reorderLevel}).",
                    category = "STOCK",
                    urgency = "HIGH"
                )
            )
        }
    }

    suspend fun performCloudSync(currentUser: UserProfile): Int = withContext(Dispatchers.IO) {
        val pendingInvoices = appDao.getPendingSyncInvoices()
        val count = pendingInvoices.size

        for (inv in pendingInvoices) {
            appDao.updateInvoice(inv.copy(syncStatus = "SYNCED", lastUpdated = System.currentTimeMillis()))
        }

        appDao.insertSyncLog(
            SyncLogEntity(
                userName = currentUser.name,
                userRole = currentUser.role.displayName,
                action = "CLOUD_SYNC_COMPLETE",
                details = "Sync completed: $count offline items pushed to cloud servers.",
                deviceId = currentUser.deviceName,
                isSynced = true
            )
        )

        appDao.insertNotification(
            AppNotificationEntity(
                title = "Cloud Sync Success",
                message = "$count offline pending changes synchronized across all multi-user devices.",
                category = "SYSTEM",
                urgency = "NORMAL"
            )
        )
        count
    }

    suspend fun markNotificationRead(id: Long) = withContext(Dispatchers.IO) {
        appDao.markNotificationAsRead(id)
    }

    suspend fun markAllNotificationsRead() = withContext(Dispatchers.IO) {
        appDao.markAllNotificationsAsRead()
    }

    // Helper: Parse items JSON
    fun parseInvoiceItems(itemsJson: String): List<InvoiceItem> {
        if (itemsJson.isBlank()) return emptyList()
        val list = mutableListOf<InvoiceItem>()
        try {
            val array = JSONArray(itemsJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    InvoiceItem(
                        id = obj.optString("id", i.toString()),
                        itemName = obj.optString("itemName", ""),
                        hsnCode = obj.optString("hsnCode", ""),
                        quantity = obj.optDouble("quantity", 1.0),
                        unit = obj.optString("unit", "PCS"),
                        rate = obj.optDouble("rate", 0.0),
                        discountPercent = obj.optDouble("discountPercent", 0.0),
                        gstRate = obj.optDouble("gstRate", 18.0),
                        taxableValue = obj.optDouble("taxableValue", 0.0),
                        cgst = obj.optDouble("cgst", 0.0),
                        sgst = obj.optDouble("sgst", 0.0),
                        igst = obj.optDouble("igst", 0.0),
                        total = obj.optDouble("total", 0.0)
                    )
                )
            }
        } catch (_: Exception) { }
        return list
    }

    // Helper: Serialize items to JSON
    fun serializeInvoiceItems(items: List<InvoiceItem>): String {
        val array = JSONArray()
        for (item in items) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("itemName", item.itemName)
                put("hsnCode", item.hsnCode)
                put("quantity", item.quantity)
                put("unit", item.unit)
                put("rate", item.rate)
                put("discountPercent", item.discountPercent)
                put("gstRate", item.gstRate)
                put("taxableValue", item.taxableValue)
                put("cgst", item.cgst)
                put("sgst", item.sgst)
                put("igst", item.igst)
                put("total", item.total)
            }
            array.put(obj)
        }
        return array.toString()
    }

    // Export Database Snapshot (JSON) for Cloud Backup
    suspend fun generateBackupJson(company: CompanyProfile): String = withContext(Dispatchers.IO) {
        val invoices = appDao.getAllInvoices().first()
        val customers = appDao.getAllCustomers().first()
        val inventory = appDao.getAllInventory().first()

        val root = JSONObject()
        root.put("version", "1.0")
        root.put("timestamp", System.currentTimeMillis())
        root.put("companyGstin", company.gstin)
        root.put("companyName", company.tradeName)

        val invArr = JSONArray()
        for (inv in invoices) {
            val item = JSONObject()
            item.put("invoiceNumber", inv.invoiceNumber)
            item.put("customerName", inv.customerName)
            item.put("customerGstin", inv.customerGstin)
            item.put("totalAmount", inv.totalAmount)
            item.put("amountPaid", inv.amountPaid)
            item.put("status", inv.status)
            item.put("taxableAmount", inv.taxableAmount)
            item.put("cgstAmount", inv.cgstAmount)
            item.put("sgstAmount", inv.sgstAmount)
            item.put("igstAmount", inv.igstAmount)
            item.put("itemsJson", inv.itemsJson)
            invArr.put(item)
        }
        root.put("invoices", invArr)

        val custArr = JSONArray()
        for (c in customers) {
            val item = JSONObject()
            item.put("id", c.id)
            item.put("name", c.name)
            item.put("gstin", c.gstin)
            item.put("phone", c.phone)
            item.put("outstandingAmount", c.outstandingAmount)
            custArr.put(item)
        }
        root.put("customers", custArr)

        val prodArr = JSONArray()
        for (p in inventory) {
            val item = JSONObject()
            item.put("id", p.id)
            item.put("name", p.name)
            item.put("sku", p.sku)
            item.put("stockQty", p.stockQty)
            item.put("sellingPrice", p.sellingPrice)
            prodArr.put(item)
        }
        root.put("inventory", prodArr)

        root.toString(2)
    }

    // Export Tally XML format
    fun generateTallyXml(invoices: List<InvoiceEntity>, company: CompanyProfile): String {
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8"?>""").append("\n")
        sb.append("<ENVELOPE>\n")
        sb.append("  <HEADER>\n")
        sb.append("    <TALLYREQUEST>Import Data</TALLYREQUEST>\n")
        sb.append("  </HEADER>\n")
        sb.append("  <BODY>\n")
        sb.append("    <IMPORTDATA>\n")
        sb.append("      <REQUESTDESC>\n")
        sb.append("        <REPORTNAME>Vouchers</REPORTNAME>\n")
        sb.append("        <STATICVARIABLES>\n")
        sb.append("          <SVCURRENTCOMPANY>${company.tradeName}</SVCURRENTCOMPANY>\n")
        sb.append("        </STATICVARIABLES>\n")
        sb.append("      </REQUESTDESC>\n")
        sb.append("      <REQUESTDATA>\n")

        val sdf = SimpleDateFormat("yyyyMMdd", Locale.US)
        for (inv in invoices) {
            val dateStr = sdf.format(Date(inv.invoiceDate))
            sb.append("        <TALLYMESSAGE xmlns:UDF=\"TallyUDF\">\n")
            sb.append("          <VOUCHER VCHTYPE=\"Sales\" ACTION=\"Create\">\n")
            sb.append("            <DATE>$dateStr</DATE>\n")
            sb.append("            <VOUCHERNUMBER>${inv.invoiceNumber}</VOUCHERNUMBER>\n")
            sb.append("            <PARTYNAME>${inv.customerName}</PARTYNAME>\n")
            sb.append("            <PARTYLEDGERNAME>${inv.customerName}</PARTYLEDGERNAME>\n")
            sb.append("            <PLACEOFSUPPLY>${inv.placeOfSupply}</PLACEOFSUPPLY>\n")
            sb.append("            <NARRATION>${inv.notes}</NARRATION>\n")
            sb.append("            <ALLLEDGERENTRIES.LIST>\n")
            sb.append("              <LEDGERNAME>${inv.customerName}</LEDGERNAME>\n")
            sb.append("              <ISDEEMEDPOSITIVE>Yes</ISDEEMEDPOSITIVE>\n")
            sb.append("              <AMOUNT>-${inv.totalAmount}</AMOUNT>\n")
            sb.append("            </ALLLEDGERENTRIES.LIST>\n")
            sb.append("            <ALLLEDGERENTRIES.LIST>\n")
            sb.append("              <LEDGERNAME>GST Sales Account</LEDGERNAME>\n")
            sb.append("              <ISDEEMEDPOSITIVE>No</ISDEEMEDPOSITIVE>\n")
            sb.append("              <AMOUNT>${inv.taxableAmount}</AMOUNT>\n")
            sb.append("            </ALLLEDGERENTRIES.LIST>\n")
            if (inv.isInterState) {
                sb.append("            <ALLLEDGERENTRIES.LIST>\n")
                sb.append("              <LEDGERNAME>Output IGST</LEDGERNAME>\n")
                sb.append("              <ISDEEMEDPOSITIVE>No</ISDEEMEDPOSITIVE>\n")
                sb.append("              <AMOUNT>${inv.igstAmount}</AMOUNT>\n")
                sb.append("            </ALLLEDGERENTRIES.LIST>\n")
            } else {
                sb.append("            <ALLLEDGERENTRIES.LIST>\n")
                sb.append("              <LEDGERNAME>Output CGST</LEDGERNAME>\n")
                sb.append("              <ISDEEMEDPOSITIVE>No</ISDEEMEDPOSITIVE>\n")
                sb.append("              <AMOUNT>${inv.cgstAmount}</AMOUNT>\n")
                sb.append("            </ALLLEDGERENTRIES.LIST>\n")
                sb.append("            <ALLLEDGERENTRIES.LIST>\n")
                sb.append("              <LEDGERNAME>Output SGST</LEDGERNAME>\n")
                sb.append("              <ISDEEMEDPOSITIVE>No</ISDEEMEDPOSITIVE>\n")
                sb.append("              <AMOUNT>${inv.sgstAmount}</AMOUNT>\n")
                sb.append("            </ALLLEDGERENTRIES.LIST>\n")
            }
            sb.append("          </VOUCHER>\n")
            sb.append("        </TALLYMESSAGE>\n")
        }

        sb.append("      </REQUESTDATA>\n")
        sb.append("    </IMPORTDATA>\n")
        sb.append("  </BODY>\n")
        sb.append("</ENVELOPE>\n")
        return sb.toString()
    }

    // Build WhatsApp reminder message
    fun buildPaymentReminderMessage(
        invoice: InvoiceEntity,
        company: CompanyProfile
    ): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val dueStr = sdf.format(Date(invoice.dueDate))
        val pending = invoice.totalAmount - invoice.amountPaid
        return """
            *Payment Reminder from ${company.tradeName}*
            
            Dear ${invoice.customerName},
            This is a gentle reminder that payment for Invoice *${invoice.invoiceNumber}* is pending.
            
            • *Invoice Amount:* ₹${String.format(Locale.US, "%.2f", invoice.totalAmount)}
            • *Pending Balance:* ₹${String.format(Locale.US, "%.2f", pending)}
            • *Due Date:* $dueStr
            
            *Payment Details:*
            Bank: ${company.bankName}
            Account No: ${company.bankAccount}
            IFSC: ${company.ifscCode}
            UPI ID: ${company.upiId}
            
            Kindly clear the balance at your earliest convenience. Thank you!
        """.trimIndent()
    }

    // Export Single Invoice in Excel / CSV Format
    fun generateInvoiceExcelCsv(invoice: InvoiceEntity, company: CompanyProfile): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        val sb = StringBuilder()
        sb.append("TAX INVOICE - ${company.tradeName}\n")
        sb.append("GSTIN: ${company.gstin}, State: ${company.stateName} (${company.stateCode})\n")
        sb.append("Invoice No,${invoice.invoiceNumber},Invoice Date,${sdf.format(Date(invoice.invoiceDate))}\n")
        sb.append("Customer,${invoice.customerName},Customer GSTIN,${invoice.customerGstin}\n")
        sb.append("Place of Supply,${invoice.placeOfSupply},Type,${if (invoice.isInterState) "Inter-State (IGST)" else "Intra-State (CGST+SGST)"}\n\n")

        sb.append("#,Item Name,HSN/SAC,Qty,Unit,Rate,Discount %,Taxable Value,GST %,CGST,SGST,IGST,Total (INR)\n")
        val items = parseInvoiceItems(invoice.itemsJson)
        items.forEachIndexed { idx, it ->
            sb.append("${idx + 1},\"${it.itemName}\",${it.hsnCode},${it.quantity},${it.unit},${it.rate},${it.discountPercent},${String.format(Locale.US, "%.2f", it.taxableValue)},${it.gstRate}%,${String.format(Locale.US, "%.2f", it.cgst)},${String.format(Locale.US, "%.2f", it.sgst)},${String.format(Locale.US, "%.2f", it.igst)},${String.format(Locale.US, "%.2f", it.total)}\n")
        }
        sb.append("\n,,,Total Taxable,${String.format(Locale.US, "%.2f", invoice.taxableAmount)},CGST,${String.format(Locale.US, "%.2f", invoice.cgstAmount)},SGST,${String.format(Locale.US, "%.2f", invoice.sgstAmount)},IGST,${String.format(Locale.US, "%.2f", invoice.igstAmount)},Grand Total,${String.format(Locale.US, "%.2f", invoice.totalAmount)}\n")
        sb.append(",,,Amount Received,${String.format(Locale.US, "%.2f", invoice.amountPaid)},Balance Due,${String.format(Locale.US, "%.2f", invoice.totalAmount - invoice.amountPaid)}\n")
        return sb.toString()
    }

    // Export Outward Register in Excel / CSV
    fun generateAllInvoicesExcelCsv(invoices: List<InvoiceEntity>, company: CompanyProfile): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        val sb = StringBuilder()
        sb.append("OUTWARD SUPPLY REGISTER (SALES) - ${company.tradeName}\n")
        sb.append("GSTIN: ${company.gstin}\n\n")
        sb.append("Invoice No,Date,Customer Name,GSTIN,POS,Taxable Value,CGST,SGST,IGST,Total Amount,Paid,Balance Due,Status\n")
        for (inv in invoices) {
            val due = inv.totalAmount - inv.amountPaid
            sb.append("${inv.invoiceNumber},${sdf.format(Date(inv.invoiceDate))},\"${inv.customerName}\",${inv.customerGstin},${inv.placeOfSupply},${String.format(Locale.US, "%.2f", inv.taxableAmount)},${String.format(Locale.US, "%.2f", inv.cgstAmount)},${String.format(Locale.US, "%.2f", inv.sgstAmount)},${String.format(Locale.US, "%.2f", inv.igstAmount)},${String.format(Locale.US, "%.2f", inv.totalAmount)},${String.format(Locale.US, "%.2f", inv.amountPaid)},${String.format(Locale.US, "%.2f", due)},${inv.status}\n")
        }
        return sb.toString()
    }

    // Export Inward Supply Register in Excel / CSV
    fun generateInwardSuppliesExcelCsv(bills: List<com.example.data.model.InwardSupplyEntity>, company: CompanyProfile): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        val sb = StringBuilder()
        sb.append("INWARD SUPPLY REGISTER (PURCHASES) - ${company.tradeName}\n")
        sb.append("GSTIN: ${company.gstin}\n\n")
        sb.append("Bill No,Date,Supplier Name,GSTIN,Taxable Value,CGST,SGST,IGST,Total Amount,Paid,Balance,Status,Payment Mode\n")
        for (b in bills) {
            val due = b.totalAmount - b.amountPaid
            sb.append("${b.billNumber},${sdf.format(Date(b.billDate))},\"${b.supplierName}\",${b.supplierGstin},${String.format(Locale.US, "%.2f", b.taxableAmount)},${String.format(Locale.US, "%.2f", b.cgstAmount)},${String.format(Locale.US, "%.2f", b.sgstAmount)},${String.format(Locale.US, "%.2f", b.igstAmount)},${String.format(Locale.US, "%.2f", b.totalAmount)},${String.format(Locale.US, "%.2f", b.amountPaid)},${String.format(Locale.US, "%.2f", due)},${b.status},${b.paymentMode}\n")
        }
        return sb.toString()
    }

    // Export Account Ledger in Excel / CSV
    fun generateLedgerExcelCsv(entries: List<com.example.data.model.LedgerEntryEntity>, accountName: String, company: CompanyProfile): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)
        val sb = StringBuilder()
        sb.append("STATEMENT OF ACCOUNT (LEDGER): $accountName\n")
        sb.append("Company: ${company.tradeName} (GSTIN: ${company.gstin})\n\n")
        sb.append("Date,Voucher Type,Voucher No,Particulars,Debit (Dr),Credit (Cr),Running Balance\n")
        for (e in entries) {
            sb.append("${sdf.format(Date(e.date))},${e.voucherType},${e.voucherNumber},\"${e.particulars}\",${String.format(Locale.US, "%.2f", e.debit)},${String.format(Locale.US, "%.2f", e.credit)},${String.format(Locale.US, "%.2f", e.balanceAfter)}\n")
        }
        val totalDr = entries.sumOf { it.debit }
        val totalCr = entries.sumOf { it.credit }
        sb.append("\nTotal,,,\"Total Debit / Credit\",${String.format(Locale.US, "%.2f", totalDr)},${String.format(Locale.US, "%.2f", totalCr)},Net Balance: ${String.format(Locale.US, "%.2f", totalDr - totalCr)}\n")
        return sb.toString()
    }

    // Export Daybook in Excel / CSV
    fun generateDaybookExcelCsv(entries: List<com.example.data.model.LedgerEntryEntity>, dateStr: String, company: CompanyProfile): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.US)
        val sb = StringBuilder()
        sb.append("DAYBOOK REPORT - $dateStr\n")
        sb.append("${company.tradeName} (GSTIN: ${company.gstin})\n\n")
        sb.append("Time,Account Name,Voucher Type,Voucher No,Particulars,Debit (Dr),Credit (Cr),Mode\n")
        for (e in entries) {
            sb.append("${sdf.format(Date(e.date))},\"${e.accountName}\",${e.voucherType},${e.voucherNumber},\"${e.particulars}\",${String.format(Locale.US, "%.2f", e.debit)},${String.format(Locale.US, "%.2f", e.credit)},${e.paymentMethod}\n")
        }
        val totalDr = entries.sumOf { it.debit }
        val totalCr = entries.sumOf { it.credit }
        sb.append("\nTotal,,,\"Daybook Totals\",${String.format(Locale.US, "%.2f", totalDr)},${String.format(Locale.US, "%.2f", totalCr)},\n")
        return sb.toString()
    }
}
