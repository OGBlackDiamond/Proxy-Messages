package dev.ogblackdiamond.proxymessages;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Identifier;

import java.util.UUID;

/**
 * Fabric companion mod for ProxyMessages.
 *
 * Replicates the behaviour of ProxyMessagesPaper for Fabric servers:
 *   - Suppresses vanilla join / leave system messages so the Velocity plugin
 *     can broadcast its own formatted versions.
 *   - Intercepts player chat, cancels the local broadcast, and forwards the
 *     raw text to the Velocity plugin over proxymessages:main.
 *   - Listens for the toggle packet sent by the Velocity plugin on
 *     proxymessages:main so it knows whether global messaging is enabled.
 *   - Responds to PAPI placeholder requests on proxymessages:papi.
 *     PlaceholderAPI does not exist on Fabric; the placeholder is echoed back
 *     unresolved.  Set enable-papi: false in the Velocity config.
 *
 * Wire protocol (must stay byte-for-byte compatible with the Velocity plugin):
 *   proxymessages:main  backend → proxy   writeLong(uuid.lsb), writeLong(uuid.msb), writeUTF(message)
 *   proxymessages:main  proxy → backend   writeBoolean(globalMessages)
 *   proxymessages:papi  backend → proxy   writeLong(uuid.lsb), writeLong(uuid.msb), writeUTF(resolved)
 *   proxymessages:papi  proxy → backend   writeLong(uuid.lsb), writeLong(uuid.msb), writeUTF(placeholder)
 */
public class ProxyMessagesFabric implements ModInitializer {

    public static final Identifier MAIN_CHANNEL = new Identifier("proxymessages", "main");
    public static final Identifier PAPI_CHANNEL = new Identifier("proxymessages", "papi");

    /**
     * Mirrors cancelPlayerMessages in ProxyMessagesPaper.
     * Toggled by the proxy each time a player sends a chat message.
     */
    private volatile boolean cancelPlayerMessages = true;

    @Override
    public void onInitialize() {
        registerProxyChannelReceivers();
        registerMessageEvents();
    }

    // -------------------------------------------------------------------------
    // Incoming plugin messages (proxy → backend)
    // -------------------------------------------------------------------------

    private void registerProxyChannelReceivers() {

        // proxymessages:main — proxy sends a single boolean: whether global
        // messaging is enabled.  This mirrors onPluginMessageReceived in Paper.
        ServerPlayNetworking.registerGlobalReceiver(MAIN_CHANNEL,
                (server, player, handler, buf, responseSender) -> {
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
            ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
            cancelPlayerMessages = in.readBoolean();
        });

        // proxymessages:papi — proxy requests placeholder resolution.
        // No PlaceholderAPI on Fabric: echo back unresolved.
        ServerPlayNetworking.registerGlobalReceiver(PAPI_CHANNEL,
                (server, player, handler, buf, responseSender) -> {
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
            ByteArrayDataInput in = ByteStreams.newDataInput(bytes);

            long lsb = in.readLong();
            long msb = in.readLong();
            String placeholder = in.readUTF();

            sendToProxy(player, PAPI_CHANNEL, new UUID(msb, lsb), placeholder);
        });
    }

    // -------------------------------------------------------------------------
    // Outgoing plugin messages (backend → proxy) and event hooks
    // -------------------------------------------------------------------------

    private void registerMessageEvents() {

        // Suppress vanilla join / leave messages.
        // These are "game messages" in Fabric API terminology.
        // The Velocity plugin broadcasts its own formatted versions instead.
        ServerMessageEvents.ALLOW_GAME_MESSAGE.register((message, overlay) -> {
            if (message.getContent() instanceof TranslatableTextContent tc) {
                String key = tc.getKey();
                return !key.equals("multiplayer.player.joined")
                        && !key.equals("multiplayer.player.joined.renamed")
                        && !key.equals("multiplayer.player.left");
            }
            return true;
        });

        // Intercept player chat.  When cancelPlayerMessages is true (the proxy
        // has enabled global messaging), forward the raw message to the proxy
        // and cancel the local broadcast.  This mirrors onAsyncPlayerChat in Paper.
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
            if (!cancelPlayerMessages) return true;
            sendToProxy(sender, MAIN_CHANNEL, sender.getUuid(), message.getSignedContent());
            return false; // cancel local broadcast
        });
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Sends a plugin message from this backend server to the Velocity proxy.
     * Uses the same binary layout as ProxyMessagesPaper so the Velocity plugin
     * can read it without modification.
     *
     * ServerPlayNetworking.send dispatches a ClientboundCustomPayloadPacket
     * (S2C direction).  Velocity intercepts this before it reaches the real
     * Minecraft client, exactly as it does for Paper's sendPluginMessage.
     */
    private static void sendToProxy(ServerPlayerEntity player, Identifier channel,
                                    UUID uuid, String text) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeLong(uuid.getLeastSignificantBits());
        out.writeLong(uuid.getMostSignificantBits());
        out.writeUTF(text);
        PacketByteBuf buf = PacketByteBufs.wrappedBuffer(out.toByteArray());
        ServerPlayNetworking.send(player, channel, buf);
    }
}
