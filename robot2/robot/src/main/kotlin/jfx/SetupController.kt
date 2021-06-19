package jfx

import Robot
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.geometry.Point2D
import javafx.geometry.Rectangle2D
import javafx.scene.control.*
import javafx.scene.image.ImageView
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent
import javafx.stage.FileChooser
import javafx.stage.Stage
import javafx.util.Callback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import script.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.pow
import kotlin.math.round
import kotlin.text.Charsets.UTF_8

class SetupController {
    lateinit var stage: Stage

    @FXML
    private lateinit var currentScriptList: ListView<ProgramAction>
    @FXML
    private lateinit var addListView: ListView<CreateInfo>
    @FXML
    private lateinit var imageView: ImageView
    @FXML
    lateinit var leftStatus: Label
    @FXML
    lateinit var rightStatus: Label

    private var script = Script(ArrayList())

    @FXML
    private fun initialize() {
        addListView.cellFactory = Callback {
            val ret = ListCell<CreateInfo>()
            ret.onMouseClicked = EventHandler { evt ->
                if (ret.item == null) {
                    return@EventHandler
                }
                if (evt.clickCount == 2 && evt.button == MouseButton.PRIMARY) {
                    leftStatus.text = "Adding ${ret.item.first}"
                    GlobalScope.launch {
                        val add = ret.item.third(this@SetupController) { text ->
                            GlobalScope.launch(Dispatchers.JavaFx) {
                                rightStatus.text = text
                            }
                        }
                        GlobalScope.launch(Dispatchers.JavaFx) {
                            currentScriptList.items.add(add)
                            leftStatus.text = "Ready"
                            rightStatus.text = ""
                        }
                    }
                }
            }
            ret.itemProperty().addListener { _, _, newValue ->
                if (newValue == null) {
                    ret.text = ""
                    ret.tooltip = null
                    return@addListener
                }
                ret.text = newValue.first
                ret.tooltip = Tooltip(newValue.second)
            }
            ret
        }

        currentScriptList.cellFactory = Callback {
            val ret = ListCell<ProgramAction>()
            ret.itemProperty().addListener { _, _, newValue ->
                if (newValue == null) {
                    ret.text = ""
                    return@addListener
                }
                ret.text = newValue.toString()
            }
            ret
        }

        addListView.items.add(Triple("Click", "Click at a given point", MouseEvents::setupClick))
        addListView.items.add(Triple("Move", "Move to a given point", MouseEvents::setupMove))
        addListView.items.add(Triple("Hold", "Press and hold the mouse at a given point", MouseEvents::setupHold))
        addListView.items.add(Triple("Release", "Release the mouse", MouseEvents::setupRelease))
        addListView.items.add(Triple("Wallet Create Clicks", "Signal the OCR should create a wallet", MouseEvents::setupWalletClicks))
        addListView.items.add(Triple("Sleep", "Sleep for a given period", RandomEvents::setupSleep))
        addListView.items.add(Triple("Select and Copy", "Select the text under the cursor and copy it", TextKeyboardEvents::setupTripleClickCopy))
        addListView.items.add(Triple("Write Plain Text", "Write text to a given position", TextKeyboardEvents::setupWrite))
        addListView.items.add(Triple("Write from Variable", "Write the text stored in a variable to a given position", TextKeyboardEvents::setupVarWrite))
        addListView.items.add(Triple("Append JSON", "Append a JSON string of variables to aa file", FileEvents::setupAppendJson))
        addListView.items.add(Triple("Append Log", "Append a templated log string to a file", FileEvents::setupAppendLog))
        addListView.items.add(Triple("Fail if not Color", "If a pixel is not a color, cancel script execution", RandomEvents::setupFailIfNotColor))
        addListView.items.add(Triple("Set Variable Explicit", "Explicitly set variable", RandomEvents::setupSetVarExplicit))
        addListView.items.add(Triple("Clipboard to Variable", "Copy to CLipboard to a variable", RandomEvents::setupClipboardToVariable))
    }

    private var savePath: Path? = null
    @FXML
    private fun saveScript() {
        if (savePath == null) {
            saveScriptAs()
            return
        }
        commonSave()
    }

    @FXML
    private fun saveScriptAs() {
        val fc = FileChooser()
        fc.extensionFilters.clear()
        fc.extensionFilters += FileChooser.ExtensionFilter("NJScript File", "*.njs")
        fc.title = "Select file to save as"
        val saveFile = fc.showSaveDialog(stage)?: return
        savePath = if (saveFile.extension != "njs") Paths.get(saveFile.absolutePath + ".njs") else saveFile.toPath()
        commonSave()
    }

    private fun commonSave() {
        requireNotNull(savePath)
        Files.write(savePath ?: error("savepath"), Json.encodeToString(Script.serializer(), createScript()).toByteArray(UTF_8))
        rightStatus.text = "Saved"
    }

    private fun createScript(): Script {
        return Script(currentScriptList.items.toCollection(ArrayList()))
    }

    @FXML
    private fun loadScript() {
        val fc = FileChooser()
        fc.extensionFilters.clear()
        fc.extensionFilters += FileChooser.ExtensionFilter("NJScript File", "*.njs")
        fc.title = "Select file to load"
        val loadPath = fc.showOpenDialog(stage)?.toPath() ?: return
        val ts = Json.decodeFromString(Script.serializer(), Files.readString(loadPath))
        currentScriptList.items.clear()
        ts.items.forEach { currentScriptList.items.add(it) }
    }

    @FXML
    private fun deleteItem() {
        val selectedIndex = currentScriptList.selectionModel.selectedIndex
        currentScriptList.items.removeAt(selectedIndex)
    }

    @FXML
    private fun moveDown() {
        val selectedIndex = currentScriptList.selectionModel.selectedIndex
        if (selectedIndex == currentScriptList.items.lastIndex) {
            return
        }
        currentScriptList.items.add(selectedIndex + 1, currentScriptList.items.removeAt(selectedIndex))
        currentScriptList.selectionModel.select(selectedIndex + 1)
    }

    @FXML
    private fun moveUp() {
        val selectedIndex = currentScriptList.selectionModel.selectedIndex
        if (selectedIndex == 0) {
            return
        }
        currentScriptList.items.add(selectedIndex - 1, currentScriptList.items.removeAt(selectedIndex))
        currentScriptList.selectionModel.select(selectedIndex - 1)
    }

    @FXML
    private fun imageScroll(event: ScrollEvent) {
        val delta = -event.deltaY
        val viewport = imageView.viewport

        val scale = clamp(
            1.01.pow(delta),  // don't scale so we're zoomed in to fewer than MIN_PIXELS in any direction:
            (40.0 / viewport.width).coerceAtMost(40.0 / viewport.height),  // don't scale so that we're bigger than image dimensions:
            (imageView.image.width / viewport.width).coerceAtLeast(imageView.image.height / viewport.height)
        )

        val mouse = imageViewToImage(imageView, Point2D(event.x, event.y))

        val newWidth = viewport.width * scale
        val newHeight = viewport.height * scale

        val newMinX = clamp(
            mouse.x - (mouse.x - viewport.minX) * scale,
            0.0, imageView.image.width - newWidth
        )
        val newMinY = clamp(
            mouse.y - (mouse.y - viewport.minY) * scale,
            0.0, imageView.image.height - newHeight
        )

        imageView.viewport = Rectangle2D(newMinX, newMinY, newWidth, newHeight)
    }

    @FXML
    private fun imageMouseClick(event: MouseEvent) {
        if (event.button != MouseButton.PRIMARY) {
            return
        }
        if (imageView.image != null) {
            lc = imageViewToImage(imageView, Point2D(event.x, event.y))
        }
        ab.set(true)
    }

    @FXML
    private fun run() {
        GlobalScope.launch(Dispatchers.IO) {
            GlobalScope.launch(Dispatchers.JavaFx) {
                stage.isIconified = true
                delay(250)
                script = createScript()
                script.execute()
                stage.isIconified = false
            }
        }
    }

    fun takeScreenshotIntoImageView() {
        Robot.fxThread {
            stage.isIconified = false
            Thread.sleep(250)
            imageView.image = Robot.fxScreenshot()
            imageView.viewport = with(imageView.image) { Rectangle2D(0.0, 0.0, width, height) }
            stage.isIconified = false
        }
    }

    @Volatile
    private var lc = Point2D(0.0, 0.0)
    private val ab = AtomicBoolean(false)
    fun getImageClickCoordinates(): Point2D {
        ab.set(false)
        while (!ab.get()) {}
        ab.set(false)
        return Point2D(round(lc.x), round(lc.y))
    }

    fun getImageColor(imgCC: Point2D): Int {
        return 6189796
    }

    private fun imageViewToImage(imageView: ImageView, imageViewCoordinates: Point2D): Point2D {
        val xProportion: Double = imageViewCoordinates.x / imageView.boundsInLocal.width
        val yProportion: Double = imageViewCoordinates.y / imageView.boundsInLocal.height
        val viewport = imageView.viewport
        return Point2D(viewport.minX + xProportion * viewport.width, viewport.minY + yProportion * viewport.height)
    }

    private fun clamp(value: Double, min: Double, max: Double): Double {
        return if (value < min) min else if (value > max) max else value
    }
}

private typealias CreateInfo = Triple<String, String, (SetupController, (String) -> Unit) -> ProgramAction?>
