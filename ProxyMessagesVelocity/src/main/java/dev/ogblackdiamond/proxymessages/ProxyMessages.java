package dev.ogblackdiamond.proxymessages;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;

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
        logger.warn("Testing warnings for ProxyMessages");
        logger.error("Testing errors for ProxyMessages");
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {

       // server.getEventManager().register(this, new PluginListener()); 
    }
}
