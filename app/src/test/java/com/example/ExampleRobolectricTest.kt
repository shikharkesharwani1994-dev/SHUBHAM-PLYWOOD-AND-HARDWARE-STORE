package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.SampleData
import com.example.data.model.InvoiceItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Kesharwani Enterprises", appName)
    }

    @Test
    fun `sample data has initial invoices with proper GST`() {
        val invoices = SampleData.createInitialInvoices()
        assertTrue(invoices.isNotEmpty())
        val first = invoices.first()
        assertNotNull(first.invoiceNumber)
        assertTrue(first.totalAmount > 0)
        assertTrue(first.cgstAmount >= 0)
        assertTrue(first.sgstAmount >= 0)
    }

    @Test
    fun `sample inventory items have valid HSN and stock`() {
        val inventory = SampleData.initialInventory
        assertTrue(inventory.size >= 5)
        for (item in inventory) {
            assertTrue(item.hsnCode.isNotBlank())
            assertTrue(item.sellingPrice > item.purchasePrice)
            assertTrue(item.gstRate in listOf(0.0, 5.0, 12.0, 18.0, 28.0))
        }
    }

    @Test
    fun `suppliers and inward supplies exist with valid ledger entries`() {
        val suppliers = SampleData.initialSuppliers
        assertTrue(suppliers.isNotEmpty())
        val inward = SampleData.createInitialInwardSupplies()
        assertTrue(inward.isNotEmpty())
        val ledgers = SampleData.createInitialLedgerEntries()
        assertTrue(ledgers.isNotEmpty())

        val cashEntries = ledgers.filter { it.accountType == "CASH" }
        val bankEntries = ledgers.filter { it.accountType == "BANK" }
        val customerEntries = ledgers.filter { it.accountType == "CUSTOMER" }
        val supplierEntries = ledgers.filter { it.accountType == "SUPPLIER" }

        assertTrue(cashEntries.isNotEmpty())
        assertTrue(bankEntries.isNotEmpty())
        assertTrue(customerEntries.isNotEmpty())
        assertTrue(supplierEntries.isNotEmpty())
    }

    @Test
    fun `export utils generates valid invoice excel file`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val invoice = SampleData.createInitialInvoices().first()
        val company = com.example.data.model.CompanyProfile()

        val excelFile = com.example.util.ExportUtils.generateInvoiceExcel(context, invoice, emptyList(), company)
        assertNotNull(excelFile)
        assertTrue(excelFile!!.exists())
        assertTrue(excelFile.length() > 0)

        val ledgerExcel = com.example.util.ExportUtils.generateLedgerExcel(
            context = context,
            accountName = "Adani_Wilmar",
            accountType = "SUPPLIER",
            entries = SampleData.createInitialLedgerEntries(),
            company = company
        )
        assertNotNull(ledgerExcel)
        assertTrue(ledgerExcel!!.exists())
        assertTrue(ledgerExcel.length() > 0)
    }
}
