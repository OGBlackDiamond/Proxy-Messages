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
import dev.ogblackdiamond.proxymessages.util.DiscordUtil;
import dev.ogblackdiamond.proxymessages.commands.GlobalMessagesCommand;
import dev.ogblackdiamond.proxymessages.commands.Reload;
import dev.ogblackdiamond.proxymessages.commands.SetColor;
import net.kyori.adventure.text.Component;
import jakarta.xml.bind.DatatypeConverter;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
/**
 * Main class for ProxyMessages.
 */
@Plugin(id = "proxymessages", name = "ProxyMessages", version = "3.1.0",
    description = "A message system for servers to interact over a proxy.", 
    authors = {"BlackDiamond"})
public class ProxyMessages {

    private final ProxyServer server;
    private final Logger logger;
    private final Metrics.Factory metricsFactory;

    @DataDirectory
    private final Path dataDirectory;

    private Path database;
    private List<String> colorMap;

    private MessageUtil messageUtil;
    private DiscordUtil discordUtil;

    private boolean discordEnabled;

    private boolean discordChatSync;

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
    
    private boolean globalMessages;

    private List<String> joinMessageOptions;
    
    private List<String> leaveMessageOptions;

    private List<String> switchMessageOptions;

    private String globalMessagePrefix;

    private boolean globalMessageDefault;

    private HashMap<UUID, Boolean> playersGlobalChat;

    public static final MinecraftChannelIdentifier IDENTIFIER = MinecraftChannelIdentifier.from("proxymessages:main");

    /**
     * Constructor, initializes the logger and the proxy server.
     */
    @Inject
    public ProxyMessages(ProxyServer server, Logger logger, Metrics.Factory metricsFactory, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.metricsFactory = metricsFactory;
        this.dataDirectory = dataDirectory;

        messageUtil = new MessageUtil(this);

        playersGlobalChat = new HashMap<UUID, Boolean>();

        logger.info("Thank you for using ProxyMessages");
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) throws IOException {

        server.getChannelRegistrar().register(IDENTIFIER);

        initialize(0);

    }

    @Subscribe void onProxyShutdown(ProxyShutdownEvent event) throws IOException {

        OutputStream out = Files.newOutputStream(database);

        for (String string : colorMap) {
            out.write((string + "\n").getBytes());
        }

        out.close();


        if (discordUtil != null)
            discordUtil.proxyOffline();

    }

    public void initialize(int dummy) throws IOException {

        int pluginID = 25855;
        Metrics metrics = metricsFactory.make(this, pluginID);

        if (Files.notExists(dataDirectory)) {
            Files.createDirectory(dataDirectory);
        }
        final Path config = dataDirectory.resolve("config.yml");
        if (Files.notExists(config)) {
            try (InputStream stream = this.getClass().getClassLoader().getResourceAsStream("config.yml")) {
                Files.copy(stream, config);
            }
        }
        
        boolean newFile = false;
        database = dataDirectory.resolve("database.txt");
        if (Files.notExists(database)) {
            newFile = true;
            try (InputStream dbstream = this.getClass().getClassLoader().getResourceAsStream("database.txt")) {
                Files.copy(dbstream, database);
            }
        }

        colorMap = Files.readAllLines(database);

        final YamlConfigurationLoader loader = YamlConfigurationLoader.builder().path(config).build();
        final CommentedConfigurationNode root = loader.load();

        // adds the default color to the color map
        if (newFile) colorMap.add(0, "default " + root.node("default-player-color").getString());

        globalJoin = root.node("global-network-join").getBoolean();

        globalLeave = root.node("global-network-leave").getBoolean();

        globalSwitch = root.node("global-network-switch").getBoolean();

        globalMessages = root.node("global-messages").getBoolean();
        
        joinMessageOptions = root.node("join-message-options").getList(String.class);
        leaveMessageOptions = root.node("leave-message-options").getList(String.class);
        switchMessageOptions = root.node("switch-message-options").getList(String.class);


        CommentedConfigurationNode discordOptions = root.node("discord");

        discordEnabled = discordOptions.node("enabled").getBoolean();
            
        if (discordEnabled) {

            discordChatSync = discordOptions.node("text-configuration", "player-chat-sync", "enabled").getBoolean();

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

        // initialize command stuff
        CommandManager commandManager = server.getCommandManager();
        
        CommandMeta reloadCommandMeta = commandManager.metaBuilder("pmReload")
            .aliases("reloadPM")
            .build();

        SimpleCommand reloadCommand = new Reload(
            arg0 -> {
                try {initialize(arg0);}
                catch (IOException e) {e.printStackTrace();}
            }
        );

        CommandMeta setColorCommandMeta = commandManager.metaBuilder("set-color")
            .aliases("setColor")
            .build();

        SimpleCommand setColorCommand = new SetColor(this);

        commandManager.register(reloadCommandMeta, reloadCommand);
        commandManager.register(setColorCommandMeta, setColorCommand);

        // optional global messages
        if (globalMessages) {
        
            globalMessagePrefix = root.node("global-message-prefix").getString();

            globalMessageDefault = root.node("global-message-default").getBoolean();
    

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

        if (event.getPreviousServer() != null && !globalSwitch) return;

        if (event.getPreviousServer() == null && !globalJoin) return;

        Player player = event.getPlayer();
        
        boolean previousServerNull = event.getPreviousServer() == null;

        String message;

        if (previousServerNull) {
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
            event.getPlayer().getUniqueId(),
            false
        );

        if (resourcePackEnabled && previousServerNull && !resourcePackExcept.contains(event.getPlayer().getCurrentServer().get().getServerInfo().getName())) {
            event.getPlayer().sendResourcePackOffer(resourcePack);
        }

        if (previousServerNull && globalMessages) playersGlobalChat.put(event.getPlayer().getUniqueId(), globalMessageDefault);

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
            (globalMessages ? globalMessagePrefix : discordUtil.getPlayerMessagePrefix()) + event.getMessage()
        );

        if (discordEnabled && discordChatSync) discordUtil.sendMessage(message.getString(), serverName);

        // handles global messages
        if (globalMessages && playersGlobalChat.get(event.getPlayer().getUniqueId())) sendMessage(message, event.getPlayer().getUniqueId(), true);

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
            globalMessagePrefix + messageData
        );

        sendMessageToServer(message, serverName);

    }


    private void sendMessage(MessageReturns message, UUID uuid, boolean exceptPlayerServer) {
        for (RegisteredServer srvr : server.getAllServers()) {
            if (srvr.getPlayersConnected().isEmpty()) continue; 
            if (exceptPlayerServer && srvr.getPlayersConnected().contains(server.getPlayer(uuid).get())) continue;

            srvr.sendMessage(message.getComponent());
        }
        if (discordUtil != null && !exceptPlayerServer) discordUtil.playerNotification(message, uuid);
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

    public List<String> getColorMap() {
        return colorMap;
    }

    public String getColor(String playerName) {
        for (String color : colorMap) {
            if (color.substring(0, color.indexOf(" ")).equals(playerName)){
                return color.substring(color.indexOf(" ") + 1);
            } 
        }
        return getColor("default");
    }

    public void appendColorMap(String colorPair) {
        for (int i = 0; i < colorMap.size(); i++) {
            if (colorMap.get(i).substring(0, colorMap.get(i).indexOf(" "))
                .equals(colorPair.substring(0, colorPair.indexOf(" ")))) {
                colorMap.remove(i);
            }
        }
        colorMap.add(colorPair);
    }

    public String getGlobalMessagePrefix() {
        return globalMessagePrefix;
    }

    public boolean getGlobalMessages() {
        return globalMessages;
    }


}
