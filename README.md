# TabFaces mod for Minecraft Forge 1.7.10

![logo](images/logo_small.png)

Display player faces in the server tab menu, in the chat, and the server population info tooltip.

[![hub](images/badges/github.png)](https://github.com/JackOfNoneTrades/TabFaces/releases)
[![modrinth](images/badges/modrinth.png)](https://modrinth.com/mod/tabfaces)
[![curse](images/badges/curse.png)](https://www.curseforge.com/minecraft/mc-mods/tabfaces)
[![67](images/badges/67.png)](https://67.fentanylsolutions.org/mod/tabfaces)
[![maven](images/badges/maven.png)](https://maven.fentanylsolutions.org/#/releases/org/fentanylsolutions/tabfaces/TabFaces)
![forge](images/badges/forge.png)
[![cord](images/badges/cord.png)](https://discord.gg/xAWCqGrguG)

![screeenshot1](images/screenshot1.png)
![screeenshot3](images/screenshot3.png)
![screeenshot4](images/screenshot4.png)

## Dependencies

* As of version 1.0.2, [UniMixins](https://modrinth.com/mod/unimixins) [![curse](images/icons/curse.png)](https://www.curseforge.com/minecraft/mc-mods/unimixins) [![modrinth](images/icons/modrinth.png)](https://modrinth.com/mod/unimixins/versions) [![git](images/icons/git.png)](https://github.com/LegacyModdingMC/UniMixins/releases) is a required dependency.
* Prior to version 1.0.10, [CarbonConfig](https://modrinth.com/mod/carbon-config) [![curse](images/icons/curse.png)](https://www.curseforge.com/minecraft/mc-mods/carbon-config) [![modrinth](images/icons/modrinth.png)](https://modrinth.com/mod/carbon-config) [![git](images/icons/git.png)](https://github.com/Carbon-Config-Project/CarbonConfig) is a required dependency.
* As of version 1.0.9, [SkinPort](https://www.curseforge.com/minecraft/mc-mods/skinport) [![curse](images/icons/curse.png)](https://www.curseforge.com/minecraft/mc-mods/skinport) [![git](images/icons/git.png)](https://github.com/zlainsama/SkinPort) is an optional dependency and has added compat.
* As of version 1.0.16, [FentLib](https://www.curseforge.com/minecraft/mc-mods/fentlib) [![curse](images/icons/curse.png)](https://www.curseforge.com/minecraft/mc-mods/fentlib) [![modrinth](images/icons/modrinth.png)](https://modrinth.com/mod/fentlib) [![67](images/icons/67.png)](https://67.fentanylsolutions.org/mod/fentlib) [![git](images/icons/git.png)](https://github.com/JackOfNoneTrades/FentLib) is an optional dependency and provides modern + HD skin compat.

## Incompatibilities
* Better Ping Display is not compatible.

## FAQ
### What does this mod do?

* Player faces in the tab menu backport.
* Player faces in the server selection menu (`1.0.2`).
* Player faces in the chat (`1.0.5`).
* Player faces in TabbyChat [![tc_git](images/icons/modrinth.png)](https://modrinth.com/mod/tabbychat-unofficial) [![tc_git](images/icons/git.png)](https://github.com/mist475/tabbychat/releases) (`1.0.7`). Since `1.0.8` TabbyChat specific configs can be changed from the TabbyChat GUI as well.

### Client? Server? Which side?

* As of version `1.0.2`, client only, will do nothing on the server.
* As of version `1.0.8`, server is required for GT:NH ServerUtilities [![curse](images/icons/curse.png)](https://www.curseforge.com/minecraft/mc-mods/serverutilities-gtnh) [![git](images/icons/git.png)](https://github.com/GTNewHorizons/ServerUtilities) compat.

### Will this work with [insert auth mod]?

It should work on all online servers. Tested on a clean server and one treated with [authlib-injector](https://github.com/yushijinhun/authlib-injector). Has compatibility with Wawel Auth [![curse](images/icons/curse.png)](https://www.curseforge.com/minecraft/mc-mods/wawel-auth) [![modrinth](images/icons/modrinth.png)](https://modrinth.com/mod/wawel-auth) [![67](images/icons/67.png)](https://67.fentanylsolutions.org/mod/wawel-auth) [![git](images/icons/git.png)](https://github.com/JackOfNoneTrades/WawelAuth) as well.

### Misc

If you use the debug config to add a team to all members (to test width handling), remove that team with the `tabfaces_dbg_cleanup` command. This command is registered only if both, server and client have the debug config set to `true`.

### Building

`./gradlew build`.

## License

`LgplV3 + SNEED`.

## Buy me a coffee

* [ko-fi.com](https://ko-fi.com/jackisasubtlejoke)
* Monero: `893tQ56jWt7czBsqAGPq8J5BDnYVCg2tvKpvwTcMY1LS79iDabopdxoUzNLEZtRTH4ewAcKLJ4DM4V41fvrJGHgeKArxwmJ`

<br>

![license](images/lgplsneed_small.png)
