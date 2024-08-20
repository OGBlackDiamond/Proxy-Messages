package dev.ogblackdiamond.proxymessages.util;

import java.util.EventListener;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

public class DiscordUtil implements EventListener { 

    private JDA jda;

    private MessageChannel messageChannel;

    private String status;
    
    public DiscordUtil(String token, String channel) {


        if (token.substring(0, 1).equals("{") || channel.substring(0, 1).equals("{")) {
            status = "Invalid channel or token provided!";
            return;
        }

        jda = JDABuilder.createDefault(token)
            .addEventListeners(this)
            .build();

        try {
            jda.awaitReady();
        } catch (InterruptedException e) {/* I dunno */}


        // a null check should be be performed in another class after construction
        messageChannel = jda.getChannelById(MessageChannel.class, channel);
    }

    public void sendMessage(String message) {
        messageChannel.sendMessage(message);
    }

    public String getStatus() {
        return status;
    }

    public boolean checkMessageChannel() {
        boolean channel = messageChannel == null;
        if (channel) status = "Channel provided could not be found!";
        return channel;
    }
} 
