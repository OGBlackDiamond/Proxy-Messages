package dev.ogblackdiamond.proxymessages;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.nio.file.Path;

import org.slf4j.Logger;

/**
 * Main class for ProxyMessages.
 */
@Plugin(id = "proxymessages", name = "Proxy Messages", version = "1.0.0", 
    url = "ogblackdiamond.dev", 
    description = "A message system for servers to interact over a proxy.", 
    authors = {"BlackDiamond"})
public class ProxyMessages {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    /**
     * Constructor, initializes the logger and the proxy server.
     */
    @Inject
    public ProxyMessages(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;

        logger.info("Thank you for using ProxyMessages");
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {

       // server.getEventManager().register(this, new PluginListener()); 
    }


    @Subscribe
    public void onPlayerConnect(ServerPostConnectEvent event) {

        Player player = event.getPlayer();

        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF(event.getPreviousServer() == null ? "join" : "switch");
        out.writeUTF(player.getUsername());

        
        if (event.getPreviousServer() != null) {
            out.writeUTF(event.getPreviousServer().getServerInfo().getName());
            out.writeUTF(player.getCurrentServer().get().getServerInfo().getName());
        }

        sendMessage(out.toByteArray());

    }

    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent event) {
        
        Player player = event.getPlayer();

        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF("quit");
        out.writeUTF(player.getUsername());

        sendMessage(out.toByteArray());

    }


    private void sendMessage(byte[] message) {
        for (RegisteredServer srvr : server.getAllServers()) {
            if (!srvr.getPlayersConnected().isEmpty()) {
                srvr.sendPluginMessage(MinecraftChannelIdentifier.from("proxymessages:main"), message);
            }
        }
    }
}
