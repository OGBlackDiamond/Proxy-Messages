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

import dev.ogblackdiamond.proxymessages.util.MessageUtil;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.Random;
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
@Plugin(id = "proxymessages", name = "ProxyMessages", version = "2.1.1", 
    description = "A message system for servers to interact over a proxy.", 
    authors = {"BlackDiamond"})
public class ProxyMessages {

    private final ProxyServer server;
    private final Logger logger;

    @DataDirectory
    private final Path dataDirectory;

    private MessageUtil messageUtil;

    private boolean globalJoin;
    
    private boolean globalLeave;

    private boolean globalSwitch;

    private List<String> joinMessageOptions;
    
    private List <String> leaveMessageOptions;

    private List<String> switchMessageOptions;

    /**
     * Constructor, initializes the logger and the proxy server.
     */
    @Inject
    public ProxyMessages(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;

        messageUtil = new MessageUtil();

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
        
        joinMessageOptions = root.node("join-message-options").getList(String.class);
        leaveMessageOptions = root.node("leave-message-options").getList(String.class);
        switchMessageOptions = root.node("switch-message-options").getList(String.class);
    }

    /**
     * Sends relevant player information to backend server when player connects.
     */
    @Subscribe
    public void onPlayerConnect(ServerPostConnectEvent event) {

        if (event.getPreviousServer() != null && !globalSwitch) return;

        if (event.getPreviousServer() == null && !globalJoin) return;

        Player player = event.getPlayer();
        
        boolean previousServerNull = event.getPreviousServer() == null;

        String message;

        if (event.getPreviousServer() == null) {
            message = joinMessageOptions.get((int) Math.random() * joinMessageOptions.size());
        } else {
            message = switchMessageOptions.get((int) Math.random() * switchMessageOptions.size());
        }

        sendMessage(
            messageUtil.compileFormattedMessage(
                event.getPreviousServer() == null ? "join" : "switch",
                player.getUsername(),
                previousServerNull ? "" : event.getPreviousServer().getServerInfo().getName(),
                previousServerNull ? "" : player.getCurrentServer().get().getServerInfo().getName(),
                message
            )
        );
    }

    /**
     * Sends relevant information to backend server when player disconnects.
     */
    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent event) {

        if (!globalLeave) return;
        
        Player player = event.getPlayer();

        sendMessage(
            messageUtil.compileFormattedMessage(
                "leave",
                player.getUsername(),
                "",
                "",
                leaveMessageOptions.get((int) Math.random() * switchMessageOptions.size())
            )
        );

    }


    private void sendMessage(Component message) {
        for (RegisteredServer srvr : server.getAllServers()) {
            if (!srvr.getPlayersConnected().isEmpty()) {
                srvr.sendMessage(message);
            }
        }
    }
}
