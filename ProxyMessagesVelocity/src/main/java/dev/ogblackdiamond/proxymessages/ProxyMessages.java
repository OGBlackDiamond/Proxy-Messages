package dev.ogblackdiamond.proxymessages;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
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

    /**
     * Constructor, initializes the logger and the proxy server.
     */
    @Inject
    public ProxyMessages(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;

        logger.info("Booting ProxyMessages.");
    }
}
