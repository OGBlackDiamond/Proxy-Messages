package dev.ogblackdiamond.proxymessages.config;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

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


