package id.datzon.glasslab

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassEditor(
    target: String,
    config: GlassConfig,
    onConfig: (GlassConfig) -> Unit,
    store: GlassStore,
    enabled: Boolean,
    onToggle: () -> Unit,
    onTarget: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var tab by remember { mutableIntStateOf(0) }
    var presetName by remember { mutableStateOf("") }
    var savedNames by remember { mutableStateOf(store.presetNames()) }
    val sectionNames = listOf("OPTICS", "LOOK", "MOTION", "PRESETS", "EXPORT")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1C1B19),
        contentColor = Color(0xFFF5EFE5),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFF77736B)) },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            Modifier.fillMaxWidth().heightIn(max = 670.dp)
                .verticalScroll(rememberScrollState())
                .padding(start = 19.dp, end = 19.dp, bottom = 36.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("LIQUID GLASS STUDIO", fontSize = 10.sp,
                        letterSpacing = 2.sp, color = Color(0xFFB7B4AB))
                    Text("Customize glass", fontSize = 25.sp, fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = onToggle) {
                    Text(if (enabled) "ON ●" else "OFF ○")
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChip(
                    selected = target == "mini", onClick = { onTarget("mini") },
                    label = { Text("Mini player") }
                )
                FilterChip(
                    selected = target == "navbar", onClick = { onTarget("navbar") },
                    label = { Text("Navbar") }
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                sectionNames.forEachIndexed { idx, name ->
                    FilterChip(
                        selected = idx == tab, onClick = { tab = idx },
                        label = { Text(name, fontSize = 11.sp) }
                    )
                }
            }
            Spacer(Modifier.height(13.dp))
            when (tab) {
                0 -> {
                    Description("Real backdrop sampling via Compose GraphicsLayer + GPU RuntimeShader.")
                    Param("Refraction", config.refraction, 0f..38f, "px") {
                        onConfig(config.copy(refraction = it))
                    }
                    Param("Index of refraction", config.ior, 1f..2.2f, "") {
                        onConfig(config.copy(ior = it))
                    }
                    Param("Glass thickness", config.thickness, 0.2f..3f, "×") {
                        onConfig(config.copy(thickness = it))
                    }
                    Param("Blur radius", config.blur, 0f..20f, "px") {
                        onConfig(config.copy(blur = it))
                    }
                    Param("Chromatic aberration", config.aberration, 0f..8f, "px") {
                        onConfig(config.copy(aberration = it))
                    }
                }
                1 -> {
                    Param("Corner radius", config.radius, 0f..46f, "dp") {
                        onConfig(config.copy(radius = it))
                    }
                    Param("Tint opacity", config.opacity, 0f..0.9f, "") {
                        onConfig(config.copy(opacity = it))
                    }
                    Param("Edge highlight", config.edge, 0f..1f, "") {
                        onConfig(config.copy(edge = it))
                    }
                    Param("Shadow elevation", config.shadow, 0f..32f, "dp") {
                        onConfig(config.copy(shadow = it))
                    }
                    Text("RGB tint", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Param("Red", config.red, 0f..1f, "") { onConfig(config.copy(red = it)) }
                    Param("Green", config.green, 0f..1f, "") { onConfig(config.copy(green = it)) }
                    Param("Blue", config.blue, 0f..1f, "") { onConfig(config.copy(blue = it)) }
                }
                2 -> {
                    Description("Spring settings are saved in the exported preset for native interaction animations.")
                    Param("Press scale", config.pressScale, 0.84f..1f, "×") {
                        onConfig(config.copy(pressScale = it))
                    }
                    Param("Spring stiffness", config.stiffness, 100f..1000f, "") {
                        onConfig(config.copy(stiffness = it))
                    }
                    Param("Spring damping", config.damping, 0.3f..1f, "") {
                        onConfig(config.copy(damping = it))
                    }
                }
                3 -> {
                    Text("Factory presets", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GlassPresets.forEach { (name, preset) ->
                            OutlinedButton(onClick = { onConfig(preset) }) { Text(name) }
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    Text("Save custom preset", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = presetName, onValueChange = { presetName = it },
                        label = { Text("Preset name") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(9.dp))
                    Button(
                        onClick = {
                            store.savePreset(presetName, config)
                            savedNames = store.presetNames()
                            Toast.makeText(context, "Preset tersimpan", Toast.LENGTH_SHORT).show()
                        },
                        enabled = presetName.isNotBlank()
                    ) { Text("Save preset") }
                    savedNames.forEach { name ->
                        Row(Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            TextButton(onClick = { store.loadPreset(name)?.let(onConfig) }) {
                                Text(name)
                            }
                            TextButton(onClick = {
                                store.deletePreset(name)
                                savedNames = store.presetNames()
                            }) { Text("Delete", color = Color(0xFFE8A6A2)) }
                        }
                    }
                }
                4 -> {
                    Description("Copy preset or implementation source. Kotlin snippet uses the same GlassPane and AGSL engine as the running app.")
                    ExportAction("Copy preset JSON") {
                        copy(context, "glass.json", config.json(target))
                    }
                    ExportAction("Copy Kotlin config + usage") {
                        copy(context, "GlassPane.kt", kotlinSnippet(config, target))
                    }
                    ExportAction("Copy exact AGSL shader") {
                        copy(context, "glass.agsl", GLASS_AGSL)
                    }
                    Spacer(Modifier.height(18.dp))
                    ExportAction("Import JSON from clipboard") {
                        val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val raw = manager.primaryClip?.getItemAt(0)?.coerceToText(context)?.toString()
                        if (!raw.isNullOrBlank() && raw.trimStart().startsWith("{")) {
                            onConfig(GlassConfig.parse(raw, config))
                            Toast.makeText(context, "Preset imported", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Clipboard tidak berisi JSON", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.10f))
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = { onConfig(GlassConfig()) }) { Text("Reset to default") }
                Button(onClick = onDismiss) { Text("Done") }
            }
        }
    }
}

@Composable
private fun Description(text: String) {
    Text(text, color = Color(0xFFBEBAB0), fontSize = 12.sp, lineHeight = 18.sp)
    Spacer(Modifier.height(10.dp))
}

@Composable
private fun Param(
    title: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    suffix: String,
    onChange: (Float) -> Unit
) {
    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, fontSize = 13.sp, color = Color(0xFFE6E2DA))
            Text(String.format(java.util.Locale.US, "%.2f", value) + suffix,
                fontSize = 12.sp, color = Color(0xFFAED1C1))
        }
        Slider(
            value = value.coerceIn(range.start, range.endInclusive),
            onValueChange = onChange,
            valueRange = range,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFCFEDD4),
                activeTrackColor = Color(0xFFB7D9C3),
                inactiveTrackColor = Color(0xFF4B4B47)
            )
        )
    }
}

@Composable
private fun ExportAction(title: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) { Text(title) }
}

private fun copy(context: Context, label: String, value: String) {
    val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    manager.setPrimaryClip(ClipData.newPlainText(label, value))
    Toast.makeText(context, "Copied: " + label, Toast.LENGTH_SHORT).show()
}

fun kotlinSnippet(c: GlassConfig, target: String): String = buildString {
    appendLine("// DATZON Glass Lab export | target: " + target)
    appendLine("// Required: GlassConfig.kt + GlassEngine.kt from this repository")
    appendLine("val glass = GlassConfig(")
    appendLine("    refraction = " + c.refraction + "f,")
    appendLine("    ior = " + c.ior + "f,")
    appendLine("    thickness = " + c.thickness + "f,")
    appendLine("    blur = " + c.blur + "f,")
    appendLine("    aberration = " + c.aberration + "f,")
    appendLine("    edge = " + c.edge + "f,")
    appendLine("    opacity = " + c.opacity + "f,")
    appendLine("    radius = " + c.radius + "f,")
    appendLine("    shadow = " + c.shadow + "f,")
    appendLine("    red = " + c.red + "f,")
    appendLine("    green = " + c.green + "f,")
    appendLine("    blue = " + c.blue + "f,")
    appendLine("    pressScale = " + c.pressScale + "f,")
    appendLine("    stiffness = " + c.stiffness + "f,")
    appendLine("    damping = " + c.damping + "f")
    appendLine(")")
    appendLine("// Inside @Composable, with a recorded GraphicsLayer of Home content:")
    appendLine("GlassPane(backdrop = backdropLayer, config = glass, enabled = true) {")
    appendLine("    // Your " + target + " content, e.g. Row { ... }")
    appendLine("}")
}
