## [Unreleased]

## [1.6.2] - 2019-04-10
### Fixed
- Slot 15 (last slot in general) can now be deleted

## [1.6.1] - 2019-04-09
## Fixed
- Slot 15 (last slot in general) is now loaded correctly

## [1.6.0] - 2019-04-07
### Fixed
- PluginChangeWatcher was not executed
### Added
- new command: `/gps spawn` teleport to your bed (player-spawn)
- new command: `/gps serverspawn` teleport to server-spawn (default spawn)
- new commands can be activated in `settings.properties`
- Russian translation by `Galochka`

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