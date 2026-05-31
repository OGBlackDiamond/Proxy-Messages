package dev.ogblackdiamond.proxymessages.mixin;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import dev.ogblackdiamond.proxymessages.ProxyMessagesNeoForge;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

/**
 * Intercepts incoming custom payload packets (C2S direction) so the mod can
 * receive plugin messages sent by the Velocity proxy to this backend server.
 *
 * Why a Mixin here?
 * NeoForge 47.x's SimpleChannel API performs mod-version negotiation that
 * makes it unsuitable for raw proxy communication.  Injecting directly into
 * handleCustomPayload gives us the raw bytes before NeoForge's channel system
 * gets involved, exactly mirroring Paper's PluginMessageListener.
 *
 * Channels handled:
 *   proxymessages:main  — proxy sends writeBoolean(globalMessages)
 *   proxymessages:papi  — proxy sends UUID lsb/msb + placeholder string
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketHandlerMixin {

    @Shadow
    public ServerPlayer player;

    @Inject(method = "handleCustomPayload", at = @At("HEAD"), cancellable = true)
    private void onHandleCustomPayload(ServerboundCustomPayloadPacket packet,
                                       CallbackInfo ci) {
        ResourceLocation channel = packet.identifier();

        if (ProxyMessagesNeoForge.MAIN_CHANNEL.equals(channel)) {
            handleMainChannel(packet.data());
            ci.cancel();
        } else if (ProxyMessagesNeoForge.PAPI_CHANNEL.equals(channel)) {
            handlePapiChannel(packet.data());
            ci.cancel();
        }
    }

    /**
     * proxymessages:main (proxy → backend):
     * Reads the single boolean the Velocity plugin writes in onPlayerMessage
     * and updates the shared cancelPlayerMessages flag.
     */
    private void handleMainChannel(FriendlyByteBuf data) {
        byte[] bytes = new byte[data.readableBytes()];
        data.readBytes(bytes);
        ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
        ProxyMessagesNeoForge.cancelPlayerMessages = in.readBoolean();
    }

    /**
     * proxymessages:papi (proxy → backend):
     * Reads UUID + placeholder string, resolves via PlaceholderAPI, and
     * replies on the same channel.  PlaceholderAPI is not available on
     * NeoForge, so the placeholder is echoed back unresolved.
     *
     * To avoid broken placeholders in messages set enable-papi: false in the
     * Velocity config when running NeoForge backends.
     */
    private void handlePapiChannel(FriendlyByteBuf data) {
        byte[] bytes = new byte[data.readableBytes()];
        data.readBytes(bytes);
        ByteArrayDataInput in = ByteStreams.newDataInput(bytes);

        long lsb = in.readLong();
        long msb = in.readLong();
        String placeholder = in.readUTF();

        // Echo back unresolved — no PAPI on NeoForge.
        ProxyMessagesNeoForge.sendToProxy(player, ProxyMessagesNeoForge.PAPI_CHANNEL,
                new UUID(msb, lsb), placeholder);
    }
}
