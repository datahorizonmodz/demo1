package id.datzon.glasslab

import android.graphics.RenderEffect as AndroidRenderEffect
import android.graphics.RuntimeShader
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/**
 * SDF smooth union and edge optics adapted from DATZON Music's dock. Surface-profile / Snell
 * refraction informed by Archis Vaze (MIT; see notices). All coordinates are preview-local device
 * pixels. Source contains no glass readers.
 */
const val GLASS_AGSL =
    """
/*
MIT License

Copyright (c) 2026 Archis Vaze

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
uniform shader backdrop;
uniform float2 viewport;
uniform float density;
uniform float count;
uniform float join;
uniform float quality;
uniform float glassEnabled;
uniform float4 rects[4];
uniform float4 geometry[4]; // radius, edge width, softness, unused
uniform float4 optics[4]; // strength, ior, thickness, bezel
uniform float4 optics2[4]; // distortion, falloff, aberration, blur
uniform float4 look[4]; // opacity, tint alpha, highlight, specular
uniform float4 tint[4]; // rgb, saturation
uniform float4 shadow[4]; // inner, outer, spread, blur
uniform float4 detail[4]; // edge refraction, border, brightness, unused
float sdBox(float2 p, float4 r, float corner) {
    float2 halfSize = max((r.zw-r.xy)*0.5, float2(0.5));
    float rad = clamp(corner,0.0,min(halfSize.x,halfSize.y));
    float2 q = abs(p-(r.xy+r.zw)*0.5)-halfSize+rad;
    return length(max(q,float2(0.0)))+min(max(q.x,q.y),0.0)-rad;
}
float unite(float a,float b,float k) {
    k=max(k,0.001);
    float h=clamp(0.5+0.5*(b-a)/k,0.0,1.0);
    return mix(b,a,h)-k*h*(1.0-h);
}
float field(float2 p) {
    float d=100000.0;
    for(int i=0;i<4;i++) {
        if(float(i)<count) d=unite(d,sdBox(p,rects[i],geometry[i].x),join);
    }
    return d;
}
half4 sampleAt(float2 p) { return backdrop.eval(clamp(p,float2(0.0),viewport-float2(1.0))); }
half4 blurred(float2 p,float r) {
    if(r<0.2) return sampleAt(p);
    half4 c=sampleAt(p)*half(0.4);
    c+=(sampleAt(p+float2(r,0.0))+sampleAt(p-float2(r,0.0))+
        sampleAt(p+float2(0.0,r))+sampleAt(p-float2(0.0,r)))*half(0.15);
    if(quality>0.5) {
        c=c*half(0.7)+(sampleAt(p+float2(r,r))+sampleAt(p+float2(-r,r))+
          sampleAt(p+float2(r,-r))+sampleAt(p-float2(r,r)))*half(0.075);
    }
    return c;
}
float profile(float t) { float s=1.0-t; return pow(max(1.0-s*s*s*s,0.0001),0.25); }
half4 main(float2 p) {
    float d=field(p);
    float4 o=optics[0], z=optics2[0], l=look[0], s=shadow[0], dt=detail[0], tc4=tint[0];
    float2 g=geometry[0].yz*density;
    float closest=100000.0;
    for(int i=0;i<4;i++) {
        if(float(i)<count) {
            float raw=sdBox(p,rects[i],geometry[i].x);
            if(raw<closest) {
                closest=raw;
                o=optics[i]; z=optics2[i]; l=look[i]; s=shadow[i]; dt=detail[i]; tc4=tint[i];
                g=geometry[i].yz*density;
            }
        }
    }
    float cover=1.0-smoothstep(-max(g.y,0.2),max(g.y,0.2),d);
    if(d>max(2.0,s.w*3.0+s.z)*density) return half4(0.0);
    float sh=s.y*exp(-max(field(p-float2(0.0,4.0*density))-s.z*density,0.0)/max(s.w*density,0.5))*(1.0-cover);
    if(cover<0.001) return half4(0.0,0.0,0.0,sh);
    if(glassEnabled<0.5) return half4(half3(0.07)*half(cover),half(cover));
    float e=max(0.3*density,0.3);
    float2 n=float2(field(p+float2(e,0.0))-field(p-float2(e,0.0)),field(p+float2(0.0,e))-field(p-float2(0.0,e)));
    n=n/max(length(n),0.001);
    float bezel=max(o.w*density,1.0);
    float t=pow(clamp(-d/bezel,0.0,1.0),z.y);
    float h=profile(t);
    float slope=(profile(min(t+0.001,1.0))-h)/0.001;
    float angle=atan(slope*o.z*density/bezel);
    float refracted=asin(clamp(sin(angle)/o.y,-1.0,1.0));
    float displacement=min(h*o.z*density*(tan(angle)-tan(refracted)),72.0*density)*o.x*z.x*dt.x;
    float2 uv=p-n*displacement;
    float rim=exp(-abs(d)/max(g.x,0.1));
    float aberration=z.z*density*(1.0-t);
    half4 base=blurred(uv,z.w*density);
    if(aberration>0.05) {
        base.r=blurred(uv+n*aberration,z.w*density).r;
        base.b=blurred(uv-n*aberration,z.w*density).b;
    }
    float3 tc=tc4.rgb;
    float lum=dot(tc,float3(0.213,0.715,0.072));
    tc=mix(float3(lum),tc,tc4.w);
    float3 color=mix(float3(base.rgb)*dt.z,tc,l.y);
    color*=1.0-s.x*(1.0-t);
    float light=max(dot(n,float2(-0.66,-0.75)),0.0);
    color+=rim*(l.z*(0.15+light*0.85)+l.w*pow(light,12.0)*0.5);
    color=mix(color,float3(0.95),rim*dt.y*0.4);
    color=mix(float3(sampleAt(p).rgb),color,l.x);
    color=clamp(color,0.0,1.0);
    return half4(half3(color)*half(cover),half(cover+sh*(1.0-cover)));
}
"""

@Stable
class GlassBackdrop(val layer: GraphicsLayer) {
    var frame by mutableIntStateOf(0)
        private set

    private var sequence = 0

    fun recorded() {
        frame = ++sequence
    }
}

@Composable
fun rememberGlassBackdrop(): GlassBackdrop {
    val layer = rememberGraphicsLayer()
    return remember(layer) { GlassBackdrop(layer) }
}

/** Optional underlay is recorded into the second-stage lens source, never itself. */
fun Modifier.recordBackdrop(source: GlassBackdrop, underlay: GlassBackdrop? = null): Modifier =
    drawWithContent {
        underlay?.frame
        source.layer.record {
            if (underlay != null) drawLayer(underlay.layer)
            this@drawWithContent.drawContent()
        }
        drawLayer(source.layer)
        source.recorded()
    }

data class GlassSurface(val rect: Rect, val radius: Float, val config: GlassConfig)

internal class GlassRenderer {
    val shader = RuntimeShader(GLASS_AGSL)
    private val data = Array(8) { FloatArray(16) }
    private val names =
        arrayOf("rects", "geometry", "optics", "optics2", "look", "tint", "shadow", "detail")

    fun effect(
        surfaces: List<GlassSurface>,
        viewport: Size,
        density: Float,
        join: Float,
        quality: Boolean,
        enabled: Boolean,
    ): RenderEffect {
        data.forEach { it.fill(0f) }
        surfaces.take(4).forEachIndexed { i, surface ->
            val c = surface.config
            fun set(array: Int, a: Float, b: Float, d: Float, e: Float) {
                val n = i * 4
                data[array][n] = a
                data[array][n + 1] = b
                data[array][n + 2] = d
                data[array][n + 3] = e
            }
            val r = surface.rect
            set(0, r.left, r.top, r.right, r.bottom)
            set(1, surface.radius, c[Param.EDGE], c[Param.SOFTNESS], 0f)
            set(2, c[Param.REFRACTION], c[Param.IOR], c[Param.THICKNESS], c[Param.BEZEL])
            set(
                3,
                c[Param.DISTORTION],
                c[Param.FALLOFF],
                c[Param.ABERRATION],
                if (quality) c[Param.BLUR] else c[Param.BLUR].coerceAtMost(8f),
            )
            set(4, c[Param.OPACITY], c[Param.TINT], c[Param.HIGHLIGHT], c[Param.SPECULAR])
            set(5, c[Param.RED], c[Param.GREEN], c[Param.BLUE], c[Param.SATURATION])
            set(6, c[Param.INNER_SHADOW], c[Param.SHADOW], c[Param.SPREAD], c[Param.SHADOW_BLUR])
            set(7, c[Param.EDGE_REFRACTION], c[Param.BORDER], c[Param.BRIGHTNESS], 0f)
        }
        names.forEachIndexed { i, name -> shader.setFloatUniform(name, data[i]) }
        shader.setFloatUniform("viewport", viewport.width, viewport.height)
        shader.setFloatUniform("density", density)
        shader.setFloatUniform("count", surfaces.size.coerceAtMost(4).toFloat())
        shader.setFloatUniform("join", join * density)
        shader.setFloatUniform("quality", if (quality) 1f else 0f)
        shader.setFloatUniform("glassEnabled", if (enabled) 1f else 0f)
        return AndroidRenderEffect.createRuntimeShaderEffect(shader, "backdrop")
            .asComposeRenderEffect()
    }
}

/** Replays GPU drawing commands; no PixelCopy, Bitmap, readback or WebView. */
@Composable
fun GlassField(
    source: GlassBackdrop,
    surfaces: () -> List<GlassSurface>,
    modifier: Modifier = Modifier,
    join: () -> Float = { 0f },
    enabled: Boolean = true,
    quality: Boolean = true,
    bounds: Boolean = false,
    onFailure: (String) -> Unit = {},
) {
    val renderer = remember { runCatching { GlassRenderer() } }
    val layer = rememberGraphicsLayer()
    val density = LocalDensity.current.density
    var failed by remember { mutableStateOf(false) }
    val failure = renderer.exceptionOrNull()
    LaunchedEffect(failure) {
        if (failure != null) {
            failed = true
            onFailure(failure.message ?: "Shader unavailable")
        }
    }
    Canvas(modifier) {
        source.frame
        val shapes = surfaces()
        if (shapes.isNotEmpty() && size.width > 0 && size.height > 0) {
            val r = renderer.getOrNull()
            if (r != null && !failed) {
                runCatching {
                        layer.record { drawLayer(source.layer) }
                        layer.renderEffect =
                            r.effect(shapes, size, density, join(), quality, enabled)
                        drawLayer(layer)
                    }
                    .onFailure {
                        failed = true
                        onFailure(it.message ?: "GPU fallback")
                    }
            } else
                shapes.forEach { s ->
                    drawRoundRect(
                        Color(0xED18212A),
                        s.rect.topLeft,
                        s.rect.size,
                        CornerRadius(s.radius),
                    )
                }
            if (bounds)
                shapes.forEach { s ->
                    drawRect(
                        Color(0xFFB6F8CE),
                        s.rect.topLeft,
                        s.rect.size,
                        style = Stroke(1.dp.toPx()),
                    )
                }
        }
    }
}
