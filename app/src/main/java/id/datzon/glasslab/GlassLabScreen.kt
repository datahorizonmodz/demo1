package id.datzon.glasslab

import android.view.Choreographer
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Cream = Color(0xFFF2EEE5)
private val Secondary = Color(0xFFDAD2C4)
private val Sepia = Color(0xFF7D7053)

@Composable
fun GlassLabScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val store = remember { GlassStore(context) }
    var mini by remember { mutableStateOf(store.load("mini")) }
    var nav by remember { mutableStateOf(store.load("navbar")) }
    var target by remember { mutableStateOf("mini") }
    var editorVisible by remember { mutableStateOf(false) }
    var enabled by remember { mutableStateOf(true) }
    var playing by remember { mutableStateOf(false) }
    var selectedNav by remember { mutableIntStateOf(0) }
    val backdrop = rememberGraphicsLayer()

    LaunchedEffect(mini) { store.save("mini", mini) }
    LaunchedEffect(nav) { store.save("navbar", nav) }

    Box(Modifier.fillMaxSize().background(Sepia).safeDrawingPadding()) {
        Box(
            Modifier.fillMaxSize().drawWithContent {
                backdrop.record { this@drawWithContent.drawContent() }
                drawLayer(backdrop)
            }
        ) { HomeMock() }

        Row(
            Modifier
                .align(Alignment.TopEnd)
                .padding(top = 12.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(30.dp),
                color = Color.Black.copy(alpha = 0.45f),
                tonalElevation = 0.dp
            ) {
                Text(
                    "DATZON GLASS LAB",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    color = Cream, fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
            }
            FilledTonalIconButton(
                onClick = { editorVisible = true },
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = Color(0xFF21201D), contentColor = Cream
                )
            ) { Icon(Icons.Default.Tune, contentDescription = "Buka editor") }
        }

        Column(
            Modifier.align(Alignment.BottomCenter).padding(bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GlassPane(
                backdrop = backdrop, config = mini, enabled = enabled,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(76.dp)
            ) {
                Row(
                    Modifier.fillMaxSize().padding(horizontal = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CoverArtwork(index = 3, modifier = Modifier.size(58.dp), corner = 11)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("Love Story", color = Color.White, fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp, maxLines = 1)
                        Text("Taylor Swift", color = Secondary, fontSize = 12.sp)
                    }
                    IconButton(onClick = { target = "mini"; editorVisible = true }) {
                        Icon(Icons.Default.Tune, null, tint = Cream)
                    }
                    IconButton(onClick = { playing = !playing }) {
                        Icon(if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playing) "Jeda" else "Putar",
                            tint = Cream, modifier = Modifier.size(32.dp))
                    }
                }
            }

            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GlassPane(
                    backdrop = backdrop, config = nav, enabled = enabled,
                    modifier = Modifier.weight(1f).height(66.dp)
                ) {
                    Row(
                        Modifier.fillMaxSize().padding(5.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NavButton(Icons.Default.Home, 0, selectedNav) { selectedNav = 0 }
                        NavButton(Icons.Default.Search, 1, selectedNav) { selectedNav = 1 }
                        NavButton(Icons.Default.Equalizer, 2, selectedNav) { selectedNav = 2 }
                    }
                }
                GlassPane(
                    backdrop = backdrop, config = nav.copy(radius = 40f), enabled = enabled,
                    modifier = Modifier.size(66.dp)
                ) {
                    Box(Modifier.fillMaxSize().clickable {
                        target = "navbar"; editorVisible = true
                    }, contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Add, "Edit tombol plus", tint = Cream,
                            modifier = Modifier.size(30.dp))
                    }
                }
            }
        }

        Column(
            Modifier.align(Alignment.CenterEnd).padding(end = 10.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Surface(
                modifier = Modifier.clickable { enabled = !enabled },
                color = Color.Black.copy(alpha = 0.50f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    if (enabled) "GLASS ON" else "GLASS OFF",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
                    color = Cream, fontSize = 9.sp
                )
            }
            FpsIndicator()
        }
    }

    if (editorVisible) {
        GlassEditor(
            target = target,
            config = if (target == "mini") mini else nav,
            onConfig = { if (target == "mini") mini = it else nav = it },
            store = store,
            enabled = enabled,
            onToggle = { enabled = !enabled },
            onTarget = { target = it },
            onDismiss = { editorVisible = false }
        )
    }
}

@Composable
private fun NavButton(icon: ImageVector, index: Int, selected: Int, onClick: () -> Unit) {
    val bg by animateColorAsState(
        if (index == selected) Color.White.copy(alpha = 0.14f) else Color.Transparent,
        label = "navbar selection"
    )
    Box(
        Modifier
            .size(width = 72.dp, height = 53.dp)
            .clip(RoundedCornerShape(27.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .animateContentSize(),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = "Pindah tab", tint = Cream, modifier = Modifier.size(28.dp))
    }
}

@Composable
private fun HomeMock() {
    Column(
        Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(
                Color(0xFF827455), Color(0xFF7C6E52),
                Color(0xFF514735), Color(0xFF1D1A15)
            )))
            .verticalScroll(rememberScrollState())
            .padding(top = 76.dp, bottom = 180.dp)
    ) {
        Text(
            "Temukan Musikmu", modifier = Modifier.padding(start = 17.dp),
            fontSize = 25.sp, color = Cream, fontWeight = FontWeight.Bold
        )
        Text(
            "PILIHAN YANG COCOK UNTUKMU",
            modifier = Modifier.padding(start = 17.dp, top = 5.dp),
            color = Secondary.copy(alpha = 0.8f), letterSpacing = 1.5.sp, fontSize = 10.sp
        )
        Spacer(Modifier.height(28.dp))
        SectionLabel(title = "Serupa dengan", subtitle = "Dire Straits", showArrow = true)
        Spacer(Modifier.height(16.dp))
        Row(
            Modifier.horizontalScroll(rememberScrollState()).padding(start = 17.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            val tracks = listOf(
                "Sultans Of Swing" to "Dire Straits",
                "Money For Nothing" to "Dire Straits",
                "Walk Of Life" to "Dire Straits"
            )
            tracks.forEachIndexed { i, item ->
                TrackTile(title = item.first, artist = item.second, index = i)
            }
        }
        Spacer(Modifier.height(51.dp))
        Text(
            "Top 50 Indonesia",
            modifier = Modifier.padding(start = 17.dp),
            fontSize = 25.sp, fontWeight = FontWeight.SemiBold, color = Cream
        )
        Spacer(Modifier.height(19.dp))
        Row(
            Modifier.horizontalScroll(rememberScrollState()).padding(start = 17.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            val tracks = listOf(
                "iqro'" to "Raim Laode",
                "Dunia Yang Nanti" to "Raim Laode",
                "Sesi Potret" to "eñau, Ari Lesmana",
                "Langit Abu" to "Raim Laode"
            )
            tracks.forEachIndexed { i, item ->
                TrackTile(title = item.first, artist = item.second, index = i + 4, rank = i + 1)
            }
        }
        Spacer(Modifier.height(41.dp))
        Text(
            "Geser halaman untuk melihat refraction di atas konten yang bergerak.",
            color = Secondary, fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 23.dp),
            lineHeight = 18.sp
        )
        Spacer(Modifier.height(22.dp))
        repeat(5) { i ->
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 17.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CoverArtwork(index = i + 6, modifier = Modifier.size(51.dp), corner = 9)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(listOf("Musik buat santai", "Favorit hari ini", "Ruang nada", "Discover", "Pilihan Editor")[i],
                        color = Cream, fontSize = 14.sp)
                    Text("DATZON Music", color = Secondary, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(title: String, subtitle: String, showArrow: Boolean) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 17.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CoverArtwork(index = 2, modifier = Modifier.size(48.dp), corner = 50)
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Secondary, fontSize = 12.sp)
            Text(subtitle, color = Cream, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        }
        if (showArrow) Text("→", color = Cream, fontSize = 30.sp)
    }
}

@Composable
private fun TrackTile(title: String, artist: String, index: Int, rank: Int? = null) {
    Column(Modifier.width(150.dp)) {
        Box {
            CoverArtwork(index = index, modifier = Modifier.size(150.dp), corner = 14)
            if (rank != null) {
                Box(
                    Modifier.align(Alignment.TopStart).padding(8.dp)
                        .size(30.dp).background(Color(0xFF272623).copy(alpha = 0.9f), CircleShape),
                    contentAlignment = Alignment.Center
                ) { Text(rank.toString(), color = Cream, fontSize = 13.sp) }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis,
            color = Cream, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Text(artist, maxLines = 1, overflow = TextOverflow.Ellipsis,
            color = Secondary, fontSize = 12.sp)
    }
}

@Composable
fun CoverArtwork(index: Int, modifier: Modifier = Modifier, corner: Int = 14) {
    val palettes = listOf(
        listOf(Color(0xFFFFEFBB), Color(0xFF8C7651)),
        listOf(Color(0xFF4DC9E5), Color(0xFF187AAB)),
        listOf(Color(0xFF62B6CF), Color(0xFF224A72)),
        listOf(Color(0xFFE3C1A2), Color(0xFF876047)),
        listOf(Color(0xFFFF9B26), Color(0xFF451C25)),
        listOf(Color(0xFFFF9B26), Color(0xFF451C25)),
        listOf(Color(0xFFB2D9D2), Color(0xFF49355E)),
        listOf(Color(0xFF7897C4), Color(0xFF243656))
    )
    val colors = palettes[index % palettes.size]
    Box(
        modifier.clip(RoundedCornerShape(corner.dp))
            .background(Brush.linearGradient(colors))
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val c = center
            drawCircle(Color.White.copy(alpha = 0.13f), radius = size.minDimension * 0.38f,
                center = Offset(size.width * 0.68f, size.height * 0.38f))
            val path = Path().apply {
                moveTo(size.width * 0.1f, size.height)
                cubicTo(size.width * 0.12f, size.height * 0.3f,
                    size.width * 0.75f, size.height * 0.22f,
                    size.width * 0.81f, size.height)
                close()
            }
            drawPath(path, Color.Black.copy(alpha = 0.31f))
            drawLine(Color.White.copy(alpha = 0.24f),
                start = Offset(c.x * 0.4f, size.height * 0.13f),
                end = Offset(size.width * 0.87f, size.height * 0.73f),
                strokeWidth = 1.5.dp.toPx())
        }
        if (index < 3) {
            Text(
                "DIRE STRAITS",
                modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
                color = if (index == 0) Color.Black else Color.White,
                fontSize = 8.sp, fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun FpsIndicator() {
    var fps by remember { mutableIntStateOf(0) }
    DisposableEffect(Unit) {
        val ch = Choreographer.getInstance()
        val cb = object : Choreographer.FrameCallback {
            var frames = 0
            var last = 0L
            override fun doFrame(frameTimeNanos: Long) {
                if (last == 0L) last = frameTimeNanos
                frames++
                if (frameTimeNanos - last >= 1_000_000_000L) {
                    fps = (frames * 1_000_000_000.0 / (frameTimeNanos - last)).toInt()
                    frames = 0
                    last = frameTimeNanos
                }
                ch.postFrameCallback(this)
            }
        }
        ch.postFrameCallback(cb)
        onDispose { ch.removeFrameCallback(cb) }
    }
    Surface(color = Color.Black.copy(alpha = 0.49f), shape = RoundedCornerShape(12.dp)) {
        Text("$fps FPS", modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            color = Cream, fontSize = 10.sp)
    }
}
