package id.datzon.glasslab

import android.content.Context
import org.json.JSONObject

data class GlassConfig(
    val refraction: Float = 18f,
    val ior: Float = 1.4f,
    val thickness: Float = 1.1f,
    val blur: Float = 1.5f,
    val aberration: Float = 1.5f,
    val edge: Float = 0.6f,
    val opacity: Float = 0.14f,
    val radius: Float = 27f,
    val shadow: Float = 12f,
    val red: Float = 0.13f,
    val green: Float = 0.12f,
    val blue: Float = 0.11f,
    val pressScale: Float = 0.97f,
    val stiffness: Float = 400f,
    val damping: Float = 0.82f
) {
    fun json(target: String = ""): String = JSONObject()
        .put("engine", "android-agsl-v1")
        .put("target", target)
        .put("refraction", refraction)
        .put("ior", ior)
        .put("thickness", thickness)
        .put("blur", blur)
        .put("aberration", aberration)
        .put("edge", edge)
        .put("opacity", opacity)
        .put("radius", radius)
        .put("shadow", shadow)
        .put("red", red)
        .put("green", green)
        .put("blue", blue)
        .put("pressScale", pressScale)
        .put("stiffness", stiffness)
        .put("damping", damping)
        .toString(2)

    companion object {
        fun parse(raw: String, fallback: GlassConfig = GlassConfig()): GlassConfig =
            runCatching {
                val j = JSONObject(raw)
                fun f(key: String, default: Float) = j.optDouble(key, default.toDouble()).toFloat()
                fallback.copy(
                    refraction = f("refraction", fallback.refraction).coerceIn(0f, 38f),
                    ior = f("ior", fallback.ior).coerceIn(1f, 2.2f),
                    thickness = f("thickness", fallback.thickness).coerceIn(0.2f, 3f),
                    blur = f("blur", fallback.blur).coerceIn(0f, 20f),
                    aberration = f("aberration", fallback.aberration).coerceIn(0f, 8f),
                    edge = f("edge", fallback.edge).coerceIn(0f, 1f),
                    opacity = f("opacity", fallback.opacity).coerceIn(0f, 0.9f),
                    radius = f("radius", fallback.radius).coerceIn(0f, 46f),
                    shadow = f("shadow", fallback.shadow).coerceIn(0f, 32f),
                    red = f("red", fallback.red).coerceIn(0f, 1f),
                    green = f("green", fallback.green).coerceIn(0f, 1f),
                    blue = f("blue", fallback.blue).coerceIn(0f, 1f),
                    pressScale = f("pressScale", fallback.pressScale).coerceIn(0.84f, 1f),
                    stiffness = f("stiffness", fallback.stiffness).coerceIn(100f, 1000f),
                    damping = f("damping", fallback.damping).coerceIn(0.3f, 1f)
                )
            }.getOrDefault(fallback)
    }
}

val GlassPresets: Map<String, GlassConfig> = linkedMapOf(
    "Crystal" to GlassConfig(refraction = 22f, blur = 0f, opacity = 0.09f, edge = 0.9f),
    "Frosted" to GlassConfig(refraction = 8f, blur = 12f, opacity = 0.24f, edge = 0.34f),
    "Liquid" to GlassConfig(refraction = 35f, ior = 1.7f, thickness = 1.8f, aberration = 3.2f, edge = 0.8f),
    "Dark" to GlassConfig(refraction = 13f, blur = 3f, opacity = 0.45f, red = 0.06f, green = 0.06f, blue = 0.07f),
    "Soft" to GlassConfig(refraction = 12f, blur = 2.3f, opacity = 0.14f, shadow = 7f)
)

class GlassStore(context: Context) {
    private val prefs = context.getSharedPreferences("datzon_glass_lab", Context.MODE_PRIVATE)
    fun load(target: String): GlassConfig =
        GlassConfig.parse(prefs.getString("target_" + target, null) ?: "{}", GlassConfig())
    fun save(target: String, config: GlassConfig) {
        prefs.edit().putString("target_" + target, config.json(target)).apply()
    }
    fun presetNames(): List<String> =
        prefs.all.keys.filter { it.startsWith("preset_") }.map { it.removePrefix("preset_") }.sorted()
    fun savePreset(name: String, config: GlassConfig) {
        if (name.isNotBlank()) prefs.edit().putString("preset_" + name.trim(), config.json()).apply()
    }
    fun loadPreset(name: String): GlassConfig? =
        prefs.getString("preset_" + name, null)?.let { GlassConfig.parse(it) }
    fun deletePreset(name: String) { prefs.edit().remove("preset_" + name).apply() }
}
