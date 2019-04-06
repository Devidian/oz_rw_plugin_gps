## [Unreleased]

## [1.5.1] - 2019-04-06
### Added
- `/gps help` now shows commands for teleport and set home
### Changed
- README.en.md updated, added command table
- `wpMaxIndex` can now be changed in `settings.properties` (maximum number of waypoints)

## [1.5.0] - 2019-04-06
### Changed
- original plugin converted to maven, merged with oz-boilerplate
- updated German translation
- class Msgs removed, now using i18n class from tools
- class Db removed, now using Wrapper class SQLite from tools.db
### Fixed
- UTF-8 bug from version 1.4.0 