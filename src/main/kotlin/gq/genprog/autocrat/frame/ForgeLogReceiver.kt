package gq.genprog.autocrat.frame

import io.github.hedgehog1029.frame.logging.ILogReceiver
import org.apache.logging.log4j.LogManager
import java.util.logging.Level

class ForgeLogReceiver: ILogReceiver {
    val logger = LogManager.getLogger("autocrat")

    override fun printLog(level: Level, message: String) {
        val apacheLevel: org.apache.logging.log4j.Level = when (level) {
            Level.FINER, Level.FINEST -> org.apache.logging.log4j.Level.TRACE
            Level.FINE -> org.apache.logging.log4j.Level.DEBUG
            Level.INFO -> org.apache.logging.log4j.Level.INFO
            Level.WARNING -> org.apache.logging.log4j.Level.WARN
            Level.SEVERE -> org.apache.logging.log4j.Level.FATAL
            else -> org.apache.logging.log4j.Level.INFO
        }

        logger.log(apacheLevel, message)
    }
}