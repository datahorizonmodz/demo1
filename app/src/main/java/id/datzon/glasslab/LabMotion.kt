package id.datzon.glasslab

import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.input.pointer.util.VelocityTracker
import kotlin.math.*
import kotlinx.coroutines.*

/**
 * Interrupted animations retain the last rendered value, matching DATZON's finger/resting
 * ownership. Cancelling an old job cannot overwrite a new gesture.
 */
@Stable
class MotionChannel(private val scope: CoroutineScope, initial: Float = 0f) {
    var value by mutableFloatStateOf(initial)
        private set

    var running by mutableStateOf(false)
        private set

    private var job: Job? = null
    private var generation = 0

    fun scrub(v: Float) {
        generation++
        job?.cancel()
        running = false
        value = v
    }

    fun animate(to: Float, c: GlassConfig, useSpring: Boolean = false, velocity: Float = 0f) {
        val from = value
        val ticket = ++generation
        job?.cancel()
        running = true
        job =
            scope.launch {
                try {
                    val spec: AnimationSpec<Float> =
                        if (useSpring)
                            spring(
                                dampingRatio =
                                    (c[Param.DAMPING] /
                                            (1f + c[Param.BOUNCE] * .3f + c[Param.OVERSHOOT] * .3f))
                                        .coerceIn(.25f, 1.4f),
                                stiffness = c[Param.STIFFNESS],
                            )
                        else
                            tween(
                                (c[Param.DURATION] / c[Param.SPEED]).toInt(),
                                easing = CubicBezierEasing(.30f, 0f, .22f, 1f),
                            )
                    animate(from, to, initialVelocity = velocity, animationSpec = spec) { v, _ ->
                        value = v
                    }
                    value = to
                } finally {
                    if (ticket == generation) running = false
                }
            }
    }
}

/**
 * The artwork path is the DATZON Music PlayerExpansion path: the horizontal tangent reaches zero
 * halfway; reversing progress uses exactly the same path.
 */
fun playerArtworkRect(start: Rect, end: Rect, progress: Float): Rect {
    val p = progress.coerceIn(0f, 1f)
    val turn = (p / .5f).coerceIn(0f, 1f)
    val horizontal = 1f - (1f - turn).pow(3)
    val center =
        Offset(
            start.center.x + (end.center.x - start.center.x) * horizontal,
            start.center.y + (end.center.y - start.center.y) * p,
        )
    val w = start.width + (end.width - start.width) * p
    val h = start.height + (end.height - start.height) * p
    return Rect(center - Offset(w / 2, h / 2), Size(w, h))
}

fun stage(p: Float, start: Float, end: Float): Float =
    ((p - start) / (end - start)).coerceIn(0f, 1f).let { it * it * (3 - 2 * it) }

fun mix(a: Float, b: Float, p: Float): Float = a + (b - a) * p

fun navigationDestination(position: Float): Int = position.roundToInt().coerceIn(0, 2)

fun navigationDragPosition(pointer: Float, inset: Float, slot: Float, grab: Float): Float =
    if (slot <= 0f) 0f else ((pointer - inset - grab) / slot - .5f).coerceIn(0f, 2f)

@Stable
class LabMotion(val scope: CoroutineScope) {
    var configs by mutableStateOf<Map<Target, GlassConfig>>(emptyMap())

    fun c(t: Target) = configs[t] ?: originalConfig(t)

    val dock = MotionChannel(scope)
    val player = MotionChannel(scope)
    val capsule = MotionChannel(scope)
    val capsulePress = MotionChannel(scope)
    val plusX = MotionChannel(scope)
    val plusY = MotionChannel(scope)
    val plusPress = MotionChannel(scope)
    val miniX = MotionChannel(scope)
    val miniY = MotionChannel(scope)
    val homePress = MotionChannel(scope)
    var navStretch by mutableFloatStateOf(0f)
    val homeX = MotionChannel(scope)
    val homeY = MotionChannel(scope)
    val contact = MotionChannel(scope)
    var selected by mutableIntStateOf(0)
    var navDragging by mutableStateOf(false)
    var playerDragging by mutableStateOf(false)
    var playing by mutableStateOf(false)
    var song by mutableIntStateOf(0)
    var springMode by mutableStateOf(false)
    var loop by mutableStateOf(false)
    var timeline by mutableStateOf("Scroll morph")
    var lastTest by mutableStateOf("Compact")
    var touch by mutableStateOf<Offset?>(null)
    private var scrollDown = 0f
    private var scrollUp = 0f
    private var directScroll = false
    private var scrollCompact = false
    private var demoJob: Job? = null

    fun scroll(deltaDp: Float, direct: Boolean) {
        if (player.value > .01f || playerDragging) return
        if (direct) directScroll = true else if (!directScroll) return
        val threshold = c(Target.SCROLL)[Param.SCROLL_THRESHOLD]
        if (deltaDp < 0) {
            scrollUp = 0f
            scrollDown -= deltaDp
            if (scrollDown >= threshold) {
                setCompact(true)
                scrollDown = 0f
            }
        }
        if (deltaDp > 0) {
            scrollDown = 0f
            scrollUp += deltaDp
            if (scrollUp >= threshold) {
                setCompact(false)
                scrollUp = 0f
            }
        }
    }

    fun setCompact(compact: Boolean) {
        scrollCompact = compact
        dock.animate(if (compact) 1f else 0f, c(Target.SCROLL), springMode)
    }

    fun top() {
        scrollDown = 0f
        scrollUp = 0f
        directScroll = false
        setCompact(false)
    }

    fun select(index: Int) {
        selected = index.coerceIn(0, 2)
        capsule.animate(selected.toFloat(), c(Target.CAPSULE_MOTION), springMode)
        scope.launch {
            capsulePress.animate(1f, c(Target.CAPSULE_MOTION))
            delay((c(Target.CAPSULE_MOTION)[Param.DURATION] * .45f).toLong())
            if (!navDragging) capsulePress.animate(0f, c(Target.RELEASE), true)
        }
    }

    fun openPlayer(open: Boolean, release: Boolean = false, velocity: Float = 0f) {
        playerDragging = false
        player.animate(
            if (open) 1f else 0f,
            if (release) c(Target.RELEASE) else c(Target.EXPANSION),
            release || springMode,
            velocity,
        )
    }

    fun cancelAll() {
        demoJob?.cancel()
        demoJob = null
        listOf(
                dock,
                player,
                capsule,
                capsulePress,
                plusX,
                plusY,
                plusPress,
                miniX,
                miniY,
                homePress,
                homeX,
                homeY,
                contact,
            )
            .forEach { it.scrub(0f) }
        selected = 0
        navDragging = false
        playerDragging = false
        loop = false
        scrollCompact = false
    }

    fun channel(): MotionChannel =
        when (timeline) {
            "Full player" -> player
            "Capsule press" -> capsulePress
            "Capsule move" -> capsule
            "Plus stretch" -> plusX
            "Contact" -> contact
            else -> dock
        }

    fun timelineProgress(): Float =
        when (timeline) {
            "Capsule move" -> capsule.value / 2f
            "Plus stretch" -> -plusX.value / 44f
            else -> channel().value
        }.coerceIn(0f, 1f)

    fun scrubTimeline(p: Float) {
        demoJob?.cancel()
        loop = false
        channel()
            .scrub(
                when (timeline) {
                    "Capsule move" -> p * 2f
                    "Plus stretch" -> -p * 44f
                    else -> p
                }
            )
    }

    fun test(name: String) {
        demoJob?.cancel()
        lastTest = name
        demoJob =
            scope.launch {
                do {
                    when (name) {
                        "Compact" -> {
                            timeline = "Scroll morph"
                            setCompact(true)
                        }
                        "Expanded" -> {
                            timeline = "Scroll morph"
                            setCompact(false)
                        }
                        "Open full player" -> {
                            timeline = "Full player"
                            openPlayer(true)
                        }
                        "Close full player" -> {
                            timeline = "Full player"
                            openPlayer(false)
                        }
                        "Capsule press" -> {
                            timeline = "Capsule press"
                            capsulePress.animate(1f, c(Target.CAPSULE_MOTION))
                            delay(400)
                            capsulePress.animate(0f, c(Target.RELEASE), true)
                        }
                        "Capsule move" -> {
                            timeline = "Capsule move"
                            select((selected + 1) % 3)
                        }
                        "Plus stretch" -> {
                            timeline = "Plus stretch"
                            plusX.animate(-44f, c(Target.PLUS_MOTION))
                            plusPress.animate(1f, c(Target.PLUS_MOTION))
                            delay(600)
                            plusX.animate(0f, c(Target.RELEASE), true)
                            plusPress.animate(0f, c(Target.RELEASE), true)
                        }
                        "Contact" -> {
                            timeline = "Contact"
                            contact.animate(1f, c(Target.CONTACT))
                            delay(700)
                            contact.animate(0f, c(Target.CONTACT))
                        }
                    }
                    delay(1800)
                    if (loop) {
                        when (name) {
                            "Compact",
                            "Expanded" ->
                                dock.animate(
                                    1f - dock.value.coerceIn(0f, 1f),
                                    c(Target.SCROLL),
                                    springMode,
                                )
                            "Open full player",
                            "Close full player" -> openPlayer(player.value < .5f)
                        }
                        delay(1800)
                    }
                } while (loop)
            }
    }
}

fun Modifier.labNavigationGesture(m: LabMotion, bar: () -> Rect, density: Float): Modifier =
    composed {
        val liveBar by rememberUpdatedState(bar)
        Modifier.pointerInput(m, density) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                val r = liveBar()
                if (!r.contains(down.position)) return@awaitEachGesture
                val slot = (r.width - 12 * density) / 3f
                val inset = r.left + 6 * density
                val start = m.capsule.value
                val committed = m.selected
                m.capsule.scrub(start)
                var latest = down.position
                var distance = Offset.Zero
                var finished = false
                val grab =
                    (down.position.x - (inset + (start + .5f) * slot)).takeIf {
                        abs(it) < slot * .6f
                    } ?: 0f
                fun begin() {
                    m.navDragging = true
                    m.capsule.scrub(navigationDragPosition(latest.x, inset, slot, grab))
                    m.capsulePress.animate(1f, m.c(Target.CAPSULE_MOTION), true)
                }
                val hold =
                    m.scope.launch {
                        delay(m.c(Target.CAPSULE_MOTION)[Param.LONG_PRESS].toLong())
                        begin()
                    }
                try {
                    while (true) {
                        val change =
                            awaitPointerEvent(PointerEventPass.Initial).changes.firstOrNull {
                                it.id == down.id
                            } ?: break
                        latest = change.position
                        if (!change.pressed) {
                            hold.cancel()
                            if (!change.isConsumed) {
                                val idx =
                                    if (m.navDragging)
                                        floor(
                                                m.capsule.value +
                                                    m.c(Target.CAPSULE_MOTION)[Param.SNAP]
                                            )
                                            .toInt()
                                            .coerceIn(0, 2)
                                    else ((latest.x - r.left) / r.width * 3).toInt().coerceIn(0, 2)
                                change.consume()
                                m.select(idx)
                                finished = true
                            }
                            break
                        }
                        val delta = change.position - change.previousPosition
                        distance += delta
                        val threshold = m.c(Target.CAPSULE_MOTION)[Param.DRAG_THRESHOLD] * density
                        if (
                            !m.navDragging &&
                                abs(distance.y) > threshold &&
                                abs(distance.y) > abs(distance.x)
                        )
                            break
                        if (
                            !m.navDragging &&
                                abs(distance.x) > threshold &&
                                abs(distance.x) > abs(distance.y)
                        ) {
                            hold.cancel()
                            begin()
                        }
                        if (m.navDragging) {
                            change.consume()
                            m.navStretch = (delta.x / slot).coerceIn(-1f, 1f)
                            val sensitivity = m.c(Target.CAPSULE_MOTION)[Param.SENSITIVITY]
                            m.capsule.scrub(
                                (start +
                                        (navigationDragPosition(latest.x, inset, slot, grab) -
                                            start) * sensitivity)
                                    .coerceIn(0f, 2f)
                            )
                        }
                    }
                } finally {
                    hold.cancel()
                    m.navDragging = false
                    m.navStretch = 0f
                    if (!finished) {
                        m.selected = committed
                        m.capsule.animate(committed.toFloat(), m.c(Target.RELEASE), true)
                    }
                    m.capsulePress.animate(0f, m.c(Target.RELEASE), true)
                }
            }
        }
    }

fun Modifier.labOrbGesture(
    m: LabMotion,
    home: Boolean,
    density: Float,
    onClick: () -> Unit,
): Modifier =
    pointerInput(m, home, density) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            val x = if (home) m.homeX else m.plusX
            val y = if (home) m.homeY else m.plusY
            val config = m.c(if (home) Target.HOME else Target.PLUS_MOTION)
            var distance = Offset.Zero
            var dragged = false
            val startOffset = Offset(x.value, y.value)
            x.scrub(x.value)
            y.scrub(y.value)
            val press = if (home) m.homePress else m.plusPress
            press.animate(1f, config, true)
            try {
                while (true) {
                    val change =
                        awaitPointerEvent(PointerEventPass.Initial).changes.firstOrNull {
                            it.id == down.id
                        } ?: break
                    if (!change.pressed) {
                        if (!change.isConsumed && !dragged) onClick()
                        change.consume()
                        break
                    }
                    distance += change.position - change.previousPosition
                    if (distance.getDistance() > config[Param.DRAG_THRESHOLD] * density)
                        dragged = true
                    if (dragged) {
                        change.consume()
                        fun rubber(v: Float): Float {
                            val q = v / density * config[Param.SENSITIVITY]
                            return 48f *
                                (1f - exp(-abs(q) / 48f)) *
                                sign(q) *
                                config[Param.RESISTANCE]
                        }
                        x.scrub(startOffset.x + rubber(distance.x))
                        y.scrub(startOffset.y + rubber(distance.y))
                    }
                }
            } finally {
                x.animate(0f, m.c(Target.RELEASE), true)
                y.animate(0f, m.c(Target.RELEASE), true)
                press.animate(0f, config, true)
            }
        }
    }

fun Modifier.labPlayerGesture(
    m: LabMotion,
    density: Float,
    mini: Boolean = false,
    travel: () -> Float,
): Modifier = composed {
    val liveTravel by rememberUpdatedState(travel)
    Modifier.pointerInput(m, density, mini) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            val start = m.player.value
            val wasOpen = start > .5f
            val interrupted = m.player.running
            if (interrupted) m.player.scrub(start)
            val dockStart = m.dock.value
            var mode = 0 // 1 full-player, 2 dock morph, 3 compact elastic
            var distance = Offset.Zero
            var finished = false
            val tracker = VelocityTracker().apply { addPosition(down.uptimeMillis, down.position) }
            try {
                while (true) {
                    val change =
                        awaitPointerEvent(PointerEventPass.Initial).changes.firstOrNull {
                            it.id == down.id
                        } ?: break
                    tracker.addPosition(change.uptimeMillis, change.position)
                    if (!change.pressed) {
                        if (change.isConsumed) break
                        when (mode) {
                            1 -> {
                                val config = m.c(Target.EXPANSION)
                                val speed =
                                    -tracker.calculateVelocity().y / density * config[Param.FLING]
                                val open =
                                    when {
                                        speed > config[Param.VELOCITY] -> true
                                        speed < -config[Param.VELOCITY] -> false
                                        else -> m.player.value >= config[Param.SNAP]
                                    }
                                m.openPlayer(
                                    open,
                                    true,
                                    speed * density / liveTravel().coerceAtLeast(1f),
                                )
                                change.consume()
                            }
                            2 -> {
                                m.dock.animate(
                                    if (distance.y / density >= 32f) 1f else 0f,
                                    m.c(Target.RELEASE),
                                    true,
                                )
                                change.consume()
                            }
                            3 -> {
                                m.miniX.animate(0f, m.c(Target.RELEASE), true)
                                m.miniY.animate(0f, m.c(Target.RELEASE), true)
                                change.consume()
                            }
                            else ->
                                if (!wasOpen || interrupted) {
                                    m.openPlayer(true)
                                    change.consume()
                                }
                        }
                        finished = true
                        break
                    }
                    distance += change.position - change.previousPosition
                    val threshold = m.c(Target.EXPANSION)[Param.DRAG_THRESHOLD] * density
                    if (mode == 0 && distance.getDistance() > threshold) {
                        mode =
                            when {
                                mini &&
                                    start < .01f &&
                                    dockStart > .98f &&
                                    abs(distance.x) > abs(distance.y) -> 3
                                mini &&
                                    start < .01f &&
                                    distance.y > 0 &&
                                    abs(distance.y) > abs(distance.x) -> 2
                                abs(distance.y) > abs(distance.x) -> 1
                                else -> 0
                            }
                        if (mode == 1) {
                            m.playerDragging = true
                            m.player.scrub(start)
                        }
                        if (mode == 2) m.dock.scrub(dockStart)
                    }
                    when (mode) {
                        1 -> {
                            change.consume()
                            m.player.scrub(
                                (start -
                                        distance.y / liveTravel().coerceAtLeast(1f) *
                                            m.c(Target.EXPANSION)[Param.SENSITIVITY])
                                    .coerceIn(0f, 1f)
                            )
                        }
                        2 -> {
                            change.consume()
                            m.dock.scrub((dockStart + distance.y / density / 96f).coerceIn(0f, 1f))
                        }
                        3 -> {
                            change.consume()
                            fun rubber(v: Float) =
                                sign(v) * 12f * (1 - exp(-abs(v / density) / 12f))
                            m.miniX.scrub(rubber(distance.x))
                            m.miniY.scrub(rubber(distance.y))
                        }
                    }
                }
            } finally {
                if (!finished)
                    when (mode) {
                        1 -> m.openPlayer(wasOpen, true)
                        2 -> m.dock.animate(dockStart, m.c(Target.RELEASE), true)
                        3 -> {
                            m.miniX.animate(0f, m.c(Target.RELEASE), true)
                            m.miniY.animate(0f, m.c(Target.RELEASE), true)
                        }
                        else -> if (interrupted) m.openPlayer(wasOpen, true)
                    }
                m.playerDragging = false
            }
        }
    }
}

/** Rounded-core separation from DATZON NavigationOrbState. */
fun navigationSurfaceJoin(
    a: Rect,
    ar: Float,
    b: Rect,
    br: Float,
    density: Float,
    reach: Float = 9f,
): Float {
    val ac = a.deflate(ar.coerceAtMost(min(a.width, a.height) / 2))
    val bc = b.deflate(br.coerceAtMost(min(b.width, b.height) / 2))
    val dx = max(0f, max(ac.left - bc.right, bc.left - ac.right))
    val dy = max(0f, max(ac.top - bc.bottom, bc.top - ac.bottom))
    val gap = hypot(dx, dy) - ar - br
    val amount = ((reach / 3f * density - gap) / (reach * density)).coerceIn(0f, 1f)
    return amount * amount * (3f - 2f * amount)
}
