## [Unreleased]

## [1.6.1] - 2019-04-09
### Fixed
- Slot 15 (der letzte) wird nun korrekt geladen

## [1.6.0] - 2019-04-07
### Fixed
- PluginChangeWatcher wurde nicht ausgeführt
### Added
- neues Kommando: `/gps spawn` teleportiert zum Bett (Spieler-Spawn)
- neues Kommando: `/gps serverspawn` teleportiert zum Srver-Startpunkt
- neue Kommandos können in der `settings.properties` aktiviert werden
- Russische Übersetzung von `Galochka`

## [1.5.1] - 2019-04-06
### Added
- In der Hilfe stehen nun auch die Befehle zum setzen und teleportieren zum Startpunkt
### Changed
- README.en.md aktualisiert, Tabelle mit Befehlen hinzugefügt
- `wpMaxIndex` kann nun in `settings.properties` eingestellt werden (Anzahl der Waypoints pro Spieler)

## [1.5.0] - 2019-04-06
### Changed
- Original Plugin nach Maven konvertiert und mit OZ Boilerplate zusammengelegt
- deutsche Übersetzung angepasst
- Klasse Msgs entfernt, verwendet nun i18n aus tools
- Klasse Db entfernt, verwendet nun Wrapper Klasse SQLite aus tools.db
### Fixed
- UTF-8 Bug aus Version 1.4.0 behoben