package script

import Main
import Robot
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.sourceforge.tess4j.Word
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.*
import kotlin.system.measureNanoTime
import kotlin.text.Charsets.UTF_8

@Serializable
sealed class ProgramAction(@Transient val globalID: Int = id++) {
    abstract fun execute(script: Script)

    @Serializable
    class Click(val x: Double, val y: Double): ProgramAction() {
        override fun execute(script: Script) {
            Robot.mouseClick(x, y)
        }

        override fun toString(): String {
            return "Click(x=$x, y=$y)"
        }
    }

    @Serializable
    class Move(val x: Double, val y: Double): ProgramAction() {
        override fun execute(script: Script) {
            Robot.mouseMove(x, y)
        }

        override fun toString(): String {
            return "Move(x=$x, y=$y)"
        }
    }

    @Serializable
    class MouseHold(val x: Double, val y: Double): ProgramAction() {
        override fun execute(script: Script) {
            Robot.mouseMove(x, y)
            Robot.mouseHold()
        }

        override fun toString(): String {
            return "MouseHold(x=$x, y=$y)"
        }
    }

    @Serializable
    object MouseRelease: ProgramAction() {
        override fun execute(script: Script) {
            Robot.mouseRelease()
        }

        override fun toString(): String {
            return "MouseRelease"
        }
    }

    @Serializable
    class CopyText(val x: Double, val y: Double, val varName: String): ProgramAction() {
        override fun execute(script: Script) {
            repeat(3) {
                Robot.mouseClick(x, y)
            }
            script.variables[varName] = Robot.getClipboardText()
            println("Copied '${Robot.getClipboardText()}' into $varName")
        }

        override fun toString(): String {
            return "CopyText(x=$x, y=$y, varName='$varName')"
        }
    }

    @Serializable
    class Sleep(val timeMs: Int): ProgramAction() {
        override fun execute(script: Script) {
            Thread.sleep(timeMs.toLong())
        }

        override fun toString(): String {
            return "Sleep(timeMs=$timeMs)"
        }
    }

    @Serializable
    class WriteText(val x: Double, val y: Double, val text: String): ProgramAction() {
        override fun execute(script: Script) {
            Robot.mouseClick(x, y)
            Robot.writeText(text)
        }

        override fun toString(): String {
            return "WriteText(x=$x, y=$y, text='$text')"
        }
    }

    @Serializable
    class WriteVar(val x: Double, val y: Double, val varName: String): ProgramAction() {
        override fun execute(script: Script) {
            Robot.mouseClick(x, y)
            Robot.writeText(script.variables[varName].toString())
        }

        override fun toString(): String {
            return "WriteVar(x=$x, y=$y, varName='$varName')"
        }
    }

    @Serializable
    class AppendLog(val file: String, val template: String, var varNames: Array<String>): ProgramAction() {
        override fun execute(script: Script) {
            val mapped = varNames.map(script.variables::get).toTypedArray()
            Files.write(Paths.get(file), (template.format(*mapped) + '\n').toByteArray(UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND)
        }

        override fun toString(): String {
            return "AppendLog(file='$file', template='$template', varNames=${varNames.contentToString()})"
        }
    }

    @Serializable
    class AppendJson(val file: String, var items: Array<String>): ProgramAction() {
        override fun execute(script: Script) {
            val nis = items.map { it to script.variables[it] }.toMap()
            Files.write(Paths.get(file), (Json.encodeToString(nis) + '\n').toByteArray(UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND)
        }

        override fun toString(): String {
            return "AppendJson(file='$file', items=${items.contentToString()})"
        }
    }

    @Serializable
    class SaveClipboardToVar(val varName: String): ProgramAction() {
        override fun execute(script: Script) {
            script.variables[varName] = Robot.getClipboardText()
        }

        override fun toString(): String {
            return "SaveClipboardToVar(varName='$varName')"
        }
    }

    @Serializable
    class SetVariableExplicit(val varName: String, val template: String): ProgramAction() {
        override fun execute(script: Script) {
            script.variables[varName] = template.replace(
                Regex("\\{uuid}"),
                UUID.randomUUID().toString().replace("-", "").toUpperCase()
            )
        }

        override fun toString(): String {
            return "SetVariableExplicit(varName='$varName', template='$template')"
        }
    }

    @Serializable
    class FailIfNotColor(val x: Double, val y: Double, val colorRGB: Int): ProgramAction() {
        override fun execute(script: Script) {
            if (colorRGB != Robot.getPixelColor(x, y)) {
                script.fail()
            }
        }

        override fun toString(): String {
            return "FailIfNotColor(x=$x, y=$y, colorRGB=$colorRGB)"
        }
    }

    @Serializable
    class PrintColorAt(val x: Double, val y: Double): ProgramAction() {
        override fun execute(script: Script) {
            println(Robot.getPixelColor(x, y))
        }

        override fun toString(): String {
            return "PrintColorAt(x=$x, y=$y)"
        }
    }

    @Serializable
    class DoWalletClicks(val below: Double, val above: Double, val finalX: Double, val finalY: Double): ProgramAction() {
        override fun execute(script: Script) {
            val wants = script.variables["words"]!!.split(" ")
            Robot.mouseMove(1.0, 1.0)
            Thread.sleep(25)
            val q = LinkedList(wants)
            while (q.isNotEmpty()) {
                val next = q.poll()
                if (q.isEmpty()) {
                    Robot.mouseClick(finalX, finalY)
                    return
                }

                val inImage = Robot.swingScreenshot()
                val image = inImage.getSubimage(0, below.toInt(), inImage.width, (above - below).toInt())

                val words: List<Word>
                val time = measureNanoTime { words = Main.tesseract.getWords(image, 3) }
                println("tesseract took ${time / 1e6} ms")
                var bound = words[0].boundingBox
                var bestScore = Int.MAX_VALUE
                for (i in words.indices) {
                    if (Main.calculate(words[i].text.toLowerCase(), next) < bestScore) {
                        bestScore = Main.calculate(words[i].text.toLowerCase(), next)
                        println("best word for $next is ${words[i]}")
                        bound = words[i].boundingBox
                    }
                }
                if (bestScore >= 2) {
                    println("failed to get word $next")
                    script.fail()
                    return
                }
                with(bound) {
                    Robot.mouseClick((x + (width / 2)).toDouble(), below + (y + (height / 2)).toDouble())
                }
                Robot.mouseMove(1.0, 1.0)
            }
        }

        override fun toString(): String {
            return "DoWalletClicks(below=$below, above=$above, finalX=$finalX, finalY=$finalY)"
        }
    }

    companion object {
        private var id = 0
    }
}
