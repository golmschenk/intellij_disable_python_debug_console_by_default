package com.github.golmschenk.intellijdisablepythondebugconsolebydefault.listeners

import com.intellij.execution.console.DuplexConsoleView
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.ui.ConsoleView
import com.intellij.openapi.util.Disposer
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.ui.UIUtil

class DebugConsoleHidingTest : BasePlatformTestCase() {

    fun testHideDebugConsoleShowsProcessOutput() {
        val duplexConsole = createDuplexConsole()
        duplexConsole.enableConsole(false)
        assertFalse(duplexConsole.isPrimaryConsoleEnabled)

        assertTrue(DebugConsoleHiding.hideDebugConsole(duplexConsole))
        assertTrue(duplexConsole.isPrimaryConsoleEnabled)
    }

    fun testHideDebugConsoleLeavesProcessOutputUntouched() {
        val duplexConsole = createDuplexConsole()
        duplexConsole.enableConsole(true)
        assertTrue(duplexConsole.isPrimaryConsoleEnabled)

        assertFalse(DebugConsoleHiding.hideDebugConsole(duplexConsole))
        assertTrue(duplexConsole.isPrimaryConsoleEnabled)
    }

    fun testHideDebugConsoleIgnoresPlainConsoles() {
        val console = createConsole()
        Disposer.register(testRootDisposable, console)

        assertFalse(DebugConsoleHiding.hideDebugConsole(console))
    }

    fun testHideDebugConsoleIgnoresMissingConsole() {
        assertFalse(DebugConsoleHiding.hideDebugConsole(null))
    }

    /**
     * Reproduces the order in which PyCharm starts a session: the console is attached after the debug
     * process has been announced, and it is switched to the interactive debug console once more from a
     * task that was queued while the session was starting.
     */
    fun testProcessOutputIsAssertedOnConsoleAttachedLateAndSwitchedAgain() {
        val duplexConsole = createDuplexConsole()
        duplexConsole.enableConsole(false)
        var attachedConsole: ConsoleView? = null
        val hiding = DebugConsoleHiding(isSessionFinished = { false }, consoleProvider = { attachedConsole })

        hiding.start()
        dispatchEventsFor(300)
        assertTrue("Waiting for the console was given up.", hiding.isRunning)
        assertFalse(duplexConsole.isPrimaryConsoleEnabled)

        attachedConsole = duplexConsole
        assertTrue(
            "The debug console of the attached console was not hidden.",
            dispatchEventsUntil { duplexConsole.isPrimaryConsoleEnabled },
        )

        duplexConsole.enableConsole(false)
        assertTrue(
            "The debug console was not hidden again after PyCharm switched to it.",
            dispatchEventsUntil { duplexConsole.isPrimaryConsoleEnabled },
        )

        assertTrue("The process output was asserted for too long.", dispatchEventsUntil { !hiding.isRunning })
        assertTrue(duplexConsole.isPrimaryConsoleEnabled)
    }

    fun testFinishedSessionIsLeftAlone() {
        val duplexConsole = createDuplexConsole()
        duplexConsole.enableConsole(false)
        val hiding = DebugConsoleHiding(isSessionFinished = { true }, consoleProvider = { duplexConsole })

        hiding.start()

        assertTrue("The finished session was kept polling.", dispatchEventsUntil { !hiding.isRunning })
        assertFalse(duplexConsole.isPrimaryConsoleEnabled)
    }

    /** [DuplexConsoleView] takes over the disposal of both consoles it is given. */
    private fun createDuplexConsole(): DuplexConsoleView<ConsoleView, ConsoleView> {
        val duplexConsole = DuplexConsoleView(createConsole(), createConsole())
        Disposer.register(testRootDisposable, duplexConsole)
        return duplexConsole
    }

    private fun createConsole(): ConsoleView =
        TextConsoleBuilderFactory.getInstance().createBuilder(project).console

    private fun dispatchEventsFor(millis: Long) {
        val deadline = System.currentTimeMillis() + millis
        while (System.currentTimeMillis() < deadline) {
            UIUtil.dispatchAllInvocationEvents()
            Thread.sleep(DISPATCH_INTERVAL_MILLIS)
        }
    }

    private fun dispatchEventsUntil(condition: () -> Boolean): Boolean {
        val deadline = System.currentTimeMillis() + DISPATCH_TIMEOUT_MILLIS
        while (System.currentTimeMillis() < deadline) {
            if (condition()) return true
            UIUtil.dispatchAllInvocationEvents()
            Thread.sleep(DISPATCH_INTERVAL_MILLIS)
        }
        return condition()
    }

    companion object {
        private const val DISPATCH_INTERVAL_MILLIS = 10L
        private const val DISPATCH_TIMEOUT_MILLIS = 10_000L
    }
}
