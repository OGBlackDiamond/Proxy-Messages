package dev.ogblackdiamond.proxymessages.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Processor;
import org.spongepowered.configurate.objectmapping.meta.Setting;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

public class ConfigUtil {

    public GeneralConfig generalConfig;
    public ResourcePackConfig resourcePackConfig;
    public DiscordConfig discordConfig;
    public DiscordTextConfig discordTextConfig;
    public DiscordChatSyncConfig discordChatSyncConfig;

    public List<String> colorMap;

    private Path database;

    public ConfigUtil(Path dataDirectory, CommentedConfigurationNode rootNode, YamlConfigurationLoader loader) throws IOException {

        generalConfig = rootNode
            .get(GeneralConfig.class);
        resourcePackConfig = rootNode.node("network-resource-pack")
            .get(ResourcePackConfig.class);
        CommentedConfigurationNode discordNode = rootNode.node("discord");
        discordConfig = discordNode
            .get(DiscordConfig.class);
        CommentedConfigurationNode discordTextConfigNode = discordNode.node("text-configuration");
        discordTextConfig = discordTextConfigNode
            .get(DiscordTextConfig.class);
        CommentedConfigurationNode discordChatSyncNode = discordTextConfigNode.node("player-chat-sync");
        discordChatSyncConfig = discordChatSyncNode 
            .get(DiscordChatSyncConfig.class);

        loader.save(rootNode);

        //if (newFile) 
        colorMap.add(0, "default " + generalConfig.defaultPlayerColor);

    }

    public void saveData() throws IOException {

        OutputStream out = Files.newOutputStream(database);

        for (String string : colorMap) {
            out.write((string + "\n").getBytes());
        }

        out.close();

    }


    public List<String> getColorMap() {
        return colorMap;
    }

    public String getColor(String playerName) {
        for (String color : colorMap) {
            if (color.substring(0, color.indexOf(" ")).equals(playerName)){
                return color.substring(color.indexOf(" ") + 1);
            } 
        }
        return getColor("default");
    }

    public void appendColorMap(String colorPair) {
        for (int i = 0; i < colorMap.size(); i++) {
            if (colorMap.get(i).substring(0, colorMap.get(i).indexOf(" "))
                .equals(colorPair.substring(0, colorPair.indexOf(" ")))) {
                colorMap.remove(i);
            }
        }
        colorMap.add(colorPair);
    }


////////////////////////////////////////////
/////////////// CLASSES ///////////////////
///////////////////////////////////////////
    @ConfigSerializable
    public class GeneralConfig {


        @Setting(value="global-network-join")
        @Comment(value="enables a network-wide join message for when a player joins the network")  
        public boolean globalNetworkJoin = true;

        @Setting(value="global-network-leave")
        @Comment(value="enables a network-wide leave message for when a player leaves the network")
        public boolean globalNetworkLeave = true;

        @Setting(value="global-network-switch")
        @Comment(value="enables a network-wide message that notifies players when a player switches between servers when true global-network-switch: true")
        public boolean globalNetworkSwitch = true;

        @Setting(value="global-messages")
        @Comment(value="enables a network-wide messaging system that allows players to communicate between servers on the proxy")
        public boolean globalMessages = true;

        @Setting(value="default-player-color")
        @Comment(value="for all message options, you can insert a hex code (example: {#ff0066}). Characters after this hex code will turn the color of the code. Multiple hex codes can be used to make a string multicolored.\nyou can also insert tags like {bold} and {italic} to style your messages. Please refer to the documentation for this plugin for more information.\n\nthe default color to be used for a player's name in messages when they have not set one")
        public String defaultPlayerColor = "#fcfcfc";

        @Setting(value="join-message-options")
        @Comment(value="options from this list will be randomly chosen to be displayed when a player joins the network\n{player} will be replaced by the player name")
        public List<String> joinMessageOptions = List.of("{player} joined the network"); 

        @Setting(value="leave-message-options")
        @Comment(value="options from this list will be randomly chosen to be displayed when a player joins the network\n{player} will be replaced by the player name")
        public List<String> leaveMessageOptions = List.of("{player} left the network");

        @Setting(value="switch-message-options")
        @Comment(value="options from this list will be randomly chosen to be displayed when a player changes between two servers on the network \n{player} will be replaced by the player name\n{prev} will be replaced by the server the player is connecting from\n{cur} will be replaced by the server the player is connecting to")
        public List<String> switchMessageOptions = List.of("{player} left {prev} and joined {cur}");

        @Setting(value="global-message-prefix")
        @Comment(value="the prefix to global messages on other servers\n{cur} will be replaced by the current server\n{player} will be replaced by the player's name")
        public String globalMessagePrefix = "[{cur}] - {player}: ";

        @Setting(value="global-message-defualt")
        @Comment(value="whether player's messages will be global by default")
        public boolean globalMessageDefault = true;

        @Setting(value="network-resource-pack")
        @Comment(value="options for a network-wide resource pack")
        private ConfigurationNode networkResourcePackNode;

        @Setting(value="discord")
        @Comment(value="options for discord integration")
        private ConfigurationNode discordNode;
    }

    @ConfigSerializable
    public class ResourcePackConfig {

        @Setting(value="enabled")
        @Comment(value="whether or not to use a resource pack")
        public boolean resourcePackEnabled = false;

        @Setting(value="url")
        @Comment(value="the url of the resource pack")
        public String resourcePackURL = "pack.zip";

        @Setting(value="sha1-hash")
        @Comment(value="the hash of the zip file\nthe hash currently does nothing, as I don't know how to properly parse this into a byte array\n\tif you know, please open a PR or issue ticket on my github. Thanks.")
        public String resourcePackHash = "hash";
        
        @Setting(value="prompt")
        @Comment(value="the prompt that users will recieve when connecting")
        public String resourcePackPrompt = "{#ff2200}This proxy reccomends the use of a datapack for a better player experience.";

        @Setting(value="required")
        @Comment(value="whether or not the resource pack is required to play on the network")
        public boolean resourcePackRequired = false;

        @Setting(value="except")
        @Comment(value="a list of servers that will not have a resource pack applied to it\nI recomend this for modded servers because resource packs can cause issues there")
        public List<String> resourcePackExcept = List.of("modded");

    }

    @ConfigSerializable
    public class DiscordConfig {
        @Setting(value="enabled")
        @Comment(value="enables discord features")
        public boolean discordEnabled;

        @Setting(value="bot-token")
        @Comment(value="your bot token")
        public String discordBotToken = "^token";

        @Setting(value="proxy-channel-id")
        @Comment(value="the main channel where general proxy activity will be reported")
        public String discordProxyChannelID = "^channel-id";

        @Setting(value="join-color")
        @Comment(value="color of the discord embed for join messages")
        public String discordJoinColor = "#00ff00";

        @Setting(value="leave-color")
        @Comment(value="color of the discord embed for leave messages")
        public String discordLeaveColor = "#ff0000";

        @Setting(value="switch-color")
        @Comment(value="color fo the discord embed for switch messages")
        public String discordSwitchColor = "#ffff00";

        @Setting(value="text-configuration")
        @Comment(value="all text options support markdown formatting\nthese messages DO NOT support hex codes like above")
        private ConfigurationNode discordTextConfigurationNode;
    }

    @ConfigSerializable
    public class DiscordTextConfig {

        @Setting(value="online-message")
        @Comment(value="the message to be displayed when the proxy comes online")
        public String discordOnlineMessage = "## Proxy Online";

        @Setting(value="offline-message")
        @Comment(value="the message to be displayed when the proxy goes offline")
        public String discordOfflineMessage = "## Proxy Offline";

        @Setting(value="server-count")
        @Comment(value="on proxy startup, lists available servers")
        public boolean discordServerCount = true;

        @Setting(value="display-icon")
        @Comment(value="on startup and shutdown, include an image in the embed")
        public boolean discordDisplayIcon = false;

        @Setting(value="discord-role-color")
        @Comment(value="color player's names in game with their discord role color")
        public boolean discordRoleColor = true;

        @Setting(value="player-chat-sync")
        @Comment(value="whether or not to sync player chat with discord")
        private ConfigurationNode discordPlayerChatSyncNode;

    }

    @ConfigSerializable
    public class DiscordChatSyncConfig {

        DiscordChatSyncConfig() {
            discordServerIDs = discordPlayerChatSyncChannelIDs.keySet();
        }

        @Setting(value="enabled")
        @Comment(value="enable discord chat sync")
        public boolean discordPlayerChatSyncEnabled = false;

        @Setting(value="player-message-prefix")
        @Comment(value="a prefix to player messages in discord - these can be formatted like above\nthis only applies when global messaging is disabled")
        public String discordPlayerChatSyncMessagePrefix = "[{cur}] - <{player}>: ";

        @Setting(value="server-channel-ids")
        @Comment(value="the servers (as listed in your velocity.toml) and their corresponding channel id\neach server will report its activity to it's specified channel")
        public Map<String, String> discordPlayerChatSyncChannelIDs =
            Map.of("^lobby", "lobby-channel-id",
                    "^vanilla", "vanilla-channel-id");

        // load in the real values, we unfortunately can't do this automatically
        public Set<String> discordServerIDs ;
    }

}
