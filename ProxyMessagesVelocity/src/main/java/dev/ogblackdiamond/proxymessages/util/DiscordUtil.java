package dev.ogblackdiamond.proxymessages.util;

import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.FileUpload;

import java.awt.Color;
import java.io.File; import java.util.HashMap;
import java.util.Map; import java.util.Set;
import java.util.UUID;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import com.velocitypowered.api.proxy.server.RegisteredServer;

import dev.ogblackdiamond.proxymessages.ProxyMessages;
import dev.ogblackdiamond.proxymessages.util.MessageUtil.MessageReturns;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class DiscordUtil implements EventListener { 

    private JDA jda;

    private ProxyMessages proxyMessages;

    private MessageUtil messageUtil;

    private TextChannel messageChannel;

    private String status;

    private FileUpload file;

    private boolean imageExists;

    private String botToken;

    private String proxyChannelID;

    private HashMap<String, TextChannel> serverNameIDPairs;

    CommentedConfigurationNode serverChannelPairsNode;
    CommentedConfigurationNode textConfiguration;

    CommentedConfigurationNode playerChatSync;
    
    private String onlineMessage;

    private String offlineMessage;

    private String playerMessagePrefix;

    private boolean discordRoleColor;

    private boolean serverCount;

    private boolean displayIcon;

    private Color joinColor;
    private Color leaveColor;
    private Color switchColor;


    public DiscordUtil(ProxyMessages proxyMessages, CommentedConfigurationNode configNode) throws SerializationException {

        // load all discord values from config
        
        botToken =  configNode.node("bot-token").getString();

        proxyChannelID = configNode.node("proxy-channel-id").getString();

        serverNameIDPairs = new HashMap<String, TextChannel>();

        textConfiguration = configNode.node("text-configuration");

        onlineMessage = textConfiguration.node("online-message").getString();

        offlineMessage = textConfiguration.node("offline-message").getString();

        discordRoleColor = textConfiguration.node("discord-role-color").getBoolean();

        serverCount = textConfiguration.node("server-count").getBoolean();
        
        displayIcon = textConfiguration.node("display-icon").getBoolean();

        playerChatSync = textConfiguration.node("player-chat-sync");

        playerMessagePrefix = playerChatSync.node("player-message-prefix").getString();

        serverChannelPairsNode = playerChatSync.node("server-channel-ids");

        Map<Object, CommentedConfigurationNode> serverChannels = serverChannelPairsNode.childrenMap();
        Set<Object> servers = serverChannels.keySet();


        String joinColorStr = configNode.node("join-message-options").getString();
        String leaveColorStr = configNode.node("leave-message-options").getString();
        String switchColorStr = configNode.node("switch-message-options").getString();

        File imageFile = new File("plugins/proxymessages/icon.jpg");

        if (imageFile != null && displayIcon) {
            file = FileUpload.fromData(imageFile, "icon.jpg");
            imageExists = true;
        } else {
            imageExists = false;
        }

        
        boolean validChannelIDs = true;

        for (String id : serverNameIDPairs.keySet()) {
            if (id.substring(0, 1).equals("^")) {
                validChannelIDs = false;
                break;
            }
        }

        if (botToken.substring(0, 1).equals("^") || proxyChannelID.substring(0, 1).equals("^") || !validChannelIDs) {
            status = "Invalid channel or token provided!";
            return; }


        // load and validate colors from config
        if(!HexUtil.isValidHex(joinColorStr)){
            joinColor = Color.decode("#00FF00");
        }else{
            joinColor = Color.decode(joinColorStr);
        }
        if(!HexUtil.isValidHex(leaveColorStr)){
            leaveColor = Color.decode("#FF0000");
        }else{
            leaveColor = Color.decode(leaveColorStr);
        }
        if(!HexUtil.isValidHex(switchColorStr)){
            switchColor = Color.decode("#FFFF00");
        }else{
            switchColor = Color.decode(switchColorStr);
        }


        // creates the jda wrapper
        jda = JDABuilder.createDefault(botToken)
            .addEventListeners(this)
            .enableIntents(GatewayIntent.MESSAGE_CONTENT)
            .build();
        
        try {
            jda.awaitReady();
        } catch (InterruptedException e) { }

        this.proxyMessages = proxyMessages;
        messageUtil = proxyMessages.getMessageUtil();
        
        // a null check should be be performed in another class after construction
        messageChannel = jda.getChannelById(TextChannel.class, proxyChannelID);

        for (Object object : servers) {
            serverNameIDPairs.put(
                object.toString(),
                jda.getChannelById(TextChannel.class, serverChannels.get(object).getString())
            );
        }

        status = "good";
    }


    // sends a message in the proxy message channel
    public void sendMessage(String message) {
        messageChannel.sendMessageFormat(message).complete();
    }

    // send a message in the given channel
    public void sendMessage(String message, String serverName) {
        serverNameIDPairs.get(serverName).sendMessageFormat(message).complete();

    }

    // sends a message to the discord channel when the proxy comes online
    public void proxyOnline() {
    
        String serversList = "";

        for (RegisteredServer server : proxyMessages.getProxy().getAllServers()) {
            String serverName = server.getServerInfo().getName();
            serversList += "* " + serverName + "\n";
        }

        EmbedBuilder builder = new EmbedBuilder()
            .setDescription(onlineMessage)
            .setColor(new Color(20, 200, 20));

        if (imageExists && displayIcon) builder.setImage("attachment://icon.jpg");

        if (serverCount) builder.addField("Current Servers:", serversList, false);

        MessageCreateAction msg = messageChannel.sendMessageEmbeds(builder.build());

        if (imageExists && displayIcon) msg.addFiles(file);

        msg.complete();

    }

    // sends a message to the discord channel when the proxy shuts down
    public void proxyOffline() {

        EmbedBuilder builder = new EmbedBuilder()
            .setDescription(offlineMessage)
            .setColor(new Color(200, 20, 20));

        if (imageExists && displayIcon) builder.setImage("attachment://icon.jpg");

        MessageCreateAction msg = messageChannel.sendMessageEmbeds(builder.build());

        if (imageExists && displayIcon) msg.addFiles(file);
        
        msg.complete();

    }

    // handles sending messages when player action happens on the proxy
    public void playerNotification(MessageUtil.MessageReturns message, UUID uuid) {
        Color messageColor = new Color(20, 20, 200);
        switch(message.getType()) {
            case "join": {
                messageColor = joinColor;
                break;
            }
            case "leave": {
                messageColor = leaveColor;
                break;
            }
            case "switch":{
                messageColor = switchColor;
                break;
            }
            default: {
                return;
            }
        }
        EmbedBuilder builder = new EmbedBuilder()
            .setColor(messageColor)
            .setAuthor(message.getString(), "https://github.com/OGBlackDiamond/Proxy-Messages", "https://crafthead.net/avatar/" + uuid.toString());

        messageChannel.sendMessageEmbeds(builder.build()).complete();
    }

    // returns the status of this object
    public String getStatus() {
        return status;
    }

    public String getPlayerMessagePrefix() {
        return playerMessagePrefix;
    }

    // returns if the message channel is not null
    public boolean checkMessageChannel() {
        boolean channel = messageChannel == null;
        if (channel) status = "Channel provided could not be found!";
        return channel;
    }

    @Override
    public void onEvent(GenericEvent event) {
        if (event.getClass() != MessageReceivedEvent.class) return;
        MessageReceivedEvent messageEvent = (MessageReceivedEvent) event;

        if (messageEvent.getAuthor().isBot() || messageEvent.getAuthor().isSystem()) return;

        if (!playerChatSync) return;


        String channelID = messageEvent.getChannel().getId();

        String prefix = proxyMessages.getGlobalMessages() ? proxyMessages.getGlobalMessagePrefix() : playerMessagePrefix;

        if (discordRoleColor) {
            prefix = colorPrefix(
                prefix,
                messageEvent.getMember().getColor()
            );

        }

        // compile the message
        MessageReturns message = messageUtil.compileFormattedMessage(
            "",
            messageEvent.getAuthor().getEffectiveName(),
            "",
            "Discord",
            prefix + messageEvent.getMessage().getContentRaw()
        );


        // sends the message globally if it's in the global channel
        if (proxyChannelID.equals(channelID)) {
            proxyMessages.sendMessage(message);
            return;
        }

        // I know this is stupid but I dont really care :)
        // why are you reading my code?
        int i = 0;
        for (TextChannel channel : serverNameIDPairs.values()) {
            if (channel.getId().equals(channelID)) {
                Object serverNames[] = serverNameIDPairs.keySet().toArray();
                proxyMessages.sendMessageToServer(message, serverNames[i].toString());
                break;
            }
            i++;
        }
    }

    private String colorPrefix(String prefix, Color color) {

        String newPrefix = prefix;

        int playerPlaceholderIndex = prefix.indexOf("{player}");
        if (playerPlaceholderIndex == -1) return prefix;
        int endPlayerPlaceholderIndex = prefix.indexOf("}", playerPlaceholderIndex);

        int previousColorIndex = 0;
        int previousColorIndexBuffer;
        String stringBeforePlayerPlaceholder = prefix.substring(0, playerPlaceholderIndex);

        do {
            previousColorIndexBuffer = previousColorIndex;
            previousColorIndex = stringBeforePlayerPlaceholder.indexOf("{#", previousColorIndexBuffer + 1);
        } while (previousColorIndex != -1);
        if (previousColorIndexBuffer == previousColorIndex) return prefix;

        // encapsulate the {#FFFFFF} string in the prefix 
        String previousColor = prefix.substring(previousColorIndexBuffer, previousColorIndexBuffer + 8);

        String redHex = Integer.toHexString(color.getRed());
        String greenHex = Integer.toHexString(color.getGreen());
        String blueHex = Integer.toHexString(color.getBlue());

        String newColor = "{#" + redHex + blueHex + greenHex + "}";

        newPrefix = prefix.substring(0, playerPlaceholderIndex) 
            + newColor 
            + prefix.substring(playerPlaceholderIndex, endPlayerPlaceholderIndex + 1)
            + previousColor
            + prefix.substring(endPlayerPlaceholderIndex);


        return newPrefix;
    }

} 
