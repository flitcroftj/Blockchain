package script

import jfx.SetupController
import jfx.StringPopupController

object FileEvents {
    fun setupAppendJson(controller: SetupController, rightStatusCallback: (String) -> Unit): ProgramAction? {
        val fileName = StringPopupController.getString(controller.stage, "File to append to: ")
        val vars = StringPopupController.getString(controller.stage, "Comma separated variables to append: ").split(", ").map(String::trim).toTypedArray()
        return ProgramAction.AppendJson(fileName, vars)
    }

    fun setupAppendLog(controller: SetupController, rightStatusCallback: (String) -> Unit): ProgramAction? {
        val fileName = StringPopupController.getString(controller.stage, "File to append to: ")
        val template = StringPopupController.getString(controller.stage, "Format String to use: ")
        val vars = StringPopupController.getString(controller.stage, "Comma separated variables to use: ").split(", ").map(String::trim).toTypedArray()
        return ProgramAction.AppendLog(fileName, template, vars)
    }
}