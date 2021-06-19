import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.layout.Pane
import javafx.stage.Stage
import jfx.SetupController
import kotlinx.serialization.json.Json
import net.sourceforge.tess4j.Tesseract
import net.sourceforge.tess4j.util.LoadLibs
import script.Script
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import kotlin.system.exitProcess

object Main {
    // If false, run the specified script forever. Else, show UI
    private const val DO_UI = true

    lateinit var tesseract: Tesseract
        private set

    @JvmStatic
    fun main(args: Array<String>) {
        if (System.getProperty("os.name").contains("windows", ignoreCase = true)) {
            println("windows")
            val tmpFolder: File = LoadLibs.extractTessResources("win32-x86-64")
            System.setProperty("java.library.path", tmpFolder.path)
        }

        tesseract = Tesseract()
        tesseract.setLanguage("eng")
        tesseract.setOcrEngineMode(1)
        tesseract.setTessVariable("user_defined_dpi", "70")

        val dataDirectory: Path = Paths.get(ClassLoader.getSystemResource("tesseract_data").toURI())
        if (!Files.exists(dataDirectory)) {
            Files.createDirectory(dataDirectory)
            Files.write(
                Paths.get("tesseract_data/eng.traineddata"),
                ClassLoader.getSystemResource("tesseract_data/eng.traineddata").readBytes()
            )
        }
        tesseract.setDatapath(dataDirectory.toString())

        Platform.startup {
            Robot.robot = javafx.scene.robot.Robot()

            if (DO_UI) {
                val stage = Stage()
                val loader = FXMLLoader()
                val hb = loader.load<Pane>(Main::class.java.classLoader.getResourceAsStream("fxml/Setup.fxml"))
                val scene = Scene(hb)
                stage.scene = scene
                val controller = loader.getController<SetupController>()
                controller.stage = stage

                stage.title = "Setup"
                stage.show()
            }
        }

        // Change name
        val script = Json.decodeFromString(Script.serializer(), Files.readString(Paths.get("t2.njs")))

        if (!DO_UI) {
            repeat(20) {
                script.execute()
            }
            exitProcess(0)
        }
    }

    fun calculate(x: String, y: String): Int {
        val dp = Array(x.length + 1) { IntArray(y.length + 1) }
        for (i in 0..x.length) {
            for (j in 0..y.length) {
                dp[i][j] = when {
                    i == 0 -> j
                    j == 0 -> i
                    else -> min(
                        dp[i - 1][j - 1] + costOfSubstitution(x[i - 1], y[j - 1]),
                        dp[i - 1][j] + 1,
                        dp[i][j - 1] + 1
                    )
                }
            }
        }
        return dp[x.length][y.length]
    }

    fun costOfSubstitution(a: Char, b: Char): Int {
        return if (a == b) 0 else 1
    }

    fun min(vararg numbers: Int): Int {
        return Arrays.stream(numbers)
            .min().orElse(Int.MAX_VALUE)
    }
}