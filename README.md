<div align="center">

![Logo](docs/simplemsgplugin.png)

[![GitHub Release](https://img.shields.io/github/v/release/MusiJVR/SimpleMsgPlugin?style=for-the-badge)](https://github.com/MusiJVR/SimpleMsgPlugin/releases)
[![Modrinth Downloads](https://img.shields.io/modrinth/dt/kspBne7T?style=for-the-badge&label=Modrinth)](https://modrinth.com/plugin/simplemsgplugin)
[![GitHub License](https://img.shields.io/github/license/MusiJVR/SimpleMsgPlugin?style=for-the-badge)](https://github.com/MusiJVR/SimpleMsgPlugin/blob/main/LICENSE.md)

[![Bukkit](https://badges.penpow.dev/badges/supported/bukkit/compact-minimal.svg)](https://bukkit.org/)
[![Spigot](https://badges.penpow.dev/badges/supported/spigot/compact-minimal.svg)](https://spigotmc.org/)
[![Paper](https://badges.penpow.dev/badges/supported/paper/compact-minimal.svg)](https://papermc.io/software/paper)
[![Folia](https://badges.penpow.dev/badges/supported/folia/compact-minimal.svg)](https://papermc.io/software/folia)
[![Purpur](https://badges.penpow.dev/badges/supported/purpur/compact-minimal.svg)](https://purpurmc.org/)

</div>

## 🌐 Overview

**SimpleMsgPlugin** adds the ability to send private messages to other players, set notification sounds, send offline messages, manage blacklists, and create groups.

All technical settings and features presented here are for the latest version of the plugin and will not always work on older versions.

## 📜 Commands

Here are all the commands that can be used in the plugin:
* `/helpmsg` - This command allows you to display all possible plugin commands
* `/reloadmsg` - This command allows you to reload the plugin config
* `/propmsg <property> <args>` - This command allows you to set player properties
* `/msg <player> <message>` - This command allows you to send private messages to the player
* `/reply <message>` - This command allows you to send a private message to the last player who wrote to you
* `/mail` - This command allows you to view unread messages
* `/acceptsend` - This command allows you to accept sending an offline message
* `/notify <sound> <volume>` - This command allows you to change the notification sound
* `/privatechat <action> <arg>(if required)` - This command allows you to interact with private chats
* `/blacklist <action> <args>` - This command allows you to manage the blacklist

## 🎵 Change default sound

By default, the sound is turned off 'false', but you can change it. 
It is necessary to indicate the name of the sound in uppercase and with underscores. 
Example - `default_sound: 'entity.player.levelup'`

All sounds you can find [here](https://minecraft.fandom.com/wiki/Sounds.json/Java_Edition_values).
Unfortunately not all of these sounds can be reproduced correctly.
Therefore, if the sound doesn't work and gives an error, then you should change the sound.

## 🔊 Change default volume

By default, the volume of notification sounds is set to `default_volume: 50`, but it can be changed.
The volume value must be specified in the range from `0` to `100`.

## 🎨 Change message style

All messages use the [MiniMessage](https://docs.advntr.dev/minimessage/index) format. [Here](https://webui.advntr.dev/) you can create a message template.

Example: `<color:#ffff55>Message</color>` - <span style="color:#ffff55">Message</span>

## ⚙️ Settings for developers or administrators

In the config you can configure sending messages to yourself `send_msg_yourself: false`.
By default, the value is `false`, but can be changed to `true`

The plugin has permissions:

| **Permissions**                   | **Meaning**                                |
|-----------------------------------|--------------------------------------------|
| `simplemsgplugin.helpmsg`         | Permission to use command `helpmsg`        |
| `simplemsgplugin.reloadmsg`       | Permission to use command `reloadmsg`      |
| `simplemsgplugin.propertiesmsg`   | Permission to use command `propmsg`        |
| `simplemsgplugin.playermsg`       | Permission to use command `msg`            |
| `simplemsgplugin.replymsg`        | Permission to use command `reply`          |
| `simplemsgplugin.acceptsend`      | Permission to use command `acceptsend`     |
| `simplemsgplugin.mailmsg`         | Permission to use command `mail`           |
| `simplemsgplugin.notificationmsg` | Permission to use command `notify`         |
| `simplemsgplugin.privatechat`     | Permission to use command `privatechat`    |
| `simplemsgplugin.blacklist`       | Permission to use command `blacklist`      |
| `simplemsgplugin.update_listener` | Permission to receive update notifications |

## ❗ Issues

Please leave messages about any errors you find [here](https://github.com/MusiJVR/SimpleMsgPlugin/issues) or on the [Discord](https://discord.gg/xY8WJt7VGr)

## 💬 Social Media

- Page on [Modrinth](https://modrinth.com/plugin/simplemsgplugin)
- Page on [GitHub](https://github.com/MusiJVR/SimpleMsgPlugin)
- Page on [Discord](https://discord.gg/xY8WJt7VGr)
