# Proxy-Messages
A minecraft plugin for server to interact over a proxy.

Currently, the only support is for Velocity and Paper.

## About
This plugin will remove boring "Player has joined the game" messages from in game. Instead, everyone on every server in the network will be notified when someone joins the network, when they switch between two servers, and when they disconnect from the network.

## Usage
Place each jar in their respective plugin folders, and run your server(s) and proxy! Its as easy as that. There are currently no configuration options at this time.

## Building
I use gradle to build this plugin. Ensure you have Java 21 or older and run `./gradle build` on Linux & MacOS, or `.\gradle.bat build` on Windows. Given that the build succeeds, the jars will spawn under their respective directories, in the `build/libs/` directory in each subproject.
