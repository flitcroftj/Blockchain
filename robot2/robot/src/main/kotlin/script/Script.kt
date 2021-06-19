package script

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Script(val items: ArrayList<ProgramAction>) {
    @Transient
    private var run = true
    @Transient
    val variables = HashMap<String, String>()

    fun execute() {
        Robot.lastPosition = null
        currentScript = this
        run = true
        for (op in items) {
            op.execute(this)
            if (!run) {
                return
            }
        }
    }

    fun fail() {
        run = false
    }

    companion object {
        lateinit var currentScript: Script
    }
}