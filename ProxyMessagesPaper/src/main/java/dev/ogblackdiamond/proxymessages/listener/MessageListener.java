package dev.ogblackdiamond.proxymessages.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import dev.ogblackdiamond.proxymessages.ProxyMessages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.audience.Audience;


public class MessageListener implements PluginMessageListener {


    public MessageListener(ProxyMessages proxyMessages) {
        proxyMessages.getServer().getMessenger().registerIncomingPluginChannel(proxyMessages, "proxymessages:main", this);
    }
    

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("proxymessages:main")) {
            return;
        }

        ByteArrayDataInput in = ByteStreams.newDataInput(message);

        String type = in.readUTF();
        String playerString = in.readUTF();

        Component text;

        switch (type) {
            case "join": {
                text = Component.text()
                    .content(playerString).color(NamedTextColor.YELLOW).decoration(TextDecoration.BOLD, true)
                    .append(Component.text(" joined the network").decoration(TextDecoration.BOLD, false))
                    .build();   
                break;
            }

            case "switch": {

                String previousServer = in.readUTF();
                String currentServer = in.readUTF();

                text = Component.text()
                    .content(playerString).color(NamedTextColor.YELLOW).decoration(TextDecoration.BOLD, true)
                    .append(Component.text(" left ").decoration(TextDecoration.BOLD, false))
                    .append(Component.text(previousServer).decoration(TextDecoration.BOLD, true))
                    .append(Component.text(" and joined ").decoration(TextDecoration.BOLD, false))
                    .append(Component.text(currentServer).decoration(TextDecoration.BOLD, true))
                    .build();
                break;
            }

            case "quit": {
                text = Component.text()
                    .content(playerString).color(NamedTextColor.YELLOW).decoration(TextDecoration.BOLD, true)
                    .append(Component.text(" left the network").decoration(TextDecoration.BOLD, false))
                    .build();
                break;
            }

            default: {
                Bukkit.getLogger().warning("INVALID TYPE PASSED");
                return;
            }

        }

        Audience audience = Bukkit.getServer();
        
        audience.sendMessage(text);

    }

}
