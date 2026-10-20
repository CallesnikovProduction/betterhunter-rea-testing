package dev.betterhunter.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale

enum class HunterGlyph { Search, Filters, Sort, Back, Close, Chevron, Briefcase }

@Composable
fun HunterIcon(kind: HunterGlyph, tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        scale(size.width / 24f, size.height / 24f, pivot = androidx.compose.ui.geometry.Offset.Zero) {
            val line = Stroke(1.8f, cap = StrokeCap.Round)
            val p = Path()
            when (kind) {
                HunterGlyph.Search -> {
                    drawCircle(tint, 6.5f, androidx.compose.ui.geometry.Offset(10.5f, 10.5f), style = line)
                    p.moveTo(15.5f, 15.5f); p.lineTo(21f, 21f)
                }
                HunterGlyph.Filters -> {
                    p.moveTo(4f, 7f); p.lineTo(20f, 7f)
                    p.moveTo(4f, 17f); p.lineTo(20f, 17f)
                    drawCircle(tint, 2.5f, androidx.compose.ui.geometry.Offset(9f, 7f), style = line)
                    drawCircle(tint, 2.5f, androidx.compose.ui.geometry.Offset(15f, 17f), style = line)
                }
                HunterGlyph.Sort -> {
                    p.moveTo(6f, 5f); p.lineTo(6f, 19f); p.moveTo(2f, 9f); p.lineTo(6f, 5f); p.lineTo(10f, 9f)
                    p.moveTo(18f, 19f); p.lineTo(18f, 5f); p.moveTo(14f, 15f); p.lineTo(18f, 19f); p.lineTo(22f, 15f)
                }
                HunterGlyph.Back -> { p.moveTo(13f, 5f); p.lineTo(6f, 12f); p.lineTo(13f, 19f); p.moveTo(6f, 12f); p.lineTo(21f, 12f) }
                HunterGlyph.Close -> { p.moveTo(6f, 6f); p.lineTo(18f, 18f); p.moveTo(18f, 6f); p.lineTo(6f, 18f) }
                HunterGlyph.Chevron -> { p.moveTo(9f, 5f); p.lineTo(16f, 12f); p.lineTo(9f, 19f) }
                HunterGlyph.Briefcase -> {
                    p.moveTo(4f, 7f); p.lineTo(20f, 7f); p.lineTo(20f, 20f); p.lineTo(4f, 20f); p.close()
                    p.moveTo(8f, 7f); p.lineTo(8f, 3f); p.lineTo(16f, 3f); p.lineTo(16f, 7f)
                    p.moveTo(4f, 12f); p.lineTo(20f, 12f)
                }
            }
            drawPath(p, tint, style = line)
        }
    }
}
