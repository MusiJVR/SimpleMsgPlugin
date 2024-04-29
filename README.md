# SimpleMsgPlugin
**SimpleMsgPlugin** adds the ability to send private messages to other players. When receiving a message, the player plays the sound of gaining experience `minecraft:entity.player.levelup` (default sound).

All technical settings and features presented here are for the latest version of the plugin and will not always work on older versions.

## Commands
Here are all the commands that can be used in the plugin:
* `/pm <nick> <message>` - This command allows you to send private messages to the player
* `/csound <sound>` - This command allows you to change the notification sound
* `/addbl <player>` - This command allows you to add players to the blacklist
* `/removebl <player>` - This command allows you to remove players from the blacklist
* `/showbl` - This command allows you to display your blacklist

## Config
When the server starts, the config file will be automatically created in this path: `plugins/SimpleMsgPlugin/config.yml`

```yml
# Plugin version 1.1.1

sendmsgyourself: false
# This item is needed to be able to send a message to yourself
# Accepts only values 'true' and 'false'

messages:
  error: '§cError'
  notmsgyouself: '§eYou cannot send message yourself'
  playermissing: '§cThis player is not on the server'
  msgpattern: '§b§l[§r§e%sender%§r §b§l->§r §e%recipient%§r§b§l]§r §e%message%§r'
  # After the '§' sign the color code is indicated
  # All codes you can find here - https://minecraft.fandom.com/wiki/Formatting_codes
  # Variables are indicated between '%' signs (I don't recommend changing them)

  msgsound: 'ENTITY_PLAYER_LEVELUP'
  # The default notification sound is here
  # But before the name of the sound you should always write 'ENTITY'
  # All sounds you can find here - https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Sound.html
  # Unfortunately not all of these sounds can be reproduced correctly
  # Therefore, if the sound doesn't work and gives an error, then you should change the sound
  soundmissing: '§cInvalid sound value'
  soundsuccess: '§aThe sound has been successfully changed'

  blmissing: '§cInvalid player value'
  blyourself: '§cYou cannot add yourself to the blacklist'
  blsuccessblock: '§aThe player was successfully blocked'
  blalreadyblock: '§cThis player is already blocked'
  blnotblock: '§cThis player is not on the blacklist'
  blsuccessunblock: '§aThe player has been successfully unblocked'

  youinbl: '§cYou cannot send messages to this player'
  youbl: '§eYou have blocked this player'

  emptybl: '§eYour blacklist is empty'
  playersbl: '§bPlayers from your blacklist: §r§e'
```

## Issues
Please leave messages about any errors you find [here](https://github.com/MusiJVR/SimpleMsgPlugin/issues) or on the [Discord](https://discord.gg/xY8WJt7VGr)

## Social Media

- Page on [Modrinth](https://modrinth.com/plugin/simplemsgplugin)
- Page on [GitHub](https://github.com/MusiJVR/SimpleMsgPlugin)
- Page on [Discord](https://discord.gg/xY8WJt7VGr)