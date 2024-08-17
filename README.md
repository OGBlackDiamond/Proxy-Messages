# Proxy-Messages
This plugin will send messages to every player on every server in the network to notify when someone joins the network, when they switch between two servers, and when they disconnect from the network.

## Usage
This plugin is only required to be on the proxy server. However, the vanilla "{player} has joined the game." messages will still appear. I reccommend using a plugin on all of your backend servers to remove this message. I created and built one for your convenience, but it has no correlation to the function of the proxy plugin. Use whatever plugin you like.

### Configuration
All configuration is proxy side, no need to copy configurations between backend servers.
In `plugins/proxymessages/config.yml` of your proxy server:
* global-network-join: enables and disables the join message that all users on the network recieve when a player joins the network.
* global-network-leave: enables and disables the leave message that all users on the network recieve when a player leaves the network.
* global-network-switch: enables and disables the switch message that all users on the network recieve when a player switches between servers on the network.

#### **COLOR**:
In all message options, you can insert a hex code inside brackets (example: `{#ff0066}`). All text after the hex code will be turned into that color. You can use this multiple times in one string for a multicolored message. Default color for all text is yellow.

* join-message-options: a list of messages that will be randomly chosen to be displayed when a player joins the network. Any instance of `{player}` in the string will be replaced with the relevant username.
* leave-message-options: a list of messages that will be randomly chosen to be displayed when a player leaves the network. Any instance of `{player}` in the string will be replaced with the relevant username.
* switch-message-options: a list of messages that will be randomly chosen to be displayed when a player switches between servers on the network. Any instance of `{player}` will be replaced with the relevant username. Any instance of `{prev}` will be replaced by the server that the player is connecting from. Any instance of `{cur}` will be replaced by the server that the player is connecting to.

## Building
I use gradle to build this plugin. Ensure you have Java 21 or older and run `./gradle build` on Linux & MacOS, or `.\gradle.bat build` on Windows. Given that the build succeeds, the jars will spawn under their respective directories, in the `build/libs/` directory in each subproject.
