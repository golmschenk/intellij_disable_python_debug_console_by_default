<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Disable Python Debug Console by Default Changelog

## [0.1.0]
Added AI slop warning.

## [0.0.2]
### Fixed
- Actually hide the debug console: the console tab of the debug tool window is now switched back to the process output
  after PyCharm forces the interactive Python debug console on it.

### Removed
- Tab selection and content attraction handling, which did not affect the debug console at all.

## [0.0.1]
### Added
- Debug session listener to hide debug console and select Debugger / Variables tab by default.
- Cancellation of unwanted console attraction policies on breakpoint events.
