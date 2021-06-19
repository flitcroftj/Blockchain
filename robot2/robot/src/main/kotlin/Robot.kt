import javafx.application.Platform
import javafx.embed.swing.SwingFXUtils
import javafx.geometry.Rectangle2D
import javafx.scene.image.WritableImage
import javafx.scene.input.Clipboard
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseButton
import javafx.scene.robot.Robot
import java.awt.Toolkit
import java.awt.image.BufferedImage
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs
import kotlin.system.exitProcess

object Robot {
    /**
     * The  amount of movement that is allowed from the mouse in a square before it is decided they are trying to exit
     */
    private const val USER_SHAKE_BOUNDS = 5.0
    lateinit var robot: Robot

    private val screenSize =
        Toolkit.getDefaultToolkit().screenSize.run { Rectangle2D(0.0, 0.0, width.toDouble(), height.toDouble()) }

    fun fxScreenshot(): WritableImage = fxThread { robot.getScreenCapture(null, screenSize) }
    fun swingScreenshot(): BufferedImage = SwingFXUtils.fromFXImage(fxScreenshot(), null)

    var lastPosition: Pair<Double, Double>? = null
    fun mouseMove(x: Double, y: Double, ignore: Boolean = false) = fxThread {
        if (lastPosition != null && !ignore) {
            val pos = robot.mousePosition
            if (abs(pos.x - lastPosition!!.first) > USER_SHAKE_BOUNDS || abs(pos.y - lastPosition!!.second) > USER_SHAKE_BOUNDS) {
                println("Exiting from user input! (lastPos $lastPosition, curPos $pos)")
                //alert("Exiting from user input!")
                exitProcess(0)
            }
        }
        lastPosition = if (!ignore) {
            x to y
        } else {
            null
        }
        println("moving to $x $y")
        robot.mouseMove(x, y)
        Thread.sleep(17)
    }

    fun mouseClick(x: Double, y: Double) = fxThread {
        mouseMove(x, y)
        robot.mouseClick(MouseButton.PRIMARY)
    }

    fun getClipboardText(): String = fxThread {
        robot.keyPress(KeyCode.CONTROL)
        Thread.sleep(17)
        robot.keyPress(KeyCode.C)
        Thread.sleep(17)
        robot.keyRelease(KeyCode.C)
        Thread.sleep(17)
        robot.keyRelease(KeyCode.CONTROL)
        Thread.sleep(17)
        return@fxThread Clipboard.getSystemClipboard().string
    }

    val repls = hashMapOf(
        '_' to "underscore", '-' to "minus", '0' to "NUMPAD0", '1' to "NUMPAD1", '2' to "NUMPAD2", '3' to "NUMPAD3",
        '4' to "NUMPAD4", '5' to "NUMPAD5", '6' to "NUMPAD6", '7' to "NUMPAD7", '8' to "NUMPAD8", '9' to "NUMPAD9",
        '.' to "PERIOD"
    )

    fun writeText(text: String) = fxThread {
        println("writing text '$text'")
        for (c in text) {
            val str = repls[c] ?: c.toString()
            if (c.isUpperCase()) {
                robot.keyPress(KeyCode.SHIFT)
            }
            robot.keyType(KeyCode.valueOf(str.toUpperCase()))
            if (c.isUpperCase()) {
                robot.keyRelease(KeyCode.SHIFT)
            }
            Thread.sleep(5)
        }
    }

    fun mouseHold() = fxThread {
        robot.mousePress(MouseButton.PRIMARY)
    }

    fun mouseRelease() = fxThread {
        robot.mouseRelease(MouseButton.PRIMARY)
    }

    fun getPixelColor(x: Double, y: Double): Int = fxThread {
        return@fxThread robot.getPixelColor(x, y)
            .run { ((red * 255).toInt() shl 16) or ((green * 255).toInt() shl 8) or (blue * 255).toInt() }
    }

    fun <T> fxThread(block: () -> T): T {
        return if (Platform.isFxApplicationThread()) {
            block()
        } else {
            val await = AtomicBoolean(false)
            var ret: T? = null
            Platform.runLater {
                ret = block()
                await.set(true)
            }
            while (!await.get()) {
            }
            ret!!
        }
    }

    fun getMousePos(): Pair<Double, Double> {
        return robot.mousePosition.run { x to y }
    }
}