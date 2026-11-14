package id.datzon.glasslab

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import kotlin.math.*

val Ink = Color(0xFF101214)
val Muted = Color(0xFFABB0AF)
val Accent = Color(0xFFCAF3DC)

data class DemoSong(val title: String, val artist: String, val color: Color, val second: Color)

val DemoSongs =
    listOf(
        DemoSong("Ruang Senja", "DATZON Sessions", Color(0xFFCA9561), Color(0xFF3C3434)),
        DemoSong("Blue Hours", "The Coastline", Color(0xFF66C1DD), Color(0xFF123650)),
        DemoSong("Afterglow", "Nara & The Waves", Color(0xFFB193D8), Color(0xFF3A2849)),
        DemoSong("Pulang Perlahan", "Ruang Nada", Color(0xFFCCD5AF), Color(0xFF34594C)),
        DemoSong("Langit Tangerine", "Studio Selatan", Color(0xFFE98E5A), Color(0xFF702C41)),
        DemoSong("Orbit", "Midnight Assembly", Color(0xFF9BB2EE), Color(0xFF232E58)),
        DemoSong("Arah Angin", "Jalur Sunyi", Color(0xFF91C9BD), Color(0xFF294D50)),
        DemoSong("Sisi Kota", "Soft Frequencies", Color(0xFFD4B589), Color(0xFF544331)),
    )

@Stable
class PreviewOptions {
    var isolated by mutableStateOf(false)
    var background by mutableStateOf("Artwork")
    var photo by mutableStateOf<Bitmap?>(null)
    var glass by mutableStateOf(true)
    var bounds by mutableStateOf(false)
    var touches by mutableStateOf(false)
    var progress by mutableStateOf(false)
    var fps by mutableStateOf(false)
    var frameTime by mutableStateOf(false)
    var quality by mutableStateOf(true)
    var fallback by mutableStateOf<String?>(null)
}

@Composable
fun CoverArtwork(index: Int, modifier: Modifier = Modifier, corner: Float = 12f) {
    val song = DemoSongs[index.mod(DemoSongs.size)]
    Box(
        modifier
            .clip(RoundedCornerShape(corner.dp))
            .background(Brush.linearGradient(listOf(song.color, song.second)))
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val d = size.minDimension
            drawCircle(
                Color.White.copy(alpha = .18f),
                d * .34f,
                Offset(size.width * .7f, size.height * .28f),
            )
            drawCircle(
                song.second.copy(alpha = .65f),
                d * .50f,
                Offset(size.width * .25f, size.height * .85f),
            )
            repeat(9) { i ->
                val x = d * (.1f + i * .12f)
                drawLine(
                    Color.White.copy(alpha = .17f),
                    Offset(x, 0f),
                    Offset(x - d * .4f, size.height),
                    d * .012f,
                )
            }
            drawCircle(Color.White.copy(alpha = .4f), d * .23f, center, style = Stroke(d * .005f))
        }
        if (corner < 20f)
            Text(
                "D / ${index+1}",
                Modifier.align(Alignment.TopStart).padding(10.dp),
                color = Color.White.copy(alpha = .7f),
                fontSize = 10.sp,
                letterSpacing = 1.sp,
            )
    }
}

@Composable
private fun PreviewBackground(options: PreviewOptions, m: LabMotion) {
    val song = DemoSongs[m.song.mod(DemoSongs.size)]
    val dominant by animateColorAsState(song.second, label = "artwork ambient")
    Canvas(Modifier.fillMaxSize()) {
        when (options.background) {
            "Photo" -> {
                val image = options.photo
                if (image != null) {
                    val bitmap = image.asImageBitmap()
                    val scale = max(size.width / bitmap.width, size.height / bitmap.height)
                    val dest =
                        IntSize(
                            (bitmap.width * scale).roundToInt(),
                            (bitmap.height * scale).roundToInt(),
                        )
                    drawImage(
                        bitmap,
                        dstOffset =
                            IntOffset(
                                ((size.width - dest.width) / 2).toInt(),
                                ((size.height - dest.height) / 2).toInt(),
                            ),
                        dstSize = dest,
                    )
                } else drawRect(Brush.linearGradient(listOf(dominant, Ink)))
            }
            "Light" -> drawRect(Color(0xFFEDEDE4))
            "Dark" -> drawRect(Ink)
            "Lines",
            "Contrast" -> {
                drawRect(if (options.background == "Lines") Color(0xFF394E56) else Color.White)
                repeat((size.width / 16.dp.toPx()).toInt() + 1) { i ->
                    drawRect(
                        if (i % 2 == 0) Color(0xFF12151B) else Accent,
                        Offset(i * 16.dp.toPx(), 0f),
                        Size(8.dp.toPx(), size.height),
                    )
                }
            }
            "Text" -> {
                drawRect(Color(0xFFCFD4C1))
            }
            "Gradient" ->
                drawRect(
                    Brush.linearGradient(
                        listOf(Color(0xFF754C99), Color(0xFF32778F), Color(0xFFB99A58))
                    )
                )
            else ->
                drawRect(Brush.verticalGradient(listOf(dominant, dominant.copy(alpha = .7f), Ink)))
        }
    }
    if (options.background == "Text")
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp)) {
            repeat(30) {
                Text(
                    "DATZON  Aa  012345   LIQUID",
                    color = Color(0xFF15271F),
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
        }
}

@Composable
private fun HomeContent(m: LabMotion, options: PreviewOptions) {
    val density = LocalDensity.current.density
    val state = rememberLazyListState()
    val connection =
        remember(m, density) {
            object : NestedScrollConnection {
                override fun onPostScroll(
                    consumed: Offset,
                    available: Offset,
                    source: NestedScrollSource,
                ): Offset {
                    m.scroll(consumed.y / density, source == NestedScrollSource.UserInput)
                    return Offset.Zero
                }
            }
        }
    LaunchedEffect(state) {
        var previous = true
        snapshotFlow { state.firstVisibleItemIndex == 0 && state.firstVisibleItemScrollOffset == 0 }
            .collect { top ->
                if (top && !previous) m.top()
                previous = top
            }
    }
    LazyColumn(
        state = state,
        modifier = Modifier.fillMaxSize().nestedScroll(connection),
        contentPadding = PaddingValues(bottom = 205.dp),
    ) {
        item {
            Row(
                Modifier.fillMaxWidth().padding(20.dp, 20.dp, 20.dp, 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(shape = CircleShape, color = Accent, modifier = Modifier.size(38.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("D", color = Ink, fontWeight = FontWeight.Black)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "YOUR DAILY ROTATION",
                        fontSize = 9.sp,
                        letterSpacing = 2.sp,
                        color = Color.White.copy(alpha = .65f),
                    )
                    Text(
                        "Selamat datang.",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
                Icon(LabIcons.GraphicEq, null, tint = Accent)
            }
            Row(
                Modifier.padding(start = 20.dp, bottom = 22.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf("Semua", "Musik", "Untuk kamu").forEachIndexed { i, s ->
                    Surface(
                        color = if (i == 0) Accent else Color.White.copy(alpha = .08f),
                        shape = CircleShape,
                    ) {
                        Text(
                            s,
                            Modifier.padding(14.dp, 8.dp),
                            fontSize = 12.sp,
                            color = if (i == 0) Ink else Color.White,
                        )
                    }
                }
            }
        }
        itemsIndexed(
            listOf(
                "Dibuat untuk kamu",
                "Top 50 Indonesia",
                "Dengarkan lagi",
                "From the community",
                "Menemani malam",
                "Pilihan hari ini",
            )
        ) { section, title ->
            Column {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (section == 0) "MUSIK YANG NGERTI KAMU"
                            else "DATZON SELECTS / 0${section+1}",
                            fontSize = 9.sp,
                            letterSpacing = 1.5.sp,
                            color = Color.White.copy(alpha = .5f),
                        )
                        Text(
                            title,
                            color = Color.White,
                            fontSize = 23.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Icon(
                        LabIcons.ArrowForward,
                        null,
                        tint = Color.White.copy(alpha = .6f),
                        modifier = Modifier.size(20.dp),
                    )
                }
                LazyRow(
                    contentPadding = PaddingValues(20.dp, 15.dp, 20.dp, 30.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    items(8) { j ->
                        val index = (j + section * 2) % DemoSongs.size
                        val song = DemoSongs[index]
                        Column(
                            Modifier.width(144.dp).clickable {
                                m.song = index
                                m.playing = true
                            }
                        ) {
                            Box {
                                CoverArtwork(index, Modifier.size(144.dp))
                                if (section == 1)
                                    Text(
                                        "#${j+1}",
                                        Modifier.padding(8.dp)
                                            .background(Color.Black.copy(alpha = .5f), CircleShape)
                                            .padding(8.dp, 4.dp),
                                        color = Color.White,
                                        fontSize = 11.sp,
                                    )
                            }
                            Spacer(Modifier.height(10.dp))
                            Text(
                                song.title,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                song.artist,
                                color = Color.White.copy(alpha = .6f),
                                fontSize = 11.sp,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class DockGeometry(
    val bar: Rect,
    val mini: Rect,
    val plus: Rect,
    val lens: Rect,
    val miniRadius: Float,
    val barRadius: Float,
    val plusRadius: Float,
    val lensRadius: Float,
)

@Composable
fun NativePreview(
    m: LabMotion,
    configs: Map<Target, GlassConfig>,
    options: PreviewOptions,
    target: Target,
    modifier: Modifier = Modifier,
) {
    val source = rememberGlassBackdrop()
    val labels = rememberGlassBackdrop()
    val density = LocalDensity.current.density
    fun c(t: Target) = configs[t] ?: originalConfig(t)
    BoxWithConstraints(
        modifier.clipToBounds().pointerInput(options.touches) {
            awaitPointerEventScope {
                while (true) {
                    val e = awaitPointerEvent(PointerEventPass.Initial)
                    if (options.touches) m.touch = e.changes.firstOrNull { it.pressed }?.position
                }
            }
        }
    ) {
        val w = constraints.maxWidth.toFloat()
        val h = constraints.maxHeight.toFloat()
        val p = m.dock.value.coerceIn(0f, 1f)
        val fadeP = p.pow(c(Target.SCROLL)[Param.OPACITY_EASE])
        val mc = blendConfig(c(Target.MINI), c(Target.MINI_COMPACT), p)
        val shapeP = p.pow(c(Target.SCROLL)[Param.SHAPE_EASE])
        val posP = p.pow(c(Target.SCROLL)[Param.POSITION_EASE])
        fun dp(v: Float) = v * density
        fun adjusted(r: Rect, cfg: GlassConfig): Rect {
            val center = r.center + Offset(dp(cfg[Param.X]), dp(cfg[Param.Y]))
            val width =
                (r.width * cfg[Param.WIDTH] * (1 + cfg[Param.DEFORMATION])).coerceIn(
                    dp(38f),
                    w - dp(12f),
                )
            val height =
                (r.height * cfg[Param.HEIGHT] * (1 - cfg[Param.DEFORMATION])).coerceAtLeast(dp(38f))
            return Rect(
                Offset(
                    (center.x - width / 2).coerceIn(0f, (w - width).coerceAtLeast(0f)),
                    center.y - height / 2,
                ),
                Size(width, height),
            )
        }
        val dockWidth = min(w - dp(32f), dp(600f))
        val left = (w - dockWidth) / 2
        val normalBar = dockWidth - dp(64f)
        val y = h - dp(76f)
        val compactConfig = c(Target.COMPACT)
        val barCfg = blendConfig(c(Target.NAVBAR), c(Target.HOME), p)
        val homeGrowth =
            mix(1f, c(Target.HOME)[Param.PRESS], m.homePress.value.coerceIn(0f, 1f)) * p + (1 - p)
        val bar =
            adjusted(
                    Rect(
                        left + dp(m.homeX.value * p),
                        y + dp(m.homeY.value * p),
                        left + mix(normalBar, dp(60f), shapeP) + dp(m.homeX.value * p),
                        y + dp(60f) + dp(m.homeY.value * p),
                    ),
                    barCfg,
                )
                .let { r ->
                    val d = dp(30f) * (homeGrowth - 1)
                    r.inflate(d)
                }
        val miniBase =
            Rect(
                Offset(mix(dp(11f), left + dp(66f), posP), mix(y - dp(73f), y, posP)),
                Size(mix(w - dp(22f), dockWidth - dp(132f), shapeP), mix(dp(67f), dp(60f), shapeP)),
            )
        val mini = adjusted(miniBase, mc).translate(Offset(dp(m.miniX.value), dp(m.miniY.value)))
        val plusCfg = c(Target.PLUS)
        val plusMotion = c(Target.PLUS_MOTION)
        val press = m.plusPress.value.coerceIn(0f, 1f)
        val plusScale = mix(1f, plusMotion[Param.PRESS], press)
        val stretch = abs(m.plusX.value) / 48f * plusMotion[Param.STRETCH]
        val plusCenter =
            Offset(
                left + dockWidth - dp(30f) + dp(m.plusX.value) - dp(30f * m.contact.value),
                y + dp(30f) + dp(m.plusY.value),
            )
        val plus =
            adjusted(
                Rect(
                    plusCenter -
                        Offset(
                            dp(30f) * plusScale * (1 + stretch * .3f),
                            dp(30f) * plusScale * (1 - stretch * .12f),
                        ),
                    Size(
                        dp(60f) * plusScale * (1 + stretch * .3f),
                        dp(60f) * plusScale * (1 - stretch * .12f),
                    ),
                ),
                plusCfg,
            )
        val lensCfg = c(Target.CAPSULE)
        val normalShell = adjusted(Rect(left, y, left + normalBar, y + dp(60f)), c(Target.NAVBAR))
        val slot = (normalShell.width - dp(12f)) / 3f
        val restingLensW = min(slot - dp(8f), dp(96f)).coerceAtLeast(dp(24f))
        val lensW = mix(restingLensW, bar.width, shapeP)
        val lensH = mix(dp(50f), bar.height, shapeP)
        val held = m.capsulePress.value.coerceIn(0f, 1f)
        val growth = mix(1f, c(Target.CAPSULE_MOTION)[Param.EXPAND_SCALE], held)
        val lensStretch = abs(m.navStretch) * c(Target.CAPSULE_MOTION)[Param.STRETCH] * .35f
        val centerX =
            bar.left +
                (dp(6f) + m.capsule.value * slot + (slot - restingLensW) / 2f) * (1 - posP) +
                lensW / 2f
        val lens =
            adjusted(
                Rect(
                    Offset(
                        centerX - lensW * growth * (1 + lensStretch) / 2,
                        bar.center.y - lensH / 2f * growth,
                    ),
                    Size(lensW * growth * (1 + lensStretch), lensH * growth),
                ),
                lensCfg,
            )
        fun corner(cfg: GlassConfig, r: Rect) =
            min(dp(cfg[Param.RADIUS]) * cfg[Param.CURVATURE], min(r.width, r.height) / 2)
        fun groupRect(r: Rect): Rect {
            val cw =
                mix(1f, compactConfig[Param.WIDTH] * (1 + compactConfig[Param.DEFORMATION]), p)
                    .coerceAtMost(w / dockWidth)
            val ch =
                mix(1f, compactConfig[Param.HEIGHT] * (1 - compactConfig[Param.DEFORMATION]), p)
            val center = Offset(w / 2, y + dp(30f))
            fun point(v: Offset) =
                Offset(
                    center.x + (v.x - center.x) * cw + dp(compactConfig[Param.X]) * p,
                    center.y + (v.y - center.y) * ch + dp(compactConfig[Param.Y]) * p,
                )
            return Rect(point(r.topLeft), point(r.bottomRight))
        }
        val g =
            DockGeometry(
                groupRect(bar),
                groupRect(mini),
                groupRect(plus),
                groupRect(lens),
                corner(mc, mini),
                corner(barCfg, bar),
                corner(plusCfg, plus),
                corner(lensCfg, lens),
            )
        val miniTarget = if (p > .5f) Target.MINI_COMPACT else Target.MINI
        val isolatedTarget =
            when (target) {
                Target.PLUS_MOTION,
                Target.CONTACT -> Target.PLUS
                Target.CAPSULE_MOTION,
                Target.TABS -> Target.CAPSULE
                Target.EXPANSION,
                Target.ARTWORK,
                Target.SCROLL,
                Target.COMPACT,
                Target.RELEASE -> miniTarget
                else -> target
            }
        val showMini =
            !options.isolated ||
                isolatedTarget == Target.MINI ||
                isolatedTarget == Target.MINI_COMPACT
        val showBar =
            !options.isolated ||
                isolatedTarget == Target.NAVBAR ||
                isolatedTarget == Target.HOME ||
                isolatedTarget == Target.CAPSULE
        val showPlus = !options.isolated || isolatedTarget == Target.PLUS
        var isolatedOffset by remember(target) { mutableStateOf(Offset.Zero) }
        val isolatedRect =
            when (isolatedTarget) {
                Target.PLUS -> g.plus
                Target.CAPSULE -> g.lens
                Target.NAVBAR,
                Target.HOME -> g.bar
                else -> g.mini
            }
        val centered = Offset(w / 2, h * .52f) - isolatedRect.center
        val isolatedDelta = if (options.isolated) centered + isolatedOffset else Offset.Zero
        val shifted =
            if (options.isolated)
                g.copy(
                    bar = g.bar.translate(isolatedDelta),
                    mini = g.mini.translate(isolatedDelta),
                    plus = g.plus.translate(isolatedDelta),
                    lens = g.lens.translate(isolatedDelta),
                )
            else g
        val current = shifted
        var seek by remember { mutableFloatStateOf(.34f) }
        val full = m.player.value.coerceIn(0f, 1f)
        val physicalJoin =
            maxOf(
                if (showBar && showPlus)
                    navigationSurfaceJoin(
                        current.bar,
                        current.barRadius,
                        current.plus,
                        current.plusRadius,
                        density,
                        c(Target.CONTACT)[Param.CONTACT_DISTANCE],
                    )
                else 0f,
                if (showMini && showBar)
                    navigationSurfaceJoin(
                        current.mini,
                        current.miniRadius,
                        current.bar,
                        current.barRadius,
                        density,
                        c(Target.CONTACT)[Param.CONTACT_DISTANCE],
                    )
                else 0f,
                if (showMini && showPlus)
                    navigationSurfaceJoin(
                        current.mini,
                        current.miniRadius,
                        current.plus,
                        current.plusRadius,
                        density,
                        c(Target.CONTACT)[Param.CONTACT_DISTANCE],
                    )
                else 0f,
            )
        val opacity = (1 - stage(full, 0f, .28f)).pow(c(Target.SCROLL)[Param.OPACITY_EASE])
        Box(Modifier.fillMaxSize().recordBackdrop(source)) {
            PreviewBackground(options, m)
            if (!options.isolated && options.background != "Text") HomeContent(m, options)
            if (options.isolated)
                Text(
                    "NATIVE MATERIAL STUDY\n${target.label}",
                    Modifier.padding(20.dp),
                    color =
                        if (options.background == "Light" || options.background == "Text") Ink
                        else Color.White.copy(alpha = .55f),
                    fontSize = 11.sp,
                    lineHeight = 20.sp,
                    letterSpacing = 1.sp,
                )
        }
        Box(
            Modifier.fillMaxSize().recordBackdrop(labels, source).graphicsLayer { alpha = opacity }
        ) {
            GlassField(
                source,
                surfaces = {
                    buildList {
                        if (showBar) add(GlassSurface(current.bar, current.barRadius, barCfg))
                        if (showMini) add(GlassSurface(current.mini, current.miniRadius, mc))
                        if (showPlus) add(GlassSurface(current.plus, current.plusRadius, plusCfg))
                    }
                },
                modifier = Modifier.fillMaxSize(),
                join = {
                    c(Target.CONTACT)[Param.CONTACT_RADIUS] * m.contact.value.coerceIn(0f, 1f) +
                        c(Target.CONTACT)[Param.CONTACT_RADIUS] * physicalJoin
                },
                enabled = options.glass,
                quality = options.quality,
                bounds = options.bounds,
                onFailure = { options.fallback = it },
            )
            if (showBar) {
                val r = current.bar
                Box(
                    Modifier.fillMaxSize()
                        .then(
                            if (p < .98f) Modifier.labNavigationGesture(m, { r }, density)
                            else Modifier
                        )
                ) {
                    val tabConfig = c(Target.TABS)
                    Row(
                        Modifier.rect(r, density).padding(horizontal = (6 * (1 - p)).dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        listOf(LabIcons.Home, LabIcons.Search, LabIcons.LibraryMusic)
                            .forEachIndexed { index, icon ->
                                val weight = if (index == 0) 1f else (1 - p).coerceAtLeast(.001f)
                                Column(
                                    Modifier.weight(weight)
                                        .fillMaxHeight()
                                        .graphicsLayer {
                                            alpha =
                                                if (index == 0) 1f
                                                else (1 - fadeP * 1.5f).coerceAtLeast(0f)
                                        }
                                        .then(
                                            if (index == 0 && p > .98f)
                                                Modifier.labOrbGesture(m, true, density) { m.top() }
                                            else Modifier
                                        ),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Icon(
                                        icon,
                                        if (index == 0 && p > .98f) "Perluas dock"
                                        else listOf("Home", "Cari", "Koleksi Kamu")[index],
                                        tint =
                                            if (m.selected == index || p > .5f && index == 0)
                                                Color.White
                                            else Color.White.copy(alpha = .52f),
                                        modifier =
                                            Modifier.size(
                                                    (24f * tabConfig[Param.WIDTH]).dp,
                                                    (24f * tabConfig[Param.HEIGHT]).dp,
                                                )
                                                .offset(
                                                    tabConfig[Param.X].dp,
                                                    tabConfig[Param.Y].dp,
                                                ),
                                    )
                                    Text(
                                        listOf("Home", "Cari", "Koleksi Kamu")[index],
                                        fontSize = 9.sp,
                                        color =
                                            Color.White.copy(
                                                alpha = (1 - fadeP) * tabConfig[Param.OPACITY]
                                            ),
                                        maxLines = 1,
                                    )
                                }
                            }
                    }
                }
            }
            if (showPlus)
                Box(
                    Modifier.rect(current.plus, density).labOrbGesture(m, false, density) {
                        m.test("Plus stretch")
                    },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        LabIcons.Add,
                        "Plus orb",
                        tint = Color.White,
                        modifier = Modifier.size(27.dp),
                    )
                }
            if (showMini) {
                val r = current.mini
                Row(
                    Modifier.rect(r, density)
                        .labPlayerGesture(m, density, mini = true) { h * .62f }
                        .padding(start = (r.height / density + 1).dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            DemoSongs[m.song].title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = mix(14f, 12f, p).sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            DemoSongs[m.song].artist,
                            color = Color(0xFFBCC1C2),
                            fontSize = mix(11f, 10f, p).sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    IconButton(
                        onClick = { m.playing = !m.playing },
                        modifier = Modifier.size(38.dp),
                    ) {
                        Icon(
                            if (m.playing) LabIcons.Pause else LabIcons.PlayArrow,
                            "Play / pause",
                            tint = Color.White,
                        )
                    }
                    if (p > .01f)
                        IconButton(
                            onClick = { m.song = (m.song + 1) % DemoSongs.size },
                            modifier =
                                Modifier.width((30 * p).dp).height(38.dp).graphicsLayer {
                                    alpha = p
                                },
                        ) {
                            Icon(LabIcons.SkipNext, "Next", tint = Color.White)
                        }
                }
            }
        }
        if (showBar && p < .999f)
            GlassField(
                labels,
                { listOf(GlassSurface(current.lens, current.lensRadius, lensCfg)) },
                Modifier.fillMaxSize().graphicsLayer { alpha = opacity * (1 - fadeP) },
                enabled = options.glass,
                quality = options.quality,
                bounds = options.bounds,
                onFailure = { options.fallback = it },
            )
        val ac = c(Target.ARTWORK)
        val artSize = (current.mini.height - dp(12f)) * ac[Param.WIDTH]
        val start =
            Rect(
                current.mini.topLeft + Offset(dp(6f + ac[Param.X]), dp(6f + ac[Param.Y])),
                Size(artSize, artSize * ac[Param.HEIGHT]),
            )
        val smallPreview = h < dp(430f)
        val endSize =
            min(w - dp(58f), h * (if (smallPreview) .30f else .48f)) *
                c(Target.EXPANSION)[Param.WIDTH]
        val end =
            Rect(
                Offset(
                    (w - endSize) / 2 + dp(c(Target.EXPANSION)[Param.X]),
                    max(dp(58f), h * .13f) + dp(c(Target.EXPANSION)[Param.Y]),
                ),
                Size(endSize, endSize),
            )
        if (full > .001f) {
            val song = DemoSongs[m.song]
            Box(
                Modifier.fillMaxSize()
                    .graphicsLayer { alpha = stage(full, .02f, .76f) }
                    .background(Brush.verticalGradient(listOf(song.color, song.second, Ink)))
                    .labPlayerGesture(m, density) { (start.top - end.top).coerceAtLeast(h * .45f) }
            )
            Box(
                Modifier.fillMaxSize()
                    .graphicsLayer { alpha = stage(full, .02f, .94f) }
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = .55f))
                        )
                    )
            )
            Column(
                Modifier.fillMaxSize().padding(24.dp).graphicsLayer {
                    alpha = stage(full, .42f, .95f)
                }
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { m.openPlayer(false) }) {
                        Icon(LabIcons.KeyboardArrowDown, "Close full player", tint = Color.White)
                    }
                    Text(
                        "NOW PLAYING / DATZON LAB",
                        color = Color.White,
                        fontSize = 10.sp,
                        letterSpacing = 1.5.sp,
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    song.title,
                    fontSize = if (smallPreview) 20.sp else 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Text(song.artist, color = Color.White.copy(alpha = .6f), fontSize = 14.sp)
                if (!smallPreview)
                    Slider(
                        value = seek,
                        onValueChange = { seek = it },
                        colors =
                            SliderDefaults.colors(
                                disabledThumbColor = Accent,
                                disabledActiveTrackColor = Accent,
                            ),
                    )
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { m.song = (m.song + 7) % 8 }) {
                        Icon(LabIcons.SkipPrevious, "Previous", tint = Color.White)
                    }
                    FilledIconButton(
                        onClick = { m.playing = !m.playing },
                        modifier = Modifier.size(if (smallPreview) 44.dp else 56.dp),
                    ) {
                        Icon(if (m.playing) LabIcons.Pause else LabIcons.PlayArrow, "Play / pause")
                    }
                    IconButton(onClick = { m.song = (m.song + 1) % 8 }) {
                        Icon(LabIcons.SkipNext, "Next", tint = Color.White)
                    }
                }
            }
        }
        if (showMini || full > 0f) {
            val rect =
                playerArtworkRect(start, end, full.pow(c(Target.EXPANSION)[Param.POSITION_EASE]))
            CoverArtwork(
                m.song,
                Modifier.rect(rect, density).labPlayerGesture(m, density, mini = true) {
                    (start.top - end.top).coerceAtLeast(h * .45f)
                },
                corner = mix(mix(ac[Param.RADIUS], 24f, p), 16f, full),
            )
        }
        if (options.isolated)
            Box(
                Modifier.fillMaxSize().pointerInput(target, w, h) {
                    detectDragGestures { change, drag ->
                        change.consume()
                        val next = isolatedOffset + drag
                        val limitX = ((w - isolatedRect.width) / 2 - 8 * density).coerceAtLeast(0f)
                        val limitY = ((h - isolatedRect.height) / 2 - 8 * density).coerceAtLeast(0f)
                        isolatedOffset =
                            Offset(
                                next.x.coerceIn(-limitX, limitX),
                                next.y.coerceIn(-limitY, limitY),
                            )
                    }
                }
            )
        if (options.touches && m.touch != null)
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(Accent.copy(alpha = .25f), 22.dp.toPx(), m.touch!!)
                drawCircle(Accent, 4.dp.toPx(), m.touch!!)
            }
        if (options.progress)
            Text(
                "DOCK ${(p*100).roundToInt()}%   PLAYER ${(full*100).roundToInt()}%",
                Modifier.align(Alignment.TopEnd)
                    .padding(12.dp)
                    .background(Ink.copy(alpha = .8f), CircleShape)
                    .padding(10.dp, 6.dp),
                color = Accent,
                fontSize = 9.sp,
            )
        BackHandler(enabled = full > .01f) { m.openPlayer(false) }
    }
}

fun blendConfig(a: GlassConfig, b: GlassConfig, p: Float): GlassConfig =
    GlassConfig(Param.entries.associateWith { mix(a[it], b[it], p) })

private fun Modifier.rect(rect: Rect, density: Float): Modifier =
    offset { IntOffset(rect.left.roundToInt(), rect.top.roundToInt()) }
        .size((rect.width / density).dp, (rect.height / density).dp)
