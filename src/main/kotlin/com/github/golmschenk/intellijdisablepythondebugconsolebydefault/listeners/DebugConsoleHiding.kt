package com.github.golmschenk.intellijdisablepythondebugconsolebydefault.listeners

import com.intellij.execution.console.DuplexConsoleView
import com.intellij.execution.ui.ConsoleView
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.Timer

/**
 * Hides the interactive Python debug console of a single debug session.
 *
 * A session is announced before its console is attached, and both the attaching and the switches
 * PyCharm performs on the console happen asynchronously, so there is no single moment at which the
 * console can be corrected. Therefore the console is awaited, and once it is there the process output
 * is re-asserted for a short while to stay behind every switch PyCharm still has queued. Afterwards
 * the console is left alone, so switching to the debug console by hand keeps working.
 *
 * All work happens on the event dispatch thread, driven by a [Timer].
 */
class DebugConsoleHiding(
    private val isSessionFinished: () -> Boolean,
    private val consoleProvider: () -> ConsoleView?,
) : ActionListener {

    private val timer = Timer(POLL_DELAY_MILLIS, this)
    private var waitedMillis = 0
    private var assertedMillis = 0

    val isRunning: Boolean
        get() = timer.isRunning

    fun start() {
        timer.start()
    }

    override fun actionPerformed(event: ActionEvent) {
        if (isSessionFinished()) {
            timer.stop()
            return
        }
        val console = consoleProvider()
        if (console !is DuplexConsoleView<*, *>) {
            waitedMillis += POLL_DELAY_MILLIS
            if (waitedMillis >= CONSOLE_WAIT_MILLIS) {
                timer.stop()
            }
            return
        }
        hideDebugConsole(console)
        assertedMillis += POLL_DELAY_MILLIS
        if (assertedMillis >= ASSERTION_WINDOW_MILLIS) {
            timer.stop()
        }
    }

    companion object {
        private const val POLL_DELAY_MILLIS = 100
        private const val CONSOLE_WAIT_MILLIS = 30_000
        private const val ASSERTION_WINDOW_MILLIS = 1_500

        /**
         * Shows the process output instead of the interactive debug console.
         *
         * Returns whether the console had to be switched.
         */
        fun hideDebugConsole(console: ConsoleView?): Boolean {
            val duplexConsole = console as? DuplexConsoleView<*, *> ?: return false
            if (duplexConsole.isPrimaryConsoleEnabled) return false
            duplexConsole.enableConsole(true)
            return true
        }
    }
}
