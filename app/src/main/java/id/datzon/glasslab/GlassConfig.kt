package id.datzon.glasslab

import android.content.Context
import org.json.JSONObject

const val ENGINE_VERSION = "datzon-agsl-2"

enum class Category(val label: String) {
    OPTICS("Optics"),
    LOOK("Appearance"),
    GEOMETRY("Geometry"),
    ANIMATION("Animation"),
    GESTURE("Gesture"),
    TRANSITION("Transition"),
}

enum class Target(val label: String, val group: String, val material: Boolean = false) {
    MINI("Floating mini player", "Mini player", true),
    MINI_COMPACT("Compact mini player", "Mini player", true),
    ARTWORK("Artwork container", "Mini player"),
    EXPANSION("Expand transition", "Mini player"),
    NAVBAR("Navbar outer shell", "Navbar", true),
    CAPSULE("Active tab capsule", "Navbar", true),
    TABS("Tab indicators", "Navbar"),
    COMPACT("Compact dock", "Navbar"),
    HOME("Home orb", "Navbar", true),
    PLUS("Plus orb", "Plus button", true),
    PLUS_MOTION("Plus press / drag", "Plus button"),
    SCROLL("Scroll morph", "Shared animation"),
    CAPSULE_MOTION("Capsule morph", "Shared animation"),
    RELEASE("Gesture release", "Shared animation"),
    CONTACT("Glass contact", "Shared animation"),
}

enum class Param(
    val label: String,
    val category: Category,
    val min: Float,
    val max: Float,
    val initial: Float,
    val unit: String = "",
) {
    REFRACTION("Refraction strength", Category.OPTICS, 0f, 2f, 1f, "×"),
    IOR("Index of refraction", Category.OPTICS, 1f, 3f, 1.4f),
    THICKNESS("Glass thickness", Category.OPTICS, 0f, 80f, 12f, "dp"),
    BEZEL("Bezel width", Category.OPTICS, 1f, 40f, 8f, "dp"),
    DISTORTION("Distortion amount", Category.OPTICS, 0f, 2f, 1f, "×"),
    FALLOFF("Distortion falloff", Category.OPTICS, .2f, 4f, 1f, "×"),
    ABERRATION("Chromatic aberration", Category.OPTICS, 0f, 6f, .6f, "dp"),
    BLUR("Blur radius", Category.OPTICS, 0f, 20f, 3f, "dp"),
    EDGE_REFRACTION("Edge refraction", Category.OPTICS, 0f, 2f, 1f, "×"),
    OPACITY("Glass opacity", Category.LOOK, 0f, 1f, 1f),
    TINT("Tint opacity", Category.LOOK, 0f, .9f, .15f),
    RED("Tint red", Category.LOOK, 0f, 1f, .045f),
    GREEN("Tint green", Category.LOOK, 0f, 1f, .065f),
    BLUE("Tint blue", Category.LOOK, 0f, 1f, .08f),
    SATURATION("Tint saturation", Category.LOOK, 0f, 2f, 1f, "×"),
    HIGHLIGHT("Highlight brightness", Category.LOOK, 0f, 1.5f, .34f),
    SPECULAR("Specular strength", Category.LOOK, 0f, 2f, .6f),
    EDGE("Edge thickness", Category.LOOK, .2f, 4f, 1.15f, "dp"),
    SOFTNESS("Edge softness", Category.LOOK, .2f, 4f, 1f, "dp"),
    INNER_SHADOW("Inner shadow", Category.LOOK, 0f, .8f, .08f),
    SHADOW("Outer shadow", Category.LOOK, 0f, .8f, .18f),
    SPREAD("Shadow spread", Category.LOOK, -4f, 16f, 0f, "dp"),
    SHADOW_BLUR("Shadow blur", Category.LOOK, 1f, 32f, 6f, "dp"),
    BORDER("Border opacity", Category.LOOK, 0f, 1f, .18f),
    BRIGHTNESS("Backdrop brightness", Category.LOOK, .3f, 1.8f, 1f, "×"),
    WIDTH("Width scale", Category.GEOMETRY, .55f, 1.35f, 1f, "×"),
    HEIGHT("Height scale", Category.GEOMETRY, .65f, 1.6f, 1f, "×"),
    RADIUS("Corner radius", Category.GEOMETRY, 0f, 80f, 30f, "dp"),
    CURVATURE("Capsule curvature", Category.GEOMETRY, 0f, 1f, 1f),
    DEFORMATION("Shape deformation", Category.GEOMETRY, -.2f, .2f, 0f),
    EXPAND_SCALE("Expansion scale", Category.GEOMETRY, 1f, 1.8f, 1.65f, "×"),
    STRETCH("Stretch intensity", Category.GEOMETRY, 0f, 2f, 1f, "×"),
    X("Horizontal offset", Category.GEOMETRY, -60f, 60f, 0f, "dp"),
    Y("Vertical offset", Category.GEOMETRY, -60f, 60f, 0f, "dp"),
    DURATION("Animation duration", Category.ANIMATION, 100f, 1600f, 420f, "ms"),
    STIFFNESS("Spring stiffness", Category.ANIMATION, 60f, 1200f, 420f),
    DAMPING("Spring damping", Category.ANIMATION, .35f, 1.2f, .75f),
    OVERSHOOT("Overshoot", Category.ANIMATION, 0f, 1f, .15f),
    BOUNCE("Bounce intensity", Category.ANIMATION, 0f, 1f, .25f),
    PRESS("Press scale", Category.ANIMATION, .85f, 1.12f, 1.04f, "×"),
    SPEED("Morph speed", Category.ANIMATION, .25f, 2f, 1f, "×"),
    SHAPE_EASE("Shape interpolation", Category.ANIMATION, .4f, 2f, 1f, "γ"),
    POSITION_EASE("Position interpolation", Category.ANIMATION, .4f, 2f, 1f, "γ"),
    OPACITY_EASE("Opacity interpolation", Category.ANIMATION, .4f, 2f, 1f, "γ"),
    LONG_PRESS("Long press delay", Category.GESTURE, 120f, 900f, 450f, "ms"),
    SENSITIVITY("Drag sensitivity", Category.GESTURE, .25f, 2f, 1f, "×"),
    DRAG_THRESHOLD("Drag threshold", Category.GESTURE, 2f, 32f, 8f, "dp"),
    SCROLL_THRESHOLD("Scroll threshold", Category.GESTURE, 24f, 180f, 64f, "dp"),
    SNAP("Snap threshold", Category.GESTURE, .15f, .85f, .42f),
    VELOCITY("Release velocity", Category.GESTURE, 200f, 2200f, 900f, "dp/s"),
    FLING("Fling influence", Category.GESTURE, 0f, 1f, 1f),
    RESISTANCE("Gesture resistance", Category.GESTURE, .1f, 1f, .38f),
    DOCK_PROGRESS("Normal → compact", Category.TRANSITION, 0f, 1f, 0f),
    REVERSE_PROGRESS("Compact → normal", Category.TRANSITION, 0f, 1f, 1f),
    PLAYER_PROGRESS("Mini → full player", Category.TRANSITION, 0f, 1f, 0f),
    CAPSULE_PROGRESS("Capsule expansion", Category.TRANSITION, 0f, 1f, 0f),
    CONTACT_RADIUS("Contact neck radius", Category.GEOMETRY, 0f, 32f, 16f, "dp"),
    CONTACT_DISTANCE("Contact proximity", Category.GESTURE, 1f, 24f, 9f, "dp"),
    CONTACT_PROGRESS("Contact bridge", Category.TRANSITION, 0f, 1f, 0f),
}

data class GlassConfig(val values: Map<Param, Float> = emptyMap()) {
    operator fun get(p: Param): Float = values[p] ?: p.initial

    fun with(p: Param, value: Float): GlassConfig {
        require(value.isFinite()) { "Non-finite value: ${p.name}" }
        return copy(values = values + (p to value.coerceIn(p.min, p.max)))
    }

    fun objectJson(): JSONObject =
        JSONObject().also { j -> Param.entries.forEach { j.put(it.name, get(it).toDouble()) } }

    fun json(target: Target): String =
        JSONObject()
            .put("engine", ENGINE_VERSION)
            .put("schema", 2)
            .put("target", target.name)
            .put("values", objectJson())
            .toString(2)

    companion object {
        fun parse(j: JSONObject): GlassConfig {
            var c = GlassConfig()
            Param.entries.forEach { p ->
                if (j.has(p.name)) c = c.with(p, j.getDouble(p.name).toFloat())
            }
            return c
        }
    }
}

fun originalConfig(t: Target): GlassConfig {
    var c = GlassConfig()
    // Geometry/motion values from musik main e80c557 (v0.6.52). Optical engine differs;
    // these are a documented native translation, never an exact source preset claim.
    if (t == Target.MINI) c = c.with(Param.RADIUS, 12f)
    if (t == Target.ARTWORK) c = c.with(Param.RADIUS, 4f)
    if (t == Target.EXPANSION) c = c.with(Param.DURATION, 650f)
    if (t == Target.CAPSULE)
        c = c.with(Param.RADIUS, 25f).with(Param.BLUR, 0f).with(Param.TINT, .08f)
    if (t == Target.CAPSULE_MOTION) c = c.with(Param.DURATION, 240f).with(Param.STIFFNESS, 650f)
    if (t == Target.RELEASE) c = c.with(Param.DAMPING, .64f).with(Param.STIFFNESS, 430f)
    if (t == Target.PLUS_MOTION) c = c.with(Param.DAMPING, .65f).with(Param.STIFFNESS, 550f)
    if (t == Target.NAVBAR || t == Target.HOME || t == Target.PLUS || t == Target.MINI_COMPACT)
        c = c.with(Param.BLUR, 8f)
    return c
}

fun factoryPresets(target: Target): Map<String, GlassConfig> =
    linkedMapOf(
        "DATZON Original" to originalConfig(target),
        "Crystal Clear" to
            originalConfig(target)
                .with(Param.BLUR, 0f)
                .with(Param.TINT, .04f)
                .with(Param.HIGHLIGHT, .5f),
        "Frosted Glass" to originalConfig(target).with(Param.BLUR, 16f).with(Param.TINT, .24f),
        "Strong Refraction" to
            originalConfig(target)
                .with(Param.REFRACTION, 1.8f)
                .with(Param.IOR, 1.8f)
                .with(Param.THICKNESS, 28f)
                .with(Param.ABERRATION, 2f),
        "Soft Liquid Glass" to
            originalConfig(target)
                .with(Param.REFRACTION, .65f)
                .with(Param.BLUR, 4f)
                .with(Param.HIGHLIGHT, .2f),
        "Dark Liquid Glass" to originalConfig(target).with(Param.TINT, .62f).with(Param.BLUR, 5f),
        "Apple-like Glass" to
            originalConfig(target)
                .with(Param.TINT, .08f)
                .with(Param.BLUR, 6f)
                .with(Param.SPECULAR, 1f)
                .with(Param.HIGHLIGHT, .48f),
    )

data class LabPreset(val name: String, val configs: Map<Target, GlassConfig>) {
    fun json(): String =
        JSONObject()
            .put("schema", 2)
            .put("engine", ENGINE_VERSION)
            .put("name", name)
            .put(
                "components",
                JSONObject().also { j ->
                    configs.forEach { (t, c) -> j.put(t.name, c.objectJson()) }
                },
            )
            .toString(2)

    companion object {
        fun parse(raw: String): LabPreset {
            val j = JSONObject(raw)
            require(j.getInt("schema") == 2 && j.getString("engine") == ENGINE_VERSION) {
                "Unsupported preset engine/schema"
            }
            val all = j.optJSONObject("components")
            if (all != null) {
                require(all.length() > 0) { "Empty preset" }
                val entries =
                    all.keys().asSequence().associate { key ->
                        Target.valueOf(key) to GlassConfig.parse(all.getJSONObject(key))
                    }
                return LabPreset(j.optString("name", "Imported"), entries)
            }
            val t = Target.valueOf(j.getString("target"))
            return LabPreset("Imported", mapOf(t to GlassConfig.parse(j.getJSONObject("values"))))
        }
    }
}

class GlassStore(context: Context) {
    private val prefs = context.getSharedPreferences("glass-lab-v2", Context.MODE_PRIVATE)

    fun load(): Map<Target, GlassConfig> =
        runCatching { LabPreset.parse(prefs.getString("session", "")!!).configs }
            .getOrDefault(emptyMap())

    fun save(configs: Map<Target, GlassConfig>) {
        prefs.edit().putString("session", LabPreset("Session", configs).json()).apply()
    }

    fun names(): List<String> =
        prefs.all.keys
            .filter { it.startsWith("preset:") }
            .map { it.removePrefix("preset:") }
            .sorted()

    fun savePreset(p: LabPreset) {
        require(p.name.isNotBlank())
        prefs.edit().putString("preset:" + p.name.take(64), p.json()).apply()
    }

    fun preset(name: String): LabPreset = LabPreset.parse(prefs.getString("preset:$name", "")!!)

    fun delete(name: String) {
        prefs.edit().remove("preset:$name").apply()
    }
}
