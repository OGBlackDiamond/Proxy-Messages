package dev.ogblackdiamond.proxymessages.util;

import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.FileUpload;

import java.awt.Color;
import java.io.File;
import java.util.UUID;
import java.util.regex.*;

import org.spongepowered.configurate.CommentedConfigurationNode;

import com.velocitypowered.api.proxy.server.RegisteredServer;

import dev.ogblackdiamond.proxymessages.ProxyMessages;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.GenericEvent;

public class DiscordUtil implements EventListener { 

    private JDA jda;

    ProxyMessages proxyMessages;

    private TextChannel messageChannel;

    private String status;

    private FileUpload file;

    private boolean imageExists;

    String botToken;

    String channelID;

    CommentedConfigurationNode textConfiguration;
    
    String onlineMessage;

    String offlineMessage;

    boolean serverCount;

    boolean displayIcon;

    Color joinColor;
    Color leaveColor;
    Color switchColor;


    public DiscordUtil(ProxyMessages proxyMessages, CommentedConfigurationNode configNode) {

        // load all discord values from config

        botToken =  configNode.node("bot-token").getString();

        channelID = configNode.node("channel-id").getString();

        textConfiguration = configNode.node("text-configuration");

        onlineMessage = textConfiguration.node("online-message").getString();

        offlineMessage = textConfiguration.node("offline-message").getString();

        serverCount = textConfiguration.node("server-count").getBoolean();
        
        displayIcon = textConfiguration.node("display-icon").getBoolean();

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

        if (botToken.substring(0, 1).equals("{") || channelID.substring(0, 1).equals("{")) {
            status = "Invalid channel or token provided!";
            return;
        }


        // load and validate colors from config
        if(!isValidHex(joinColorStr)){
            joinColor = Color.decode("#00FF00");
        }else{
            joinColor = Color.decode(joinColorStr);
        }
        if(!isValidHex(leaveColorStr)){
            leaveColor = Color.decode("#FF0000");
        }else{
            leaveColor = Color.decode(leaveColorStr);
        }
        if(!isValidHex(switchColorStr)){
            switchColor = Color.decode("#FFFF00");
        }else{
            switchColor = Color.decode(switchColorStr);
        }


        // creates the jda wrapper
        jda = JDABuilder.createDefault(botToken)
            .addEventListeners(this)
            .build();
        
        try {
            jda.awaitReady();
        } catch (InterruptedException e) { }

        this.proxyMessages = proxyMessages;
        

        // a null check should be be performed in another class after construction
        messageChannel = jda.getChannelById(TextChannel.class, channelID);
        status = "good";
    }

    // sends a message in the given message channel
    public void sendMessage(String message) {
        messageChannel.sendMessageFormat(message).complete();

    }

    // sends a message to the discord channel when the proxy comes online
    public void proxyOnline() {
    
        String serversList = "";

        for (RegisteredServer server : proxyMessages.getProxy().getAllServers()) {
            serversList += "* " + server.getServerInfo().getName() + "\n";
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
        switch(message.getType()){
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
            default: {}
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

    // returns if the message channel is not null
    public boolean checkMessageChannel() {
        boolean channel = messageChannel == null;
        if (channel) status = "Channel provided could not be found!";
        return channel;
    }

    @Override
    public void onEvent(GenericEvent event) {
        // something here in the future?
    }

    // checks to see if a string is a valid hex code
    public boolean isValidHex(String str)
    {
        boolean ret = false;

        if(str != null) {
            String hexRegex = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$";
            Pattern hexPattern = Pattern.compile(hexRegex);
            Matcher hexMatcher = hexPattern.matcher(str);
            ret = hexMatcher.matches();
        }
        return ret;
    }
} 
