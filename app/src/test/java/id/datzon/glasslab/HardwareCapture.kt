package id.datzon.glasslab

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.HardwareRenderer
import android.graphics.RenderNode

/** Native HWUI readback is test-only; production never reads a backdrop bitmap. */
fun hardwareCapture(width: Int, height: Int, draw: (Canvas) -> Unit): Bitmap {
    val node = RenderNode("glass-test")
    node.setPosition(0, 0, width, height)
    val canvas = node.beginRecording(width, height)
    draw(canvas)
    node.endRecording()
    val method =
        HardwareRenderer::class
            .java
            .getDeclaredMethod(
                "createHardwareBitmap",
                RenderNode::class.java,
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
            )
    method.isAccessible = true
    val hardware = method.invoke(null, node, width, height) as Bitmap
    return hardware.copy(Bitmap.Config.ARGB_8888, false)
}
