package com.example.finora.core.export

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.finora.core.database.entity.TransactionEntity
import com.example.finora.core.repository.CategoryRepository
import com.example.finora.core.repository.WalletRepository
import com.example.finora.core.util.CurrencyFormatter
import com.example.finora.core.util.DateUtils
import java.io.File
import java.io.FileOutputStream

object PdfExporter {

    suspend fun generatePdfReport(
        context: Context,
        title: String,
        periodName: String,
        totalIncome: Long,
        totalExpense: Long,
        transactions: List<TransactionEntity>,
        categoryRepository: CategoryRepository,
        walletRepository: WalletRepository
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size in points
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint()
        var y = 40f

        // Title Header
        paint.color = android.graphics.Color.parseColor("#6C63FF")
        paint.textSize = 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("FINORA — LAPORAN KEUANGAN", 40f, y, paint)
        y += 24f

        paint.color = android.graphics.Color.DKGRAY
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Periode: $periodName • Dibuat pada: ${DateUtils.formatFullDate(System.currentTimeMillis())}", 40f, y, paint)
        y += 30f

        // Financial Summary Box
        paint.color = android.graphics.Color.parseColor("#F1F5F9")
        canvas.drawRoundRect(40f, y, 555f, y + 70f, 12f, 12f, paint)

        paint.color = android.graphics.Color.BLACK
        paint.textSize = 11f
        canvas.drawText("Total Pemasukan:", 56f, y + 26f, paint)
        paint.color = android.graphics.Color.parseColor("#059669")
        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(CurrencyFormatter.formatRupiah(totalIncome), 56f, y + 46f, paint)

        paint.color = android.graphics.Color.BLACK
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Total Pengeluaran:", 220f, y + 26f, paint)
        paint.color = android.graphics.Color.parseColor("#DC2626")
        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(CurrencyFormatter.formatRupiah(totalExpense), 220f, y + 46f, paint)

        val net = totalIncome - totalExpense
        paint.color = android.graphics.Color.BLACK
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Arus Kas Bersih:", 390f, y + 26f, paint)
        paint.color = if (net >= 0) android.graphics.Color.parseColor("#0284C7") else android.graphics.Color.parseColor("#DC2626")
        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(CurrencyFormatter.formatRupiah(net), 390f, y + 46f, paint)

        y += 100f

        // Table Header
        paint.color = android.graphics.Color.BLACK
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Riwayat Transaksi", 40f, y, paint)
        y += 20f

        paint.color = android.graphics.Color.parseColor("#6C63FF")
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("TANGGAL", 40f, y, paint)
        canvas.drawText("KATEGORI", 120f, y, paint)
        canvas.drawText("REKENING", 230f, y, paint)
        canvas.drawText("CATATAN", 330f, y, paint)
        canvas.drawText("NOMINAL", 470f, y, paint)
        y += 10f

        paint.color = android.graphics.Color.LTGRAY
        paint.strokeWidth = 1f
        canvas.drawLine(40f, y, 555f, y, paint)
        y += 16f

        // Table Rows
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 10f

        for (tx in transactions.take(25)) {
            val cat = tx.categoryId?.let { categoryRepository.getById(it) }
            val wallet = walletRepository.getById(tx.walletId)
            val isIncome = tx.transactionType == "INCOME"

            paint.color = android.graphics.Color.DKGRAY
            canvas.drawText(DateUtils.formatShortDate(tx.date), 40f, y, paint)
            canvas.drawText((cat?.name ?: "-").take(15), 120f, y, paint)
            canvas.drawText((wallet?.name ?: "-").take(14), 230f, y, paint)
            canvas.drawText((tx.note ?: "-").take(20), 330f, y, paint)

            paint.color = if (isIncome) android.graphics.Color.parseColor("#059669") else android.graphics.Color.parseColor("#DC2626")
            canvas.drawText(CurrencyFormatter.formatSigned(tx.amount, isIncome), 470f, y, paint)

            y += 18f
            if (y > 800f) break
        }

        pdfDocument.finishPage(page)

        val file = File(context.cacheDir, "laporan_keuangan_finora.pdf")
        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()
        return file
    }
}
