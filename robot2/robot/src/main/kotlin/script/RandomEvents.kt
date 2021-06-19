package script

import javafx.fxml.FXMLLoader
import javafx.stage.Popup
import jfx.SetupController
import jfx.SleepPopupController
import jfx.StringPopupController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

object RandomEvents {
    fun setupSleep(controller: SetupController, rightStatusCallback: (String) -> Unit): ProgramAction? {
        val msWait = AtomicInteger(-1)
        GlobalScope.launch(Dispatchers.JavaFx) {
            val popup = Popup()
            val loader = FXMLLoader()
            loader.location = RandomEvents::class.java.classLoader.getResource("fxml/SleepPopup.fxml")
            popup.content.add(loader.load())
            val sleepController = loader.getController<SleepPopupController>()
            sleepController.popup = popup
            sleepController.wait = msWait
            popup.show(controller.stage)
        }

        while (msWait.get() == -1) {
            Thread.sleep(5)
        }
        return ProgramAction.Sleep(msWait.get())
    }

    fun setupFailIfNotColor(controller: SetupController, rightStatusCallback: (String) -> Unit): ProgramAction? {
        rightStatusCallback("Click where you want the program to check")
        controller.takeScreenshotIntoImageView()
        val clickPos = controller.getImageClickCoordinates()
        return ProgramAction.FailIfNotColor(clickPos.x, clickPos.y, controller.getImageColor(clickPos))
    }

    fun setupSetVarExplicit(controller: SetupController, rightStatusCallback: (String) -> Unit): ProgramAction? {
        val varName = StringPopupController.getString(controller.stage, "VarName to set: ")
        rightStatusCallback("{uuid} will be replaced with a new, random UUID")
        val template = StringPopupController.getString(controller.stage, "Text to set to: ")
        return ProgramAction.SetVariableExplicit(varName, template)
    }

    fun setupClipboardToVariable(controller: SetupController, rightStatusCallback: (String) -> Unit): ProgramAction? {
        val varName = StringPopupController.getString(controller.stage, "VarName to set: ")
        return ProgramAction.SaveClipboardToVar(varName)
    }
}