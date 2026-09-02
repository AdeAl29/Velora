package com.example.finora.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun FinoraCategoryIcon(
    iconName: String,
    color: Color,
    size: Dp = 24.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size.minDimension
        val strokeW = s * 0.09f
        val stroke = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)

        when (iconName) {
            "food", "makanan" -> drawFoodIcon(s, color, stroke)
            "transport", "transportasi" -> drawTransportIcon(s, color, stroke)
            "shopping", "belanja" -> drawShoppingIcon(s, color, stroke)
            "entertainment", "hiburan" -> drawEntertainmentIcon(s, color, stroke)
            "health", "kesehatan" -> drawHealthIcon(s, color, stroke)
            "education", "pendidikan" -> drawEducationIcon(s, color, stroke)
            "housing", "kos", "sewa" -> drawHousingIcon(s, color, stroke)
            "electricity", "listrik" -> drawElectricityIcon(s, color, stroke)
            "internet" -> drawInternetIcon(s, color, stroke)
            "installment", "cicilan" -> drawInstallmentIcon(s, color, stroke)
            "insurance", "asuransi" -> drawInsuranceIcon(s, color, stroke)
            "subscription", "langganan" -> drawSubscriptionIcon(s, color, stroke)
            "bill", "tagihan" -> drawBillIcon(s, color, stroke)
            "salary", "gaji" -> drawSalaryIcon(s, color, stroke)
            "pocket_money", "uang_saku" -> drawPocketMoneyIcon(s, color, stroke)
            "bonus" -> drawBonusIcon(s, color, stroke)
            "freelance" -> drawFreelanceIcon(s, color, stroke)
            "gift", "hadiah" -> drawGiftIcon(s, color, stroke)
            "investment", "investasi" -> drawInvestmentIcon(s, color, stroke)
            "technology", "teknologi" -> drawTechnologyIcon(s, color, stroke)
            "hangout", "nongkrong" -> drawHangoutIcon(s, color, stroke)
            "cash" -> drawCashIcon(s, color, stroke)
            "bank" -> drawBankIcon(s, color, stroke)
            "ewallet" -> drawEwalletIcon(s, color, stroke)
            "laptop" -> drawLaptopIcon(s, color, stroke)
            "emergency" -> drawEmergencyIcon(s, color, stroke)
            "travel" -> drawTravelIcon(s, color, stroke)
            "target" -> drawTargetIcon(s, color, stroke)
            else -> drawOtherIcon(s, color, stroke)
        }
    }
}

// Each icon is a Canvas-drawn vector using consistent stroke width and proportions
private fun DrawScope.drawFoodIcon(s: Float, color: Color, stroke: Stroke) {
    // Bowl shape
    val bowlPath = Path().apply {
        moveTo(s * 0.2f, s * 0.45f)
        quadraticBezierTo(s * 0.2f, s * 0.8f, s * 0.5f, s * 0.8f)
        quadraticBezierTo(s * 0.8f, s * 0.8f, s * 0.8f, s * 0.45f)
    }
    drawPath(bowlPath, color, style = stroke)
    // Steam lines
    drawLine(color, Offset(s * 0.35f, s * 0.35f), Offset(s * 0.35f, s * 0.2f), strokeWidth = stroke.width)
    drawLine(color, Offset(s * 0.5f, s * 0.32f), Offset(s * 0.5f, s * 0.15f), strokeWidth = stroke.width)
    drawLine(color, Offset(s * 0.65f, s * 0.35f), Offset(s * 0.65f, s * 0.2f), strokeWidth = stroke.width)
}

private fun DrawScope.drawTransportIcon(s: Float, color: Color, stroke: Stroke) {
    // Car body
    drawRoundRect(color, Offset(s * 0.15f, s * 0.4f), Size(s * 0.7f, s * 0.3f), CornerRadius(s * 0.08f), style = stroke)
    // Car top
    val topPath = Path().apply {
        moveTo(s * 0.25f, s * 0.4f)
        lineTo(s * 0.35f, s * 0.2f)
        lineTo(s * 0.65f, s * 0.2f)
        lineTo(s * 0.75f, s * 0.4f)
    }
    drawPath(topPath, color, style = stroke)
    // Wheels
    drawCircle(color, s * 0.06f, Offset(s * 0.32f, s * 0.73f), style = stroke)
    drawCircle(color, s * 0.06f, Offset(s * 0.68f, s * 0.73f), style = stroke)
}

private fun DrawScope.drawShoppingIcon(s: Float, color: Color, stroke: Stroke) {
    // Bag body
    drawRoundRect(color, Offset(s * 0.2f, s * 0.35f), Size(s * 0.6f, s * 0.5f), CornerRadius(s * 0.06f), style = stroke)
    // Handles
    val handlePath = Path().apply {
        moveTo(s * 0.35f, s * 0.35f)
        quadraticBezierTo(s * 0.35f, s * 0.15f, s * 0.5f, s * 0.15f)
        quadraticBezierTo(s * 0.65f, s * 0.15f, s * 0.65f, s * 0.35f)
    }
    drawPath(handlePath, color, style = stroke)
}

private fun DrawScope.drawEntertainmentIcon(s: Float, color: Color, stroke: Stroke) {
    // Game controller shape
    drawRoundRect(color, Offset(s * 0.15f, s * 0.3f), Size(s * 0.7f, s * 0.35f), CornerRadius(s * 0.15f), style = stroke)
    // D-pad
    drawLine(color, Offset(s * 0.32f, s * 0.42f), Offset(s * 0.32f, s * 0.55f), strokeWidth = stroke.width)
    drawLine(color, Offset(s * 0.25f, s * 0.485f), Offset(s * 0.39f, s * 0.485f), strokeWidth = stroke.width)
    // Buttons
    drawCircle(color, s * 0.03f, Offset(s * 0.62f, s * 0.43f))
    drawCircle(color, s * 0.03f, Offset(s * 0.7f, s * 0.5f))
}

private fun DrawScope.drawHealthIcon(s: Float, color: Color, stroke: Stroke) {
    // Cross
    drawLine(color, Offset(s * 0.5f, s * 0.2f), Offset(s * 0.5f, s * 0.8f), strokeWidth = stroke.width * 2)
    drawLine(color, Offset(s * 0.2f, s * 0.5f), Offset(s * 0.8f, s * 0.5f), strokeWidth = stroke.width * 2)
    // Heart outline
    drawCircle(color, s * 0.38f, Offset(s * 0.5f, s * 0.5f), style = stroke)
}

private fun DrawScope.drawEducationIcon(s: Float, color: Color, stroke: Stroke) {
    // Book
    drawRoundRect(color, Offset(s * 0.2f, s * 0.15f), Size(s * 0.6f, s * 0.7f), CornerRadius(s * 0.04f), style = stroke)
    // Spine
    drawLine(color, Offset(s * 0.5f, s * 0.15f), Offset(s * 0.5f, s * 0.85f), strokeWidth = stroke.width)
    // Lines
    drawLine(color, Offset(s * 0.55f, s * 0.35f), Offset(s * 0.72f, s * 0.35f), strokeWidth = stroke.width * 0.7f)
    drawLine(color, Offset(s * 0.55f, s * 0.48f), Offset(s * 0.72f, s * 0.48f), strokeWidth = stroke.width * 0.7f)
}

private fun DrawScope.drawHousingIcon(s: Float, color: Color, stroke: Stroke) {
    // Roof
    val roofPath = Path().apply {
        moveTo(s * 0.1f, s * 0.45f)
        lineTo(s * 0.5f, s * 0.15f)
        lineTo(s * 0.9f, s * 0.45f)
    }
    drawPath(roofPath, color, style = stroke)
    // Walls
    drawRect(color, Offset(s * 0.2f, s * 0.45f), Size(s * 0.6f, s * 0.42f), style = stroke)
    // Door
    drawRoundRect(color, Offset(s * 0.4f, s * 0.55f), Size(s * 0.2f, s * 0.32f), CornerRadius(s * 0.03f), style = stroke)
}

private fun DrawScope.drawElectricityIcon(s: Float, color: Color, stroke: Stroke) {
    val boltPath = Path().apply {
        moveTo(s * 0.55f, s * 0.1f)
        lineTo(s * 0.3f, s * 0.5f)
        lineTo(s * 0.48f, s * 0.5f)
        lineTo(s * 0.4f, s * 0.9f)
        lineTo(s * 0.7f, s * 0.45f)
        lineTo(s * 0.52f, s * 0.45f)
        close()
    }
    drawPath(boltPath, color, style = stroke)
}

private fun DrawScope.drawInternetIcon(s: Float, color: Color, stroke: Stroke) {
    // WiFi arcs
    drawCircle(color, s * 0.04f, Offset(s * 0.5f, s * 0.7f))
    val arc1 = Path().apply {
        moveTo(s * 0.3f, s * 0.55f)
        quadraticBezierTo(s * 0.5f, s * 0.35f, s * 0.7f, s * 0.55f)
    }
    val arc2 = Path().apply {
        moveTo(s * 0.18f, s * 0.4f)
        quadraticBezierTo(s * 0.5f, s * 0.12f, s * 0.82f, s * 0.4f)
    }
    drawPath(arc1, color, style = stroke)
    drawPath(arc2, color, style = stroke)
}

private fun DrawScope.drawInstallmentIcon(s: Float, color: Color, stroke: Stroke) {
    // Credit card
    drawRoundRect(color, Offset(s * 0.12f, s * 0.25f), Size(s * 0.76f, s * 0.5f), CornerRadius(s * 0.06f), style = stroke)
    drawLine(color, Offset(s * 0.12f, s * 0.42f), Offset(s * 0.88f, s * 0.42f), strokeWidth = stroke.width)
    drawLine(color, Offset(s * 0.2f, s * 0.58f), Offset(s * 0.45f, s * 0.58f), strokeWidth = stroke.width * 0.7f)
}

private fun DrawScope.drawInsuranceIcon(s: Float, color: Color, stroke: Stroke) {
    // Shield
    val shieldPath = Path().apply {
        moveTo(s * 0.5f, s * 0.12f)
        lineTo(s * 0.8f, s * 0.28f)
        quadraticBezierTo(s * 0.8f, s * 0.7f, s * 0.5f, s * 0.88f)
        quadraticBezierTo(s * 0.2f, s * 0.7f, s * 0.2f, s * 0.28f)
        close()
    }
    drawPath(shieldPath, color, style = stroke)
    // Check
    val checkPath = Path().apply {
        moveTo(s * 0.35f, s * 0.52f)
        lineTo(s * 0.46f, s * 0.63f)
        lineTo(s * 0.65f, s * 0.38f)
    }
    drawPath(checkPath, color, style = stroke)
}

private fun DrawScope.drawSubscriptionIcon(s: Float, color: Color, stroke: Stroke) {
    // Loop/repeat arrows
    val path1 = Path().apply {
        moveTo(s * 0.3f, s * 0.35f)
        quadraticBezierTo(s * 0.1f, s * 0.5f, s * 0.3f, s * 0.65f)
        lineTo(s * 0.7f, s * 0.65f)
    }
    val path2 = Path().apply {
        moveTo(s * 0.7f, s * 0.65f)
        quadraticBezierTo(s * 0.9f, s * 0.5f, s * 0.7f, s * 0.35f)
        lineTo(s * 0.3f, s * 0.35f)
    }
    drawPath(path1, color, style = stroke)
    drawPath(path2, color, style = stroke)
    // Arrow heads
    drawLine(color, Offset(s * 0.65f, s * 0.58f), Offset(s * 0.7f, s * 0.65f), strokeWidth = stroke.width)
    drawLine(color, Offset(s * 0.75f, s * 0.58f), Offset(s * 0.7f, s * 0.65f), strokeWidth = stroke.width)
}

private fun DrawScope.drawBillIcon(s: Float, color: Color, stroke: Stroke) {
    // Receipt
    val receiptPath = Path().apply {
        moveTo(s * 0.25f, s * 0.15f)
        lineTo(s * 0.75f, s * 0.15f)
        lineTo(s * 0.75f, s * 0.78f)
        lineTo(s * 0.65f, s * 0.72f)
        lineTo(s * 0.55f, s * 0.78f)
        lineTo(s * 0.45f, s * 0.72f)
        lineTo(s * 0.35f, s * 0.78f)
        lineTo(s * 0.25f, s * 0.72f)
        close()
    }
    drawPath(receiptPath, color, style = stroke)
    // Lines
    drawLine(color, Offset(s * 0.35f, s * 0.35f), Offset(s * 0.65f, s * 0.35f), strokeWidth = stroke.width * 0.7f)
    drawLine(color, Offset(s * 0.35f, s * 0.48f), Offset(s * 0.65f, s * 0.48f), strokeWidth = stroke.width * 0.7f)
    drawLine(color, Offset(s * 0.35f, s * 0.61f), Offset(s * 0.55f, s * 0.61f), strokeWidth = stroke.width * 0.7f)
}

private fun DrawScope.drawSalaryIcon(s: Float, color: Color, stroke: Stroke) {
    // Money/wallet
    drawRoundRect(color, Offset(s * 0.15f, s * 0.25f), Size(s * 0.7f, s * 0.5f), CornerRadius(s * 0.06f), style = stroke)
    // Dollar/Rupiah line
    drawLine(color, Offset(s * 0.5f, s * 0.35f), Offset(s * 0.5f, s * 0.65f), strokeWidth = stroke.width)
    drawLine(color, Offset(s * 0.38f, s * 0.42f), Offset(s * 0.62f, s * 0.42f), strokeWidth = stroke.width * 0.7f)
    drawLine(color, Offset(s * 0.38f, s * 0.58f), Offset(s * 0.62f, s * 0.58f), strokeWidth = stroke.width * 0.7f)
}

private fun DrawScope.drawPocketMoneyIcon(s: Float, color: Color, stroke: Stroke) {
    // Coin
    drawCircle(color, s * 0.3f, Offset(s * 0.5f, s * 0.5f), style = stroke)
    drawCircle(color, s * 0.18f, Offset(s * 0.5f, s * 0.5f), style = Stroke(width = stroke.width * 0.6f))
}

private fun DrawScope.drawBonusIcon(s: Float, color: Color, stroke: Stroke) {
    // Star
    val starPath = Path().apply {
        moveTo(s * 0.5f, s * 0.12f)
        lineTo(s * 0.58f, s * 0.38f)
        lineTo(s * 0.85f, s * 0.38f)
        lineTo(s * 0.63f, s * 0.55f)
        lineTo(s * 0.72f, s * 0.82f)
        lineTo(s * 0.5f, s * 0.65f)
        lineTo(s * 0.28f, s * 0.82f)
        lineTo(s * 0.37f, s * 0.55f)
        lineTo(s * 0.15f, s * 0.38f)
        lineTo(s * 0.42f, s * 0.38f)
        close()
    }
    drawPath(starPath, color, style = stroke)
}

private fun DrawScope.drawFreelanceIcon(s: Float, color: Color, stroke: Stroke) {
    // Briefcase
    drawRoundRect(color, Offset(s * 0.15f, s * 0.35f), Size(s * 0.7f, s * 0.45f), CornerRadius(s * 0.05f), style = stroke)
    val handlePath = Path().apply {
        moveTo(s * 0.35f, s * 0.35f)
        lineTo(s * 0.35f, s * 0.25f)
        quadraticBezierTo(s * 0.35f, s * 0.18f, s * 0.42f, s * 0.18f)
        lineTo(s * 0.58f, s * 0.18f)
        quadraticBezierTo(s * 0.65f, s * 0.18f, s * 0.65f, s * 0.25f)
        lineTo(s * 0.65f, s * 0.35f)
    }
    drawPath(handlePath, color, style = stroke)
}

private fun DrawScope.drawGiftIcon(s: Float, color: Color, stroke: Stroke) {
    // Gift box
    drawRect(color, Offset(s * 0.2f, s * 0.4f), Size(s * 0.6f, s * 0.45f), style = stroke)
    drawRect(color, Offset(s * 0.15f, s * 0.32f), Size(s * 0.7f, s * 0.12f), style = stroke)
    // Ribbon
    drawLine(color, Offset(s * 0.5f, s * 0.32f), Offset(s * 0.5f, s * 0.85f), strokeWidth = stroke.width)
    val bowPath = Path().apply {
        moveTo(s * 0.5f, s * 0.32f)
        quadraticBezierTo(s * 0.3f, s * 0.12f, s * 0.38f, s * 0.28f)
        moveTo(s * 0.5f, s * 0.32f)
        quadraticBezierTo(s * 0.7f, s * 0.12f, s * 0.62f, s * 0.28f)
    }
    drawPath(bowPath, color, style = stroke)
}

private fun DrawScope.drawInvestmentIcon(s: Float, color: Color, stroke: Stroke) {
    // Trending up chart
    drawLine(color, Offset(s * 0.15f, s * 0.75f), Offset(s * 0.85f, s * 0.75f), strokeWidth = stroke.width * 0.6f)
    drawLine(color, Offset(s * 0.15f, s * 0.75f), Offset(s * 0.15f, s * 0.2f), strokeWidth = stroke.width * 0.6f)
    val trendPath = Path().apply {
        moveTo(s * 0.2f, s * 0.65f)
        lineTo(s * 0.4f, s * 0.45f)
        lineTo(s * 0.55f, s * 0.55f)
        lineTo(s * 0.8f, s * 0.25f)
    }
    drawPath(trendPath, color, style = stroke)
    // Arrow
    drawLine(color, Offset(s * 0.72f, s * 0.25f), Offset(s * 0.8f, s * 0.25f), strokeWidth = stroke.width)
    drawLine(color, Offset(s * 0.8f, s * 0.25f), Offset(s * 0.8f, s * 0.33f), strokeWidth = stroke.width)
}

private fun DrawScope.drawTechnologyIcon(s: Float, color: Color, stroke: Stroke) {
    // Monitor
    drawRoundRect(color, Offset(s * 0.15f, s * 0.2f), Size(s * 0.7f, s * 0.45f), CornerRadius(s * 0.04f), style = stroke)
    drawLine(color, Offset(s * 0.5f, s * 0.65f), Offset(s * 0.5f, s * 0.78f), strokeWidth = stroke.width)
    drawLine(color, Offset(s * 0.32f, s * 0.78f), Offset(s * 0.68f, s * 0.78f), strokeWidth = stroke.width)
}

private fun DrawScope.drawHangoutIcon(s: Float, color: Color, stroke: Stroke) {
    // Coffee cup
    drawRoundRect(color, Offset(s * 0.22f, s * 0.3f), Size(s * 0.45f, s * 0.5f), CornerRadius(s * 0.03f, s * 0.06f), style = stroke)
    // Handle
    val handlePath = Path().apply {
        moveTo(s * 0.67f, s * 0.4f)
        quadraticBezierTo(s * 0.82f, s * 0.4f, s * 0.82f, s * 0.52f)
        quadraticBezierTo(s * 0.82f, s * 0.64f, s * 0.67f, s * 0.64f)
    }
    drawPath(handlePath, color, style = stroke)
    // Steam
    drawLine(color, Offset(s * 0.38f, s * 0.25f), Offset(s * 0.38f, s * 0.15f), strokeWidth = stroke.width * 0.7f)
    drawLine(color, Offset(s * 0.5f, s * 0.22f), Offset(s * 0.5f, s * 0.12f), strokeWidth = stroke.width * 0.7f)
}

private fun DrawScope.drawCashIcon(s: Float, color: Color, stroke: Stroke) {
    // Dollar bills
    drawRoundRect(color, Offset(s * 0.12f, s * 0.28f), Size(s * 0.7f, s * 0.44f), CornerRadius(s * 0.04f), style = stroke)
    drawRoundRect(color, Offset(s * 0.18f, s * 0.22f), Size(s * 0.7f, s * 0.44f), CornerRadius(s * 0.04f), style = stroke)
    drawCircle(color, s * 0.1f, Offset(s * 0.53f, s * 0.44f), style = stroke)
}

private fun DrawScope.drawBankIcon(s: Float, color: Color, stroke: Stroke) {
    // Bank building
    val roofPath = Path().apply {
        moveTo(s * 0.5f, s * 0.15f)
        lineTo(s * 0.85f, s * 0.35f)
        lineTo(s * 0.15f, s * 0.35f)
        close()
    }
    drawPath(roofPath, color, style = stroke)
    // Pillars
    drawLine(color, Offset(s * 0.3f, s * 0.38f), Offset(s * 0.3f, s * 0.72f), strokeWidth = stroke.width)
    drawLine(color, Offset(s * 0.5f, s * 0.38f), Offset(s * 0.5f, s * 0.72f), strokeWidth = stroke.width)
    drawLine(color, Offset(s * 0.7f, s * 0.38f), Offset(s * 0.7f, s * 0.72f), strokeWidth = stroke.width)
    // Base
    drawLine(color, Offset(s * 0.18f, s * 0.75f), Offset(s * 0.82f, s * 0.75f), strokeWidth = stroke.width * 1.2f)
}

private fun DrawScope.drawEwalletIcon(s: Float, color: Color, stroke: Stroke) {
    // Phone
    drawRoundRect(color, Offset(s * 0.28f, s * 0.12f), Size(s * 0.44f, s * 0.76f), CornerRadius(s * 0.06f), style = stroke)
    // Screen
    drawLine(color, Offset(s * 0.28f, s * 0.25f), Offset(s * 0.72f, s * 0.25f), strokeWidth = stroke.width * 0.6f)
    drawLine(color, Offset(s * 0.28f, s * 0.72f), Offset(s * 0.72f, s * 0.72f), strokeWidth = stroke.width * 0.6f)
    // $ on screen
    drawCircle(color, s * 0.08f, Offset(s * 0.5f, s * 0.48f), style = stroke)
}

private fun DrawScope.drawLaptopIcon(s: Float, color: Color, stroke: Stroke) {
    // Screen
    drawRoundRect(color, Offset(s * 0.18f, s * 0.2f), Size(s * 0.64f, s * 0.4f), CornerRadius(s * 0.04f), style = stroke)
    // Base
    val basePath = Path().apply {
        moveTo(s * 0.1f, s * 0.65f)
        lineTo(s * 0.18f, s * 0.6f)
        lineTo(s * 0.82f, s * 0.6f)
        lineTo(s * 0.9f, s * 0.65f)
        quadraticBezierTo(s * 0.9f, s * 0.72f, s * 0.82f, s * 0.72f)
        lineTo(s * 0.18f, s * 0.72f)
        quadraticBezierTo(s * 0.1f, s * 0.72f, s * 0.1f, s * 0.65f)
    }
    drawPath(basePath, color, style = stroke)
}

private fun DrawScope.drawEmergencyIcon(s: Float, color: Color, stroke: Stroke) {
    // Shield with exclamation
    val shieldPath = Path().apply {
        moveTo(s * 0.5f, s * 0.12f)
        lineTo(s * 0.82f, s * 0.28f)
        quadraticBezierTo(s * 0.82f, s * 0.68f, s * 0.5f, s * 0.88f)
        quadraticBezierTo(s * 0.18f, s * 0.68f, s * 0.18f, s * 0.28f)
        close()
    }
    drawPath(shieldPath, color, style = stroke)
    drawLine(color, Offset(s * 0.5f, s * 0.35f), Offset(s * 0.5f, s * 0.58f), strokeWidth = stroke.width * 1.3f)
    drawCircle(color, s * 0.035f, Offset(s * 0.5f, s * 0.7f))
}

private fun DrawScope.drawTravelIcon(s: Float, color: Color, stroke: Stroke) {
    // Airplane
    val planePath = Path().apply {
        moveTo(s * 0.5f, s * 0.12f)
        lineTo(s * 0.55f, s * 0.35f)
        lineTo(s * 0.85f, s * 0.48f)
        lineTo(s * 0.55f, s * 0.48f)
        lineTo(s * 0.55f, s * 0.68f)
        lineTo(s * 0.65f, s * 0.78f)
        lineTo(s * 0.5f, s * 0.73f)
        lineTo(s * 0.35f, s * 0.78f)
        lineTo(s * 0.45f, s * 0.68f)
        lineTo(s * 0.45f, s * 0.48f)
        lineTo(s * 0.15f, s * 0.48f)
        lineTo(s * 0.45f, s * 0.35f)
        close()
    }
    drawPath(planePath, color, style = stroke)
}

private fun DrawScope.drawTargetIcon(s: Float, color: Color, stroke: Stroke) {
    drawCircle(color, s * 0.35f, Offset(s * 0.5f, s * 0.5f), style = stroke)
    drawCircle(color, s * 0.22f, Offset(s * 0.5f, s * 0.5f), style = stroke)
    drawCircle(color, s * 0.08f, Offset(s * 0.5f, s * 0.5f))
}

private fun DrawScope.drawOtherIcon(s: Float, color: Color, stroke: Stroke) {
    // Three dots
    drawCircle(color, s * 0.06f, Offset(s * 0.3f, s * 0.5f))
    drawCircle(color, s * 0.06f, Offset(s * 0.5f, s * 0.5f))
    drawCircle(color, s * 0.06f, Offset(s * 0.7f, s * 0.5f))
}
