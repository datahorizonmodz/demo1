package id.datzon.glasslab

import android.graphics.ImageDecoder
import android.view.Choreographer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.*
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.debounce

@OptIn(kotlinx.coroutines.FlowPreview::class)
@Composable
fun GlassLabScreen() {
    val context = LocalContext.current
    val store = remember { GlassStore(context) }
    val scope = rememberCoroutineScope()
    val motion = remember { LabMotion(scope) }
    val options = remember { PreviewOptions() }
    var configs by remember {
        mutableStateOf(Target.entries.associateWith { originalConfig(it) } + store.load())
    }
    var target by remember { mutableStateOf(Target.MINI) }
    var page by remember { mutableStateOf("Preview") }
    var previewSettings by remember { mutableStateOf(false) }
    var linked by remember { mutableStateOf(false) }
    var before by remember { mutableStateOf(configs) }
    var compare by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var saveState by remember { mutableStateOf("Saved locally") }
    val latestConfigs by rememberUpdatedState(configs)
    DisposableEffect(store) { onDispose { store.save(latestConfigs) } }
    SideEffect { motion.configs = configs }
    LaunchedEffect(Unit) {
        snapshotFlow { configs }
            .debounce(350)
            .collect { snapshot ->
                store.save(snapshot)
                saveState = "Saved locally"
            }
    }
    fun apply(values: Map<Target, GlassConfig>) {
        configs = configs + values
        saveState = "Saving…"
    }
    fun select(t: Target) {
        target = t
        when (t) {
            Target.MINI_COMPACT,
            Target.COMPACT,
            Target.HOME -> motion.setCompact(true)
            Target.MINI,
            Target.NAVBAR,
            Target.CAPSULE,
            Target.CAPSULE_MOTION,
            Target.TABS -> motion.setCompact(false)
            else -> Unit
        }
    }
    val photo =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null)
                scope.launch {
                    runCatching {
                            withContext(Dispatchers.IO) {
                                ImageDecoder.decodeBitmap(
                                    ImageDecoder.createSource(context.contentResolver, uri)
                                ) { decoder, info, _ ->
                                    decoder.setTargetSampleSize(
                                        (maxOf(info.size.width, info.size.height) / 1600)
                                            .coerceAtLeast(1)
                                    )
                                }
                            }
                        }
                        .onSuccess {
                            options.photo = it
                            options.background = "Photo"
                        }
                        .onFailure { options.fallback = "Photo could not be opened" }
                }
        }
    BoxWithConstraints(Modifier.fillMaxSize().background(Ink).safeDrawingPadding()) {
        val available = maxHeight
        val open = page != "Preview" || previewSettings
        val panelTarget =
            if (open) if (expanded) available * .57f else (available * .44f).coerceAtMost(365.dp)
            else 0.dp
        val panelHeight by animateDpAsState(panelTarget, tween(300), label = "editor lift")
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().height(53.dp).padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "DATZON",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        letterSpacing = 3.sp,
                        color = Color.White,
                    )
                    Text(
                        "GLASS LAB  /  NATIVE MATERIAL STUDIO",
                        fontSize = 8.sp,
                        letterSpacing = 1.sp,
                        color = Muted,
                    )
                }
                Text(
                    if (compare) "BEFORE" else "LIVE",
                    color = Accent,
                    fontSize = 9.sp,
                    letterSpacing = 1.sp,
                )
                IconButton(
                    onClick = {
                        previewSettings = !previewSettings
                        page = "Preview"
                    }
                ) {
                    Icon(
                        LabIcons.Tune,
                        "Preview settings",
                        tint = Muted,
                        modifier = Modifier.size(21.dp),
                    )
                }
            }
            Row(
                Modifier.fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 7.dp)
                    .selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                listOf("Preview", "Components", "Animation", "Presets", "Export").forEach { p ->
                    val selected = page == p && !previewSettings
                    Box(
                        Modifier.weight(1f)
                            .height(34.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected) Accent.copy(alpha = .14f) else Color.Transparent
                            )
                            .selectable(selected, role = Role.Tab) {
                                page = p
                                previewSettings = false
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            p,
                            fontSize = 11.sp,
                            maxLines = 1,
                            color = if (selected) Accent else Muted,
                        )
                    }
                }
            }
            Box(Modifier.weight(1f).fillMaxWidth()) {
                NativePreview(
                    motion,
                    if (compare) before else configs,
                    options,
                    target,
                    Modifier.fillMaxSize(),
                )
                if (options.fps || options.frameTime)
                    FrameStats(
                        options.fps,
                        options.frameTime,
                        Modifier.align(Alignment.TopStart).padding(12.dp),
                    )
                if (options.fallback != null)
                    Text(
                        "FALLBACK · ${options.fallback}",
                        Modifier.align(Alignment.TopStart)
                            .padding(12.dp)
                            .background(Ink, CircleShape)
                            .padding(10.dp),
                        color = Color(0xFFFFC0AA),
                        fontSize = 9.sp,
                    )
            }
            if (panelHeight > 0.dp)
                Surface(
                    color = Color(0xFF191D20),
                    shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
                    modifier = Modifier.fillMaxWidth().height(panelHeight),
                ) {
                    Column {
                        Box(
                            Modifier.fillMaxWidth()
                                .height(23.dp)
                                .pointerInput(Unit) {
                                    detectVerticalDragGestures { change, amount ->
                                        change.consume()
                                        if (amount < -5) expanded = true
                                        if (amount > 5) expanded = false
                                    }
                                }
                                .clickable { expanded = !expanded },
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                Modifier.size(34.dp, 3.dp)
                                    .background(Color(0xFF4C5255), CircleShape)
                            )
                        }
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    if (previewSettings) "Preview controls"
                                    else
                                        when (page) {
                                            "Components" -> target.label
                                            "Animation" -> "Motion studio"
                                            "Presets" -> "Material library"
                                            else -> "Take it to DATZON"
                                        },
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                )
                                Text(saveState, fontSize = 9.sp, color = Muted)
                            }
                            IconButton(
                                onClick = {
                                    page = "Preview"
                                    previewSettings = false
                                }
                            ) {
                                Icon(
                                    LabIcons.Close,
                                    "Close editor",
                                    tint = Muted,
                                    modifier = Modifier.size(19.dp),
                                )
                            }
                        }
                        Column(
                            Modifier.fillMaxWidth()
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                                .padding(start = 18.dp, end = 18.dp, bottom = 20.dp)
                        ) {
                            if (previewSettings) {
                                ChoiceRow(
                                    listOf("Real app", "Isolated"),
                                    if (options.isolated) "Isolated" else "Real app",
                                ) {
                                    options.isolated = it == "Isolated"
                                }
                                SmallLabel("Backdrop")
                                ChoiceRow(
                                    listOf(
                                        "Artwork",
                                        "Gradient",
                                        "Dark",
                                        "Light",
                                        "Lines",
                                        "Text",
                                        "Contrast",
                                        "Photo",
                                    ),
                                    options.background,
                                ) {
                                    if (it == "Photo" && options.photo == null)
                                        photo.launch(arrayOf("image/*"))
                                    else options.background = it
                                }
                                if (options.background == "Photo")
                                    TextButton(onClick = { photo.launch(arrayOf("image/*")) }) {
                                        Text("Choose another photo")
                                    }
                                LabToggle("Glass", options.glass) { options.glass = it }
                                LabToggle("Component bounds", options.bounds) {
                                    options.bounds = it
                                }
                                LabToggle("Touch points", options.touches) { options.touches = it }
                                LabToggle("Animation progress", options.progress) {
                                    options.progress = it
                                }
                                LabToggle("FPS · UI cadence", options.fps) { options.fps = it }
                                LabToggle("Frame time · UI cadence", options.frameTime) {
                                    options.frameTime = it
                                }
                                LabToggle("High quality optics", options.quality) {
                                    options.quality = it
                                }
                                Note(
                                    "Balanced quality caps blur at 8 dp and uses five taps. FPS measures UI frame callbacks, not GPU benchmark performance."
                                )
                            } else
                                when (page) {
                                    "Components" ->
                                        ComponentEditor(
                                            target,
                                            ::select,
                                            configs.getValue(target),
                                            { c ->
                                                if (linked && target.material) {
                                                    val source = configs.getValue(target)
                                                    val changes =
                                                        Param.entries
                                                            .filter {
                                                                it.category == Category.OPTICS ||
                                                                    it.category == Category.LOOK
                                                            }
                                                            .filter { source[it] != c[it] }
                                                    apply(
                                                        Target.entries
                                                            .filter { it.material }
                                                            .associateWith { t ->
                                                                changes.fold(configs.getValue(t)) {
                                                                    cfg,
                                                                    p ->
                                                                    cfg.with(p, c[p])
                                                                }
                                                            } + mapOf(target to c)
                                                    )
                                                } else apply(mapOf(target to c))
                                            },
                                            motion,
                                            linked,
                                            { linked = it },
                                        )
                                    "Animation" -> AnimationPlayground(motion)
                                    "Presets" ->
                                        PresetEditor(
                                            target,
                                            configs,
                                            store,
                                            ::apply,
                                            { compare = it },
                                            { before = configs },
                                        )
                                    "Export" -> ExportPanel(target, configs, ::apply)
                                }
                        }
                    }
                }
        }
    }
}

@Composable
private fun FrameStats(showFps: Boolean, showTime: Boolean, modifier: Modifier) {
    var fps by remember { mutableIntStateOf(0) }
    var ms by remember { mutableFloatStateOf(0f) }
    DisposableEffect(Unit) {
        val choreographer = Choreographer.getInstance()
        val callback =
            object : Choreographer.FrameCallback {
                var last = 0L
                var started = 0L
                var frames = 0
                var elapsed = 0L

                override fun doFrame(time: Long) {
                    if (last != 0L) {
                        elapsed += time - last
                        frames++
                    }
                    last = time
                    if (started == 0L) started = time
                    if (time - started >= 500_000_000L && frames > 0) {
                        ms = elapsed / frames / 1_000_000f
                        fps = (1_000_000_000.0 * frames / elapsed).toInt()
                        started = time
                        elapsed = 0
                        frames = 0
                    }
                    choreographer.postFrameCallback(this)
                }
            }
        choreographer.postFrameCallback(callback)
        onDispose { choreographer.removeFrameCallback(callback) }
    }
    Text(
        listOfNotNull(
                if (showFps) "$fps FPS" else null,
                if (showTime) "%.1f ms".format(ms) else null,
            )
            .joinToString(" · "),
        modifier.background(Ink.copy(alpha = .85f), CircleShape).padding(10.dp, 6.dp),
        color = Accent,
        fontSize = 9.sp,
    )
}
