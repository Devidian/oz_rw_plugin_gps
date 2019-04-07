# GPS
A Rising World Java plug-in to help in finding own way while moving across a world.
Originally written by Miwarre -> [to origin](https://github.com/mgavioli/RisingWorld-GPS)

## Features
- Displays current player heading as a standard navigation route, position in 'standard' geographic coordinates (N E) and block altitude (note that internal RW positive W longitude is converted into a more common positive E longitude).
- Optionally, in `settings.properties`, the display can be set to Rising World own proprietary format.
- Define a Home position and display radial, side (to the left or to the right of the player) and distance to it.
- Define up to 15 way points and display radial, side and distance to any of them in turn.
- Working with the GPS is done via a GUI, which allows to turn the GPS on/off, to set the Home and each way-point, to show/hide them. The GUI adapts itself to the current GPS status.
- Teleport to home and to any defined waypoint. Teleporting to waypoints can be disabled for servers which do not like players popping up everywhere. In fact, it is disabled by default; edit `allowTpToWp=false` within `setting.properties` into `allowTpToWp=true` to enable (remember to reload the plug-ins or restart the server to enable the change).
- It can receive temporary way points ("targets") from other plug-ins; targets are removed once reached and are not persistent. An example of a plug-in sending targets to GPS can be found [here](https://github.com/mgavioli/sampleGPSclient).
- The plugin uses the player system language to translate all messages (since 1.5.0) you can add your system language to the i18n folder if it is not available yet and translate it by yourself. (Send your translation to Devidian to integrate it in the next update!)
- The plug-in stores player-specific data into a separate data base for each world; so, even, in a local single player context, home and waypoints defined for one world do not affect other worlds.
____________________

#  **Important - Important - Important - Important**:

**In order to work the plug-in requires the GUI back-end plug-in 0.5.0 (or later) available [here](https://github.com/mgavioli/rwgui). Install it at the same time of (or before) installing this plug-in or the server will crash at start-up!**

**Since version `1.5.0` this plugin also needs the tools lib from Devidian available [here](https://github.com/Devidian/oz_rw_plugin_tools/releases)**
____________________

## Commands

|Command|Description|
|-|-|
|/gps|Open control panel (GUI)|
|/gps help|Show available commands|
|/gps info|Show Plugin description & Credits|
|/gps status|Show plugin version, language etc.|
|/gps spawn|Teleport to your spawn (bed) location|
|/gps serverspawn|Teleport to server-default spawn location (new player spawn)|
|/hsethome|shortcut to set your home to your current location|
|/home|teleport to your home location|

## Installation

Extract the files in the ZIP placing the whole GPS folder into the plugins folder of RW (if the plugins folder does not exist, just create one). The resulting hierarchy should look like this (including rwgui/tools):


```css
    ── RisingWorld
        ├── plugins
        │    ├── GPS
        │    │    ├── assets
        │    │    ├── i18n
        │    │    ├── COPYING
        │    │    ├── HISTORY.en.md
        │    │    ├── HISTORY.md
        │    │    ├── OZ-GPS-Plugin.jar
        │    │    ├── README.en.md
        │    │    ├── README.md
        │    │    ├── *.db
        │    │    └── settings.properties
        │    ├── rwgui
        │    │    ├── assets
        │    │    └── rwgui.jar
        │    ├── shared
        │    │    └── lib
        │    │         ├── HISTORY.en.md
        │    │         └── tools.jar
        :    :
```
