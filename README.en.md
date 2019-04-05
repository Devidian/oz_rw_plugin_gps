# GPS
A Rising World Java plug-in to help in finding own way while moving across a world.
Originally written by Miwarre -> [to origin](https://github.com/mgavioli/RisingWorld-GPS)

## Features
- Displays current player heading as a standard navigation route, position in 'standard' geographic coordinates (N E) and block altitude (note that internal RW positive W longitude is converted into a more common positive E longitude).
- Optionally, in settings.properties, the display can be set to Rising World own proprietary format.
- Define a Home position and display radial, side (to the left or to the right of the player) and distance to it.
- Define up to 15 way points and display radial, side and distance to any of them in turn.
- Working with the GPS is done via a GUI, which allows to turn the GPS on/off, to set the Home and each way-point, to show/hide them. The GUI adapts itself to the current GPS status.
- Teleport to home and to any defined waypoint. Teleporting to waypoints can be disabled for servers which do not like players popping up everywhere. In fact, it is disabled by default; edit allowTpToWp=0 within setting.properties into allowTpToWp=1 to enable (remember to reload the plug-ins or restart the server to enable the change).
- It can receive temporary way points ("targets") from other plug-ins; targets are removed once reached and are not persistent. An example of a plug-in sending targets to GPS can be found [here](https://github.com/mgavioli/sampleGPSclient).
- The plug-in user interface defaults to English, but it can be translated in any language (whose characters are supported by Rising World) through external files by the users themselves. Details in the readme.txt file.
- The plug-in stores player-specific data into a separate data base for each world; so, even, in a local single player context, home and waypoints defined for one world do not affect other worlds.
____________________

**Important - Important - Important - Important**:

**In order to work the plug-in requires the GUI back-end plug-in 0.5.0 (or later) available [here](https://github.com/mgavioli/rwgui). Install it at the same time of (or before) installing this plug-in or the server will crash at start-up!**
____________________

## Commands

There is only one chat command: `/gps` (configurable in the plug-in settings.properties) which shows the GPS control panel.

## Installation

Extract the files in the ZIP placing the whole gps folder into the plugins folder of RW (if the plugins folder does not exist, just create one). The resulting hierarchy shall be:

    ── RisingWorld
        ├── plugins
        │    ├── gps
        │    │    ├── assets
        │    │    ├── locale
        │    │    ├── gps.jar
        │    │    ├── readme.txt
        │    │    └── settings.properties