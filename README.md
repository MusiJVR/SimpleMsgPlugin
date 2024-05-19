# SimpleMsgPlugin
**SimpleMsgPlugin** adds the ability to send private messages to other players. The default sound value is `false`, but it can be changed to another existing sound, such as `ENTITY_PLAYER_LEVELUP`

All technical settings and features presented here are for the latest version of the plugin and will not always work on older versions.

## Commands
Here are all the commands that can be used in the plugin:
* `/msghelp` - This command allows you to display all possible plugin commands
* `/msgrc` - This command allows you to reload the plugin config
* `/pm <nick> <message>` - This command allows you to send private messages to the player
* `/acceptsend` - This command allows you to accept sending an offline message
* `/csound <sound>` - This command allows you to change the notification sound
* `/cvolume <volume>` - This command allows you to change the notification volume
* `/addbl <player>` - This command allows you to add players to the blacklist
* `/removebl <player>` - This command allows you to remove players from the blacklist
* `/showbl` - This command allows you to display your blacklist

## Config
When the server starts, the config file will be automatically created in this path: `plugins/SimpleMsgPlugin/config.yml`

```yml
# Plugin version 1.4.0

sendmsgyourself: false
# This item is needed to be able to send a message to yourself
# Accepts only values 'true' and 'false'

msgsound: 'false'
# By default, the sound is turned off 'false', but you can change it
# It is necessary to indicate the name of the sound in uppercase and with underscores
# Example - msgsound: 'ENTITY_PLAYER_LEVELUP'
# All sounds you can find here - https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Sound.html
# Unfortunately not all of these sounds can be reproduced correctly
# Therefore, if the sound doesn't work and gives an error, then you should change the sound

volumesound: 50
# By default, the volume of notification sounds is set to '50', but it can be changed
# The volume value must be specified in the range from '0' to '100'

messages:
  error: '&#ff5555Error'

  configreloadsuccessfully: '&#55ff55The plugin config was successfully reloaded'

  notmsgyouself: '&#ffff55You cannot send message yourself'
  playermissing: '&#ff5555This player is not on the server'
  msgsendoffline: '&#ffff55Send this player an offline message?'

  acceptsend: '§eAccept§r §b§l[✔]§r'
  # I don't recommend setting the color of this message using HEX encoding, as the color will be displayed incorrectly

  clickmsgsendoffline: 'Click to send offline message'
  msgsendofflinesuccessfully: "&#55ff55You have successfully sent an offline message"

  msgsenderpattern: '&#55ffff§l[§r&#ffff55%sender%§r &#55ffff§l->§r &#ffff55%receiver%§r&#55ffff§l]§r &#ffff55%message%§r'
  msgreceiverpattern: '&#55ffff§l[§r&#ffff55%sender%§r &#55ffff§l->§r &#ffff55%receiver%§r&#55ffff§l]§r &#ffff55%message%§r'
  msgofflinepattern: '&#55ffff§l[§r&#ffff55%sender%§r&#55ffff§l]§r &#55ffff§l->§r &#ffff55%messages%§r'
  # In order to change the color of the message, you need to specify the '&' icon, followed by the color code in HEX format with '#' at the beginning
  # You can also change the color using standard Minecraft colors by specifying '§' at the beginning
  # The '§r' icon is used to reset the color and the '§l' icon is used to make the font bold
  # All codes you can find here - https://minecraft.fandom.com/wiki/Formatting_codes
  # Variables are indicated between '%' signs (I don't recommend changing them)

  soundmissing: '&#ff5555Invalid sound value'
  soundsuccess: '&#55ff55The sound has been successfully changed'

  volumemissing: '&#ff5555Invalid volume value'
  volumesuccess: '&#55ff55The volume has been successfully changed'

  blmissing: '&#ff5555Invalid player value'
  blyourself: '&#ff5555You cannot add yourself to the blacklist'
  blsuccessblock: '&#55ff55The player was successfully blocked'
  blalreadyblock: '&#ff5555This player is already blocked'
  blnotblock: '&#ff5555This player is not on the blacklist'
  blsuccessunblock: '&#55ff55The player has been successfully unblocked'

  youinbl: '&#ff5555You cannot send messages to this player'
  youbl: '&#ffff55You have blocked this player'

  emptybl: '&#ffff55Your blacklist is empty'
  playersbl: '&#55ffffPlayers from your blacklist: §r&#ffff55'

  haveunreadmsg: "&#55ffffYou have unread messages:"

helpmessages:
  consolehelplist: '[SimpleMsg] ⏴-------SimpleMsgPlugin-------⏵
                    
                    [SimpleMsg] Command: msghelp
                    
                    [SimpleMsg] Description: This command allows you to display all possible plugin commands
                    
                    [SimpleMsg] Usage: /<command>
                    
                    [SimpleMsg]
                    
                    [SimpleMsg] Command: msgrc
                    
                    [SimpleMsg] Description: This command allows you to reload the plugin config
                    
                    [SimpleMsg] Usage: /<command>
                    
                    [SimpleMsg]
                    
                    [SimpleMsg] Command: msg
                    
                    [SimpleMsg] Description: This command allows you to send private messages to the player
                    
                    [SimpleMsg] Usage: /<command> <player> <message>
                    
                    [SimpleMsg]
                    
                    [SimpleMsg] Command: acceptsend
                    
                    [SimpleMsg] Description: This command allows you to accept sending an offline message
                    
                    [SimpleMsg] Usage: /<command>
                    
                    [SimpleMsg]
                    
                    [SimpleMsg] Command: csound
                    
                    [SimpleMsg] Description: This command allows you to change the notification sound
                    
                    [SimpleMsg] Usage: /<command> <sound>
                    
                    [SimpleMsg]
                    
                    [SimpleMsg] Command: cvolume
                    
                    [SimpleMsg] Description: This command allows you to change the notification volume
                    
                    [SimpleMsg] Usage: /<command> <volume>
                    
                    [SimpleMsg]
                    
                    [SimpleMsg] Command: addbl
                    
                    [SimpleMsg] Description: This command allows you to add players to the blacklist
                    
                    [SimpleMsg] Usage: /<command> <player>
                    
                    [SimpleMsg]
                    
                    [SimpleMsg] Command: removebl
                    
                    [SimpleMsg] Description: This command allows you to remove players from the blacklist
                    
                    [SimpleMsg] Usage: /<command> <player>
                    
                    [SimpleMsg]
                    
                    [SimpleMsg] Command: showbl
                    
                    [SimpleMsg] Description: This command allows you to display your blacklist
                    
                    [SimpleMsg] Usage: /<command>
                    
                    [SimpleMsg] ⏴----------------------------⏵'
  playerhelplist: '§b§l⏴-------§r§e§lSimpleMsgPlugin§r§b§l-------⏵§r
  
                    §bCommand:§r §emsghelp§r
                    
                    §bDescription:§r §eThis command allows you to display all possible plugin commands§r
                    
                    §bUsage:§r §e/<command>§r
                    
                    
                    §bCommand:§r §emsgrc§r
                    
                    §bDescription:§r §eThis command allows you to reload the plugin config§r
                    
                    §bUsage:§r §e/<command>§r
                    
                    
                    §bCommand:§r §emsg§r
                    
                    §bDescription:§r §eThis command allows you to send private messages to the player§r
                    
                    §bUsage:§r §e/<command> <player> <message>§r
                    
                    
                    §bCommand:§r §eacceptsend§r
                    
                    §bDescription:§r §eThis command allows you to accept sending an offline message§r
                    
                    §bUsage:§r §e/<command>§r
                    
                    
                    §bCommand:§r §ecsound§r
                    
                    §bDescription:§r §eThis command allows you to change the notification sound§r
                    
                    §bUsage:§r §e/<command> <sound>§r
                    
                    
                    §bCommand:§r §ecvolume§r
                    
                    §bDescription:§r §eThis command allows you to change the notification volume§r
                    
                    §bUsage:§r §e/<command> <volume>§r
                    
                    
                    §bCommand:§r §eaddbl§r
                    
                    §bDescription:§r §eThis command allows you to add players to the blacklist§r
                    
                    §bUsage:§r §e/<command> <player>§r
                    
                    
                    §bCommand:§r §eremovebl§r
                    
                    §bDescription:§r §eThis command allows you to remove players from the blacklist§r
                    
                    §bUsage:§r §e/<command> <player>§r
                    
                    
                    §bCommand:§r §eshowbl§r
                    
                    §bDescription:§r §eThis command allows you to display your blacklist§r
                    
                    §bUsage:§r §e/<command>§r
                    
                    §b§l⏴----------------------------⏵§r'
```

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

|            **Permissions**             |              **Meaning**               |
|----------------------------------------|----------------------------------------|
| `simplemsgplugin.msghelp`              | Permission to use command `msghelp`    |
| `simplemsgplugin.msgreloadconfig`      | Permission to use command `msgrc`      |
| `simplemsgplugin.playermsg`            | Permission to use command `msg`        |
| `simplemsgplugin.acceptsend`           | Permission to use command `acceptsend` |
| `simplemsgplugin.changesound`          | Permission to use command `csound`     |
| `simplemsgplugin.changevolume`         | Permission to use command `cvolume`    |
| `simplemsgplugin.addblacklist`         | Permission to use command `addbl`      |
| `simplemsgplugin.removeblacklist`      | Permission to use command `removebl`   |
| `simplemsgplugin.showblacklist`        | Permission to use command `showbl`     |

## Issues
Please leave messages about any errors you find [here](https://github.com/MusiJVR/SimpleMsgPlugin/issues) or on the [Discord](https://discord.gg/xY8WJt7VGr)

## Social Media

- Page on [Modrinth](https://modrinth.com/plugin/simplemsgplugin)
- Page on [GitHub](https://github.com/MusiJVR/SimpleMsgPlugin)
- Page on [Discord](https://discord.gg/xY8WJt7VGr)