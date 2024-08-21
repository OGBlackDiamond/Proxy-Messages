# Proxy-Messages
## Unified Join, Leave, and Server Switch Messages for Your Velocity/Paper Network
ProxyMessages is a powerful Velocity plugin designed to unify your network’s messaging system across all servers. With ProxyMessages, your players will receive consistent and configurable notifications whenever someone joins, leaves, or switches between servers in your network. Whether you run a small network or a large-scale operation, ProxyMessages helps you deliver a seamless player experience by synchronizing key messages across your entire server network.

## Key Features:
* Configurable Messages: Customize join, leave, and server switch messages with ease, tailoring them to fit the tone and style of your server network.
* Multiple Message Variants: Create multiple potential messages for each event, allowing for variety and keeping the experience fresh for your players.
* Custom Colors with HEX Support: Enhance your messages with custom colors using HEX codes to match your server's branding or aesthetic.
* Integration with DiscordSRV to keep your sever network connected over discord! 

### Configuration Overview:
All configurations are managed on the proxy side, meaning there’s no need to synchronize settings across individual backend servers. Simply edit the `config.yml` file located in `plugins/proxymessages/` on your proxy server to get started.

#### Note:
This plugin is only required to be on the proxy server. However, the vanilla "{player} has joined the game." messages will still appear. I reccommend using a plugin on all of your backend servers to remove this message. I created and built one for your convenience, but it has no correlation to the function of the proxy plugin. Use whatever plugin you like.

### Key Configuration Options:
* `global-network-join`: Toggle the join message for all users when a player joins the network.
* `global-network-leave`: Toggle the leave message for all users when a player leaves the network.
* `global-network-switch`: Toggle the server switch message for all users when a player moves between servers.
* `join-message-options`: Define a list of potential join messages. Use {player} as a placeholder for the player’s username.
* `leave-message-options`: Define a list of potential leave messages. Use {player} as a placeholder for the player’s username.
* `switch-message-options`: Define a list of potential switch messages. Use {player}, {prev} for the previous server, and {cur} for the current server as placeholders.

#### **COLOR**:
In all message options, you can insert a hex code inside brackets (example: `{#ff0066}`). All text after the hex code will be turned into that color. You can use this multiple times in one string for a multicolored message. Default color for all text is yellow.

### Discord:
This section discusses the discord integration functionality features and customizability.

Under the `discord` section of your `config.yml`:

`enabled`: Toggles the discord functionality.

**Note:** None of the configuration from this point forward will not matter if `enabled` is `false`

* `bot-token`: A string that represents the bot token that the plugin will attempt to connect to. This is the same as the one you might use in the DiscordSRV plugin.
* `channel-id`: The ID of the channel you want the proxy messages to be sent to.

* `text-configuration`: Options to customize the messages that get sent. Text options support markdown formatting.
  * `online-message`: The message to be sent when the proxy boots up.
  * `offline-message`: The message to be sent when the proxy shuts down.
  * `server-count`: Toggles whether a list of active servers will be printed when booting the proxy.
  * `display-icon`: Toggles whether or not to display a provided image on startup and shutdown.

**Note:** `display-icon` will not work unless you provide an image for it to display. You can do this by placing an image named `icon.jpg` in your `plugins/proxymessages` directory. When turned to `true`, the plugin will detect the image and use it automatically.



## Building
I use gradle to build this plugin. Ensure you have Java 21 or older and run `./gradle build` on Linux & MacOS, or `.\gradle.bat build` on Windows. Given that the build succeeds, the jars will spawn under their respective directories, in the `build/libs/` directory in each subproject.
