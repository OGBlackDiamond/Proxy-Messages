package dev.ogblackdiamond.proxymessages.util;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.proxy.Player;

import dev.ogblackdiamond.proxymessages.ProxyMessages;
import dev.ogblackdiamond.proxymessages.config.ConfigUtil;

public class PapiUtil extends MessageUtil {

    ProxyMessages proxyMessages;

    private Map<UUID, CompletableFuture<String>> pending = new ConcurrentHashMap<>();

    public PapiUtil(ProxyMessages proxyMessages, ConfigUtil configUtil) {
        super(configUtil);

        this.proxyMessages = proxyMessages;
    }

    @Override
    public CompletableFuture<MessageReturns> compileFormattedMessage(MessageType type, Player player, String previousServer,
            String newServer, String ogString, boolean fromDiscord) {

        return resolveFutureString(ogString, player).thenCompose(
                resolved -> super.compileFormattedMessage(type, player, previousServer, newServer, resolved, fromDiscord));

    }

    private CompletableFuture<String> resolveFutureString(String ogString, Player player) {
        CompletableFuture<String> future = new CompletableFuture<>();
        future.completeOnTimeout(ogString, 5, TimeUnit.SECONDS);

        pending.put(player.getUniqueId(), future);

        // build the message
        ByteArrayDataOutput data = ByteStreams.newDataOutput();

        // write the og string to be processed
        data.writeUTF(ogString);

        player.sendPluginMessage(ProxyMessages.PAPI_IDENTIFIER, data.toByteArray());

        return future;
    }

    public CompletableFuture<String> getFuture(UUID uniqueID) {
        return pending.remove(uniqueID);
    }
}
