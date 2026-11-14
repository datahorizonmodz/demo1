package id.datzon.glasslab

import android.graphics.*
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
// Robolectric SDK 26–34 uses an Android 12 native shader parser that rejects
// child.eval; see upstream #9691. API 35 exercises the actual AGSL child input.
@Config(sdk = [35])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NativeOpticsTest {
    @Test
    fun shaderCompilesAndRefractionDisplacesRealBackdropPixels() {
        val pattern = Bitmap.createBitmap(320, 240, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(pattern)
        (0 until 320 step 8).forEach { x ->
            canvas.drawRect(
                x.toFloat(),
                0f,
                x + 8f,
                240f,
                Paint().apply { color = if (x % 16 == 0) Color.WHITE else Color.rgb(20, 45, 80) },
            )
        }
        val base =
            GlassConfig()
                .with(Param.TINT, 0f)
                .with(Param.BLUR, 0f)
                .with(Param.HIGHLIGHT, 0f)
                .with(Param.SPECULAR, 0f)
                .with(Param.BORDER, 0f)
                .with(Param.INNER_SHADOW, 0f)
                .with(Param.ABERRATION, 0f)
                .with(Param.SHADOW, 0f)
        fun render(config: GlassConfig): Bitmap {
            val renderer = GlassRenderer()
            renderer.effect(
                listOf(GlassSurface(Rect(32f, 40f, 288f, 200f), 30f, config)),
                Size(320f, 240f),
                1f,
                0f,
                true,
                true,
            )
            renderer.shader.setInputShader(
                "backdrop",
                BitmapShader(pattern, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP),
            )
            return hardwareCapture(320, 240) { canvas ->
                canvas.drawRect(0f, 0f, 320f, 240f, Paint().apply { shader = renderer.shader })
            }
        }
        val off = render(base.with(Param.REFRACTION, 0f))
        val on =
            render(base.with(Param.REFRACTION, 2f).with(Param.THICKNESS, 30f).with(Param.IOR, 1.8f))
        var displaced = 0
        for (y in 65..175) for (x in 35..284) if (off.getPixel(x, y) != on.getPixel(x, y))
            displaced++
        assertTrue("Expected real displaced samples, got $displaced", displaced > 100)
        assertEquals(off.getPixel(160, 120), on.getPixel(160, 120))
        assertEquals(pattern.getPixel(160, 120), off.getPixel(160, 120))
    }
}
