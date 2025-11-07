package dev.ogblackdiamond.proxymessages.config;

import java.util.Map;
import java.util.Set;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

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


