package com.example.finora.core.ocr

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.example.finora.core.util.DateUtils
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.regex.Pattern
import kotlin.coroutines.resume

data class ScannedReceipt(
    val merchantName: String? = null,
    val totalAmount: Long? = null,
    val date: Long? = null,
    val detectedCategoryHint: String? = null,
    val rawText: String = ""
)

object ReceiptScanner {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun scanFromBitmap(bitmap: Bitmap): ScannedReceipt {
        val image = InputImage.fromBitmap(bitmap, 0)
        return processImage(image)
    }

    suspend fun scanFromUri(context: Context, uri: Uri): ScannedReceipt {
        val image = InputImage.fromFilePath(context, uri)
        return processImage(image)
    }

    private suspend fun processImage(image: InputImage): ScannedReceipt =
        suspendCancellableCoroutine { continuation ->
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val scanned = parseReceiptText(visionText.text)
                    continuation.resume(scanned)
                }
                .addOnFailureListener {
                    continuation.resume(ScannedReceipt(rawText = ""))
                }
        }

    fun parseReceiptText(text: String): ScannedReceipt {
        if (text.isBlank()) return ScannedReceipt()

        val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
        val rawLower = text.lowercase()

        // 1. Merchant Name: First 1-3 lines typically contain the store name
        var merchant: String? = null
        for (i in 0 until minOf(4, lines.size)) {
            val line = lines[i]
            // Skip common non-merchant header lines
            if (!line.contains("selamat datang", ignoreCase = true) &&
                !line.contains("struk", ignoreCase = true) &&
                !line.contains("nota", ignoreCase = true) &&
                line.length in 3..40
            ) {
                merchant = line
                break
            }
        }

        // 2. Total Amount Extraction
        val amount = extractTotalAmount(lines)

        // 3. Date Extraction
        val dateMillis = extractDate(text)

        // 4. Category Hint
        val categoryHint = detectCategory(rawLower)

        return ScannedReceipt(
            merchantName = merchant,
            totalAmount = amount,
            date = dateMillis,
            detectedCategoryHint = categoryHint,
            rawText = text
        )
    }

    private fun extractTotalAmount(lines: List<String>): Long? {
        val totalKeywords = listOf(
            "total bayar", "grand total", "total", "sub total", "subtotal",
            "jumlah", "tagihan", "netto", "tunai", "cash", "debit"
        )

        // Strategy 1: Look for lines explicitly mentioning Total
        for (keyword in totalKeywords) {
            for (line in lines.reversed()) {
                val lower = line.lowercase()
                if (lower.contains(keyword)) {
                    val numbers = extractNumbersFromLine(line)
                    if (numbers.isNotEmpty()) {
                        val maxInLine = numbers.maxOrNull()
                        if (maxInLine != null && maxInLine > 500) {
                            return maxInLine
                        }
                    }
                }
            }
        }

        // Strategy 2: Look for 'Rp' followed by digits
        val rpPattern = Pattern.compile("(?i)rp\\.?\\s*([0-9.,]+)")
        val matcher = rpPattern.matcher(lines.joinToString(" "))
        val rpAmounts = mutableListOf<Long>()
        while (matcher.find()) {
            val numStr = matcher.group(1)?.replace(".", "")?.replace(",", "")
            numStr?.toLongOrNull()?.let { if (it > 500) rpAmounts.add(it) }
        }
        if (rpAmounts.isNotEmpty()) {
            return rpAmounts.maxOrNull()
        }

        // Strategy 3: Find the largest sensible number on the receipt (excluding phone/barcodes)
        val allNumbers = lines.flatMap { extractNumbersFromLine(it) }.filter { it in 1_000..500_000_000 }
        return allNumbers.maxOrNull()
    }

    private fun extractNumbersFromLine(line: String): List<Long> {
        val results = mutableListOf<Long>()
        // Match numbers with possible period/comma thousand separators e.g. 85.000 or 150,000
        val pattern = Pattern.compile("\\b(\\d{1,3}(?:[.,]\\d{3})*|\\d+)\\b")
        val matcher = pattern.matcher(line)
        while (matcher.find()) {
            val raw = matcher.group(1)?.replace(".", "")?.replace(",", "")
            val parsed = raw?.toLongOrNull()
            if (parsed != null && parsed in 500..500_000_000) {
                results.add(parsed)
            }
        }
        return results
    }

    private fun extractDate(text: String): Long? {
        // Formats: dd/MM/yyyy, dd-MM-yyyy, dd/MM/yy
        val datePattern = Pattern.compile("\\b(\\d{1,2})[/-](\\d{1,2})[/-](\\d{2,4})\\b")
        val matcher = datePattern.matcher(text)
        if (matcher.find()) {
            try {
                val day = matcher.group(1)?.toIntOrNull() ?: 1
                val month = matcher.group(2)?.toIntOrNull() ?: 1
                var year = matcher.group(3)?.toIntOrNull() ?: LocalDate.now().year
                if (year < 100) year += 2000
                if (month in 1..12 && day in 1..31) {
                    val localDate = LocalDate.of(year, month, day)
                    return DateUtils.toEpochMillis(localDate)
                }
            } catch (_: Exception) {
            }
        }
        return null
    }

    private fun detectCategory(lower: String): String {
        return when {
            lower.contains("resto") || lower.contains("cafe") || lower.contains("kopi") ||
            lower.contains("makan") || lower.contains("bakso") || lower.contains("ayam") ||
            lower.contains("kfc") || lower.contains("mcd") || lower.contains("dapur") ||
            lower.contains("burger") || lower.contains("food") -> "Makanan & Minuman"

            lower.contains("spbu") || lower.contains("pertamina") || lower.contains("shell") ||
            lower.contains("bensin") || lower.contains("gojek") || lower.contains("grab") ||
            lower.contains("parkir") || lower.contains("tol") -> "Transportasi"

            lower.contains("indomaret") || lower.contains("alfamart") || lower.contains("superindo") ||
            lower.contains("hypermart") || lower.contains("mart") || lower.contains("belanja") ||
            lower.contains("shopee") || lower.contains("tokopedia") -> "Belanja"

            lower.contains("pln") || lower.contains("listrik") || lower.contains("token") -> "Listrik"

            lower.contains("pulsa") || lower.contains("kuota") || lower.contains("telkom") ||
            lower.contains("indihome") || lower.contains("wifi") -> "Internet & Pulsa"

            lower.contains("apotek") || lower.contains("kimia farma") || lower.contains("obat") ||
            lower.contains("klinik") || lower.contains("dokter") || lower.contains("rs") -> "Kesehatan"

            lower.contains("bioskop") || lower.contains("cinema") || lower.contains("xxi") ||
            lower.contains("nonton") || lower.contains("game") -> "Hiburan"

            else -> "Lainnya"
        }
    }
}
