package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.PrimaryBlue

enum class ScannerMode {
    BARCODE,
    INVOICE
}

data class ScannedBarcodeItem(
    val barcode: String,
    val name: String,
    val sku: String,
    val hsnCode: String,
    val rate: Double,
    val gstRate: Double,
    val unit: String
)

data class ScannedInvoiceData(
    val invoiceNumber: String,
    val partyName: String,
    val gstin: String,
    val amount: Double,
    val date: String,
    val description: String
)

object ScannerPresets {
    val fmcgBarcodes = listOf(
        ScannedBarcodeItem(
            barcode = "8901063001234",
            name = "Parle-G Glucose Biscuits (Master Case 144 Packs)",
            sku = "BIS-PARLE-144",
            hsnCode = "19053100",
            rate = 1320.0,
            gstRate = 18.0,
            unit = "BOX"
        ),
        ScannedBarcodeItem(
            barcode = "8901030383020",
            name = "Tata Tea Gold Granules (Master Carton 24x500g)",
            sku = "TEA-GOLD-24",
            hsnCode = "09024020",
            rate = 6100.0,
            gstRate = 5.0,
            unit = "BOX"
        ),
        ScannedBarcodeItem(
            barcode = "8906007281012",
            name = "Fortune Premium Chakki Fresh Atta (50kg Bag)",
            sku = "FLOUR-CHAKKI-50K",
            hsnCode = "11010000",
            rate = 1680.0,
            gstRate = 5.0,
            unit = "BAG"
        ),
        ScannedBarcodeItem(
            barcode = "8901499009821",
            name = "Gemini Refined Soyabean Cooking Oil (15L Tin)",
            sku = "OIL-SOYA-15L",
            hsnCode = "15079010",
            rate = 1850.0,
            gstRate = 5.0,
            unit = "TIN"
        ),
        ScannedBarcodeItem(
            barcode = "8901058852331",
            name = "Maggi 2-Minute Masala Noodles (72 Pack Case)",
            sku = "NOD-MAGGI-72",
            hsnCode = "19023010",
            rate = 980.0,
            gstRate = 12.0,
            unit = "BOX"
        ),
        ScannedBarcodeItem(
            barcode = "8901030691231",
            name = "Surf Excel Easy Wash Detergent (10x1kg Bag)",
            sku = "DET-SURF-10K",
            hsnCode = "34022010",
            rate = 1420.0,
            gstRate = 18.0,
            unit = "BAG"
        )
    )

    val sampleInvoices = listOf(
        ScannedInvoiceData(
            invoiceNumber = "ITC-UP-8921",
            partyName = "ITC Limited (Wholesale Division)",
            gstin = "09AAACI1681G1ZM",
            amount = 128450.0,
            date = "2024-11-04",
            description = "Inward Supply: Aashirvaad Atta, Sunfeast, Yippee"
        ),
        ScannedInvoiceData(
            invoiceNumber = "HUL-NOIDA-4421",
            partyName = "Hindustan Unilever Limited",
            gstin = "09AAACH1773L1Z4",
            amount = 94300.0,
            date = "2024-11-03",
            description = "Inward Supply: Lifebuoy, Surf Excel, Dove"
        ),
        ScannedInvoiceData(
            invoiceNumber = "INV-2024-0089",
            partyName = "Gupta Provision & Supermarket",
            gstin = "09AABCG4532B1ZM",
            amount = 64500.0,
            date = "2024-11-04",
            description = "Outward Supply: Chakki Atta & Basmati Rice"
        ),
        ScannedInvoiceData(
            invoiceNumber = "INV-2024-0090",
            partyName = "Varanasi Mega Kirana Mart",
            gstin = "09AAACV8821L1Z9",
            amount = 48200.0,
            date = "2024-11-02",
            description = "Outward Supply: Cooking Oil & Biscuits"
        )
    )
}

/**
 * Full-featured CameraX Scanner Dialog for Barcode and Invoice scanning.
 * Checks runtime CAMERA permission, displays live preview with reticle and laser animation,
 * and provides one-tap presets for fast wholesale billing & inward entry.
 */
@Composable
fun CameraScannerDialog(
    initialMode: ScannerMode = ScannerMode.BARCODE,
    onDismiss: () -> Unit,
    onBarcodeScanned: (ScannedBarcodeItem) -> Unit = {},
    onInvoiceScanned: (ScannedInvoiceData) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var selectedMode by remember { mutableStateOf(initialMode) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var permissionDeniedCount by remember { mutableStateOf(0) }
    var isTorchOn by remember { mutableStateOf(false) }
    var cameraControlRef by remember { mutableStateOf<androidx.camera.core.CameraControl?>(null) }
    var cameraError by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            permissionDeniedCount++
            Toast.makeText(
                context,
                "Camera permission is required to scan barcodes & paper bills.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Auto-request permission on first dialog open
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("camera_scanner_dialog"),
            color = Color.Black
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            .testTag("scanner_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Scanner",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = if (selectedMode == ScannerMode.BARCODE) "Scan Product Barcode" else "Scan Paper Invoice / Bill",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = {
                            isTorchOn = !isTorchOn
                            try {
                                cameraControlRef?.enableTorch(isTorchOn)
                            } catch (e: Exception) {
                                Log.e("CameraScanner", "Torch error", e)
                            }
                        },
                        enabled = hasCameraPermission,
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                if (isTorchOn) PrimaryBlue else Color.White.copy(alpha = 0.2f),
                                CircleShape
                            )
                            .testTag("scanner_torch_button")
                    ) {
                        Icon(
                            imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Toggle Flash",
                            tint = Color.White
                        )
                    }
                }

                // Mode Tabs
                TabRow(
                    selectedTabIndex = selectedMode.ordinal,
                    containerColor = Color.Black,
                    contentColor = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedMode == ScannerMode.BARCODE,
                        onClick = { selectedMode = ScannerMode.BARCODE },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Barcode / EAN-13")
                            }
                        }
                    )
                    Tab(
                        selected = selectedMode == ScannerMode.INVOICE,
                        onClick = { selectedMode = ScannerMode.INVOICE },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Invoice OCR / Challan")
                            }
                        }
                    )
                }

                // Camera Viewfinder / Permission Prompt Body
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (hasCameraPermission) {
                        // Live CameraX View
                        AndroidView(
                            factory = { ctx ->
                                val previewView = PreviewView(ctx).apply {
                                    layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                    scaleType = PreviewView.ScaleType.FILL_CENTER
                                }

                                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                cameraProviderFuture.addListener({
                                    try {
                                        val cameraProvider = cameraProviderFuture.get()
                                        val preview = Preview.Builder().build().also {
                                            it.surfaceProvider = previewView.surfaceProvider
                                        }

                                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                                        cameraProvider.unbindAll()
                                        val camera = cameraProvider.bindToLifecycle(
                                            lifecycleOwner,
                                            cameraSelector,
                                            preview
                                        )
                                        cameraControlRef = camera.cameraControl
                                    } catch (e: Exception) {
                                        Log.e("CameraScanner", "Camera binding failed", e)
                                        cameraError = e.message ?: "Camera device error"
                                    }
                                }, ContextCompat.getMainExecutor(ctx))

                                previewView
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        // Reticle Overlay (Laser Scan Bar & Bounding Box)
                        ScannerOverlay(mode = selectedMode)

                    } else {
                        // Camera Permission Request Screen
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(PrimaryBlue.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(42.dp)
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = "Camera Permission Required",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Kesharwani Enterprises needs camera access to scan FMCG product barcodes directly into invoices and digitize paper purchase bills.",
                                color = Color.LightGray,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(Modifier.height(24.dp))
                            Button(
                                onClick = {
                                    permissionLauncher.launch(Manifest.permission.CAMERA)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .testTag("request_camera_permission_button")
                            ) {
                                Icon(Icons.Default.Security, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Allow Camera Access", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Bottom Control & Quick Simulation Bar
                Surface(
                    color = Color(0xFF1E293B),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (selectedMode == ScannerMode.BARCODE)
                                    "Tap any FMCG item to simulate scan:"
                                else
                                    "Tap sample invoice or snap photo:",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )

                            if (selectedMode == ScannerMode.INVOICE) {
                                Button(
                                    onClick = {
                                        val sample = ScannerPresets.sampleInvoices.first()
                                        onInvoiceScanned(sample)
                                        Toast.makeText(context, "Scanned Bill #${sample.invoiceNumber}", Toast.LENGTH_SHORT).show()
                                        onDismiss()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                    modifier = Modifier.testTag("capture_invoice_button")
                                ) {
                                    Icon(Icons.Default.DocumentScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Snap & Read", fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        if (selectedMode == ScannerMode.BARCODE) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(ScannerPresets.fmcgBarcodes) { item ->
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(0xFF334155)
                                        ),
                                        modifier = Modifier
                                            .clickable {
                                                onBarcodeScanned(item)
                                                Toast.makeText(context, "Scanned: ${item.name}", Toast.LENGTH_SHORT).show()
                                                onDismiss()
                                            }
                                            .testTag("barcode_preset_${item.barcode}")
                                    ) {
                                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                            Text(
                                                text = item.name.take(24) + if (item.name.length > 24) "..." else "",
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Row(
                                                modifier = Modifier.padding(top = 2.dp),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = "₹${item.rate.toInt()}",
                                                    color = EmeraldGreen,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "GST ${item.gstRate.toInt()}%",
                                                    color = Color.LightGray,
                                                    fontSize = 11.sp
                                                )
                                                Text(
                                                    text = item.barcode,
                                                    color = Color.Yellow,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(ScannerPresets.sampleInvoices) { inv ->
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(0xFF334155)
                                        ),
                                        modifier = Modifier
                                            .clickable {
                                                onInvoiceScanned(inv)
                                                Toast.makeText(context, "Scanned: ${inv.invoiceNumber} (${inv.partyName})", Toast.LENGTH_SHORT).show()
                                                onDismiss()
                                            }
                                            .testTag("invoice_preset_${inv.invoiceNumber}")
                                    ) {
                                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                            Text(
                                                text = inv.partyName.take(24) + if (inv.partyName.length > 24) "..." else "",
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Row(
                                                modifier = Modifier.padding(top = 2.dp),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = inv.invoiceNumber,
                                                    color = Color.Yellow,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Text(
                                                    text = "₹${inv.amount.toInt()}",
                                                    color = EmeraldGreen,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Animated reticle and laser scanner overlay for CameraX preview.
 */
@Composable
fun ScannerOverlay(
    mode: ScannerMode,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "laser_transition")
    val laserProgress by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_progress"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Define bounding scan area
        val boxWidth = if (mode == ScannerMode.BARCODE) width * 0.78f else width * 0.88f
        val boxHeight = if (mode == ScannerMode.BARCODE) height * 0.32f else height * 0.58f

        val left = (width - boxWidth) / 2
        val top = (height - boxHeight) / 2
        val right = left + boxWidth
        val bottom = top + boxHeight

        // Dark dimming around viewfinder
        drawRect(
            color = Color.Black.copy(alpha = 0.5f),
            topLeft = Offset(0f, 0f),
            size = Size(width, top)
        )
        drawRect(
            color = Color.Black.copy(alpha = 0.5f),
            topLeft = Offset(0f, bottom),
            size = Size(width, height - bottom)
        )
        drawRect(
            color = Color.Black.copy(alpha = 0.5f),
            topLeft = Offset(0f, top),
            size = Size(left, boxHeight)
        )
        drawRect(
            color = Color.Black.copy(alpha = 0.5f),
            topLeft = Offset(right, top),
            size = Size(width - right, boxHeight)
        )

        // Viewfinder Border Box
        drawRoundRect(
            color = Color.White.copy(alpha = 0.8f),
            topLeft = Offset(left, top),
            size = Size(boxWidth, boxHeight),
            cornerRadius = CornerRadius(16f, 16f),
            style = Stroke(width = 3f)
        )

        // Corner accents (Bright Neon Primary Blue / Emerald Green)
        val cornerColor = if (mode == ScannerMode.BARCODE) Color(0xFF38BDF8) else Color(0xFF34D399)
        val cornerLen = 32f
        val cornerStroke = 8f

        // Top-Left
        drawLine(cornerColor, Offset(left, top), Offset(left + cornerLen, top), strokeWidth = cornerStroke)
        drawLine(cornerColor, Offset(left, top), Offset(left, top + cornerLen), strokeWidth = cornerStroke)

        // Top-Right
        drawLine(cornerColor, Offset(right, top), Offset(right - cornerLen, top), strokeWidth = cornerStroke)
        drawLine(cornerColor, Offset(right, top), Offset(right, top + cornerLen), strokeWidth = cornerStroke)

        // Bottom-Left
        drawLine(cornerColor, Offset(left, bottom), Offset(left + cornerLen, bottom), strokeWidth = cornerStroke)
        drawLine(cornerColor, Offset(left, bottom), Offset(left, bottom - cornerLen), strokeWidth = cornerStroke)

        // Bottom-Right
        drawLine(cornerColor, Offset(right, bottom), Offset(right - cornerLen, bottom), strokeWidth = cornerStroke)
        drawLine(cornerColor, Offset(right, bottom), Offset(right, bottom - cornerLen), strokeWidth = cornerStroke)

        // Animated Laser Scanning Line
        val laserY = top + (boxHeight * laserProgress)
        drawLine(
            color = cornerColor,
            start = Offset(left + 8f, laserY),
            end = Offset(right - 8f, laserY),
            strokeWidth = 4f
        )
    }
}
