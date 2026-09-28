package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AppNotificationEntity
import com.example.data.model.CustomerEntity
import com.example.data.model.InventoryItemEntity
import com.example.data.model.InvoiceEntity
import com.example.data.model.InwardSupplyEntity
import com.example.data.model.LedgerEntryEntity
import com.example.data.model.SupplierEntity
import com.example.data.model.SyncLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // Invoices (Outward Supply)
    @Query("SELECT * FROM invoices ORDER BY invoiceDate DESC")
    fun getAllInvoices(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE invoiceNumber = :invoiceNumber LIMIT 1")
    fun getInvoiceByNumber(invoiceNumber: String): Flow<InvoiceEntity?>

    @Query("SELECT * FROM invoices WHERE syncStatus = 'PENDING_SYNC'")
    suspend fun getPendingSyncInvoices(): List<InvoiceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoices(invoices: List<InvoiceEntity>)

    @Update
    suspend fun updateInvoice(invoice: InvoiceEntity)

    @Delete
    suspend fun deleteInvoice(invoice: InvoiceEntity)

    // Suppliers
    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    fun getAllSuppliers(): Flow<List<SupplierEntity>>

    @Query("SELECT * FROM suppliers WHERE id = :id LIMIT 1")
    fun getSupplierById(id: String): Flow<SupplierEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: SupplierEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuppliers(suppliers: List<SupplierEntity>)

    @Update
    suspend fun updateSupplier(supplier: SupplierEntity)

    @Delete
    suspend fun deleteSupplier(supplier: SupplierEntity)

    // Inward Supplies (Purchases / Supplier Bills)
    @Query("SELECT * FROM inward_supplies ORDER BY billDate DESC")
    fun getAllInwardSupplies(): Flow<List<InwardSupplyEntity>>

    @Query("SELECT * FROM inward_supplies WHERE billNumber = :billNumber LIMIT 1")
    fun getInwardSupplyByNumber(billNumber: String): Flow<InwardSupplyEntity?>

    @Query("SELECT * FROM inward_supplies WHERE supplierId = :supplierId ORDER BY billDate DESC")
    fun getInwardSuppliesBySupplier(supplierId: String): Flow<List<InwardSupplyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInwardSupply(bill: InwardSupplyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInwardSupplies(bills: List<InwardSupplyEntity>)

    @Update
    suspend fun updateInwardSupply(bill: InwardSupplyEntity)

    @Delete
    suspend fun deleteInwardSupply(bill: InwardSupplyEntity)

    // Ledger Entries (General Ledger, Customer, Supplier, Cash, Bank, Daybook)
    @Query("SELECT * FROM ledger_entries ORDER BY date DESC, id DESC")
    fun getAllLedgerEntries(): Flow<List<LedgerEntryEntity>>

    @Query("SELECT * FROM ledger_entries WHERE accountId = :accountId ORDER BY date ASC, id ASC")
    fun getLedgerEntriesByAccount(accountId: String): Flow<List<LedgerEntryEntity>>

    @Query("SELECT * FROM ledger_entries WHERE accountType = :accountType ORDER BY date DESC, id DESC")
    fun getLedgerEntriesByType(accountType: String): Flow<List<LedgerEntryEntity>>

    @Query("SELECT * FROM ledger_entries WHERE date >= :startOfDay AND date <= :endOfDay ORDER BY date ASC, id ASC")
    fun getDaybookEntries(startOfDay: Long, endOfDay: Long): Flow<List<LedgerEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLedgerEntry(entry: LedgerEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLedgerEntries(entries: List<LedgerEntryEntity>)

    @Delete
    suspend fun deleteLedgerEntry(entry: LedgerEntryEntity)

    // Customers
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    fun getCustomerById(id: String): Flow<CustomerEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<CustomerEntity>)

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)

    // Inventory
    @Query("SELECT * FROM inventory ORDER BY name ASC")
    fun getAllInventory(): Flow<List<InventoryItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItem(item: InventoryItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItems(items: List<InventoryItemEntity>)

    @Update
    suspend fun updateInventoryItem(item: InventoryItemEntity)

    @Delete
    suspend fun deleteInventoryItem(item: InventoryItemEntity)

    // Sync logs / audit trail
    @Query("SELECT * FROM sync_logs ORDER BY timestamp DESC LIMIT 100")
    fun getAllSyncLogs(): Flow<List<SyncLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncLog(log: SyncLogEntity)

    @Query("DELETE FROM sync_logs")
    suspend fun clearSyncLogs()

    // Notifications
    @Query("SELECT * FROM app_notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<AppNotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: AppNotificationEntity)

    @Query("UPDATE app_notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: Long)

    @Query("UPDATE app_notifications SET isRead = 1")
    suspend fun markAllNotificationsAsRead()

    @Query("DELETE FROM app_notifications")
    suspend fun clearNotifications()
}
