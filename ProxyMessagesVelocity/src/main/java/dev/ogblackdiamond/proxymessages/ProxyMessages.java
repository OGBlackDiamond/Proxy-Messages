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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

/**
 * Main class for ProxyMessages.
 */
@Plugin(id = "proxymessages", name = "Proxy Messages", version = "1.1.0", 
    description = "A message system for servers to interact over a proxy.", 
    authors = {"BlackDiamond"})
public class ProxyMessages {

    private final ProxyServer server;
    private final Logger logger;

    @DataDirectory
    private final Path dataDirectory;


    private boolean globalJoin;
    
    private boolean globalLeave;

    private boolean globalSwitch;

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
    public void onProxyInitialization(ProxyInitializeEvent event) throws IOException {
        if (Files.notExists(dataDirectory)) {
            Files.createDirectory(dataDirectory);
        }
        final Path config = dataDirectory.resolve("config.yml");
        if (Files.notExists(config)) {
            try (InputStream stream = this.getClass().getClassLoader().getResourceAsStream("config.yml")) {
                Files.copy(stream, config);
            }
        }

        final YamlConfigurationLoader loader = YamlConfigurationLoader.builder().path(config).build();
        final CommentedConfigurationNode root = loader.load();

        globalJoin = root.node("global-network-join").getBoolean();

        globalLeave = root.node("global-network-leave").getBoolean();

        globalSwitch = root.node("global-network-switch").getBoolean();

    }

    /**
     * Sends relevant player information to backend server when player connects.
     */
    @Subscribe
    public void onPlayerConnect(ServerPostConnectEvent event) {

        if (event.getPreviousServer() != null && !globalSwitch) return;

        if (event.getPreviousServer() == null && !globalJoin) return;

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

    /**
     * Sends relevant information to backend server when player disconnects.
     */
    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent event) {

        if (!globalLeave) return;
        
        Player player = event.getPlayer();

        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF("quit");
        out.writeUTF(player.getUsername());

        sendMessage(out.toByteArray());

    }


    private void sendMessage(byte[] message) {
        for (RegisteredServer srvr : server.getAllServers()) {
            if (!srvr.getPlayersConnected().isEmpty()) {
                srvr.sendPluginMessage(
                    MinecraftChannelIdentifier.from("proxymessages:main"),
                    message
                );
            }
        }
    }
}
