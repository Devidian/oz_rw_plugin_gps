## [Unreleased]

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