package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AppNotificationEntity
import com.example.data.model.CustomerEntity
import com.example.data.model.InventoryItemEntity
import com.example.data.model.InvoiceEntity
import com.example.data.model.InwardSupplyEntity
import com.example.data.model.LedgerEntryEntity
import com.example.data.model.SupplierEntity
import com.example.data.model.SyncLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        InvoiceEntity::class,
        CustomerEntity::class,
        SupplierEntity::class,
        InwardSupplyEntity::class,
        LedgerEntryEntity::class,
        InventoryItemEntity::class,
        SyncLogEntity::class,
        AppNotificationEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gst_pro_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.appDao())
                    }
                }
            }

            private suspend fun populateInitialData(dao: AppDao) {
                dao.insertCustomers(SampleData.initialCustomers)
                dao.insertSuppliers(SampleData.initialSuppliers)
                dao.insertInventoryItems(SampleData.initialInventory)
                dao.insertInvoices(SampleData.createInitialInvoices())
                dao.insertInwardSupplies(SampleData.createInitialInwardSupplies())
                dao.insertLedgerEntries(SampleData.createInitialLedgerEntries())
                for (log in SampleData.initialSyncLogs) {
                    dao.insertSyncLog(log)
                }
                for (notif in SampleData.initialNotifications) {
                    dao.insertNotification(notif)
                }
            }
        }
    }
}
