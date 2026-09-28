package com.example.data.local

import com.example.data.model.AppNotificationEntity
import com.example.data.model.CustomerEntity
import com.example.data.model.InventoryItemEntity
import com.example.data.model.InvoiceEntity
import com.example.data.model.SyncLogEntity
import org.json.JSONArray
import org.json.JSONObject

object SampleData {
    val initialCustomers = listOf(
        CustomerEntity(
            id = "CUST-001",
            name = "Gupta Provision & Supermarket",
            companyName = "Gupta Retail Stores Pvt Ltd",
            gstin = "09AABCG4532B1ZM",
            phone = "+91 94151 44556",
            email = "guptakirana.prg@gmail.com",
            address = "Shop 12-14, Civil Lines Main Market, Prayagraj 211001",
            stateCode = "09",
            outstandingAmount = 145200.0,
            creditLimit = 300000.0
        ),
        CustomerEntity(
            id = "CUST-002",
            name = "Varanasi Mega Kirana Mart",
            companyName = "Varanasi Food Distributors LLP",
            gstin = "09AAACV8821L1Z9",
            phone = "+91 98390 22334",
            email = "orders@varanasikirana.in",
            address = "Rath Yatra Crossing, Mahmoorganj, Varanasi 221010",
            stateCode = "09",
            outstandingAmount = 88500.0,
            creditLimit = 250000.0
        ),
        CustomerEntity(
            id = "CUST-003",
            name = "Patna Daily Provisions Hub",
            companyName = "Patna Wholesale Mart Ltd",
            gstin = "10AAACP9911K1ZX",
            phone = "+91 93340 99887",
            email = "procure@patnamart.co",
            address = "Fraser Road, Near Dak Bungalow, Patna 800001",
            stateCode = "10", // Inter-State (Bihar)
            outstandingAmount = 0.0,
            creditLimit = 500000.0
        ),
        CustomerEntity(
            id = "CUST-004",
            name = "Sahu Kirana Bhandar (Wholesale Counter)",
            companyName = "Sahu Retailers",
            gstin = "", // Unregistered B2C
            phone = "+91 99350 11223",
            email = "sahu.retail@gmail.com",
            address = "Chowk Bazar, Naini, Prayagraj 211008",
            stateCode = "09",
            outstandingAmount = 18450.0,
            creditLimit = 50000.0
        ),
        CustomerEntity(
            id = "CUST-005",
            name = "Kalka Departmental Stores",
            companyName = "Kalka Retailers",
            gstin = "09AABCK7766D1Z2",
            phone = "+91 98380 77665",
            email = "kalka.stores@gmail.com",
            address = "Birhana Road, Kanpur 208001",
            stateCode = "09",
            outstandingAmount = 45000.0,
            creditLimit = 200000.0
        )
    )

    val initialInventory = listOf(
        InventoryItemEntity(
            id = "FMCG-001",
            name = "Fortune Premium Chakki Fresh Atta (50kg Bag)",
            sku = "ATTA-50K-BAG",
            hsnCode = "11010000",
            category = "Grains & Flour",
            unit = "BAG",
            purchasePrice = 1450.0,
            sellingPrice = 1680.0,
            gstRate = 5.0,
            stockQty = 180.0,
            reorderLevel = 30.0
        ),
        InventoryItemEntity(
            id = "FMCG-002",
            name = "India Gate Feast Rozzana Basmati Rice (25kg Sack)",
            sku = "RICE-BAS-25K",
            hsnCode = "10063020",
            category = "Grains & Rice",
            unit = "BAG",
            purchasePrice = 1850.0,
            sellingPrice = 2150.0,
            gstRate = 5.0,
            stockQty = 120.0,
            reorderLevel = 25.0
        ),
        InventoryItemEntity(
            id = "FMCG-003",
            name = "Gemini Refined Soyabean Cooking Oil (15L Tin)",
            sku = "OIL-SOYA-15L",
            hsnCode = "15079010",
            category = "Edible Oils",
            unit = "TIN",
            purchasePrice = 1620.0,
            sellingPrice = 1850.0,
            gstRate = 5.0,
            stockQty = 6.0, // Low stock!
            reorderLevel = 20.0
        ),
        InventoryItemEntity(
            id = "FMCG-004",
            name = "Tata Tea Gold Granules (Master Carton 24x500g)",
            sku = "TEA-GOLD-24",
            hsnCode = "09024020",
            category = "Beverages",
            unit = "BOX",
            purchasePrice = 5200.0,
            sellingPrice = 6100.0,
            gstRate = 5.0,
            stockQty = 42.0,
            reorderLevel = 15.0
        ),
        InventoryItemEntity(
            id = "FMCG-005",
            name = "Parle-G Glucose Biscuits (Master Case 144 Packs)",
            sku = "BIS-PARLE-144",
            hsnCode = "19053100",
            category = "Bakery & Biscuits",
            unit = "BOX",
            purchasePrice = 1080.0,
            sellingPrice = 1320.0,
            gstRate = 18.0,
            stockQty = 5.0, // Low stock!
            reorderLevel = 15.0
        ),
        InventoryItemEntity(
            id = "FMCG-006",
            name = "Dettol Antiseptic Bathing Soap (Crate 72 Bars)",
            sku = "SOAP-DET-72",
            hsnCode = "34011110",
            category = "Personal Care",
            unit = "BOX",
            purchasePrice = 2200.0,
            sellingPrice = 2650.0,
            gstRate = 18.0,
            stockQty = 38.0,
            reorderLevel = 12.0
        ),
        InventoryItemEntity(
            id = "FMCG-007",
            name = "Maggi 2-Minute Masala Noodles (Master Carton 96 Packs)",
            sku = "FMCG-MAG-96",
            hsnCode = "19023010",
            category = "Instant Foods",
            unit = "BOX",
            purchasePrice = 1120.0,
            sellingPrice = 1350.0,
            gstRate = 12.0,
            stockQty = 75.0,
            reorderLevel = 20.0
        ),
        InventoryItemEntity(
            id = "FMCG-008",
            name = "Surf Excel Easy Wash Detergent Powder (20kg Sack)",
            sku = "DET-SURF-20K",
            hsnCode = "34029011",
            category = "Home Care",
            unit = "BAG",
            purchasePrice = 1950.0,
            sellingPrice = 2380.0,
            gstRate = 18.0,
            stockQty = 4.0, // Low stock!
            reorderLevel = 10.0
        )
    )

    fun createInitialInvoices(): List<InvoiceEntity> {
        val now = System.currentTimeMillis()
        val dayMs = 86400000L

        // Items for Inv 1 (Intra-state UP: 5% & 18% CGST+SGST)
        val items1 = JSONArray().apply {
            put(JSONObject().apply {
                put("id", "1")
                put("itemName", "Fortune Premium Chakki Fresh Atta (50kg Bag)")
                put("hsnCode", "11010000")
                put("quantity", 50.0)
                put("unit", "BAG")
                put("rate", 1680.0)
                put("discountPercent", 2.0)
                put("gstRate", 5.0)
                put("taxableValue", 82320.0)
                put("cgst", 2058.0)
                put("sgst", 2058.0)
                put("igst", 0.0)
                put("total", 86436.0)
            })
            put(JSONObject().apply {
                put("id", "2")
                put("itemName", "Parle-G Glucose Biscuits (Master Case 144 Packs)")
                put("hsnCode", "19053100")
                put("quantity", 20.0)
                put("unit", "BOX")
                put("rate", 1320.0)
                put("discountPercent", 0.0)
                put("gstRate", 18.0)
                put("taxableValue", 26400.0)
                put("cgst", 2376.0)
                put("sgst", 2376.0)
                put("igst", 0.0)
                put("total", 31152.0)
            })
        }

        // Items for Inv 2 (Varanasi: Tea & Soap)
        val items2 = JSONArray().apply {
            put(JSONObject().apply {
                put("id", "1")
                put("itemName", "Tata Tea Gold Granules (Master Carton 24x500g)")
                put("hsnCode", "09024020")
                put("quantity", 10.0)
                put("unit", "BOX")
                put("rate", 6100.0)
                put("discountPercent", 0.0)
                put("gstRate", 5.0)
                put("taxableValue", 61000.0)
                put("cgst", 1525.0)
                put("sgst", 1525.0)
                put("igst", 0.0)
                put("total", 64050.0)
            })
            put(JSONObject().apply {
                put("id", "2")
                put("itemName", "Dettol Antiseptic Bathing Soap (Crate 72 Bars)")
                put("hsnCode", "34011110")
                put("quantity", 8.0)
                put("unit", "BOX")
                put("rate", 2650.0)
                put("discountPercent", 0.0)
                put("gstRate", 18.0)
                put("taxableValue", 21200.0)
                put("cgst", 1908.0)
                put("sgst", 1908.0)
                put("igst", 0.0)
                put("total", 25016.0)
            })
        }

        // Items for Inv 3 (Inter-State Bihar: IGST 5% Basmati Rice bulk)
        val items3 = JSONArray().apply {
            put(JSONObject().apply {
                put("id", "1")
                put("itemName", "India Gate Feast Rozzana Basmati Rice (25kg Sack)")
                put("hsnCode", "10063020")
                put("quantity", 60.0)
                put("unit", "BAG")
                put("rate", 2150.0)
                put("discountPercent", 3.0)
                put("gstRate", 5.0)
                put("taxableValue", 125130.0)
                put("cgst", 0.0)
                put("sgst", 0.0)
                put("igst", 6256.5)
                put("total", 131386.5)
            })
        }

        // Items for Inv 4 (B2C Wholesale: Maggi & Oil)
        val items4 = JSONArray().apply {
            put(JSONObject().apply {
                put("id", "1")
                put("itemName", "Maggi 2-Minute Masala Noodles (Master Carton 96 Packs)")
                put("hsnCode", "19023010")
                put("quantity", 12.0)
                put("unit", "BOX")
                put("rate", 1350.0)
                put("discountPercent", 0.0)
                put("gstRate", 12.0)
                put("taxableValue", 16200.0)
                put("cgst", 972.0)
                put("sgst", 972.0)
                put("igst", 0.0)
                put("total", 18144.0)
            })
        }

        return listOf(
            InvoiceEntity(
                invoiceNumber = "INV-2024-0012",
                customerName = "Gupta Provision & Supermarket",
                customerGstin = "09AABCG4532B1ZM",
                customerPhone = "+91 94151 44556",
                customerEmail = "guptakirana.prg@gmail.com",
                billingAddress = "Shop 12-14, Civil Lines Main Market, Prayagraj 211001",
                placeOfSupply = "09-Uttar Pradesh",
                invoiceDate = now - (2 * dayMs),
                dueDate = now + (13 * dayMs),
                taxableAmount = 108720.0,
                cgstAmount = 4434.0,
                sgstAmount = 4434.0,
                igstAmount = 0.0,
                totalAmount = 117588.0,
                amountPaid = 50000.0,
                status = "PARTIAL",
                isInterState = false,
                notes = "Wholesale delivery verified at Civil Lines godown.",
                itemsJson = items1.toString(),
                syncStatus = "SYNCED",
                createdBy = "Amit Kesharwani",
                createdByRole = "Admin"
            ),
            InvoiceEntity(
                invoiceNumber = "INV-2024-0011",
                customerName = "Varanasi Mega Kirana Mart",
                customerGstin = "09AAACV8821L1Z9",
                customerPhone = "+91 98390 22334",
                customerEmail = "orders@varanasikirana.in",
                billingAddress = "Rath Yatra Crossing, Mahmoorganj, Varanasi 221010",
                placeOfSupply = "09-Uttar Pradesh",
                invoiceDate = now - (19 * dayMs),
                dueDate = now - (4 * dayMs), // Overdue!
                taxableAmount = 82200.0,
                cgstAmount = 3433.0,
                sgstAmount = 3433.0,
                igstAmount = 0.0,
                totalAmount = 89066.0,
                amountPaid = 0.0,
                status = "OVERDUE",
                isInterState = false,
                notes = "Net 15 days credit terms expired. Payment reminder sent.",
                itemsJson = items2.toString(),
                syncStatus = "SYNCED",
                createdBy = "Shikhar Kesharwani",
                createdByRole = "Manager"
            ),
            InvoiceEntity(
                invoiceNumber = "INV-2024-0010",
                customerName = "Patna Daily Provisions Hub",
                customerGstin = "10AAACP9911K1ZX",
                customerPhone = "+91 93340 99887",
                customerEmail = "procure@patnamart.co",
                billingAddress = "Fraser Road, Near Dak Bungalow, Patna 800001",
                placeOfSupply = "10-Bihar",
                invoiceDate = now - (7 * dayMs),
                dueDate = now + (23 * dayMs),
                taxableAmount = 125130.0,
                cgstAmount = 0.0,
                sgstAmount = 0.0,
                igstAmount = 6256.5,
                totalAmount = 131386.5,
                amountPaid = 131386.5,
                status = "PAID",
                isInterState = true,
                notes = "Full settlement received via RTGS UTR: SBIN20240928.",
                itemsJson = items3.toString(),
                syncStatus = "SYNCED",
                createdBy = "Sunil Kumar",
                createdByRole = "Sales"
            ),
            InvoiceEntity(
                invoiceNumber = "INV-2024-0009",
                customerName = "Sahu Kirana Bhandar (Wholesale Counter)",
                customerGstin = "",
                customerPhone = "+91 99350 11223",
                customerEmail = "sahu.retail@gmail.com",
                billingAddress = "Chowk Bazar, Naini, Prayagraj 211008",
                placeOfSupply = "09-Uttar Pradesh",
                invoiceDate = now - (1 * dayMs),
                dueDate = now + (5 * dayMs),
                taxableAmount = 16200.0,
                cgstAmount = 972.0,
                sgstAmount = 972.0,
                igstAmount = 0.0,
                totalAmount = 18144.0,
                amountPaid = 0.0,
                status = "PENDING",
                isInterState = false,
                notes = "Direct cash counter supply with 12% GST.",
                itemsJson = items4.toString(),
                syncStatus = "SYNCED",
                createdBy = "Sunil Kumar",
                createdByRole = "Sales"
            )
        )
    }

    val initialSyncLogs = listOf(
        SyncLogEntity(
            timestamp = System.currentTimeMillis() - 120000,
            userName = "Amit Kesharwani",
            userRole = "Admin",
            action = "FIREBASE_AUTH_SYNC",
            details = "Operator authenticated via Firebase Auth & Credential Manager",
            deviceId = "Device: Android-Pixel-8",
            isSynced = true
        ),
        SyncLogEntity(
            timestamp = System.currentTimeMillis() - 720000,
            userName = "Rakesh Agrawal",
            userRole = "Accountant",
            action = "GSTR1_EXPORTED",
            details = "FMCG GSTR-1 JSON summary generated for September",
            deviceId = "Device: Desktop-CA-Workstation",
            isSynced = true
        ),
        SyncLogEntity(
            timestamp = System.currentTimeMillis() - 1800000,
            userName = "Shikhar Kesharwani",
            userRole = "Manager",
            action = "INVENTORY_RESTOCK",
            details = "Restocked 100 bags of Fortune Chakki Atta (50kg)",
            deviceId = "Device: Mandi-Warehouse-Tab",
            isSynced = true
        ),
        SyncLogEntity(
            timestamp = System.currentTimeMillis() - 3600000,
            userName = "Sunil Kumar",
            userRole = "Sales",
            action = "INVOICE_CREATED",
            details = "Generated INV-2024-0012 for Gupta Provision",
            deviceId = "Device: Mobile-Billing-S24",
            isSynced = true
        )
    )

    val initialNotifications = listOf(
        AppNotificationEntity(
            title = "Payment Overdue: Varanasi Mega Kirana",
            message = "Invoice INV-2024-0011 for ₹89,066 is 4 days overdue. Tap to send instant WhatsApp reminder.",
            category = "PAYMENT",
            timestamp = System.currentTimeMillis() - 3600000,
            isRead = false,
            urgency = "HIGH"
        ),
        AppNotificationEntity(
            title = "Low Stock Alert: Cooking Oil & Biscuits",
            message = "Gemini Soyabean Oil (6 tins left) and Parle-G (5 cases left) have reached critical threshold.",
            category = "STOCK",
            timestamp = System.currentTimeMillis() - 14400000,
            isRead = false,
            urgency = "NORMAL"
        ),
        AppNotificationEntity(
            title = "Firebase Auth Ready",
            message = "Google Sign-In and Firebase Auth are integrated with Android Credential Manager.",
            category = "SYSTEM",
            timestamp = System.currentTimeMillis() - 86400000,
            isRead = true,
            urgency = "LOW"
        )
    )

    val initialSuppliers = listOf(
        com.example.data.model.SupplierEntity(
            id = "SUP-001",
            name = "Adani Wilmar Ltd (Fortune Mill)",
            companyName = "Adani Wilmar Limited",
            gstin = "24AAACA1234D1Z2",
            phone = "+91 79255 12345",
            email = "sales@adaniwilmar.in",
            address = "Fortune House, Navrangpura, Ahmedabad 380009",
            stateCode = "24",
            payableBalance = 125000.0,
            creditPeriodDays = 21
        ),
        com.example.data.model.SupplierEntity(
            id = "SUP-002",
            name = "Tata Consumer Products Ltd",
            companyName = "Tata Consumer Products Limited",
            gstin = "19AAACT9876K1Z1",
            phone = "+91 33228 83000",
            email = "wholesale@tataconsumer.com",
            address = "1 Bishop Lefroy Road, Kolkata 700020",
            stateCode = "19",
            payableBalance = 64200.0,
            creditPeriodDays = 30
        ),
        com.example.data.model.SupplierEntity(
            id = "SUP-003",
            name = "Parle Products Pvt Ltd",
            companyName = "Parle Products Private Limited",
            gstin = "27AAACP0123M1ZU",
            phone = "+91 22669 11000",
            email = "trade@parle.biz",
            address = "North Level Crossing, Vile Parle East, Mumbai 400057",
            stateCode = "27",
            payableBalance = 28400.0,
            creditPeriodDays = 15
        ),
        com.example.data.model.SupplierEntity(
            id = "SUP-004",
            name = "Vindhya Agro Grain Mills (Local Mill)",
            companyName = "Vindhya Agro Processors",
            gstin = "09AAAFV5432G1ZP",
            phone = "+91 94150 99881",
            email = "vindhya.grains@gmail.com",
            address = "Industrial Area, Naini, Prayagraj 211008",
            stateCode = "09",
            payableBalance = 42000.0,
            creditPeriodDays = 10
        )
    )

    fun createInitialInwardSupplies(): List<com.example.data.model.InwardSupplyEntity> {
        val now = System.currentTimeMillis()
        val dayMs = 86400000L

        val items1 = JSONArray().apply {
            put(JSONObject().apply {
                put("id", "1")
                put("itemName", "Fortune Premium Chakki Fresh Atta (50kg Bag)")
                put("hsnCode", "11010000")
                put("quantity", 100.0)
                put("unit", "BAG")
                put("rate", 1450.0)
                put("discountPercent", 0.0)
                put("gstRate", 5.0)
                put("taxableValue", 145000.0)
                put("cgst", 0.0)
                put("sgst", 0.0)
                put("igst", 7250.0)
                put("total", 152250.0)
            })
            put(JSONObject().apply {
                put("id", "2")
                put("itemName", "Gemini Refined Soyabean Cooking Oil (15L Tin)")
                put("hsnCode", "15079010")
                put("quantity", 20.0)
                put("unit", "TIN")
                put("rate", 1620.0)
                put("discountPercent", 0.0)
                put("gstRate", 5.0)
                put("taxableValue", 32400.0)
                put("cgst", 0.0)
                put("sgst", 0.0)
                put("igst", 1620.0)
                put("total", 34020.0)
            })
        }

        val items2 = JSONArray().apply {
            put(JSONObject().apply {
                put("id", "1")
                put("itemName", "Tata Tea Gold Granules (Master Carton 24x500g)")
                put("hsnCode", "09024020")
                put("quantity", 10.0)
                put("unit", "BOX")
                put("rate", 5200.0)
                put("discountPercent", 0.0)
                put("gstRate", 5.0)
                put("taxableValue", 52000.0)
                put("cgst", 0.0)
                put("sgst", 0.0)
                put("igst", 2600.0)
                put("total", 54600.0)
            })
        }

        return listOf(
            com.example.data.model.InwardSupplyEntity(
                billNumber = "PUR-2024-0045",
                supplierId = "SUP-001",
                supplierName = "Adani Wilmar Ltd (Fortune Mill)",
                supplierGstin = "24AAACA1234D1Z2",
                billDate = now - (5 * dayMs),
                dueDate = now + (16 * dayMs),
                taxableAmount = 177400.0,
                cgstAmount = 0.0,
                sgstAmount = 0.0,
                igstAmount = 8870.0,
                totalAmount = 186270.0,
                amountPaid = 61270.0,
                status = "PARTIAL",
                isInterState = true,
                itcEligible = true,
                itemsJson = items1.toString(),
                paymentMode = "BANK",
                notes = "Truck load delivered at G.T. Road godown. Unloading complete.",
                createdBy = "Amit Kesharwani"
            ),
            com.example.data.model.InwardSupplyEntity(
                billNumber = "PUR-2024-0044",
                supplierId = "SUP-002",
                supplierName = "Tata Consumer Products Ltd",
                supplierGstin = "19AAACT9876K1Z1",
                billDate = now - (14 * dayMs),
                dueDate = now + (16 * dayMs),
                taxableAmount = 52000.0,
                cgstAmount = 0.0,
                sgstAmount = 0.0,
                igstAmount = 2600.0,
                totalAmount = 54600.0,
                amountPaid = 54600.0,
                status = "PAID",
                isInterState = true,
                itcEligible = true,
                itemsJson = items2.toString(),
                paymentMode = "BANK",
                notes = "Settled via SBI RTGS Ref: SBIN993817.",
                createdBy = "Shikhar Kesharwani"
            ),
            com.example.data.model.InwardSupplyEntity(
                billNumber = "PUR-2024-0043",
                supplierId = "SUP-004",
                supplierName = "Vindhya Agro Grain Mills (Local Mill)",
                supplierGstin = "09AAAFV5432G1ZP",
                billDate = now - (2 * dayMs),
                dueDate = now + (8 * dayMs),
                taxableAmount = 40000.0,
                cgstAmount = 1000.0,
                sgstAmount = 1000.0,
                igstAmount = 0.0,
                totalAmount = 42000.0,
                amountPaid = 0.0,
                status = "PENDING",
                isInterState = false,
                itcEligible = true,
                itemsJson = "",
                paymentMode = "CREDIT",
                notes = "Local milling paddy delivery.",
                createdBy = "Sunil Kumar"
            )
        )
    }

    fun createInitialLedgerEntries(): List<com.example.data.model.LedgerEntryEntity> {
        val now = System.currentTimeMillis()
        val dayMs = 86400000L

        return listOf(
            // Customer Ledger entries (Outward Supply + Receipts)
            com.example.data.model.LedgerEntryEntity(
                date = now - (2 * dayMs),
                accountType = "CUSTOMER",
                accountId = "CUST-001",
                accountName = "Gupta Provision & Supermarket",
                voucherType = "SALES",
                voucherNumber = "INV-2024-0012",
                debit = 117588.0,
                credit = 0.0,
                particulars = "To Sales A/c (FMCG Atta & Biscuits Supply)",
                paymentMethod = "BANK",
                balanceAfter = 145200.0
            ),
            com.example.data.model.LedgerEntryEntity(
                date = now - (1 * dayMs),
                accountType = "CUSTOMER",
                accountId = "CUST-001",
                accountName = "Gupta Provision & Supermarket",
                voucherType = "RECEIPT",
                voucherNumber = "RCPT-2024-082",
                debit = 0.0,
                credit = 50000.0,
                particulars = "By SBI Bank UPI Settlement (Ref: 3094821)",
                paymentMethod = "BANK_UPI",
                balanceAfter = 95200.0
            ),

            // Supplier Ledger entries (Inward Supply + Payments)
            com.example.data.model.LedgerEntryEntity(
                date = now - (5 * dayMs),
                accountType = "SUPPLIER",
                accountId = "SUP-001",
                accountName = "Adani Wilmar Ltd (Fortune Mill)",
                voucherType = "PURCHASE",
                voucherNumber = "PUR-2024-0045",
                debit = 0.0,
                credit = 186270.0,
                particulars = "By Inward Supply (Atta & Oil Bulk)",
                paymentMethod = "BANK",
                balanceAfter = 186270.0
            ),
            com.example.data.model.LedgerEntryEntity(
                date = now - (3 * dayMs),
                accountType = "SUPPLIER",
                accountId = "SUP-001",
                accountName = "Adani Wilmar Ltd (Fortune Mill)",
                voucherType = "PAYMENT",
                voucherNumber = "PAY-2024-031",
                debit = 61270.0,
                credit = 0.0,
                particulars = "To SBI Bank RTGS Transfer (Ref: SBIN493021)",
                paymentMethod = "BANK_NEFT",
                balanceAfter = 125000.0
            ),

            // Cash Ledger (Cash Book)
            com.example.data.model.LedgerEntryEntity(
                date = now - (1 * dayMs),
                accountType = "CASH",
                accountId = "CASH_A_C",
                accountName = "Cash in Hand A/c",
                voucherType = "RECEIPT",
                voucherNumber = "CSH-RCPT-102",
                debit = 18144.0, // Cash in
                credit = 0.0,
                particulars = "Cash Counter Sale - Sahu Kirana INV-2024-0009",
                paymentMethod = "CASH",
                balanceAfter = 68450.0
            ),
            com.example.data.model.LedgerEntryEntity(
                date = now - (6 * 3600000L),
                accountType = "CASH",
                accountId = "CASH_A_C",
                accountName = "Cash in Hand A/c",
                voucherType = "EXPENSE",
                voucherNumber = "EXP-2024-019",
                debit = 0.0,
                credit = 3500.0, // Cash out
                particulars = "Godown Labor & Mandi Loading Wages",
                paymentMethod = "CASH",
                balanceAfter = 64950.0
            ),

            // Bank Ledger (SBI Bank Book)
            com.example.data.model.LedgerEntryEntity(
                date = now - (1 * dayMs),
                accountType = "BANK",
                accountId = "SBI_BANK_A_C",
                accountName = "State Bank of India (30920014567890)",
                voucherType = "RECEIPT",
                voucherNumber = "BNK-REC-301",
                debit = 50000.0, // Bank in (Deposit/UPI)
                credit = 0.0,
                particulars = "UPI Inward: Gupta Provision (INV-2024-0012)",
                paymentMethod = "BANK_UPI",
                balanceAfter = 412500.0
            ),
            com.example.data.model.LedgerEntryEntity(
                date = now - (3 * dayMs),
                accountType = "BANK",
                accountId = "SBI_BANK_A_C",
                accountName = "State Bank of India (30920014567890)",
                voucherType = "PAYMENT",
                voucherNumber = "BNK-PAY-209",
                debit = 0.0,
                credit = 61270.0, // Bank out
                particulars = "NEFT Payment: Adani Wilmar Ltd",
                paymentMethod = "BANK_NEFT",
                balanceAfter = 362500.0
            ),
            com.example.data.model.LedgerEntryEntity(
                date = now - (12 * 3600000L),
                accountType = "BANK",
                accountId = "SBI_BANK_A_C",
                accountName = "State Bank of India (30920014567890)",
                voucherType = "CONTRA",
                voucherNumber = "BNK-DEP-012",
                debit = 25000.0, // Cash deposited into bank
                credit = 0.0,
                particulars = "Cash Deposited in SBI Mandi Branch (Contra)",
                paymentMethod = "CASH",
                balanceAfter = 387500.0
            )
        )
    }
}
