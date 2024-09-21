package dev.ogblackdiamond.proxymessages;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.player.ResourcePackInfo;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import dev.ogblackdiamond.proxymessages.util.MessageUtil;
import dev.ogblackdiamond.proxymessages.util.DiscordUtil;

import net.kyori.adventure.text.Component;

import jakarta.xml.bind.DatatypeConverter;
import java.util.List;
import java.util.UUID;
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
@Plugin(id = "proxymessages", name = "ProxyMessages", version = "2.4.1",
    description = "A message system for servers to interact over a proxy.", 
    authors = {"BlackDiamond"})
public class ProxyMessages {

    private final ProxyServer server;
    private final Logger logger;

    @DataDirectory
    private final Path dataDirectory;

    private MessageUtil messageUtil;
    private DiscordUtil discordUtil;

    private boolean discordEnabled;

    private boolean resourcePackEnabled;
    
    private boolean resourcePackRequired;

    private String resourcePackUrl;

    private byte[] resourcePackHash;

    private Component resourcePackPrompt;

    private ResourcePackInfo resourcePack;

    private List<String> resourcePackExcept;

    private boolean globalJoin;
    
    private boolean globalLeave;

    private boolean globalSwitch;

    private List<String> joinMessageOptions;
    
    private List<String> leaveMessageOptions;

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

        CommentedConfigurationNode discordOptions = root.node("discord");

        discordEnabled = discordOptions.node("enabled").getBoolean();
            
        if (discordEnabled) {

            discordUtil = new DiscordUtil(
                this,
                discordOptions
            );

            String status = discordUtil.getStatus();

            if (!status.equals("good") || discordUtil.checkMessageChannel()) {
                logger.error(status);
                return;
            }
        
            discordUtil.proxyOnline();
        } 

        CommentedConfigurationNode resourcePackOptions = root.node("network-resource-pack");

        resourcePackEnabled = resourcePackOptions.node("enabled").getBoolean();

        if (resourcePackEnabled) {

            resourcePackUrl = resourcePackOptions.node("url").getString();
            resourcePackHash = DatatypeConverter.parseHexBinary(resourcePackOptions.node("sha1-hash").getString());
            resourcePackRequired = resourcePackOptions.node("required").getBoolean();
            resourcePackPrompt = messageUtil.compileColoredMessage(resourcePackOptions.node("prompt").getString()).getComponent();
            resourcePackExcept = resourcePackOptions.node("except").getList(String.class);

            ResourcePackInfo.Builder builder = server.createResourcePackBuilder(resourcePackUrl);
            builder.setHash(resourcePackHash);
            builder.setPrompt(resourcePackPrompt);
            builder.setShouldForce(resourcePackRequired);

            resourcePack = builder.build();
        }

    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (discordUtil != null)
            discordUtil.proxyOffline();
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
            message = joinMessageOptions.get((int) (Math.random() * joinMessageOptions.size()));
        } else {
            message = switchMessageOptions.get((int) (Math.random() * switchMessageOptions.size()));
        }

        sendMessage(
            messageUtil.compileFormattedMessage(
                previousServerNull ? "join" : "switch",
                player.getUsername(),
                previousServerNull ? "" : event.getPreviousServer().getServerInfo().getName(),
                player.getCurrentServer().get().getServerInfo().getName(),
                message
            ),
            event.getPlayer().getUniqueId()
        );
 
        if (resourcePackEnabled && !resourcePackExcept.contains(event.getPlayer().getCurrentServer().get().getServerInfo().getName())) {
            event.getPlayer().sendResourcePackOffer(resourcePack);
        }

    }

    /**
     * Sends relevant information to backend server when player disconnects.
     */
    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent event) {

        if (!globalLeave) return;

        // checks to ensure that a player was actually connected to the server before printing a message
        if (event.getLoginStatus() != DisconnectEvent.LoginStatus.SUCCESSFUL_LOGIN) return;
        
        Player player = event.getPlayer();

        sendMessage(
            messageUtil.compileFormattedMessage(
                "leave",
                player.getUsername(),
                player.getCurrentServer().get().getServerInfo().getName(),
                "",
                leaveMessageOptions.get((int) (Math.random() * leaveMessageOptions.size()))
            ),
            event.getPlayer().getUniqueId()
        );
    }

    public ProxyServer getProxy() {
        return server;
    }

    private void sendMessage(MessageUtil.MessageReturns message, UUID uuid) {
        for (RegisteredServer srvr : server.getAllServers()) {
            if (!srvr.getPlayersConnected().isEmpty()) {
                srvr.sendMessage(message.getComponent());
            }
        }
        if (discordUtil != null) discordUtil.playerNotification(message, uuid);
    }
}
