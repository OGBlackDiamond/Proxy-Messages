package dev.ogblackdiamond.proxymessages.config;

import java.util.List;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;


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
