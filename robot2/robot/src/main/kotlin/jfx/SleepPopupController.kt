package jfx

import javafx.fxml.FXML
import javafx.scene.control.TextField
import javafx.stage.Popup
import java.util.concurrent.atomic.AtomicInteger

class SleepPopupController {
    lateinit var popup: Popup
    lateinit var wait: AtomicInteger

    @FXML
    private lateinit var entryBox: TextField

    @FXML
    private fun initialize() {
        entryBox.textProperty().addListener { _, _, newValue ->
            if (!newValue.matches(Regex("\\d*"))) {
                entryBox.text = newValue.replace(Regex("[^\\d]"), "");
            }
        }
    }

    @FXML
    fun ok() {
        val int = entryBox.text.toIntOrNull() ?: return
        wait.set(int)
        popup.hide()
    }
}
