package script

import jfx.SetupController
import jfx.StringPopupController

object TextKeyboardEvents {
    fun setupTripleClickCopy(controller: SetupController, rightStatusCallback: (String) -> Unit): ProgramAction? {
        rightStatusCallback("Click where you want the program to triple click and copy")
        controller.takeScreenshotIntoImageView()
        val clickPos = controller.getImageClickCoordinates()
        val varName = StringPopupController.getString(controller.stage, "Variable to copy into: ")
        return ProgramAction.CopyText(clickPos.x, clickPos.y, varName)
    }

    fun setupWrite(controller: SetupController, rightStatusCallback: (String) -> Unit): ProgramAction? {
        rightStatusCallback("Click where you want the program to write the text")
        controller.takeScreenshotIntoImageView()
        val clickPos = controller.getImageClickCoordinates()
        val textToWrite = StringPopupController.getString(controller.stage, "Text to write: ")
        return ProgramAction.WriteText(clickPos.x, clickPos.y, textToWrite)
    }

    fun setupVarWrite(controller: SetupController, rightStatusCallback: (String) -> Unit): ProgramAction? {
        rightStatusCallback("Click where you want the program to write the text")
        controller.takeScreenshotIntoImageView()
        val clickPos = controller.getImageClickCoordinates()
        val varName = StringPopupController.getString(controller.stage, "VarName to write from: ")
        return ProgramAction.WriteVar(clickPos.x, clickPos.y, varName)
    }
}