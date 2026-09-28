package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey
    val invoiceNumber: String,
    val customerName: String,
    val customerGstin: String,
    val customerPhone: String,
    val customerEmail: String,
    val billingAddress: String,
    val placeOfSupply: String, // e.g., "27-Maharashtra"
    val invoiceDate: Long,
    val dueDate: Long,
    val taxableAmount: Double,
    val cgstAmount: Double,
    val sgstAmount: Double,
    val igstAmount: Double,
    val totalAmount: Double,
    val amountPaid: Double,
    val status: String, // PAID, PENDING, OVERDUE, PARTIAL
    val isInterState: Boolean,
    val notes: String = "",
    val itemsJson: String = "", // serialized items
    val syncStatus: String = "SYNCED", // SYNCED, PENDING_SYNC, CONFLICT
    val lastUpdated: Long = System.currentTimeMillis(),
    val createdBy: String = "Rajesh Sharma",
    val createdByRole: String = "Admin"
)

data class InvoiceItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val itemName: String,
    val hsnCode: String,
    val quantity: Double,
    val unit: String = "PCS",
    val rate: Double,
    val discountPercent: Double = 0.0,
    val gstRate: Double = 18.0, // 0, 5, 12, 18, 28
    val taxableValue: Double = 0.0,
    val cgst: Double = 0.0,
    val sgst: Double = 0.0,
    val igst: Double = 0.0,
    val total: Double = 0.0
)

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val companyName: String,
    val gstin: String,
    val phone: String,
    val email: String,
    val address: String,
    val stateCode: String, // e.g. "09"
    val outstandingAmount: Double = 0.0,
    val creditLimit: Double = 100000.0,
    val syncStatus: String = "SYNCED",
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "suppliers")
data class SupplierEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val companyName: String,
    val gstin: String,
    val phone: String,
    val email: String,
    val address: String,
    val stateCode: String, // e.g. "09"
    val payableBalance: Double = 0.0,
    val creditPeriodDays: Int = 15,
    val syncStatus: String = "SYNCED",
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "inward_supplies")
data class InwardSupplyEntity(
    @PrimaryKey
    val billNumber: String,
    val supplierId: String,
    val supplierName: String,
    val supplierGstin: String,
    val billDate: Long,
    val dueDate: Long,
    val taxableAmount: Double,
    val cgstAmount: Double,
    val sgstAmount: Double,
    val igstAmount: Double,
    val totalAmount: Double,
    val amountPaid: Double = 0.0,
    val status: String = "PENDING", // PAID, PENDING, PARTIAL
    val isInterState: Boolean = false,
    val itcEligible: Boolean = true,
    val itemsJson: String = "",
    val paymentMode: String = "BANK", // CASH, BANK, CREDIT
    val notes: String = "",
    val syncStatus: String = "SYNCED",
    val lastUpdated: Long = System.currentTimeMillis(),
    val createdBy: String = "Amit Kesharwani"
)

@Entity(tableName = "ledger_entries")
data class LedgerEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long = System.currentTimeMillis(),
    val accountType: String, // "CUSTOMER", "SUPPLIER", "CASH", "BANK"
    val accountId: String,   // Customer ID, Supplier ID, "CASH_A_C", "SBI_BANK_A_C"
    val accountName: String, // Party Name or Ledger Name
    val voucherType: String, // "SALES", "PURCHASE", "RECEIPT", "PAYMENT", "CONTRA", "EXPENSE"
    val voucherNumber: String,
    val debit: Double = 0.0,  // Dr
    val credit: Double = 0.0, // Cr
    val particulars: String,
    val paymentMethod: String = "BANK", // "CASH", "BANK_UPI", "BANK_NEFT", "BANK_CHEQUE"
    val balanceAfter: Double = 0.0,
    val createdBy: String = "Amit Kesharwani"
)

@Entity(tableName = "inventory")
data class InventoryItemEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val sku: String,
    val hsnCode: String,
    val category: String,
    val unit: String = "PCS",
    val purchasePrice: Double,
    val sellingPrice: Double,
    val gstRate: Double = 18.0,
    val stockQty: Double,
    val reorderLevel: Double = 10.0,
    val syncStatus: String = "SYNCED",
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "sync_logs")
data class SyncLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val userName: String,
    val userRole: String,
    val action: String,
    val details: String,
    val deviceId: String,
    val isSynced: Boolean = true
)

@Entity(tableName = "app_notifications")
data class AppNotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val message: String,
    val category: String, // "PAYMENT", "TAX", "STOCK", "SYSTEM"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val urgency: String = "NORMAL" // "HIGH", "NORMAL", "LOW"
)

enum class UserRole(val displayName: String, val description: String) {
    ADMIN("Admin / Owner", "Full access to settings, tax filing, approvals & deletion"),
    ACCOUNTANT("Chartered Accountant", "Tax compliance, GSTR reports, ITC audit & ledger"),
    MANAGER("Operations Manager", "Sales & inventory oversight, invoicing & reminders"),
    SALES("Sales Executive", "Create invoices, record payments & customer relations")
}

data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val avatarInitials: String,
    val deviceName: String,
    val firebaseUid: String? = null,
    val photoUrl: String? = null
)

data class CompanyProfile(
    val tradeName: String = "Kesharwani Enterprises",
    val legalName: String = "Kesharwani Enterprises FMCG Wholesale Pvt Ltd",
    val gstin: String = "09AABCK1234F1Z5",
    val stateCode: String = "09",
    val stateName: String = "Uttar Pradesh",
    val address: String = "Shop No. 14-16, Wholesale Kirana Mandi, G.T. Road, Prayagraj 211001",
    val phone: String = "+91 94152 33445",
    val email: String = "kesharwani.enterprises@gmail.com",
    val pan: String = "AABCK1234F",
    val bankName: String = "State Bank of India",
    val bankAccount: String = "30920014567890",
    val ifscCode: String = "SBIN0001234",
    val upiId: String = "kesharwani.fmcg@sbi"
)
