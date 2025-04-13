package dev.ogblackdiamond.proxymessages.util;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;

import dev.ogblackdiamond.proxymessages.ProxyMessages;

import java.util.concurrent.CompletableFuture;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;


public final class GlobalMessagesCommand implements SimpleCommand {


    private ProxyMessages proxyMessages;
    private HashMap<UUID, Boolean> playerGlobalChat;

    public GlobalMessagesCommand(ProxyMessages proxyMessages, HashMap<UUID, Boolean> playerGlobalChat) {
        this.proxyMessages = proxyMessages; 
        this.playerGlobalChat = playerGlobalChat;
    }

    @Override
    public void execute(final Invocation invocation) {
        CommandSource source = invocation.source();

        source.sendMessage(Component.text(""));

        if (source instanceof Player player) {
            final String playerName = player.getUsername();
            final UUID playerUUID = player.getUniqueId();

            proxyMessages.togglePlayerGlobalChat(playerUUID);
            playerGlobalChat.replace(playerUUID, !playerGlobalChat.get(playerUUID));
            boolean globalMessage = playerGlobalChat.get(playerUUID);

            source.sendMessage(Component.text(
                "Your messages are now " + (globalMessage ? "global" : "local"),
                NamedTextColor.DARK_AQUA
            ));
        }
    }
}
