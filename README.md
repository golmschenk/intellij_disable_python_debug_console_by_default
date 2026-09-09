# Disable Python Debug Console by Default

A plugin for JetBrains IDEs (PyCharm, IntelliJ IDEA) that counteracts [PY-61959](https://youtrack.jetbrains.com/issue/PY-61959/Always-shows-debug-console-option-is-not-disabled) by hiding the interactive Python debug console when a debug session starts.

## AI slop warning

This plugin is pure vibecoded, AI slop. JetBrains will eventually fix the real issue, so I devoted almost no actual
effort to checking or verifying the code. Expect terrible code quality. Use at your own risk.

## Features

- Starts every debug session on the plain process output instead of the interactive Python debug console.
- Works regardless of <kbd>Settings/Preferences</kbd> > <kbd>Build, Execution, Deployment</kbd> > <kbd>Console</kbd> > <kbd>Always show debug console</kbd>, which PyCharm currently ignores.
- Leaves the debug console one click away through the <kbd>Show Debug Console</kbd> toggle of the console tab.

## How it works

The console tab of the debug tool window holds two consoles: the process output and the interactive Python debug
console. While starting a session, PyCharm picks one of them twice. The second decision, in `PyDebugRunner`, tests the
Swing `isEnabled()` flag of the console component (always `true`) instead of the *Always show debug console* setting, so
the interactive console always wins.

This plugin listens for started debug processes. Since a session is announced before its console is attached, and both
the attaching and PyCharm's switches happen asynchronously, the plugin waits for the console and then re-asserts the
process output for a moment, which keeps it behind every switch PyCharm still has queued. Afterwards it leaves the
console alone, so switching to the debug console by hand keeps working.

## Installation

- Using the IDE built-in plugin system:
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "Disable Python Debug Console by Default"</kbd> > <kbd>Install</kbd>

- Manually:
  Download the latest release from GitHub and install it using:
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>
