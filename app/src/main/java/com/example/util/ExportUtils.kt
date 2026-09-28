package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.CompanyProfile
import com.example.data.model.InvoiceEntity
import com.example.data.model.InvoiceItem
import com.example.data.model.LedgerEntryEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportUtils {

    /**
     * Generate an authentic A4 PDF Tax Invoice with Kesharwani Enterprises header,
     * itemized GST breakdown, bank details, and totals.
     */
    fun generateInvoicePdf(
        context: Context,
        invoice: InvoiceEntity,
        items: List<InvoiceItem>,
        company: CompanyProfile
    ): File? {
        return try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 points (72 dpi)
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val paint = Paint().apply { isAntiAlias = true }
            val primaryColor = Color.rgb(15, 41, 74) // Deep Navy
            val darkTextColor = Color.rgb(30, 41, 59)
            val grayTextColor = Color.rgb(100, 116, 139)
            val lightBgColor = Color.rgb(241, 245, 249)
            val accentGreen = Color.rgb(16, 185, 129)

            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            val invoiceDateStr = sdf.format(Date(invoice.invoiceDate))
            val dueDateStr = sdf.format(Date(invoice.dueDate))

            // 1. Header Band
            paint.color = primaryColor
            canvas.drawRect(0f, 0f, 595f, 75f, paint)

            paint.color = Color.WHITE
            paint.textSize = 18f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(company.tradeName, 24f, 32f, paint)

            paint.textSize = 9.5f
            paint.typeface = Typeface.DEFAULT
            canvas.drawText("FMCG Wholesale & Distribution | GSTIN: ${company.gstin} | PAN: ${company.pan}", 24f, 48f, paint)
            canvas.drawText("${company.address} | Phone: ${company.phone}", 24f, 62f, paint)

            // Tax Invoice badge
            paint.color = Color.rgb(245, 158, 11) // Amber
            paint.textSize = 13f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val titleText = "TAX INVOICE"
            canvas.drawText(titleText, 480f, 34f, paint)

            paint.color = Color.WHITE
            paint.textSize = 9f
            paint.typeface = Typeface.DEFAULT
            canvas.drawText(if (invoice.isInterState) "INTER-STATE (IGST)" else "INTRA-STATE (CGST+SGST)", 440f, 50f, paint)

            // 2. Invoice Meta & Bill To Box
            var y = 92f
            paint.color = lightBgColor
            canvas.drawRoundRect(20f, y, 575f, y + 68f, 6f, 6f, paint)

            // Left: Invoice Meta
            paint.color = darkTextColor
            paint.textSize = 9f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Invoice No: ", 30f, y + 16f, paint)
            paint.typeface = Typeface.DEFAULT
            canvas.drawText(invoice.invoiceNumber, 85f, y + 16f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Date: ", 30f, y + 32f, paint)
            paint.typeface = Typeface.DEFAULT
            canvas.drawText(invoiceDateStr, 65f, y + 32f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Due Date: ", 30f, y + 48f, paint)
            paint.typeface = Typeface.DEFAULT
            canvas.drawText(dueDateStr, 80f, y + 48f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Place of Supply: ", 30f, y + 62f, paint)
            paint.typeface = Typeface.DEFAULT
            canvas.drawText(invoice.placeOfSupply, 105f, y + 62f, paint)

            // Right: Billed To Customer
            val custX = 300f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.color = primaryColor
            canvas.drawText("BILLED TO (CUSTOMER):", custX, y + 16f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.color = darkTextColor
            canvas.drawText(invoice.customerName, custX, y + 30f, paint)

            paint.typeface = Typeface.DEFAULT
            paint.color = grayTextColor
            val gstinText = if (invoice.customerGstin.isNotBlank()) "GSTIN: ${invoice.customerGstin}" else "Unregistered B2C Consumer"
            canvas.drawText(gstinText, custX, y + 44f, paint)
            canvas.drawText("Address: ${invoice.billingAddress.take(45)}", custX, y + 56f, paint)

            // 3. Table Header
            y += 82f
            paint.color = primaryColor
            canvas.drawRect(20f, y, 575f, y + 20f, paint)

            paint.color = Color.WHITE
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("#", 26f, y + 14f, paint)
            canvas.drawText("Item & Description", 44f, y + 14f, paint)
            canvas.drawText("HSN", 250f, y + 14f, paint)
            canvas.drawText("Qty", 295f, y + 14f, paint)
            canvas.drawText("Rate", 345f, y + 14f, paint)
            canvas.drawText("GST %", 390f, y + 14f, paint)
            canvas.drawText("Taxable", 440f, y + 14f, paint)
            canvas.drawText("Total (₹)", 515f, y + 14f, paint)

            // 4. Table Rows
            y += 20f
            paint.typeface = Typeface.DEFAULT
            paint.textSize = 8.5f

            items.forEachIndexed { index, item ->
                paint.color = if (index % 2 == 0) Color.WHITE else Color.rgb(248, 250, 252)
                canvas.drawRect(20f, y, 575f, y + 18f, paint)

                paint.color = darkTextColor
                canvas.drawText("${index + 1}", 26f, y + 13f, paint)
                val displayName = if (item.itemName.length > 34) item.itemName.take(32) + ".." else item.itemName
                canvas.drawText(displayName, 44f, y + 13f, paint)
                canvas.drawText(item.hsnCode, 250f, y + 13f, paint)
                canvas.drawText("${item.quantity} ${item.unit}", 295f, y + 13f, paint)
                canvas.drawText(String.format(Locale.US, "%.1f", item.rate), 345f, y + 13f, paint)
                canvas.drawText("${item.gstRate}%", 390f, y + 13f, paint)
                canvas.drawText(String.format(Locale.US, "%.2f", item.taxableValue), 440f, y + 13f, paint)
                canvas.drawText(String.format(Locale.US, "%.2f", item.total), 515f, y + 13f, paint)

                y += 18f
            }

            // Divider
            paint.color = Color.rgb(203, 213, 225)
            paint.strokeWidth = 1f
            canvas.drawLine(20f, y, 575f, y, paint)

            // 5. Totals & Tax Breakup Box
            y += 12f
            // Left: Bank Account details & Terms
            paint.color = lightBgColor
            canvas.drawRoundRect(20f, y, 320f, y + 105f, 6f, 6f, paint)

            paint.color = primaryColor
            paint.textSize = 9.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("BANK DETAILS FOR SETTLEMENT:", 30f, y + 18f, paint)

            paint.color = darkTextColor
            paint.textSize = 8.5f
            paint.typeface = Typeface.DEFAULT
            canvas.drawText("Bank: ${company.bankName}", 30f, y + 34f, paint)
            canvas.drawText("A/C No: ${company.bankAccount}", 30f, y + 48f, paint)
            canvas.drawText("IFSC Code: ${company.ifscCode} | Branch: Mandi Branch", 30f, y + 62f, paint)
            canvas.drawText("UPI ID: ${company.upiId}", 30f, y + 76f, paint)
            paint.color = grayTextColor
            paint.textSize = 7.5f
            canvas.drawText("Subject to Prayagraj jurisdiction. Interest @ 18% p.a. on overdue.", 30f, y + 94f, paint)

            // Right: Tax & Grand Total
            paint.color = darkTextColor
            paint.textSize = 9f
            paint.typeface = Typeface.DEFAULT

            val rightX = 350f
            val valX = 490f

            canvas.drawText("Taxable Subtotal:", rightX, y + 16f, paint)
            canvas.drawText(String.format(Locale.US, "₹ %,.2f", invoice.taxableAmount), valX, y + 16f, paint)

            if (invoice.isInterState) {
                canvas.drawText("Output IGST:", rightX, y + 32f, paint)
                canvas.drawText(String.format(Locale.US, "₹ %,.2f", invoice.igstAmount), valX, y + 32f, paint)
            } else {
                canvas.drawText("Output CGST:", rightX, y + 32f, paint)
                canvas.drawText(String.format(Locale.US, "₹ %,.2f", invoice.cgstAmount), valX, y + 32f, paint)
                canvas.drawText("Output SGST:", rightX, y + 48f, paint)
                canvas.drawText(String.format(Locale.US, "₹ %,.2f", invoice.sgstAmount), valX, y + 48f, paint)
            }

            paint.color = primaryColor
            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("GRAND TOTAL:", rightX, y + 72f, paint)
            canvas.drawText(String.format(Locale.US, "₹ %,.2f", invoice.totalAmount), valX, y + 72f, paint)

            paint.color = if (invoice.amountPaid >= invoice.totalAmount) accentGreen else Color.rgb(225, 29, 72)
            paint.textSize = 9.5f
            val due = (invoice.totalAmount - invoice.amountPaid).coerceAtLeast(0.0)
            canvas.drawText("Paid: ₹ %,.2f | Balance Due: ₹ %,.2f".format(Locale.US, invoice.amountPaid, due), rightX, y + 90f, paint)

            // 6. Signatory
            y += 120f
            paint.color = grayTextColor
            paint.textSize = 8.5f
            paint.typeface = Typeface.DEFAULT
            canvas.drawText("This is a computer generated GST invoice under Rule 46 of CGST Rules 2017.", 20f, y + 16f, paint)

            paint.color = darkTextColor
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("For Kesharwani Enterprises", 420f, y + 16f, paint)
            paint.typeface = Typeface.DEFAULT
            canvas.drawText("Authorized Signatory", 440f, y + 45f, paint)

            pdfDocument.finishPage(page)

            // Save PDF to cache
            val file = File(context.cacheDir, "Invoice_${invoice.invoiceNumber}.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Generate an Excel (.csv) file for an invoice.
     */
    fun generateInvoiceExcel(
        context: Context,
        invoice: InvoiceEntity,
        items: List<InvoiceItem>,
        company: CompanyProfile
    ): File? {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            val sb = StringBuilder()
            sb.append("TAX INVOICE - ${company.tradeName}\n")
            sb.append("FMCG Wholesale & Retail Mandi, Prayagraj, UP\n")
            sb.append("GSTIN: ${company.gstin}, State: ${company.stateName} (Code: ${company.stateCode})\n\n")

            sb.append("Invoice Number,${invoice.invoiceNumber},Invoice Date,${sdf.format(Date(invoice.invoiceDate))}\n")
            sb.append("Due Date,${sdf.format(Date(invoice.dueDate))},Place of Supply,${invoice.placeOfSupply}\n")
            sb.append("Customer Name,\"${invoice.customerName}\",Customer GSTIN,${invoice.customerGstin.ifBlank { "Unregistered" }}\n")
            sb.append("Billing Address,\"${invoice.billingAddress}\",Customer Phone,${invoice.customerPhone}\n\n")

            sb.append("#,Item Name,HSN/SAC,Qty,Unit,Rate,Discount %,Taxable Value,GST %,CGST,SGST,IGST,Total Amount\n")
            items.forEachIndexed { i, it ->
                sb.append("${i + 1},\"${it.itemName}\",${it.hsnCode},${it.quantity},${it.unit},${it.rate},${it.discountPercent},${String.format(Locale.US, "%.2f", it.taxableValue)},${it.gstRate}%,${String.format(Locale.US, "%.2f", it.cgst)},${String.format(Locale.US, "%.2f", it.sgst)},${String.format(Locale.US, "%.2f", it.igst)},${String.format(Locale.US, "%.2f", it.total)}\n")
            }

            sb.append("\n,,,,,Subtotal Taxable,${String.format(Locale.US, "%.2f", invoice.taxableAmount)},CGST,${String.format(Locale.US, "%.2f", invoice.cgstAmount)},SGST,${String.format(Locale.US, "%.2f", invoice.sgstAmount)},IGST,${String.format(Locale.US, "%.2f", invoice.igstAmount)},Grand Total,${String.format(Locale.US, "%.2f", invoice.totalAmount)}\n")
            sb.append(",,,,,Amount Paid,${String.format(Locale.US, "%.2f", invoice.amountPaid)},Balance Due,${String.format(Locale.US, "%.2f", invoice.totalAmount - invoice.amountPaid)}\n\n")

            sb.append("Bank Name,${company.bankName},A/C Number,${company.bankAccount}\n")
            sb.append("IFSC Code,${company.ifscCode},UPI ID,${company.upiId}\n")

            val file = File(context.cacheDir, "Invoice_${invoice.invoiceNumber}.csv")
            file.writeText(sb.toString())
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Generate an Excel (.csv) file for Statement of Account / Ledger (Customer, Supplier, Cash, Bank).
     */
    fun generateLedgerExcel(
        context: Context,
        accountName: String,
        accountType: String,
        entries: List<LedgerEntryEntity>,
        company: CompanyProfile
    ): File? {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)
            val sb = StringBuilder()
            sb.append("STATEMENT OF ACCOUNT: ${accountName.uppercase()}\n")
            sb.append("Account Type: $accountType | Company: ${company.tradeName} (GSTIN: ${company.gstin})\n\n")

            sb.append("Date,Voucher Type,Voucher No,Particulars,Debit (Dr),Credit (Cr),Payment Mode,Running Balance\n")
            for (e in entries) {
                sb.append("${sdf.format(Date(e.date))},${e.voucherType},${e.voucherNumber},\"${e.particulars}\",${String.format(Locale.US, "%.2f", e.debit)},${String.format(Locale.US, "%.2f", e.credit)},${e.paymentMethod},${String.format(Locale.US, "%.2f", e.balanceAfter)}\n")
            }

            val totalDr = entries.sumOf { it.debit }
            val totalCr = entries.sumOf { it.credit }
            sb.append("\nTotal,,,\"TOTALS\",${String.format(Locale.US, "%.2f", totalDr)},${String.format(Locale.US, "%.2f", totalCr)},,Net: ${String.format(Locale.US, "%.2f", totalDr - totalCr)}\n")

            val cleanName = accountName.replace(Regex("[^a-zA-Z0-9]"), "_")
            val file = File(context.cacheDir, "Ledger_${cleanName}.csv")
            file.writeText(sb.toString())
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Generate PDF for Statement of Account (Ledger).
     */
    fun generateLedgerPdf(
        context: Context,
        accountName: String,
        accountType: String,
        entries: List<LedgerEntryEntity>,
        company: CompanyProfile
    ): File? {
        return try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val paint = Paint().apply { isAntiAlias = true }
            val primaryColor = Color.rgb(15, 41, 74)
            val darkTextColor = Color.rgb(30, 41, 59)
            val grayTextColor = Color.rgb(100, 116, 139)

            // Header
            paint.color = primaryColor
            canvas.drawRect(0f, 0f, 595f, 65f, paint)

            paint.color = Color.WHITE
            paint.textSize = 16f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(company.tradeName, 24f, 28f, paint)

            paint.textSize = 9f
            paint.typeface = Typeface.DEFAULT
            canvas.drawText("STATEMENT OF ACCOUNT: $accountName ($accountType)", 24f, 44f, paint)
            canvas.drawText("Company GSTIN: ${company.gstin} | As on: ${SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date())}", 24f, 56f, paint)

            // Table Header
            var y = 85f
            paint.color = primaryColor
            canvas.drawRect(20f, y, 575f, y + 20f, paint)

            paint.color = Color.WHITE
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Date", 25f, y + 14f, paint)
            canvas.drawText("Type", 95f, y + 14f, paint)
            canvas.drawText("Voucher #", 150f, y + 14f, paint)
            canvas.drawText("Particulars", 225f, y + 14f, paint)
            canvas.drawText("Debit (Dr)", 390f, y + 14f, paint)
            canvas.drawText("Credit (Cr)", 460f, y + 14f, paint)
            canvas.drawText("Balance", 525f, y + 14f, paint)

            y += 20f
            val sdf = SimpleDateFormat("dd/MM/yy", Locale.US)
            paint.typeface = Typeface.DEFAULT
            paint.textSize = 8f

            entries.take(35).forEachIndexed { i, e ->
                paint.color = if (i % 2 == 0) Color.WHITE else Color.rgb(248, 250, 252)
                canvas.drawRect(20f, y, 575f, y + 17f, paint)

                paint.color = darkTextColor
                canvas.drawText(sdf.format(Date(e.date)), 25f, y + 12f, paint)
                canvas.drawText(e.voucherType, 95f, y + 12f, paint)
                canvas.drawText(e.voucherNumber.take(12), 150f, y + 12f, paint)
                val part = if (e.particulars.length > 28) e.particulars.take(26) + ".." else e.particulars
                canvas.drawText(part, 225f, y + 12f, paint)
                canvas.drawText(if (e.debit > 0) String.format(Locale.US, "%.1f", e.debit) else "-", 390f, y + 12f, paint)
                canvas.drawText(if (e.credit > 0) String.format(Locale.US, "%.1f", e.credit) else "-", 460f, y + 12f, paint)
                canvas.drawText(String.format(Locale.US, "%.1f", e.balanceAfter), 525f, y + 12f, paint)

                y += 17f
            }

            // Summary Totals
            y += 15f
            paint.color = Color.rgb(241, 245, 249)
            canvas.drawRoundRect(20f, y, 575f, y + 36f, 6f, 6f, paint)

            val totalDr = entries.sumOf { it.debit }
            val totalCr = entries.sumOf { it.credit }
            paint.color = primaryColor
            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Total Debit: ₹ %,.2f".format(Locale.US, totalDr), 35f, y + 22f, paint)
            canvas.drawText("Total Credit: ₹ %,.2f".format(Locale.US, totalCr), 220f, y + 22f, paint)
            canvas.drawText("Net Balance: ₹ %,.2f".format(Locale.US, totalDr - totalCr), 410f, y + 22f, paint)

            pdfDocument.finishPage(page)

            val cleanName = accountName.replace(Regex("[^a-zA-Z0-9]"), "_")
            val file = File(context.cacheDir, "Ledger_${cleanName}.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Share any generated file (PDF or Excel/CSV) using FileProvider.
     */
    fun shareExportedFile(context: Context, file: File, mimeType: String, chooserTitle: String) {
        try {
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, chooserTitle))
        } catch (e: Exception) {
            Toast.makeText(context, "Error sharing file: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
}
