# Aruphia — Personal Finance Management Android App

<p align="center">
  <b>Aplikasi Manajemen Keuangan Pribadi Modern, Elegan, dan Komprehensif</b>
</p>

---

## 🌟 Fitur Utama

- **Dashboard Finansial Modern**:
  - Saldo total gabungan (*Net Worth*) dengan fitur sembunyikan/tampilkan nominal (*privacy toggle*).
  - Grafik mini sparkline tren pengeluaran 7 hari terakhir.
  - Donut chart interaktif alokasi belanja per kategori.
  - Perbandingan pengeluaran Tetap vs Tidak Tetap.
- **Pencatatan Transaksi Lengkap (CRUD)**:
  - Pemasukan, Pengeluaran Tetap, dan Pengeluaran Variabel.
  - Pemilihan kategori dengan ikon dinamis berbasis Jetpack Compose Canvas.
  - Filter chip cepat (Semua, Pemasukan, Tetap, Tidak Tetap) dan pencarian transaksi.
- **Multi-Rekening & Dompet Digital**:
  - Pengelolaan rekening bank, e-wallet, dan tunai (cash).
  - **Transfer Saldo Antar Rekening** tanpa memengaruhi catatan pemasukan/pengeluaran.
- **Anggaran Bulanan (Budgeting)**:
  - Batas pengeluaran per kategori dengan indikator visual adaptif (Aman, Waspada, Overbudget).
- **Tujuan Finansial (Financial Goals)**:
  - Target impian (Gadget, DP Rumah, Dana Darurat, Liburan).
  - Tracking progress real-time dengan dialog **+ Tabung Dana** dan **− Tarik Dana**.
- **Tagihan & Pengingat (Bills)**:
  - Pengingat jatuh tempo bulanan dengan integrasi notifikasi Android.
  - Checklist satu sentuhan untuk menandai lunas.
- **Pengeluaran Tetap Rutin**:
  - Penjadwalan transaksi otomatis harian, mingguan, bulanan, dan tahunan.
- **Statistik & Analisis Pintar**:
  - Analisis tren arus kas (*Cash Flow*), perbandingan bar chart pemasukan vs pengeluaran.
  - Insight finansial otomatis yang dihasilkan dari data riil database lokal.
- **Kalender Keuangan**:
  - Kalender visual dengan dot indikator hijau/merah pada setiap tanggal aktivitas transaksi.
- **Ekspor Dokumen**:
  - Pembuatan laporan keuangan dalam format **PDF** dan **CSV** yang siap dibagikan.
- **Pencarian Global**:
  - Pencarian seketika ke seluruh transaksi, kategori, rekening, tagihan, dan goals.
- **Pengaturan & Personalisasi**:
  - Dukungan mata uang multi-negara (IDR, USD, SGD, EUR, JPY, dll).
  - Tema Gelap (*Dark Mode*), Tema Terang (*Light Mode*), dan Mengikuti Sistem.
  - Tombol **Muat Data Demo / Sampel** untuk pengujian langsung.

---

## 🛠️ Teknologi & Arsitektur

- **Bahasa**: Kotlin (100%)
- **UI Toolkit**: Jetpack Compose & Material 3
- **Arsitektur**: MVVM + Clean Architecture + StateFlow
- **Database Lokal**: Room Database (SQLite)
- **Dependency Injection**: Hilt (Dagger)
- **Background Tasks**: WorkManager
- **Navigasi**: AndroidX Navigation 3
- **Penyimpanan Preferensi**: Jetpack DataStore Preferences
- **Target SDK**: Android 15 (API 35/36), Min SDK 26 (Android 8.0+)

---

## 🚀 Cara Menjalankan

1. Clone repositori ini:
   ```bash
   git clone https://github.com/AdeAl29/Velora.git
   ```
2. Buka project di **Android Studio Ladybug** atau versi terbaru.
3. Pastikan JDK 17 atau JDK 21 terpilih sebagai Gradle JDK.
4. Jalankan build atau instal ke device/emulator:
   ```bash
   ./gradlew assembleDebug
   ```
   File APK akan tersedia di `app/build/outputs/apk/debug/app-debug.apk`.

---

## 📄 Lisensi

Distributed under the MIT License.
