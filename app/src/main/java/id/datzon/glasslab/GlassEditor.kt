package id.datzon.glasslab

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import java.util.Locale

fun relevantParams(t: Target): List<Param> {
    val motion =
        listOf(
            Param.DURATION,
            Param.SPEED,
            Param.STIFFNESS,
            Param.DAMPING,
            Param.OVERSHOOT,
            Param.BOUNCE,
        )
    val geometry =
        listOf(
            Param.WIDTH,
            Param.HEIGHT,
            Param.RADIUS,
            Param.CURVATURE,
            Param.DEFORMATION,
            Param.X,
            Param.Y,
        )
    return when {
        t.material ->
            Param.entries.filter {
                it.category == Category.OPTICS || it.category == Category.LOOK
            } +
                geometry +
                if (t == Target.HOME)
                    motion.filterNot { it == Param.DURATION || it == Param.SPEED } +
                        listOf(
                            Param.PRESS,
                            Param.SENSITIVITY,
                            Param.RESISTANCE,
                            Param.DRAG_THRESHOLD,
                        )
                else emptyList()
        t == Target.ARTWORK -> listOf(Param.WIDTH, Param.HEIGHT, Param.RADIUS, Param.X, Param.Y)
        t == Target.EXPANSION ->
            motion +
                listOf(
                    Param.POSITION_EASE,
                    Param.WIDTH,
                    Param.X,
                    Param.Y,
                    Param.SENSITIVITY,
                    Param.DRAG_THRESHOLD,
                    Param.SNAP,
                    Param.VELOCITY,
                    Param.FLING,
                    Param.PLAYER_PROGRESS,
                )
        t == Target.SCROLL ->
            motion +
                listOf(
                    Param.SHAPE_EASE,
                    Param.POSITION_EASE,
                    Param.OPACITY_EASE,
                    Param.SCROLL_THRESHOLD,
                    Param.DOCK_PROGRESS,
                    Param.REVERSE_PROGRESS,
                )
        t == Target.CAPSULE_MOTION ->
            motion +
                listOf(
                    Param.EXPAND_SCALE,
                    Param.STRETCH,
                    Param.LONG_PRESS,
                    Param.DRAG_THRESHOLD,
                    Param.SENSITIVITY,
                    Param.SNAP,
                    Param.CAPSULE_PROGRESS,
                )
        t == Target.PLUS_MOTION ->
            motion +
                listOf(
                    Param.PRESS,
                    Param.STRETCH,
                    Param.SENSITIVITY,
                    Param.RESISTANCE,
                    Param.DRAG_THRESHOLD,
                )
        t == Target.RELEASE -> listOf(Param.STIFFNESS, Param.DAMPING, Param.OVERSHOOT, Param.BOUNCE)
        t == Target.CONTACT ->
            listOf(
                Param.CONTACT_RADIUS,
                Param.CONTACT_DISTANCE,
                Param.DURATION,
                Param.SPEED,
                Param.CONTACT_PROGRESS,
            )
        t == Target.COMPACT ->
            listOf(
                Param.WIDTH,
                Param.HEIGHT,
                Param.DEFORMATION,
                Param.X,
                Param.Y,
                Param.DOCK_PROGRESS,
            )
        else -> listOf(Param.OPACITY, Param.WIDTH, Param.HEIGHT, Param.X, Param.Y)
    }
}

@Composable
fun ChoiceRow(items: List<String>, selected: String, onSelect: (String) -> Unit) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        items.forEach { name ->
            FilterChip(
                selected = name == selected,
                onClick = { onSelect(name) },
                label = { Text(name, fontSize = 11.sp) },
                shape = CircleShape,
            )
        }
    }
}

@Composable
fun SmallLabel(text: String) {
    Text(
        text.uppercase(),
        fontSize = 9.sp,
        letterSpacing = 1.6.sp,
        color = Muted,
        modifier = Modifier.padding(top = 13.dp, bottom = 5.dp),
    )
}

@Composable
fun Note(text: String) {
    Text(
        text,
        color = Muted,
        fontSize = 11.sp,
        lineHeight = 17.sp,
        modifier = Modifier.padding(vertical = 8.dp),
    )
}

@Composable
fun LabToggle(label: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 42.dp).clickable { onChange(!value) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 12.sp,
            modifier = Modifier.weight(1f),
        )
        Switch(checked = value, onCheckedChange = onChange, modifier = Modifier.height(30.dp))
    }
}

@Composable
fun ParamSlider(p: Param, value: Float, onValue: (Float) -> Unit, onReset: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(p.label, fontSize = 12.sp, modifier = Modifier.weight(1f))
            Text(
                String.format(Locale.US, if (p.max > 20) "%.0f" else "%.2f", value) +
                    if (p.unit.isEmpty()) "" else " ${p.unit}",
                color = Accent,
                fontSize = 11.sp,
            )
            IconButton(onClick = onReset, modifier = Modifier.size(32.dp)) {
                Icon(
                    LabIcons.RestartAlt,
                    "Reset ${p.label}",
                    modifier = Modifier.size(14.dp),
                    tint = Muted,
                )
            }
        }
        Slider(
            value = value.coerceIn(p.min, p.max),
            onValueChange = onValue,
            valueRange = p.min..p.max,
            modifier = Modifier.fillMaxWidth().height(30.dp),
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${p.min} ${p.unit}", fontSize = 8.sp, color = Muted.copy(alpha = .6f))
            Text("${p.max} ${p.unit}", fontSize = 8.sp, color = Muted.copy(alpha = .6f))
        }
    }
}

@Composable
fun ComponentEditor(
    target: Target,
    onTarget: (Target) -> Unit,
    config: GlassConfig,
    onConfig: (GlassConfig) -> Unit,
    m: LabMotion,
    linked: Boolean,
    onLink: (Boolean) -> Unit,
) {
    var category by remember(target) { mutableStateOf(relevantParams(target).first().category) }
    var group by remember(target) { mutableStateOf(target.group) }
    ChoiceRow(Target.entries.map { it.group }.distinct(), group) {
        group = it
        onTarget(Target.entries.first { t -> t.group == it })
    }
    ChoiceRow(Target.entries.filter { it.group == group }.map { it.label }, target.label) { s ->
        onTarget(Target.entries.first { it.label == s })
    }
    val params = relevantParams(target)
    val categories = params.map { it.category }.distinct()
    ChoiceRow(categories.map { it.label }, category.label) { label ->
        category = categories.first { it.label == label }
    }
    if (category == Category.ANIMATION)
        Note(
            "Duration applies to easing. Stiffness, damping, bounce and overshoot apply to spring mode / gesture release."
        )
    params
        .filter { it.category == category }
        .forEach { p ->
            val value =
                when (p) {
                    Param.DOCK_PROGRESS -> m.dock.value
                    Param.REVERSE_PROGRESS -> 1 - m.dock.value
                    Param.PLAYER_PROGRESS -> m.player.value
                    Param.CAPSULE_PROGRESS -> m.capsulePress.value
                    Param.CONTACT_PROGRESS -> m.contact.value
                    else -> config[p]
                }
            fun update(v: Float) {
                when (p) {
                    Param.DOCK_PROGRESS -> m.dock.scrub(v)
                    Param.REVERSE_PROGRESS -> m.dock.scrub(1 - v)
                    Param.PLAYER_PROGRESS -> m.player.scrub(v)
                    Param.CAPSULE_PROGRESS -> m.capsulePress.scrub(v)
                    Param.CONTACT_PROGRESS -> m.contact.scrub(v)
                    else -> Unit
                }
                onConfig(config.with(p, v))
            }
            ParamSlider(p, value, { update(it) }, { update(originalConfig(target)[p]) })
        }
    if (target.material) LabToggle("Link optics across glass components", linked, onLink)
    TextButton(onClick = { onConfig(originalConfig(target)) }) { Text("Reset ${target.label}") }
}

@Composable
fun AnimationPlayground(m: LabMotion) {
    SmallLabel("Animation playground")
    ChoiceRow(
        listOf(
            "Scroll morph",
            "Full player",
            "Capsule press",
            "Capsule move",
            "Plus stretch",
            "Contact",
        ),
        m.timeline,
    ) {
        m.timeline = it
    }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            "Timeline",
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            modifier = Modifier.weight(1f),
        )
        Text("${(m.timelineProgress()*100).toInt()}%", color = Accent, fontSize = 12.sp)
    }
    Slider(m.timelineProgress(), { m.scrubTimeline(it) })
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        listOf(0f, .3f, .6f, .85f, 1f).forEach { v ->
            TextButton(onClick = { m.scrubTimeline(v) }) {
                Text("${(v*100).toInt()}%", fontSize = 10.sp)
            }
        }
    }
    ChoiceRow(listOf("Easing", "Spring"), if (m.springMode) "Spring" else "Easing") {
        m.springMode = it == "Spring"
    }
    LabToggle("Loop test", m.loop) { m.loop = it }
    listOf(
            listOf("Compact", "Expanded"),
            listOf("Open full player", "Close full player"),
            listOf("Capsule press", "Capsule move"),
            listOf("Plus stretch", "Contact"),
        )
        .forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pair.forEach { name ->
                    OutlinedButton(
                        onClick = { m.test(name) },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(8.dp, 10.dp),
                    ) {
                        Text("Test $name", fontSize = 11.sp, maxLines = 1)
                    }
                }
            }
        }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(onClick = { m.cancelAll() }, modifier = Modifier.weight(1f)) {
            Text("Reset animation")
        }
        Button(onClick = { m.test(m.lastTest) }, modifier = Modifier.weight(1f)) {
            Icon(LabIcons.Replay, null, Modifier.size(16.dp))
            Text("Replay")
        }
    }
    Note(
        "Timeline controls the exact channels used by the dock, artwork, lens and orb. Hold at any progress and keep editing optics."
    )
}

@Composable
fun PresetEditor(
    target: Target,
    configs: Map<Target, GlassConfig>,
    store: GlassStore,
    onApply: (Map<Target, GlassConfig>) -> Unit,
    onCompare: (Boolean) -> Unit,
    onCapture: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf(store.names()) }
    var chosen by remember { mutableStateOf("Custom") }
    var all by remember { mutableStateOf(false) }
    SmallLabel("Factory presets")
    Note(
        "DATZON Original uses source geometry/motion from v0.6.52. Optics are a translation to this engine; see source provenance in Export."
    )
    LabToggle("Apply factory preset to all glass", all) { all = it }
    ChoiceRow(factoryPresets(target).keys.toList() + "Custom", chosen) { n ->
        chosen = n
        if (n != "Custom") {
            onCapture()
            onApply(
                if (all)
                    Target.entries
                        .filter { it.material }
                        .associateWith { factoryPresets(it).getValue(n) }
                else mapOf(target to factoryPresets(target).getValue(n))
            )
        }
    }
    SmallLabel("Compare before / after")
    OutlinedButton(onClick = onCapture, modifier = Modifier.fillMaxWidth()) {
        Text("Capture current settings as Before")
    }
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = CircleShape,
        modifier =
            Modifier.fillMaxWidth().pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onCompare(true)
                        try {
                            awaitRelease()
                        } finally {
                            onCompare(false)
                        }
                    }
                )
            },
    ) {
        Text(
            "Hold to inspect Before",
            Modifier.padding(15.dp),
            fontSize = 12.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = Accent,
        )
    }
    SmallLabel("Save complete preset")
    OutlinedTextField(
        name,
        { name = it.take(64) },
        label = { Text("Preset name") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
    )
    Button(
        onClick = {
            store.savePreset(LabPreset(name.trim(), configs))
            saved = store.names()
            chosen = "Custom"
        },
        enabled = name.isNotBlank(),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text("Save all component settings")
    }
    saved.forEach { n ->
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextButton(
                onClick = {
                    onCapture()
                    onApply(store.preset(n).configs)
                    chosen = "Custom"
                },
                modifier = Modifier.weight(1f),
            ) {
                Text(n)
            }
            IconButton(
                onClick = {
                    store.delete(n)
                    saved = store.names()
                }
            ) {
                Icon(LabIcons.DeleteOutline, "Delete $n", tint = Muted)
            }
        }
    }
}
