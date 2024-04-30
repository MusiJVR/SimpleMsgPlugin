# SimpleMsgPlugin
**SimpleMsgPlugin** adds the ability to send private messages to other players. The default sound value is `false`, but it can be changed to another existing sound, such as `ENTITY_PLAYER_LEVELUP`

All technical settings and features presented here are for the latest version of the plugin and will not always work on older versions.

## Commands
Here are all the commands that can be used in the plugin:
* `/msgrc` - This command allows you to reload the plugin config
* `/pm <nick> <message>` - This command allows you to send private messages to the player
* `/csound <sound>` - This command allows you to change the notification sound
* `/addbl <player>` - This command allows you to add players to the blacklist
* `/removebl <player>` - This command allows you to remove players from the blacklist
* `/showbl` - This command allows you to display your blacklist

## Config
When the server starts, the config file will be automatically created in this path: `plugins/SimpleMsgPlugin/config.yml`

```yml
# Plugin version 1.2.0

sendmsgyourself: false
# This item is needed to be able to send a message to yourself
# Accepts only values 'true' and 'false'

messages:
  error: '&#ff5555Error'

  configreloadsuccessfully: '&#55ff55The plugin config was successfully reloaded'
  configreloaderror: '&#ff5555Plugin config could not be reloaded'

  notmsgyouself: '&#ffff55You cannot send message yourself'
  playermissing: '&#ff5555This player is not on the server'
  msgpattern: '&#55ffff§l[§r&#ffff55%sender%§r &#55ffff§l->§r &#ffff55%recipient%§r&#55ffff§l]§r &#ffff55%message%§r'
  # After the '§' sign the color code is indicated
  # All codes you can find here - https://minecraft.fandom.com/wiki/Formatting_codes
  # Variables are indicated between '%' signs (I don't recommend changing them)

  msgsound: 'false'
  # By default the sound is turned off 'false', but you can change it
  # It is necessary to indicate the name of the sound in uppercase and with underscores
  # Example - msgsound: 'ENTITY_PLAYER_LEVELUP'
  # All sounds you can find here - https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Sound.html
  # Unfortunately not all of these sounds can be reproduced correctly
  # Therefore, if the sound doesn't work and gives an error, then you should change the sound
  soundmissing: '&#ff5555Invalid sound value'
  soundsuccess: '&#55ff55The sound has been successfully changed'

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
```

## Change message color
In order to change the color of the message, you need to specify the `&` icon, followed by the color code in HEX format with `#` at the beginning.
The `§r` icon is used to reset the color and the `§l` icon is used to make the font bold.

Example: `&#55ff55Message` - <span style="color:green">Message</span>

## Issues
Please leave messages about any errors you find [here](https://github.com/MusiJVR/SimpleMsgPlugin/issues) or on the [Discord](https://discord.gg/xY8WJt7VGr)

## Social Media

- Page on [Modrinth](https://modrinth.com/plugin/simplemsgplugin)
- Page on [GitHub](https://github.com/MusiJVR/SimpleMsgPlugin)
- Page on [Discord](https://discord.gg/xY8WJt7VGr)