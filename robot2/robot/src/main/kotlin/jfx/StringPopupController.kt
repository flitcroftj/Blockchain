package jfx

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.stage.Popup
import javafx.stage.Stage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import script.RandomEvents
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

class StringPopupController {
    lateinit var popup: Popup
    lateinit var wait: AtomicReference<String?>

    @FXML
    private lateinit var entryBox: TextField
    @FXML
    lateinit var customPrompt: Label

    @FXML
    fun ok() {
        if (entryBox.text.isEmpty()) {
            return
        }
        wait.set(entryBox.text)
        popup.hide()
    }

    companion object {
        fun getString(parent: Stage, promptText: String): String {
            val wait = AtomicReference<String?>(null)
            GlobalScope.launch(Dispatchers.JavaFx) {
                val popup = Popup()
                val loader = FXMLLoader()
                loader.location = RandomEvents::class.java.classLoader.getResource("fxml/StringPopup.fxml")
                popup.content.add(loader.load())
                val sleepController = loader.getController<StringPopupController>()
                sleepController.popup = popup
                sleepController.wait = wait
                sleepController.customPrompt.text = promptText
                popup.show(parent)
            }

            while (wait.get() == null) {
                Thread.sleep(5)
            }

            return wait.get()!!
        }
    }
}
