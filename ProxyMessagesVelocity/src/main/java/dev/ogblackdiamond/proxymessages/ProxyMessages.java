package dev.ogblackdiamond.proxymessages;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.player.ResourcePackInfo;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import dev.ogblackdiamond.proxymessages.util.MessageUtil;
import dev.ogblackdiamond.proxymessages.util.Metrics;
import dev.ogblackdiamond.proxymessages.util.MessageUtil.MessageReturns;
import dev.ogblackdiamond.proxymessages.config.ConfigUtil;
import dev.ogblackdiamond.proxymessages.util.DiscordUtil;
import dev.ogblackdiamond.proxymessages.commands.GlobalMessagesCommand;
import dev.ogblackdiamond.proxymessages.commands.Reload;
import dev.ogblackdiamond.proxymessages.commands.SetColor;

import java.util.HashMap;
import java.util.UUID;
import java.io.IOException;
import java.nio.file.Path;

import org.slf4j.Logger;


/**
 * Main class for ProxyMessages.
 */
@Plugin(id = "proxymessages", name = "ProxyMessages", version = "3.5.2",
    description = "A message system for servers to interact over a proxy.", 
    authors = {"BlackDiamond"})
public class ProxyMessages {

    private final ProxyServer server;
    private final Logger logger;
    private final Metrics.Factory metricsFactory;

    @DataDirectory
    private final Path dataDirectory;


    private MessageUtil messageUtil;
    private DiscordUtil discordUtil;
    private ConfigUtil configUtil;


    private HashMap<UUID, Boolean> playersGlobalChat;

    private ResourcePackInfo resourcePack;

    public static final MinecraftChannelIdentifier IDENTIFIER = MinecraftChannelIdentifier.from("proxymessages:main");

    /**
     * Constructor, initializes the logger and the proxy server.
     * @throws IOException 
     */
    @Inject
    public ProxyMessages(ProxyServer server, Logger logger, Metrics.Factory metricsFactory, @DataDirectory Path dataDirectory) throws IOException {
        this.server = server;
        this.logger = logger;
        this.metricsFactory = metricsFactory;
        this.dataDirectory = dataDirectory;

        logger.info("Thank you for using ProxyMessages");
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) throws IOException {

        server.getChannelRegistrar().register(IDENTIFIER);

        initialize();

    }

    @Subscribe void onProxyShutdown(ProxyShutdownEvent event) throws IOException {

        configUtil.saveData();

        if (configUtil.discordConfig.discordEnabled)
            discordUtil.proxyOffline();

    }

    public void initialize() throws IOException {

        // instance our main util classes 
        configUtil = new ConfigUtil(dataDirectory);
        messageUtil = new MessageUtil(configUtil);

        playersGlobalChat = new HashMap<UUID, Boolean>();

        int pluginID = 25855;
        Metrics metrics = metricsFactory.make(this, pluginID);
               
        if (configUtil.discordConfig.discordEnabled) {

            discordUtil = new DiscordUtil(
                this,
                messageUtil,
                configUtil 
            );

            String status = discordUtil.getStatus();

            if (!status.equals("good") || discordUtil.checkMessageChannel()) {
                logger.error(status);
                return;
            }
            discordUtil.proxyOnline();
        } 


        if (configUtil.resourcePackConfig.resourcePackEnabled) {

            ResourcePackInfo.Builder builder = server.createResourcePackBuilder(configUtil.resourcePackConfig.resourcePackURL);
            //builder.setHash(configUtil.resourcePackConfig.resourcePackHash.getBytes()); // TODO: fix this
            builder.setPrompt(messageUtil.compileColoredMessage(configUtil.resourcePackConfig.resourcePackPrompt).getComponent());
            builder.setShouldForce(configUtil.resourcePackConfig.resourcePackRequired);

            resourcePack = builder.build();
        }

        // initialize command stuff
        CommandManager commandManager = server.getCommandManager();
        
        CommandMeta reloadCommandMeta = commandManager.metaBuilder("pmReload")
            .aliases("reloadPM")
            .build();

        SimpleCommand reloadCommand = new Reload(
            () -> {
                try {initialize();}
                catch (IOException e) {e.printStackTrace();}
            }
        );

        CommandMeta setColorCommandMeta = commandManager.metaBuilder("set-color")
            .aliases("setColor")
            .build();

        SimpleCommand setColorCommand = new SetColor(configUtil);

        commandManager.register(reloadCommandMeta, reloadCommand);
        commandManager.register(setColorCommandMeta, setColorCommand);

        // optional global messages
        if (configUtil.generalConfig.globalMessages) {
        
            CommandMeta commandMeta = commandManager.metaBuilder("toggleGM")
                .aliases("tGM", "pmToggle")
                .plugin(this)
                .build();

            SimpleCommand globalMessagesCommand = new GlobalMessagesCommand(playersGlobalChat);

            commandManager.register(commandMeta, globalMessagesCommand);

        }

    }

    /**
     * Sends relevant player information to backend server when player connects.
     */
    @Subscribe
    public void onPlayerConnect(ServerPostConnectEvent event) {

        if (event.getPreviousServer() != null && !configUtil.generalConfig.globalNetworkSwitch) return;

        if (event.getPreviousServer() == null && !configUtil.generalConfig.globalNetworkJoin) return;

        Player player = event.getPlayer();
        
        boolean previousServerNull = event.getPreviousServer() == null;

        String message;

        if (previousServerNull) {
            message = configUtil.generalConfig.joinMessageOptions.get((int) (Math.random() * configUtil.generalConfig.joinMessageOptions.size()));
        } else {
            message = configUtil.generalConfig.switchMessageOptions.get((int) (Math.random() * configUtil.generalConfig.switchMessageOptions.size()));
        }

        sendMessage(
            messageUtil.compileFormattedMessage(
                previousServerNull ? "join" : "switch",
                player.getUsername(),
                previousServerNull ? "" : event.getPreviousServer().getServerInfo().getName(),
                player.getCurrentServer().get().getServerInfo().getName(),
                message
            ),
            event.getPlayer().getUniqueId(),
            false
        );

        if (configUtil.resourcePackConfig.resourcePackEnabled && previousServerNull && !configUtil.resourcePackConfig.resourcePackExcept.contains(event.getPlayer().getCurrentServer().get().getServerInfo().getName())) {
            event.getPlayer().sendResourcePackOffer(resourcePack);
        }

        if (previousServerNull && configUtil.generalConfig.globalMessages) playersGlobalChat.put(event.getPlayer().getUniqueId(), configUtil.generalConfig.globalMessageDefault);

    }

    /**
     * Sends relevant information to backend server when player disconnects.
     */
    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent event) {

        if (!configUtil.generalConfig.globalNetworkLeave) return;

        // checks to ensure that a player was actually connected to the server before printing a message
        if (event.getLoginStatus() != DisconnectEvent.LoginStatus.SUCCESSFUL_LOGIN) return;
        
        Player player = event.getPlayer();

        sendMessage(
            messageUtil.compileFormattedMessage(
                "leave",
                player.getUsername(),
                player.getCurrentServer().get().getServerInfo().getName(),
                "",
                configUtil.generalConfig.leaveMessageOptions.get((int) (Math.random() * configUtil.generalConfig.leaveMessageOptions.size()))
            ),
            event.getPlayer().getUniqueId(),
            false
        );

        playersGlobalChat.remove(event.getPlayer().getUniqueId());
    }

    @Subscribe
    public void onPlayerMessage(PlayerChatEvent event) {
        
        String serverName = event.getPlayer().getCurrentServer().get().getServerInfo().getName();

        MessageUtil.MessageReturns message = messageUtil.compileFormattedMessage(
            "",
            event.getPlayer().getUsername(),
            "",
            event.getPlayer().getCurrentServer().get().getServerInfo().getName(),
            (configUtil.generalConfig.globalMessages ? configUtil.generalConfig.globalMessagePrefix : configUtil.discordChatSyncConfig.discordPlayerChatSyncMessagePrefix) + event.getMessage()
        );

        if (configUtil.discordConfig.discordEnabled && configUtil.discordChatSyncConfig.discordPlayerChatSyncEnabled) discordUtil.sendMessage(message.getString(), serverName);

        // handles global messages
        if (configUtil.generalConfig.globalMessages && playersGlobalChat.get(event.getPlayer().getUniqueId())) sendMessage(message, event.getPlayer().getUniqueId(), true);

    }

    @Subscribe
    public void onPluginMessageFromBackend(PluginMessageEvent event) {

        if (!IDENTIFIER.equals(event.getIdentifier())) return;

        ByteArrayDataInput data = ByteStreams.newDataInput(event.getData());

        String playerName = data.readUTF();
        long playerLSB = data.readLong();
        long playerMSB = data.readLong();
        UUID playerUUID = new UUID(playerMSB, playerLSB);
        String messageData = data.readUTF();

        String serverNameRaw = event.getSource().toString();
        int serverNameIndex = serverNameRaw.indexOf("->");
        String serverName = serverNameRaw.substring(serverNameIndex + 3);


        MessageUtil.MessageReturns message = messageUtil.compileFormattedMessage(
            "",
            playerName,
            "",
            serverName,
            configUtil.generalConfig.globalMessagePrefix + messageData
        );

        sendMessageToServer(message, serverName);

    }


    private void sendMessage(MessageReturns message, UUID uuid, boolean exceptPlayerServer) {
        for (RegisteredServer srvr : server.getAllServers()) {
            if (srvr.getPlayersConnected().isEmpty()) continue; 
            if (exceptPlayerServer && srvr.getPlayersConnected().contains(server.getPlayer(uuid).get())) continue;

            srvr.sendMessage(message.getComponent());
        }
        if (configUtil.discordConfig.discordEnabled && !exceptPlayerServer) discordUtil.playerNotification(message, uuid);
    }

    public void sendMessage(MessageReturns message) {
        sendMessage(message, new UUID(0, 0), false);
    }

    public void sendMessageToServer(MessageReturns message, String serverName) {
        server.getServer(serverName).get().sendMessage(message.getComponent()); 
    }


    public ProxyServer getProxy() {
        return server;
    }

    public MessageUtil getMessageUtil() {
        return messageUtil;
    }

}
