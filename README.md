# Proxy-Messages
## Unified System and Player Messages for Your Velocity/Paper Network
ProxyMessages is a powerful Velocity plugin designed to unify your network’s messaging system across all servers. ProxyMessages can be configured to send messages to players across all servers for player chat messages, proxy connections, disconnections, and server changes. Whether you run a small network or a large-scale operation, ProxyMessages helps you deliver a seamless player experience by synchronizing key messages across your entire server network.

Join [my Discord!](https://discord.gg/C2PJNTN97u)

![Usage Examples](https://github.com/user-attachments/assets/fe5e1ed0-9c39-49ef-b246-d282ac1a4d0f)

## Key Features:
* Global messages so players can communicate with one another, even if they aren't connected to the same server.
* Configurable Messages: Customize join, leave, and server switch messages with ease, tailoring them to fit the tone and style of your server network.
* Multiple Message Variants: Create multiple potential messages for each event, allowing for variety and keeping the experience fresh for your players.
* Custom Colors with HEX Support: Enhance your messages with custom colors using HEX codes to match your server's branding or aesthetic.
* Abitility to instate network-wide resource packs.
* Integration with DiscordSRV to keep your sever network connected over discord! 

### Configuration Overview:
All configurations are managed on the proxy side, meaning there’s no need to synchronize settings across individual backend servers. Simply edit the `config.yml` file located in `plugins/proxymessages/` on your proxy server to get started.

### The Paper Plugin:
ProxyMessages can work standalone on a velocity server. However, due to limitations in the velocity API, it cannot edit or change the vanilla messages that normally appear on servers. These include the vanilla join and leave messages, as well as vanilla chat messages on the server that the player is connected to. If you'd like remove the vanilla join and leave messages, and format chat messages, please use the provided paper plugin on all of your backend servers. Once the paper plugin is installed, the aforementioned features will automatically start working. No extra configuration is needed.

#### **COLOR**:
In all message options, you can insert a hex code inside brackets (example: `{#ff0066}`). All text after the hex code will be turned into that color. You can use this multiple times in one string for a multicolored message. Default color for all text is yellow.

#### **STYLING**
In all message options, you can use Kyori Adventure TextDecoration options to further style your join options. Options include:
* `{bold}` - text after this tag is bold
* `{italic}` - text after this tag is italic 
* `{strike}` - text after this tag has strikethroughs
* `{underline}` - text after this tag is underlined
* `{obfuscate}` - text after this tag is obfuscated

Two sets of these tags will "open" and "close" their syle. For example, you can stylize a chunk of text by sandwitching it between the corresponding tags.


### Key Configuration Options:
* `global-network-join`: Toggle the join message for all users when a player joins the network.
* `global-network-leave`: Toggle the leave message for all users when a player leaves the network.
* `global-network-switch`: Toggle the server switch message for all users when a player moves between servers.
* `global-messages`: Toggle the server's ability to send player's messages across all servers.
* `join-message-options`: Define a list of potential join messages. Use {player} as a placeholder for the player’s username.
* `leave-message-options`: Define a list of potential leave messages. Use {player} as a placeholder for the player’s username.
* `switch-message-options`: Define a list of potential switch messages. Use {player}, {prev} for the previous server, and {cur} for the current server as placeholders.
* `global-message-prefix`: The prefix to global messages for other servers. Use {player} for the player sending the message, and {cur} as the server they're sending the message from.

### Config Reloading
The configuration for this plugin can reload on the fly!
Simply make changes to your config file and use either `pmReload` or `reloadPM`, and the config changes will take effect!

### Global Player Messaging:
Once `global-messages` is enabled, players can toggle their messages between "global" and "local" by using any of the following commands (aliases used for convenience):
* `toggleGM`
* `tGM`
* `pmToggle`

#### `global-message-defaults` is whether or not player's messages will be global by default (they can still be toggled if true)

**Global messages** mean that the user's messages will be displayed in the server they are connected to, as well as all other servers on the network.

**Local messages** mean that the user's messages will only be displayed in the server they are connected to.

### Resource Packs:
This section discusses the ability to instate a network-wide resource pack

Under the `network-resource-pack` section of your `config.yml`:

`enabled`: toggles the resource pack on and off

**Note:** None of the configuration from this point forward will not matter if `enabled` is `false`

* `url`: The download url of your resource pack
* `sha1-hash`: The hash of your resource pack 
**NOTE**: At this time, a hash will do nothing, as I don't know how to properly translate it to a byte array. If you know how, please open a PR or Issue ticket on the github page for this project. 
* `prompt`: The prompt users will recieve, inquiring abotu whether they want the resource pack
* `required`: Whether or not the resource pack is required to play on the network
* `except`: A list of servers to ignore when sending resource packs to players.


### Discord:
This section discusses the discord integration functionality features and customizability.

Under the `discord` section of your `config.yml`:

`enabled`: Toggles the discord functionality.

**Note:** None of the configuration from this point forward will not matter if `enabled` is `false`

* `bot-token`: A string that represents the bot token that the plugin will attempt to connect to. This is the same as the one you might use in the DiscordSRV plugin.
* `proxy-channel-id`: The ID of the channel you want the proxy's messages to be sent to.
* `server-channel-ids`: A list of server names and a corresponding discord channel ID. Messages will be linked between each server and its corresponding channel.
* `join-color`: Hex color code to be used for the Discord embed for join messages.
* `leave-color`: Hex color code to be used for the Discord embed for leave messages.
* `switch-color`: Hex color code to be used for the Discord embed for switch messages.
* `text-configuration`: Options to customize the messages that get sent. Text options support markdown formatting.
  * `online-message`: The message to be sent when the proxy boots up.
  * `offline-message`: The message to be sent when the proxy shuts down.
  * `player-message-prefix`: The prefix to a player's message going to discord - if global messages are enabled, it will use the global messaging prefix instead
  * `discord-role-color`: When true, any messages from discord will color the name of the sender with the color of their role in discord.
  * `server-count`: Toggles whether a list of active servers will be printed when booting the proxy.
  * `display-icon`: Toggles whether or not to display a provided image on startup and shutdown.

**Note:** `display-icon` will not work unless you provide an image for it to display. You can do this by placing an image named `icon.jpg` in your `plugins/proxymessages` directory. When turned to `true`, the plugin will detect the image and use it automatically.



## Building
I use gradle to build this plugin. Ensure you have Java 21 or older and run `./gradle build` on Linux & MacOS, or `.\gradle.bat build` on Windows. Given that the build succeeds, the jars will spawn under their respective directories, in the `build/libs/` directory in each subproject.
