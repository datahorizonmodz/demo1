package id.datzon.glasslab

import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.ui.geometry.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class GlassLabTest {
    private class Clock : MonotonicFrameClock {
        var time = 0L

        override suspend fun <R> withFrameNanos(onFrame: (Long) -> R): R {
            delay(16)
            time += 16_000_000
            return onFrame(time)
        }
    }

    @Test
    fun presetRoundTripRetainsEveryTargetAndRejectsInvalidInput() {
        val all =
            Target.entries.associateWith { t ->
                originalConfig(t).with(Param.RED, .71f).with(Param.X, -14f)
            }
        val p = LabPreset("Round trip", all)
        val restored = LabPreset.parse(p.json())
        assertEquals(p.name, restored.name)
        Target.entries.forEach { t ->
            Param.entries.forEach { parameter ->
                assertEquals(
                    p.configs.getValue(t)[parameter],
                    restored.configs.getValue(t)[parameter],
                    0f,
                )
            }
        }
        assertThrows(Exception::class.java) { LabPreset.parse("{\"schema\":1}") }
        assertThrows(Exception::class.java) { GlassConfig().with(Param.IOR, Float.NaN) }
        assertEquals(3f, GlassConfig().with(Param.IOR, 999f)[Param.IOR], 0f)
    }

    @Test
    fun changingOneTargetDoesNotChangeTheOtherGlass() {
        val initial = Target.entries.associateWith(::originalConfig)
        val changed = initial + (Target.MINI to initial.getValue(Target.MINI).with(Param.IOR, 2.4f))
        assertEquals(initial.getValue(Target.NAVBAR), changed.getValue(Target.NAVBAR))
        assertNotEquals(initial.getValue(Target.MINI), changed.getValue(Target.MINI))
    }

    @Test
    fun curvedArtworkIsContinuousAndReversibleAtEveryProgress() {
        val start = Rect(12f, 650f, 67f, 705f)
        val end = Rect(32f, 80f, 328f, 376f)
        assertEquals(start, playerArtworkRect(start, end, 0f))
        assertEquals(end, playerArtworkRect(start, end, 1f))
        var previous = start
        (1..1000).forEach { i ->
            val r = playerArtworkRect(start, end, i / 1000f)
            assertTrue(r.width >= previous.width)
            assertTrue(r.center.y <= previous.center.y)
            assertTrue((r.center - previous.center).getDistance() < 2f)
            previous = r
        }
        val a = playerArtworkRect(start, end, .4999f)
        val b = playerArtworkRect(start, end, .5001f)
        assertEquals(a.center.x, b.center.x, .01f)
    }

    @Test
    fun hysteresisIgnoresTinyScrollAndRequiresDirectionalAccumulation() = runTest {
        val m = LabMotion(CoroutineScope(coroutineContext + Clock()))
        repeat(7) { m.scroll(-8f, true) }
        assertFalse(m.dock.running)
        m.scroll(5f, true)
        m.scroll(-12f, true)
        assertFalse(m.dock.running)
        m.scroll(-52f, true)
        advanceUntilIdle()
        assertEquals(1f, m.dock.value, 0f)
        m.scroll(32f, true)
        advanceUntilIdle()
        assertEquals(1f, m.dock.value, 0f)
        m.scroll(32f, true)
        advanceUntilIdle()
        assertEquals(0f, m.dock.value, 0f)
    }

    @Test
    fun newScrubAndSpringTakeOverWithoutOldAnimationCompleting() = runTest {
        val m = LabMotion(CoroutineScope(coroutineContext + Clock()))
        m.openPlayer(true)
        advanceTimeBy(200)
        assertTrue(m.player.value in 0.001f..0.999f)
        m.player.scrub(.3f)
        advanceTimeBy(1000)
        assertEquals(.3f, m.player.value, 0f)
        m.openPlayer(false, true)
        advanceUntilIdle()
        assertEquals(0f, m.player.value, 0f)
        m.timeline = "Capsule move"
        m.scrubTimeline(.6f)
        assertEquals(1.2f, m.capsule.value, 0f)
        m.cancelAll()
        assertFalse(m.player.running)
        assertEquals(0f, m.capsule.value, 0f)
    }

    @Test
    fun navigationGrabAndClampRetainFingerAnchor() {
        assertEquals(1f, navigationDragPosition(160f, 10f, 100f, 0f), 0f)
        assertEquals(1f, navigationDragPosition(175f, 10f, 100f, 15f), 0f)
        assertEquals(0, navigationDestination(-20f))
        assertEquals(2, navigationDestination(40f))
    }
}
