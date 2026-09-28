package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth.FirebaseAuthManager
import com.example.data.local.AppDatabase
import com.example.data.model.AppNotificationEntity
import com.example.data.model.CompanyProfile
import com.example.data.model.CustomerEntity
import com.example.data.model.InventoryItemEntity
import com.example.data.model.InvoiceEntity
import com.example.data.model.InvoiceItem
import com.example.data.model.InwardSupplyEntity
import com.example.data.model.LedgerEntryEntity
import com.example.data.model.SupplierEntity
import com.example.data.model.SyncLogEntity
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.data.repository.GstRepository
import com.example.util.ExportUtils
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class GstViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = GstRepository(database.appDao())
    val authManager: FirebaseAuthManager = FirebaseAuthManager(application)

    // Available Users for Multi-User RBAC (Kesharwani Enterprises FMCG Wholesale)
    val availableUsers = listOf(
        UserProfile("USR-1", "Amit Kesharwani", "amit@kesharwani.in", UserRole.ADMIN, "AK", "Pixel-8-Admin"),
        UserProfile("USR-2", "Shikhar Kesharwani", "shikhar@kesharwani.in", UserRole.MANAGER, "SK", "Mandi-Warehouse-Tab"),
        UserProfile("USR-3", "Rakesh Agrawal", "rakesh.ca@kesharwani.in", UserRole.ACCOUNTANT, "RA", "Desktop-CA-Workstation"),
        UserProfile("USR-4", "Sunil Kumar", "sunil.sales@kesharwani.in", UserRole.SALES, "SK", "Mobile-Billing-S24")
    )

    // Current Logged-in User
    private val _currentUser = MutableStateFlow(availableUsers[0])
    val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    init {
        viewModelScope.launch {
            authManager.currentUser.collect { firebaseUser ->
                if (firebaseUser != null) {
                    val name = firebaseUser.displayName ?: firebaseUser.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() } ?: "Firebase Operator"
                    val initials = name.split(" ")
                        .mapNotNull { it.firstOrNull()?.toString()?.uppercase() }
                        .take(2)
                        .joinToString("")
                        .ifBlank { "FB" }
                    _currentUser.value = _currentUser.value.copy(
                        name = name,
                        email = firebaseUser.email ?: _currentUser.value.email,
                        avatarInitials = initials,
                        firebaseUid = firebaseUser.uid,
                        photoUrl = firebaseUser.photoUrl?.toString()
                    )
                    _userMessage.value = "Authenticated with Firebase: ${firebaseUser.email ?: name}"
                }
            }
        }
    }

    // Company Profile
    private val _companyProfile = MutableStateFlow(CompanyProfile())
    val companyProfile: StateFlow<CompanyProfile> = _companyProfile.asStateFlow()

    // Network / Offline State
    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    // Syncing Progress indicator
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    // Theme: null means system default, true = dark, false = light
    private val _darkModeOverride = MutableStateFlow<Boolean?>(null)
    val darkModeOverride: StateFlow<Boolean?> = _darkModeOverride.asStateFlow()

    // Search and Filter States for Invoices
    val invoiceSearchQuery = MutableStateFlow("")
    val invoiceStatusFilter = MutableStateFlow("ALL") // ALL, PAID, PENDING, OVERDUE, PARTIAL

    // Raw Flows from DB
    val rawInvoices: StateFlow<List<InvoiceEntity>> = repository.allInvoices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customers: StateFlow<List<CustomerEntity>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val suppliers: StateFlow<List<SupplierEntity>> = repository.allSuppliers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val inwardSupplies: StateFlow<List<InwardSupplyEntity>> = repository.allInwardSupplies
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val ledgerEntries: StateFlow<List<LedgerEntryEntity>> = repository.allLedgerEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val inventory: StateFlow<List<InventoryItemEntity>> = repository.allInventory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val syncLogs: StateFlow<List<SyncLogEntity>> = repository.allSyncLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<AppNotificationEntity>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Invoices
    val filteredInvoices: StateFlow<List<InvoiceEntity>> = combine(
        rawInvoices,
        invoiceSearchQuery,
        invoiceStatusFilter
    ) { invoices, query, filter ->
        invoices.filter { inv ->
            val matchesQuery = query.isBlank() ||
                inv.invoiceNumber.contains(query, ignoreCase = true) ||
                inv.customerName.contains(query, ignoreCase = true) ||
                inv.customerGstin.contains(query, ignoreCase = true)
            val matchesFilter = when (filter) {
                "ALL" -> true
                else -> inv.status.equals(filter, ignoreCase = true)
            }
            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Invoice for Detail / Edit
    private val _selectedInvoice = MutableStateFlow<InvoiceEntity?>(null)
    val selectedInvoice: StateFlow<InvoiceEntity?> = _selectedInvoice.asStateFlow()

    // Notification Preferences
    val notifyOverdue = MutableStateFlow(true)
    val notifyLowStock = MutableStateFlow(true)
    val notifyTaxDeadlines = MutableStateFlow(true)
    val notifyCloudSync = MutableStateFlow(true)

    // User Feedback Toast/Snackbar Message
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun switchUser(user: UserProfile) {
        _currentUser.value = user
        _userMessage.value = "Switched active session to ${user.name} (${user.role.displayName})"
    }

    fun toggleOnlineStatus() {
        val newState = !_isOnline.value
        _isOnline.value = newState
        if (newState) {
            _userMessage.value = "Network restored: Syncing offline changes..."
            triggerSync()
        } else {
            _userMessage.value = "Offline Mode active: All invoices will be cached locally."
        }
    }

    fun setDarkModeOverride(dark: Boolean?) {
        _darkModeOverride.value = dark
    }

    fun selectInvoice(invoice: InvoiceEntity?) {
        _selectedInvoice.value = invoice
    }

    fun triggerSync() {
        viewModelScope.launch {
            _isSyncing.value = true
            val count = repository.performCloudSync(_currentUser.value)
            _isSyncing.value = false
            _userMessage.value = if (count > 0) {
                "Cloud sync successful: $count offline items synchronized!"
            } else {
                "Cloud sync complete: All devices in sync."
            }
        }
    }

    fun saveInvoice(
        invoiceNumber: String,
        customerName: String,
        customerGstin: String,
        customerPhone: String,
        customerEmail: String,
        billingAddress: String,
        placeOfSupply: String,
        dueDate: Long,
        items: List<InvoiceItem>,
        notes: String
    ) {
        viewModelScope.launch {
            val isInterState = !placeOfSupply.startsWith(_companyProfile.value.stateCode)

            var subtotal = 0.0
            var cgstTotal = 0.0
            var sgstTotal = 0.0
            var igstTotal = 0.0

            val computedItems = items.map { item ->
                val gross = item.quantity * item.rate
                val disc = gross * (item.discountPercent / 100.0)
                val taxable = (gross - disc).coerceAtLeast(0.0)
                subtotal += taxable

                val tax = taxable * (item.gstRate / 100.0)
                val (cgst, sgst, igst) = if (isInterState) {
                    igstTotal += tax
                    Triple(0.0, 0.0, tax)
                } else {
                    val half = tax / 2.0
                    cgstTotal += half
                    sgstTotal += half
                    Triple(half, half, 0.0)
                }
                item.copy(
                    taxableValue = taxable,
                    cgst = cgst,
                    sgst = sgst,
                    igst = igst,
                    total = taxable + tax
                )
            }

            val grandTotal = subtotal + cgstTotal + sgstTotal + igstTotal
            val itemsJson = repository.serializeInvoiceItems(computedItems)

            val invoice = InvoiceEntity(
                invoiceNumber = invoiceNumber.ifBlank { "INV-${System.currentTimeMillis() % 100000}" },
                customerName = customerName,
                customerGstin = customerGstin.trim().uppercase(),
                customerPhone = customerPhone,
                customerEmail = customerEmail,
                billingAddress = billingAddress,
                placeOfSupply = placeOfSupply,
                invoiceDate = System.currentTimeMillis(),
                dueDate = dueDate,
                taxableAmount = subtotal,
                cgstAmount = cgstTotal,
                sgstAmount = sgstTotal,
                igstAmount = igstTotal,
                totalAmount = grandTotal,
                amountPaid = 0.0,
                status = "PENDING",
                isInterState = isInterState,
                notes = notes,
                itemsJson = itemsJson,
                syncStatus = if (_isOnline.value) "SYNCED" else "PENDING_SYNC",
                createdBy = _currentUser.value.name,
                createdByRole = _currentUser.value.role.name
            )

            repository.saveInvoice(invoice, _isOnline.value, _currentUser.value)
            _userMessage.value = "Invoice ${invoice.invoiceNumber} created (${if (_isOnline.value) "Synced" else "Cached Offline"})."
        }
    }

    fun recordPayment(invoiceNumber: String, amount: Double) {
        viewModelScope.launch {
            repository.recordPayment(invoiceNumber, amount, _currentUser.value, _isOnline.value)
            _userMessage.value = "Payment of ₹$amount recorded."
        }
    }

    fun deleteInvoice(invoice: InvoiceEntity) {
        if (_currentUser.value.role != UserRole.ADMIN) {
            _userMessage.value = "Access Denied: Only Admin can delete invoices."
            return
        }
        viewModelScope.launch {
            repository.deleteInvoice(invoice, _currentUser.value)
            _userMessage.value = "Invoice ${invoice.invoiceNumber} deleted."
            if (_selectedInvoice.value?.invoiceNumber == invoice.invoiceNumber) {
                _selectedInvoice.value = null
            }
        }
    }

    fun saveCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.saveCustomer(customer, _currentUser.value)
            _userMessage.value = "Customer ${customer.name} saved."
        }
    }

    fun saveInventoryItem(item: InventoryItemEntity) {
        viewModelScope.launch {
            repository.saveInventoryItem(item, _currentUser.value)
            _userMessage.value = "Product ${item.name} saved."
        }
    }

    fun markNotificationRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationRead(id)
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsRead()
            _userMessage.value = "All notifications marked as read."
        }
    }

    fun updateCompanyProfile(updated: CompanyProfile) {
        if (_currentUser.value.role != UserRole.ADMIN) {
            _userMessage.value = "Access Denied: Only Admin can update company details."
            return
        }
        _companyProfile.value = updated
        _userMessage.value = "Company GST profile updated."
    }

    fun parseItems(itemsJson: String): List<InvoiceItem> {
        return repository.parseInvoiceItems(itemsJson)
    }

    fun getPaymentReminderText(invoice: InvoiceEntity): String {
        return repository.buildPaymentReminderMessage(invoice, _companyProfile.value)
    }

    fun getTallyXmlExport(): String {
        return repository.generateTallyXml(rawInvoices.value, _companyProfile.value)
    }

    suspend fun getCloudBackupJson(): String {
        return repository.generateBackupJson(_companyProfile.value)
    }

    fun saveSupplier(supplier: SupplierEntity) {
        viewModelScope.launch {
            repository.saveSupplier(supplier, _currentUser.value)
            _userMessage.value = "Supplier ${supplier.name} saved."
        }
    }

    fun saveInwardSupply(
        billNumber: String,
        supplier: SupplierEntity,
        taxableAmount: Double,
        cgst: Double,
        sgst: Double,
        igst: Double,
        totalAmount: Double,
        amountPaid: Double,
        paymentMode: String,
        isInterState: Boolean,
        notes: String
    ) {
        viewModelScope.launch {
            val status = if (amountPaid >= totalAmount) "PAID" else if (amountPaid > 0) "PARTIAL" else "PENDING"
            val bill = InwardSupplyEntity(
                billNumber = billNumber.ifBlank { "PUR-${System.currentTimeMillis() % 100000}" },
                supplierId = supplier.id,
                supplierName = supplier.name,
                supplierGstin = supplier.gstin,
                billDate = System.currentTimeMillis(),
                dueDate = System.currentTimeMillis() + (supplier.creditPeriodDays * 86400000L),
                taxableAmount = taxableAmount,
                cgstAmount = cgst,
                sgstAmount = sgst,
                igstAmount = igst,
                totalAmount = totalAmount,
                amountPaid = amountPaid,
                status = status,
                isInterState = isInterState,
                itcEligible = true,
                paymentMode = paymentMode,
                notes = notes,
                createdBy = _currentUser.value.name
            )
            repository.saveInwardSupply(bill, _isOnline.value, _currentUser.value)
            _userMessage.value = "Inward Supply ${bill.billNumber} recorded in Supplier Ledger."
        }
    }

    fun recordSupplierPayment(billNumber: String, amount: Double, paymentMode: String) {
        viewModelScope.launch {
            repository.recordSupplierPayment(billNumber, amount, paymentMode, _currentUser.value)
            _userMessage.value = "Payment of ₹$amount to supplier recorded."
        }
    }

    fun addBankLedgerEntry(
        type: String, // "DEPOSIT", "WITHDRAWAL", "INFLOW", "OUTFLOW", "CHARGES"
        amount: Double,
        particulars: String,
        referenceNo: String,
        paymentMethod: String
    ) {
        viewModelScope.launch {
            repository.addBankLedgerEntry(
                type = type,
                amount = amount,
                particulars = particulars,
                referenceNo = referenceNo,
                paymentMethod = paymentMethod,
                currentUser = _currentUser.value
            )
            _userMessage.value = "Bank Ledger Entry recorded successfully."
        }
    }

    fun addCashEntry(
        type: String, // "RECEIPT", "EXPENSE"
        amount: Double,
        particulars: String,
        voucherNumber: String
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val isReceipt = type.equals("RECEIPT", ignoreCase = true)
            database.appDao().insertLedgerEntry(
                LedgerEntryEntity(
                    date = now,
                    accountType = "CASH",
                    accountId = "CASH_A_C",
                    accountName = "Cash in Hand A/c",
                    voucherType = if (isReceipt) "RECEIPT" else "EXPENSE",
                    voucherNumber = voucherNumber.ifBlank { "CSH-${now % 10000}" },
                    debit = if (isReceipt) amount else 0.0,
                    credit = if (!isReceipt) amount else 0.0,
                    particulars = particulars.ifBlank { if (isReceipt) "Cash Received" else "Shop Expense" },
                    paymentMethod = "CASH",
                    createdBy = _currentUser.value.name
                )
            )
            _userMessage.value = "Cash ${if (isReceipt) "Receipt" else "Expense"} of ₹$amount recorded."
        }
    }
}
