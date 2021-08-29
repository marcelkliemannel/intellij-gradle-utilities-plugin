# Changelog

## [Unreleased]
### Added

### Changed

### Removed

### Fixed
## [1.2.2] - 2021-08-29
### Fixed

- Fix context menu is visible in the running daemons table even though there are no entries (GitHub issue #13)
- Fix "Update Gradle Wrapper" command uses wrong version (GitHub issue #15)

## [1.2.1] - 2021-07-28
### Fixed
- Fix compatibility with IntelliJ 2021.2

## [1.2.0] - 2021-05-29
### Added
- Add action to open Gradle user home (GitHub issue #11). 
- Check if Gradle wrapper JAR checksum verification is configured (GitHub issue #9).
- Collect running Gradle daemons improvements (GitHub issue #7):
    - Show uptime of Gradle daemons.
    - Provide a detailed description for collecting the statuses.

## [1.1.0] - 2021-05-17
### Added
- Actions are now cancelable if they were started from a dialog (GitHub issue #4).

## [1.0.1] - 2021-05-17
### Fixed
- The deletion of directories was disabled for testing and was not re-enabled for the productive deployment (GitHub issue #2).

## [1.0.0] - 2021-05-16
### Added
- Initial release.