package id.datzon.glasslab

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.graphics.RenderEffect as AndroidRenderEffect
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.drawLayer
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.dp

/**
 * Shader is applied to a live replay of the underlying Home layer, not an image
 * of an unrelated background. Parameters in [GlassConfig] are wired to this
 * exact shader so exported presets have a reproducible native implementation.
 */
const val GLASS_AGSL = """
uniform shader composable;
uniform float2 bounds;
uniform float refraction;
uniform float ior;
uniform float thickness;
uniform float aberration;
uniform float highlight;

half4 main(float2 xy) {
    float2 b = max(bounds, float2(1.0));
    float2 uv = xy / b;
    float2 inside = min(uv, 1.0 - uv);
    float rim = 1.0 - smoothstep(0.005, 0.24, min(inside.x, inside.y));
    float2 vectorFromCenter = uv - float2(0.5);
    float displacement = refraction * max(ior - 1.0, 0.0) * thickness * rim;
    float2 origin = clamp(xy + vectorFromCenter * displacement, float2(0.0), b - float2(1.0));
    float2 shift = float2(aberration * rim, 0.0);
    half4 center = composable.eval(origin);
    half r = composable.eval(clamp(origin + shift, float2(0.0), b - float2(1.0))).r;
    half bch = composable.eval(clamp(origin - shift, float2(0.0), b - float2(1.0))).b;
    return half4(
        min(r + half(rim * highlight * 0.12), half(1.0)),
        min(center.g + half(rim * highlight * 0.11), half(1.0)),
        min(bch + half(rim * highlight * 0.13), half(1.0)),
        center.a
    );
}
"""

@Composable
fun GlassPane(
    backdrop: GraphicsLayer,
    config: GlassConfig,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    var origin by remember { mutableStateOf(Offset.Zero) }
    val shape = RoundedCornerShape(config.radius.dp)
    val shader = remember { RuntimeShader(GLASS_AGSL) }
    var isPressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) config.pressScale else 1f,
        animationSpec = spring(dampingRatio = config.damping, stiffness = config.stiffness),
        label = "glass spring"
    )

    Box(
        modifier = modifier
            .graphicsLayer { scaleX = pressScale; scaleY = pressScale }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        isPressed = event.changes.any { it.pressed }
                    }
                }
            }
            .onGloballyPositioned { origin = it.positionInRoot() }
            .shadow(config.shadow.dp, shape)
            .clip(shape)
    ) {
        if (enabled) {
            Box(
                Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        shader.setFloatUniform("bounds", size.width.coerceAtLeast(1f), size.height.coerceAtLeast(1f))
                        shader.setFloatUniform("refraction", config.refraction)
                        shader.setFloatUniform("ior", config.ior)
                        shader.setFloatUniform("thickness", config.thickness)
                        shader.setFloatUniform("aberration", config.aberration)
                        shader.setFloatUniform("highlight", config.edge)
                        val distortion = AndroidRenderEffect.createRuntimeShaderEffect(shader, "composable")
                        val effect = if (config.blur > 0.1f) {
                            val blur = AndroidRenderEffect.createBlurEffect(
                                config.blur, config.blur, Shader.TileMode.CLAMP
                            )
                            AndroidRenderEffect.createChainEffect(distortion, blur)
                        } else distortion
                        renderEffect = effect.asComposeRenderEffect()
                    }
                    .drawBehind {
                        translate(-origin.x, -origin.y) { drawLayer(backdrop) }
                    }
            )
        }
        Box(
            Modifier.matchParentSize().background(
                Color(config.red, config.green, config.blue, if (enabled) config.opacity else 0.82f)
            )
        )
        Box(
            Modifier.matchParentSize().border(
                BorderStroke(
                    1.dp,
                    Color.White.copy(alpha = if (enabled) (config.edge * 0.68f + 0.09f).coerceIn(0f, 0.9f) else 0.12f)
                ),
                shape
            )
        )
        content()
    }
}
