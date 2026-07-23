package com.james.mathwakealarm

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SneezingCatLogo(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = w * 0.045f
        val head = Path().apply {
            moveTo(w * 0.20f, h * 0.43f)
            lineTo(w * 0.25f, h * 0.13f)
            lineTo(w * 0.40f, h * 0.27f)
            cubicTo(w * 0.48f, h * 0.23f, w * 0.57f, h * 0.23f, w * 0.64f, h * 0.27f)
            lineTo(w * 0.79f, h * 0.13f)
            lineTo(w * 0.84f, h * 0.43f)
            cubicTo(w * 0.91f, h * 0.67f, w * 0.72f, h * 0.82f, w * 0.52f, h * 0.82f)
            cubicTo(w * 0.32f, h * 0.82f, w * 0.13f, h * 0.67f, w * 0.20f, h * 0.43f)
        }
        drawPath(head, color, style = Stroke(stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawLine(color, Offset(w * .33f, h * .48f), Offset(w * .42f, h * .50f), stroke, StrokeCap.Round)
        drawLine(color, Offset(w * .62f, h * .50f), Offset(w * .71f, h * .48f), stroke, StrokeCap.Round)
        drawCircle(color, radius = stroke * .75f, center = Offset(w * .52f, h * .59f))
        drawLine(color, Offset(w * .52f, h * .61f), Offset(w * .48f, h * .68f), stroke * .8f, StrokeCap.Round)
        drawLine(color, Offset(w * .52f, h * .61f), Offset(w * .56f, h * .68f), stroke * .8f, StrokeCap.Round)
        drawLine(color, Offset(w * .31f, h * .60f), Offset(w * .10f, h * .56f), stroke * .75f, StrokeCap.Round)
        drawLine(color, Offset(w * .31f, h * .67f), Offset(w * .08f, h * .68f), stroke * .75f, StrokeCap.Round)
        drawLine(color, Offset(w * .72f, h * .60f), Offset(w * .87f, h * .56f), stroke * .75f, StrokeCap.Round)
        drawLine(color, Offset(w * .72f, h * .67f), Offset(w * .88f, h * .68f), stroke * .75f, StrokeCap.Round)
        drawCircle(color, radius = stroke * .85f, center = Offset(w * .91f, h * .55f))
        drawCircle(color, radius = stroke * .65f, center = Offset(w * .96f, h * .67f))
        val drop = Path().apply {
            moveTo(w * .87f, h * .72f)
            cubicTo(w * .82f, h * .80f, w * .84f, h * .90f, w * .89f, h * .91f)
            cubicTo(w * .95f, h * .88f, w * .94f, h * .80f, w * .87f, h * .72f)
        }
        drawPath(drop, color, style = Stroke(stroke * .75f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun BrandHeader(modifier: Modifier = Modifier, compact: Boolean = false) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        SneezingCatLogo(Modifier.size(if (compact) 42.dp else 54.dp))
        Spacer(Modifier.width(10.dp))
        Text(
            text = "TAZALARM",
            fontSize = if (compact) 21.sp else 25.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.2.sp
        )
    }
}
