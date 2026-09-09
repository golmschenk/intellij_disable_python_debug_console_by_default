package com.github.golmschenk.intellijdisablepythondebugconsolebydefault.listeners

import com.intellij.execution.console.DuplexConsoleView
import com.intellij.xdebugger.XDebugProcess
import com.intellij.xdebugger.XDebuggerManagerListener

/**
 * Hides the interactive Python debug console when a debug session starts.
 *
 * The console tab of the debug tool window is a [DuplexConsoleView]: its primary side shows the plain
 * process output and its secondary side shows the interactive Python debug console. PyCharm decides
 * which side to show twice while a session starts, and the second decision ignores the
 * "Always show debug console" setting (see PY-61959), so the interactive console always wins. This
 * listener switches the console tab back to the process output once the session is up. The debug
 * console stays one click away through the "Show Debug Console" toggle of the console tab.
 */
class DebugConsoleListener : XDebuggerManagerListener {

    override fun processStarted(debugProcess: XDebugProcess) {
        val session = debugProcess.session
        DebugConsoleHiding(
            isSessionFinished = { session.isStopped },
            consoleProvider = { session.consoleView },
        ).start()
    }
}
