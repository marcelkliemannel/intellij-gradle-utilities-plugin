# Changelog

## Unreleased

### Added

### Changed

### Removed

### Fixed

## 1.2.5 - 2023-09-16

### Added
- Support of configured SSL certificates for HTTP requests

### Fixed
- Fix API breaking code changes in newer IntelliJ versions
- Fix parsing of Gradle Wrapper version

## 1.2.4 - 2022-03-12

### Fixed
- Actions error dialogs used message as title and title as message
- Don't fail "Manage Gradle Daemons" action if there is an I/O error during process collection

## 1.2.3 - 2021-11-22

### Changed
- Fix incompatibilities with future IntelliJ releases

## 1.2.2 - 2021-08-29

### Fixed
- Fix context menu is visible in the running daemons table even though there are no entries (GitHub issue #13)
- Fix "Update Gradle Wrapper" command uses wrong version (GitHub issue #15)

## 1.2.1 - 2021-07-28

### Fixed
- Fix compatibility with IntelliJ 2021.2

## 1.2.0 - 2021-05-29

### Added
- Add action to open Gradle user home (GitHub issue #11). 
- Check if Gradle wrapper JAR checksum verification is configured (GitHub issue #9).
- Collect running Gradle daemons improvements (GitHub issue #7):
    - Show uptime of Gradle daemons.
    - Provide a detailed description for collecting the statuses.

## 1.1.0 - 2021-05-17

### Added
- Actions are now cancelable if they were started from a dialog (GitHub issue #4).

## 1.0.1 - 2021-05-17

### Fixed
- The deletion of directories was disabled for testing and was not re-enabled for the productive deployment (GitHub issue #2).

## 1.0.0 - 2021-05-16

### Added
- Initial release.
