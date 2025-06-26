package dev.ogblackdiamond.proxymessages.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;

import java.util.HashMap;
import java.util.UUID;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;


public final class GlobalMessagesCommand implements SimpleCommand {

    private HashMap<UUID, Boolean> playerGlobalChat;

    public GlobalMessagesCommand(HashMap<UUID, Boolean> playerGlobalChat) {
        this.playerGlobalChat = playerGlobalChat;
    }

    @Override
    public void execute(final Invocation invocation) {
        CommandSource source = invocation.source();

        if (source instanceof Player player) {
            final UUID playerUUID = player.getUniqueId();

            playerGlobalChat.replace(playerUUID, !playerGlobalChat.get(playerUUID));
            boolean globalMessage = playerGlobalChat.get(playerUUID);

            source.sendMessage(Component.text(
                "Your messages are now " + (globalMessage ? "global" : "local"),
                NamedTextColor.DARK_AQUA
            ));
        }
    }
}
