package dev.ogblackdiamond.proxymessages.mixin;

import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Suppresses vanilla join and leave system messages.
 *
 * Paper exposes this as event.joinMessage(null) / event.quitMessage(null).
 * NeoForge has no equivalent API hook, so we redirect the specific calls to
 * PlayerList#broadcastSystemMessage inside placeNewPlayer and remove.
 *
 * @Redirect replaces a single call-site, leaving the rest of each method
 * intact.  ordinal = 0 targets the first (and only) broadcastSystemMessage
 * call in each method, which is always the join/leave notification.
 *
 * The Velocity plugin broadcasts its own formatted versions of these messages,
 * so suppressing the vanilla ones avoids duplicates on the network.
 */
@Mixin(PlayerList.class)
public abstract class PlayerListMixin {

    /**
     * Suppresses the vanilla join message broadcast inside placeNewPlayer.
     * The redirected method body is intentionally empty (no-op).
     */
    @Redirect(
        method = "placeNewPlayer",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/players/PlayerList;"
                   + "broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V",
            ordinal = 0
        )
    )
    private void suppressJoinMessage(PlayerList instance,
                                     Component message, boolean overlay,
                                     Connection connection, ServerPlayer player) {
        // no-op — vanilla join message suppressed
    }

    /**
     * Suppresses the vanilla leave message broadcast inside remove.
     * The redirected method body is intentionally empty (no-op).
     */
    @Redirect(
        method = "remove",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/players/PlayerList;"
                   + "broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V",
            ordinal = 0
        )
    )
    private void suppressLeaveMessage(PlayerList instance,
                                      Component message, boolean overlay,
                                      ServerPlayer player) {
        // no-op — vanilla leave message suppressed
    }
}
