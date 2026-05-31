package dev.ogblackdiamond.proxymessages;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import io.netty.buffer.Unpooled;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.ServerChatEvent;

import java.util.UUID;

/**
 * NeoForge companion mod for ProxyMessages.
 *
 * Replicates the behaviour of ProxyMessagesPaper for NeoForge (1.20.1) servers:
 *   - Suppresses vanilla join / leave messages via Mixin (PlayerListMixin).
 *   - Intercepts player chat via ServerChatEvent, cancels local broadcast,
 *     and forwards raw text to the Velocity plugin over proxymessages:main.
 *   - Receives the global-messaging toggle from the proxy via Mixin
 *     (ServerGamePacketHandlerMixin) because NeoForge 47.x does not expose
 *     a clean API for raw proxy-channel ingestion.
 *   - Responds to PAPI placeholder requests on proxymessages:papi.
 *     PlaceholderAPI does not exist on NeoForge; the placeholder is echoed
 *     back unresolved.  Set enable-papi: false in the Velocity config.
 *
 * Wire protocol (must stay byte-for-byte compatible with the Velocity plugin):
 *   proxymessages:main  backend → proxy   writeLong(uuid.lsb), writeLong(uuid.msb), writeUTF(message)
 *   proxymessages:main  proxy → backend   writeBoolean(globalMessages)
 *   proxymessages:papi  backend → proxy   writeLong(uuid.lsb), writeLong(uuid.msb), writeUTF(resolved)
 *   proxymessages:papi  proxy → backend   writeLong(uuid.lsb), writeLong(uuid.msb), writeUTF(placeholder)
 */
@Mod(ProxyMessagesNeoForge.MOD_ID)
public class ProxyMessagesNeoForge {

    public static final String MOD_ID = "proxymessages";

    public static final ResourceLocation MAIN_CHANNEL =
            new ResourceLocation(MOD_ID, "main");
    public static final ResourceLocation PAPI_CHANNEL =
            new ResourceLocation(MOD_ID, "papi");

    /**
     * Toggled by the proxy on each chat event (via proxymessages:main).
     * Accessed from ServerGamePacketHandlerMixin, so it must be public static.
     */
    public static volatile boolean cancelPlayerMessages = true;

    public ProxyMessagesNeoForge(IEventBus modEventBus) {
        NeoForge.EVENT_BUS.register(this);
    }

    // -------------------------------------------------------------------------
    // Chat event
    // -------------------------------------------------------------------------

    /**
     * Intercepts chat on the server game event bus.
     *
     * When the proxy has enabled global messaging (cancelPlayerMessages == true),
     * the raw text is forwarded over proxymessages:main and the local broadcast
     * is cancelled.  This mirrors onAsyncPlayerChat in ProxyMessagesPaper.
     *
     * NOTE: event.getRawText() returns the original unsigned string the player
     * typed.  event.getMessage() is the formatted display Component and would
     * include the player name — do not use it here.
     */
    @SubscribeEvent
    public void onServerChat(ServerChatEvent event) {
        if (!cancelPlayerMessages) return;

        sendToProxy(event.getPlayer(), MAIN_CHANNEL,
                event.getPlayer().getUUID(), event.getRawText());
        event.setCanceled(true);
    }

    // -------------------------------------------------------------------------
    // Helpers (called from Mixins too, so package-visible static)
    // -------------------------------------------------------------------------

    /**
     * Sends a plugin message from this backend server to the Velocity proxy.
     *
     * ClientboundCustomPayloadPacket travels in the S2C direction.  Velocity
     * intercepts it before it reaches the real Minecraft client, identical to
     * how it handles Paper's Player#sendPluginMessage.
     *
     * DataOutputStream / ByteArrayDataOutput write longs and UTF in big-endian
     * format.  FriendlyByteBuf also writes longs big-endian, but its
     * writeUtf uses a VarInt length prefix which is incompatible with
     * DataInputStream#readUTF used on the Velocity side.  Use Guava's
     * ByteArrayDataOutput to build the payload, then wrap it.
     */
    public static void sendToProxy(ServerPlayer player, ResourceLocation channel,
                                   UUID uuid, String text) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeLong(uuid.getLeastSignificantBits());
        out.writeLong(uuid.getMostSignificantBits());
        out.writeUTF(text);
        FriendlyByteBuf buf =
                new FriendlyByteBuf(Unpooled.wrappedBuffer(out.toByteArray()));
        player.connection.send(new ClientboundCustomPayloadPacket(channel, buf));
    }
}
