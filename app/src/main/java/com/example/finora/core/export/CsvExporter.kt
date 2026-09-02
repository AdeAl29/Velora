package com.example.finora.core.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.finora.core.database.entity.TransactionEntity
import com.example.finora.core.repository.CategoryRepository
import com.example.finora.core.repository.WalletRepository
import com.example.finora.core.util.DateUtils
import java.io.File
import java.io.FileWriter

object CsvExporter {

    suspend fun exportTransactionsToCsv(
        context: Context,
        transactions: List<TransactionEntity>,
        categoryRepository: CategoryRepository,
        walletRepository: WalletRepository
    ): File {
        val file = File(context.cacheDir, "laporan_keuangan_finora.csv")
        FileWriter(file).use { writer ->
            writer.append("ID,Tanggal,Waktu,Tipe,Kategori,Rekening,Nominal,Catatan,Rutin\n")
            for (tx in transactions) {
                val cat = tx.categoryId?.let { categoryRepository.getById(it) }
                val wallet = walletRepository.getById(tx.walletId)
                val type = when (tx.transactionType) {
                    "INCOME" -> "Pemasukan"
                    "EXPENSE_FIXED" -> "Pengeluaran Tetap"
                    else -> "Pengeluaran Tidak Tetap"
                }
                val dateStr = DateUtils.formatFullDate(tx.date)
                val catName = cat?.name ?: "-"
                val walletName = wallet?.name ?: "-"
                val note = (tx.note ?: "").replace(",", " ")
                writer.append("${tx.id},$dateStr,${tx.time},$type,$catName,$walletName,${tx.amount},$note,${tx.isRecurring}\n")
            }
        }
        return file
    }

    fun shareFile(context: Context, file: File, mimeType: String = "text/csv") {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Bagikan Laporan Finora"))
    }
}
