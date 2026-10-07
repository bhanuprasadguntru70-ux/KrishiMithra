package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.KrishiHeaderGreen
import com.example.ui.theme.KrishiPrimaryGreen

data class PricePoint(
    val day: String,
    val price: Double
)

data class CropFinancialData(
    val cropName: String,
    val income: Double,
    val expense: Double,
    val color: Color
)

@Composable
fun CropPriceTrendChart(
    cropName: String,
    currentPrice: Double,
    priceHistory7d: List<PricePoint>,
    priceHistory30d: List<PricePoint> = emptyList(),
    modifier: Modifier = Modifier
) {
    var selectedRange by remember { mutableStateOf("7D") } // 7D or 30D
    val dataPoints = if (selectedRange == "7D") priceHistory7d else if (priceHistory30d.isNotEmpty()) priceHistory30d else priceHistory7d

    if (dataPoints.isEmpty()) return

    val minPrice = dataPoints.minOf { it.price }
    val maxPrice = dataPoints.maxOf { it.price }
    val firstPrice = dataPoints.first().price
    val lastPrice = dataPoints.last().price
    val priceDiff = lastPrice - firstPrice
    val pctChange = if (firstPrice > 0) (priceDiff / firstPrice) * 100 else 0.0
    val isPositive = priceDiff >= 0

    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2EBE4)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header with trend percentage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "📈 $cropName Price Trend",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = KrishiHeaderGreen
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = if (isPositive) KrishiHeaderGreen else Color(0xFFC62828),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${if (isPositive) "+" else ""}₹${priceDiff.toInt()} (${String.format("%.1f", pctChange)}%)",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = if (isPositive) KrishiHeaderGreen else Color(0xFFC62828)
                        )
                        Text(
                            text = " in $selectedRange",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Range Toggle Buttons (7D / 30D)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFEAF5ED))
                        .padding(2.dp)
                ) {
                    listOf("7D", "30D").forEach { range ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (selectedRange == range) KrishiHeaderGreen else Color.Transparent)
                                .clickable { selectedRange = range; selectedIndex = null }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = range,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedRange == range) Color.White else KrishiHeaderGreen
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Selected point preview banner
            val displayPoint = if (selectedIndex != null && selectedIndex!! in dataPoints.indices) dataPoints[selectedIndex!!] else dataPoints.last()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF4F9F5))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Date: ${displayPoint.day}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.DarkGray
                )
                Text(
                    text = "Price: ₹${displayPoint.price.toInt()} / qtl",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = KrishiHeaderGreen
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // CANVAS LINE CHART WITH SMOOTH GRADIENT
            val lineColor = if (isPositive) KrishiHeaderGreen else Color(0xFFC62828)
            val gradientBottom = if (isPositive) Color(0x002E7D32) else Color(0x00C62828)
            val gradientTop = if (isPositive) Color(0x332E7D32) else Color(0x33C62828)

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .pointerInput(dataPoints) {
                        detectTapGestures { offset ->
                            val width = size.width
                            val stepX = width / (dataPoints.size - 1).coerceAtLeast(1)
                            val index = ((offset.x + stepX / 2) / stepX).toInt().coerceIn(0, dataPoints.size - 1)
                            selectedIndex = index
                        }
                    }
            ) {
                val width = size.width
                val height = size.height
                val padding = 16f

                val chartWidth = width - (padding * 2)
                val chartHeight = height - (padding * 2)

                val priceRange = (maxPrice - minPrice).let { if (it == 0.0) 1.0 else it }
                val stepX = chartWidth / (dataPoints.size - 1).coerceAtLeast(1)

                val points = dataPoints.mapIndexed { index, point ->
                    val x = padding + (index * stepX)
                    val normalizedY = (point.price - minPrice) / priceRange
                    val y = height - padding - (normalizedY * chartHeight).toFloat()
                    Offset(x, y)
                }

                // Draw Grid lines
                val gridColor = Color(0xFFE2EBE4)
                for (i in 0..3) {
                    val y = padding + (i * (chartHeight / 3))
                    drawLine(
                        color = gridColor,
                        start = Offset(padding, y),
                        end = Offset(width - padding, y),
                        strokeWidth = 1f
                    )
                }

                // Path for line chart
                val strokePath = Path().apply {
                    if (points.isNotEmpty()) {
                        moveTo(points.first().x, points.first().y)
                        for (i in 0 until points.size - 1) {
                            val p1 = points[i]
                            val p2 = points[i + 1]
                            val controlPoint1 = Offset(p1.x + (p2.x - p1.x) / 2, p1.y)
                            val controlPoint2 = Offset(p1.x + (p2.x - p1.x) / 2, p2.y)
                            cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, p2.x, p2.y)
                        }
                    }
                }

                // Fill Path for Gradient below line
                val fillPath = Path().apply {
                    addPath(strokePath)
                    lineTo(points.last().x, height - padding)
                    lineTo(points.first().x, height - padding)
                    close()
                }

                // Draw Fill
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(gradientTop, gradientBottom),
                        startY = padding,
                        endY = height - padding
                    )
                )

                // Draw Stroke
                drawPath(
                    path = strokePath,
                    color = lineColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Draw Points
                points.forEachIndexed { index, point ->
                    val isSelected = index == selectedIndex
                    drawCircle(
                        color = if (isSelected) Color.White else lineColor,
                        radius = if (isSelected) 6.dp.toPx() else 3.5.dp.toPx(),
                        center = point
                    )
                    drawCircle(
                        color = lineColor,
                        radius = if (isSelected) 4.dp.toPx() else 2.dp.toPx(),
                        center = point,
                        style = if (isSelected) Stroke(width = 2.dp.toPx()) else Fill
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // X-Axis Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                dataPoints.forEachIndexed { idx, pt ->
                    if (dataPoints.size <= 7 || idx % (dataPoints.size / 5) == 0 || idx == dataPoints.lastIndex) {
                        Text(
                            text = pt.day,
                            fontSize = 9.sp,
                            color = if (idx == selectedIndex) KrishiHeaderGreen else Color.Gray,
                            fontWeight = if (idx == selectedIndex) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CropFinancialBarChart(
    cropsData: List<CropFinancialData>,
    modifier: Modifier = Modifier
) {
    if (cropsData.isEmpty()) return

    val maxVal = cropsData.maxOf { maxOf(it.income, it.expense) }.let { if (it == 0.0) 1.0 else it }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2EBE4)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📊 Crop Financial Comparison",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = KrishiHeaderGreen
                )

                // Legend
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(KrishiHeaderGreen))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Income", fontSize = 10.sp, color = Color.DarkGray)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFC62828)))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Expense", fontSize = 10.sp, color = Color.DarkGray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bars list
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                cropsData.forEach { crop ->
                    val incomeRatio = (crop.income / maxVal).toFloat()
                    val expenseRatio = (crop.expense / maxVal).toFloat()
                    val netProfit = crop.income - crop.expense

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(crop.cropName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = KrishiHeaderGreen)
                            Text(
                                text = "Net: ₹${netProfit.toInt()}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (netProfit >= 0) KrishiHeaderGreen else Color(0xFFC62828)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Income Bar
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .height(10.dp)
                                    .fillMaxWidth(incomeRatio.coerceIn(0.05f, 1f))
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(KrishiHeaderGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("₹${crop.income.toInt()}", fontSize = 10.sp, color = Color.Gray)
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Expense Bar
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .height(10.dp)
                                    .fillMaxWidth(expenseRatio.coerceIn(0.05f, 1f))
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFC62828))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("₹${crop.expense.toInt()}", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}
