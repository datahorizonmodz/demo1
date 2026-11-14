package id.datzon.glasslab

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import java.io.File
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w393dp-h873dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LabUiTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    private fun capture(name: String) {
        compose.runOnIdle {
            val root = compose.activity.window.decorView
            val bitmap = hardwareCapture(root.width, root.height) { root.draw(it) }
            val file = File("build/reports/preview/$name.png")
            file.parentFile?.mkdirs()
            file.outputStream().use {
                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
            }
        }
    }

    @Test
    fun editorAndPlaygroundUseLivePreviewWithoutCoveringDock() {
        compose.setContent { GlassLabTheme { GlassLabScreen() } }
        compose.waitForIdle()
        capture("home")
        compose.onNodeWithText("Components").performClick()
        compose.onAllNodesWithText("Floating mini player").onFirst().assertIsDisplayed()
        compose.onNodeWithText("Optics").assertIsDisplayed()
        capture("editor")
        compose.onNodeWithText("Animation").performClick()
        compose.onNodeWithText("Test Compact").performScrollTo().performClick()
        compose.waitForIdle()
        capture("compact")
        compose.onNodeWithText("Test Open full player").performScrollTo().performClick()
        compose.waitForIdle()
        capture("full-player")
        compose.onNodeWithContentDescription("Close full player").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Export").performClick()
        compose.onNodeWithText("Copy Compose Component").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun touchGesturesSnapCancelAndOpenTheSamePlayer() {
        lateinit var motion: LabMotion
        var density = 1f
        compose.setContent {
            val scope = androidx.compose.runtime.rememberCoroutineScope()
            val state = androidx.compose.runtime.remember { LabMotion(scope) }
            val configs =
                androidx.compose.runtime.remember { Target.entries.associateWith(::originalConfig) }
            val options = androidx.compose.runtime.remember { PreviewOptions() }
            density = androidx.compose.ui.platform.LocalDensity.current.density
            androidx.compose.runtime.SideEffect {
                motion = state
                state.configs = configs
            }
            GlassLabTheme {
                NativePreview(
                    state,
                    configs,
                    options,
                    Target.MINI,
                    androidx.compose.ui.Modifier.fillMaxSize(),
                )
            }
        }
        val size = compose.onRoot().fetchSemanticsNode().size
        val w = size.width.toFloat()
        val h = size.height.toFloat()
        val nav = androidx.compose.ui.geometry.Offset(w * .43f, h - 46f * density)
        compose.onRoot().performTouchInput { click(nav) }
        compose.waitForIdle()
        compose.runOnIdle { org.junit.Assert.assertEquals(1, motion.selected) }
        compose.onRoot().performTouchInput { down(nav) }
        compose.mainClock.advanceTimeBy(600)
        compose.onRoot().performTouchInput {
            moveTo(androidx.compose.ui.geometry.Offset(w * .7f, nav.y))
            cancel()
        }
        compose.waitForIdle()
        compose.runOnIdle {
            org.junit.Assert.assertFalse(motion.navDragging)
            org.junit.Assert.assertEquals(1f, motion.capsule.value, .001f)
        }
        compose.onRoot().performTouchInput {
            swipe(
                androidx.compose.ui.geometry.Offset(w / 2, h - 112f * density),
                androidx.compose.ui.geometry.Offset(w / 2, h * .15f),
                400,
            )
        }
        compose.waitForIdle()
        compose.runOnIdle { org.junit.Assert.assertEquals(1f, motion.player.value, .001f) }
        compose.onRoot().performTouchInput {
            swipe(
                androidx.compose.ui.geometry.Offset(w / 2, h * .3f),
                androidx.compose.ui.geometry.Offset(w / 2, h * .94f),
                350,
            )
        }
        compose.waitForIdle()
        compose.runOnIdle { org.junit.Assert.assertEquals(0f, motion.player.value, .001f) }
        val plus = androidx.compose.ui.geometry.Offset(w - 46f * density, h - 46f * density)
        compose.onRoot().performTouchInput {
            down(plus)
            moveBy(androidx.compose.ui.geometry.Offset(-30f * density, 0f))
            cancel()
        }
        compose.waitForIdle()
        compose.runOnIdle { org.junit.Assert.assertEquals(0f, motion.plusX.value, .001f) }
    }
}
