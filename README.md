# SimpleMsgPlugin
**SimpleMsgPlugin** adds the ability to send private messages to other players. The default sound value is `false`, but it can be changed to another existing sound, such as `ENTITY_PLAYER_LEVELUP`

All technical settings and features presented here are for the latest version of the plugin and will not always work on older versions.

## Commands
Here are all the commands that can be used in the plugin:
* `/msghelp` - This command allows you to display all possible plugin commands
* `/msgrc` - This command allows you to reload the plugin config
* `/msg <player> <message>` - This command allows you to send private messages to the player
* `/reply <message>` - This command allows you to send a private message to the last player who wrote to you
* `/acceptsend` - This command allows you to accept sending an offline message
* `/csound <sound>` - This command allows you to change the notification sound
* `/cvolume <volume>` - This command allows you to change the notification volume
* `/addbl <player>` - This command allows you to add players to the blacklist
* `/removebl <player>` - This command allows you to remove players from the blacklist
* `/showbl` - This command allows you to display your blacklist

## Change default sound
By default, the sound is turned off 'false', but you can change it. 
It is necessary to indicate the name of the sound in uppercase and with underscores. 
Example - `msgsound: 'ENTITY_PLAYER_LEVELUP'`

All sounds you can find [here](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Sound.html).
Unfortunately not all of these sounds can be reproduced correctly.
Therefore, if the sound doesn't work and gives an error, then you should change the sound.

## Change default volume
By default, the volume of notification sounds is set to `50`, but it can be changed.
The volume value must be specified in the range from `0` to `100`.

## Change message color
In order to change the color of the message, you need to specify the `&` icon, followed by the color code in HEX format with `#` at the beginning.
You can also change the color using standard Minecraft colors by specifying `§` at the beginning.
The `§r` icon is used to reset the color and the `§l` icon is used to make the font bold.
All codes you can find [here](https://minecraft.fandom.com/wiki/Formatting_codes).
Variables are indicated between `%` signs (I don't recommend changing them).

Example: `&#55ff55Message` - <span style="color:green">Message</span>

## Settings for developers or administrators
In the config you can configure sending messages to yourself `sendmsgyourself: false`.
By default, the value is `false`, but can be changed to `true`

The plugin has permissions:

| **Permissions**                      | **Meaning**                            |
|--------------------------------------|----------------------------------------|
| `simplemsgplugin.msghelp`            | Permission to use command `msghelp`    |
| `simplemsgplugin.msgreloadconfig`    | Permission to use command `msgrc`      |
| `simplemsgplugin.playermsg`          | Permission to use command `msg`        |
| `simplemsgplugin.replymsg`           | Permission to use command `reply`      |
| `simplemsgplugin.acceptsend`         | Permission to use command `acceptsend` |
| `simplemsgplugin.changesound`        | Permission to use command `csound`     |
| `simplemsgplugin.changevolume`       | Permission to use command `cvolume`    |
| `simplemsgplugin.addblacklist`       | Permission to use command `addbl`      |
| `simplemsgplugin.removeblacklist`    | Permission to use command `removebl`   |
| `simplemsgplugin.showblacklist`      | Permission to use command `showbl`     |

## Issues
Please leave messages about any errors you find [here](https://github.com/MusiJVR/SimpleMsgPlugin/issues) or on the [Discord](https://discord.gg/xY8WJt7VGr)

## Social Media

- Page on [Modrinth](https://modrinth.com/plugin/simplemsgplugin)
- Page on [GitHub](https://github.com/MusiJVR/SimpleMsgPlugin)
- Page on [Discord](https://discord.gg/xY8WJt7VGr)