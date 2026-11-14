package id.datzon.glasslab

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/** Small purpose-built vector set; avoids bundling the entire extended icon catalog. */
object LabIcons {
    private fun icon(name: String, path: String): ImageVector =
        ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f)
            .addPath(
                pathData = PathParser().parsePathString(path).toNodes(),
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.7f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
            .build()

    val Add by lazy { icon("Add", "M12,5 L12,19 M5,12 L19,12") }
    val Close by lazy { icon("Close", "M6,6 L18,18 M18,6 L6,18") }
    val Home by lazy {
        icon("Home", "M3,10 L12,3 L21,10 M5,9 L5,21 L10,21 L10,14 L14,14 L14,21 L19,21 L19,9")
    }
    val Search by lazy {
        icon("Search", "M19,10 A9,9 0,1 1,1,10 A9,9 0,1 1,19,10 M16.5,16.5 L22,22")
    }
    val LibraryMusic by lazy {
        icon(
            "LibraryMusic",
            "M4,4 L4,21 L20,21 M8,3 L8,17 L21,17 L21,3 Z M13,7 L18,6 L18,12 M13,8 L13,13 M10,13 A1.5,1.5 0,1 0,13,13 A1.5,1.5 0,1 0,10,13 M15,12 A1.5,1.5 0,1 0,18,12 A1.5,1.5 0,1 0,15,12",
        )
    }
    val GraphicEq by lazy {
        icon("GraphicEq", "M4,9 L4,15 M8,5 L8,19 M12,2 L12,22 M16,7 L16,17 M20,10 L20,14")
    }
    val ArrowForward by lazy { icon("ArrowForward", "M4,12 L20,12 M14,6 L20,12 L14,18") }
    val Pause by lazy { icon("Pause", "M8,5 L8,19 M16,5 L16,19") }
    val PlayArrow by lazy { icon("PlayArrow", "M7,4 L20,12 L7,20 Z") }
    val SkipNext by lazy { icon("SkipNext", "M5,5 L16,12 L5,19 Z M19,5 L19,19") }
    val SkipPrevious by lazy { icon("SkipPrevious", "M19,5 L8,12 L19,19 Z M5,5 L5,19") }
    val KeyboardArrowDown by lazy { icon("KeyboardArrowDown", "M5,9 L12,16 L19,9") }
    val RestartAlt by lazy { icon("RestartAlt", "M5,9 A8,8 0,1 1,4,15 M5,4 L5,9 L10,9") }
    val Replay by lazy { icon("Replay", "M7,6 A8,8 0,1 1,4,13 M7,2 L7,7 L12,7") }
    val DeleteOutline by lazy {
        icon(
            "DeleteOutline",
            "M4,6 L20,6 M9,6 L9,3 L15,3 L15,6 M6,6 L7,21 L17,21 L18,6 M10,10 L10,17 M14,10 L14,17",
        )
    }
    val Tune by lazy {
        icon(
            "Tune",
            "M3,6 L8,6 M12,6 L21,6 M8,3 L8,9 M3,12 L15,12 M19,12 L21,12 M15,9 L15,15 M3,18 L7,18 M11,18 L21,18 M7,15 L7,21",
        )
    }
}
