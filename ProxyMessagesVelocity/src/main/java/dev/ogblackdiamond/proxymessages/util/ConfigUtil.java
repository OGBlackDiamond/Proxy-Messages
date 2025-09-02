package dev.ogblackdiamond.proxymessages.util;

import java.util.List;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public class ConfigUtil{

    @Setting(value="global-network-join")
    @Comment(value="enables a network-wide join message for when a player joins the network")  
    public boolean globalNetworkJoin;

    @Setting(value="global-network-leave")
    @Comment(value="enables a network-wide leave message for when a player leaves the network")
    public boolean globalNetworkLeave;

    @Setting(value="global-network-switch")
    @Comment(value="enables a network-wide message that notifies players when a player switches between servers when true global-network-switch: true")
    public boolean globalNetworkSwitch;

    @Setting(value="global-messages")
    @Comment(value="enables a network-wide messaging system that allows players to communicate between servers on the proxy")
    public boolean globalMessages;

    @Setting(value="default-player-color")
    @Comment(value="for all message options, you can insert a hex code (example: {#ff0066}). Characters after this hex code will turn the color of the code. Multiple hex codes can be used to make a string multicolored.\nyou can also insert tags like {bold} and {italic} to style your messages. Please refer to the documentation for this plugin for more information.\n\nthe default color to be used for a player's name in messages when they have not set one")
    public String defaultPlayerColor;

    @Setting(value="join-message-options")
    @Comment(value="options from this list will be randomly chosen to be displayed when a player joins the network\n{player} will be replaced by the player name")
    public List<String> joinMessageOptions; 

    @Setting(value="leave-message-options")
    @Comment(value="options from this list will be randomly chosen to be displayed when a player joins the network\n{player} will be replaced by the player name")
    public List<String> leaveMessageOptions;

    @Setting(value="switch-message-options")
    @Comment(value="options from this list will be randomly chosen to be displayed when a player changes between two servers on the network \n{player} will be replaced by the player name\n{prev} will be replaced by the server the player is connecting from\n{cur} will be replaced by the server the player is connecting to")
    public List<String> switchMessageOptions;

    @Setting(value="global-message-prefix")
    @Comment(value="the prefix to global messages on other servers\n{cur} will be replaced by the current server\n{player} will be replaced by the player's name")
    public String globalMessagePrefix;

    @Setting(value="global-message-defualt")
    @Comment(value="whether player's messages will be global by default")
    public boolean globalMessageDefault;


}
