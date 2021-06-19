package script

import jfx.SetupController

object MouseEvents {
    fun setupHold(controller: SetupController, rightStatusCallback: (String) -> Unit): ProgramAction? {
        rightStatusCallback("Click where you want the program to hold")
        controller.takeScreenshotIntoImageView()
        val clickPos = controller.getImageClickCoordinates()
        return ProgramAction.MouseHold(clickPos.x, clickPos.y)
    }

    fun setupRelease(controller: SetupController, rightStatusCallback: (String) -> Unit): ProgramAction? {
        return ProgramAction.MouseRelease
    }

    fun setupClick(controller: SetupController, rightStatusCallback: (String) -> Unit): ProgramAction? {
        rightStatusCallback("Click where you want the program to click")
        controller.takeScreenshotIntoImageView()
        val clickPos = controller.getImageClickCoordinates()
        return ProgramAction.Click(clickPos.x, clickPos.y)
    }

    fun setupMove(controller: SetupController, rightStatusCallback: (String) -> Unit): ProgramAction? {
        rightStatusCallback("Click where you want the program to move")
        controller.takeScreenshotIntoImageView()
        val clickPos = controller.getImageClickCoordinates()
        return ProgramAction.Click(clickPos.x, clickPos.y)
    }

    fun setupWalletClicks(controller: SetupController, rightStatusCallback: (String) -> Unit): ProgramAction? {
        controller.takeScreenshotIntoImageView()
        rightStatusCallback("Click just above all of the words")
        val abovePos = controller.getImageClickCoordinates()
        rightStatusCallback("Click just below all of the words")
        val belowPos = controller.getImageClickCoordinates()
        rightStatusCallback("Click on where the final word will always be")
        val finalPos = controller.getImageClickCoordinates()
        return ProgramAction.DoWalletClicks(abovePos.y, belowPos.y, finalPos.x, finalPos.y)
    }
}
