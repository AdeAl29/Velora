package com.example.finora

import com.example.finora.core.util.CurrencyFormatter
import org.junit.Assert.assertEquals
import org.junit.Test

class CurrencyFormatterTest {

    @Test
    fun testFormatRupiah() {
        assertEquals("Rp50.000", CurrencyFormatter.formatRupiah(50000))
        assertEquals("Rp1.500.000", CurrencyFormatter.formatRupiah(1500000))
    }

    @Test
    fun testFormatSigned() {
        assertEquals("+Rp250.000", CurrencyFormatter.formatSigned(250000, isIncome = true))
        assertEquals("-Rp75.000", CurrencyFormatter.formatSigned(75000, isIncome = false))
    }

    @Test
    fun testFormatCompact() {
        assertEquals("Rp1.5jt", CurrencyFormatter.formatCompact(1500000))
        assertEquals("Rp500rb", CurrencyFormatter.formatCompact(500000))
    }
}
